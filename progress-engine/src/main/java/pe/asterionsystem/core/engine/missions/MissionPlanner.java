package pe.asterionsystem.core.engine.missions;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import pe.asterionsystem.core.engine.exercises.ExerciseCatalog;
import pe.asterionsystem.core.engine.exercises.ExerciseCategoryMapper;
import pe.asterionsystem.core.engine.exercises.ExerciseFamily;
import pe.asterionsystem.core.engine.exercises.ExerciseType;
import pe.asterionsystem.core.engine.exercises.ExerciseVariant;
import pe.asterionsystem.core.engine.progression.CategoryEffortPolicy;

/**
 * Deterministic mission proposals built exclusively from the versioned
 * exercise catalog. This is a proposal engine, NOT exercise prescription:
 * it never sets repetition targets, weight, intensity, penalties, or
 * automatically writes completed activities to the tracking database.
 *
 * The Monarca theme is optional. Its five candidates span a five-day cycle,
 * but must be explicitly activated by the user through a separate store.
 */
public final class MissionPlanner {
    public static final String DAILY = "daily";
    public static final String MONARCH = "monarch";
    private static final String[] WEEK_CATEGORIES = {
            "tren_superior", "tronco", "piernas", "movilidad", "cardio", "descanso"
    };

    public static final class Profile {
        public final String experience;
        public final String goal;
        public final List<Integer> weekly;
        public final Set<String> equipment;
        public final int dailyLimit;

        public Profile(String experience, String goal, List<Integer> weekly,
                       Set<String> equipment, int dailyLimit) {
            this.experience = experience == null ? "principiante" : experience;
            this.goal = goal == null ? "bienestar" : goal;
            if (weekly == null || weekly.size() != 7) {
                throw new IllegalArgumentException("Se requieren los siete días");
            }
            for (int index : weekly) {
                if (index < 0 || index > 5) {
                    throw new IllegalArgumentException("Categoría semanal inválida");
                }
            }
            this.weekly = Collections.unmodifiableList(new ArrayList<>(weekly));
            this.equipment = Collections.unmodifiableSet(
                    new HashSet<>(Objects.requireNonNull(equipment, "equipment")));
            this.dailyLimit = Math.max(1, Math.min(7, dailyLimit));
        }
    }

    public static final class Proposal {
        private final String kind;
        private final LocalDate start;
        private final LocalDate untilExclusive;
        private final List<ExerciseVariant> candidates;
        private final boolean requiresConsent;
        private final String explanation;

        private Proposal(String kind, LocalDate start, LocalDate untilExclusive,
                         List<ExerciseVariant> candidates, boolean requiresConsent,
                         String explanation) {
            this.kind = kind;
            this.start = start;
            this.untilExclusive = untilExclusive;
            this.candidates = Collections.unmodifiableList(new ArrayList<>(candidates));
            this.requiresConsent = requiresConsent;
            this.explanation = explanation;
        }

        public String kind() { return kind; }
        public LocalDate start() { return start; }
        public LocalDate untilExclusive() { return untilExclusive; }
        public List<ExerciseVariant> candidates() { return candidates; }
        public boolean requiresConsent() { return requiresConsent; }
        public String explanation() { return explanation; }
        public boolean empty() { return candidates.isEmpty(); }
    }

    private final ExerciseCatalog catalog;
    private final Map<String, String> familyForType = new HashMap<>();
    private final Map<String, String> categoryForType = new HashMap<>();

    public MissionPlanner(ExerciseCatalog catalog) {
        this.catalog = Objects.requireNonNull(catalog, "catalog");
        Map<String, ExerciseFamily> families = new HashMap<>();
        for (ExerciseFamily family : catalog.families()) families.put(family.id(), family);
        for (ExerciseType type : catalog.types()) {
            familyForType.put(type.id(), type.familyId());
            categoryForType.put(type.id(), ExerciseCategoryMapper.primaryCategory(
                    families.get(type.familyId()).category()));
        }
    }

    /** Recomputes from the local date; no internet, clock timer or background service. */
    public Proposal daily(Profile profile, LocalDate today) {
        Objects.requireNonNull(profile, "profile");
        Objects.requireNonNull(today, "today");
        int index = today.getDayOfWeek().getValue() - 1;
        int dayCategory = profile.weekly.get(index);
        if (dayCategory == 5) {
            return new Proposal(DAILY, today, today.plusDays(1),
                    Collections.emptyList(), false,
                    "Día de descanso configurado en el perfil.");
        }
        String selectedCategory = WEEK_CATEGORIES[dayCategory];
        List<ExerciseVariant> matching = candidates(profile, selectedCategory);
        List<ExerciseVariant> chosen = takeDiverse(matching, profile.dailyLimit, today.toEpochDay());
        return new Proposal(DAILY, today, today.plusDays(1), chosen, false,
                chosen.isEmpty()
                ? "No hay alternativas compatibles con el equipo y la categoría de este día."
                : "Propuesta del día según tu calendario, equipamiento y catálogo local.");
    }

    /**
     * Autonomous category-aware mission selection. Only the category scheduled
     * for today can affect this proposal. A low RPE rotates technically
     * equivalent variants within the SAME movement type, never to a harder
     * difficulty or a higher repetition target. Medium RPE preserves the
     * previous candidates if still compatible. High RPE reduces suggestions
     * and caps technical complexity.
     *
     * previousVariantIds are the actually displayed candidates from the
     * preceding session of this category, not another category or the day
     * before by default.
     */
    public Proposal daily(Profile profile, LocalDate today,
                          CategoryEffortPolicy.Decision decision,
                          List<String> previousVariantIds) {
        return daily(profile,today,decision,previousVariantIds,true);
    }

    /**
     * On-device weekly evidence controls whether low-RPE novelty is enabled.
     * Lower suggestions for high effort remain active independently.
     */
    public Proposal daily(Profile profile, LocalDate today,
                          CategoryEffortPolicy.Decision decision,
                          List<String> previousVariantIds,
                          boolean enableEquivalentRotation) {
        Objects.requireNonNull(profile, "profile");
        Objects.requireNonNull(today, "today");
        Objects.requireNonNull(decision, "decision");
        Objects.requireNonNull(previousVariantIds, "previousVariantIds");
        int category=profile.weekly.get(today.getDayOfWeek().getValue()-1);
        if(category!=decision.category) {
            throw new IllegalArgumentException("La valoración no corresponde a la categoría del día");
        }
        if(category==CategoryEffortPolicy.REST) {
            return daily(profile,today);
        }
        int limit=Math.min(profile.dailyLimit,Math.max(0,decision.suggestedActivities));
        if(limit==0) {
            return new Proposal(DAILY,today,today.plusDays(1),
                    Collections.emptyList(),false,
                    "La categoría está en pausa. Se respeta el descanso.");
        }
        String categoryName=WEEK_CATEGORIES[category];
        List<ExerciseVariant> eligible=candidates(profile,categoryName);
        if(decision.preferSimplerTechnique) {
            eligible.removeIf(v->v.difficulty()>1);
        }
        if(eligible.isEmpty()) {
            return new Proposal(DAILY,today,today.plusDays(1),
                    Collections.emptyList(),false,
                    "No hay opciones compatibles seguras para la categoría actual.");
        }
        List<ExerciseVariant> chosen=new ArrayList<>();
        Set<String> usedTypes=new HashSet<>();
        for(String priorId:previousVariantIds) {
            if(chosen.size()==limit)break;
            ExerciseVariant prior=catalog.variant(priorId).orElse(null);
            if(prior==null)continue;
            ExerciseVariant selected=null;
            if(enableEquivalentRotation
                    && decision.guidance==CategoryEffortPolicy.Guidance.REVIEW_OPTIONAL) {
                // Only when repeated completed low-effort sessions were seen
                // at the seven-day checkpoint: same type and technical level.
                for(ExerciseVariant option:eligible) {
                    if(option.typeId().equals(prior.typeId())
                            && option.difficulty()==prior.difficulty()
                            && !option.id().equals(prior.id())
                            && !usedTypes.contains(option.typeId())) {
                        selected=option;
                        break;
                    }
                }
            }
            if(selected==null && eligible.contains(prior)
                    && !usedTypes.contains(prior.typeId())) {
                selected=prior;
            }
            if(selected!=null && usedTypes.add(selected.typeId()))chosen.add(selected);
        }
        List<ExerciseVariant> ranked=new ArrayList<>(eligible);
        if(decision.preferSimplerTechnique) {
            ranked.sort(Comparator.comparingInt(ExerciseVariant::difficulty)
                    .thenComparing(ExerciseVariant::id));
        } else {
            ranked=takeDiverse(ranked,ranked.size(),today.toEpochDay());
        }
        for(ExerciseVariant option:ranked) {
            if(chosen.size()==limit)break;
            if(usedTypes.add(option.typeId()))chosen.add(option);
        }
        // If the catalog has fewer distinct types than the requested count,
        // allow a distinct variant, never a duplicated candidate.
        Set<String> usedIds=new HashSet<>();
        for(ExerciseVariant item:chosen)usedIds.add(item.id());
        for(ExerciseVariant option:ranked) {
            if(chosen.size()==limit)break;
            if(usedIds.add(option.id()))chosen.add(option);
        }
        return new Proposal(DAILY,today,today.plusDays(1),chosen,false,
                decision.explanation+" Selección realizada por el motor a partir "
                +"del perfil y el catálogo sin modificar las repeticiones.");
    }

    /** Five possible activities for five days, not five compulsory daily workouts. */
    public Proposal monarch(Profile profile, LocalDate starts) {
        Objects.requireNonNull(profile, "profile");
        Objects.requireNonNull(starts, "starts");
        Set<String> scheduledCategories = new LinkedHashSet<>();
        for (int index : profile.weekly) {
            if (index != 5) scheduledCategories.add(WEEK_CATEGORIES[index]);
        }
        if (scheduledCategories.isEmpty()) {
            return new Proposal(MONARCH, starts, starts.plusDays(5),
                    Collections.emptyList(), true,
                    "El calendario contiene solo descansos; no se propone desafío.");
        }
        // Profile objective influences the order of eligible categories,
        // never the numerical workload or technical difficulty.
        List<String> ranked = new ArrayList<>(scheduledCategories);
        ranked.sort(Comparator.comparingInt(category -> priority(profile.goal, category)));
        List<ExerciseVariant> pool = new ArrayList<>();
        for (String category : ranked) {
            pool.addAll(candidates(profile, category));
        }
        List<ExerciseVariant> chosen = takeDiverse(pool, 5, starts.toEpochDay() / 5L);
        return new Proposal(MONARCH, starts, starts.plusDays(5), chosen, true,
                chosen.size() == 5
                ? "Cinco opciones para un período de cinco días; requieren autorización explícita. No hay cuotas obligatorias."
                : "Hay menos de cinco opciones compatibles; no se puede activar la misión.");
    }

    private int priority(String goal, String category) {
        if ("movilidad".equals(goal) && "movilidad".equals(category)) return 0;
        if ("resistencia".equals(goal) && "cardio".equals(category)) return 0;
        if ("fuerza".equals(goal) &&
                ("tren_superior".equals(category) || "piernas".equals(category))) return 0;
        if ("bienestar".equals(goal) &&
                ("movilidad".equals(category) || "cardio".equals(category))) return 0;
        return 1;
    }

    private List<ExerciseVariant> candidates(Profile profile, String category) {
        List<ExerciseVariant> result = new ArrayList<>();
        int maxTechnicalLevel = "principiante".equals(profile.experience) ? 1 : 2;
        for (ExerciseVariant variant : catalog.variants()) {
            if (!category.equals(categoryForType.get(variant.typeId()))) continue;
            if (!variant.eligibleForAutomaticSuggestion()
                    || variant.difficulty() > maxTechnicalLevel) continue;
            if (!profile.equipment.containsAll(variant.equipment())) continue;
            result.add(variant);
        }
        result.sort(Comparator.comparing(ExerciseVariant::id));
        return result;
    }

    private List<ExerciseVariant> takeDiverse(List<ExerciseVariant> pool, int count, long seed) {
        if (pool.isEmpty() || count <= 0) return Collections.emptyList();
        int start = (int)Math.floorMod(seed, pool.size());
        ArrayList<ExerciseVariant> selected = new ArrayList<>();
        Set<String> familyUsed = new HashSet<>();
        // First pass: distribute across families, avoiding duplicate movements.
        for (int i=0; i<pool.size() && selected.size()<count; i++) {
            ExerciseVariant variant = pool.get((start + i) % pool.size());
            String familyId = familyForType.get(variant.typeId());
            if (familyUsed.add(familyId)) selected.add(variant);
        }
        // Fallback: different exercise types, still with safe technical eligibility.
        Set<String> typeUsed = new HashSet<>();
        for (ExerciseVariant picked : selected) typeUsed.add(picked.typeId());
        for (int i=0; i<pool.size() && selected.size()<count; i++) {
            ExerciseVariant variant = pool.get((start + i) % pool.size());
            if (typeUsed.add(variant.typeId())) selected.add(variant);
        }
        return selected;
    }
}

package pe.asterionsystem.core.engine.missions;

import java.time.LocalDate;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Random;
import pe.asterionsystem.core.engine.exercises.ExerciseIntensity;
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
import pe.asterionsystem.core.engine.exercises.ExerciseMuscleFocus;
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
        public final String intensityPreference;
        private final String additionalPreferences;

        public Profile(String experience, String goal, List<Integer> weekly,
                       Set<String> equipment, int dailyLimit) {
            this(experience,goal,weekly,equipment,dailyLimit,"moderada","");
        }

        public Profile(String experience, String goal, List<Integer> weekly,
                       Set<String> equipment, int dailyLimit, String intensityPreference) {
            this(experience,goal,weekly,equipment,dailyLimit,intensityPreference,"");
        }

        public Profile(String experience, String goal, List<Integer> weekly,
                       Set<String> equipment, int dailyLimit, String intensityPreference,
                       String additionalPreferences) {
            this.additionalPreferences=additionalPreferences==null?"":additionalPreferences;
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
            this.intensityPreference = "baja".equals(intensityPreference)
                    ? "baja" : "moderada";
        }

        /** Conservative adaptation retaining the original profile settings. */
        public Profile adapted(String experienceForToday,int limitForToday) {
            return new Profile(experienceForToday,goal,weekly,equipment,
                    limitForToday,intensityPreference,additionalPreferences);
        }

        /**
         * Stable identity of mission-affecting preferences only.
         * Cosmetic theme, user name and body measurements cannot reshuffle.
         */
        public String missionSignature() {
            ArrayList<String> gear=new ArrayList<>(equipment);
            Collections.sort(gear);
            String canonical=experience+"|"+goal+"|"+weekly.toString()
                    +"|"+String.join(",",gear)+"|"+dailyLimit
                    +"|"+intensityPreference+"|"+additionalPreferences;
            return digestHex(canonical);
        }
    }

    private static byte[] digest(String input) {
        try {
            return MessageDigest.getInstance("SHA-256").digest(
                    input.getBytes(StandardCharsets.UTF_8));
        } catch(NoSuchAlgorithmException impossible) {
            throw new IllegalStateException("SHA-256 unavailable", impossible);
        }
    }

    private static String digestHex(String input) {
        byte[] bytes=digest(input);
        StringBuilder hex=new StringBuilder(64);
        for(byte value:bytes)hex.append(String.format(java.util.Locale.ROOT,
                "%02x", value & 0xff));
        return hex.toString();
    }

    /** Stable pseudo-random choice for a particular local day and profile. */
    private static long daySeed(Profile profile,LocalDate date) {
        byte[] bytes=digest("asterion.daily.v2|"+date+"|"+profile.missionSignature());
        long seed=0L;
        for(int i=0;i<8;i++)seed=(seed<<8)|(bytes[i]&0xffL);
        return seed;
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
        List<ExerciseVariant> chosen = takeLevelMix(matching, profile.dailyLimit,
                daySeed(profile,today),Collections.emptySet(),profile.experience,0,
                Collections.emptyList());
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
        return daily(profile,today,decision,previousVariantIds,
                enableEquivalentRotation,Collections.emptyList(),0);
    }

    /**
     * Prefer variants not completed during the previous 14 days in this
     * category, but reuse eligible prior variants when alternatives run out.
     * Previously displayed (unpinned) proposals do not become progress.
     */
    public Proposal daily(Profile profile, LocalDate today,
                          CategoryEffortPolicy.Decision decision,
                          List<String> previousVariantIds,
                          boolean enableEquivalentRotation,
                          List<String> recentCompletedIds) {
        return daily(profile,today,decision,previousVariantIds,
                enableEquivalentRotation,recentCompletedIds,0);
    }

    /** Experience-specific mix phase is derived from completed category evidence. */
    public Proposal daily(Profile profile, LocalDate today,
                          CategoryEffortPolicy.Decision decision,
                          List<String> previousVariantIds,
                          boolean enableEquivalentRotation,
                          List<String> recentCompletedIds,int mixPhase) {
        Objects.requireNonNull(profile, "profile");
        Objects.requireNonNull(today, "today");
        Objects.requireNonNull(decision, "decision");
        Objects.requireNonNull(previousVariantIds, "previousVariantIds");
        Objects.requireNonNull(recentCompletedIds, "recentCompletedIds");
        Set<String> recentCompleted=new HashSet<>(recentCompletedIds);
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
        if(MissionDifficultyMixPolicy.mixed(profile.experience)
                && !decision.preferSimplerTechnique) {
            List<ExerciseVariant> preferred=preferredFromRecentSession(
                    eligible,previousVariantIds,decision,
                    enableEquivalentRotation,recentCompleted);
            List<ExerciseVariant> mixed=takeLevelMix(eligible,limit,
                    daySeed(profile,today),recentCompleted,profile.experience,
                    mixPhase,preferred);
            return new Proposal(DAILY,today,today.plusDays(1),mixed,false,
                    decision.explanation+" La mezcla de niveles respeta "
                    +"las opciones compatibles y el historial de la categoría.");
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
                            && !recentCompleted.contains(option.id())
                            && !usedTypes.contains(option.typeId())) {
                        selected=option;
                        break;
                    }
                }
            }
            if(selected==null && eligible.contains(prior)
                    && !recentCompleted.contains(prior.id())
                    && !usedTypes.contains(prior.typeId())) {
                selected=prior;
            }
            if(selected!=null && usedTypes.add(selected.typeId()))chosen.add(selected);
        }
        List<ExerciseVariant> ranked=new ArrayList<>(eligible);
        if(decision.preferSimplerTechnique) {
            ranked.sort(Comparator
                    .comparingInt((ExerciseVariant v)->recentCompleted.contains(v.id())?1:0)
                    .thenComparingInt(ExerciseVariant::difficulty)
                    .thenComparing(ExerciseVariant::id));
        } else {
            ranked=takeDiverse(ranked,ranked.size(),daySeed(profile,today),
                    recentCompleted);
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

    /**
     * Rebuild today's visible proposal solely from a small list of catalog IDs.
     * The app stores no exercise descriptions, targets, training plans or
     * duplicate catalog rows. Its ephemeral pinned IDs expire at midnight or
     * when any mission-affecting profile preference changes.
     */
    public Proposal restoreDaily(Profile profile,LocalDate today,List<String> ids) {
        Objects.requireNonNull(profile,"profile");
        Objects.requireNonNull(today,"today");
        Objects.requireNonNull(ids,"ids");
        int day=profile.weekly.get(today.getDayOfWeek().getValue()-1);
        if(day==5 || ids.isEmpty() || ids.size()>profile.dailyLimit) {
            throw new IllegalArgumentException("Selección diaria no compatible");
        }
        String category=WEEK_CATEGORIES[day];
        List<ExerciseVariant> eligible=candidates(profile,category);
        ArrayList<ExerciseVariant> restored=new ArrayList<>();
        Set<String> unique=new HashSet<>();
        for(String id:ids) {
            if(!unique.add(id))throw new IllegalArgumentException("ID diario repetido");
            ExerciseVariant entry=catalog.variant(id).orElse(null);
            if(entry==null || !eligible.contains(entry)) {
                throw new IllegalArgumentException("Variante diaria incompatible");
            }
            restored.add(entry);
        }
        return new Proposal(DAILY,today,today.plusDays(1),restored,false,
                "Selección diaria estable reconstruida desde la Biblioteca.");
    }

    /** Five optional proposals for five days, never mandatory workouts. */
    public Proposal monarch(Profile profile, LocalDate starts) {
        return monarch(profile,starts,Collections.emptyList());
    }

    /** Deterministic variation across five-day proposals, preferring new IDs. */
    public Proposal monarch(Profile profile, LocalDate starts,
                            List<String> previousCycleIds) {
        Objects.requireNonNull(profile, "profile");
        Objects.requireNonNull(starts, "starts");
        Objects.requireNonNull(previousCycleIds,"previousCycleIds");
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
            // Preserve the established Monarca candidate cap independently
            // of the daily experience-mix stages and consent policy.
            int priorCap="principiante".equals(profile.experience)?1:2;
            for(ExerciseVariant variant:candidates(profile,category)) {
                if(variant.difficulty()<=priorCap)pool.add(variant);
            }
        }
        List<ExerciseVariant> shuffled=takeDiverse(pool,pool.size(),
                monarchSeed(profile,starts),new HashSet<>(previousCycleIds));
        // Prefer different scheduled categories for five independent options.
        // The preceding cycle's IDs are already ranked last when alternatives
        // remain; no intensity, technical cap or equipment filter is bypassed.
        List<ExerciseVariant> chosen=new ArrayList<>();
        Set<String> usedCategories=new HashSet<>();
        Set<String> usedIds=new HashSet<>();
        for(ExerciseVariant variant:shuffled) {
            if(chosen.size()==5)break;
            if(usedCategories.add(categoryForType.get(variant.typeId()))
                    && usedIds.add(variant.id()))chosen.add(variant);
        }
        for(ExerciseVariant variant:shuffled) {
            if(chosen.size()==5)break;
            if(usedIds.add(variant.id()))chosen.add(variant);
        }
        return new Proposal(MONARCH, starts, starts.plusDays(5), chosen, true,
                chosen.size() == 5
                ? "Cinco opciones para un período de cinco días; requieren autorización explícita. No hay cuotas obligatorias."
                : "Hay menos de cinco opciones compatibles; no se puede activar la misión.");
    }

    /** Independent seeded draw: different cycles and profiles vary predictably. */
    private static long monarchSeed(Profile profile,LocalDate starts) {
        byte[] bytes=digest("asterion.monarch.v3|"+starts+"|"
                +profile.missionSignature());
        long seed=0L;
        for(int i=0;i<8;i++)seed=(seed<<8)|(bytes[i]&0xffL);
        return seed;
    }

    /**
     * Rehydrate only the five candidates previously shown and accepted.
     * A corrupt or incompatible snapshot must not become an active mission.
     */
    public Proposal restoreMonarch(Profile profile,LocalDate start,
                                   List<String> ids) {
        Objects.requireNonNull(profile,"profile");
        Objects.requireNonNull(start,"start");
        Objects.requireNonNull(ids,"ids");
        if(ids.size()!=5)throw new IllegalArgumentException(
                "Monarca requiere cinco candidatos");
        Set<String> allowedCategories=new HashSet<>();
        for(int cat:profile.weekly)if(cat!=5)
            allowedCategories.add(WEEK_CATEGORIES[cat]);
        int cap="principiante".equals(profile.experience)?1:2;
        Set<String> seen=new HashSet<>();
        List<ExerciseVariant> result=new ArrayList<>();
        for(String id:ids) {
            if(!seen.add(id))throw new IllegalArgumentException(
                    "Monarca contiene un ID repetido");
            ExerciseVariant v=catalog.variant(id).orElse(null);
            if(v==null || v.difficulty()>cap
                    || !allowedCategories.contains(categoryForType.get(v.typeId()))
                    || !catalog.eligibleForAutomaticSuggestion(v)
                    || !profile.equipment.containsAll(v.equipment())
                    || ("baja".equals(profile.intensityPreference)
                        && catalog.intensity(v.id())!=ExerciseIntensity.LOW)) {
                throw new IllegalArgumentException(
                        "Monarca contiene una variante incompatible");
            }
            result.add(v);
        }
        return new Proposal(MONARCH,start,start.plusDays(5),result,true,
                "Selección Monarca previamente revisada y estable.");
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
        int maxTechnicalLevel = MissionDifficultyMixPolicy.maxTechnicalLevel(profile.experience);
        for (ExerciseVariant variant : catalog.variants()) {
            if (!category.equals(categoryForType.get(variant.typeId()))) continue;
            if (!catalog.eligibleForAutomaticSuggestion(variant)
                    || variant.difficulty() > maxTechnicalLevel) continue;
            if ("baja".equals(profile.intensityPreference)
                    && catalog.intensity(variant.id())!=ExerciseIntensity.LOW) continue;
            if (!profile.equipment.containsAll(variant.equipment())) continue;
            result.add(variant);
        }
        result.sort(Comparator.comparing(ExerciseVariant::id));
        return result;
    }

    private List<ExerciseVariant> preferredFromRecentSession(
            List<ExerciseVariant> eligible,List<String> previousVariantIds,
            CategoryEffortPolicy.Decision decision,boolean enableRotation,
            Set<String> recentCompleted) {
        ArrayList<ExerciseVariant> preferred=new ArrayList<>();
        Set<String> ids=new HashSet<>();
        for(String oldId:previousVariantIds) {
            ExerciseVariant previous=catalog.variant(oldId).orElse(null);
            if(previous==null)continue;
            ExerciseVariant selected=null;
            if(enableRotation
                    && decision.guidance==CategoryEffortPolicy.Guidance.REVIEW_OPTIONAL) {
                for(ExerciseVariant option:eligible) {
                    if(option.typeId().equals(previous.typeId())
                            && option.difficulty()==previous.difficulty()
                            && !option.id().equals(previous.id())
                            && !recentCompleted.contains(option.id())) {
                        selected=option;
                        break;
                    }
                }
            }
            if(selected==null && eligible.contains(previous)
                    && !recentCompleted.contains(previous.id())) {
                selected=previous;
            }
            if(selected!=null && ids.add(selected.id()))preferred.add(selected);
        }
        return preferred;
    }

    /**
     * Constrained, deterministic draw. Split the compatible pool into easy
     * technical levels (1-2) and non-supervised level 3. A completed weekly
     * review adjusts only the SHARE of eligible choices, never their volume,
     * physical intensity or the safety eligibility filters.
     */
    private List<ExerciseVariant> takeLevelMix(List<ExerciseVariant> eligible,
            int count,long seed,Set<String> recentCompleted,
            String experience,int mixPhase,List<ExerciseVariant> preferred) {
        if(eligible.isEmpty() || count<=0)return Collections.emptyList();
        if(!MissionDifficultyMixPolicy.mixed(experience)) {
            return takeDiverse(eligible,count,seed,recentCompleted);
        }
        List<ExerciseVariant> shuffled=takeDiverse(eligible,eligible.size(),
                seed,recentCompleted);
        ArrayList<ExerciseVariant> ranked=new ArrayList<>();
        Set<String> seen=new HashSet<>();
        for(ExerciseVariant candidate:preferred) {
            if(eligible.contains(candidate) && seen.add(candidate.id()))ranked.add(candidate);
        }
        for(ExerciseVariant candidate:shuffled) {
            if(seen.add(candidate.id()))ranked.add(candidate);
        }
        ArrayList<ExerciseVariant> lower=new ArrayList<>();
        ArrayList<ExerciseVariant> higher=new ArrayList<>();
        ArrayList<ExerciseVariant> fallback=new ArrayList<>();
        for(ExerciseVariant v:ranked) {
            if(v.difficulty()==3)higher.add(v);
            else if(v.difficulty()==2)lower.add(v);
            else fallback.add(v);
        }
        // Advanced level emphasizes intermediate (2) + harder (3);
        // if level-2 choices are scarce, safe level-1 remains a fallback.
        if(!"avanzado".equals(experience))lower.addAll(fallback);
        int upperTarget=MissionDifficultyMixPolicy.higherTarget(
                experience,mixPhase,count);
        ArrayList<ExerciseVariant> selected=new ArrayList<>();
        Set<String> chosenIds=new HashSet<>();
        addWithDiversity(lower,count-upperTarget,selected,chosenIds);
        // addWithDiversity receives an absolute target, not an increment.
        addWithDiversity(higher,Math.min(count,selected.size()+upperTarget),
                selected,chosenIds);
        addWithDiversity(lower,count,selected,chosenIds);
        addWithDiversity(higher,count,selected,chosenIds);
        addWithDiversity(fallback,count,selected,chosenIds);
        return selected;
    }

    private void addWithDiversity(List<ExerciseVariant> options,int target,
            List<ExerciseVariant> selected,Set<String> chosenIds) {
        if(selected.size()>=target)return;
        Set<String> usedFamilies=new HashSet<>();
        Set<String> usedTypes=new HashSet<>();
        for(ExerciseVariant chosen:selected) {
            usedFamilies.add(familyForType.get(chosen.typeId()));
            usedTypes.add(chosen.typeId());
        }
        for(int pass=0;pass<3 && selected.size()<target;pass++) {
            for(ExerciseVariant v:options) {
                if(selected.size()>=target)break;
                if(chosenIds.contains(v.id()))continue;
                if(pass==0 && usedFamilies.contains(familyForType.get(v.typeId())))
                    continue;
                if(pass==1 && usedTypes.contains(v.typeId()))continue;
                if(chosenIds.add(v.id())) {
                    selected.add(v);
                    usedFamilies.add(familyForType.get(v.typeId()));
                    usedTypes.add(v.typeId());
                }
            }
        }
    }

    private List<ExerciseVariant> takeDiverse(List<ExerciseVariant> pool,
                                              int count,long seed) {
        return takeDiverse(pool,count,seed,Collections.emptySet());
    }

    private List<ExerciseVariant> takeDiverse(List<ExerciseVariant> pool,
                                              int count,long seed,
                                              Set<String> recentlyCompleted) {
        if(pool.isEmpty() || count<=0)return Collections.emptyList();
        Objects.requireNonNull(recentlyCompleted,"recentlyCompleted");
        // Seeded full-catalog shuffle is reproducible for one date/profile.
        ArrayList<ExerciseVariant> shuffled=new ArrayList<>(pool);
        Collections.shuffle(shuffled,new Random(seed));
        ArrayList<ExerciseVariant> selected=new ArrayList<>();
        Set<String> usedIds=new HashSet<>();
        // First prefer exercises not completed recently. Only afterwards reuse
        // previously completed safe options when the eligible pool is small.
        for(boolean useRecent:new boolean[]{false,true}) {
            Set<String> usedFocus=new HashSet<>();
            for(ExerciseVariant v:selected)usedFocus.add(
                    ExerciseMuscleFocus.forFamily(familyForType.get(v.typeId())).key());
            for(ExerciseVariant v:shuffled) {
                if(selected.size()>=count)break;
                if(recentlyCompleted.contains(v.id())!=useRecent)continue;
                String focus=ExerciseMuscleFocus.forFamily(
                        familyForType.get(v.typeId())).key();
                if(usedFocus.add(focus) && usedIds.add(v.id()))selected.add(v);
            }
            Set<String> usedFamilies=new HashSet<>();
            for(ExerciseVariant v:selected)usedFamilies.add(familyForType.get(v.typeId()));
            for(ExerciseVariant v:shuffled) {
                if(selected.size()>=count)break;
                if(recentlyCompleted.contains(v.id())!=useRecent)continue;
                if(usedFamilies.add(familyForType.get(v.typeId()))
                        && usedIds.add(v.id()))selected.add(v);
            }
            Set<String> usedTypes=new HashSet<>();
            for(ExerciseVariant v:selected)usedTypes.add(v.typeId());
            for(ExerciseVariant v:shuffled) {
                if(selected.size()>=count)break;
                if(recentlyCompleted.contains(v.id())!=useRecent)continue;
                if(usedTypes.add(v.typeId()) && usedIds.add(v.id()))selected.add(v);
            }
            // With few distinct families/types the eligible variants must
            // remain usable; never force a duplicate or a non-eligible exercise.
            for(ExerciseVariant v:shuffled) {
                if(selected.size()>=count)break;
                if(recentlyCompleted.contains(v.id())==useRecent
                        && usedIds.add(v.id()))selected.add(v);
            }
            if(selected.size()>=count)break;
        }
        return selected;
    }
}

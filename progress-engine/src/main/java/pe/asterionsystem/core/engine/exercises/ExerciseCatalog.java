package pe.asterionsystem.core.engine.exercises;

import java.text.Normalizer;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

/**
 * Immutable, validated family -> type -> variation index.
 * Reading it has no side effects and never modifies routines or progress.
 */
public final class ExerciseCatalog {
    private final List<ExerciseFamily> families;
    private final List<ExerciseType> types;
    private final List<ExerciseVariant> variants;
    private final Map<String, ExerciseFamily> byFamily;
    private final Map<String, ExerciseType> byType;
    private final Map<String, ExerciseVariant> byVariant;
    private final Map<String, ExerciseIntensity> intensityById;
    private final ExerciseCategoryIndex categoryIndex;
    private final ExerciseSportIndex sportIndex;

    ExerciseCatalog(List<ExerciseFamily> allFamilies, List<ExerciseType> allTypes,
                    List<ExerciseVariant> allVariants) {
        this(allFamilies,allTypes,allVariants,Collections.emptyMap());
    }

    ExerciseCatalog(List<ExerciseFamily> allFamilies, List<ExerciseType> allTypes,
                    List<ExerciseVariant> allVariants,
                    Map<String, ExerciseIntensity> intensityOverrides) {
        this(allFamilies,allTypes,allVariants,intensityOverrides,ExerciseSportIndex.empty());
    }

    ExerciseCatalog(List<ExerciseFamily> allFamilies, List<ExerciseType> allTypes,
                    List<ExerciseVariant> allVariants,
                    Map<String, ExerciseIntensity> intensityOverrides,
                    ExerciseSportIndex sportIndex) {
        Objects.requireNonNull(intensityOverrides,"intensityOverrides");
        Objects.requireNonNull(sportIndex,"sportIndex");
        if (allFamilies.isEmpty() || allTypes.isEmpty() || allVariants.isEmpty()) {
            throw new IllegalArgumentException("El catálogo no puede estar vacío");
        }
        if (allFamilies.size() > 400 || allTypes.size() > 1500 || allVariants.size() > 10000) {
            throw new IllegalArgumentException("Límite de catálogo excedido");
        }
        byFamily = new LinkedHashMap<>();
        byType = new LinkedHashMap<>();
        byVariant = new LinkedHashMap<>();
        for (ExerciseFamily family : allFamilies) {
            ExerciseFamily checked = Objects.requireNonNull(family, "family");
            if (byFamily.putIfAbsent(checked.id(), checked) != null) {
                throw new IllegalArgumentException("Familia duplicada: " + checked.id());
            }
        }
        for (ExerciseType type : allTypes) {
            ExerciseType checked = Objects.requireNonNull(type, "type");
            if (!byFamily.containsKey(checked.familyId())) {
                throw new IllegalArgumentException("Tipo sin familia: " + checked.id());
            }
            if (byType.putIfAbsent(checked.id(), checked) != null) {
                throw new IllegalArgumentException("Tipo duplicado: " + checked.id());
            }
        }
        Map<String, Integer> lastLevel = new LinkedHashMap<>();
        for (ExerciseVariant variant : allVariants) {
            ExerciseVariant checked = Objects.requireNonNull(variant, "variant");
            if (!byType.containsKey(checked.typeId())) {
                throw new IllegalArgumentException("Variante sin tipo: " + checked.id());
            }
            if (byVariant.putIfAbsent(checked.id(), checked) != null) {
                throw new IllegalArgumentException("Variante duplicada: " + checked.id());
            }
            int last = lastLevel.getOrDefault(checked.typeId(), 0);
            if (checked.difficulty() < last) {
                throw new IllegalArgumentException("Variante fuera de orden progresivo: " + checked.id());
            }
            lastLevel.put(checked.typeId(), checked.difficulty());
        }
        if (lastLevel.size() != byType.size()) {
            throw new IllegalArgumentException("Existen tipos sin variantes");
        }
        for (ExerciseFamily family : allFamilies) {
            boolean found = false;
            for (ExerciseType type : allTypes) {
                if (type.familyId().equals(family.id())) { found = true; break; }
            }
            if (!found) throw new IllegalArgumentException("Familia sin tipos: " + family.id());
        }
        HashMap<String, ExerciseIntensity> tags=new HashMap<>();
        for(Map.Entry<String, ExerciseIntensity> item:intensityOverrides.entrySet()) {
            if(!byVariant.containsKey(item.getKey())) {
                throw new IllegalArgumentException("Intensidad de variante inexistente: "+item.getKey());
            }
            tags.put(item.getKey(),Objects.requireNonNull(item.getValue(),"intensity"));
        }
        this.intensityById=Collections.unmodifiableMap(tags);
        this.families = Collections.unmodifiableList(new ArrayList<>(allFamilies));
        this.types = Collections.unmodifiableList(new ArrayList<>(allTypes));
        this.variants = Collections.unmodifiableList(new ArrayList<>(allVariants));
        this.categoryIndex = new ExerciseCategoryIndex(this.families,this.types,this.variants);
        this.sportIndex = sportIndex;
    }

    public List<ExerciseFamily> families() { return families; }
    public List<ExerciseType> types() { return types; }
    public List<ExerciseVariant> variants() { return variants; }
    public ExerciseCategoryIndex categoryIndex() { return categoryIndex; }
    public ExerciseSportIndex sportIndex() { return sportIndex; }

    /** Unlabelled older variants are preserved, not assigned speculative intensity. */
    public ExerciseIntensity intensity(String variantId) {
        return intensityById.getOrDefault(TaxonomyChecks.id(variantId),
                ExerciseIntensity.UNCLASSIFIED);
    }

    /**
     * New explicitly high or variable intensity records are informational,
     * not automatically assigned to an unsupervised mission.
     */
    public boolean eligibleForAutomaticSuggestion(ExerciseVariant variant) {
        if(!variant.eligibleForAutomaticSuggestion() || sportIndex.manualOnly(variant.typeId()))return false;
        ExerciseIntensity tag=intensity(variant.id());
        return tag!=ExerciseIntensity.HIGH && tag!=ExerciseIntensity.VARIABLE;
    }
    public Optional<ExerciseVariant> variant(String id) {
        return Optional.ofNullable(byVariant.get(TaxonomyChecks.id(id)));
    }
    public List<ExerciseType> typesInFamily(String familyId) {
        TaxonomyChecks.id(familyId);
        ArrayList<ExerciseType> result = new ArrayList<>();
        for (ExerciseType item : types) if (item.familyId().equals(familyId)) result.add(item);
        return Collections.unmodifiableList(result);
    }
    public List<ExerciseVariant> variantsInType(String typeId) {
        TaxonomyChecks.id(typeId);
        ArrayList<ExerciseVariant> result = new ArrayList<>();
        for (ExerciseVariant item : variants) if (item.typeId().equals(typeId)) result.add(item);
        return Collections.unmodifiableList(result);
    }
    public List<ExerciseVariant> search(String query, String category,
                                        String equipment, int maximumDifficulty,
                                        int limit, int offset) {
        return search(query, category, equipment, null, maximumDifficulty, limit, offset);
    }

    /** Cross-filter by body region, available implement, execution mode and skill level. */
    public List<ExerciseVariant> search(String query, String category,
                                        String equipment, String execution,
                                        int maximumDifficulty, int limit, int offset) {
        return search(query, category, equipment, execution, null,
                maximumDifficulty, limit, offset);
    }

    /** Sports are search tags, never an alternative primary category. */
    public List<ExerciseVariant> search(String query, String category,
                                        String equipment, String execution,
                                        String sportCode, int maximumDifficulty,
                                        int limit, int offset) {
        if (query == null || query.length() > 120 || maximumDifficulty < 1 ||
                maximumDifficulty > 5 || limit < 1 || limit > 100 || offset < 0) {
            throw new IllegalArgumentException("Filtros de búsqueda inválidos");
        }
        if (category != null) TaxonomyChecks.token(category);
        if (equipment != null) TaxonomyChecks.token(equipment);
        if (execution != null) TaxonomyChecks.token(execution);
        if (sportCode != null) sportIndex.sportName(sportCode);
        String q = normalized(query);
        ArrayList<ExerciseVariant> hits = new ArrayList<>();
        for (ExerciseVariant variant : variants) {
            if (variant.difficulty() == 0 || variant.difficulty() > maximumDifficulty) continue;
            if (equipment != null && !variant.equipment().contains(equipment)) continue;
            if (execution != null && !variant.execution().equals(execution)) continue;
            ExerciseType type = byType.get(variant.typeId());
            if (sportCode != null && !sportIndex.includes(type.id(), sportCode)) continue;
            ExerciseFamily family = byFamily.get(type.familyId());
            // Accept the public five-category code while retaining older
            // direct searches by the original taxonomy code.
            if (category != null &&
                    !ExerciseCategoryMapper.primaryCategory(category).equals(
                            categoryIndex.categoryForFamily(family.id()))) continue;
            if (!q.isEmpty() && !normalized(variant.name() + " " + type.name() + " " + family.name()
                    + " " + String.join(" ",sportIndex.namesForType(type.id()))
                    + " " + sportIndex.goal(type.id())).contains(q)) {
                continue;
            }
            hits.add(variant);
        }
        if (offset >= hits.size()) return Collections.emptyList();
        return Collections.unmodifiableList(new ArrayList<>(
                hits.subList(offset, Math.min(hits.size(), offset + limit))));
    }

    private static String normalized(String source) {
        String normal = Normalizer.normalize(source.toLowerCase(Locale.ROOT), Normalizer.Form.NFD);
        return normal.replaceAll("\\p{M}+", "");
    }
}

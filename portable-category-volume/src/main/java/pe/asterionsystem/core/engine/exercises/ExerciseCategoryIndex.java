package pe.asterionsystem.core.engine.exercises;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * Immutable five-category view of the complete exercise hierarchy.
 *
 * category (five user-facing groups) -> families -> movement types -> variants.
 * Historical taxonomy codes (coordinacion and integrado) are mapped once via
 * ExerciseCategoryMapper. Neither source IDs nor assigned routines are changed.
 */
public final class ExerciseCategoryIndex {
    private final Map<String,List<ExerciseFamily>> familiesByCategory;
    private final Map<String,List<ExerciseType>> typesByCategory;
    private final Map<String,List<ExerciseVariant>> variantsByCategory;
    private final Map<String,String> categoryByFamily;
    private final Map<String,String> categoryByType;
    private final Map<String,String> categoryByVariant;
    private final Map<String,String> familyByType;
    private final Map<String,String> typeByVariant;

    ExerciseCategoryIndex(List<ExerciseFamily> families, List<ExerciseType> types,
                          List<ExerciseVariant> variants) {
        Map<String,List<ExerciseFamily>> fs=new LinkedHashMap<>();
        Map<String,List<ExerciseType>> ts=new LinkedHashMap<>();
        Map<String,List<ExerciseVariant>> vs=new LinkedHashMap<>();
        for(String code:ExerciseCategoryMapper.weeklyCodes()) {
            fs.put(code,new ArrayList<>());
            ts.put(code,new ArrayList<>());
            vs.put(code,new ArrayList<>());
        }
        Map<String,String> byFamily=new LinkedHashMap<>();
        Map<String,String> byType=new LinkedHashMap<>();
        Map<String,String> byVariant=new LinkedHashMap<>();
        Map<String,String> typeFamilies=new LinkedHashMap<>();
        Map<String,String> variantTypes=new LinkedHashMap<>();
        for(ExerciseFamily item:families) {
            String category=ExerciseCategoryMapper.primaryCategory(item.category());
            if(!fs.containsKey(category))throw new IllegalArgumentException(
                    "Familia sin categoría operativa: "+item.id());
            fs.get(category).add(item);
            byFamily.put(item.id(),category);
        }
        for(ExerciseType item:types) {
            String category=byFamily.get(item.familyId());
            if(category==null)throw new IllegalArgumentException(
                    "Tipo sin familia registrada: "+item.id());
            ts.get(category).add(item);
            byType.put(item.id(),category);
            typeFamilies.put(item.id(),item.familyId());
        }
        for(ExerciseVariant item:variants) {
            String category=byType.get(item.typeId());
            if(category==null)throw new IllegalArgumentException(
                    "Variante sin tipo registrado: "+item.id());
            vs.get(category).add(item);
            byVariant.put(item.id(),category);
            variantTypes.put(item.id(),item.typeId());
        }
        this.familiesByCategory=freeze(fs);
        this.typesByCategory=freeze(ts);
        this.variantsByCategory=freeze(vs);
        this.categoryByFamily=Collections.unmodifiableMap(byFamily);
        this.categoryByType=Collections.unmodifiableMap(byType);
        this.categoryByVariant=Collections.unmodifiableMap(byVariant);
        this.familyByType=Collections.unmodifiableMap(typeFamilies);
        this.typeByVariant=Collections.unmodifiableMap(variantTypes);
    }

    private static <T> Map<String,List<T>> freeze(Map<String,List<T>> input) {
        Map<String,List<T>> output=new LinkedHashMap<>();
        for(Map.Entry<String,List<T>> entry:input.entrySet()) {
            output.put(entry.getKey(),Collections.unmodifiableList(
                    new ArrayList<>(entry.getValue())));
        }
        return Collections.unmodifiableMap(output);
    }

    public List<String> categories() {return ExerciseCategoryMapper.weeklyCodes();}
    public List<ExerciseFamily> families(String category) {
        return requireCategory(familiesByCategory,category);
    }
    public List<ExerciseType> types(String category) {
        return requireCategory(typesByCategory,category);
    }
    public List<ExerciseVariant> variants(String category) {
        return requireCategory(variantsByCategory,category);
    }
    private static <T> List<T> requireCategory(Map<String,List<T>> items,String category) {
        Objects.requireNonNull(category,"category");
        List<T> found=items.get(category);
        if(found==null)throw new IllegalArgumentException(
                "Categoría operativa desconocida: "+category);
        return found;
    }
    public String categoryForFamily(String id) {
        return requireId(categoryByFamily,id,"familia");
    }
    public String categoryForType(String id) {
        return requireId(categoryByType,id,"tipo");
    }
    public String categoryForVariant(String id) {
        return requireId(categoryByVariant,id,"variante");
    }
    public String familyForType(String id) {
        return requireId(familyByType,id,"tipo");
    }
    public String typeForVariant(String id) {
        return requireId(typeByVariant,id,"variante");
    }
    public String familyForVariant(String id) {
        return familyForType(typeForVariant(id));
    }
    private static String requireId(Map<String,String> mapping,String id,String kind) {
        String result=mapping.get(TaxonomyChecks.id(id));
        if(result==null)throw new IllegalArgumentException(
                "Identificador de "+kind+" desconocido: "+id);
        return result;
    }
}

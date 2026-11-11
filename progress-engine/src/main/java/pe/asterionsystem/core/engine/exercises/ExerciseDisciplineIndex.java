package pe.asterionsystem.core.engine.exercises;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

/**
 * Optional editorial cross-sport taxonomy for exercise movement TYPES.
 * One type can serve several sports; each variant still has only one
 * canonical type, family and operational category.
 */
public final class ExerciseDisciplineIndex {
    private final Map<String,List<String>> disciplinesByType;
    private final Map<String,String> roleByType;
    private final Map<String,List<ExerciseType>> typesByDiscipline;
    private final Map<String,List<ExerciseVariant>> variantsByDiscipline;

    ExerciseDisciplineIndex(List<ExerciseType> types,List<ExerciseVariant> variants,
                            Map<String,List<String>> tags,Map<String,String> roles) {
        Objects.requireNonNull(tags,"tags");
        Objects.requireNonNull(roles,"roles");
        Map<String,ExerciseType> knownTypes=new LinkedHashMap<>();
        for(ExerciseType item:types)knownTypes.put(item.id(),item);
        for(String typeId:tags.keySet()) {
            if(!knownTypes.containsKey(typeId))
                throw new IllegalArgumentException("Disciplina asociada a tipo desconocido: "+typeId);
        }
        if(!tags.keySet().equals(roles.keySet()))
            throw new IllegalArgumentException("Tipos sin función deportiva o sin disciplina");
        if(!tags.isEmpty() && tags.size()!=types.size())
            throw new IllegalArgumentException("Se requieren etiquetas para todos los tipos");
        Map<String,List<String>> tagged=new LinkedHashMap<>();
        Map<String,String> functions=new LinkedHashMap<>();
        Map<String,List<ExerciseType>> typeGroups=new LinkedHashMap<>();
        Map<String,List<ExerciseVariant>> variantGroups=new LinkedHashMap<>();
        for(ExerciseType type:types) {
            List<String> raw=tags.get(type.id());
            if(raw==null)continue; // Legacy three/four-reader API needs no sport file.
            if(raw.isEmpty() || raw.size()>8)
                throw new IllegalArgumentException("Cantidad de disciplinas inválida: "+type.id());
            Set<String> unique=new LinkedHashSet<>();
            for(String tag:raw) {
                String code=TaxonomyChecks.token(tag);
                if(!unique.add(code))
                    throw new IllegalArgumentException("Disciplina repetida: "+type.id());
            }
            String role=TaxonomyChecks.token(roles.get(type.id()));
            if(!List.of("fuerza","habilidad","acondicionamiento","movilidad","coordinacion")
                    .contains(role))
                throw new IllegalArgumentException("Función deportiva no admitida: "+role);
            List<String> stable=Collections.unmodifiableList(new ArrayList<>(unique));
            tagged.put(type.id(),stable);
            functions.put(type.id(),role);
            for(String tag:stable) {
                typeGroups.computeIfAbsent(tag,unused->new ArrayList<>()).add(type);
                variantGroups.computeIfAbsent(tag,unused->new ArrayList<>());
            }
        }
        Map<String,ExerciseType> typeLookup=new LinkedHashMap<>();
        for(ExerciseType type:types)typeLookup.put(type.id(),type);
        for(ExerciseVariant variant:variants) {
            List<String> sports=tagged.get(variant.typeId());
            if(sports==null)continue;
            for(String sport:sports)variantGroups.get(sport).add(variant);
        }
        disciplinesByType=Collections.unmodifiableMap(tagged);
        roleByType=Collections.unmodifiableMap(functions);
        typesByDiscipline=freeze(typeGroups);
        variantsByDiscipline=freeze(variantGroups);
    }

    private static <T> Map<String,List<T>> freeze(Map<String,List<T>> source) {
        Map<String,List<T>> out=new LinkedHashMap<>();
        for(Map.Entry<String,List<T>> entry:source.entrySet())
            out.put(entry.getKey(),Collections.unmodifiableList(
                    new ArrayList<>(entry.getValue())));
        return Collections.unmodifiableMap(out);
    }

    public List<String> disciplines() {
        return Collections.unmodifiableList(new ArrayList<>(typesByDiscipline.keySet()));
    }
    public List<String> disciplinesForType(String typeId) {
        List<String> result=disciplinesByType.get(TaxonomyChecks.id(typeId));
        return result==null ? Collections.emptyList() : result;
    }
    public String roleForType(String typeId) {
        return roleByType.getOrDefault(TaxonomyChecks.id(typeId),"sin_clasificar");
    }
    public List<ExerciseType> types(String discipline) {
        return typesByDiscipline.getOrDefault(TaxonomyChecks.token(discipline),
                Collections.emptyList());
    }
    public List<ExerciseVariant> variants(String discipline) {
        return variantsByDiscipline.getOrDefault(TaxonomyChecks.token(discipline),
                Collections.emptyList());
    }
}

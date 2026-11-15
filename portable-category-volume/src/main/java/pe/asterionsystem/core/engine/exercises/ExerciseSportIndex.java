package pe.asterionsystem.core.engine.exercises;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.Reader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

/**
 * Sports, equipment and goals are searchable cross-cutting tags, NEVER
 * additional top-level workout categories. Every type retains exactly one
 * family and category via ExerciseCategoryIndex.
 */
public final class ExerciseSportIndex {
    public static final class Details {
        private final List<String> codes;
        private final String goal, sourceUrl, sourceOrganization;
        private final boolean manualOnly;

        private Details(List<String> codes, String goal, String sourceUrl,
                        String sourceOrganization, boolean manualOnly) {
            this.codes=Collections.unmodifiableList(new ArrayList<>(codes));
            this.goal=goal;
            this.sourceUrl=sourceUrl;
            this.sourceOrganization=sourceOrganization;
            this.manualOnly=manualOnly;
        }
        public List<String> sports() { return codes; }
        public String goal() { return goal; }
        public String sourceUrl() { return sourceUrl; }
        public String sourceOrganization() { return sourceOrganization; }
        public boolean manualOnly() { return manualOnly; }
    }

    private final Map<String,String> displayNames;
    private final Map<String,Details> byType;

    private ExerciseSportIndex(Map<String,String> displayNames,
                               Map<String,Details> byType) {
        this.displayNames=Collections.unmodifiableMap(new LinkedHashMap<>(displayNames));
        this.byType=Collections.unmodifiableMap(new LinkedHashMap<>(byType));
    }
    static ExerciseSportIndex empty() {
        return new ExerciseSportIndex(Collections.emptyMap(),Collections.emptyMap());
    }

    public static ExerciseSportIndex load(Reader registry, Reader entries,
                                          List<ExerciseType> types)
            throws IOException {
        Objects.requireNonNull(registry,"sport registry");
        Objects.requireNonNull(entries,"sport entries");
        Objects.requireNonNull(types,"exercise types");
        Map<String,String> names=new LinkedHashMap<>();
        BufferedReader dictionary=new BufferedReader(registry);
        if(!"code\tname".equals(dictionary.readLine()))
            throw new IllegalArgumentException("Formato de disciplinas no válido");
        String line;
        while((line=dictionary.readLine())!=null) {
            if(line.isEmpty()||line.startsWith("#"))continue;
            String[] fields=columns(line,2);
            String code=TaxonomyChecks.token(fields[0]);
            String name=TaxonomyChecks.label(fields[1]);
            if(names.putIfAbsent(code,name)!=null)
                throw new IllegalArgumentException("Deporte duplicado: "+code);
            if(names.size()>150)throw new IllegalArgumentException("Demasiados deportes");
        }
        if(names.isEmpty())throw new IllegalArgumentException("Disciplinas vacías");

        Set<String> validTypes=new HashSet<>();
        for(ExerciseType type:types)validTypes.add(type.id());
        Map<String,Details> metadata=new LinkedHashMap<>();
        BufferedReader reader=new BufferedReader(entries);
        if(!"type_id\tsports\tgoal\tsource_url\tsource_org\tmanual_only".equals(reader.readLine()))
            throw new IllegalArgumentException("Formato de etiquetas deportivas inválido");
        while((line=reader.readLine())!=null) {
            if(line.isEmpty()||line.startsWith("#"))continue;
            String[] fields=columns(line,6);
            String typeId=TaxonomyChecks.id(fields[0]);
            if(!validTypes.contains(typeId))
                throw new IllegalArgumentException("Deporte para tipo inexistente: "+typeId);
            List<String> codes=new ArrayList<>();
            for(String raw:fields[1].split("\\+",-1)) {
                String code=TaxonomyChecks.token(raw);
                if(!names.containsKey(code) || codes.contains(code))
                    throw new IllegalArgumentException("Deporte inexistente/duplicado: "+code);
                codes.add(code);
            }
            if(codes.isEmpty()||codes.size()>25)
                throw new IllegalArgumentException("Etiquetas deportivas no válidas");
            String goal=TaxonomyChecks.label(fields[2]);
            String url=fields[3];
            if(url.length()>600||!url.startsWith("https://")||url.contains(" "))
                throw new IllegalArgumentException("Referencia HTTPS no válida");
            String organization=TaxonomyChecks.label(fields[4]);
            if(!"0".equals(fields[5])&&!"1".equals(fields[5]))
                throw new IllegalArgumentException("Filtro manual inválido");
            Details value=new Details(codes,goal,url,organization,"1".equals(fields[5]));
            if(metadata.putIfAbsent(typeId,value)!=null)
                throw new IllegalArgumentException("Tipo deportivo duplicado: "+typeId);
            if(metadata.size()>1500)
                throw new IllegalArgumentException("Demasiadas etiquetas");
        }
        return new ExerciseSportIndex(names,metadata);
    }

    private static String[] columns(String line,int count) {
        if(line.length()>1400)throw new IllegalArgumentException("Fila larga");
        String[] values=line.split("\t",-1);
        if(values.length!=count)throw new IllegalArgumentException("Columnas inválidas");
        for(String field:values)
            if(field.trim().isEmpty())throw new IllegalArgumentException("Campo vacío");
        return values;
    }

    public List<String> sports() {
        return Collections.unmodifiableList(new ArrayList<>(displayNames.keySet()));
    }
    public String sportName(String code) {
        String result=displayNames.get(TaxonomyChecks.token(code));
        if(result==null)throw new IllegalArgumentException("Deporte no registrado: "+code);
        return result;
    }
    public List<String> codesForType(String typeId) {
        Details record=byType.get(TaxonomyChecks.id(typeId));
        return record==null?Collections.emptyList():record.sports();
    }
    public List<String> namesForType(String typeId) {
        List<String> names=new ArrayList<>();
        for(String code:codesForType(typeId))names.add(displayNames.get(code));
        return Collections.unmodifiableList(names);
    }
    public boolean includes(String typeId,String sportCode) {
        sportName(sportCode);
        return codesForType(typeId).contains(sportCode);
    }
    public boolean manualOnly(String typeId) {
        Details record=byType.get(TaxonomyChecks.id(typeId));
        return record!=null && record.manualOnly();
    }
    public String goal(String typeId) {
        Details record=byType.get(TaxonomyChecks.id(typeId));
        return record==null?"":record.goal();
    }
    public String sourceOrganization(String typeId) {
        Details record=byType.get(TaxonomyChecks.id(typeId));
        return record==null?"":record.sourceOrganization();
    }
    public String sourceUrl(String typeId) {
        Details record=byType.get(TaxonomyChecks.id(typeId));
        return record==null?"":record.sourceUrl();
    }
}

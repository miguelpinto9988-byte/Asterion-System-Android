package pe.asterionsystem.core.engine.exercises;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.Reader;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.Objects;

/**
 * Strict TSV v1 parser. Accepts only static data; never code or external scripts.
 * The caller owns Reader lifecycle.
 */
public final class ExerciseCatalogLoader {
    private ExerciseCatalogLoader() { }

    public static ExerciseCatalog load(Reader families, Reader types, Reader variants) throws IOException {
        return load(families,types,variants,null);
    }

    /** Optional fourth TSV keeps all historical seven-column variant assets compatible. */
    public static ExerciseCatalog load(Reader families, Reader types, Reader variants,
                                       Reader intensity) throws IOException {
        return load(families,types,variants,intensity,null);
    }

    /** Optional fifth TSV attaches sports to types without altering existing IDs. */
    public static ExerciseCatalog load(Reader families, Reader types, Reader variants,
                                       Reader intensity, Reader disciplines) throws IOException {
        List<ExerciseFamily> parsedFamilies = new ArrayList<>();
        for (String[] row : rows(families, 4, "family_id\\tname\\tcategory\\tpattern", 200)) {
            parsedFamilies.add(new ExerciseFamily(row[0], row[1], row[2], row[3]));
        }
        List<ExerciseType> parsedTypes = new ArrayList<>();
        for (String[] row : rows(types, 3, "type_id\\tfamily_id\\tname", 500)) {
            parsedTypes.add(new ExerciseType(row[0], row[1], row[2]));
        }
        List<ExerciseVariant> parsedVariants = new ArrayList<>();
        for (String[] row : rows(variants, 7,
                "variant_id\\ttype_id\\tname\\tlevel\\tequipment\\texecution\\tsupervision", 4000)) {
            final int level;
            if (!row[3].matches("[1-5]")) throw new IllegalArgumentException("Nivel inválido: " + row[3]);
            level = Integer.parseInt(row[3]);
            if (!row[6].equals("0") && !row[6].equals("1")) {
                throw new IllegalArgumentException("Indicador de orientación inválido");
            }
            parsedVariants.add(new ExerciseVariant(
                    row[0], row[1], row[2], level, row[4], row[5], row[6].equals("1")));
        }
        Map<String,ExerciseIntensity> tags=new HashMap<>();
        if(intensity!=null) {
            for(String[] row:rows(intensity,2,"variant_id\\tintensity",4000)) {
                if(tags.putIfAbsent(row[0],ExerciseIntensity.parse(row[1]))!=null) {
                    throw new IllegalArgumentException("Intensidad duplicada: "+row[0]);
                }
            }
        }
        Map<String,List<String>> sportTags=new HashMap<>();
        Map<String,String> roles=new HashMap<>();
        if(disciplines!=null) {
            for(String[] row:rows(disciplines,3,
                    "type_id\\tdisciplines\\trole",500)) {
                if(!row[1].matches("[a-z][a-z0-9_+]{1,300}"))
                    throw new IllegalArgumentException("Etiquetas deportivas inválidas");
                if(sportTags.putIfAbsent(row[0],List.of(row[1].split("\\\\+",-1)))!=null)
                    throw new IllegalArgumentException("Tipo deportivo duplicado: "+row[0]);
                roles.put(row[0],row[2]);
            }
        }
        return new ExerciseCatalog(parsedFamilies,parsedTypes,parsedVariants,
                tags,sportTags,roles);
    }

    private static List<String[]> rows(Reader input, int columns, String header, int max)
            throws IOException {
        Objects.requireNonNull(input, "reader");
        BufferedReader reader = input instanceof BufferedReader
                ? (BufferedReader) input : new BufferedReader(input);
        String first = reader.readLine();
        if (!header.replace("\\t", "\t").equals(first)) {
            throw new IllegalArgumentException("Formato de catálogo TSV no reconocido");
        }
        List<String[]> result = new ArrayList<>();
        String line;
        while ((line = reader.readLine()) != null) {
            if (line.isEmpty() || line.startsWith("#")) continue;
            if (line.length() > 700) throw new IllegalArgumentException("Fila demasiado larga");
            String[] values = line.split("\t", -1);
            if (values.length != columns) throw new IllegalArgumentException("Columnas inválidas");
            result.add(values);
            if (result.size() > max) throw new IllegalArgumentException("Límite de filas excedido");
        }
        return result;
    }
}

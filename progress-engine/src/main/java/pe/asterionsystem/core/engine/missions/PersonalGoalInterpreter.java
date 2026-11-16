package pe.asterionsystem.core.engine.missions;

import java.text.Normalizer;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import pe.asterionsystem.core.engine.exercises.ExerciseVariant;

/**
 * Offline, conservative interpretation of the optional free-text goal.
 * This is preference ranking, not diagnosis or exercise prescription.
 * Only positive, recognizable activity preferences affect eligible catalog choices.
 */
public final class PersonalGoalInterpreter {
    private static final String[][] RULES = {
        // Match user wording, then match metadata of already-eligible exercises.
        {"caminar,caminata,pasear,senderismo,trekking,montana",
         "caminata,marcha,pasos,recorridos,aire_libre"},
        {"bicicleta,ciclismo,pedalear,bici",
         "bicicleta,bici,pedal,eliptica"},
        {"correr,trotar,running",
         "carrera,trote,correr"},
        {"bailar,baile,danza,ritmo",
         "danza,baile,ritmo,coordinacion"},
        {"equilibrio,balance,estabilidad",
         "equilibrio,estabilidad,control"},
        {"flexibilidad,estiramiento,estirar",
         "flexibilidad,movilidad,yoga,articular"},
        {"movilidad,moverme,articulaciones",
         "movilidad,articular,control,yoga"},
        {"postura,espalda,columna",
         "postura,espalda,columna,escapula,tronco"},
        {"abdomen,abdominal,core,tronco",
         "abdomen,core,tronco,antirotacion,anti_rotacion"},
        {"hombros,hombro,brazos,brazo",
         "hombro,brazo,empuje,traccion,escapula"},
        {"piernas,pierna,rodillas,rodilla",
         "piernas,pierna,sentadilla,zancada,rodilla,muslos"},
        {"resistencia,cardio,aguante",
         "cardio,caminata,marcha,pasos,bicicleta,danza"},
        {"fuerza,fortalecer,fortalecimiento",
         "fuerza,empuje,traccion,sentadilla,estabilidad"},
        {"respiracion,respirar,relajacion",
         "respiracion,postura,movilidad"}
    };

    private final List<String> matchingTags;

    private PersonalGoalInterpreter(List<String> tags) {
        matchingTags=Collections.unmodifiableList(tags);
    }

    public static PersonalGoalInterpreter from(String writtenGoal) {
        String text=normalize(writtenGoal);
        if(text.isEmpty())return new PersonalGoalInterpreter(Collections.emptyList());
        ArrayList<String> tags=new ArrayList<>();
        for(String[] rule:RULES) {
            boolean requested=false;
            for(String word:rule[0].split(",")) {
                if(positiveMention(text,word)) {
                    requested=true;
                    break;
                }
            }
            if(requested)for(String tag:rule[1].split(",")) {
                if(!tags.contains(tag))tags.add(tag);
            }
        }
        return new PersonalGoalInterpreter(tags);
    }

    public boolean recognized() { return !matchingTags.isEmpty(); }

    /** Higher scores ONLY reorder options that passed all existing safety gates. */
    public int score(ExerciseVariant exercise,String familyId) {
        if(!recognized())return 0;
        String metadata=normalize(exercise.name()+" "+exercise.typeId()+" "
                +(familyId==null?"":familyId));
        int result=0;
        for(String tag:matchingTags) {
            if(metadata.contains(tag))result++;
        }
        return result;
    }

    private static boolean positiveMention(String text,String term) {
        String[] words=text.trim().split(" ");
        String[] pattern=term.split(" ");
        for(int i=0;i<=words.length-pattern.length;i++) {
            boolean match=true;
            for(int j=0;j<pattern.length;j++) {
                if(!words[i+j].equals(pattern[j])){match=false;break;}
            }
            if(!match)continue;
            // Negated requests should NOT be converted into positive preferences.
            boolean negated=false;
            for(int j=Math.max(0,i-3);j<i;j++) {
                if(words[j].equals("no") || words[j].equals("evitar")
                        || words[j].equals("sin") || words[j].equals("menos")) {
                    negated=true;break;
                }
            }
            if(!negated)return true;
        }
        return false;
    }

    private static String normalize(String text) {
        if(text==null)return "";
        String stripped=Normalizer.normalize(text,Normalizer.Form.NFD)
                .replaceAll("\\p{M}+","");
        return stripped.toLowerCase(Locale.ROOT)
                .replaceAll("[^a-z0-9]+"," ").trim().replaceAll(" +"," ");
    }
}

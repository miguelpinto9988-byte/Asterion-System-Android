package pe.asterionsystem.app;

import static org.junit.Assert.*;
import java.io.StringReader;
import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.Test;
import pe.asterionsystem.core.engine.exercises.ExerciseCatalog;
import pe.asterionsystem.core.engine.exercises.ExerciseCatalogLoader;

public final class AvatarVariantCatalogTest {
    private Path root(){return Files.isDirectory(Path.of("progress-engine"))?Path.of("."):Path.of("..");}
    private ExerciseCatalog taxonomy() throws Exception{
        Path p=root().resolve("progress-engine/src/main/resources/asterion_exercises/v1");
        try(java.io.Reader a=Files.newBufferedReader(p.resolve("families.tsv"));
            java.io.Reader b=Files.newBufferedReader(p.resolve("types.tsv"));
            java.io.Reader c=Files.newBufferedReader(p.resolve("variants.tsv"))){
            return ExerciseCatalogLoader.load(a,b,c);
        }
    }
    private String catalog() throws Exception{
        return Files.readString(root().resolve("app/src/main/assets/asterion_exercises/v1/avatar_motion_commands.tsv"));
    }
    @Test public void allIdsLoadAndEveryNewRecipeHasAnExactSignature() throws Exception{
        AvatarExerciseCommandCatalog c=AvatarExerciseCommandCatalog.load(new StringReader(catalog()),taxonomy());
        assertEquals(1257,c.size());
        assertEquals(78,c.all().values().stream().filter(e->e.canIllustrate()).count());
        for(AvatarVariantScene.Recipe r:AvatarVariantScene.all().values())r.requireConsistent(c.require(r.id));
        assertTrue(c.require("curl_biceps_v06").supervision);
        assertTrue(c.require("curl_biceps_v06").safeDescription().contains("supervisada"));
        assertFalse(c.require("curl_biceps_v05").canIllustrate());
        assertFalse(c.require("marcha_v02").canIllustrate());
    }
    private String alter(String source,String id,int column,String value){
        StringBuilder out=new StringBuilder();
        for(String line:source.split("\n")){
            if(line.startsWith(id+"\t")){
                String[] fields=line.split("\t",-1);fields[column]=value;line=String.join("\t",fields);
            }
            out.append(line).append('\n');
        }
        return out.toString();
    }
    @Test public void wrongCommandEquipmentCameraOrSupervisionCannotAuthorizeARecipe() throws Exception{
        String source=catalog();ExerciseCatalog t=taxonomy();
        for(Object[] change:new Object[][]{{1,"SQUAT"},{2,"FRONT"},{3,"ARM_CURL"},
                {4,"SPEC_ONLY"},{6,"5"},{7,"bilateral"},{8,"sin_equipo"},{9,"1"}}){
            String modified=alter(source,"curl_biceps_v01",(Integer)change[0],(String)change[1]);
            assertThrows(IllegalArgumentException.class,()->AvatarExerciseCommandCatalog.load(new StringReader(modified),t));
        }
        String unsupported=alter(alter(source,"curl_biceps_v05",3,"AUTHORED_VARIANT"),
                "curl_biceps_v05",4,"ILLUSTRATIVE");
        assertThrows(IllegalArgumentException.class,()->AvatarExerciseCommandCatalog.load(new StringReader(unsupported),t));
    }
}

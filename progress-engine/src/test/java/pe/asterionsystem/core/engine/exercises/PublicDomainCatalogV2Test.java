package pe.asterionsystem.core.engine.exercises;

import static org.junit.Assert.*;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.util.HashSet;
import org.junit.Test;
import pe.asterionsystem.core.engine.progression.ExerciseVolumeProfileCatalog;

/**
 * Full v2 generated TSV integration. Never affects the canonical v1 tests.
 */
public final class PublicDomainCatalogV2Test {
    private Reader resource(String file) {
        InputStream input=getClass().getResourceAsStream("/asterion_exercises/v2/"+file);
        assertNotNull("Falta archivo v2: "+file,input);
        return new InputStreamReader(input,StandardCharsets.UTF_8);
    }
    @Test public void historicalProgressionRemainsStableWithPublicDomainCandidates() throws Exception {
        try(Reader f=resource("families.tsv");
            Reader t=resource("types.tsv");
            Reader v=resource("variants.tsv");
            Reader i=resource("intensity.tsv");
            Reader sr=resource("sport_registry.tsv");
            Reader st=resource("sport_tags.tsv");
            Reader volume=resource("volume_profiles.tsv")) {
            ExerciseCatalog catalog=ExerciseCatalogLoader.load(f,t,v,i,sr,st);
            ExerciseVolumeProfileCatalog profiles=
                    ExerciseVolumeProfileCatalog.load(volume,catalog);
            assertEquals(catalog.variants().size(),profiles.size());
            assertTrue(catalog.variants().size()>1257);
            assertTrue(catalog.families().size()>=155);
            assertTrue(catalog.types().size()>=474);
            assertEquals("Sentadilla a silla",
                    catalog.variant("sentadilla_bilateral_v01").orElseThrow().name());
            var historical=catalog.variant("sentadilla_bilateral_v01").orElseThrow();
            assertEquals(1,historical.difficulty());
            assertEquals(17,profiles.require(historical).initialValue);
            var ids=new HashSet<String>();
            int newCount=0;
            for(ExerciseVariant item:catalog.variants()) {
                assertTrue("ID repetido: "+item.id(),ids.add(item.id()));
                if(!item.id().startsWith("pd_e_"))continue;
                newCount++;
                assertEquals("Nivel no revisado",0,item.difficulty());
                assertTrue(item.supervisionRecommended());
                assertFalse(item.eligibleForAutomaticSuggestion());
                assertFalse(catalog.eligibleForAutomaticSuggestion(item));
                assertFalse(profiles.require(item).automaticEligible);
            }
            assertEquals(newCount,catalog.variants().size()-1257);
            assertTrue(newCount>0);
        }
    }
}

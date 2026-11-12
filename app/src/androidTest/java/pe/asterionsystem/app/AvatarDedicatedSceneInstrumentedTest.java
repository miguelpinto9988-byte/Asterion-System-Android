package pe.asterionsystem.app;

import static org.junit.Assert.*;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.platform.app.InstrumentationRegistry;
import java.io.File;
import java.io.FileOutputStream;
import org.junit.Test;
import org.junit.runner.RunWith;
import pe.asterionsystem.app.data.AssetExerciseCatalogRepository;
import pe.asterionsystem.app.data.AssetAvatarExerciseCommandRepository;
import pe.asterionsystem.core.engine.exercises.ExerciseCatalog;

@RunWith(AndroidJUnit4.class)
public final class AvatarDedicatedSceneInstrumentedTest {
    @Test public void exportedNativeCanvasMatchesFiveMatAndChairCatalogExamples(){
        Context context=InstrumentationRegistry.getInstrumentation().getTargetContext();
        ExerciseCatalog c=new AssetExerciseCatalogRepository(context).load();
        AvatarExerciseCommandCatalog commands=
                new AssetAvatarExerciseCommandRepository(context).load(c);
        File base=context.getExternalFilesDir(null);
        assertNotNull(base);
        File dir=new File(base,"asterion-avatar-supported-mat-chair");
        assertTrue(dir.isDirectory()||dir.mkdirs());
        String[] ids={"puente_gluteo_v01","puente_gluteo_v02",
                "extension_rodilla_sin_carga_v01",
                "dorsiflexion_tibial_v01","alcances_tronco_sentado_v02",
                "puente_gluteo_v03","extension_rodilla_sin_carga_v02",
                "extension_rodilla_sin_carga_v03",
                "puente_gluteo_v04","dorsiflexion_tobillo_v01",
                "elevacion_gemelos_v01","balanceo_talon_punta_sentado_v01"};
        for(String id:ids){
            AvatarExerciseCommandCatalog.Entry entry=commands.require(id);
            assertTrue(entry.canIllustrate());
            assertNotNull(entry.clip.dedicatedScene());
        }
        InstrumentationRegistry.getInstrumentation().runOnMainSync(()->{
            NativeAvatarView view=new NativeAvatarView(context);
            view.layout(0,0,400,480);
            view.setPlaying(false);
            for(boolean purple:new boolean[]{false,true}){
                view.setFemale(purple);
                for(String id:ids){
                    AvatarExerciseCommandCatalog.Entry entry=commands.require(id);
                    view.setDedicatedScene(entry.clip.dedicatedScene());
                    // Alternating knees need two middle frames: first leg at
                    // 25% and the opposite leg at 75%, not a neutral 50%.
                    float[] phases=entry.clip.dedicatedScene()==
                            AvatarDedicatedScene.Kind.SEATED_KNEE_EXTENSION_ALTERNATING
                            ?new float[]{0f,.25f,.75f,1f}
                            :new float[]{0f,.49f,1f};
                    for(int i=0;i<phases.length;i++){
                        view.setPhase(phases[i]);
                        Bitmap b=Bitmap.createBitmap(400,480,Bitmap.Config.ARGB_8888);
                        try{
                            view.draw(new Canvas(b));
                            File out=new File(dir,(purple?"morado_":"azul_")+id+"_"+i+".png");
                            try(FileOutputStream stream=new FileOutputStream(out)){
                                assertTrue(b.compress(Bitmap.CompressFormat.PNG,100,stream));
                            }catch(java.io.IOException error){
                                throw new IllegalStateException(error);
                            }
                            assertTrue(out.length()>2000);
                            assertNotEquals(0,b.getPixel(200,215));
                        }finally{b.recycle();}
                    }
                }
            }
        });
        File[] files=dir.listFiles((p,name)->name.endsWith(".png"));
        assertNotNull(files);
        assertEquals(74,files.length);
    }
}

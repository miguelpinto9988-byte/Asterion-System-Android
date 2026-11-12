package pe.asterionsystem.app;

import static org.junit.Assert.*;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.platform.app.InstrumentationRegistry;
import java.io.File;
import java.io.FileOutputStream;
import java.util.Map;
import org.junit.Test;
import org.junit.runner.RunWith;
import pe.asterionsystem.app.data.AssetExerciseCatalogRepository;
import pe.asterionsystem.app.data.AssetAvatarExerciseCommandRepository;
import pe.asterionsystem.core.engine.exercises.ExerciseCatalog;
import pe.asterionsystem.core.engine.exercises.ExerciseVariant;

@RunWith(AndroidJUnit4.class)
public final class AvatarExerciseCommandCatalogInstrumentedTest {
    private AvatarExerciseCommandCatalog load(){
        Context context=InstrumentationRegistry.getInstrumentation().getTargetContext();
        ExerciseCatalog catalog=new AssetExerciseCatalogRepository(context).load();
        return new AssetAvatarExerciseCommandRepository(context).load(catalog);
    }
    @Test public void everyRealCatalogExerciseHasAnExplicitCommandWithoutFakeClips(){
        Context context=InstrumentationRegistry.getInstrumentation().getTargetContext();
        ExerciseCatalog catalog=new AssetExerciseCatalogRepository(context).load();
        AvatarExerciseCommandCatalog profiles=
                new AssetAvatarExerciseCommandRepository(context).load(catalog);
        assertEquals(1257,profiles.size());
        int illustrated=0,unreviewed=0;
        for(ExerciseVariant exercise:catalog.variants()){
            AvatarExerciseCommandCatalog.Entry e=profiles.require(exercise.id());
            assertEquals(exercise.id(),e.variantId);
            assertEquals(exercise.difficulty(),e.level);
            assertNotNull(e.timeline());
            assertEquals(24,e.timeline().describe(.5f).split("\n").length);
            assertTrue(e.safeDescription().contains(exercise.id()));
            assertEquals(e.canIllustrate(),
                    e.status==AvatarExerciseCommandCatalog.Status.ILLUSTRATIVE);
            if(e.canIllustrate()) {
                illustrated++;
                assertTrue(exercise.difficulty()<=3);
                assertNotEquals(AvatarExerciseCommandCatalog.Clip.NONE,e.clip);
            }else{
                unreviewed++;
                assertEquals(AvatarExerciseCommandCatalog.Clip.NONE,e.clip);
            }
        }
        assertEquals(11,illustrated);
        assertEquals(1246,unreviewed);
        for(String id:new String[]{"curl_biceps_v01","flexion_rodilla_bipeda_v03",
                "equilibrio_unipodal_v04","caminata_banda_v01","elevacion_lateral_v01",
                "inclinacion_cervical_suave_v01"}){
            assertTrue(profiles.require(id).canIllustrate());
        }
        for(String id:new String[]{"puente_gluteo_v02","rotacion_cervical_suave_v02",
                "alcance_unipodal_v01"}){
            assertFalse("No substitute animation for "+id,profiles.require(id).canIllustrate());
        }
    }


    /** Twenty-four authentic Canvas frames: four distinct front movements,
     * both retained palettes and complete beginning/peak/end states. */
    @Test public void exportFrontalLateralBalanceAndSideStepFrames(){
        Context context=InstrumentationRegistry.getInstrumentation().getTargetContext();
        File root=context.getExternalFilesDir(null);
        assertNotNull(root);
        File folder=new File(root,"asterion-avatar-new-front-samples");
        assertTrue(folder.isDirectory()||folder.mkdirs());
        InstrumentationRegistry.getInstrumentation().runOnMainSync(()->{
            NativeAvatarView view=new NativeAvatarView(context);
            view.layout(0,0,400,480);
            view.setPlaying(false);
            AvatarMultiViewKinematics.ViewMode[] scenes={
                    AvatarMultiViewKinematics.ViewMode.FRONT_LATERAL,
                    AvatarMultiViewKinematics.ViewMode.FRONT_BALANCE,
                    AvatarMultiViewKinematics.ViewMode.FRONT_SIDE_STEP,
                    AvatarMultiViewKinematics.ViewMode.FRONT_NECK_TILT};
            String[] ids={"elevacion_lateral_v01","equilibrio_unipodal_v04",
                    "caminata_banda_v01","inclinacion_cervical_suave_v01"};
            for(boolean purple:new boolean[]{false,true}){
                view.setFemale(purple);
                for(int i=0;i<scenes.length;i++){
                    view.setViewMode(scenes[i]);
                    for(int step=0;step<3;step++){
                        view.setPhase(step==0?0f:step==1?.49f:1f);
                        Bitmap bm=Bitmap.createBitmap(400,480,Bitmap.Config.ARGB_8888);
                        try{
                            view.draw(new Canvas(bm));
                            File image=new File(folder,
                                    (purple?"morado_":"azul_")+ids[i]+"_"+step+".png");
                            try(FileOutputStream output=new FileOutputStream(image)){
                                assertTrue(bm.compress(Bitmap.CompressFormat.PNG,100,output));
                            }catch(java.io.IOException ex){
                                throw new IllegalStateException(ex);
                            }
                            assertTrue(image.length()>2000);
                            assertNotEquals(0,bm.getPixel(200,220));
                        }finally{bm.recycle();}
                    }
                }
            }
        });
        File[] output=folder.listFiles((dir,name)->name.endsWith(".png"));
        assertNotNull(output);
        assertEquals(24,output.length);
    }

    @Test public void exportRealAndroidFramesForTwoPreviouslyUnanimatedJointMotions(){
        Context context=InstrumentationRegistry.getInstrumentation().getTargetContext();
        File root=context.getExternalFilesDir(null);
        assertNotNull(root);
        File folder=new File(root,"asterion-avatar-command-samples");
        assertTrue(folder.isDirectory()||folder.mkdirs());
        InstrumentationRegistry.getInstrumentation().runOnMainSync(()->{
            NativeAvatarView view=new NativeAvatarView(context);
            view.layout(0,0,400,480);
            view.setPlaying(false);
            view.setViewMode(AvatarMultiViewKinematics.ViewMode.SIDE);
            AvatarMotionEngine.Motion[] motions={
                    AvatarMotionEngine.Motion.ARM_CURL,
                    AvatarMotionEngine.Motion.KNEE_CURL
            };
            String[] ids={"curl_biceps_v01","flexion_rodilla_bipeda_v03"};
            for(boolean purple:new boolean[]{false,true}){
                view.setFemale(purple);
                for(int m=0;m<motions.length;m++){
                    view.setMotion(motions[m]);
                    for(int f=0;f<3;f++){
                        view.setPhase(f==0?0f:f==1?.49f:1f);
                        Bitmap image=Bitmap.createBitmap(400,480,Bitmap.Config.ARGB_8888);
                        try{
                            view.draw(new Canvas(image));
                            File output=new File(folder,
                                    (purple?"morado_":"azul_")+ids[m]+"_"+f+".png");
                            try(FileOutputStream stream=new FileOutputStream(output)){
                                assertTrue(image.compress(Bitmap.CompressFormat.PNG,100,stream));
                            }catch(java.io.IOException error){
                                throw new IllegalStateException(error);
                            }
                            assertTrue(output.length()>2000);
                            assertNotEquals(0,image.getPixel(200,220));
                        }finally{image.recycle();}
                    }
                }
            }
        });
        File[] frames=folder.listFiles((parent,name)->name.endsWith(".png"));
        assertNotNull(frames);
        assertEquals(12,frames.length);
    }
}

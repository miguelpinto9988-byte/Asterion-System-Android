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

@RunWith(AndroidJUnit4.class)
public final class AvatarVariantSceneInstrumentedTest {
    @Test public void everyRecipeExportsNativeFramesAndRestoresLegacyModes(){
        Context context=InstrumentationRegistry.getInstrumentation().getTargetContext();
        AvatarExerciseCommandCatalog catalog=new AssetAvatarExerciseCommandRepository(context)
                .load(new AssetExerciseCatalogRepository(context).load());
        File folder=new File(context.getExternalFilesDir(null),"asterion-avatar-variant-scenes");
        assertTrue(folder.isDirectory()||folder.mkdirs());
        InstrumentationRegistry.getInstrumentation().runOnMainSync(()->{
            NativeAvatarView view=new NativeAvatarView(context);
            view.layout(0,0,400,480);view.setPlaying(false);
            for(AvatarVariantScene.Recipe recipe:AvatarVariantScene.all().values()){
                recipe.requireConsistent(catalog.require(recipe.id));
                for(boolean purple:new boolean[]{false,true}){
                    view.setFemale(purple);view.setVariantScene(recipe);
                    assertEquals(recipe,view.getVariantScene());
                    assertTrue(view.getContentDescription().toString().contains(recipe.label));
                    int index=0;
                    for(float phase:new float[]{0f,.25f,.5f,.75f,1f}){
                        view.setPhase(phase);
                        Bitmap bitmap=Bitmap.createBitmap(400,480,Bitmap.Config.ARGB_8888);
                        try{
                            view.draw(new Canvas(bitmap));
                            File target=new File(folder,(purple?"morado_":"azul_")+recipe.id+"_"+(index++)+".png");
                            try(FileOutputStream out=new FileOutputStream(target)){
                                assertTrue(bitmap.compress(Bitmap.CompressFormat.PNG,100,out));
                            }catch(java.io.IOException e){throw new IllegalStateException(e);}
                            assertTrue(target.length()>2000);
                            assertNotEquals(0,bitmap.getPixel(200,215));
                        }finally{bitmap.recycle();}
                    }
                }
            }
            view.setMotion(AvatarMotionEngine.Motion.SQUAT);
            assertNull(view.getVariantScene());
            view.setVariantScene(AvatarVariantScene.find("curl_biceps_v01"));
            view.setDedicatedScene(AvatarDedicatedScene.Kind.SEATED_TOE_RAISE);
            assertNull(view.getVariantScene());
            view.setVariantScene(AvatarVariantScene.find("curl_biceps_v01"));
            view.setViewMode(AvatarMultiViewKinematics.ViewMode.FRONT);
            assertNull(view.getVariantScene());
        });
        File[] images=folder.listFiles((dir,name)->name.endsWith(".png"));
        assertNotNull(images);assertEquals(AvatarVariantScene.all().size()*10,images.length);
    }
}

package pe.asterionsystem.app;

import static org.junit.Assert.*;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.platform.app.InstrumentationRegistry;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * Ensures both native palettes really paint an articulated squat pose.
 * Runs without an Activity, network, MP4 player or third-party engine.
 */
@RunWith(AndroidJUnit4.class)
public final class NativeAvatarRenderTest {
    @Test public void blueAndPurpleDrawDifferentPausedPosePixels() {
        Context context=InstrumentationRegistry.getInstrumentation().getTargetContext();
        final Bitmap[] frames=new Bitmap[2];
        InstrumentationRegistry.getInstrumentation().runOnMainSync(()->{
            NativeAvatarView view=new NativeAvatarView(context);
            view.layout(0,0,400,480);
            view.setPlaying(false);
            view.setPhase(.49f);
            assertEquals(.49f,view.getPhase(),.001f);
            for(int i=0;i<2;i++) {
                view.setFemale(i==1);
                frames[i]=Bitmap.createBitmap(400,480,Bitmap.Config.ARGB_8888);
                Canvas canvas=new Canvas(frames[i]);
                view.draw(canvas);
            }
        });
        assertNotNull(frames[0]);assertNotNull(frames[1]);
        int male=frames[0].getPixel(170,208);
        int female=frames[1].getPixel(170,208);
        assertNotEquals("Different tops should have visibly distinct colors",male,female);
        frames[0].recycle();frames[1].recycle();
    }
    @Test public void playbackParametersRejectNonFiniteValuesAndRemainAccessible() {
        Context context=InstrumentationRegistry.getInstrumentation().getTargetContext();
        InstrumentationRegistry.getInstrumentation().runOnMainSync(()->{
            NativeAvatarView view=new NativeAvatarView(context);
            view.setPlaying(false);
            view.setPhase(Float.NaN);
            assertEquals(0f,view.getPhase(),.0001f);
            view.setPhase(Float.NEGATIVE_INFINITY);
            assertEquals(0f,view.getPhase(),.0001f);
            view.setPhase(Float.POSITIVE_INFINITY);
            assertEquals(1f,view.getPhase(),.0001f);
            view.setSpeed(1f);
            view.setSpeed(Float.NaN);
            assertEquals(1f,view.getSpeed(),.0001f);
            view.setSpeed(Float.POSITIVE_INFINITY);
            assertEquals(1f,view.getSpeed(),.0001f);
            view.setFemale(true);
            assertTrue(view.getContentDescription().toString().contains("Avatar morado"));
            assertTrue(view.getContentDescription().toString().contains("pausada"));
            view.setPlaying(true);
            assertTrue(view.getContentDescription().toString().contains("reproducción"));
        });
    }
}

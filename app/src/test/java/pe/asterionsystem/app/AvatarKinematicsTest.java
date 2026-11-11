package pe.asterionsystem.app;

import static org.junit.Assert.*;
import org.junit.Test;

/** Exercises correctness of the native illustrative two-bone 2D skeleton. */
public final class AvatarKinematicsTest {
    private static final float EPS=.03f;
    @Test public void shinAndThighLengthsStayConstantForFullMovement() {
        for(int i=0;i<=100;i++) {
            AvatarKinematics.Pose p=AvatarKinematics.squat(i/100f);
            assertEquals(AvatarKinematics.UPPER_LEG,p.hip.distance(p.knee),EPS);
            assertEquals(AvatarKinematics.LOWER_LEG,p.knee.distance(p.ankle),EPS);
            assertEquals(AvatarKinematics.UPPER_LEG,p.farHip.distance(p.farKnee),EPS);
            assertEquals(AvatarKinematics.LOWER_LEG,p.farKnee.distance(p.farAnkle),EPS);
            assertEquals(AvatarKinematics.TORSO,p.hip.distance(p.shoulder),EPS);
            assertTrue("Ankle does not float",p.ankle.y==364f);
            assertTrue("Both ankles stay planted",p.farAnkle.y==363f);
            assertTrue("Head stays above torso",p.head.y<p.shoulder.y);
            assertTrue("Knee should face forward",p.knee.x>p.ankle.x-1f);
        }
    }
    @Test public void pelvisDescendsContinuouslyWhenSquatting() {
        float lastY=AvatarKinematics.squat(0).hip.y;
        for(int i=1;i<=100;i++) {
            AvatarKinematics.Pose p=AvatarKinematics.squat(i/100f);
            assertTrue(p.hip.y+EPS>=lastY);
            lastY=p.hip.y;
        }
        assertTrue(AvatarKinematics.squat(1).hip.y
                >AvatarKinematics.squat(0).hip.y+60);
    }
    @Test public void oneCycleHasSoftStartHoldAndReturn() {
        assertEquals(0f,AvatarKinematics.depthAtPhase(0f),EPS);
        assertEquals(0f,AvatarKinematics.depthAtPhase(.08f),EPS);
        assertEquals(1f,AvatarKinematics.depthAtPhase(.45f),EPS);
        assertEquals(1f,AvatarKinematics.depthAtPhase(.54f),EPS);
        assertEquals(0f,AvatarKinematics.depthAtPhase(.96f),EPS);
        for(int i=0;i<=100;i++) {
            float d=AvatarKinematics.depthAtPhase(i/100f);
            assertTrue(d>=0f&&d<=1f);
        }
    }
    @Test public void invalidPoseDepthCannotProduceNan() {
        AvatarKinematics.Pose one=AvatarKinematics.squat(Float.NaN);
        assertTrue(Float.isFinite(one.knee.x)&&Float.isFinite(one.knee.y));
        AvatarKinematics.Pose two=AvatarKinematics.squat(10f);
        assertTrue(Float.isFinite(two.knee.x)&&Float.isFinite(two.knee.y));
    }
}

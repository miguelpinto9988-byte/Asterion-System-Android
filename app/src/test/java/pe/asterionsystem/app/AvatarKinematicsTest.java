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
    @Test public void armBonesRemainConstantThroughFullIllustratedSquat() {
        for(int i=0;i<=100;i++) {
            AvatarKinematics.Pose p=AvatarKinematics.squat(i/100f);
            assertEquals(AvatarKinematics.UPPER_ARM,
                    p.shoulder.distance(p.elbow),EPS);
            assertEquals(AvatarKinematics.FOREARM,
                    p.elbow.distance(p.wrist),EPS);
            assertEquals(AvatarKinematics.UPPER_ARM,
                    p.farShoulder.distance(p.farElbow),EPS);
            assertEquals(AvatarKinematics.FOREARM,
                    p.farElbow.distance(p.farWrist),EPS);
        }
    }
    @Test public void elbowsMoveContinuouslyWithoutTeleporting() {
        AvatarKinematics.Pose previous=AvatarKinematics.squat(0f);
        for(int i=1;i<=200;i++) {
            AvatarKinematics.Pose current=AvatarKinematics.squat(i/200f);
            assertTrue("Front elbow jumped",current.elbow.distance(previous.elbow)<3f);
            assertTrue("Far elbow jumped",current.farElbow.distance(previous.farElbow)<3f);
            previous=current;
        }
    }
    @Test public void invalidArmSegmentLengthsAreRejected() {
        AvatarKinematics.Point a=new AvatarKinematics.Point(0,0);
        AvatarKinematics.Point b=new AvatarKinematics.Point(10,10);
        assertThrows(IllegalArgumentException.class,
                ()->AvatarKinematics.elbowBelow(a,b,0f,20f));
        assertThrows(IllegalArgumentException.class,
                ()->AvatarKinematics.elbowBelow(a,b,Float.NaN,20f));
    }
    @Test public void customBodySpecPreservesBoneLengthsAndImmutablePose() {
        AvatarBodySpec spec=new AvatarBodySpec(
                96f,98f,120f,60f,64f,22f,31f,23f,
                34f,25f,27f,18f,1f);
        AvatarPose atRest=AvatarKinematics.squat(0f,spec);
        for(int i=0;i<=100;i++) {
            AvatarPose p=AvatarKinematics.squat(i/100f,spec);
            assertEquals(spec.upperLeg,p.hip.distance(p.knee),EPS);
            assertEquals(spec.lowerLeg,p.knee.distance(p.ankle),EPS);
            assertEquals(spec.torso,p.hip.distance(p.shoulder),EPS);
            assertEquals(spec.upperArm,p.shoulder.distance(p.elbow),EPS);
            assertEquals(spec.forearm,p.elbow.distance(p.wrist),EPS);
        }
        assertEquals(364f,atRest.ankle.y,EPS);
    }

    @Test public void squatNowStartsWithExtendedKneesAndElbows(){
        AvatarBodySpec spec=AvatarBodySpec.DEFAULT;
        AvatarPose start=AvatarKinematics.squat(0f);
        AvatarPose finish=AvatarKinematics.squat(1f);
        assertTrue(start.hip.distance(start.ankle)>
                spec.upperLeg+spec.lowerLeg-.55f);
        assertTrue(start.farHip.distance(start.farAnkle)>
                spec.upperLeg+spec.lowerLeg-.55f);
        assertTrue(start.shoulder.distance(start.wrist)>
                spec.upperArm+spec.forearm-.08f);
        assertTrue(start.farShoulder.distance(start.farWrist)>
                spec.upperArm+spec.forearm-.08f);
        assertTrue(finish.hip.y>start.hip.y+75f);
        assertTrue(finish.hip.distance(finish.ankle)<
                start.hip.distance(start.ankle)-50f);
        assertEquals(start.ankle.x,finish.ankle.x,.001f);
        assertEquals(start.ankle.y,finish.ankle.y,.001f);
    }

    @Test public void invalidPoseDepthCannotProduceNan() {
        AvatarKinematics.Pose one=AvatarKinematics.squat(Float.NaN);
        assertTrue(Float.isFinite(one.knee.x)&&Float.isFinite(one.knee.y));
        AvatarKinematics.Pose two=AvatarKinematics.squat(10f);
        assertTrue(Float.isFinite(two.knee.x)&&Float.isFinite(two.knee.y));
    }
}

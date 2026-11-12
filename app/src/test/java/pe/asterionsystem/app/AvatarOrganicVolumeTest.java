package pe.asterionsystem.app;

import static org.junit.Assert.*;
import org.junit.Test;

/** Curved continuous joint envelopes do not detach or protrude at a hinge. */
public final class AvatarOrganicVolumeTest {
    @Test public void hingeRadiusNeverExceedsBoneEnvelope(){
        AvatarKinematics.Point a=new AvatarKinematics.Point(40,70);
        AvatarKinematics.Point j=new AvatarKinematics.Point(100,70);
        for(int degrees=0;degrees<=175;degrees+=5){
            double rad=Math.toRadians(degrees);
            AvatarKinematics.Point b=new AvatarKinematics.Point(
                    j.x+(float)Math.cos(rad)*58f,
                    j.y+(float)Math.sin(rad)*58f);
            AvatarOrganicVolume.JointMetrics m=AvatarOrganicVolume.measure(
                    a,j,b,25f,20f,16f);
            assertEquals(60f,m.upperLength,.01f);
            assertEquals(58f,m.lowerLength,.01f);
            assertTrue("No protruding elbow sphere",m.bridgeRadius<=10.01f);
            assertTrue(m.bridgeRadius>0f);
            assertTrue(Float.isFinite(m.bendDegrees));
        }
    }

    @Test public void projectedArmsCanBeSkinnedInEveryFrame(){
        AvatarBodySpec body=AvatarBodySpec.DEFAULT;
        for(AvatarMultiViewKinematics.ViewMode mode:
                new AvatarMultiViewKinematics.ViewMode[]{
                AvatarMultiViewKinematics.ViewMode.FRONT,
                AvatarMultiViewKinematics.ViewMode.FRONT_SQUAT,
                AvatarMultiViewKinematics.ViewMode.FRONT_JACKS,
                AvatarMultiViewKinematics.ViewMode.FRONT_ELBOW,
                AvatarMultiViewKinematics.ViewMode.TOP}){
            for(int i=0;i<=240;i++){
                float p=i/240f;
                AvatarMultiViewKinematics.Pose v;
                switch(mode){
                    case FRONT:v=AvatarMultiViewKinematics.frontArms(p,body);break;
                    case FRONT_SQUAT:v=AvatarMultiViewKinematics.frontWideSquat(p,body);break;
                    case FRONT_JACKS:v=AvatarMultiViewKinematics.frontJumpingJack(p,body);break;
                    case FRONT_ELBOW:v=AvatarMultiViewKinematics.frontElbowFlex(p,body);break;
                    case TOP:v=AvatarMultiViewKinematics.diamondTop(p,body);break;
                    default:throw new AssertionError();
                }
                AvatarOrganicVolume.JointMetrics left=AvatarOrganicVolume.measure(
                        v.leftShoulder,v.leftElbow,v.leftWrist,
                        body.upperArmWidth,body.forearmWidth,body.forearmWidth*.7f);
                AvatarOrganicVolume.JointMetrics right=AvatarOrganicVolume.measure(
                        v.rightShoulder,v.rightElbow,v.rightWrist,
                        body.upperArmWidth,body.forearmWidth,body.forearmWidth*.7f);
                assertTrue(left.bridgeRadius>0f);
                assertTrue(right.bridgeRadius>0f);
            }
        }
    }

    @Test public void invalidWidthsAndCollapsedLinksAreRejected(){
        AvatarKinematics.Point a=new AvatarKinematics.Point(0,0);
        AvatarKinematics.Point j=new AvatarKinematics.Point(50,10);
        AvatarKinematics.Point b=new AvatarKinematics.Point(100,20);
        assertThrows(IllegalArgumentException.class,()->
                AvatarOrganicVolume.measure(a,j,b,Float.NaN,18f,12f));
        assertThrows(IllegalArgumentException.class,()->
                AvatarOrganicVolume.measure(a,j,b,22f,-4f,12f));
        assertThrows(IllegalArgumentException.class,()->
                AvatarOrganicVolume.measure(a,a,b,22f,18f,12f));
        assertThrows(IllegalArgumentException.class,()->
                AvatarOrganicVolume.measure(a,j,j,22f,18f,12f));
        assertThrows(IllegalArgumentException.class,()->
                AvatarOrganicVolume.measure(a,j,b,400f,18f,12f));
        assertThrows(IllegalArgumentException.class,()->
                AvatarOrganicVolume.measure(null,j,b,22f,18f,12f));
    }
}

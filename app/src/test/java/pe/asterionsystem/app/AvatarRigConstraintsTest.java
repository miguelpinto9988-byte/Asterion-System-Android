package pe.asterionsystem.app;

import static org.junit.Assert.*;
import org.junit.Test;

/** Invariants for analytical 2-bone IK and multi-link FABRIK. */
public final class AvatarRigConstraintsTest {
    private static final float EPS=.06f;
    private static AvatarKinematics.Point p(float x,float y){
        return new AvatarKinematics.Point(x,y);
    }
    @Test public void noStretchForUnreachableTarget() {
        AvatarRigConstraints.TwoBone s=AvatarRigConstraints.twoBone(
                p(0,0),p(500,50),58f,63f,1,0f,170f);
        assertTrue(s.targetLimited);
        assertEquals(58f,s.root.distance(s.joint),EPS);
        assertEquals(63f,s.joint.distance(s.end),EPS);
        assertTrue(s.root.distance(s.end)<=121.001f);
    }
    @Test public void exactlyReachableEndRemainsAttached() {
        AvatarKinematics.Point requested=p(60,70);
        AvatarRigConstraints.TwoBone s=AvatarRigConstraints.twoBone(
                p(0,0),requested,58f,63f,1,0f,170f);
        assertFalse(s.targetLimited);
        assertEquals(0f,s.end.distance(requested),EPS);
        assertEquals(58f,s.root.distance(s.joint),EPS);
        assertEquals(63f,s.joint.distance(s.end),EPS);
    }
    @Test public void minAndMaxBendLimitsApplyWithoutChangingBoneLengths() {
        for(float radius:new float[]{.1f,1f,15f,40f,180f,10000f}){
            AvatarRigConstraints.TwoBone s=AvatarRigConstraints.twoBone(
                    p(0,0),p(radius,0),50f,55f,-1,10f,120f);
            assertTrue(s.flexDegrees>=9.9f&&s.flexDegrees<=120.1f);
            assertEquals(50f,s.root.distance(s.joint),EPS);
            assertEquals(55f,s.joint.distance(s.end),EPS);
        }
    }
    @Test public void plantedContactIsExactWithRootProjection() {
        AvatarKinematics.Point foot=p(180,360);
        AvatarKinematics.Point hip=p(0,0);
        AvatarKinematics.Point compatible=AvatarRigConstraints.rootForPinnedEnd(
                hip,foot,94f,96f,0f,165f);
        AvatarRigConstraints.TwoBone leg=AvatarRigConstraints.twoBone(
                compatible,foot,94f,96f,-1,0f,165f);
        assertFalse(leg.targetLimited);
        assertEquals(0f,leg.end.distance(foot),EPS);
        assertEquals(94f,leg.root.distance(leg.joint),EPS);
        assertEquals(96f,leg.joint.distance(leg.end),EPS);
    }
    @Test public void fabrikMultiBoneChainsAlwaysKeepLengths() {
        float[] links={20f,28f,24f,19f};
        AvatarKinematics.Point origin=p(0,0);
        for(int i=0;i<=24;i++){
            AvatarKinematics.Point target=p(24f+i*.7f,18f+i*.3f);
            AvatarRigConstraints.Chain s=AvatarRigConstraints.fabrik(
                    origin,target,links,40,.1f);
            assertEquals(links.length+1,s.joints.length);
            assertEquals(0f,s.joints[0].distance(origin),EPS);
            for(int j=0;j<links.length;j++){
                assertEquals("link "+j,links[j],
                        s.joints[j].distance(s.joints[j+1]),EPS);
            }
            assertTrue(Float.isFinite(s.targetError));
        }
    }
    @Test public void fabrikOutOfReachDoesNotStretch(){
        AvatarRigConstraints.Chain s=AvatarRigConstraints.fabrik(
                p(0,0),p(1000,0),new float[]{10,15,20},20,.01f);
        assertTrue(s.targetLimited);
        assertEquals(45f,s.joints[0].distance(s.joints[3]),EPS);
        assertEquals(10f,s.joints[0].distance(s.joints[1]),EPS);
        assertEquals(15f,s.joints[1].distance(s.joints[2]),EPS);
        assertEquals(20f,s.joints[2].distance(s.joints[3]),EPS);
    }
    @Test public void invalidCoordinatesAndLimitsRejected(){
        assertThrows(IllegalArgumentException.class,()->
                AvatarRigConstraints.twoBone(p(Float.NaN,0),p(5,6),
                        10,10,1,0,170));
        assertThrows(IllegalArgumentException.class,()->
                AvatarRigConstraints.twoBone(p(0,0),p(5,6),
                        10,10,1,100,90));
        assertThrows(IllegalArgumentException.class,()->
                AvatarRigConstraints.fabrik(p(0,0),p(5,6),
                        new float[]{10,0,10},10,.1f));
        assertThrows(IllegalArgumentException.class,()->
                AvatarRigConstraints.twoBone(p(0,0),p(5,6),
                        10,10,3,0,170));
    }
}

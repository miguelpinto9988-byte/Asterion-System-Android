package pe.asterionsystem.app;

import static org.junit.Assert.*;
import org.junit.Test;

/** Test that ground support is authored, not guessed from a camera image. */
public final class AvatarContactFrameTest {
    @Test public void diamondAlwaysContactsPalmsAndToes(){
        for(int i=0;i<=180;i++){
            AvatarContactFrame c=AvatarContactFrame.forCamera(
                    AvatarMultiViewKinematics.ViewMode.TOP,i/180f);
            c.assertProneUpperCamera();
            assertEquals(AvatarContactFrame.Ground.TOE,c.leftFoot);
            assertEquals(AvatarContactFrame.Ground.PALM,c.rightHand);
            assertFalse(c.airborne);
        }
    }

    @Test public void standingFrontKeepsSolesGrounded(){
        for(AvatarMultiViewKinematics.ViewMode camera:
                new AvatarMultiViewKinematics.ViewMode[]{
                    AvatarMultiViewKinematics.ViewMode.FRONT,
                    AvatarMultiViewKinematics.ViewMode.FRONT_SQUAT,
                    AvatarMultiViewKinematics.ViewMode.FRONT_ELBOW}){
            AvatarContactFrame c=AvatarContactFrame.forCamera(camera,.49f);
            assertEquals(AvatarContactFrame.Ground.SOLE,c.leftFoot);
            assertEquals(AvatarContactFrame.Ground.SOLE,c.rightFoot);
            assertEquals(AvatarContactFrame.Ground.NONE,c.leftHand);
        }
    }

    @Test public void frontJacksSeparateFlightFromGroundPhase(){
        AvatarContactFrame a=AvatarContactFrame.forCamera(
                AvatarMultiViewKinematics.ViewMode.FRONT_JACKS,0f);
        AvatarContactFrame flying=AvatarContactFrame.forCamera(
                AvatarMultiViewKinematics.ViewMode.FRONT_JACKS,.25f);
        AvatarContactFrame b=AvatarContactFrame.forCamera(
                AvatarMultiViewKinematics.ViewMode.FRONT_JACKS,.5f);
        assertEquals(AvatarContactFrame.Ground.SOLE,a.leftFoot);
        assertTrue(flying.airborne);
        assertEquals(AvatarContactFrame.Ground.NONE,flying.leftFoot);
        assertEquals(AvatarContactFrame.Ground.SOLE,b.rightFoot);
    }

    @Test public void burpeeContactsChangeAtAuthorDefinedTransitions(){
        AvatarContactFrame stand=AvatarContactFrame.forSide(
                AvatarMotionEngine.Motion.BURPEE,0f);
        AvatarContactFrame hands=AvatarContactFrame.forSide(
                AvatarMotionEngine.Motion.BURPEE,.5f);
        AvatarContactFrame travel=AvatarContactFrame.forSide(
                AvatarMotionEngine.Motion.BURPEE,.32f);
        assertEquals(AvatarContactFrame.Ground.NONE,stand.leftHand);
        assertEquals(AvatarContactFrame.Ground.PALM,hands.rightHand);
        assertEquals(AvatarContactFrame.Ground.TOE,hands.leftFoot);
        assertEquals(AvatarContactFrame.Ground.NONE,travel.rightFoot);
        assertEquals(AvatarContactFrame.Ground.PALM,travel.rightHand);
    }

    @Test public void sidePresetStatesExistForAllAnimations(){
        for(AvatarMotionEngine.Motion m:AvatarMotionEngine.Motion.values()){
            for(int i=0;i<=100;i++){
                AvatarContactFrame c=AvatarContactFrame.forSide(m,i/100f);
                assertNotNull(c.leftFoot);
                assertNotNull(c.rightFoot);
                assertNotNull(c.posture);
            }
        }
        assertEquals(AvatarContactFrame.Ground.SOLE,
                AvatarContactFrame.forSide(AvatarMotionEngine.Motion.MARCH,0f).leftFoot);
        assertEquals(AvatarContactFrame.Ground.NONE,
                AvatarContactFrame.forSide(AvatarMotionEngine.Motion.MARCH,.5f).leftFoot);
    }

    @Test public void invalidInputsAreRejected(){
        assertThrows(IllegalArgumentException.class,()->
                AvatarContactFrame.forCamera(null,.25f));
        assertThrows(IllegalArgumentException.class,()->
                AvatarContactFrame.forCamera(AvatarMultiViewKinematics.ViewMode.SIDE,.25f));
        assertThrows(IllegalArgumentException.class,()->
                AvatarContactFrame.forSide(null,.2f));
        assertThrows(IllegalArgumentException.class,()->
                AvatarContactFrame.forSide(AvatarMotionEngine.Motion.BURPEE,Float.NaN));
    }
}

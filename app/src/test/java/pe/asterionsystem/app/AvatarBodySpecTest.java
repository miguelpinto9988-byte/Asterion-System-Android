package pe.asterionsystem.app;

import static org.junit.Assert.*;
import org.junit.Test;

/** Immutable virtual proportions and brand palettes for the option D rig. */
public final class AvatarBodySpecTest {
    @Test public void defaultSpecMatchesExistingRig() {
        AvatarBodySpec d=AvatarBodySpec.DEFAULT;
        assertEquals(AvatarKinematics.UPPER_LEG,d.upperLeg,0f);
        assertEquals(AvatarKinematics.LOWER_LEG,d.lowerLeg,0f);
        assertEquals(AvatarKinematics.TORSO,d.torso,0f);
        assertEquals(AvatarKinematics.UPPER_ARM,d.upperArm,0f);
        assertEquals(AvatarKinematics.FOREARM,d.forearm,0f);
        assertTrue(d.chestHalfWidth>d.waistHalfWidth);
    }
    @Test public void rejectsNonFiniteAndNonPositiveDimensions() {
        assertThrows(IllegalArgumentException.class,()->
                new AvatarBodySpec(94,96,118,58,63,-1,31,23,33,24,26,17,1));
        assertThrows(IllegalArgumentException.class,()->
                new AvatarBodySpec(94,96,118,58,63,21,31,23,33,24,Float.NaN,17,1));
        assertThrows(IllegalArgumentException.class,()->
                new AvatarBodySpec(94,96,118,58,63,21,31,23,33,24,26,17,Float.POSITIVE_INFINITY));
    }
    @Test public void palettesKeepBrandAndSilhouetteIndependent() {
        assertFalse(AvatarPalette.BLUE.female);
        assertTrue(AvatarPalette.PURPLE.female);
        assertNotEquals(AvatarPalette.BLUE.top,AvatarPalette.PURPLE.top);
        assertEquals(AvatarPalette.BLUE.skin,AvatarPalette.PURPLE.skin);
    }
}

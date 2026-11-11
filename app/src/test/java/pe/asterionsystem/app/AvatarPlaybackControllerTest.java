package pe.asterionsystem.app;

import static org.junit.Assert.*;
import org.junit.Test;

/** Regression coverage for user-selected and lifecycle-triggered playback. */
public final class AvatarPlaybackControllerTest {
    @Test public void initialPlaybackStartsOnlyWhenForeground(){
        AvatarPlaybackController c=new AvatarPlaybackController();
        assertTrue(c.isPlaybackRequested());
        assertFalse(c.shouldPlay());
        c.onResume();
        assertTrue(c.shouldPlay());
    }

    @Test public void backgroundStopsFramesButReturnsToRequestedPlayback(){
        AvatarPlaybackController c=new AvatarPlaybackController();
        c.onResume();
        c.onPause();
        assertFalse(c.shouldPlay());
        assertTrue(c.isPlaybackRequested());
        c.onResume();
        assertTrue(c.shouldPlay());
    }

    @Test public void voluntaryPauseSurvivesBackgroundAndRestoration(){
        AvatarPlaybackController c=new AvatarPlaybackController();
        c.onResume();
        c.toggleRequested();
        assertFalse(c.isPlaybackRequested());
        c.onPause();
        c.onResume();
        assertFalse(c.shouldPlay());
        AvatarPlaybackController recreated=new AvatarPlaybackController();
        recreated.setPlaybackRequested(c.isPlaybackRequested());
        recreated.onResume();
        assertFalse(recreated.shouldPlay());
    }

    @Test public void scrubbingTemporarilySuspendsPlaybackWithoutChangingPreference(){
        AvatarPlaybackController c=new AvatarPlaybackController();
        c.onResume();
        c.beginScrub();
        assertFalse(c.shouldPlay());
        assertTrue(c.isPlaybackRequested());
        c.endScrub();
        assertTrue(c.shouldPlay());
    }

    @Test public void scrubbingNeverResumesExplicitlyPausedPlayback(){
        AvatarPlaybackController c=new AvatarPlaybackController();
        c.setPlaybackRequested(false);
        c.onResume();
        c.beginScrub();
        c.endScrub();
        assertFalse(c.shouldPlay());
    }
}

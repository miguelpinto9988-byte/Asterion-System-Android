package pe.asterionsystem.app;

/**
 * Keeps user playback intent independent from Activity visibility and scrubbing.
 * Android lifecycle events pause frames without erasing a manual play preference.
 * Pure Java: can be tested without an emulator.
 */
public final class AvatarPlaybackController {
    private boolean playbackRequested=true;
    private boolean foreground;
    private boolean scrubbing;

    public void onResume(){foreground=true;}
    public void onPause(){foreground=false;}

    public void setPlaybackRequested(boolean value){playbackRequested=value;}
    public void toggleRequested(){playbackRequested=!playbackRequested;}
    public boolean isPlaybackRequested(){return playbackRequested;}

    public void beginScrub(){scrubbing=true;}
    public void endScrub(){scrubbing=false;}

    public boolean shouldPlay(){
        return playbackRequested && foreground && !scrubbing;
    }
}

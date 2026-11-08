package pe.asterionsystem.app;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.Movie;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.view.WindowInsets;
import android.view.WindowInsetsController;
import android.view.WindowManager;
import android.webkit.WebView;
import android.widget.FrameLayout;
import android.widget.ImageView;

import androidx.activity.ComponentActivity;
import androidx.annotation.OptIn;
import androidx.media3.common.MediaItem;
import androidx.media3.common.PlaybackException;
import androidx.media3.common.Player;
import androidx.media3.common.util.UnstableApi;
import androidx.media3.exoplayer.ExoPlayer;
import androidx.media3.ui.PlayerView;

import java.io.IOException;
import java.io.InputStream;
import java.util.Calendar;

/**
 * Offline Asterion opening: local Media3 MP4 + local HTML/CSS layer,
 * falling back to the animated GIF or the original morning/night PNG.
 * Neither the player nor the HTML view requires internet access.
 */
@OptIn(markerClass = UnstableApi.class)
public final class SplashActivity extends ComponentActivity {
    private static final long DISPLAY_MILLIS = 1600L;
    private static final String CELESTIAL_RESOURCE = "asterion_splash_celestial";

    private final Handler handler = new Handler(Looper.getMainLooper());
    private final Runnable openApp = this::openMainActivity;
    private ImageView staticArtwork;
    private PlayerView videoView;
    private WebView htmlOverlay;
    private ExoPlayer videoPlayer;
    private AnimatedSplashView gifFallback;
    private boolean videoReady;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
        // Android 16: wait until focus to query decor WindowInsetsController.
        setContentView(R.layout.activity_splash);

        boolean morning = SplashTimeWindow.isMorning(
                Calendar.getInstance().get(Calendar.HOUR_OF_DAY));
        staticArtwork = findViewById(R.id.splash_artwork);
        staticArtwork.setImageResource(morning
                ? R.drawable.asterion_loading_manana
                : R.drawable.asterion_loading_tarde_noche);
        staticArtwork.setContentDescription(getString(morning
                ? R.string.splash_day_description
                : R.string.splash_night_description));

        videoView = findViewById(R.id.splash_video);
        htmlOverlay = findViewById(R.id.splash_web_overlay);
        if (!initializeMp4()) {
            initializeGifFallback();
        }
    }

    private int optionalRawResource() {
        return getResources().getIdentifier(CELESTIAL_RESOURCE, "raw", getPackageName());
    }

    private boolean initializeMp4() {
        int resourceId = optionalRawResource();
        if (resourceId == 0) {
            return false;
        }
        try {
            ExoPlayer player = new ExoPlayer.Builder(this).build();
            videoPlayer = player;
            videoView.setUseController(false);
            videoView.setPlayer(player);
            player.setVolume(0f);
            player.setRepeatMode(Player.REPEAT_MODE_ONE);
            player.addListener(new Player.Listener() {
                @Override
                public void onPlaybackStateChanged(@Player.State int state) {
                    if (state == Player.STATE_READY && !videoReady) {
                        showVideoAndLocalHtml();
                    }
                }

                @Override
                public void onPlayerError(PlaybackException error) {
                    restoreStaticArtwork();
                }
            });
            // android.resource is supported by Media3's local data source.
            Uri uri = Uri.parse("android.resource://" + getPackageName() + "/" + resourceId);
            player.setMediaItem(MediaItem.fromUri(uri));
            player.prepare();
            return true;
        } catch (RuntimeException error) {
            restoreStaticArtwork();
            return false;
        }
    }

    private void showVideoAndLocalHtml() {
        if (isFinishing() || isDestroyed()) {
            return;
        }
        videoReady = true;
        videoView.setVisibility(View.VISIBLE);
        staticArtwork.setVisibility(View.INVISIBLE);
        staticArtwork.setImportantForAccessibility(View.IMPORTANT_FOR_ACCESSIBILITY_NO);

        // HTML is only a transparent decorative overlay; the actual HD
        // video is decoded natively with Media3, not played by WebView.
        htmlOverlay.setBackgroundColor(Color.TRANSPARENT);
        htmlOverlay.getSettings().setJavaScriptEnabled(false);
        htmlOverlay.getSettings().setDomStorageEnabled(false);
        htmlOverlay.getSettings().setAllowContentAccess(false);
        htmlOverlay.getSettings().setAllowFileAccess(true);
        htmlOverlay.getSettings().setBlockNetworkLoads(true);
        htmlOverlay.loadUrl("file:///android_asset/asterion/splash_overlay.html");
        htmlOverlay.setVisibility(View.VISIBLE);
    }

    private void restoreStaticArtwork() {
        videoReady = false;
        if (htmlOverlay != null) {
            htmlOverlay.setVisibility(View.GONE);
        }
        if (videoView != null) {
            videoView.setVisibility(View.GONE);
            videoView.setPlayer(null);
        }
        if (staticArtwork != null) {
            staticArtwork.setVisibility(View.VISIBLE);
            staticArtwork.setImportantForAccessibility(View.IMPORTANT_FOR_ACCESSIBILITY_YES);
        }
        if (videoPlayer != null) {
            videoPlayer.release();
            videoPlayer = null;
        }
    }

    private void initializeGifFallback() {
        // Backwards compatibility with the previous experimental GIF.
        int id = optionalRawResource();
        if (id == 0) {
            return;
        }
        try (InputStream input = getResources().openRawResource(id)) {
            Movie gif = Movie.decodeStream(input);
            if (gif == null || gif.width() <= 0 || gif.height() <= 0) {
                return;
            }
            gifFallback = new AnimatedSplashView(this, gif);
            FrameLayout root = findViewById(R.id.splash_container);
            root.addView(gifFallback, new FrameLayout.LayoutParams(
                    FrameLayout.LayoutParams.MATCH_PARENT,
                    FrameLayout.LayoutParams.MATCH_PARENT));
            staticArtwork.setVisibility(View.INVISIBLE);
        } catch (IOException | RuntimeException error) {
            // On damaged/missing media show the original PNG without crashing.
        }
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        if (hasFocus && Build.VERSION.SDK_INT >= 30) {
            WindowInsetsController controller =
                    getWindow().getDecorView().getWindowInsetsController();
            if (controller != null) {
                controller.hide(WindowInsets.Type.navigationBars());
            }
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (videoPlayer != null) {
            videoPlayer.play();
        }
        if (gifFallback != null) {
            gifFallback.start();
        }
        handler.removeCallbacks(openApp);
        handler.postDelayed(openApp, DISPLAY_MILLIS);
    }

    @Override
    protected void onPause() {
        handler.removeCallbacks(openApp);
        if (videoPlayer != null) {
            videoPlayer.pause();
        }
        if (gifFallback != null) {
            gifFallback.stop();
        }
        super.onPause();
    }

    private void openMainActivity() {
        if (isFinishing() || isDestroyed()) {
            return;
        }
        Class<?> next = ProfileStore.hasProfile(this)
                ? MainActivity.class : ProfileGateActivity.class;
        startActivity(new Intent(this, next));
        finish();
    }

    @Override
    protected void onDestroy() {
        handler.removeCallbacks(openApp);
        if (gifFallback != null) {
            gifFallback.stop();
        }
        if (videoView != null) {
            videoView.setPlayer(null);
        }
        if (videoPlayer != null) {
            videoPlayer.release();
            videoPlayer = null;
        }
        if (htmlOverlay != null) {
            ((FrameLayout) htmlOverlay.getParent()).removeView(htmlOverlay);
            htmlOverlay.destroy();
            htmlOverlay = null;
        }
        super.onDestroy();
    }
}

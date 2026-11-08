package pe.asterionsystem.app;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.Movie;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.View;
import android.view.WindowInsets;
import android.view.WindowInsetsController;
import android.view.WindowManager;
import android.webkit.WebView;
import android.webkit.WebViewClient;
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
 * Asterion startup: ONE visible artwork at a time.
 *
 * The old morning/night PNG is an emergency fallback, never a temporary poster
 * while the celestial MP4 is preparing. Android 12+ draws its own brief, plain
 * system launch background before this Activity; it has no branded splash art.
 *
 * Media is packaged in the APK. No network or JavaScript is used.
 */
@OptIn(markerClass = UnstableApi.class)
public final class SplashActivity extends ComponentActivity {
    private static final long DISPLAY_MILLIS = 1600L;
    private static final String CELESTIAL_RESOURCE = "asterion_splash_celestial";
    private static final String TAG = "AsterionSplash";

    private final Handler handler = new Handler(Looper.getMainLooper());
    private final Runnable openApp = this::openMainActivity;

    private ImageView staticArtwork;
    private PlayerView videoView;
    private WebView htmlOverlay;
    private ExoPlayer videoPlayer;
    private AnimatedSplashView gifFallback;
    private boolean videoReady;
    private boolean htmlInitialized;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
        // Android 16: do not request decor WindowInsetsController until focus.
        setContentView(R.layout.activity_splash);

        staticArtwork = findViewById(R.id.splash_artwork);
        videoView = findViewById(R.id.splash_video);
        htmlOverlay = findViewById(R.id.splash_web_overlay);

        // The layout starts with a plain dark background; the approved MP4 is
        // the only branded visual. Do not inflate the old PNG during buffering.
        staticArtwork.setVisibility(View.INVISIBLE);
        staticArtwork.setImportantForAccessibility(View.IMPORTANT_FOR_ACCESSIBILITY_NO);

        if (!initializeMp4()) {
            if (!initializeGifFallback()) {
                showOriginalFallback();
            }
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
            // Keep the decoding surface attached so the first frame can render,
            // but do not display it until Media3 signals onRenderedFirstFrame().
            videoView.setUseController(false);
            videoView.setVisibility(View.VISIBLE);
            videoView.setAlpha(0f);

            ExoPlayer player = new ExoPlayer.Builder(this).build();
            videoPlayer = player;
            videoView.setPlayer(player);
            player.setVolume(0f);
            player.setRepeatMode(Player.REPEAT_MODE_ONE);
            player.addListener(new Player.Listener() {
                @Override
                public void onRenderedFirstFrame() {
                    showVideoAndLocalHtml();
                }

                @Override
                public void onPlayerError(PlaybackException error) {
                    // Never show a different illustration AFTER the video was
                    // already visible; finish the splash instead.
                    if (videoReady) {
                        handler.removeCallbacks(openApp);
                        handler.post(openApp);
                    } else {
                        handler.post(() -> {
                            releaseVideoPlayer();
                            if (!initializeGifFallback()) {
                                showOriginalFallback();
                            }
                        });
                    }
                }
            });

            Uri uri = Uri.parse("android.resource://" + getPackageName() + "/" + resourceId);
            player.setMediaItem(MediaItem.fromUri(uri));
            player.prepare();
            return true;
        } catch (RuntimeException error) {
            releaseVideoPlayer();
            return false;
        }
    }

    private void showVideoAndLocalHtml() {
        if (videoReady || isFinishing() || isDestroyed() || videoPlayer == null) {
            return;
        }
        videoReady = true;
        Log.i(TAG, "VIDEO_FIRST_FRAME");
        // The first image frame is already decoded. Reveal just this artwork;
        // the old static ImageView remains hidden for the entire normal launch.
        videoView.setAlpha(1f);
        staticArtwork.setVisibility(View.INVISIBLE);
        staticArtwork.setImportantForAccessibility(View.IMPORTANT_FOR_ACCESSIBILITY_NO);
        initializeHtmlOverlay();
    }

    private void initializeHtmlOverlay() {
        if (htmlInitialized || htmlOverlay == null) {
            return;
        }
        htmlInitialized = true;
        // Keep WebView hidden until its transparent CSS has finished loading.
        // This prevents the white/blank second-screen flash on slower phones.
        htmlOverlay.setVisibility(View.GONE);
        htmlOverlay.setBackgroundColor(Color.TRANSPARENT);
        htmlOverlay.setLayerType(View.LAYER_TYPE_HARDWARE, null);
        htmlOverlay.setImportantForAccessibility(View.IMPORTANT_FOR_ACCESSIBILITY_NO);
        htmlOverlay.getSettings().setJavaScriptEnabled(false);
        htmlOverlay.getSettings().setDomStorageEnabled(false);
        htmlOverlay.getSettings().setAllowContentAccess(false);
        htmlOverlay.getSettings().setAllowFileAccess(true);
        htmlOverlay.getSettings().setBlockNetworkLoads(true);
        htmlOverlay.setWebViewClient(new WebViewClient() {
            @Override
            public void onPageFinished(WebView view, String url) {
                if (videoReady && !isFinishing() && !isDestroyed()) {
                    view.setVisibility(View.VISIBLE);
                }
            }
        });
        htmlOverlay.loadUrl("file:///android_asset/asterion/splash_overlay.html");
    }

    private void releaseVideoPlayer() {
        if (videoView != null) {
            videoView.setAlpha(0f);
            videoView.setVisibility(View.GONE);
            videoView.setPlayer(null);
        }
        if (videoPlayer != null) {
            ExoPlayer player = videoPlayer;
            videoPlayer = null;
            player.release();
        }
    }

    private boolean initializeGifFallback() {
        // A legacy GIF is allowed as one *alternative* when no MP4 can play.
        // On the Media3 build, the .mp4 resource will not decode as a GIF.
        int id = optionalRawResource();
        if (id == 0) {
            return false;
        }
        try (InputStream input = getResources().openRawResource(id)) {
            Movie gif = Movie.decodeStream(input);
            if (gif == null || gif.width() <= 0 || gif.height() <= 0) {
                return false;
            }
            gifFallback = new AnimatedSplashView(this, gif);
            gifFallback.setContentDescription("Asterion System. Animación celestial.");
            FrameLayout root = findViewById(R.id.splash_container);
            root.addView(gifFallback, new FrameLayout.LayoutParams(
                    FrameLayout.LayoutParams.MATCH_PARENT,
                    FrameLayout.LayoutParams.MATCH_PARENT));
            staticArtwork.setVisibility(View.INVISIBLE);
            gifFallback.start();
            return true;
        } catch (IOException | RuntimeException error) {
            return false;
        }
    }

    private void showOriginalFallback() {
        // Only called if the celestial media is missing or cannot be decoded.
        boolean morning = SplashTimeWindow.isMorning(
                Calendar.getInstance().get(Calendar.HOUR_OF_DAY));
        staticArtwork.setImageResource(morning
                ? R.drawable.asterion_loading_manana
                : R.drawable.asterion_loading_tarde_noche);
        staticArtwork.setContentDescription(getString(morning
                ? R.string.splash_day_description
                : R.string.splash_night_description));
        staticArtwork.setVisibility(View.VISIBLE);
        staticArtwork.setImportantForAccessibility(View.IMPORTANT_FOR_ACCESSIBILITY_YES);
        Log.w(TAG, "FALLBACK_PNG");
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
        releaseVideoPlayer();
        if (htmlOverlay != null) {
            if (htmlOverlay.getParent() instanceof FrameLayout) {
                ((FrameLayout) htmlOverlay.getParent()).removeView(htmlOverlay);
            }
            htmlOverlay.destroy();
            htmlOverlay = null;
        }
        super.onDestroy();
    }
}

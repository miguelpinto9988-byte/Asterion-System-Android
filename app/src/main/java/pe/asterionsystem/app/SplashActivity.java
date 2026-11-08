package pe.asterionsystem.app;

import android.content.Intent;
import android.graphics.Movie;
import android.view.View;
import android.widget.FrameLayout;
import java.io.IOException;
import java.io.InputStream;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.WindowInsets;
import android.view.WindowInsetsController;
import android.view.WindowManager;
import android.widget.ImageView;

import androidx.activity.ComponentActivity;

import java.util.Calendar;

/**
 * Offline splash screen with an optional animated GIF and local PNG fallback.
 */
public final class SplashActivity extends ComponentActivity {
    private static final long DISPLAY_MILLIS = 1600L;
    // Animated resource is optional until the new design has been packaged.
    private static final String SPLASH_GIF_RESOURCE = "asterion_splash_celestial";

    private final Handler handler = new Handler(Looper.getMainLooper());
    private final Runnable openApp = this::openMainActivity;
    private AnimatedSplashView animatedArtwork;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
        // The decor view does not exist yet during onCreate on Android 16.
        // Never request the WindowInsetsController before window focus.
        setContentView(R.layout.activity_splash);

        boolean morning = SplashTimeWindow.isMorning(
                Calendar.getInstance().get(Calendar.HOUR_OF_DAY));
        ImageView artwork = findViewById(R.id.splash_artwork);
        artwork.setImageResource(morning
                ? R.drawable.asterion_loading_manana
                : R.drawable.asterion_loading_tarde_noche);
        artwork.setContentDescription(getString(morning
                ? R.string.splash_day_description
                : R.string.splash_night_description));

        // Apply the new GIF in both app variants when it is packaged.
        addOptionalPreviewGif(artwork);
    }

    private void addOptionalPreviewGif(ImageView staticArtwork) {
        // Dynamic lookup keeps the existing PNG screens as offline fallback.
        int gifId = getResources().getIdentifier(
                SPLASH_GIF_RESOURCE, "raw", getPackageName());
        if (gifId == 0) {
            return;
        }
        try (InputStream input = getResources().openRawResource(gifId)) {
            Movie movie = Movie.decodeStream(input);
            if (movie == null || movie.width() <= 0 || movie.height() <= 0) {
                return;
            }
            AnimatedSplashView view = new AnimatedSplashView(this, movie);
            view.setContentDescription(
                    "Asterion System. Animación celestial de carga del sistema.");
            FrameLayout root = findViewById(R.id.splash_container);
            root.addView(view, new FrameLayout.LayoutParams(
                    FrameLayout.LayoutParams.MATCH_PARENT,
                    FrameLayout.LayoutParams.MATCH_PARENT));
            staticArtwork.setVisibility(View.INVISIBLE);
            staticArtwork.setImportantForAccessibility(
                    View.IMPORTANT_FOR_ACCESSIBILITY_NO);
            animatedArtwork = view;
        } catch (IOException | RuntimeException ignored) {
            // Never block startup: existing PNG artwork remains the fallback.
        }
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        // Android 16 can throw NullPointerException if PhoneWindow's
        // insets controller is requested before its decor view is attached.
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
        if (animatedArtwork != null) {
            animatedArtwork.start();
        }
        handler.removeCallbacks(openApp);
        handler.postDelayed(openApp, DISPLAY_MILLIS);
    }

    @Override
    protected void onPause() {
        handler.removeCallbacks(openApp);
        if (animatedArtwork != null) {
            animatedArtwork.stop();
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
        if (animatedArtwork != null) {
            animatedArtwork.stop();
        }
        super.onDestroy();
    }
}

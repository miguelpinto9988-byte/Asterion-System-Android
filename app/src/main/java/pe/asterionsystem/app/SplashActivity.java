package pe.asterionsystem.app;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.WindowInsets;
import android.view.WindowInsetsController;
import android.view.WindowManager;
import android.view.View;
import android.widget.ImageView;

import androidx.activity.ComponentActivity;

import java.util.Calendar;

/**
 * Dedicated, offline splash screen. Its artwork is immutable and selected by
 * the phone's local hour, independently of language, accounts or network.
 * The embedded progress bar is part of the fixed approved illustration.
 */
public final class SplashActivity extends ComponentActivity {
    private static final long DISPLAY_MILLIS = 1600L;

    private final Handler handler = new Handler(Looper.getMainLooper());
    private final Runnable openApp = this::openMainActivity;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
        // The decor view does not exist yet during onCreate on Android 16.
        // Never request the WindowInsetsController before window focus.
        setContentView(R.layout.activity_splash);
        if (Build.VERSION.SDK_INT >= 30) {
            // Draw behind the system bar areas for an edge-to-edge illustration.
            getWindow().setDecorFitsSystemWindows(false);
        }

        boolean morning = SplashTimeWindow.isMorning(
                Calendar.getInstance().get(Calendar.HOUR_OF_DAY));
        ImageView artwork = findViewById(R.id.splash_artwork);
        artwork.setImageResource(morning
                ? R.drawable.asterion_loading_manana_4k
                : R.drawable.asterion_loading_tarde_noche_4k);
        artwork.setContentDescription(getString(morning
                ? R.string.splash_day_description
                : R.string.splash_night_description));
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
                controller.hide(WindowInsets.Type.systemBars());
                controller.setSystemBarsBehavior(
                        WindowInsetsController.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE);
            }
        } else if (hasFocus) {
            getWindow().getDecorView().setSystemUiVisibility(
                    View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                            | View.SYSTEM_UI_FLAG_FULLSCREEN
                            | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                            | View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                            | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                            | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        handler.removeCallbacks(openApp);
        handler.postDelayed(openApp, DISPLAY_MILLIS);
    }

    @Override
    protected void onPause() {
        handler.removeCallbacks(openApp);
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
        super.onDestroy();
    }
}

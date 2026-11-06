package pe.asterionsystem.app;

import android.content.Intent;
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
        if (Build.VERSION.SDK_INT >= 30) {
            WindowInsetsController controller = getWindow().getInsetsController();
            if (controller != null) {
                controller.hide(WindowInsets.Type.navigationBars());
            }
        }
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
        startActivity(new Intent(this, MainActivity.class));
        finish();
    }

    @Override
    protected void onDestroy() {
        handler.removeCallbacks(openApp);
        super.onDestroy();
    }
}

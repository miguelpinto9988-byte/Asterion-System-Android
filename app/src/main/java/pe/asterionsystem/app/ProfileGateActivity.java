package pe.asterionsystem.app;

import android.app.AlertDialog;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.WindowManager;
import android.view.View;
import androidx.activity.ComponentActivity;
import pe.asterionsystem.app.presentation.NeonFrameDrawable;

/** Appears once after splash when no local hunter identity exists. */
public final class ProfileGateActivity extends ComponentActivity {
    private static final int PURPLE = Color.rgb(182, 121, 255);
    private static final int BLUE = Color.rgb(80, 196, 255);

    @Override protected void onCreate(Bundle state) {
        super.onCreate(state);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.screen_profile_gate);
        float density = getResources().getDisplayMetrics().density;
        View panel = findViewById(R.id.profile_gate_panel);
        View create = findViewById(R.id.profile_gate_create);
        View restore = findViewById(R.id.profile_gate_restore);
        panel.setBackground(new NeonFrameDrawable(BLUE, density));
        create.setBackground(new NeonFrameDrawable(PURPLE, density));
        restore.setBackground(new NeonFrameDrawable(BLUE, density));
        create.setOnClickListener(v -> startActivity(
                new Intent(this, ProfileCreateActivity.class)));
        restore.setOnClickListener(v -> new AlertDialog.Builder(this)
                .setTitle(R.string.profile_restoration_unavailable_title)
                .setMessage(R.string.profile_restoration_unavailable_body)
                .setPositiveButton(android.R.string.ok, (dialog, which) -> dialog.dismiss())
                .show());
    }

    @Override protected void onResume() {
        super.onResume();
        if (ProfileStore.hasProfile(this)) {
            Intent next = new Intent(this, MainActivity.class);
            next.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(next);
            finish();
        }
    }
}

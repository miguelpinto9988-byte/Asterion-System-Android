package pe.asterionsystem.app;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import androidx.activity.ComponentActivity;
import pe.asterionsystem.app.presentation.NeonFrameDrawable;
import android.graphics.Color;
import android.widget.Toast;

/** Minimal approved first step. Does not assign skills or exercise intensity. */
public final class ProfileCreateActivity extends ComponentActivity {
    @Override protected void onCreate(Bundle saved) {
        super.onCreate(saved);
        setContentView(R.layout.screen_profile_create);
        float density = getResources().getDisplayMetrics().density;
        View card = findViewById(R.id.profile_create_panel);
        card.setBackground(new NeonFrameDrawable(Color.rgb(182, 121, 255), density));
        EditText name = findViewById(R.id.profile_name_input);
        if (saved != null) {
            name.setText(saved.getString("draft_name", ""));
        }
        findViewById(R.id.profile_create_submit).setOnClickListener(v -> {
            String userName = name.getText() == null ? "" : name.getText().toString().trim();
            if (userName.isEmpty() || userName.length() > 40) {
                name.setError(getString(R.string.profile_create_invalid));
                return;
            }
            if (!ProfileStore.saveName(this, userName)) {
                Toast.makeText(this, R.string.profile_create_save_error, Toast.LENGTH_LONG).show();
                return;
            }
            Intent home = new Intent(this, MainActivity.class);
            home.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(home);
            finish();
        });
    }

    @Override protected void onSaveInstanceState(Bundle state) {
        EditText name = findViewById(R.id.profile_name_input);
        state.putString("draft_name", name.getText() == null ? "" : name.getText().toString());
        super.onSaveInstanceState(state);
    }
}

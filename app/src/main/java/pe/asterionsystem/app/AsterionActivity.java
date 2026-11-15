package pe.asterionsystem.app;

import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowInsets;
import android.view.ViewTreeObserver;
import androidx.activity.ComponentActivity;

/** Shared palette, typography and safe system bars for every native destination. */
public abstract class AsterionActivity extends ComponentActivity {
    private boolean visualLight;
    private boolean savedLight;
    private View content;
    private ViewTreeObserver.OnGlobalLayoutListener themeListener;

    @Override protected void onCreate(Bundle state) {
        savedLight=visualLight=AppTheme.isLight(this);
        setTheme(visualLight?R.style.Theme_AsterionSystem_Light:R.style.Theme_AsterionSystem);
        super.onCreate(state);
        AppTheme.systemBars(this,visualLight);
    }

    @Override protected void onPostCreate(Bundle state) {
        super.onPostCreate(state);
        content=findViewById(android.R.id.content);
        // Main already owns its navigation-host insets; never apply them twice.
        if(Build.VERSION.SDK_INT>=35 && !(this instanceof MainActivity)) {
            content.setOnApplyWindowInsetsListener((v,insets)->{
                android.graphics.Insets safe=insets.getInsets(
                        WindowInsets.Type.systemBars()|WindowInsets.Type.displayCutout());
                v.setPadding(safe.left,safe.top,safe.right,safe.bottom);
                return insets;
            });
            content.requestApplyInsets();
        }
        themeListener=()->AppTheme.apply(content,visualLight);
        content.getViewTreeObserver().addOnGlobalLayoutListener(themeListener);
        AppTheme.apply(content,visualLight);
    }

    /** Registration can preview a theme without committing the user's draft. */
    public final void previewTheme(boolean light) {
        visualLight=light;
        AppTheme.systemBars(this,light);
        if(content!=null)AppTheme.apply(content,light);
    }

    @Override protected void onResume() {
        super.onResume();
        boolean now=AppTheme.isLight(this);
        if(now!=savedLight && !(this instanceof ProfileCreateActivity)) {
            savedLight=now;
            recreate();
        }
    }

    @Override protected void onDestroy() {
        if(content!=null && themeListener!=null && content.getViewTreeObserver().isAlive())
            content.getViewTreeObserver().removeOnGlobalLayoutListener(themeListener);
        super.onDestroy();
    }
}

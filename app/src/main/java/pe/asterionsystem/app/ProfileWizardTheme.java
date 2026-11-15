package pe.asterionsystem.app;

import android.content.res.ColorStateList;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;

/**
 * The approved light/dark violet system across all registration steps.
 * The theme selection remains in Step 3.
 *
 * Exception: the approved weekly training taxonomy retains six distinct
 * category colors (five activities plus Rest), including its legend.
 */
final class ProfileWizardTheme {
    private ProfileWizardTheme() {}
    static int foreground(boolean light) { return AppTheme.foreground(light); }
    static int background(boolean light) { return AppTheme.background(light); }
    static int muted(boolean light) { return AppTheme.muted(light); }
    static GradientDrawable box(boolean light,boolean selected,int radiusDp,float density) {
        GradientDrawable box=AppTheme.panel(light,Math.max(12,radiusDp),density);
        if(selected){box.setColor(AppTheme.soft(light));box.setStroke(Math.max(1,Math.round(2*density)),AppTheme.accent(light));}
        return box;
    }

    static void apply(View view, boolean light, float density) {
        // Preserve the six meaningful weekly taxonomy colors.
        // Never overwrite colored activity tiles or their colored legend text.
        Object tag = view.getTag();
        if ("asterion_week_color".equals(tag)) {
            // The weekly tile owns its darker semantic ink, shared across
            // light and dark themes; never replace that ink with white.
            return;
        }
        if ("asterion_week_legend".equals(tag)) {
            return;
        }
        // Full-color WebP layers must not inherit the monochrome icon tint.
        // Otherwise changing light/dark mode recolors portraits, landscapes,
        // frames and companions and destroys the approved visual palette.
        if ("asterion_fullcolor_art".equals(tag)) {
            if (view instanceof ImageView) {
                ((ImageView)view).setImageTintList(null);
            }
            return;
        }
        if (view instanceof ImageView && view.getId() != R.id.profile_avatar_preview) {
            view.setVisibility(View.VISIBLE);
            ((ImageView)view).setImageTintList(ColorStateList.valueOf(AppTheme.accent(light)));
            return;
        }

        if (view instanceof TextView) {
            TextView text = (TextView)view;
            text.setTextColor(foreground(light));
            text.setTypeface(Typeface.DEFAULT, Typeface.BOLD);
            if (view instanceof EditText) {
                text.setHintTextColor(muted(light));
                text.setBackground(box(light,false,8,density));
                text.setPadding(dp(12,density),dp(5,density),
                        dp(12,density),dp(5,density));
            } else if (text.isClickable() && !(view instanceof CompoundButton)) {
                text.setBackground(box(light,false,8,density));
                text.setMinimumHeight(dp(48,density));
            } else if (text.getBackground() != null && !(view instanceof CompoundButton)) {
                // Expandable review details once used a dark navy drawable.
                // In white mode this must become white like every other panel.
                text.setBackground(box(light,false,8,density));
            }
            if (view instanceof CompoundButton) {
                ((CompoundButton)view).setButtonTintList(
                        ColorStateList.valueOf(AppTheme.accent(light)));
            }
        }
        if (view instanceof Spinner) {
            view.setBackground(box(light,false,8,density));
        }
        if (view instanceof ViewGroup) {
            ViewGroup group = (ViewGroup)view;
            for (int i=0; i<group.getChildCount();i++) {
                apply(group.getChildAt(i),light,density);
            }
        }
    }

    static void applyWizard(View root, View panel, View forward,
                            TextView forwardText, boolean light, float density) {
        root.setBackgroundColor(background(light));
        apply(root,light,density);
        panel.setBackground(box(light,false,12,density));
        forward.setTag("primary");
        forward.setTag(R.id.asterion_theme_applied,null);
        AppTheme.apply(forward,light);
        forwardText.setTextColor(AppTheme.onAccent(light));
    }

    private static int dp(int n,float density) {return Math.round(n*density);}
}

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
 * One monochrome palette for all four registration steps. No mixed-color
 * cards, gradients, or separate theme switch outside Step 3.
 *
 * Exception: the approved weekly training taxonomy retains six distinct
 * category colors (five activities plus Rest), including its legend.
 */
final class ProfileWizardTheme {
    private static final int LIGHT_BG = Color.WHITE;
    private static final int DARK_BG = 0xFF080808;
    private static final int LIGHT_TEXT = 0xFF101010;
    private static final int DARK_TEXT = 0xFFF8F8F8;
    private static final int LIGHT_BORDER = 0xFFCECECE;
    private static final int DARK_BORDER = 0xFF454545;

    private ProfileWizardTheme() {}

    static int foreground(boolean light) { return light ? LIGHT_TEXT : DARK_TEXT; }
    static int background(boolean light) { return light ? LIGHT_BG : DARK_BG; }
    static int muted(boolean light) { return light ? 0xFF5F5F5F : 0xFFB9B9B9; }

    static GradientDrawable box(boolean light, boolean selected, int radiusDp,
                                float density) {
        GradientDrawable drawable = new GradientDrawable();
        drawable.setColor(background(light));
        drawable.setCornerRadius(radiusDp * density);
        drawable.setStroke(
                Math.max(1, Math.round((selected ? 2.0f : 1.0f) * density)),
                selected ? foreground(light)
                        : (light ? LIGHT_BORDER : DARK_BORDER));
        return drawable;
    }

    static void apply(View view, boolean light, float density) {
        // These are the ONLY authorized non-monochrome visual accents.
        // Never overwrite colored activity tiles or their colored legend text.
        Object tag = view.getTag();
        if ("asterion_week_color".equals(tag)) {
            if (view instanceof TextView) ((TextView)view).setTextColor(Color.WHITE);
            return;
        }
        if ("asterion_week_legend".equals(tag)) {
            return;
        }
        if (view instanceof ImageView && view.getId() != R.id.profile_avatar_preview) {
            view.setVisibility(View.GONE);
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
            }
            if (view instanceof CompoundButton) {
                ((CompoundButton)view).setButtonTintList(
                        ColorStateList.valueOf(foreground(light)));
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
        forward.setBackground(box(light,true,9,density));
        forwardText.setTextColor(foreground(light));
    }

    private static int dp(int n,float density) {return Math.round(n*density);}
}

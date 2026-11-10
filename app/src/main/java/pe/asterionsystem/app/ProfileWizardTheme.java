package pe.asterionsystem.app;

import android.content.res.ColorStateList;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.Spinner;
import android.widget.TextView;

/**
 * Two neutral visual themes for the existing registration wizard.
 * Only view appearance changes; draft keys and validation remain in their
 * original controllers. No remote art, images or screenshot coordinates.
 */
final class ProfileWizardTheme {
    private ProfileWizardTheme() {}

    static final int BLACK = Color.rgb(12, 12, 12);
    static final int WHITE = Color.WHITE;
    private static final int DARK_PANEL = Color.rgb(27, 27, 27);
    private static final int LIGHT_PANEL = Color.rgb(247, 247, 247);
    private static final int DARK_EDGE = Color.rgb(86, 86, 86);
    private static final int LIGHT_EDGE = Color.rgb(190, 190, 190);

    static GradientDrawable box(boolean light, boolean selected, int radiusDp, float density) {
        int background = selected ? (light ? BLACK : WHITE)
                : (light ? LIGHT_PANEL : DARK_PANEL);
        int stroke = selected ? (light ? BLACK : WHITE)
                : (light ? LIGHT_EDGE : DARK_EDGE);
        GradientDrawable result = new GradientDrawable();
        result.setColor(background);
        result.setCornerRadius(radiusDp * density);
        result.setStroke(Math.max(1, Math.round(density)), stroke);
        return result;
    }

    static int foreground(boolean light) { return light ? BLACK : WHITE; }
    static int background(boolean light) { return light ? WHITE : BLACK; }
    static int muted(boolean light) { return light ? Color.DKGRAY : 0xFFCCCCCC; }

    static void apply(View root, boolean light, float density) {
        int fg = foreground(light);
        if (root instanceof ImageView) {
            // Only the real avatar remains visible. Illustrations are not
            // decoration in this deliberately minimal registration interface.
            if (root.getId() != R.id.profile_avatar_preview) {
                root.setVisibility(View.GONE);
            }
        }
        if (root instanceof TextView) {
            TextView text = (TextView) root;
            text.setTextColor(fg);
            if (root instanceof EditText) {
                text.setHintTextColor(muted(light));
                text.setBackground(box(light, false, 8, density));
                text.setPadding(dp(12,density),dp(5,density),dp(12,density),dp(5,density));
            } else if (root instanceof Button) {
                text.setBackground(box(light,false,9,density));
            } else if (text.isClickable() && !(root instanceof CompoundButton)) {
                text.setBackground(box(light,false,8,density));
                text.setMinimumHeight(dp(48,density));
            }
            if (root instanceof CompoundButton) {
                ((CompoundButton) root).setButtonTintList(ColorStateList.valueOf(fg));
            }
        }
        if (root instanceof Spinner) {
            root.setBackground(box(light,false,8,density));
        }
        if (root instanceof ViewGroup) {
            ViewGroup group = (ViewGroup) root;
            for (int i=0; i<group.getChildCount(); i++) {
                apply(group.getChildAt(i), light, density);
            }
        }
    }

    static void applyWizard(View root, View panel, View forward, TextView forwardText,
                            Button themeLight, Button themeDark,
                            boolean light, float density) {
        root.setBackgroundColor(background(light));
        apply(root, light, density);
        panel.setBackground(box(light,false,13,density));
        forward.setBackground(box(light,true,10,density));
        forwardText.setTextColor(background(light));
        themeLight.setBackground(box(light,light,9,density));
        themeDark.setBackground(box(light,!light,9,density));
        themeLight.setTextColor(light ? WHITE : WHITE);
        themeDark.setTextColor(light ? BLACK : BLACK);
    }

    private static int dp(int value,float density) {return Math.round(value*density);}
}

package pe.asterionsystem.app;

import android.app.Activity;
import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.Typeface;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.graphics.drawable.RippleDrawable;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;

/** Approved porcelain/graphite and violet system. Roles are shared by XML and native views. */
public final class AppTheme {
    private AppTheme() {}
    public static boolean isLight(Context c) {
        return "light".equals(ProfileStore.getRegistration(c).optString("theme","light"));
    }
    public static int background(boolean light){return light?0xFFF6F6F9:0xFF09090F;}
    public static int surface(boolean light){return light?0xFFFFFFFF:0xFF17171F;}
    public static int soft(boolean light){return light?0xFFEEEBF8:0xFF262033;}
    public static int foreground(boolean light){return light?0xFF181821:0xFFF5F4FA;}
    public static int muted(boolean light){return light?0xFF656170:0xFFB5B3C3;}
    public static int border(boolean light){return light?0xFFDDD9E8:0xFF383342;}
    public static int accent(boolean light){return light?0xFF6544AA:0xFFC4ABFF;}
    public static int onAccent(boolean light){return light?0xFFFFFFFF:0xFF181025;}
    public static int dp(Context c,int n){return Math.round(n*c.getResources().getDisplayMetrics().density);}

    public static GradientDrawable panel(boolean light,float radius,float density) {
        GradientDrawable d=new GradientDrawable();
        d.setColor(surface(light));d.setCornerRadius(radius*density);
        d.setStroke(Math.max(1,Math.round(density)),border(light));
        return d;
    }
    public static GradientDrawable panel(Context c,boolean light){
        return panel(light,20,c.getResources().getDisplayMetrics().density);
    }
    public static void card(View v,boolean light){v.setTag("card");v.setBackground(panel(v.getContext(),light));}
    public static void primary(Button b,boolean light){b.setTag("primary");b.setTag(R.id.asterion_theme_applied,null);apply(b,light);}
    public static void secondary(Button b,boolean light){b.setTag("secondary");b.setTag(R.id.asterion_theme_applied,null);apply(b,light);}

    public static void systemBars(Activity a,boolean light) {
        a.getWindow().setStatusBarColor(background(light));
        a.getWindow().setNavigationBarColor(background(light));
        int flags=a.getWindow().getDecorView().getSystemUiVisibility();
        int appearance=View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR|View.SYSTEM_UI_FLAG_LIGHT_NAVIGATION_BAR;
        a.getWindow().getDecorView().setSystemUiVisibility(light?flags|appearance:flags&~appearance);
    }

    public static void apply(View view,boolean light){apply(view,light,false,false);}
    private static void apply(View view,boolean light,boolean primaryParent,boolean inverseParent) {
        Object tag=view.getTag();
        boolean weekly="asterion_week_color".equals(tag)||"asterion_week_legend".equals(tag);
        boolean inverse=inverseParent||"inverse".equals(tag);
        boolean primary=primaryParent||"primary".equals(tag);
        if(view instanceof TextView) {
            TextView text=(TextView)view;
            if(text.getTypeface()==null||!text.getTypeface().isBold())
                text.setTypeface(Typeface.create("sans-serif",Typeface.BOLD));
        }
        // Keep the six meaningful calendar/category colors, never wash out their legend.
        if(weekly||"art".equals(tag)||"preserve".equals(tag))return;
        String signature=(light?"light:":"dark:")+String.valueOf(tag)+":"+primary+":"+inverse;
        if(!signature.equals(view.getTag(R.id.asterion_theme_applied))) {
            view.setTag(R.id.asterion_theme_applied,signature);
            Context c=view.getContext();float density=c.getResources().getDisplayMetrics().density;
            if(view instanceof TextView) {
                TextView text=(TextView)view;
                int color=inverse?0xFFF5F4FA:primary?onAccent(light):
                        "muted".equals(tag)?muted(light):
                        ("overline".equals(tag)||"link".equals(tag))?accent(light):foreground(light);
                text.setTextColor(color);
                text.setHighlightColor(soft(light));
                if("overline".equals(tag))text.setLetterSpacing(.10f);
                if(view instanceof EditText) {
                    text.setHintTextColor(muted(light));
                    text.setBackground(panel(light,12,density));
                    text.setPadding(dp(c,14),dp(c,10),dp(c,14),dp(c,10));
                    text.setMinimumHeight(dp(c,50));
                } else if(view instanceof CompoundButton) {
                    ((CompoundButton)view).setButtonTintList(new ColorStateList(
                            new int[][]{new int[]{android.R.attr.state_checked},new int[]{}},
                            new int[]{accent(light),muted(light)}));
                    text.setMinimumHeight(dp(c,48));
                } else if(view instanceof Button) {
                    Button b=(Button)view;b.setAllCaps(false);
                    b.setStateListAnimator(null);
                    GradientDrawable shape=panel(light,14,density);
                    if(primary){shape.setColor(accent(light));shape.setStroke(0,accent(light));}
                    if("selected".equals(tag)){shape.setColor(soft(light));shape.setStroke(dp(c,2),accent(light));}
                    b.setBackground(new RippleDrawable(ColorStateList.valueOf(light?0x226544AA:0x33C4ABFF),shape,null));
                    int iconColor=primary?onAccent(light):accent(light);
                    for(Drawable icon:b.getCompoundDrawablesRelative()) {
                        if(icon!=null)icon.mutate().setTint(iconColor);
                    }
                    b.setMinHeight(dp(c,48));b.setMinimumHeight(dp(c,48));
                    b.setPadding(dp(c,12),dp(c,8),dp(c,12),dp(c,8));
                }
            }
            if(view instanceof ImageButton && "avatar".equals(tag)) {
                GradientDrawable d=panel(light,26,density);d.setColor(soft(light));
                view.setBackground(d);view.setClipToOutline(true);
            }
            if(view instanceof ImageView && "icon".equals(tag))
                ((ImageView)view).setImageTintList(ColorStateList.valueOf(accent(light)));
            if(view instanceof ProgressBar) {
                ((ProgressBar)view).setProgressTintList(ColorStateList.valueOf(accent(light)));
                ((ProgressBar)view).setProgressBackgroundTintList(ColorStateList.valueOf(soft(light)));
            }
            if(view instanceof Spinner)view.setBackground(panel(light,12,density));
            if(view instanceof ViewGroup) {
                Drawable old=view.getBackground();
                if("card".equals(tag))view.setBackground(panel(light,20,density));
                else if("hero".equals(tag)) {
                    GradientDrawable d=new GradientDrawable(GradientDrawable.Orientation.TL_BR,
                            light?new int[]{0xFFF2EDFF,0xFFE7DFF6}:new int[]{0xFF21172D,0xFF14111E});
                    d.setCornerRadius(dp(c,22));d.setStroke(dp(c,1),border(light));view.setBackground(d);
                } else if("primary".equals(tag)) {
                    GradientDrawable d=panel(light,14,density);d.setColor(accent(light));view.setBackground(d);
                } else if(old instanceof GradientDrawable && !inverse) {
                    view.setBackground(panel(light,18,density));
                } else if(old instanceof ColorDrawable && ((ColorDrawable)old).getColor()!=0 && !inverse) {
                    view.setBackgroundColor(background(light));
                }
            }
        }
        if(view instanceof ViewGroup) {
            ViewGroup g=(ViewGroup)view;
            for(int i=0;i<g.getChildCount();i++)apply(g.getChildAt(i),light,primary,inverse);
        }
    }
}

package pe.asterionsystem.app;

import android.app.Activity;
import android.graphics.drawable.GradientDrawable;
import android.view.Gravity;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.HorizontalScrollView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

/** Compact horizontally scrollable boss windows with a dedicated diffused backdrop. */
final class MonarchBossPicker {
    interface Listener { void select(String bossId); }
    private MonarchBossPicker() {}

    static View create(Activity activity,String selected,boolean enabled,Listener listener) {
        boolean light=AppTheme.isLight(activity);
        LinearLayout section=new LinearLayout(activity);section.setOrientation(LinearLayout.VERTICAL);
        TextView overline=text(activity,"ELIGE TU ENCUENTRO",11);overline.setTag("overline");
        section.addView(overline);
        TextView hint=text(activity,enabled
                ?"Cada jefe cambia el enfoque del ciclo opcional."
                :"Este encuentro queda bloqueado mientras el ciclo está activo.",13);
        hint.setTag("muted");section.addView(hint);
        HorizontalScrollView scroller=new HorizontalScrollView(activity);
        scroller.setHorizontalScrollBarEnabled(false);scroller.setFillViewport(false);
        LinearLayout row=new LinearLayout(activity);row.setOrientation(LinearLayout.HORIZONTAL);
        row.setPadding(0,AppTheme.dp(activity,12),AppTheme.dp(activity,4),AppTheme.dp(activity,8));
        for(MonarchBoss boss:MonarchBoss.ALL) {
            boolean active=boss.id.equals(selected);
            View card=card(activity,boss,active,enabled,listener);
            LinearLayout.LayoutParams lp=new LinearLayout.LayoutParams(
                    AppTheme.dp(activity,246),AppTheme.dp(activity,164));
            lp.rightMargin=AppTheme.dp(activity,12);row.addView(card,lp);
        }
        scroller.addView(row);section.addView(scroller,new LinearLayout.LayoutParams(-1,AppTheme.dp(activity,184)));
        AppTheme.apply(section,light);return section;
    }

    private static View card(Activity activity,MonarchBoss boss,boolean selected,
                             boolean enabled,Listener listener) {
        FrameLayout frame=new FrameLayout(activity);frame.setTag("inverse");frame.setClipToOutline(true);
        GradientDrawable outline=new GradientDrawable();outline.setColor(0xFF17121F);
        outline.setCornerRadius(AppTheme.dp(activity,20));
        outline.setStroke(AppTheme.dp(activity,selected?3:1),
                selected?0xFFD5BCFF:0xFF5B5068);frame.setBackground(outline);
        ImageView art=new ImageView(activity);art.setTag("art");art.setImageResource(boss.art);
        art.setScaleType(ImageView.ScaleType.CENTER_CROP);art.setImageAlpha(190);
        frame.addView(art,new FrameLayout.LayoutParams(-1,-1));
        View shade=new View(activity);shade.setTag("inverse");
        GradientDrawable veil=new GradientDrawable(GradientDrawable.Orientation.LEFT_RIGHT,
                new int[]{0xF20C0912,0xB0151020,0x330B0810});veil.setCornerRadius(AppTheme.dp(activity,20));
        shade.setBackground(veil);frame.addView(shade,new FrameLayout.LayoutParams(-1,-1));
        LinearLayout copy=new LinearLayout(activity);copy.setTag("inverse");copy.setOrientation(LinearLayout.VERTICAL);
        copy.setGravity(Gravity.BOTTOM);copy.setPadding(AppTheme.dp(activity,16),AppTheme.dp(activity,14),
                AppTheme.dp(activity,14),AppTheme.dp(activity,14));
        TextView difficulty=text(activity,boss.difficulty+(selected?" · ELEGIDO":""),10);difficulty.setTag("overline");
        TextView name=text(activity,boss.name,23);TextView focus=text(activity,boss.focus,13);focus.setTag("muted");
        TextView description=text(activity,boss.description,11);description.setMaxLines(2);
        copy.addView(difficulty);copy.addView(name);copy.addView(focus);copy.addView(description);
        frame.addView(copy,new FrameLayout.LayoutParams(-1,-1));
        frame.setEnabled(enabled&&!selected);frame.setAlpha(enabled||selected?1f:.72f);
        frame.setContentDescription(boss.name+", "+boss.difficulty+", "+boss.focus
                +(selected?", encuentro elegido":""));
        if(enabled&&!selected)frame.setOnClickListener(v->listener.select(boss.id));
        return frame;
    }

    private static TextView text(Activity activity,String value,int size) {
        TextView text=new TextView(activity);text.setText(value);text.setTextSize(size);
        text.setTypeface(android.graphics.Typeface.create("sans-serif",android.graphics.Typeface.BOLD));
        return text;
    }
}

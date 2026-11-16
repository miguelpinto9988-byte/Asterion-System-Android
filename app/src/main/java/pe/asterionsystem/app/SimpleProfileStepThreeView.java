package pe.asterionsystem.app;

import android.app.Activity;
import android.graphics.Typeface;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.view.Gravity;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * Plain native controls for the existing Step 3 modules. The option ids are
 * byte-for-byte compatible with the previous Canva step 3 implementation.
 * No screenshot-based clicks, no image download and no extra choices.
 */
final class SimpleProfileStepThreeView extends LinearLayout {
    private static final String[] THEMES = {"light","dark"};
    private static final String[] THEME_LABELS = {"Blanco","Negro"};
    private static final String[] ACCENTS = {
            "#258FE6","#9A68F2","#ED72B8","#34BC91","#E65260","#F2F5FF"};
    private static final String[] ACCENT_LABELS = {
            "Azul","Morado","Rosa","Verde","Rojo","Blanco"};
    private static final String[] COMPANIONS = {
            "igris","beru","tank","tusk","iron"};
    private static final String[] COMPANION_LABELS = {
            "Igris","Beru","Tank","Tusk","Iron"};
    // Keep stable stored IDs; the artwork is now the four approved ORIGINAL portraits.
    private static final String[] HUNTERS = {
            "sung_jinwoo","cha_haein","yoo_joonghyuk","kim_dokja"};
    private static final String[] HUNTER_LABELS = {
            "Cazador azul","Cazadora blanca y dorada","Guerrero oscuro","Estratega de blanco"};
    private static final String[] BACKGROUNDS = {
            "ruinas","niebla","obsidiana","aurora","bosque"};
    private static final String[] BACKGROUND_LABELS = {
            "Templo oriental","Playa tropical","Ciudad occidental","Desierto y cañones","Bosque oriental"};

    private final Activity activity;
    private final JSONObject draft;
    private final Runnable persist;
    private final Runnable onThemeChanged;
    private final float density;
    private TextView preview;
    private ImageView portraitView;
    private ImageView backgroundView;
    private ImageView companionView;
    private final java.util.ArrayList<ImageView> sectionIcons = new java.util.ArrayList<>();
    private final java.util.ArrayList<String> sectionIconNames = new java.util.ArrayList<>();

    SimpleProfileStepThreeView(Activity activity, JSONObject draft,
                               Runnable persist, Runnable onThemeChanged) {
        super(activity);
        this.activity=activity;
        this.draft=draft;
        this.persist=persist;
        this.onThemeChanged=onThemeChanged;
        density=activity.getResources().getDisplayMetrics().density;
        setOrientation(VERTICAL);
        migrateLegacy();

        section("01 · Tema del sistema", "theme", THEMES, THEME_LABELS,"light");
        section("02 · Color de acento", "accent", ACCENTS, ACCENT_LABELS,ACCENTS[0]);
        section("03 · Acompañante del sistema", "companion",
                COMPANIONS, COMPANION_LABELS, COMPANIONS[0]);
        section("04 · Silueta del cazador", "card_silhouette",
                HUNTERS, HUNTER_LABELS, HUNTERS[0]);
        section("05 · Fondo de la tarjeta", "card_background",
                BACKGROUNDS, BACKGROUND_LABELS, BACKGROUNDS[0]);

        // Native layered horizontal card: background WebP + transparent hunter WebP.
        // Missing drawables show a safe fallback, never reset saved selections.
        FrameLayout card = new FrameLayout(activity) {
            @Override protected void onMeasure(int widthMeasureSpec,int heightMeasureSpec) {
                int width = View.MeasureSpec.getSize(widthMeasureSpec);
                int height = Math.round(width * 9f / 16f);
                super.onMeasure(widthMeasureSpec,View.MeasureSpec.makeMeasureSpec(
                        height,View.MeasureSpec.EXACTLY));
            }
        };
        GradientDrawable cardFallback = new GradientDrawable(
                GradientDrawable.Orientation.TL_BR,
                new int[]{Color.rgb(31,37,61), Color.rgb(91,58,136)});
        cardFallback.setCornerRadius(dp(14));
        card.setBackground(cardFallback);
        card.setClipToOutline(true);
        backgroundView = new ImageView(activity);
        backgroundView.setScaleType(ImageView.ScaleType.CENTER_CROP);
        backgroundView.setTag("asterion_fullcolor_art");
        card.addView(backgroundView, new FrameLayout.LayoutParams(-1,-1));
        portraitView = new ImageView(activity);
        portraitView.setScaleType(ImageView.ScaleType.FIT_CENTER);
        portraitView.setTag("asterion_fullcolor_art");
        // The approved portrait is a transparent square; never crop it to a circle.
        // The card's fixed 16:9 aspect ratio makes 56.25% width equal 100% height.
        int cardWidth = activity.getResources().getDisplayMetrics().widthPixels;
        int squareSide = Math.round(cardWidth * 0.53f);
        FrameLayout.LayoutParams portraitParams = new FrameLayout.LayoutParams(
                squareSide, -1, Gravity.LEFT | Gravity.BOTTOM);
        card.addView(portraitView, portraitParams);
        companionView = new ImageView(activity);
        companionView.setScaleType(ImageView.ScaleType.FIT_CENTER);
        companionView.setTag("asterion_fullcolor_art");
        companionView.setContentDescription("Acompañante seleccionado");
        FrameLayout.LayoutParams companionParams = new FrameLayout.LayoutParams(
                dp(76), dp(76), Gravity.BOTTOM | Gravity.RIGHT);
        companionParams.rightMargin = dp(18);
        companionParams.bottomMargin = dp(12);
        card.addView(companionView, companionParams);
        TextView nameOnCard = new TextView(activity);
        String hunterName = draft.optString("name", "CAZADOR").trim();
        nameOnCard.setText(hunterName.isEmpty() ? "CAZADOR" : hunterName);
        nameOnCard.setTextColor(Color.WHITE);
        nameOnCard.setShadowLayer(dp(4),dp(1),dp(2),Color.BLACK);
        nameOnCard.setTextSize(15);
        nameOnCard.setTypeface(Typeface.DEFAULT,Typeface.BOLD);
        nameOnCard.setGravity(Gravity.RIGHT);
        nameOnCard.setTag("asterion_card_name");
        FrameLayout.LayoutParams nameParams = new FrameLayout.LayoutParams(
                -2,-2,Gravity.RIGHT | Gravity.TOP);
        nameParams.topMargin=dp(30);
        nameParams.rightMargin=dp(30);
        card.addView(nameOnCard,nameParams);
        LayoutParams cardLayout = new LayoutParams(-1,-2);
        cardLayout.topMargin = dp(18);
        addView(card, cardLayout);

        preview = new TextView(activity);
        preview.setTextSize(13);
        preview.setTypeface(Typeface.DEFAULT,Typeface.BOLD);
        preview.setPadding(dp(8),dp(14),dp(8),dp(14));
        addView(preview,new LayoutParams(-1,-2));
        refreshPreview();
    }

    private void section(String title, String key, String[] ids,
                         String[] labels, String fallback) {
        TextView heading=new TextView(activity);
        heading.setText(title);
        heading.setTextSize(16);
        heading.setTypeface(Typeface.DEFAULT,Typeface.BOLD);
        LayoutParams h=new LayoutParams(-1,-2);
        h.topMargin=dp(18);
        h.bottomMargin=dp(6);
        // Use the approved monochrome WebP icon set, not substitute symbols.
        String iconName = "tema".equals(key) ? "tema"
                : "accent".equals(key) ? "paleta"
                : "companion".equals(key) ? "companero"
                : "card_silhouette".equals(key) ? "silueta"
                : "card_background".equals(key) ? "fondo" : "paleta";
        LinearLayout headerRow=new LinearLayout(activity);
        headerRow.setOrientation(LinearLayout.HORIZONTAL);
        headerRow.setGravity(Gravity.CENTER_VERTICAL);
        ImageView icon=new ImageView(activity);
        icon.setTag("asterion_approved_icon");
        icon.setContentDescription(null);
        LinearLayout.LayoutParams iconParams=new LinearLayout.LayoutParams(dp(26),dp(26));
        iconParams.rightMargin=dp(9);
        headerRow.addView(icon,iconParams);
        headerRow.addView(heading,new LinearLayout.LayoutParams(-2,-2));
        addView(headerRow,h);
        sectionIcons.add(icon);
        sectionIconNames.add(iconName);

        RadioGroup group=new RadioGroup(activity);
        group.setOrientation(RadioGroup.VERTICAL);
        String selected=get(key,fallback);
        int checkedId=-1;
        for(int i=0;i<ids.length;i++) {
            RadioButton radio=new RadioButton(activity);
            radio.setId(View.generateViewId());
            radio.setText(labels[i]);
            radio.setTextSize(14);
            radio.setMinHeight(dp(48));
            radio.setPadding(dp(9),dp(6),dp(9),dp(6));
            radio.setContentDescription(title + ": " + labels[i]);
            group.addView(radio,new RadioGroup.LayoutParams(-1,-2));
            if(ids[i].equals(selected))checkedId=radio.getId();
        }
        if(checkedId>0)group.check(checkedId);
        if(checkedId<0 && "card_silhouette".equals(key) && "yoo_sangah".equals(selected)) {
            // Existing profiles may have an older fifth avatar selection.
            // Keep the stored value visible rather than silently overwriting it.
            RadioButton legacy=new RadioButton(activity);
            legacy.setId(View.generateViewId());
            legacy.setText("Retrato anterior (conservado)");
            legacy.setTextSize(14);
            legacy.setMinHeight(dp(48));
            group.addView(legacy,new RadioGroup.LayoutParams(-1,-2));
            group.check(legacy.getId());
        }
        group.setOnCheckedChangeListener((changed, id)-> {
            for(int i=0;i<ids.length;i++) {
                if(changed.getChildAt(i).getId()==id) {
                    put(key,ids[i]);
                    if("theme".equals(key))onThemeChanged.run();
                    refreshPreview();
                    return;
                }
            }
        });
        addView(group,new LayoutParams(-1,-2));
    }

    private void refreshPreview() {
        if(preview==null)return;
        boolean light = "light".equals(get("theme","light"));
        for (int i=0;i<sectionIcons.size();i++) {
            int res=ProfileCardVisualAssets.resourceId(activity,
                    (light ? "asterion_light_ic_" : "asterion_dark_ic_")
                            + sectionIconNames.get(i));
            sectionIcons.get(i).setImageResource(res != 0 ? res
                    : R.drawable.asterion_ic_settings);
        }
        int bg=ProfileCardVisualAssets.resourceId(activity,
                ProfileCardVisualAssets.backgroundDrawable(get("card_background", BACKGROUNDS[0])));
        int hunter=ProfileCardVisualAssets.resourceId(activity,
                ProfileCardVisualAssets.hunterDrawable(get("card_silhouette", HUNTERS[0])));
        backgroundView.setImageResource(bg != 0 ? bg : android.R.color.transparent);
        portraitView.setImageResource(hunter != 0 ? hunter : android.R.color.transparent);
        String companionName=ProfileCardVisualAssets.companionDrawable(
                get("companion", COMPANIONS[0]));
        int companion=companionName.isEmpty() ? 0 :
                ProfileCardVisualAssets.resourceId(activity, companionName);
        companionView.setImageResource(companion != 0 ? companion : android.R.color.transparent);
        preview.setText("Tarjeta del cazador\n"
                +"Tema: "+("light".equals(get("theme","light"))?"Blanco":"Negro")
                +" · Borde: "+get("accent",ACCENTS[0])
                +"\nAcompañante: "+labelFor(COMPANIONS,COMPANION_LABELS,
                        get("companion",COMPANIONS[0]))
                +"\nSilueta: "+labelFor(HUNTERS,HUNTER_LABELS,
                        get("card_silhouette",HUNTERS[0]))
                +"\nFondo: "+labelFor(BACKGROUNDS,BACKGROUND_LABELS,
                        get("card_background",BACKGROUNDS[0])));
    }

    private String labelFor(String[] ids,String[] labels,String value) {
        for(int i=0;i<ids.length;i++)if(ids[i].equals(value))return labels[i];
        return labels[0];
    }

    private void migrateLegacy() {
        mapOld("companion",
                new String[]{"vigia","estratega","archivista","oraculo","centinela"},
                COMPANIONS);
        mapOld("card_silhouette",
                new String[]{"caballero","errante","vigia","arconte","centinela"},
                HUNTERS);
    }
    private void mapOld(String key,String[] older,String[] current) {
        String value=get(key,"");
        for(int i=0;i<older.length;i++) {
            if(older[i].equals(value)) {
                // A retired fifth avatar must remain in the saved draft until
                // the user explicitly chooses another; never index beyond the
                // four approved options or silently discard their selection.
                if(i < current.length) put(key,current[i]);
                break;
            }
        }
    }

    private String get(String key,String fallback) {
        return draft.optString(key,fallback);
    }
    private void put(String key,String value) {
        try {
            draft.put(key,value);
            persist.run();
        }catch(JSONException failure){throw new IllegalStateException(failure);}
    }
    private int dp(int value){return Math.round(value*density);}
}

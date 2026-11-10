package pe.asterionsystem.app;

import android.app.Activity;
import android.graphics.Typeface;
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
    private static final String[] THEMES = {"dark","light"};
    private static final String[] THEME_LABELS = {"Negro","Blanco"};
    private static final String[] ACCENTS = {
            "#258FE6","#9A68F2","#ED72B8","#34BC91","#E65260","#F2F5FF"};
    private static final String[] ACCENT_LABELS = {
            "Azul","Morado","Rosa","Verde","Rojo","Blanco"};
    private static final String[] COMPANIONS = {
            "igris","beru","tank","tusk","iron"};
    private static final String[] COMPANION_LABELS = {
            "Igris","Beru","Tank","Tusk","Iron"};
    private static final String[] HUNTERS = {
            "sung_jinwoo","cha_haein","kim_dokja","yoo_joonghyuk","yoo_sangah"};
    private static final String[] HUNTER_LABELS = {
            "Sung Jinwoo","Cha Hae-In","Kim Dokja","Yoo Joonghyuk","Yoo Sangah"};
    private static final String[] BACKGROUNDS = {
            "ruinas","niebla","obsidiana","aurora","bosque"};
    private static final String[] BACKGROUND_LABELS = {
            "Ruinas arcanas","Niebla violeta","Obsidiana","Aurora dorada","Bosque ancestral"};

    private final Activity activity;
    private final JSONObject draft;
    private final Runnable persist;
    private final Runnable onThemeChanged;
    private final float density;
    private TextView preview;

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

        section("01 · Tema del sistema", "theme", THEMES, THEME_LABELS,"dark");
        section("02 · Color de los bordes", "accent", ACCENTS, ACCENT_LABELS,ACCENTS[0]);
        section("03 · Acompañante del sistema", "companion",
                COMPANIONS, COMPANION_LABELS, COMPANIONS[0]);
        section("04 · Silueta del cazador", "card_silhouette",
                HUNTERS, HUNTER_LABELS, HUNTERS[0]);
        section("05 · Fondo de la tarjeta", "card_background",
                BACKGROUNDS, BACKGROUND_LABELS, BACKGROUNDS[0]);

        preview = new TextView(activity);
        preview.setTextSize(13);
        preview.setTypeface(Typeface.DEFAULT,Typeface.NORMAL);
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
        addView(heading,h);

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
        preview.setText("Tarjeta del cazador\n"
                +"Tema: "+("light".equals(get("theme","dark"))?"Blanco":"Negro")
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
                put(key,current[i]);
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

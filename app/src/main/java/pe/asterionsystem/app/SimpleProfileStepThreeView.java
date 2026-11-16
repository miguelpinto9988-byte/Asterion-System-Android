package pe.asterionsystem.app;

import android.app.Activity;
import android.graphics.Typeface;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.graphics.drawable.Drawable;
import android.view.Gravity;
import android.widget.FrameLayout;
import android.widget.HorizontalScrollView;
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
    private final java.util.ArrayList<Runnable> selectorRefreshers = new java.util.ArrayList<>();

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

        // A single frameless 16:9 preview, below its own independent section.
        LinearLayout cardPanel = new LinearLayout(activity);
        cardPanel.setOrientation(VERTICAL);
        cardPanel.setTag("card");
        cardPanel.setPadding(dp(12),dp(12),dp(12),dp(12));
        LayoutParams panelParams=new LayoutParams(-1,-2);
        panelParams.topMargin=dp(14);
        addView(cardPanel,panelParams);
        TextView previewHeading=new TextView(activity);
        previewHeading.setText("Vista previa · Tarjeta del cazador");
        previewHeading.setTextSize(16);
        previewHeading.setTypeface(Typeface.DEFAULT,Typeface.BOLD);
        cardPanel.addView(previewHeading,new LayoutParams(-1,-2));

        FrameLayout card = new FrameLayout(activity) {
            @Override protected void onMeasure(int widthMeasureSpec,int heightMeasureSpec) {
                int width=View.MeasureSpec.getSize(widthMeasureSpec);
                int height=Math.round(width*9f/16f);
                super.onMeasure(widthMeasureSpec,View.MeasureSpec.makeMeasureSpec(
                        height,View.MeasureSpec.EXACTLY));
            }
        };
        GradientDrawable cardFallback=new GradientDrawable(
                GradientDrawable.Orientation.TL_BR,
                new int[]{Color.rgb(31,37,61),Color.rgb(91,58,136)});
        cardFallback.setCornerRadius(dp(16));
        card.setBackground(cardFallback);
        card.setClipToOutline(true);
        backgroundView=new ImageView(activity);
        backgroundView.setTag("asterion_fullcolor_art");
        backgroundView.setScaleType(ImageView.ScaleType.CENTER_CROP);
        card.addView(backgroundView,new FrameLayout.LayoutParams(-1,-1));

        // Keep all the approved square hunter art visible, fitted left at
        // exactly the card height. No CENTER_CROP and no circular clipping.
        portraitView=new ImageView(activity);
        portraitView.setTag("asterion_fullcolor_art");
        portraitView.setScaleType(ImageView.ScaleType.FIT_START);
        card.addView(portraitView,new FrameLayout.LayoutParams(
                -1,-1,Gravity.START|Gravity.BOTTOM));

        View textContrast=new View(activity);
        textContrast.setTag("asterion_fullcolor_art");
        textContrast.setBackground(new GradientDrawable(
                GradientDrawable.Orientation.TOP_BOTTOM,
                new int[]{0xB3000000,0x00000000,0x00000000,0x77000000}));
        card.addView(textContrast,new FrameLayout.LayoutParams(-1,-1));

        companionView=new ImageView(activity);
        companionView.setScaleType(ImageView.ScaleType.FIT_CENTER);
        companionView.setTag("asterion_fullcolor_art");
        companionView.setContentDescription("Acompañante seleccionado");
        FrameLayout.LayoutParams companionParams=new FrameLayout.LayoutParams(
                dp(78),dp(78),Gravity.BOTTOM|Gravity.END);
        companionParams.rightMargin=dp(10);
        companionParams.bottomMargin=dp(7);
        card.addView(companionView,companionParams);

        TextView nameOnCard=new TextView(activity);
        String hunterName=draft.optString("name","CAZADOR").trim();
        nameOnCard.setText(hunterName.isEmpty()?"CAZADOR":hunterName);
        nameOnCard.setTextColor(Color.WHITE);
        nameOnCard.setShadowLayer(dp(4),0,dp(2),Color.BLACK);
        nameOnCard.setTextSize(15);
        nameOnCard.setTypeface(Typeface.DEFAULT,Typeface.BOLD);
        nameOnCard.setGravity(Gravity.END);
        nameOnCard.setTag("asterion_card_name");
        FrameLayout.LayoutParams nameParams=new FrameLayout.LayoutParams(
                -2,-2,Gravity.END|Gravity.TOP);
        nameParams.topMargin=dp(12);
        nameParams.rightMargin=dp(12);
        card.addView(nameOnCard,nameParams);
        LayoutParams cardParams=new LayoutParams(-1,-2);
        cardParams.topMargin=dp(12);
        cardPanel.addView(card,cardParams);

        preview=new TextView(activity);
        preview.setTextSize(12);
        preview.setTypeface(Typeface.DEFAULT,Typeface.BOLD);
        preview.setPadding(dp(3),dp(12),dp(3),dp(2));
        cardPanel.addView(preview,new LayoutParams(-1,-2));
        refreshPreview();
    }

    /** A separate rounded panel and a finger-scrollable horizontal option strip. */
    private void section(String title, String key, String[] ids,
                         String[] labels, String fallback) {
        final boolean light="light".equals(get("theme","light"));
        LinearLayout panel=new LinearLayout(activity);
        panel.setOrientation(VERTICAL);
        panel.setTag("card");
        panel.setPadding(dp(12),dp(12),dp(12),dp(12));
        LayoutParams panelLayout=new LayoutParams(-1,-2);
        panelLayout.topMargin=dp(12);
        addView(panel,panelLayout);

        String iconName="theme".equals(key)?"tema"
                :"accent".equals(key)?"paleta"
                :"companion".equals(key)?"companero"
                :"card_silhouette".equals(key)?"silueta"
                :"card_background".equals(key)?"fondo":"paleta";
        LinearLayout headerRow=new LinearLayout(activity);
        headerRow.setOrientation(HORIZONTAL);
        headerRow.setGravity(Gravity.CENTER_VERTICAL);
        ImageView icon=new ImageView(activity);
        icon.setTag("asterion_approved_icon");
        icon.setContentDescription(null);
        LinearLayout.LayoutParams iconParams=new LinearLayout.LayoutParams(
                dp(25),dp(25));
        iconParams.rightMargin=dp(9);
        headerRow.addView(icon,iconParams);
        TextView heading=new TextView(activity);
        heading.setText(title);
        heading.setTypeface(Typeface.DEFAULT,Typeface.BOLD);
        heading.setTextSize(16);
        headerRow.addView(heading,new LinearLayout.LayoutParams(-1,-2));
        panel.addView(headerRow,new LayoutParams(-1,-2));
        sectionIcons.add(icon);
        sectionIconNames.add(iconName);

        TextView hint=new TextView(activity);
        hint.setText("Desliza horizontalmente para ver más opciones");
        hint.setTextSize(11);
        hint.setTypeface(Typeface.DEFAULT,Typeface.BOLD);
        hint.setTextColor(AppTheme.muted(light));
        LayoutParams hintLayout=new LayoutParams(-1,-2);
        hintLayout.topMargin=dp(5);
        hintLayout.bottomMargin=dp(9);
        panel.addView(hint,hintLayout);

        HorizontalScrollView scroll=new HorizontalScrollView(activity);
        scroll.setHorizontalScrollBarEnabled(false);
        scroll.setFillViewport(false);
        scroll.setClipToPadding(false);
        LinearLayout strip=new LinearLayout(activity);
        strip.setOrientation(HORIZONTAL);
        strip.setGravity(Gravity.TOP);
        scroll.addView(strip,new HorizontalScrollView.LayoutParams(-2,-2));
        panel.addView(scroll,new LayoutParams(-1,-2));

        final java.util.ArrayList<LinearLayout> tiles=new java.util.ArrayList<>();
        final java.util.ArrayList<TextView> tileLabels=new java.util.ArrayList<>();
        final String[][] capturedLabels={labels};
        for(int i=0;i<ids.length;i++){
            final String id=ids[i];
            LinearLayout tile=new LinearLayout(activity);
            tile.setTag("asterion_profile_option");
            tile.setOrientation(VERTICAL);
            tile.setGravity(Gravity.CENTER);
            tile.setPadding(dp(9),dp(9),dp(9),dp(9));
            int itemWidth="card_background".equals(key)?dp(194)
                    :"card_silhouette".equals(key)?dp(143)
                    :"companion".equals(key)?dp(126):dp(112);
            LinearLayout.LayoutParams tileParams=new LinearLayout.LayoutParams(
                    itemWidth,-2);
            tileParams.rightMargin=dp(9);
            strip.addView(tile,tileParams);
            View art=null;
            if("card_silhouette".equals(key)||"card_background".equals(key)
                    ||"companion".equals(key)) {
                ImageView picture=new ImageView(activity);
                picture.setTag("asterion_fullcolor_art");
                picture.setScaleType(ImageView.ScaleType.FIT_CENTER);
                String name="card_silhouette".equals(key)
                        ?ProfileCardVisualAssets.hunterDrawable(id)
                        :"companion".equals(key)
                        ?ProfileCardVisualAssets.companionDrawable(id)
                        :ProfileCardVisualAssets.backgroundDrawable(id);
                int res=ProfileCardVisualAssets.resourceId(activity,name);
                if(res!=0)picture.setImageResource(res);
                int height="card_background".equals(key)?dp(93)
                        :"card_silhouette".equals(key)?dp(124):dp(103);
                tile.addView(picture,new LinearLayout.LayoutParams(-1,height));
                art=picture;
            }else{
                View swatch=new View(activity);
                GradientDrawable swatchBg=new GradientDrawable();
                int fill="accent".equals(key)?Color.parseColor(id)
                        :"light".equals(id)?Color.WHITE:Color.rgb(24,24,33);
                swatchBg.setColor(fill);
                swatchBg.setCornerRadius(dp(12));
                swatchBg.setStroke(dp(1),AppTheme.border(light));
                swatch.setBackground(swatchBg);
                tile.addView(swatch,new LinearLayout.LayoutParams(dp(74),dp(48)));
                art=swatch;
            }
            TextView caption=new TextView(activity);
            caption.setText(labels[i]);
            caption.setTag("asterion_profile_label");
            caption.setTextSize(12);
            caption.setTypeface(Typeface.DEFAULT,Typeface.BOLD);
            caption.setGravity(Gravity.CENTER);
            caption.setTextColor(AppTheme.foreground(light));
            caption.setMaxLines(2);
            caption.setMinHeight(dp(38));
            LayoutParams labelParams=new LayoutParams(-1,-2);
            labelParams.topMargin=dp(6);
            tile.addView(caption,labelParams);
            tiles.add(tile);
            tileLabels.add(caption);
            tile.setClickable(true);
            tile.setFocusable(true);
            tile.setOnClickListener(v->{
                put(key,id);
                if("theme".equals(key))onThemeChanged.run();
                refreshPreview();
            });
        }
        Runnable redraw=()->{
            boolean themeLight="light".equals(get("theme","light"));
            String selected=get(key,fallback);
            hint.setTextColor(AppTheme.muted(themeLight));
            for(int i=0;i<ids.length;i++){
                LinearLayout tile=tiles.get(i);
                TextView label=tileLabels.get(i);
                boolean isSelected=ids[i].equals(selected);
                GradientDrawable bg=new GradientDrawable();
                bg.setColor(isSelected?AppTheme.soft(themeLight)
                        :AppTheme.surface(themeLight));
                bg.setCornerRadius(dp(14));
                bg.setStroke(isSelected?dp(2):dp(1),
                        isSelected?AppTheme.accent(themeLight):AppTheme.border(themeLight));
                tile.setBackground(bg);
                label.setTextColor(AppTheme.foreground(themeLight));
                tile.setSelected(isSelected);
                tile.setContentDescription(title+": "+labels[i]
                        +(isSelected?", seleccionado":"; toca para seleccionar"));
            }
        };
        selectorRefreshers.add(redraw);
        redraw.run();
        int selectedIndex=java.util.Arrays.asList(ids).indexOf(get(key,fallback));
        if(selectedIndex>0){
            final int pos=selectedIndex;
            scroll.post(()->scroll.scrollTo(tiles.get(pos).getLeft()-dp(8),0));
        }
    }

    private void refreshPreview() {
        for(Runnable update:selectorRefreshers)update.run();
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
                +" · Acento: "+get("accent",ACCENTS[0])
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

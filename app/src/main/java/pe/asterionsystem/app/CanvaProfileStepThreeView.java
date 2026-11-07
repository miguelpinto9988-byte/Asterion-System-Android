package pe.asterionsystem.app;

import android.app.Activity;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.view.Gravity;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.HorizontalScrollView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import org.json.JSONException;
import org.json.JSONObject;
import java.util.ArrayList;
import java.util.List;
import pe.asterionsystem.app.presentation.NeonFrameDrawable;

/**
 * Step 3, adapted from the approved Asterion Canva screen.
 *
 * All labels and controls are native Android views (not text baked into a
 * screenshot). Artwork is looked up from packaged local drawables so no
 * runtime network access, permissions or image generation are necessary.
 * The original four-step registration and persisted draft keys are retained.
 */
final class CanvaProfileStepThreeView extends LinearLayout {
    private static final int NAVY = Color.rgb(7, 22, 44);
    private static final int WHITE = Color.rgb(244, 248, 255);
    private static final int GOLD = Color.rgb(255, 207, 127);
    private static final int MUTED = Color.rgb(201, 222, 244);
    private static final int CYAN = Color.rgb(80, 196, 255);
    private static final String[] ACCENTS = {
            "#258FE6", "#9A68F2", "#ED72B8", "#34BC91", "#E65260", "#F2F5FF"};
    private static final String[] COLOR_NAMES = {
            "Azul", "Morado", "Rosa", "Verde", "Rojo", "Blanco"};
    private static final String[] COMPANIONS = {
            "igris", "beru", "tank", "tusk", "iron"};
    private static final String[] COMPANION_NAMES = {
            "Igris", "Beru", "Tank", "Tusk", "Iron"};
    private static final String[] HUNTERS = {
            "sung_jinwoo", "cha_haein", "kim_dokja", "yoo_joonghyuk", "yoo_sangah"};
    private static final String[] HUNTER_NAMES = {
            "Sung Jinwoo", "Cha Hae-In", "Kim Dokja", "Yoo Joonghyuk", "Yoo Sangah"};
    private static final String[] BACKGROUNDS = {
            "ruinas", "niebla", "obsidiana", "aurora", "bosque"};
    private static final String[] BACKGROUND_NAMES = {
            "Ruinas arcanas", "Niebla violeta", "Obsidiana",
            "Aurora dorada", "Bosque ancestral"};

    private static final class Option {
        final String key;
        final String id;
        final View surface;
        final int color;
        Option(String key, String id, View surface, int color) {
            this.key=key;
            this.id=id;
            this.surface=surface;
            this.color=color;
        }
    }

    private final Activity activity;
    private final JSONObject draft;
    private final Runnable save;
    private final float density;
    private final List<Option> options = new ArrayList<>();
    private FrameLayout hunterCard;
    private ImageView cardLandscape;
    private ImageView cardPortrait;
    private TextView cardName;
    private TextView borderExample;
    private View cardShade;
    private View cardBorder;

    CanvaProfileStepThreeView(Activity activity, JSONObject draft, Runnable save) {
        super(activity);
        this.activity=activity;
        this.draft=draft;
        this.save=save;
        this.density=activity.getResources().getDisplayMetrics().density;
        setOrientation(VERTICAL);
        setPadding(dp(2),dp(2),dp(2),dp(15));
        setClipChildren(false);
        migrateLegacyOptions();

        buildThemeSection();
        buildColorSection();
        buildCompanionSection();
        buildHunterCardSection();
        refresh();
    }

    private void buildThemeSection() {
        LinearLayout group=section("01. TEMA DEL SISTEMA",
                "Elige el estilo visual de la interfaz.", "☾", CYAN);
        LinearLayout row=row();
        addTheme(row,"dark","☾  Oscuro");
        addTheme(row,"light","☀  Claro");
        group.addView(row);
    }

    private void addTheme(LinearLayout row,String id,String label) {
        TextView item=label(label,15,WHITE);
        item.setGravity(Gravity.CENTER);
        item.setMinHeight(dp(52));
        item.setPadding(dp(8),dp(9),dp(8),dp(9));
        LinearLayout.LayoutParams p=new LinearLayout.LayoutParams(0,-2,1);
        p.setMargins(dp(3),dp(3),dp(3),dp(3));
        row.addView(item,p);
        select(item,"theme",id,CYAN);
    }

    private void buildColorSection() {
        LinearLayout group=section("02. COLOR DE LOS BORDES",
                "Personaliza el color principal del sistema.", "◈", 0xFFCB85FF);
        LinearLayout chips=scrollingRow(group);
        for(int i=0;i<ACCENTS.length;i++) {
            final String code=ACCENTS[i];
            TextView option=label("▢\n"+COLOR_NAMES[i],14,WHITE);
            option.setGravity(Gravity.CENTER);
            option.setMinHeight(dp(76));
            option.setMinWidth(dp(75));
            option.setMaxLines(2);
            option.setPadding(dp(6),dp(9),dp(6),dp(9));
            addTile(chips,option,75);
            select(option,"accent",code,Color.parseColor(code));
        }
        borderExample=label("✦  VISTA PREVIA DEL CONTORNO  ✦",14,WHITE);
        borderExample.setGravity(Gravity.CENTER);
        borderExample.setMinHeight(dp(58));
        borderExample.setPadding(dp(8),dp(9),dp(8),dp(9));
        LinearLayout.LayoutParams b=new LinearLayout.LayoutParams(-1,-2);
        b.topMargin=dp(10);
        group.addView(borderExample,b);
    }

    private void buildCompanionSection() {
        LinearLayout group=section("03. ACOMPAÑANTE DEL SISTEMA",
                "Selecciona tu acompañante de apoyo.", "♟", 0xFF56D89D);
        LinearLayout tiles=scrollingRow(group);
        for(int i=0;i<COMPANIONS.length;i++) {
            String id=COMPANIONS[i];
            View tile=portraitTile(
                    "asterion_companion_"+id,
                    R.drawable.asterion_reg_profile,
                    COMPANION_NAMES[i], dp(83),dp(85));
            addTile(tiles,tile,106);
            select(tile,"companion",id,0xFF9C7CFF);
        }
    }

    private void buildHunterCardSection() {
        LinearLayout group=section("04. TARJETA DEL CAZADOR",
                "Configura el diseño de tu tarjeta de cazador.", "▣", GOLD);

        addSubhead(group,"Silueta del cazador:");
        LinearLayout hunters=scrollingRow(group);
        for(int i=0;i<HUNTERS.length;i++) {
            String id=HUNTERS[i];
            View tile=portraitTile(
                    "asterion_hunter_"+id,
                    R.drawable.asterion_reg_profile,
                    HUNTER_NAMES[i],dp(95),dp(106));
            addTile(hunters,tile,116);
            select(tile,"card_silhouette",id,CYAN);
        }

        addSubhead(group,"Fondo de la tarjeta:");
        LinearLayout backgrounds=scrollingRow(group);
        for(int i=0;i<BACKGROUNDS.length;i++) {
            String id=BACKGROUNDS[i];
            View tile=portraitTile(
                    "asterion_card_background_"+id,
                    R.drawable.asterion_reg_background,
                    BACKGROUND_NAMES[i],dp(95),dp(73));
            addTile(backgrounds,tile,116);
            select(tile,"card_background",id,CYAN);
        }

        addSubhead(group,"Vista previa de la tarjeta:");
        hunterCard=new FrameLayout(activity);
        hunterCard.setMinimumHeight(dp(170));
        hunterCard.setClipToPadding(false);
        cardLandscape=new ImageView(activity);
        cardLandscape.setScaleType(ImageView.ScaleType.CENTER_CROP);
        hunterCard.addView(cardLandscape,new FrameLayout.LayoutParams(-1,-1));
        cardShade=new View(activity);
        cardShade.setBackground(new GradientDrawable(
                GradientDrawable.Orientation.LEFT_RIGHT,
                new int[]{0x2004091A,0xA9081432,0xEE07132B}));
        hunterCard.addView(cardShade,new FrameLayout.LayoutParams(-1,-1));
        cardPortrait=new ImageView(activity);
        cardPortrait.setScaleType(ImageView.ScaleType.FIT_CENTER);
        FrameLayout.LayoutParams portraitParams=new FrameLayout.LayoutParams(
                dp(139),-1,Gravity.START|Gravity.CENTER_VERTICAL);
        portraitParams.leftMargin=dp(1);
        hunterCard.addView(cardPortrait,portraitParams);
        cardName=label("",16,WHITE);
        cardName.setGravity(Gravity.CENTER_VERTICAL|Gravity.END);
        cardName.setMaxLines(3);
        cardName.setPadding(dp(6),dp(8),dp(8),dp(8));
        FrameLayout.LayoutParams nameParams=new FrameLayout.LayoutParams(
                -1,-1,Gravity.END);
        nameParams.leftMargin=dp(137);
        hunterCard.addView(cardName,nameParams);
        cardBorder=new View(activity);
        hunterCard.addView(cardBorder,new FrameLayout.LayoutParams(-1,-1));
        LinearLayout.LayoutParams cardParams=new LinearLayout.LayoutParams(-1,dp(176));
        cardParams.topMargin=dp(7);
        group.addView(hunterCard,cardParams);
    }

    private void migrateLegacyOptions() {
        replaceOld("companion",
                new String[]{"vigia","estratega","archivista","oraculo","centinela"},
                COMPANIONS);
        replaceOld("card_silhouette",
                new String[]{"caballero","errante","vigia","arconte","centinela"},
                HUNTERS);
    }

    private void replaceOld(String key,String[] oldIds,String[] newIds) {
        String current=draft.optString(key,"");
        for(int i=0;i<oldIds.length;i++) {
            if(oldIds[i].equals(current)) {
                put(key,newIds[i]);
                return;
            }
        }
    }

    private LinearLayout section(String title,String subtitle,String glyph,int iconColor) {
        LinearLayout outer=new LinearLayout(activity);
        outer.setOrientation(VERTICAL);
        outer.setPadding(dp(11),dp(13),dp(11),dp(13));
        outer.setBackground(panel(NAVY,0xFF315879,10,1));
        LinearLayout.LayoutParams params=new LinearLayout.LayoutParams(-1,-2);
        params.topMargin=dp(13);
        addView(outer,params);

        LinearLayout header=row();
        TextView emblem=label(glyph,23,WHITE);
        emblem.setGravity(Gravity.CENTER);
        emblem.setBackground(new NeonFrameDrawable(iconColor,density));
        header.addView(emblem,new LinearLayout.LayoutParams(dp(40),dp(43)));
        LinearLayout heading=new LinearLayout(activity);
        heading.setOrientation(VERTICAL);
        heading.setPadding(dp(10),0,0,0);
        TextView titleText=label(title,17,GOLD);
        titleText.setTypeface(Typeface.SERIF,Typeface.BOLD);
        titleText.setMaxLines(2);
        heading.addView(titleText);
        TextView detail=label(subtitle,13,MUTED);
        detail.setMaxLines(3);
        LinearLayout.LayoutParams detailParams=new LinearLayout.LayoutParams(-1,-2);
        detailParams.topMargin=dp(2);
        heading.addView(detail,detailParams);
        header.addView(heading,new LinearLayout.LayoutParams(0,-2,1));
        outer.addView(header);
        return outer;
    }

    private void addSubhead(LinearLayout parent,String title) {
        TextView text=label(title,14,MUTED);
        LinearLayout.LayoutParams p=new LinearLayout.LayoutParams(-1,-2);
        p.topMargin=dp(13);
        p.bottomMargin=dp(5);
        parent.addView(text,p);
    }

    private LinearLayout scrollingRow(LinearLayout parent) {
        HorizontalScrollView scroll=new HorizontalScrollView(activity);
        scroll.setHorizontalScrollBarEnabled(false);
        scroll.setFillViewport(false);
        scroll.setClipToPadding(false);
        LinearLayout line=row();
        line.setPadding(0,dp(4),0,dp(4));
        scroll.addView(line,new HorizontalScrollView.LayoutParams(-2,-2));
        LinearLayout.LayoutParams params=new LinearLayout.LayoutParams(-1,-2);
        params.topMargin=dp(5);
        parent.addView(scroll,params);
        return line;
    }

    private View portraitTile(String asset,int fallback,String name,int artWidth,int artHeight) {
        LinearLayout item=new LinearLayout(activity);
        item.setOrientation(VERTICAL);
        item.setGravity(Gravity.CENTER);
        item.setPadding(dp(4),dp(4),dp(4),dp(5));
        ImageView picture=new ImageView(activity);
        picture.setScaleType(ImageView.ScaleType.CENTER_CROP);
        picture.setImageResource(resolve(asset,fallback));
        picture.setContentDescription(null);
        item.addView(picture,new LinearLayout.LayoutParams(artWidth,artHeight));
        TextView caption=label(name,12,WHITE);
        caption.setGravity(Gravity.CENTER);
        caption.setMaxLines(2);
        caption.setMinHeight(dp(36));
        caption.setPadding(dp(1),dp(3),dp(1),dp(2));
        item.addView(caption,new LinearLayout.LayoutParams(-1,-2));
        return item;
    }

    private void addTile(LinearLayout line,View tile,int width) {
        LinearLayout.LayoutParams p=new LinearLayout.LayoutParams(dp(width),-2);
        p.setMargins(dp(3),dp(2),dp(3),dp(2));
        line.addView(tile,p);
    }

    private void select(View view,String key,String value,int selectedColor) {
        view.setClickable(true);
        view.setFocusable(true);
        view.setContentDescription(key+": "+value);
        options.add(new Option(key,value,view,selectedColor));
        view.setOnClickListener(v->{
            put(key,value);
            refresh();
        });
    }

    private void refresh() {
        for(Option option:options) {
            boolean selected=get(option.key,defaultFor(option.key)).equals(option.id);
            option.surface.setSelected(selected);
            option.surface.setBackground(
                    selected
                    ? panel(0xFF102950,option.color,9,2)
                    : panel(0xE4091930,0xFF6681A7,9,1));
            option.surface.setAlpha(selected?1f:0.94f);
            option.surface.setContentDescription(
                    option.id+(selected?", seleccionado":", no seleccionado"));
        }
        int color=Color.parseColor(get("accent",ACCENTS[0]));
        if(borderExample!=null)borderExample.setBackground(new NeonFrameDrawable(color,density));
        if(hunterCard!=null) {
            String selectedBackground=get("card_background","ruinas");
            cardLandscape.setImageResource(resolve("asterion_card_background_"+selectedBackground,
                    R.drawable.asterion_reg_background));
            String chosen=get("card_silhouette",HUNTERS[0]);
            cardPortrait.setImageResource(resolve("asterion_card_silhouette_"+chosen,
                    resolve("asterion_hunter_"+chosen,R.drawable.asterion_reg_profile)));
            String name=HUNTER_NAMES[0];
            for(int i=0;i<HUNTERS.length;i++) {
                if(HUNTERS[i].equals(chosen))name=HUNTER_NAMES[i];
            }
            cardName.setText("✦\n"+name.toUpperCase(java.util.Locale.ROOT)+"\nCAZADOR");
            boolean light=get("theme","dark").equals("light");
            cardShade.setBackground(new GradientDrawable(
                    GradientDrawable.Orientation.LEFT_RIGHT,
                    light ? new int[]{0x080F253D,0x928396A9,0xD8D7E4F4}
                          : new int[]{0x2004091A,0xA9081432,0xEE07132B}));
            cardName.setTextColor(light?0xFF0A1B34:WHITE);
            cardBorder.setBackground(panel(Color.TRANSPARENT,color,9,dp(2)));
            hunterCard.setBackground(panel(0xFF071A34,color,9,dp(2)));
        }
    }

    private String defaultFor(String key) {
        switch(key) {
            case "theme": return "dark";
            case "accent": return ACCENTS[0];
            case "companion": return COMPANIONS[0];
            case "card_silhouette": return HUNTERS[0];
            case "card_background": return BACKGROUNDS[0];
            default: return "";
        }
    }

    private int resolve(String name,int fallback) {
        int id=activity.getResources().getIdentifier(name,"drawable",activity.getPackageName());
        return id==0?fallback:id;
    }

    private String get(String key,String fallback) {
        String value=draft.optString(key,fallback);
        return value==null || value.isEmpty()?fallback:value;
    }

    private void put(String key,String value) {
        try {
            draft.put(key,value);
            save.run();
        } catch(JSONException error) {
            throw new IllegalStateException(error);
        }
    }

    private LinearLayout row() {
        LinearLayout r=new LinearLayout(activity);
        r.setOrientation(HORIZONTAL);
        r.setGravity(Gravity.CENTER_VERTICAL);
        return r;
    }

    private TextView label(String text,int sp,int color) {
        TextView view=new TextView(activity);
        view.setText(text);
        view.setTextColor(color);
        view.setTextSize(sp);
        view.setTypeface(Typeface.DEFAULT,Typeface.BOLD);
        view.setIncludeFontPadding(true);
        return view;
    }

    private static GradientDrawable panel(int background,int border,int corner,int stroke) {
        GradientDrawable drawable=new GradientDrawable();
        drawable.setColor(background);
        drawable.setStroke(stroke,border);
        drawable.setCornerRadius(corner);
        return drawable;
    }

    private int dp(float value) {return Math.round(value*density);}
}

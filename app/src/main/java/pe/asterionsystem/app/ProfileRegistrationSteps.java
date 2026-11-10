package pe.asterionsystem.app;

import android.app.Activity;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.graphics.drawable.Drawable;
import android.text.InputType;
import android.text.TextWatcher;
import android.text.Editable;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.AdapterView;
import org.json.JSONObject;
import org.json.JSONException;
import pe.asterionsystem.app.presentation.NeonFrameDrawable;

/** Native, data-driven forms for steps 2–4. These are provisional layouts; no HTML or runtime art. */
final class ProfileRegistrationSteps {
    static final int WHITE = Color.rgb(243, 247, 255);
    static final int MUTED = Color.rgb(186, 203, 236);
    static final int[] CAT_COLORS = {
        Color.rgb(32,136,183), Color.rgb(132,86,202), Color.rgb(211,148,55),
        Color.rgb(37,141,112), Color.rgb(185,81,114), Color.rgb(100,116,139)
    };
    static final String[] CATS = {
        "Tren superior", "Tren central", "Tren inferior", "Movilidad", "Cardio", "Descanso"
    };
    static final String[] CAT_GLYPHS = {"↟", "◈", "↡", "✧", "♥", "☾"};
    static final String[] CAT_ASSETS = {
        "upper", "core", "lower", "mobility", "cardio", "rest"
    };
    static final String[] DAYS = {"L","M","M","J","V","S","D"};
    static final String[] DAY_NAMES = {"Lunes","Martes","Miércoles","Jueves","Viernes","Sábado","Domingo"};
    static final String[] ACCENTS = {"#258FE6","#9A68F2","#ED72B8","#34BC91","#E65260","#F2F5FF"};

    private final Activity activity;
    private final JSONObject draft;
    private final Runnable persist;
    private final java.util.function.Consumer<Boolean> confirmListener;
    private final float density;

    ProfileRegistrationSteps(Activity activity, JSONObject draft, Runnable persist,
                             java.util.function.Consumer<Boolean> confirmListener) {
        this.activity = activity;
        this.draft = draft;
        this.persist = persist;
        this.confirmListener = confirmListener;
        this.density = activity.getResources().getDisplayMetrics().density;
    }

    private int dp(float value) { return Math.round(value * density); }
    private String get(String k, String fallback) {
        String value = draft.optString(k, fallback);
        return value == null ? fallback : value;
    }
    private void put(String k, Object value) {
        try {
            draft.put(k, value);
            persist.run();
        } catch (JSONException impossible) {
            throw new IllegalStateException(impossible);
        }
    }
    private LinearLayout vertical() {
        LinearLayout group = new LinearLayout(activity);
        group.setOrientation(LinearLayout.VERTICAL);
        group.setLayoutParams(new LinearLayout.LayoutParams(-1, -2));
        return group;
    }
    private LinearLayout row() {
        LinearLayout group = new LinearLayout(activity);
        group.setOrientation(LinearLayout.HORIZONTAL);
        group.setGravity(Gravity.CENTER_VERTICAL);
        group.setLayoutParams(new LinearLayout.LayoutParams(-1, -2));
        return group;
    }
    private TextView text(String text, int sp, int color) {
        TextView view = new TextView(activity);
        view.setText(text);
        view.setTextColor(color);
        view.setTextSize(sp);
        view.setTypeface(Typeface.DEFAULT, Typeface.BOLD);
        view.setGravity(Gravity.CENTER_VERTICAL);
        return view;
    }
    // Single shared mapping for icon reuse across the approved registration.
    // Once the companion WebP assets are uploaded, Android resolves their IDs;
    // older APKs continue using the existing vector/compiled fallback icons.
    private static final String[][] HEADING_ICONS = {
        {},
        {},
        {"experience","goal","custom_goal","mode","calendar","equipment"},
        {"theme","border","companion","hunter_card"},
        {"review","confirmation"}
    };
    private static final int[][] HEADING_FALLBACKS = {
        {},
        {},
        {R.drawable.asterion_ic_progress,R.drawable.asterion_ic_plan,
         R.drawable.asterion_reg_name,R.drawable.asterion_ic_plan,
         R.drawable.asterion_reg_age,R.drawable.asterion_ic_catalog},
        {R.drawable.asterion_ic_settings,R.drawable.asterion_reg_photo,
         R.drawable.asterion_reg_profile,R.drawable.asterion_reg_profile},
        {R.drawable.asterion_ic_status,R.drawable.asterion_ic_status}
    };
    private int visualStep = 2;
    private int sectionNumber = 0;

    private int iconResource(String name, int fallback) {
        int id = activity.getResources().getIdentifier(
                "asterion_ic_" + name, "drawable", activity.getPackageName());
        return id != 0 ? id : fallback;
    }

    private void addHeading(LinearLayout parent, String heading) {
        String[] names = HEADING_ICONS[visualStep];
        int currentSection = sectionNumber++;
        if (currentSection < names.length) {
            LinearLayout header = row();
            ImageView icon = new ImageView(activity);
            icon.setImageResource(iconResource(
                    names[currentSection], HEADING_FALLBACKS[visualStep][currentSection]));
            icon.setContentDescription(null);
            LinearLayout.LayoutParams iconParams =
                    new LinearLayout.LayoutParams(dp(33), dp(33));
            iconParams.rightMargin = dp(9);
            header.addView(icon,iconParams);
            header.addView(text(heading, 16, WHITE),
                    new LinearLayout.LayoutParams(0, -2, 1));
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(-1, -2);
            params.topMargin = dp(19);
            params.bottomMargin = dp(8);
            parent.addView(header,params);
            return;
        }

        TextView view = text(heading, 16, WHITE);
        LinearLayout.LayoutParams p = new LinearLayout.LayoutParams(-1, -2);
        p.topMargin = dp(19);
        p.bottomMargin = dp(8);
        view.setLayoutParams(p);
        parent.addView(view);
    }
    private void addInfo(LinearLayout parent, String content) {
        TextView view = text(content, 12, MUTED);
        LinearLayout.LayoutParams p = new LinearLayout.LayoutParams(-1, -2);
        p.bottomMargin = dp(8);
        view.setLayoutParams(p);
        parent.addView(view);
    }
    private EditText input(LinearLayout parent, String key, String hint, boolean multiline) {
        EditText edit = new EditText(activity);
        edit.setHint(hint);
        edit.setTextColor(WHITE);
        edit.setHintTextColor(MUTED);
        edit.setTextSize(15);
        edit.setTypeface(Typeface.DEFAULT, Typeface.BOLD);
        edit.setPadding(dp(11),dp(7),dp(11),dp(7));
        edit.setInputType(multiline
                ? InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_MULTI_LINE
                : InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_CAP_SENTENCES);
        if(multiline)edit.setMinLines(3); else edit.setSingleLine(true);
        edit.setBackground(fieldBackground());
        edit.setText(get(key, ""));
        edit.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s,int st,int c,int after) { }
            @Override public void onTextChanged(CharSequence s,int st,int before,int count) {
                put(key, s.toString());
            }
            @Override public void afterTextChanged(Editable s) { }
        });
        LinearLayout.LayoutParams p = new LinearLayout.LayoutParams(-1, -2);
        p.bottomMargin = dp(7);
        parent.addView(edit,p);
        return edit;
    }
    private GradientDrawable fieldBackground() {
        GradientDrawable bg = new GradientDrawable();
        bg.setColor(Color.argb(238, 13, 24, 51));
        bg.setCornerRadius(dp(8));
        bg.setStroke(dp(1),Color.rgb(115,139,197));
        return bg;
    }
    private RadioGroup radios(LinearLayout parent, String key, String[] ids, String[] labels,
                              String defaultId) {
        RadioGroup group = new RadioGroup(activity);
        group.setOrientation(RadioGroup.VERTICAL);
        String selected = get(key,defaultId);
        for(int i=0;i<ids.length;i++){
            final String value=ids[i];
            RadioButton button = new RadioButton(activity);
            button.setId(View.generateViewId());
            button.setText(labels[i]);
            button.setTextColor(WHITE);
            button.setTypeface(Typeface.DEFAULT,Typeface.BOLD);
            button.setTextSize(13);
            button.setButtonTintList(ColorStateList.valueOf(Color.rgb(172,148,245)));
            button.setMinHeight(dp(44));
            group.addView(button);
            if(value.equals(selected))button.setChecked(true);
            button.setOnClickListener(v->put(key,value));
        }
        parent.addView(group);
        return group;
    }
    private CheckBox check(LinearLayout parent, String key,String label,boolean defaultChecked) {
        CheckBox view = new CheckBox(activity);
        view.setText(label);
        view.setTextColor(WHITE);
        view.setTypeface(Typeface.DEFAULT, Typeface.BOLD);
        view.setButtonTintList(ColorStateList.valueOf(Color.rgb(172,148,245)));
        view.setTextSize(13);
        view.setMinHeight(dp(45));
        view.setChecked(Boolean.parseBoolean(get(key,String.valueOf(defaultChecked))));
        view.setOnCheckedChangeListener((button,checked)->put(key,checked));
        parent.addView(view);
        return view;
    }
    private Spinner spinner(LinearLayout parent, String key,String[] choices,String defaultValue) {
        Spinner control = new Spinner(activity);
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(
                activity, android.R.layout.simple_spinner_dropdown_item, choices) {
            @Override public View getView(int position, View convert, ViewGroup parent) {
                TextView item = (TextView)super.getView(position,convert,parent);
                boolean light = "light".equals(get("theme","light"));
                item.setTextColor(ProfileWizardTheme.foreground(light));
                item.setBackgroundColor(ProfileWizardTheme.background(light));
                item.setTypeface(Typeface.DEFAULT,Typeface.BOLD);
                return item;
            }
            @Override public View getDropDownView(int position, View convert, ViewGroup parent) {
                TextView item = (TextView)super.getDropDownView(position,convert,parent);
                boolean light = "light".equals(get("theme","light"));
                item.setTextColor(ProfileWizardTheme.foreground(light));
                item.setBackgroundColor(ProfileWizardTheme.background(light));
                item.setTypeface(Typeface.DEFAULT,Typeface.BOLD);
                return item;
            }
        };
        control.setAdapter(adapter);
        control.setBackground(fieldBackground());
        control.setPadding(dp(7),0,dp(7),0);
        String selected=get(key,defaultValue);
        for(int i=0;i<choices.length;i++)if(selected.equals(choices[i]))control.setSelection(i);
        control.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener(){
            @Override public void onItemSelected(AdapterView<?> p, View v,int i,long id){
                if(v instanceof TextView){
                    ((TextView)v).setTextColor(ProfileWizardTheme.foreground(
                            "light".equals(get("theme","light"))));
                    ((TextView)v).setTypeface(Typeface.DEFAULT,Typeface.BOLD);
                }
                put(key,choices[i]);
            }
            @Override public void onNothingSelected(AdapterView<?> p) { }
        });
        LinearLayout.LayoutParams lp=new LinearLayout.LayoutParams(-1,dp(54));
        lp.bottomMargin=dp(10);
        parent.addView(control,lp);
        return control;
    }

    View step2() {
        visualStep = 2;
        sectionNumber = 0;
        LinearLayout out=vertical();
        addHeading(out,"01 — Experiencia de entrenamiento");
        radios(out,"experience",new String[]{"principiante","intermedio","avanzado"},
                new String[]{"Principiante","Intermedio","Avanzado"},"principiante");
        addHeading(out,"02 — Objetivo principal");
        radios(out,"goal",new String[]{"bienestar","fuerza","resistencia","movilidad","constancia"},
                new String[]{"Mejorar mi bienestar general","Desarrollar fuerza funcional",
                        "Mejorar mi resistencia","Mejorar mi movilidad",
                        "Crear un hábito de actividad física"},"bienestar");
        addHeading(out,"03 — Objetivo personalizado");
        addInfo(out,"Tu texto se conservará exactamente para las futuras preferencias del motor de progreso.");
        input(out,"custom_goal","Describe tu objetivo con tus propias palabras",true);
        addHeading(out,"04 — Modalidad del Sistema");
        LinearLayout missionExtra=vertical();
        LinearLayout routineExtra=vertical();
        RadioGroup mode = radios(out,"mode",new String[]{"misiones","rutinas"},
                new String[]{"Misiones diarias","Rutinas"},"misiones");
        addInfo(missionExtra,"El número de misiones incluye actividades ligeras, aprendizaje y descansos; no significa entrenar varias veces al día.");
        spinner(missionExtra,"missions",new String[]{"1","2","3","4","5","6","7"},"3");
        addInfo(routineExtra,"Descanso preferido de 30, 60 o 90 segundos.");
        radios(routineExtra,"rest_seconds",new String[]{"30","60","90"},
                new String[]{"30 segundos","60 segundos","90 segundos"},"60");
        addInfo(routineExtra,"¿Cómo prefieres seguir tu rutina?");
        radios(routineExtra,"sequence",new String[]{"rondas","series"},
                new String[]{"Sesiones por rondas","Sesiones por series"},"rondas");
        addInfo(routineExtra,"Tiempo disponible por sesión");
        spinner(routineExtra,"minutes",new String[]{"10","15","20","30","45","60"},"30");
        out.addView(missionExtra);out.addView(routineExtra);
        Runnable showMode=()->{
            boolean missions=get("mode","misiones").equals("misiones");
            missionExtra.setVisibility(missions?View.VISIBLE:View.GONE);
            routineExtra.setVisibility(missions?View.GONE:View.VISIBLE);
        };
        showMode.run();
        for(int i=0;i<mode.getChildCount();i++){
            RadioButton button=(RadioButton)mode.getChildAt(i);
            button.setOnClickListener(v->{
                put("mode",iMode(mode,button)==0?"misiones":"rutinas");
                showMode.run();
            });
        }

        addHeading(out,"05 — Planificación semanal");
        addInfo(out,"Pulsa varias veces un día para avanzar: superior → central → inferior → movilidad → cardio → descanso.");
        LinearLayout week=row();
        final int[] index=readWeek();
        for(int day=0;day<7;day++){
            final int d=day;
            TextView chip=text("",13,WHITE);
            chip.setTag("asterion_week_color");
            chip.setGravity(Gravity.CENTER);
            chip.setMinHeight(dp(72));
            LinearLayout.LayoutParams p=new LinearLayout.LayoutParams(0,dp(72),1);
            p.setMargins(dp(1),dp(1),dp(1),dp(1));
            week.addView(chip,p);
            Runnable refresh=()->{
                // The color AND icon advance together on every press. If the
                // approved local WebP has not been uploaded, keep the glyph.
                int symbolId = iconResource(CAT_ASSETS[index[d]], 0);
                if (symbolId != 0) {
                    Drawable symbol = activity.getDrawable(symbolId);
                    symbol.setBounds(0, 0, dp(22), dp(22));
                    chip.setCompoundDrawables(null, null, null, symbol);
                    chip.setCompoundDrawablePadding(dp(4));
                    chip.setText(DAYS[d]);
                } else {
                    chip.setCompoundDrawables(null, null, null, null);
                    chip.setText(DAYS[d]+"\n"+CAT_GLYPHS[index[d]]);
                }
                GradientDrawable bg=new GradientDrawable();
                bg.setColor(CAT_COLORS[index[d]]);
                bg.setCornerRadius(dp(9));
                chip.setBackground(bg);
                chip.setContentDescription(DAY_NAMES[d]+": "+CATS[index[d]]+". Tocar para cambiar.");
            };
            refresh.run();
            chip.setOnClickListener(v->{
                index[d]=(index[d]+1)%6;
                put("week",formatWeek(index));
                refresh.run();
            });
        }
        out.addView(week);
        for(int k=0;k<6;k++){
            TextView legend=text(CAT_GLYPHS[k]+"  "+CATS[k],12,CAT_COLORS[k]);
            legend.setTag("asterion_week_legend");
            legend.setContentDescription("Leyenda: " + CATS[k] + ", color de categoría");
            legend.setPadding(dp(2),dp(4),0,dp(4));
            out.addView(legend);
        }

        addHeading(out,"06 — Equipamiento disponible");
        check(out,"bodyweight","Peso corporal / sin equipo",true);
        // Optional common supports; never assume the user has these items.
        check(out,"mat","Colchoneta o superficie acolchada estable",false);
        check(out,"wall","Pared o apoyo vertical firme",false);
        check(out,"chair","Silla estable",false);
        CheckBox dumbbells=check(out,"dumbbells","Mancuernas",false);
        LinearLayout dumbbellPanel=vertical();
        addInfo(dumbbellPanel,"Pesos de las mancuernas disponibles (kg), separados por comas");
        input(dumbbellPanel,"dumbbell_weights","Ejemplo: 2, 4, 6",false);
        out.addView(dumbbellPanel);
        dumbbellPanel.setVisibility(dumbbells.isChecked()?View.VISIBLE:View.GONE);
        dumbbells.setOnCheckedChangeListener((b,v)->{
            put("dumbbells",v);dumbbellPanel.setVisibility(v?View.VISIBLE:View.GONE);
        });
        CheckBox gym=check(out,"gym","Gimnasio",false);
        LinearLayout gymPanel=vertical();
        addInfo(gymPanel,"Selecciona los equipos a los que tienes acceso.");
        String[] equipment={"Poleas","Máquinas guiadas","Banco","Barras",
                "Bicicleta estática","Máquina de remo","Elíptica"};
        String[] keys={"polea","maquina","banco","barra","bicicleta","remo","eliptica"};
        for(int i=0;i<equipment.length;i++)check(gymPanel,"gym_"+keys[i],equipment[i],false);
        out.addView(gymPanel);
        gymPanel.setVisibility(gym.isChecked()?View.VISIBLE:View.GONE);
        gym.setOnCheckedChangeListener((b,v)->{
            put("gym",v);gymPanel.setVisibility(v?View.VISIBLE:View.GONE);
        });
        addHeading(out,"07 — Intensidad orientativa de las misiones");
        addInfo(out,"Baja selecciona opciones etiquetadas como suaves. Moderada "
                +"incluye también actividades moderadas y conserva las variantes "
                +"anteriores sin etiquetar. No se asigna intensidad alta "
                +"automáticamente.");
        radios(out,"intensity",new String[]{"baja","moderada"},
                new String[]{"Baja: movimientos suaves",
                        "Moderada: mayor variedad compatible con mi perfil"},
                "moderada");
        addInfo(out,"La categoría y la intensidad elegidas determinan qué ejercicios "
                +"puede proponer el motor. Cambiarlas genera una nueva selección "
                +"para el mismo día; cambiar de pantalla no la modifica.");
        return out;
    }
    private int iMode(RadioGroup group, RadioButton selected) {
        for(int i=0;i<group.getChildCount();i++)if(group.getChildAt(i)==selected)return i;
        return 0;
    }
    private int[] readWeek() {
        int[] week={5,5,5,5,5,5,5};
        String[] parts=get("week","5,5,5,5,5,5,5").split(",");
        if(parts.length==7)for(int i=0;i<7;i++)try{
            week[i]=Math.min(5,Math.max(0,Integer.parseInt(parts[i])));
        }catch(NumberFormatException ignore){week[i]=5;}
        return week;
    }
    private String formatWeek(int[] week) {
        StringBuilder s=new StringBuilder();
        for(int i=0;i<week.length;i++){
            if(i>0)s.append(',');
            s.append(week[i]);
        }
        return s.toString();
    }

    /** The Canva-approved Step 3 has independent, accessible native controls. */
    View step3() {
        visualStep=3;
        sectionNumber=0;
        return new CanvaProfileStepThreeView(activity,draft,persist);
    }

    private void addExpandableReview(LinearLayout out, TextView details,
                                     String title) {
        LinearLayout section=vertical();
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(-1,-2);
        lp.topMargin=dp(10);
        TextView header=text(title+"    ▾",14,WHITE);
        header.setMinHeight(dp(60));
        header.setPadding(dp(13),dp(11),dp(13),dp(11));
        header.setBackground(new NeonFrameDrawable(
                Color.rgb(120,172,255),density));
        header.setClickable(true);
        header.setFocusable(true);
        header.setContentDescription("Mostrar u ocultar "+title);
        details.setPadding(dp(13),dp(12),dp(13),dp(12));
        details.setBackground(fieldBackground());
        details.setVisibility(View.GONE);
        section.addView(header);
        section.addView(details);
        header.setOnClickListener(v->{
            boolean show=details.getVisibility()!=View.VISIBLE;
            details.setVisibility(show?View.VISIBLE:View.GONE);
            header.setText(title+(show?"    ▴":"    ▾"));
        });
        out.addView(section,lp);
    }

    View step4() {
        visualStep = 4;
        sectionNumber = 0;
        LinearLayout out=vertical();
        addHeading(out,"01 — Verificación del registro");
        addInfo(out,"Revisa tus respuestas; puedes volver a los pasos anteriores para modificarlas.");
        TextView identity=text("IDENTIDAD DEL CAZADOR\n"
                +"Nombre: "+get("name","Sin especificar")
                +"\nEdad: "+get("age","")
                +"\nEstatura (opcional): "+get("height","—")
                +"\nPeso (opcional): "+get("weight","—")
                +"\nGénero: "+get("gender","prefiero no decirlo"),13,WHITE);
        addExpandableReview(out,identity,"IDENTIDAD DEL CAZADOR");
        TextView training=text("ENTRENAMIENTO\nExperiencia: "+get("experience","principiante")
                +"\nObjetivo: "+get("goal","bienestar")
                +"\nIntensidad: "+get("intensity","moderada")
                +"\nObjetivo libre: "+get("custom_goal","—")
                +"\nModalidad: "+get("mode","misiones")
                +"\nDescanso: "+get("rest_seconds","60")+" segundos"
                +"\nSemana: "+readableWeek()
                +"\nEquipamiento: "+equipmentSummary(),13,WHITE);
        addExpandableReview(out,training,"ENTRENAMIENTO");
        TextView visual=text("PERSONALIZACIÓN\nTema: "+get("theme","light")
                +"\nBordes: "+get("accent","#9A68F2")
                +"\nAcompañante: "+get("companion","igris")
                +"\nSilueta: "+get("card_silhouette","sung_jinwoo")
                +"\nFondo de tarjeta: "+get("card_background","ruinas"),13,WHITE);
        addExpandableReview(out,visual,"PERSONALIZACIÓN");
        addHeading(out,"02 — Confirmación final");
        addInfo(out,"El perfil se guardará en almacenamiento privado. Podrás editar tus preferencias posteriormente; no se compartirá por internet.");
        CheckBox confirm=new CheckBox(activity);
        confirm.setText("He revisado mi información y deseo activar mi perfil de cazador.");
        confirm.setTextColor(WHITE);
        confirm.setTypeface(Typeface.DEFAULT,Typeface.BOLD);
        confirm.setButtonTintList(ColorStateList.valueOf(Color.rgb(172,148,245)));
        confirm.setMinHeight(dp(64));
        confirm.setChecked(false);
        confirm.setOnCheckedChangeListener((b,yes)->confirmListener.accept(yes));
        out.addView(confirm);
        confirmListener.accept(false);
        return out;
    }

    private String readableWeek() {
        int[] values=readWeek();
        StringBuilder b=new StringBuilder();
        for(int i=0;i<7;i++) {
            if(i>0)b.append("; ");
            b.append(DAY_NAMES[i]).append(": ").append(CATS[values[i]]);
        }
        return b.toString();
    }

    private String equipmentSummary() {
        StringBuilder b=new StringBuilder();
        if(Boolean.parseBoolean(get("bodyweight","true")))b.append("Peso corporal; ");
        if(Boolean.parseBoolean(get("dumbbells","false")))b.append("Mancuernas; ");
        if(Boolean.parseBoolean(get("gym","false")))b.append("Gimnasio; ");
        return b.length()==0?"Por definir":b.toString();
    }
}

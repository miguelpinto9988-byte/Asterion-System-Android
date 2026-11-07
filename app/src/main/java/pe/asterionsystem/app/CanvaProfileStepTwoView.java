package pe.asterionsystem.app;

import android.app.Activity;
import android.app.AlertDialog;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.text.Editable;
import android.text.InputType;
import android.text.TextWatcher;
import android.view.Gravity;
import android.view.View;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * Two approved Canva Paso 2 states with native interactive controls.
 * All preferences use the existing draft keys, without network access.
 */
final class CanvaProfileStepTwoView extends FrameLayout {
    private static final int ART_W = 641, ART_H = 1209;
    private static final int GOLD = Color.rgb(247, 195, 100);
    private static final int CYAN = Color.rgb(102, 219, 255);
    private static final String[] GOALS = {"bienestar","fuerza","resistencia","movilidad","constancia"};
    private static final String[] CATEGORIES = {"Tren superior","Tren central","Tren inferior","Movilidad","Cardio","Descanso"};
    private static final String[] DAYS = {"Lunes","Martes","Miércoles","Jueves","Viernes","Sábado","Domingo"};
    private static final String[] GYM_NAMES = {"Poleas","Máquinas guiadas","Banco","Barras","Bicicleta estática","Máquina de remo","Elíptica"};
    private static final String[] GYM_KEYS = {"polea","maquina","banco","barra","bicicleta","remo","eliptica"};
    private final Activity activity;
    private final JSONObject draft;
    private final Runnable save;
    private final FrameLayout canvas;
    private final ImageView picture;
    private final TextView missionValue, routineValue;
    private final View[] mission = new View[7], duration = new View[6],
            rest = new View[3], sequence = new View[2],
            experience = new View[3], goal = new View[5], equipment = new View[3];

    CanvaProfileStepTwoView(Activity owner, JSONObject draft, Runnable save,
                            Runnable previous, Runnable next) {
        super(owner);
        this.activity=owner; this.draft=draft; this.save=save;
        setBackgroundColor(Color.rgb(6, 23, 43));
        canvas=new FrameLayout(owner);
        addView(canvas,new FrameLayout.LayoutParams(ART_W,ART_H));
        picture=new ImageView(owner);
        picture.setScaleType(ImageView.ScaleType.FIT_XY);
        picture.setContentDescription("Paso 2 de 4 · Entrenamiento");
        canvas.addView(picture,box(0,0,ART_W,ART_H));
        String[] experienceCodes={"principiante","intermedio","avanzado"};
        for(int i=0;i<3;i++) {
            final int k=i;
            experience[i]=hit(208+121*i,286,114,35,"Experiencia: "+experienceCodes[i],
                    ()->{put("experience",experienceCodes[k]);render();});
        }
        for(int i=0;i<5;i++) {
            final int k=i;
            goal[i]=hit(197+76*i,375,73,65,"Objetivo principal: "+GOALS[i],
                    ()->{put("goal",GOALS[k]);render();});
        }
        EditText personal=new EditText(owner);
        personal.setSingleLine(true); personal.setTextSize(12);
        personal.setTextColor(Color.WHITE);personal.setHintTextColor(Color.rgb(170,190,210));
        personal.setHint("Escribe tu objetivo");
        personal.setTypeface(Typeface.SERIF,Typeface.ITALIC);
        personal.setText(get("custom_goal",""));
        personal.setBackground(panel(0xEF08192D,Color.rgb(75,127,169),8,1));
        personal.setPadding(6,0,5,0);
        canvas.addView(personal,box(249,485,322,33));
        personal.addTextChangedListener(new TextWatcher() {
            public void beforeTextChanged(CharSequence s,int start,int count,int after){}
            public void onTextChanged(CharSequence s,int start,int before,int count){put("custom_goal",s.toString());}
            public void afterTextChanged(Editable s){}
        });

        hit(207,572,174,37,"Seleccionar Misiones",()->{put("mode","misiones");render();});
        hit(392,572,178,37,"Seleccionar Rutinas",()->{put("mode","rutinas");render();});
        missionValue=valueLabel(313,617,255,36);
        routineValue=valueLabel(358,689,213,34);
        for(int i=0;i<7;i++) {
            final int count=i+1;
            mission[i]=hit(321,683+i*27,248,26,"Número de misiones: "+count,
                    ()->{put("missions",count);render();});
        }
        for(int i=0;i<3;i++) {
            final int seconds=30+i*30;
            rest[i]=hit(316+i*83,619,72,30,"Descanso: "+seconds+" segundos",
                    ()->{put("rest_seconds",seconds);render();});
        }
        sequence[0]=hit(316,654,133,31,"Sesiones por rondas",
                ()->{put("sequence","rondas");render();});
        sequence[1]=hit(456,654,111,31,"Sesiones por series",
                ()->{put("sequence","series");render();});
        int[] mins={10,15,20,30,45,60};
        for(int i=0;i<mins.length;i++) {
            final int minutes=mins[i];
            duration[i]=hit(371,741+25*i,196,25,"Duración: "+minutes+" minutos",
                    ()->{put("minutes",minutes);render();});
        }
        hit(189,678,110,40,"Planificación semanal",this::editWeek);
        hit(189,727,115,65,"Planificación semanal",this::editWeek);
        equipment[0]=hit(198,917,117,54,"Peso corporal",
                ()->{put("bodyweight",!bool("bodyweight",true));render();});
        equipment[1]=hit(325,917,117,54,"Mancuernas",this::editDumbbells);
        equipment[2]=hit(451,917,119,54,"Gimnasio",this::editGym);
        hit(181,984,163,70,"Anterior, Paso 1",previous);
        hit(352,984,225,70,"Siguiente, Paso 3",next);
        render();
    }

    private void render() {
        boolean missions=get("mode","misiones").equals("misiones");
        picture.setImageResource(missions ? R.drawable.asterion_canva_paso2_misiones
                : R.drawable.asterion_canva_paso2_rutinas);
        missionValue.setVisibility(missions?VISIBLE:GONE);
        routineValue.setVisibility(missions?GONE:VISIBLE);
        String count=get("missions","3");
        missionValue.setText(count+(count.equals("1")?" misión":" misiones"));
        routineValue.setText(get("minutes","30")+" minutos");
        for(int i=0;i<7;i++){
            mission[i].setVisibility(missions?VISIBLE:GONE);
            highlight(mission[i],count.equals(String.valueOf(i+1)));
        }
        int[] mins={10,15,20,30,45,60};
        for(int i=0;i<6;i++){
            duration[i].setVisibility(missions?GONE:VISIBLE);
            highlight(duration[i],get("minutes","30").equals(String.valueOf(mins[i])));
        }
        for(int i=0;i<3;i++){
            rest[i].setVisibility(missions?GONE:VISIBLE);
            highlight(rest[i],get("rest_seconds","60").equals(String.valueOf(30+i*30)));
            highlight(experience[i],get("experience","principiante").equals(
                    new String[]{"principiante","intermedio","avanzado"}[i]));
        }
        for(int i=0;i<2;i++){
            sequence[i].setVisibility(missions?GONE:VISIBLE);
            highlight(sequence[i],get("sequence","rondas").equals(i==0?"rondas":"series"));
        }
        for(int i=0;i<5;i++)highlight(goal[i],get("goal","bienestar").equals(GOALS[i]));
        highlight(equipment[0],bool("bodyweight",true));
        highlight(equipment[1],bool("dumbbells",false));
        highlight(equipment[2],bool("gym",false));
    }

    private void editDumbbells() {
        boolean enabled=bool("dumbbells",false);
        new AlertDialog.Builder(activity).setTitle("Mancuernas disponibles")
                .setMessage("Configura tus mancuernas y sus pesos disponibles en kg.")
                .setPositiveButton(enabled?"Desactivar":"Activar",
                    (d,w)->{put("dumbbells",!enabled);render();})
                .setNeutralButton("Configurar pesos",(d,w)->{
                    EditText field=new EditText(activity);
                    field.setHint("Ej.: 2, 4, 6 kg");
                    field.setInputType(InputType.TYPE_CLASS_TEXT);
                    field.setText(get("dumbbell_weights",""));
                    new AlertDialog.Builder(activity).setTitle("Pesos disponibles")
                        .setView(field).setPositiveButton("Guardar",(dialog,which)->{
                            put("dumbbells",true);
                            put("dumbbell_weights",field.getText().toString());
                            render();
                        }).setNegativeButton("Cancelar",null).show();
                }).setNegativeButton("Cancelar",null).show();
    }
    private void editGym() {
        boolean[] checked=new boolean[GYM_KEYS.length];
        for(int i=0;i<checked.length;i++)checked[i]=bool("gym_"+GYM_KEYS[i],false);
        new AlertDialog.Builder(activity).setTitle("Equipamiento de gimnasio")
            .setMultiChoiceItems(GYM_NAMES,checked,(d,i,value)->checked[i]=value)
            .setPositiveButton("Guardar",(d,w)->{
                put("gym",true);
                for(int i=0;i<GYM_KEYS.length;i++)put("gym_"+GYM_KEYS[i],checked[i]);
                render();
            }).setNeutralButton("Sin gimnasio",(d,w)->{put("gym",false);render();})
            .setNegativeButton("Cancelar",null).show();
    }
    private void editWeek() {
        new AlertDialog.Builder(activity).setTitle("Planificación semanal")
            .setItems(DAYS,(dialog,day)->{
                String[] current=get("week","5,5,5,5,5,5,5").split(",");
                int selected=5;
                if(current.length==7)try{selected=Integer.parseInt(current[day]);}
                catch(NumberFormatException ignored){}
                new AlertDialog.Builder(activity).setTitle(DAYS[day])
                    .setSingleChoiceItems(CATEGORIES,Math.max(0,Math.min(5,selected)),
                        (d,chosen)->{
                            String[] week=get("week","5,5,5,5,5,5,5").split(",");
                            if(week.length!=7)week=new String[]{"5","5","5","5","5","5","5"};
                            week[day]=String.valueOf(chosen);
                            put("week",android.text.TextUtils.join(",",week));
                            d.dismiss();
                        }).setNegativeButton("Cancelar",null).show();
            }).setNegativeButton("Cerrar",null).show();
    }
    private void put(String key,Object value) {
        try{draft.put(key,value);save.run();}
        catch(JSONException failure){throw new IllegalStateException(failure);}
    }
    private String get(String key,String def){return draft.optString(key,def);}
    private boolean bool(String key,boolean def){return draft.optBoolean(key,def);}
    private View hit(int x,int y,int w,int h,String desc,Runnable action) {
        View v=new View(activity);
        v.setClickable(true);v.setFocusable(true);
        v.setContentDescription(desc);
        canvas.addView(v,box(x,y,w,h));
        v.setOnClickListener(view->action.run());
        return v;
    }
    private TextView valueLabel(int x,int y,int w,int h) {
        TextView label=new TextView(activity);
        label.setGravity(Gravity.CENTER);label.setTextSize(12);label.setTextColor(GOLD);
        label.setTypeface(Typeface.SERIF,Typeface.BOLD);
        label.setBackground(panel(0xF2091C31,0xFF328ABD,7,1));
        canvas.addView(label,box(x,y,w,h));
        return label;
    }
    private void highlight(View view,boolean active) {
        view.setBackground(active?panel(0x270088BB,CYAN,6,2):null);
        view.setSelected(active);
    }
    private static GradientDrawable panel(int fill,int border,int radius,int stroke) {
        GradientDrawable d=new GradientDrawable();
        d.setColor(fill);d.setCornerRadius(radius);d.setStroke(stroke,border);
        return d;
    }
    private static FrameLayout.LayoutParams box(int x,int y,int w,int h) {
        FrameLayout.LayoutParams p=new FrameLayout.LayoutParams(w,h);
        p.leftMargin=x;p.topMargin=y;return p;
    }
    @Override protected void onSizeChanged(int w,int h,int oldw,int oldh){
        super.onSizeChanged(w,h,oldw,oldh);
        float scale=Math.min((float)w/ART_W,(float)h/ART_H);
        canvas.setPivotX(0);canvas.setPivotY(0);
        canvas.setScaleX(scale);canvas.setScaleY(scale);
        canvas.setX((w-ART_W*scale)/2f);
        canvas.setY((h-ART_H*scale)/2f);
    }
}

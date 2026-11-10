package pe.asterionsystem.app;

import android.app.AlertDialog;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.activity.ComponentActivity;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.HashMap;
import java.util.Map;
import pe.asterionsystem.core.engine.exercises.ExerciseFamily;
import pe.asterionsystem.core.engine.exercises.ExerciseType;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import org.json.JSONObject;
import pe.asterionsystem.app.data.AssetExerciseCatalogRepository;
import pe.asterionsystem.core.engine.exercises.ExerciseCatalog;
import pe.asterionsystem.core.engine.exercises.ExerciseVariant;
import pe.asterionsystem.core.engine.missions.MissionPlanner;
import pe.asterionsystem.core.engine.missions.SessionInterruptionPolicy;

/**
 * Guided, resumable session for daily tasks or an ACTIVE opt-in Monarca cycle.
 * Series: finish one exercise at a time, optionally recording additional series.
 * Rounds: visit each exercise, optionally choose another round.
 *
 * No prescribed reps/sets, no forced exercise, no auto-resume after serious signs.
 */
public final class RoutineStepActivity extends ComponentActivity {
    private final ExecutorService worker=Executors.newSingleThreadExecutor();
    private List<ExerciseVariant> exercises=Collections.emptyList();
    private final Map<String,Integer> categoriesById=new HashMap<>();
    private String kind;
    private LocalDate planDate;
    private int category;
    private String sequence;
    private int restSeconds;
    private int index;
    private int round=1;
    private int extraSeries;
    private String token;
    private boolean light;
    private boolean closed;
    private float density;
    private CountDownTimer restTimer;

    private TextView title;
    private TextView subtitle;
    private LinearLayout details;
    private Button restButton;
    private Button nextButton;
    private Button pauseButton;
    private Button stopButton;

    @Override protected void onCreate(Bundle saved) {
        super.onCreate(saved);
        setContentView(R.layout.screen_routine_step);
        density=getResources().getDisplayMetrics().density;
        title=findViewById(R.id.routine_title);
        subtitle=findViewById(R.id.routine_subtitle);
        details=findViewById(R.id.routine_content);
        restButton=findViewById(R.id.routine_rest);
        nextButton=findViewById(R.id.routine_next);
        pauseButton=findViewById(R.id.routine_pause);
        stopButton=findViewById(R.id.routine_stop);

        JSONObject savedProfile=ProfileStore.getRegistration(this);
        light="light".equals(savedProfile.optString("theme","light"));
        sequence="series".equals(savedProfile.optString("sequence","rondas"))
                ?"series":"rondas";
        restSeconds=parseRest(savedProfile.optString("rest_seconds","60"));
        kind=getIntent().getStringExtra("kind");
        category=getIntent().getIntExtra("category",-1);
        try {
            planDate=LocalDate.parse(getIntent().getStringExtra("date"));
        }catch(RuntimeException badDate) {
            block("No se pudo comprobar la fecha de esta misión.");
            return;
        }
        if(!"daily".equals(kind) && !"monarch".equals(kind)) {
            block("Tipo de misión desconocido.");
            return;
        }
        final ArrayList<String> ids=getIntent().getStringArrayListExtra("variants");
        if(ids==null || ids.isEmpty() || ids.size()>7) {
            block("No hay ejercicios para esta sesión.");
            return;
        }
        if(SessionSafetyStore.globallyPaused(this)) {
            block("El entrenamiento está suspendido por síntomas importantes. "
                    +"Solicita valoración médica; no se reanudará automáticamente.");
            return;
        }

        int[] colors={0xFF2088B7,0xFF8456CA,0xFFD39437,
                0xFF258D70,0xFFB95172,0xFF64748B};
        findViewById(R.id.routine_category_color).setBackgroundColor(
                category>=0&&category<colors.length?colors[category]:colors[5]);
        applyTheme(findViewById(R.id.routine_root));

        restButton.setOnClickListener(v->startOrCancelRest());
        pauseButton.setOnClickListener(v->new AlertDialog.Builder(this)
                .setTitle("Pausar sesión")
                .setMessage("Puedes detenerte y continuar en otro momento, "
                        +"siempre que no haya dolor ni señales de alarma. "
                        +"No habrá penalizaciones.")
                .setNegativeButton("Continuar",null)
                .setPositiveButton("Pausar",(d,w)->finish()).show());
        stopButton.setOnClickListener(v->questionnaire());
        nextButton.setEnabled(false);
        subtitle.setText("Comprobando ejercicios y autorización…");

        worker.execute(()->{
            try {
                ExerciseCatalog catalog=new AssetExerciseCatalogRepository(this).load();
                Map<String,String> familyForType=new HashMap<>();
                Map<String,Integer> categoryForFamily=new HashMap<>();
                for(ExerciseType type:catalog.types())
                    familyForType.put(type.id(),type.familyId());
                for(ExerciseFamily family:catalog.families()) {
                    int index=codeToCategory(family.category());
                    categoryForFamily.put(family.id(),index);
                }
                ArrayList<ExerciseVariant> validated=new ArrayList<>();
                for(String id:ids) {
                    ExerciseVariant variant=catalog.variant(id).orElse(null);
                    if(variant==null || validated.contains(variant)) {
                        runOnUiThread(()->block("La lista de ejercicios ya no es válida."));
                        return;
                    }
                    validated.add(variant);
                    categoriesById.put(variant.id(),categoryForFamily.getOrDefault(
                            familyForType.get(variant.typeId()),-1));
                }

                if("daily".equals(kind)) {
                    MissionPlanner.Profile profile=MissionProfileAdapter.fromRegistration(
                            ProfileStore.getRegistration(this));
                    if(category<0||category>4||!planDate.equals(LocalDate.now())
                            ||profile.weekly.get(planDate.getDayOfWeek().getValue()-1)!=category
                            ||SessionSafetyStore.categoryPaused(this,category)) {
                        runOnUiThread(()->block("La misión diaria cambió o quedó en pausa."));
                        return;
                    }
                } else {
                    MonarchSessionStore.Snapshot snapshot=MonarchSessionStore.current(
                            this,LocalDate.now(),new MissionPlanner(catalog),
                            MissionProfileAdapter.fromRegistration(
                                    ProfileStore.getRegistration(this)),catalog);
                    boolean matching=MonarchSessionStore.ACTIVE.equals(snapshot.status)
                            &&planDate.equals(snapshot.acceptedDate)
                            &&snapshot.exercises.size()==validated.size();
                    if(matching) {
                        Set<String> allowed=new java.util.HashSet<>();
                        for(ExerciseVariant v:snapshot.exercises)allowed.add(v.id());
                        for(ExerciseVariant v:validated)if(!allowed.contains(v.id()))matching=false;
                    }
                    if(!matching) {
                        runOnUiThread(()->block("La misión Monarca no está activa o caducó."));
                        return;
                    }
                    // Monarca mixes categories. Infer each exercise category
                    // from taxonomy, not today's unrelated calendar slot.
                }
                runOnUiThread(()->{
                    if(closed || isFinishing())return;
                    if(SessionSafetyStore.globallyPaused(this)){
                        block("La sesión está suspendida; solicita valoración médica.");
                        return;
                    }
                    exercises=validated;
                    token=RoutineSessionStore.token(kind,planDate,exercises);
                    RoutineSessionStore.State state=RoutineSessionStore.read(
                            this,token,exercises.size());
                    index=state.index;
                    round=state.round;
                    extraSeries=state.extraSeries;
                    render();
                });
            }catch(RuntimeException failure) {
                runOnUiThread(()->block("La biblioteca no pudo cargar los ejercicios. "
                        +"No se iniciará una rutina con datos incompletos."));
            }
        });
    }

    private int parseRest(String value) {
        try {
            int sec=Integer.parseInt(value);
            return sec==30||sec==60||sec==90?sec:60;
        }catch(NumberFormatException bad){return 60;}
    }

    private void block(String why) {
        subtitle.setText(why);
        details.removeAllViews();
        nextButton.setEnabled(false);
        restButton.setEnabled(false);
        stopButton.setEnabled(false);
    }

    private void render() {
        if(exercises.isEmpty())return;
        if(SessionSafetyStore.globallyPaused(this)
                ||("daily".equals(kind) && SessionSafetyStore.categoryPaused(this,category))) {
            block("Sesión detenida por seguridad. Vuelve al módulo de Misiones.");
            return;
        }
        details.removeAllViews();
        if(index<exercises.size() && "monarch".equals(kind)) {
            int currentCategory=effectiveCategory();
            if(currentCategory>=0
                    && SessionSafetyStore.categoryPaused(this,currentCategory)) {
                title.setText("CATEGORÍA EN PAUSA");
                subtitle.setText("Este movimiento pertenece a una categoría pausada.");
                addText("Puedes omitirlo y continuar con otros grupos musculares "
                        +"que no estén en pausa.",14);
                addAction("Omitir sin registrar",()->{
                    index++;save();render();
                });
                nextButton.setEnabled(false);
                return;
            }
        }
        boolean byRounds="rondas".equals(sequence);
        title.setText("RUTINA PASO A PASO");
        subtitle.setText(("daily".equals(kind)?"Misión diaria":"Misión Monarca")
                +" · Modo: "+(byRounds?"Rondas":"Series")
                +" · Descanso opcional: "+restSeconds+" segundos");
        nextButton.setEnabled(true);
        if(index>=exercises.size()) {
            addText(byRounds?"Bloque de ronda completado":"Bloque de ejercicios completado",22);
            addText("Puedes finalizar ahora. No se exige otra ronda, "
                    +"otra serie ni un número adicional de repeticiones.",14);
            if(byRounds) {
                Button another=addAction("Nueva ronda (opcional)",()->{
                    index=0;round++;extraSeries=0;save();render();
                });
                another.setContentDescription("Empezar otra ronda opcional");
            }
            nextButton.setText("Finalizar sesión");
            nextButton.setOnClickListener(v->{
                RoutineSessionStore.clear(this,token);
                if("daily".equals(kind))askEffortAfterDailyMission();
                else finish();
            });
            return;
        }
        ExerciseVariant current=exercises.get(index);
        addText("Ejercicio "+(index+1)+" de "+exercises.size()
                +(byRounds?" · Ronda "+round:" · Series registradas "+extraSeries),14);
        addText(current.name(),23);
        addText("Material: "+String.join(", ",current.equipment()).replace("_"," ")
                +" · Nivel técnico "+current.difficulty(),13);
        addText("Elige tu ritmo. Puedes parar, descansar o no terminar "
                +"si algo resulta incómodo.",13);
        if(!byRounds) {
            addAction("Registrar otra serie (opcional)",()->{
                extraSeries++;
                save();render();
            });
        }
        nextButton.setText(byRounds?"Registrar ejercicio y continuar"
                :"Terminé este ejercicio");
        nextButton.setOnClickListener(v->{
            if("daily".equals(kind)) {
                MissionTrackingStore.setDailyCompleted(this,planDate,current.id(),true);
            } else {
                MissionTrackingStore.markMonarch(this,planDate,current.id());
            }
            index++;
            extraSeries=0;
            save();
            render();
        });
    }

    private void save() {
        RoutineSessionStore.save(this,token,index,round,extraSeries);
    }

    private void startOrCancelRest() {
        if(restTimer!=null) {
            restTimer.cancel();restTimer=null;
            restButton.setText("Descanso opcional ("+restSeconds+" s)");
            return;
        }
        restTimer=new CountDownTimer(restSeconds*1000L,1000L) {
            @Override public void onTick(long milliseconds) {
                restButton.setText("Descanso: "+((milliseconds+999)/1000)+" s · Toca para omitir");
            }
            @Override public void onFinish() {
                restTimer=null;
                restButton.setText("Descanso terminado · Puedes continuar");
            }
        };
        restTimer.start();
    }

    /** Open the existing voluntary 1–10 category RPE after completion. */
    private void askEffortAfterDailyMission() {
        if(category<0||category>4) {finish();return;}
        String[] choices=new String[10];
        for(int i=0;i<10;i++)choices[i]=(i+1)+" / 10";
        final int[] selected={0};
        final AlertDialog[] open={null};
        AlertDialog dialog=new AlertDialog.Builder(this)
                .setTitle("Valoración de la misión diaria")
                .setMessage("¿Qué esfuerzo percibiste en esta categoría? "
                        +"Puedes omitir la valoración.")
                .setSingleChoiceItems(choices,-1,(d,index)->{
                    selected[0]=index+1;
                    if(open[0]!=null) {
                        open[0].getButton(AlertDialog.BUTTON_POSITIVE).setEnabled(true);
                    }
                })
                .setNegativeButton("Ahora no",(d,which)->finish())
                .setPositiveButton("Guardar valoración",(d,which)->{
                    if(selected[0]>=1 && selected[0]<=10)
                        MissionTrackingStore.rateCategory(
                                this,LocalDate.now(),category,selected[0]);
                    finish();
                })
                .setOnCancelListener(d->finish())
                .create();
        open[0]=dialog;
        dialog.setOnShowListener(d->dialog.getButton(
                AlertDialog.BUTTON_POSITIVE).setEnabled(false));
        dialog.show();
    }

    private void questionnaire() {
        if(exercises.isEmpty())return;
        String[] reasons={
                "Estoy muy agotado, sin lesión aparente",
                "Molestia o dolor leve en esta categoría",
                "Dolor articular fuerte, posible esguince o luxación",
                "Mareo muy intenso, vómitos o desmayo",
                "Quiero detenerme sin explicar el motivo"
        };
        new AlertDialog.Builder(this)
                .setTitle("Detener sesión por lesión o malestar")
                .setMessage("Selecciona la situación que mejor describa lo ocurrido. "
                        +"No es un diagnóstico médico.")
                .setItems(reasons,(dialog,which)->recordInterruption(which))
                .setNegativeButton("Volver",null).show();
    }

    private void recordInterruption(int choice) {
        SessionInterruptionPolicy.Assessment assessment=
                SessionInterruptionPolicy.assess(choice);
        int affected=effectiveCategory();
        if(affected<0 && !assessment.urgentMedicalAdvice) {
            // Unclassified movement: do not invent an affected body category.
            assessment=SessionInterruptionPolicy.assess(
                    SessionInterruptionPolicy.STOP_WITHOUT_REASON);
        }
        SessionSafetyStore.report(this,affected,LocalDate.now(),assessment);
        if(restTimer!=null){restTimer.cancel();restTimer=null;}
        nextButton.setEnabled(false);
        restButton.setEnabled(false);
        stopButton.setEnabled(false);
        new AlertDialog.Builder(this)
                .setTitle(assessment.urgentMedicalAdvice
                        ?"Sesión suspendida: solicita atención médica"
                        :"Sesión detenida")
                .setMessage(assessment.explanation
                        +(assessment.urgentMedicalAdvice
                        ?" Si hay desmayo, dificultad respiratoria, empeoramiento "
                                +"o síntomas intensos, solicita ayuda de emergencia.":""))
                .setCancelable(false)
                .setPositiveButton("Entendido",(dialog,which)->finish()).show();
    }

    private int effectiveCategory() {
        if("daily".equals(kind))return category;
        if(exercises.isEmpty())return -1;
        int position=Math.min(index,exercises.size()-1);
        return categoriesById.getOrDefault(exercises.get(position).id(),-1);
    }

    private static int codeToCategory(String taxonomyCode) {
        if("tren_superior".equals(taxonomyCode))return 0;
        if("tronco".equals(taxonomyCode))return 1;
        if("piernas".equals(taxonomyCode))return 2;
        if("movilidad".equals(taxonomyCode))return 3;
        if("cardio".equals(taxonomyCode))return 4;
        return -1; // Integrated and coordination do not map to weekly categories.
    }

    private void addText(String value,int size) {
        TextView view=new TextView(this);
        view.setText(value);
        view.setTextSize(size);
        view.setTypeface(Typeface.DEFAULT,Typeface.BOLD);
        view.setTextColor(light?0xFF111111:Color.WHITE);
        LinearLayout.LayoutParams lp=new LinearLayout.LayoutParams(-1,-2);
        lp.bottomMargin=dp(15);
        details.addView(view,lp);
    }

    private Button addAction(String label,Runnable action) {
        Button button=new Button(this);
        button.setText(label);
        button.setAllCaps(false);
        button.setTypeface(Typeface.DEFAULT,Typeface.BOLD);
        button.setOnClickListener(v->action.run());
        button.setBackground(outline());
        button.setTextColor(light?0xFF111111:Color.WHITE);
        LinearLayout.LayoutParams lp=new LinearLayout.LayoutParams(-1,dp(52));
        lp.bottomMargin=dp(10);
        details.addView(button,lp);
        return button;
    }

    private GradientDrawable outline() {
        GradientDrawable shape=new GradientDrawable();
        shape.setColor(light?Color.WHITE:0xFF090909);
        shape.setStroke(dp(1),light?0xFFB0B0B0:0xFF888888);
        shape.setCornerRadius(dp(10));
        return shape;
    }

    private void applyTheme(View view) {
        if(view instanceof TextView) {
            TextView text=(TextView)view;
            text.setTextColor(light?0xFF111111:Color.WHITE);
            text.setTypeface(Typeface.DEFAULT,Typeface.BOLD);
            if(view instanceof Button)text.setBackground(outline());
        }
        if(view instanceof ViewGroup) {
            ViewGroup group=(ViewGroup)view;
            for(int i=0;i<group.getChildCount();i++)applyTheme(group.getChildAt(i));
        }
        if(view.getId()==R.id.routine_root)
            view.setBackgroundColor(light?Color.WHITE:0xFF090909);
    }

    private int dp(int value) {return Math.round(value*density);}

    @Override protected void onDestroy() {
        closed=true;
        if(restTimer!=null){restTimer.cancel();restTimer=null;}
        worker.shutdownNow();
        super.onDestroy();
    }
}

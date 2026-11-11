package pe.asterionsystem.app;

import android.app.AlertDialog;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import androidx.activity.ComponentActivity;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import pe.asterionsystem.app.data.AssetExerciseCatalogRepository;
import pe.asterionsystem.core.engine.exercises.ExerciseCatalog;
import pe.asterionsystem.core.engine.exercises.ExerciseCategoryMapper;
import pe.asterionsystem.core.engine.exercises.ExerciseVariant;
import pe.asterionsystem.core.engine.missions.MissionPlanner;
import pe.asterionsystem.core.engine.missions.RoutineGenerator;

/**
 * Six offline reproducible previews, user-selected only; separate from
 * daily missions and Monarca. No forced targets, timed exertion or auto level-up.
 */
public final class RoutinesActivity extends ComponentActivity {
    private static final int[] MINUTES={10,15,20,30,45,60};
    private static final String[] LEVELS={"basico","intermedio","avanzado"};
    private static final String[] LEVEL_NAMES={"Básico","Intermedio","Avanzado"};
    private final ExecutorService worker=Executors.newSingleThreadExecutor();
    private ExerciseCatalog catalog;
    private MissionPlanner.Profile profile;
    private LinearLayout body;
    private boolean light,closed;
    private int categoryIndex=0,minutes=20,levelIndex=0,variation=0;
    private float density;

    @Override protected void onCreate(Bundle saved) {
        super.onCreate(saved);
        density=getResources().getDisplayMetrics().density;
        light="light".equals(ProfileStore.getRegistration(this).optString("theme","light"));
        if(saved!=null) {
            categoryIndex=Math.max(0,Math.min(4,saved.getInt("category",0)));
            int wanted=saved.getInt("minutes",20);
            for(int option:MINUTES)if(option==wanted)minutes=wanted;
            levelIndex=Math.max(0,Math.min(2,saved.getInt("level",0)));
            variation=Math.max(0,Math.min(999,saved.getInt("variation",0)));
        }
        LinearLayout root=column();
        root.setPadding(dp(18),dp(22),dp(18),dp(16));
        root.setBackgroundColor(background());
        TextView name=label("ASTERION · RUTINAS",22,true);
        root.addView(name);
        TextView explanation=label("Elige una categoría, tu tiempo disponible "
                +"y un nivel técnico. Se muestran hasta seis opciones; "
                +"tú decides si quieres comenzar.",13,false);
        root.addView(explanation);
        ScrollView scroller=new ScrollView(this);
        body=column();
        body.setPadding(0,dp(18),0,dp(16));
        scroller.addView(body);
        root.addView(scroller,new LinearLayout.LayoutParams(-1,0,1));
        Button back=button("Volver");
        back.setOnClickListener(v->finish());
        root.addView(back);
        setContentView(root);
        body.addView(label("Cargando catálogo de ejercicios…",15,true));
        worker.execute(()->{
            try {
                ExerciseCatalog fetched=new AssetExerciseCatalogRepository(this).load();
                MissionPlanner.Profile preferences=MissionProfileAdapter.fromRegistration(
                        ProfileStore.getRegistration(this));
                runOnUiThread(()->{
                    if(closed||isFinishing())return;
                    catalog=fetched;profile=preferences;render();
                });
            } catch(RuntimeException invalid) {
                runOnUiThread(()->{
                    if(closed||isFinishing())return;
                    body.removeAllViews();
                    body.addView(label("El catálogo no pudo cargarse. "
                            +"No se crearán opciones incompletas.",15,true));
                });
            }
        });
    }

    @Override protected void onResume() {
        super.onResume();
        if(catalog!=null) {
            profile=MissionProfileAdapter.fromRegistration(
                    ProfileStore.getRegistration(this));
            render();
        }
    }
    @Override protected void onSaveInstanceState(Bundle state) {
        state.putInt("category",categoryIndex);
        state.putInt("minutes",minutes);
        state.putInt("level",levelIndex);
        state.putInt("variation",variation);
        super.onSaveInstanceState(state);
    }

    private void render() {
        if(catalog==null||profile==null)return;
        body.removeAllViews();
        body.addView(label("CONFIGURACIÓN",16,true));
        choose("Categoría · "+ExerciseCategoryMapper.displayName(categoryIndex),
                ()->new AlertDialog.Builder(this)
                        .setTitle("Escoger una de cinco categorías")
                        .setSingleChoiceItems(new String[]{
                                "Tren superior","Tren central","Tren inferior",
                                "Movilidad","Cardio"},categoryIndex,(d,index)->{
                            categoryIndex=index;variation=0;d.dismiss();render();
                        }).setNegativeButton("Cancelar",null).show());
        choose("Tiempo disponible · "+minutes+" min",()->{
            String[] options={"10 minutos","15 minutos","20 minutos",
                    "30 minutos","45 minutos","60 minutos"};
            new AlertDialog.Builder(this).setTitle("Tiempo disponible (orientativo)")
                    .setSingleChoiceItems(options,indexOfMinutes(),(d,index)->{
                        minutes=MINUTES[index];variation=0;d.dismiss();render();
                    }).setNegativeButton("Cancelar",null).show();
        });
        choose("Nivel técnico · "+LEVEL_NAMES[levelIndex],()->
                new AlertDialog.Builder(this).setTitle("Seleccionar nivel")
                        .setMessage("Todos los niveles mantienen los filtros de "
                                +"seguridad. Avanzado no habilita movimientos "
                                +"de supervisión obligatoria.")
                        .setSingleChoiceItems(LEVEL_NAMES,levelIndex,(d,index)->{
                            levelIndex=index;variation=0;d.dismiss();render();
                        }).setNegativeButton("Cancelar",null).show());

        body.addView(label("XP de participación registrado: "
                +RoutineExperienceStore.total(this),13,false));
        if(RoutineExperienceStore.recorded(this,LocalDate.now())) {
            body.addView(label("La experiencia de hoy ya está registrada. "
                    +"Puedes descansar sin repetir ninguna actividad.",13,false));
        }
        if(SessionSafetyStore.globallyPaused(this)
                ||SessionSafetyStore.categoryPaused(this,categoryIndex)) {
            body.addView(label("La categoría o el entrenamiento está en pausa. "
                    +"No se propondrán nuevas sesiones.",14,true));
            return;
        }

        RoutineGenerator.Request request=new RoutineGenerator.Request(
                ExerciseCategoryMapper.weeklyCode(categoryIndex),
                minutes,LEVELS[levelIndex],LocalDate.now(),variation);
        List<RoutineGenerator.Offer> offers=new RoutineGenerator(catalog)
                .generate(request,profile);
        body.addView(label("OPCIONES PARA ELEGIR · "+offers.size(),17,true));
        body.addView(label("Selección estable al reabrir la aplicación. "
                +"Solo «Ver otras opciones» cambia el sorteo explícitamente. "
                +"El tiempo es una referencia, no una meta de rendimiento.",12,false));
        if(offers.isEmpty()) {
            body.addView(label("No hay suficientes movimientos seguros "
                    +"para esta combinación y tu material registrado. "
                    +"Prueba menos tiempo u otra categoría.",14,false));
        } else {
            int n=1;
            for(RoutineGenerator.Offer offer:offers) {
                final int number=n++;
                StringBuilder names=new StringBuilder();
                for(int i=0;i<Math.min(3,offer.exercises().size());i++) {
                    if(i>0)names.append(" · ");
                    names.append(offer.exercises().get(i).name());
                }
                body.addView(label("Rutina "+number+" · "+offer.exercises().size()
                        +" movimientos · "+offer.minutes()+" min disponibles",14,true));
                body.addView(label(names.toString(),12,false));
                choose("Revisar rutina "+number,()->preview(offer,number));
            }
        }
        if(variation<999)choose("Ver otras opciones (voluntario)",()->{
            variation++;render();
        });
        body.addView(label("Descansar, omitir una sesión o detenerse no "
                +"supone penalizaciones. Las sesiones no suben automáticamente "
                +"la dificultad ni las repeticiones.",12,false));
    }

    private int indexOfMinutes() {
        for(int i=0;i<MINUTES.length;i++)if(MINUTES[i]==minutes)return i;
        return 2;
    }

    private void preview(RoutineGenerator.Offer offer,int number) {
        StringBuilder steps=new StringBuilder();
        int i=1;
        for(ExerciseVariant variant:offer.exercises()) {
            steps.append(i++).append(". ").append(variant.name())
                    .append(" · Nivel ").append(variant.difficulty())
                    .append(" · ").append(String.join(", ",variant.equipment())
                            .replace('_',' ')).append("\n\n");
        }
        new AlertDialog.Builder(this)
                .setTitle("Rutina "+number+" · vista previa")
                .setMessage("Tiempo disponible: "+offer.minutes()+" minutos.\n"
                        +"Nivel: "+LEVEL_NAMES[levelIndex]+".\n\n"
                        +steps+"Es una selección opcional y no prescribe "
                        +"repeticiones o cargas. Descansa o termina cuando lo necesites.")
                .setNegativeButton("Volver",null)
                .setPositiveButton("Elegir y comenzar",(d,w)->start(offer))
                .show();
    }

    private void start(RoutineGenerator.Offer offer) {
        if(SessionSafetyStore.globallyPaused(this)
                ||SessionSafetyStore.categoryPaused(this,categoryIndex)) {
            render();return;
        }
        RoutineGenerator.Request request=new RoutineGenerator.Request(
                ExerciseCategoryMapper.weeklyCode(categoryIndex),
                minutes,LEVELS[levelIndex],LocalDate.now(),variation);
        boolean stillValid=false;
        for(RoutineGenerator.Offer latest:new RoutineGenerator(catalog)
                .generate(request,MissionProfileAdapter.fromRegistration(
                        ProfileStore.getRegistration(this)))) {
            if(latest.id().equals(offer.id())) {
                stillValid=true;break;
            }
        }
        if(!stillValid) {
            new AlertDialog.Builder(this).setTitle("Revisar rutina")
                    .setMessage("Cambió tu configuración. Revisa otra vez las opciones.")
                    .setPositiveButton("Aceptar",(d,w)->render()).show();
            return;
        }
        ArrayList<String> ids=new ArrayList<>();
        for(ExerciseVariant variant:offer.exercises())ids.add(variant.id());
        startActivity(new Intent(this,RoutineStepActivity.class)
                .putExtra("kind","custom")
                .putExtra("date",LocalDate.now().toString())
                .putExtra("category",categoryIndex)
                .putExtra("minutes",minutes)
                .putExtra("level",LEVELS[levelIndex])
                .putExtra("variation",variation)
                .putExtra("offer_id",offer.id())
                .putStringArrayListExtra("variants",ids));
    }

    private LinearLayout column() {
        LinearLayout col=new LinearLayout(this);
        col.setOrientation(LinearLayout.VERTICAL);
        return col;
    }
    private int background(){return light?Color.WHITE:0xFF080808;}
    private int foreground(){return light?0xFF171717:Color.WHITE;}
    private TextView label(String value,int size,boolean bold) {
        TextView label=new TextView(this);
        label.setText(value);label.setTextSize(size);
        label.setTextColor(foreground());
        if(bold)label.setTypeface(Typeface.DEFAULT,Typeface.BOLD);
        label.setPadding(0,dp(7),0,dp(7));
        return label;
    }
    private Button button(String value) {
        Button button=new Button(this);
        button.setText(value);button.setAllCaps(false);
        button.setMinHeight(dp(48));button.setTextColor(foreground());
        GradientDrawable shape=new GradientDrawable();
        shape.setColor(background());shape.setCornerRadius(dp(10));
        shape.setStroke(dp(1),light?0xFFBBBBBB:0xFF777777);
        button.setBackground(shape);
        return button;
    }
    private void choose(String value,Runnable action) {
        Button button=button(value);
        LinearLayout.LayoutParams params=new LinearLayout.LayoutParams(-1,dp(51));
        params.bottomMargin=dp(10);
        body.addView(button,params);
        button.setOnClickListener(v->action.run());
    }
    private int dp(int value){return Math.round(value*density);}
    @Override protected void onDestroy() {
        closed=true;worker.shutdownNow();super.onDestroy();
    }
}

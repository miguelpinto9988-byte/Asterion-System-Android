package pe.asterionsystem.app;

import android.app.AlertDialog;
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
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import pe.asterionsystem.app.data.AssetExerciseCatalogRepository;
import pe.asterionsystem.core.engine.exercises.ExerciseCatalog;
import pe.asterionsystem.core.engine.exercises.ExerciseFamily;
import pe.asterionsystem.core.engine.exercises.ExerciseType;
import pe.asterionsystem.core.engine.exercises.ExerciseVariant;
import pe.asterionsystem.core.engine.missions.MissionPlanner;

/**
 * Real read-only catalog + daily proposals + five-day opt-in Monarca preview.
 * Proposals contain no numeric workout quotas or unsupervised advanced
 * movements. This Activity does not mark tasks completed in legacy SQLite.
 */
public final class MissionsActivity extends ComponentActivity {
    private static final String SECTION_MONARCH="monarch";
    private static final String SECTION_DAILY="daily";
    private static final String SECTION_LIBRARY="library";

    private final ExecutorService worker=Executors.newSingleThreadExecutor();
    private String section=SECTION_DAILY;
    private ExerciseCatalog catalog;
    private MissionPlanner planner;
    private MissionPlanner.Profile profile;
    private LinearLayout content;
    private ScrollView scroll;
    private TextView header;
    private TextView loading;
    private boolean light;
    private boolean destroyed;
    private float density;

    @Override protected void onCreate(Bundle savedState) {
        super.onCreate(savedState);
        setContentView(R.layout.screen_missions);
        density=getResources().getDisplayMetrics().density;
        light="light".equals(ProfileStore.getRegistration(this).optString("theme","light"));
        section=getIntent().getStringExtra("section");
        if(!SECTION_LIBRARY.equals(section)&&!SECTION_MONARCH.equals(section)) {
            section=SECTION_DAILY;
        }

        content=findViewById(R.id.missions_content);
        scroll=findViewById(R.id.missions_scroll);
        header=findViewById(R.id.missions_header);
        loading=findViewById(R.id.missions_loading);
        findViewById(R.id.missions_back).setOnClickListener(v->finish());
        findViewById(R.id.missions_tab_daily).setOnClickListener(v->show(SECTION_DAILY));
        findViewById(R.id.missions_tab_monarch).setOnClickListener(v->show(SECTION_MONARCH));
        findViewById(R.id.missions_tab_library).setOnClickListener(v->show(SECTION_LIBRARY));

        themeTree(findViewById(R.id.missions_root));
        loading.setText("Leyendo 39 familias y 445 variantes locales…");
        worker.execute(()-> {
            try {
                ExerciseCatalog fetched=new AssetExerciseCatalogRepository(this).load();
                MissionPlanner engine=new MissionPlanner(fetched);
                MissionPlanner.Profile preferences=
                        MissionProfileAdapter.fromRegistration(ProfileStore.getRegistration(this));
                runOnUiThread(()->{
                    if(destroyed || isFinishing())return;
                    catalog=fetched;
                    planner=engine;
                    profile=preferences;
                    show(section);
                });
            } catch(RuntimeException failure) {
                runOnUiThread(()->{
                    if(destroyed || isFinishing())return;
                    loading.setText("La biblioteca local no pudo cargarse. "
                            +"No se crearán misiones sin datos comprobados.");
                });
            }
        });
    }

    @Override protected void onResume() {
        super.onResume();
        if(catalog!=null)show(section);
    }

    private void show(String destination) {
        section=destination;
        if(planner==null)return;
        content.removeAllViews();
        loading.setText("");
        if(SECTION_DAILY.equals(section))daily();
        if(SECTION_MONARCH.equals(section))monarch();
        if(SECTION_LIBRARY.equals(section))library();
        themeTree(content);
        scroll.post(()->scroll.scrollTo(0,0));
    }

    private void daily() {
        title("Misiones diarias");
        MissionPlanner.Proposal proposal=planner.daily(profile,LocalDate.now());
        paragraph("Fecha: "+proposal.start()+". Se renueva según la fecha local.");
        paragraph(proposal.explanation());
        if(proposal.empty()) {
            paragraph("No hay tareas propuestas para este día. "
                    +"El descanso y los datos incompletos se respetan.");
        } else {
            int index=1;
            for(ExerciseVariant entry:proposal.candidates()) {
                exerciseRow(index+++". "+entry.name(),
                        "Material: "+material(entry)+" · Nivel técnico "+entry.difficulty());
            }
        }
        paragraph("Estas son propuestas del catálogo. No se imponen "
                +"repeticiones, cargas o penalizaciones; "
                +"el registro de realización se mantiene en el módulo Seguimiento.");
    }

    private void monarch() {
        title("Misiones Monarca");
        MonarchSessionStore.Snapshot snapshot=MonarchSessionStore.current(
                this,LocalDate.now(),planner,profile,catalog);
        paragraph("Misión especial: cinco ejercicios en un período de cinco días. "
                +"La previsualización no significa activación.");
        if(MonarchSessionStore.ACTIVE.equals(snapshot.status)) {
            paragraph("Estado: ACTIVADA con autorización. "
                    +"Finaliza al comenzar el día "+snapshot.activeUntilExclusive()+".");
        } else if(MonarchSessionStore.DECLINED.equals(snapshot.status)) {
            paragraph("Estado: RECHAZADA. No está activa; se ofrecerá otra "
                    +"previsualización al terminar el ciclo de cinco días.");
        } else {
            paragraph("Estado: VISTA PREVIA, pendiente de tu decisión.");
        }
        paragraph("La lista se ha filtrado por el perfil y el equipamiento registrado. "
                +"No hay metas obligatorias de repeticiones ni dificultad progresiva automática.");
        int number=1;
        for(ExerciseVariant exercise:snapshot.exercises) {
            exerciseRow(number+++". "+exercise.name(),
                    "Material: "+material(exercise)+" · Nivel técnico "+exercise.difficulty());
        }
        if(snapshot.exercises.isEmpty()) {
            paragraph("No hay candidatos compatibles. Configura primero tus días "
                    +"y equipos disponibles en el perfil.");
        }
        if(snapshot.canAccept()) {
            action("Revisar y autorizar",()->authorize(snapshot));
            action("Rechazar esta propuesta",()->{
                MonarchSessionStore.decline(this);
                show(SECTION_MONARCH);
            });
        }
        if(MonarchSessionStore.ACTIVE.equals(snapshot.status)) {
            action("Abandonar sin penalización",()->new AlertDialog.Builder(this)
                    .setTitle("Finalizar misión Monarca")
                    .setMessage("Puedes abandonar cuando quieras sin penalizaciones. "
                            +"¿Deseas retirarla?")
                    .setPositiveButton("Abandonar",(dialog,which)->{
                        MonarchSessionStore.abandon(this);
                        show(SECTION_MONARCH);
                    }).setNegativeButton("Conservar",null).show());
        }
    }

    private void authorize(MonarchSessionStore.Snapshot reviewed) {
        if(reviewed.exercises.size()!=5)return;
        StringBuilder details=new StringBuilder();
        for(int i=0;i<reviewed.exercises.size();i++) {
            details.append(i+1).append(". ")
                    .append(reviewed.exercises.get(i).name()).append("\n");
        }
        new AlertDialog.Builder(this)
                .setTitle("Autorizar misión Monarca")
                .setMessage("Revisa los cinco ejercicios:\n\n"+details
                        +"\nSon propuestas sin cuotas obligatorias. "
                        +"Puedes detenerte o retirarte en cualquier momento. "
                        +"¿Deseas activar un ciclo de cinco días?")
                .setNegativeButton("Ahora no",null)
                .setPositiveButton("Aceptar y activar",(dialog,which)->{
                    boolean accepted=MonarchSessionStore.accept(
                            this,LocalDate.now(),reviewed);
                    if(!accepted)loading.setText("La propuesta cambió; vuelve a revisarla.");
                    show(SECTION_MONARCH);
                }).show();
    }

    private void library() {
        title("Biblioteca de ejercicios");
        paragraph("Catálogo real local · "+catalog.families().size()+" familias · "
                +catalog.types().size()+" tipos · "+catalog.variants().size()+" variantes.");
        paragraph("Explora familias y variantes. Las de nivel técnico avanzado "
                +"son informativas y no forman parte de las propuestas automáticas.");
        Map<String,List<ExerciseFamily>> byGroup=new HashMap<>();
        for(ExerciseFamily item:catalog.families()) {
            byGroup.computeIfAbsent(item.category(),ignored->new ArrayList<>()).add(item);
        }
        String[] keys={"piernas","tren_superior","tronco","coordinacion",
                "integrado","cardio","movilidad"};
        String[] names={"Piernas y cadera","Tren superior","Tronco y estabilidad",
                "Equilibrio y coordinación","Trabajo integrado","Cardio",
                "Movilidad, respiración y postura"};
        for(int i=0;i<keys.length;i++) {
            List<ExerciseFamily> families=byGroup.get(keys[i]);
            if(families==null)continue;
            paragraph(names[i]+" · "+families.size()+" familias");
            for(ExerciseFamily family:families) {
                action(family.name(),()->showFamily(family));
            }
        }
    }

    private void showFamily(ExerciseFamily family) {
        content.removeAllViews();
        title(family.name());
        paragraph("Tipos de movimiento en esta familia:");
        for(ExerciseType type:catalog.typesInFamily(family.id())) {
            action(type.name(),()->showType(type,family));
        }
        action("Volver a las familias",this::libraryFromDetail);
        themeTree(content);
        scroll.post(()->scroll.scrollTo(0,0));
    }

    private void showType(ExerciseType type, ExerciseFamily family) {
        content.removeAllViews();
        title(type.name());
        List<ExerciseVariant> variants=new ArrayList<>(catalog.variantsInType(type.id()));
        variants.sort(Comparator.comparingInt(ExerciseVariant::difficulty));
        for(ExerciseVariant v:variants) {
            exerciseRow(v.name(),"Material: "+material(v)+" · Nivel técnico "
                    +v.difficulty()
                    +(v.supervisionRecommended()?" · Requiere orientación":""));
        }
        action("Volver a "+family.name(),()->showFamily(family));
        themeTree(content);
        scroll.post(()->scroll.scrollTo(0,0));
    }

    private void libraryFromDetail() { show(SECTION_LIBRARY); }

    private String material(ExerciseVariant variant) {
        return String.join(", ",variant.equipment()).replace("_"," ");
    }

    private void title(String value) {
        TextView label=text(value,22,true);
        LinearLayout.LayoutParams lp=new LinearLayout.LayoutParams(-1,-2);
        lp.bottomMargin=dp(12);
        content.addView(label,lp);
    }

    private void paragraph(String value) {
        TextView label=text(value,13,false);
        LinearLayout.LayoutParams lp=new LinearLayout.LayoutParams(-1,-2);
        lp.bottomMargin=dp(12);
        content.addView(label,lp);
    }

    private void exerciseRow(String title,String subtitle) {
        LinearLayout box=new LinearLayout(this);
        box.setOrientation(LinearLayout.VERTICAL);
        box.setPadding(dp(14),dp(14),dp(14),dp(14));
        box.setBackground(boxDrawable());
        TextView name=text(title,15,true);
        TextView detail=text(subtitle,12,false);
        box.addView(name);
        box.addView(detail);
        LinearLayout.LayoutParams lp=new LinearLayout.LayoutParams(-1,-2);
        lp.bottomMargin=dp(9);
        content.addView(box,lp);
    }

    private void action(String label,Runnable run) {
        Button button=new Button(this);
        button.setText(label);
        button.setAllCaps(false);
        button.setTypeface(Typeface.DEFAULT,Typeface.BOLD);
        button.setMinHeight(dp(48));
        button.setOnClickListener(v->run.run());
        LinearLayout.LayoutParams lp=new LinearLayout.LayoutParams(-1,-2);
        lp.bottomMargin=dp(9);
        content.addView(button,lp);
    }

    private TextView text(String raw,int size,boolean emphatic) {
        TextView view=new TextView(this);
        view.setText(raw);
        view.setTextSize(size);
        view.setTypeface(Typeface.DEFAULT,Typeface.BOLD);
        view.setTextColor(light?Color.BLACK:Color.WHITE);
        return view;
    }

    private GradientDrawable boxDrawable() {
        GradientDrawable d=new GradientDrawable();
        d.setColor(light?Color.WHITE:0xFF080808);
        d.setCornerRadius(dp(10));
        d.setStroke(dp(1),light?0xFFBBBBBB:0xFF5C5C5C);
        return d;
    }

    private void themeTree(View root) {
        root.setBackgroundColor(light?Color.WHITE:0xFF080808);
        themeDescendants(root);
    }

    private void themeDescendants(View view) {
        if(view instanceof TextView) {
            TextView label=(TextView)view;
            label.setTextColor(light?Color.BLACK:Color.WHITE);
            label.setTypeface(Typeface.DEFAULT,Typeface.BOLD);
            if(view instanceof Button) {
                view.setBackground(boxDrawable());
                view.setMinimumHeight(dp(48));
            }
        }
        if(view instanceof android.view.ViewGroup) {
            android.view.ViewGroup group=(android.view.ViewGroup)view;
            for(int i=0;i<group.getChildCount();i++)themeDescendants(group.getChildAt(i));
        }
    }

    private int dp(int n){return Math.round(n*density);}

    @Override protected void onDestroy() {
        destroyed=true;
        worker.shutdownNow();
        super.onDestroy();
    }
}

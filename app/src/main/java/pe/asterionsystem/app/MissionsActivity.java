package pe.asterionsystem.app;

import android.app.AlertDialog;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
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
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import pe.asterionsystem.app.data.AssetExerciseCatalogRepository;
import pe.asterionsystem.core.engine.exercises.ExerciseCatalog;
import pe.asterionsystem.core.engine.exercises.ExerciseFamily;
import pe.asterionsystem.core.engine.exercises.ExerciseType;
import pe.asterionsystem.core.engine.exercises.ExerciseVariant;
import pe.asterionsystem.core.engine.missions.MissionPlanner;
import pe.asterionsystem.core.engine.progression.EffortReviewEngine;
import pe.asterionsystem.core.engine.progression.CategoryEffortPolicy;

/**
 * Real read-only catalog + daily proposals + five-day opt-in Monarca preview.
 * Proposals contain no numeric workout quotas or unsupervised advanced
 * movements. This Activity does not mark tasks completed in legacy SQLite.
 */
public final class MissionsActivity extends ComponentActivity {
    private static final String SECTION_MONARCH="monarch";
    private static final String SECTION_DAILY="daily";
    private static final String SECTION_LIBRARY="library";
    private static final String SECTION_PROGRESS="progress";

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
        if(!SECTION_LIBRARY.equals(section)&&!SECTION_MONARCH.equals(section)
                && !SECTION_PROGRESS.equals(section)) {
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
        if(SECTION_PROGRESS.equals(section))progress();
        themeTree(content);
        scroll.post(()->scroll.scrollTo(0,0));
    }

    private void daily() {
        title("Misiones diarias");
        LocalDate today=LocalDate.now();
        // The last rating belongs ONLY to the training category scheduled
        // for today. A lower-body report never changes upper-body suggestions.
        int category=profile.weekly.get(today.getDayOfWeek().getValue()-1);
        int previousRating=category==CategoryEffortPolicy.REST ? 0
                :MissionTrackingStore.latestCategoryRating(this,category,today);
        CategoryEffortPolicy.Decision decision=CategoryEffortPolicy.evaluate(
                category,previousRating,profile.dailyLimit);
        MissionPlanner.Profile dayProfile=decision.preferSimplerTechnique
                ||decision.suggestedActivities<profile.dailyLimit
                ?new MissionPlanner.Profile(
                    decision.preferSimplerTechnique?"principiante":profile.experience,
                    profile.goal,profile.weekly,profile.equipment,
                    Math.max(1,decision.suggestedActivities))
                :profile;
        List<String> previousVariants=category==CategoryEffortPolicy.REST
                ?java.util.Collections.emptyList()
                :MissionTrackingStore.previousCategoryVariants(this,category,today);
        MissionPlanner.Proposal proposal=planner.daily(
                dayProfile,today,decision,previousVariants);
        if(category!=CategoryEffortPolicy.REST && !proposal.empty()) {
            MissionTrackingStore.rememberCategoryProposal(
                    this,category,today,proposal.candidates());
        }
        paragraph("Fecha: "+proposal.start()+". Se renueva según el calendario local.");
        if(category!=CategoryEffortPolicy.REST) {
            paragraph("Categoría: "+CategoryEffortPolicy.CATEGORY_NAMES[category]
                    +". "+decision.explanation);
            if(decision.guidance==CategoryEffortPolicy.Guidance.REVIEW_OPTIONAL) {
                paragraph("Asterion seleccionó las variantes automáticamente. "
                        +"Si no existe una alternativa del mismo tipo y nivel técnico, "
                        +"mantiene la opción compatible anterior.");
            }
        }
        paragraph(proposal.explanation());
        if(proposal.empty()) {
            paragraph("Sin propuestas de ejercicio para hoy. Se respeta el descanso.");
            return;
        }
        Set<String> completed=MissionTrackingStore.completedToday(this,today);
        paragraph("Registradas: "+completed.stream().filter(
                id->proposal.candidates().stream().anyMatch(v->v.id().equals(id)))
                .count()+" de "+proposal.candidates().size()
                +". Puedes parar cuando lo necesites.");
        int index=1;
        for(ExerciseVariant entry:proposal.candidates()) {
            dailyExerciseRow(index++,entry,today,category,proposal);
        }
        int rating=MissionTrackingStore.ratingForCategory(this,today,category);
        if(rating!=0) paragraph("Valoración de esfuerzo guardada: "+rating+" / 10.");
        action(rating==0?"Finalizar la actividad y valorar esfuerzo"
                :"Revisar valoración de esfuerzo",()->requestEffort(today,category));
        paragraph("La valoración es voluntaria. No aumenta automáticamente "
                +"el volumen de ejercicio ni añade penalizaciones.");
    }

    private void dailyExerciseRow(int index,ExerciseVariant entry,LocalDate day,
                                  int category,MissionPlanner.Proposal proposal) {
        exerciseRow(index+". "+entry.name(),
                "Material: "+material(entry)+" · Complejidad técnica "+entry.difficulty());
        CheckBox done=new CheckBox(this);
        done.setText("Actividad realizada (opcional)");
        done.setTextSize(14);
        done.setTypeface(Typeface.DEFAULT,Typeface.BOLD);
        done.setTextColor(light?Color.BLACK:Color.WHITE);
        done.setButtonTintList(ColorStateList.valueOf(light?Color.BLACK:Color.WHITE));
        done.setMinHeight(dp(48));
        done.setChecked(MissionTrackingStore.completedToday(this,day).contains(entry.id()));
        done.setOnCheckedChangeListener((button,checked)->{
            MissionTrackingStore.setDailyCompleted(this,day,entry.id(),checked);
            if(checked && MissionTrackingStore.ratingForCategory(this,day,category)==0) {
                Set<String> all=MissionTrackingStore.completedToday(this,day);
                boolean finished=!proposal.empty();
                for(ExerciseVariant item:proposal.candidates()) {
                    if(!all.contains(item.id()))finished=false;
                }
                if(finished)requestEffort(day,category);
            }
        });
        content.addView(done,new LinearLayout.LayoutParams(-1,-2));
    }

    private void requestEffort(LocalDate day,int category) {
        String[] values=new String[10];
        for(int i=0;i<10;i++) values[i]=String.valueOf(i+1);
        final int[] selected={MissionTrackingStore.ratingForCategory(this,day,category)};
        AlertDialog.Builder builder=new AlertDialog.Builder(this)
                .setTitle("Esfuerzo: "+CategoryEffortPolicy.CATEGORY_NAMES[category])
                .setMessage("Valora de 1 (muy ligero) a 10 (muy exigente). "
                        +"Solo afectará a esta categoría. Puedes descansar o detenerte.")
                .setSingleChoiceItems(values,selected[0]-1,(dialog,which)->selected[0]=which+1)
                .setNegativeButton("Ahora no",null)
                .setPositiveButton("Guardar valoración",(dialog,which)->{
                    if(selected[0]>=1 && selected[0]<=10) {
                        MissionTrackingStore.rateCategory(this,day,category,selected[0]);
                        show(SECTION_DAILY);
                    } else {
                        loading.setText("Elige un número entre 1 y 10 si deseas registrar tu esfuerzo.");
                    }
                });
        builder.show();
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
            LocalDate accepted=snapshot.acceptedDate;
            Set<String> recorded=MissionTrackingStore.monarchCompleted(this,accepted);
            long done=snapshot.exercises.stream().filter(v->recorded.contains(v.id())).count();
            paragraph("Participaciones registradas: "+done+" de "+snapshot.exercises.size()
                    +". Se acumulan durante los cinco días; no se exige un número de repeticiones.");
            for(ExerciseVariant exercise:snapshot.exercises) {
                if(!recorded.contains(exercise.id())) {
                    action("Registrar participación: "+exercise.name(),()->{
                        MissionTrackingStore.markMonarch(this,accepted,exercise.id());
                        show(SECTION_MONARCH);
                    });
                }
            }
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

    private void progress() {
        title("Progreso y evolución");
        paragraph("Valoraciones voluntarias de las misiones diarias; "
                +"no se utiliza el peso o la apariencia para determinar el avance.");
        LocalDate today=LocalDate.now();
        LocalDate first=MissionTrackingStore.firstReport(this);
        EffortReviewEngine.Review review=EffortReviewEngine.review(
                first,today,MissionTrackingStore.ratings(this,today));
        if(review.dayInCycle==0) {
            paragraph("Todavía no hay valoraciones de esfuerzo. "
                    +"Al terminar una misión diaria puedes registrar tu percepción de 1 a 10.");
        } else {
            paragraph("Día "+review.dayInCycle+" de 21 del ciclo de observación.");
            paragraph("Revisiones previstas: días 7, 14 y 21. "
                    +(review.checkpoint>0?"Última revisión alcanzada: día "+review.checkpoint+"."
                    :"Próxima revisión: día 7."));
            if(review.observationCount>0) {
                paragraph("Registros recientes: "+review.observationCount
                        +" · Esfuerzo medio: "
                        +String.format(java.util.Locale.getDefault(),"%.1f",review.averageEffort)
                        +" / 10.");
            }
            paragraph(review.recommendation);
        }
        paragraph("Seguimiento independiente por categoría:");
        for(int category=0;category<5;category++) {
            int latest=MissionTrackingStore.latestCategoryRating(
                    this,category,today.plusDays(1));
            CategoryEffortPolicy.Decision state=CategoryEffortPolicy.evaluate(
                    category,latest,profile.dailyLimit);
            paragraph(CategoryEffortPolicy.CATEGORY_NAMES[category]+": "
                    +(latest==0?"Sin valoración":latest+" / 10")
                    +" · "+state.explanation);
        }
        paragraph("No se sube automáticamente la intensidad, "
                +"ni siquiera el día 21. Puedes ajustar el plan manualmente o descansar.");
        action("Ir a misiones diarias",()->show(SECTION_DAILY));
        action("Borrar estas valoraciones y marcas",()->new AlertDialog.Builder(this)
                .setTitle("Borrar seguimiento de misiones")
                .setMessage("Se eliminarán valoraciones y actividades marcadas de este módulo. "
                        +"No se alteran los registros históricos del sistema.")
                .setNegativeButton("Cancelar",null)
                .setPositiveButton("Borrar",(dialog,which)->{
                    MissionTrackingStore.clear(this);
                    show(SECTION_PROGRESS);
                }).show());
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

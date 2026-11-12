package pe.asterionsystem.app;

import android.app.AlertDialog;
import android.content.Intent;
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
import pe.asterionsystem.core.engine.exercises.ExerciseIntensity;
import pe.asterionsystem.core.engine.exercises.ExerciseCategoryMapper;
import pe.asterionsystem.core.engine.exercises.ExerciseFamily;
import pe.asterionsystem.core.engine.exercises.ExerciseType;
import pe.asterionsystem.core.engine.exercises.ExerciseVariant;
import pe.asterionsystem.core.engine.exercises.ExerciseMuscleFocus;
import pe.asterionsystem.core.engine.missions.MissionPlanner;
import pe.asterionsystem.core.engine.missions.MissionDifficultyMixPolicy;
import pe.asterionsystem.core.engine.progression.EffortReviewEngine;
import pe.asterionsystem.core.engine.progression.CategoryEffortPolicy;
import pe.asterionsystem.core.engine.progression.CoordinatedTrainingEngine;
import pe.asterionsystem.core.engine.progression.VolumeProgressionEngine;
import pe.asterionsystem.core.engine.progression.WeeklyCategoryChallengePolicy;

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
        SimpleBottomNav.attach(this,
                findViewById(R.id.missions_bottom_bar),
                SimpleBottomNav.HOME);
        findViewById(R.id.missions_tab_daily).setOnClickListener(v->show(SECTION_DAILY));
        findViewById(R.id.missions_tab_monarch).setOnClickListener(v->show(SECTION_MONARCH));
        findViewById(R.id.missions_tab_library).setOnClickListener(v->show(SECTION_LIBRARY));

        themeTree(findViewById(R.id.missions_root));
        loading.setText("Cargando biblioteca local de ejercicios…");
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
        // Profile might have been edited while this screen was in the back
        // stack. Date and profile configuration are the ONLY normal reasons
        // for a different same-day exercise selection.
        if(catalog!=null) {
            profile=MissionProfileAdapter.fromRegistration(
                    ProfileStore.getRegistration(this));
            light="light".equals(
                    ProfileStore.getRegistration(this).optString("theme","light"));
            show(section);
        }
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
        // Only the scheduled category can be adjusted by a session report.
        int category=profile.weekly.get(today.getDayOfWeek().getValue()-1);
        if(SessionSafetyStore.globallyPaused(this)) {
            showMedicalHold();
            return;
        }
        if(category!=CategoryEffortPolicy.REST
                && SessionSafetyStore.categoryPaused(this,category)) {
            paragraph("Esta categoría quedó en pausa por una molestia comunicada. "
                    +"No se propondrán ejercicios hasta que haya cesado.");
            action("Revisar la pausa de esta categoría",()->new AlertDialog.Builder(this)
                    .setTitle("¿Ha cesado la molestia?")
                    .setMessage("No continúes si persiste o empeora el dolor. "
                            +"Busca orientación médica si es necesario.")
                    .setNegativeButton("Seguir en pausa",null)
                    .setPositiveButton("Se ha resuelto",(d,w)->{
                        SessionSafetyStore.clearMinorCategoryPause(this,category);
                        show(SECTION_DAILY);
                    }).show());
            return;
        }
        boolean softer=category!=CategoryEffortPolicy.REST
                && SessionSafetyStore.lighterToday(this,category,today);
        int previousRating=category==CategoryEffortPolicy.REST ? 0
                :softer?9:MissionTrackingStore.latestCategoryRating(this,category,today);
        // Volume history is category-isolated, and only PRIOR days count.
        List<VolumeProgressionEngine.Observation> volumeHistory=
                category==CategoryEffortPolicy.REST
                ?java.util.Collections.emptyList()
                :VolumeProgressStore.previousForCategory(this,category,today);
        previousRating=Math.max(previousRating,
                VolumeProgressionEngine.latestCategoryEffort(category,today,volumeHistory));
        CategoryEffortPolicy.Decision decision=CategoryEffortPolicy.evaluate(
                category,previousRating,profile.dailyLimit);
        MissionPlanner.Profile dayProfile=decision.preferSimplerTechnique
                ||decision.suggestedActivities<profile.dailyLimit
                ?profile.adapted(
                    decision.preferSimplerTechnique?"principiante":profile.experience,
                    Math.max(1,decision.suggestedActivities))
                :profile;
        // Only today's IDs are pinned, never full generated mission records.
        // Returning to another panel, reopening the app and completing an
        // exercise do not redraw the rest of today's planned exercises.
        MissionTrackingStore.clearLegacyProposalSnapshots(this);
        WeeklyCategoryChallengePolicy.Review weekly=category==CategoryEffortPolicy.REST
                ?null:WeeklyCategoryChallengePolicy.evaluate(
                    category,MissionTrackingStore.firstReport(this),today,
                    MissionTrackingStore.recentCategoryObservations(this,catalog,category,today));
        // Phase starts at the first visit after selecting a new experience
        // level. Only completed low-effort rated sessions in this category
        // can move a phase after the initial seven calendar days.
        boolean recentlyInterrupted=category!=CategoryEffortPolicy.REST
                && SessionSafetyStore.recentInterruption(this,category,today,7);
        MissionLevelProgressStore.Snapshot levelState=
                MissionLevelProgressStore.observe(this,profile.experience,
                        category,today,weekly,recentlyInterrupted);
        List<String> pinned=DailyMissionStore.read(this,today,profile,catalog);
        MissionPlanner.Proposal proposal=null;
        if(!pinned.isEmpty()) {
            try {
                proposal=planner.restoreDaily(profile,today,pinned);
            } catch(IllegalArgumentException invalid) {
                DailyMissionStore.clear(this);
            }
        }
        if(proposal==null) {
            List<String> previousVariants=category==CategoryEffortPolicy.REST
                    ?java.util.Collections.emptyList()
                    :MissionTrackingStore.latestCompletedForCategory(
                            this,catalog,category,today);
            List<String> recentCompleted=category==CategoryEffortPolicy.REST
                    ?java.util.Collections.emptyList()
                    :MissionTrackingStore.recentCompletedForCategory(
                            this,catalog,category,today);
            // Coordinator delegates selection to planner.daily(...) and
            // feeds prior volume effort into the same category decision.
            proposal=new CoordinatedTrainingEngine(planner).daily(
                    profile,today,previousRating,previousVariants,recentCompleted,
                    weekly!=null && weekly.rotateEquivalentVariant(),
                    levelState.phase,volumeHistory,recentlyInterrupted).mission;
            if(category!=CategoryEffortPolicy.REST && !proposal.empty()) {
                DailyMissionStore.write(this,today,profile,catalog,
                        proposal.candidates());
            }
        }
        // Register the selected catalog variants at this category's next
        // 7/14/21 checkpoint. A new variant or technical level begins a fresh
        // numeric volume epoch, without deleting earlier completion records.
        List<VolumeProgressionEngine.Observation> numericHistory=
                java.util.Collections.emptyList();
        if(category!=CategoryEffortPolicy.REST && !proposal.empty()) {
            VolumeProgressStore.observeCheckpointSelection(
                    this,category,today,levelState.entered,
                    levelState.technicalReview.checkpoint,proposal.candidates());
            numericHistory=VolumeProgressStore.progressionHistoryForCategory(
                    this,category,today);
        }
        final MissionPlanner.Proposal dayProposal=proposal;
        paragraph("Fecha: "+dayProposal.start()
                +". Esta selección permanece igual durante el día, incluso "
                +"si cierras la aplicación o cambias de panel. Se renueva "
                +"mañana o al modificar la configuración del perfil.");
        if(category!=CategoryEffortPolicy.REST) {
            paragraph("Categoría: "+CategoryEffortPolicy.CATEGORY_NAMES[category]
                    +". "+decision.explanation);
            if(weekly!=null) {
                paragraph("Revisión automática semanal: "+weekly.explanation);
            }
            if(weekly!=null && weekly.rotateEquivalentVariant()) {
                paragraph("El motor buscó automáticamente una alternativa equivalente "
                        +"del catálogo. La progresión numérica se calcula de manera independiente.");
            }
        }
        paragraph(dayProposal.explanation());
        paragraph(MissionDifficultyMixPolicy.explanation(
                profile.experience,levelState.phase));
        paragraph("Punto de revisión técnica (7/14/21 días, por categoría): "
                +levelState.technicalReview.explanation);
        if(dayProposal.empty()) {
            paragraph("Sin propuestas de ejercicio para hoy. Se respeta el descanso.");
            return;
        }
        if(MissionDifficultyMixPolicy.mixed(profile.experience)) {
            long higher=dayProposal.candidates().stream()
                    .filter(variant->variant.difficulty()==3).count();
            paragraph("Mezcla técnica de hoy: "
                    +(dayProposal.candidates().size()-higher)
                    +" variantes sencillas (1-2) y "+higher
                    +" variantes intermedias (3). La proporción depende "
                    +"del material y las alternativas compatibles; no "
                    +"implica aumentar automáticamente el esfuerzo.");
        }
        Set<String> completed=MissionTrackingStore.completedToday(this,today);
        paragraph("Registradas: "+completed.stream().filter(
                id->dayProposal.candidates().stream().anyMatch(v->v.id().equals(id)))
                .count()+" de "+dayProposal.candidates().size()
                +". Puedes parar cuando lo necesites.");
        int index=1;
        for(ExerciseVariant entry:dayProposal.candidates()) {
            dailyExerciseRow(index++,entry,today,category,dayProposal);
            VolumeProgressionEngine.Suggestion volume=VolumeProgressionEngine.review(
                    entry,category,today,numericHistory,
                    recentlyInterrupted);
            if(volume.previousSets>0) {
                String comparison=volume.previousSets+" series × "
                        +volume.previousRepetitions+" repeticiones registradas";
                if(volume.offeredSets!=volume.previousSets
                        || volume.offeredRepetitions!=volume.previousRepetitions) {
                    comparison+="; siguiente referencia calculada: "
                            +volume.offeredSets+" series × "
                            +volume.offeredRepetitions+" repeticiones "
                            +"(no obligatoria).";
                } else comparison+="; se mantiene la referencia.";
                paragraph(comparison+" "+volume.explanation);
            } else if(VolumeProgressStore.restartedOn(
                    this,category,entry.id(),today)) {
                paragraph("Nuevo bloque técnico: volumen reiniciado para esta "
                        +"variante. Se define un punto de partida propio, "
                        +"sin trasladar las series o repeticiones acumuladas.");
            } else if(volume.status==VolumeProgressionEngine.Status.NO_HISTORY) {
                paragraph("Volumen inicial sin registrar para esta variante: "
                        +"no se heredan objetivos de otros ejercicios.");
            } else if(volume.status==VolumeProgressionEngine.Status.RECOVERY_PRIORITY) {
                paragraph(volume.explanation);
            }
        }
        action("Ver ventana de misión diaria",()->previewMissionPopup(
                "Misiones de hoy",dayProposal.candidates(),
                "Selección estable hasta el siguiente día. "
                +"Registrar o detener una actividad es voluntario."));
        action("Rutinas paso a paso",()->openRoutine(
                SECTION_DAILY,today,category,dayProposal.candidates()));
        int rating=MissionTrackingStore.ratingForCategory(this,today,category);
        if(rating!=0) paragraph("Valoración de esfuerzo guardada: "+rating+" / 10.");
        action(rating==0?"Finalizar la actividad y valorar esfuerzo"
                :"Revisar valoración de esfuerzo",()->requestEffort(today,category));
        paragraph("La valoración permite recalcular referencias del siguiente "
                +"entrenamiento. Ningún aumento es obligatorio ni penaliza su omisión.");
    }

    private void dailyExerciseRow(int index,ExerciseVariant entry,LocalDate day,
                                  int category,MissionPlanner.Proposal proposal) {
        exerciseRow(index+". "+entry.name(),
                "Foco orientativo: "+ExerciseMuscleFocus.forVariant(catalog,entry).description()
                +" · Material: "+material(entry)
                +" · Complejidad técnica "+entry.difficulty());
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
        if(SessionSafetyStore.globallyPaused(this)) {
            showMedicalHold();
            return;
        }
        MonarchSessionStore.Snapshot snapshot=MonarchSessionStore.current(
                this,LocalDate.now(),planner,profile,catalog);
        paragraph("Misión especial: cinco opciones para un ciclo de cinco días. "
                +"El sorteo se mantiene estable al reabrir la app y puede variar "
                +"entre ciclos, siempre dentro del equipo y perfil compatible. "
                +"La previsualización no significa activación.");
        if(MonarchSessionStore.ACTIVE.equals(snapshot.status)) {
            paragraph("Estado: ACTIVADA con autorización. "
                    +"Finaliza al comenzar el día "+snapshot.activeUntilExclusive()+".");
        } else if(MonarchSessionStore.REVIEW_REQUIRED.equals(snapshot.status)) {
            paragraph("Estado: PAUSADA para revisión. Cambió tu configuración "
                    +"o alguna variante dejó de ser compatible. La misión "
                    +"aceptada no se ha sustituido por otro sorteo.");
            action("Revisar una nueva propuesta con mi perfil actual",()->{
                MonarchSessionStore.restartAfterReview(this);
                show(SECTION_MONARCH);
            });
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
        if(!snapshot.exercises.isEmpty()) {
            action("Ver ventana de misión Monarca",()->previewMissionPopup(
                    "Misión Monarca · cinco días",snapshot.exercises,
                    "Esta ventana es solo informativa y nunca activa la misión. "
                    +"La autorización requiere una confirmación separada."));
        }
        if(snapshot.exercises.isEmpty()
                && !MonarchSessionStore.REVIEW_REQUIRED.equals(snapshot.status)) {
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
            action("Rutinas paso a paso",()->openRoutine(
                    SECTION_MONARCH,accepted,
                    profile.weekly.get(LocalDate.now().getDayOfWeek().getValue()-1),
                    snapshot.exercises));
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

    private void previewMissionPopup(String heading,List<ExerciseVariant> exercises,
                                     String note) {
        StringBuilder body=new StringBuilder();
        int index=1;
        for(ExerciseVariant exercise:exercises) {
            body.append(index++).append(". ").append(exercise.name())
                .append("\\nMaterial: ").append(material(exercise))
                .append(" · Complejidad ").append(exercise.difficulty())
                .append("\\n\\n");
        }
        new AlertDialog.Builder(this).setTitle(heading)
                .setMessage(body.toString()+note)
                .setPositiveButton("Cerrar",null).show();
    }

    private void openRoutine(String kind,LocalDate date,int category,
                             List<ExerciseVariant> selected) {
        if(SessionSafetyStore.globallyPaused(this)) {
            showMedicalHold();
            return;
        }
        if(selected==null || selected.isEmpty())return;
        ArrayList<String> ids=new ArrayList<>();
        for(ExerciseVariant entry:selected)ids.add(entry.id());
        Intent guided=new Intent(this,RoutineStepActivity.class);
        guided.putExtra("kind",kind);
        guided.putExtra("date",date.toString());
        guided.putExtra("category",category);
        guided.putStringArrayListExtra("variants",ids);
        startActivity(guided);
    }

    private void showMedicalHold() {
        paragraph("Sesiones suspendidas por síntomas importantes.");
        paragraph(SessionSafetyStore.globalReason(this));
        paragraph("No se reanudan automáticamente al día siguiente. "
                +"Ante síntomas graves solicita asistencia médica urgente.");
        action("Ya recibí evaluación médica",()->new AlertDialog.Builder(this)
                .setTitle("Confirmar evaluación médica")
                .setMessage("Esta opción no sustituye una evaluación profesional. "
                        +"Confirma únicamente si realmente has recibido una evaluación "
                        +"y sigues las indicaciones para reanudar actividades.")
                .setNegativeButton("Conservar pausa",null)
                .setPositiveButton("Confirmar evaluación",(d,w)->{
                    SessionSafetyStore.acknowledgeProfessionalEvaluation(this);
                    show(section);
                }).show());
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
                    // Re-check the stored proposal and current profile before
                    // accepting a dialog opened before a profile/day change.
                    MissionPlanner.Profile currentProfile=
                            MissionProfileAdapter.fromRegistration(
                                    ProfileStore.getRegistration(this));
                    MonarchSessionStore.Snapshot latest=MonarchSessionStore.current(
                            this,LocalDate.now(),planner,currentProfile,catalog);
                    boolean unchanged=latest.canAccept()
                            && sameMonarchChoices(latest.exercises,reviewed.exercises);
                    boolean accepted=unchanged && MonarchSessionStore.accept(
                            this,LocalDate.now(),reviewed,currentProfile);
                    if(!accepted)loading.setText("La propuesta cambió; vuelve a revisarla.");
                    show(SECTION_MONARCH);
                }).show();
    }

    private boolean sameMonarchChoices(List<ExerciseVariant> a,
                                       List<ExerciseVariant> b) {
        if(a.size()!=b.size())return false;
        for(int i=0;i<a.size();i++) {
            if(!a.get(i).id().equals(b.get(i).id()))return false;
        }
        return true;
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
            MissionTrackingStore.DiversitySummary diversity=
                    MissionTrackingStore.recentDiversityForCategory(
                            this,catalog,category,today);
            paragraph("Variedad observada (14 días): "
                    +diversity.uniqueVariants+" variantes distintas, "
                    +diversity.focusGroups+" focos funcionales, "
                    +diversity.activeDays+" días registrados. "
                    +"Solo cuenta actividades marcadas como realizadas.");
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
        paragraph("Explora familias y variantes por intensidad orientativa: "
                +"baja, moderada, alta, variable o sin clasificar. "
                +"Las opciones de intensidad alta y nivel técnico avanzado "
                +"son informativas y no forman parte de las propuestas automáticas.");
        action("Buscar ejercicios por deporte",this::chooseSport);
        action("Buscar ejercicios por material",this::chooseEquipment);
        for(int i=0;i<5;i++) {
            String code=ExerciseCategoryMapper.weeklyCode(i);
            List<ExerciseFamily> families=catalog.categoryIndex().families(code);
            paragraph(ExerciseCategoryMapper.displayName(i)+" · "
                    +families.size()+" familias · "
                    +catalog.categoryIndex().types(code).size()+" tipos · "
                    +catalog.categoryIndex().variants(code).size()+" variantes");
            for(ExerciseFamily family:families) {
                action(family.name(),()->showFamily(family));
            }
        }
    }

    /** A discipline is a tag on exercise types; every result retains its one category. */
    private void chooseSport() {
        List<String> codes=catalog.sportIndex().sports();
        String[] names=new String[codes.size()];
        for(int i=0;i<codes.size();i++)names[i]=catalog.sportIndex().sportName(codes.get(i));
        new AlertDialog.Builder(this).setTitle("Explorar por deporte")
                .setItems(names,(dialog,which)->showSportLibrary(codes.get(which)))
                .setNegativeButton("Cancelar",null).show();
    }

    private void showSportLibrary(String sportCode) {
        content.removeAllViews();
        title("Deporte: "+catalog.sportIndex().sportName(sportCode));
        paragraph("Deporte como etiqueta secundaria; la categoría física se mantiene.");
        int count=0;
        for(int i=0;i<5;i++) {
            String category=ExerciseCategoryMapper.weeklyCode(i);
            List<ExerciseType> matches=new ArrayList<>();
            for(ExerciseType type:catalog.categoryIndex().types(category))
                if(catalog.sportIndex().includes(type.id(),sportCode))matches.add(type);
            if(matches.isEmpty())continue;
            paragraph(ExerciseCategoryMapper.displayName(i)+" · "+matches.size()+" tipos");
            for(ExerciseType type:matches)
                action(type.name(),()->showType(type,familyForType(type)));
            count+=matches.size();
        }
        if(count==0)paragraph("Sin movimientos registrados para este deporte.");
        action("Volver al catálogo",this::libraryFromDetail);
        themeTree(content);
        scroll.post(()->scroll.scrollTo(0,0));
    }

    private void chooseEquipment() {
        java.util.Set<String> materials=new java.util.TreeSet<>();
        for(ExerciseVariant variant:catalog.variants())materials.addAll(variant.equipment());
        List<String> codes=new ArrayList<>(materials);
        String[] labels=new String[codes.size()];
        for(int i=0;i<codes.size();i++)labels[i]=codes.get(i).replace('_',' ');
        new AlertDialog.Builder(this).setTitle("Filtrar por material")
                .setItems(labels,(dialog,which)->showEquipmentLibrary(codes.get(which)))
                .setNegativeButton("Cancelar",null).show();
    }

    private void showEquipmentLibrary(String materialCode) {
        content.removeAllViews();
        title("Material: "+materialCode.replace('_',' '));
        paragraph("Solo se muestran tipos que tienen al menos una variante con este material.");
        int count=0;
        for(int i=0;i<5;i++) {
            String category=ExerciseCategoryMapper.weeklyCode(i);
            List<ExerciseType> matches=new ArrayList<>();
            for(ExerciseType type:catalog.categoryIndex().types(category)) {
                boolean found=false;
                for(ExerciseVariant variant:catalog.variantsInType(type.id()))
                    if(variant.equipment().contains(materialCode)){found=true;break;}
                if(found)matches.add(type);
            }
            if(matches.isEmpty())continue;
            paragraph(ExerciseCategoryMapper.displayName(i)+" · "+matches.size()+" tipos");
            for(ExerciseType type:matches)
                action(type.name(),()->showType(type,familyForType(type)));
            count+=matches.size();
        }
        if(count==0)paragraph("No existen ejercicios para este material.");
        action("Volver al catálogo",this::libraryFromDetail);
        themeTree(content);
        scroll.post(()->scroll.scrollTo(0,0));
    }

    private ExerciseFamily familyForType(ExerciseType type) {
        for(ExerciseFamily family:catalog.families())
            if(family.id().equals(type.familyId()))return family;
        throw new IllegalArgumentException("Familia inexistente para "+type.id());
    }

    private void showFamily(ExerciseFamily family) {
        content.removeAllViews();
        title(family.name());
        paragraph("Categoría: "+ExerciseCategoryMapper.displayName(
                ExerciseCategoryMapper.weeklyIndex(family.category()))
                +" · Patrón: "+family.movementPattern().replace('_',' '));
        paragraph("Tipos de movimiento en esta familia:");
        for(ExerciseType type:catalog.typesInFamily(family.id())) {
            action(type.name(),()->showType(type,family));
        }
        action("Volver a las familias",this::libraryFromDetail);
        themeTree(content);
        scroll.post(()->scroll.scrollTo(0,0));
    }

    private void showType(ExerciseType type, ExerciseFamily family) {
        showTypeFiltered(type,family,null);
    }

    /**
     * Descriptive intensity is separate from technical difficulty. Filtering
     * high-intensity items does not activate them as daily mission candidates.
     */
    private void showTypeFiltered(ExerciseType type,ExerciseFamily family,
                                  ExerciseIntensity intensityFilter) {
        content.removeAllViews();
        title(type.name());
        List<String> disciplines=catalog.sportIndex().namesForType(type.id());
        if(!disciplines.isEmpty()) {
            paragraph("Deportes relacionados: "+String.join(", ",disciplines));
            paragraph("Objetivo del movimiento: "+catalog.sportIndex().goal(type.id()));
            paragraph("Referencia: "+catalog.sportIndex().sourceOrganization(type.id())
                    +" · "+catalog.sportIndex().sourceUrl(type.id()));
            if(catalog.sportIndex().manualOnly(type.id()))
                paragraph("Solo como referencia. No se asigna a misiones automáticas.");
        }
        paragraph("Intensidad orientativa: "+(intensityFilter==null
                ?"Todas":intensityFilter.label())
                +". Depende de la persona y de cómo se realice la actividad.");
        action("Filtrar por intensidad",()->{
            final String[] labels={"Todas","Baja","Moderada","Alta",
                    "Variable","Sin clasificar"};
            final ExerciseIntensity[] tags={null,ExerciseIntensity.LOW,
                    ExerciseIntensity.MODERATE,ExerciseIntensity.HIGH,
                    ExerciseIntensity.VARIABLE,ExerciseIntensity.UNCLASSIFIED};
            new AlertDialog.Builder(this).setTitle("Intensidad orientativa")
                    .setItems(labels,(dialog,which)->showTypeFiltered(
                            type,family,tags[which])).show();
        });
        List<ExerciseVariant> variants=new ArrayList<>(catalog.variantsInType(type.id()));
        variants.sort(Comparator.comparingInt(ExerciseVariant::difficulty));
        int shown=0;
        for(ExerciseVariant v:variants) {
            ExerciseIntensity tag=catalog.intensity(v.id());
            if(intensityFilter!=null && tag!=intensityFilter)continue;
            exerciseRow(v.name(),"Intensidad: "+tag.label()
                    +" · Material: "+material(v)+" · Nivel técnico "+v.difficulty()
                    +(v.supervisionRecommended()?" · Requiere orientación":"")
                    +(catalog.sportIndex().manualOnly(type.id())
                        ?" · Solo para revisión técnica":"")
                    +(tag==ExerciseIntensity.HIGH
                        ?" · Solo información; no se asigna automáticamente":""));
            shown++;
        }
        if(shown==0)paragraph("No hay variantes de este tipo con el filtro elegido.");
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

package pe.asterionsystem.app;

import android.app.Activity;
import android.view.View;
import android.widget.CheckBox;
import android.widget.LinearLayout;
import android.widget.TextView;
import java.time.LocalDate;
import java.util.List;
import java.util.Set;
import pe.asterionsystem.app.data.AssetExerciseCatalogRepository;
import pe.asterionsystem.core.engine.exercises.ExerciseCatalog;
import pe.asterionsystem.core.engine.exercises.ExerciseCategoryMapper;
import pe.asterionsystem.core.engine.exercises.ExerciseVariant;
import pe.asterionsystem.core.engine.missions.MissionPlanner;

/** Read today's pinned IDs. Selection remains exclusively in the daily mission engine. */
public final class HomeOverview {
    private HomeOverview(){}
    public static void bind(Activity activity,View root){
        LocalDate today=LocalDate.now();
        MissionPlanner.Profile profile=MissionProfileAdapter.fromRegistration(ProfileStore.getRegistration(activity));
        int category=profile.weekly.get(today.getDayOfWeek().getValue()-1);
        TextView summary=root.findViewById(R.id.home_daily_summary);
        summary.setText(category==5?"Hoy toca descanso":ExerciseCategoryMapper.displayName(category)+" · Según tu configuración");
        int active=0;LocalDate monday=today.minusDays(today.getDayOfWeek().getValue()-1);
        for(int i=0;i<7;i++)if(!MissionTrackingStore.completedToday(activity,monday.plusDays(i)).isEmpty()
                ||RoutineExperienceStore.recorded(activity,monday.plusDays(i)))active++;
        root.<TextView>findViewById(R.id.home_progress_summary).setText(active+" días activos esta semana");
        if(category==5||SessionSafetyStore.globallyPaused(activity)||SessionSafetyStore.categoryPaused(activity,category))return;
        new Thread(()->{
            try{
                ExerciseCatalog catalog=new AssetExerciseCatalogRepository(activity.getApplicationContext()).load();
                List<String> ids=DailyMissionStore.read(activity,today,profile,catalog);
                if(ids.isEmpty())return;
                MissionPlanner.Proposal verified=new MissionPlanner(catalog).restoreDaily(profile,today,ids);
                activity.runOnUiThread(()->{
                    if(activity.isDestroyed()||activity.isFinishing()||!root.isAttachedToWindow())return;
                    if(!today.equals(LocalDate.now())||!profile.missionSignature().equals(
                            MissionProfileAdapter.fromRegistration(ProfileStore.getRegistration(activity)).missionSignature()))return;
                    LinearLayout entries=root.findViewById(R.id.home_daily_entries);entries.removeAllViews();
                    Set<String> completed=MissionTrackingStore.completedToday(activity,today);
                    long done=verified.candidates().stream().filter(v->completed.contains(v.id())).count();
                    summary.setText(done+" de "+ids.size()+" completadas · "+ExerciseCategoryMapper.displayName(category));
                    for(ExerciseVariant exercise:verified.candidates().subList(0,Math.min(3,ids.size()))){
                        CheckBox check=new CheckBox(activity);check.setText(exercise.name());check.setTextSize(13);
                        check.setChecked(completed.contains(exercise.id()));entries.addView(check,new LinearLayout.LayoutParams(-1,-2));
                        check.setOnCheckedChangeListener((b,checked)->{
                            if(!today.equals(LocalDate.now())){bind(activity,root);return;}
                            MissionTrackingStore.setDailyCompleted(activity,today,exercise.id(),checked);
                            Set<String> current=MissionTrackingStore.completedToday(activity,today);
                            long count=verified.candidates().stream().filter(v->current.contains(v.id())).count();
                            summary.setText(count+" de "+ids.size()+" completadas · "+ExerciseCategoryMapper.displayName(category));
                            root.findViewById(R.id.home_week_progress).invalidate();
                            if(checked&&count==ids.size()&&MissionTrackingStore.ratingForCategory(activity,today,category)==0){
                                String[] values={"1 · Muy ligero","2","3","4","5 · Moderado","6","7","8","9","10 · Muy exigente"};
                                new AsterionDialog(activity).setTitle("¿Cómo sentiste la sesión?")
                                        .setItems(values,(d,which)->MissionTrackingStore.rateCategory(activity,today,category,which+1))
                                        .setNegativeButton("Ahora no",null).show();
                            }
                        });
                    }
                    AppTheme.apply(entries,AppTheme.isLight(activity));
                });
            }catch(RuntimeException unavailable){/* The daily destination remains the source of generation and recovery. */}
        },"asterion-home-summary").start();
    }
}

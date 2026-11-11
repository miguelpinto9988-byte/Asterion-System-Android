package pe.asterionsystem.app;

import android.app.AlertDialog;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.text.InputType;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import androidx.activity.ComponentActivity;
import java.time.DateTimeException;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import pe.asterionsystem.core.engine.exercises.ExerciseCategoryMapper;
import pe.asterionsystem.core.engine.missions.MonthCalendar;
import pe.asterionsystem.core.engine.missions.MissionPlanner;

/**
 * Functional offline month calendar: previous/next months and years, exact
 * Gregorian leap years, day selection and read-only history markers.
 * Dates are not exercise quotas and never cause automatic mission assignment.
 */
public final class CalendarActivity extends ComponentActivity {
    private static final DateTimeFormatter MONTH_NAME=DateTimeFormatter
            .ofPattern("LLLL yyyy",Locale.forLanguageTag("es-PE"));
    private YearMonth month;
    private LocalDate selected;
    private LinearLayout root;
    private float density;
    private boolean light;

    @Override protected void onCreate(Bundle saved) {
        super.onCreate(saved);
        density=getResources().getDisplayMetrics().density;
        light="light".equals(ProfileStore.getRegistration(this).optString("theme","light"));
        month=YearMonth.now();selected=LocalDate.now();
        if(saved!=null) {
            try {
                month=YearMonth.parse(saved.getString("month",month.toString()));
                selected=LocalDate.parse(saved.getString("selected",selected.toString()));
            }catch(RuntimeException ignore) {month=YearMonth.now();selected=LocalDate.now();}
        }
        root=new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setPadding(dp(12),dp(20),dp(12),dp(12));
        root.setBackgroundColor(background());
        setContentView(root);
        render();
    }

    @Override protected void onSaveInstanceState(Bundle state) {
        state.putString("month",month.toString());
        state.putString("selected",selected.toString());
        super.onSaveInstanceState(state);
    }

    private void render() {
        root.removeAllViews();
        root.addView(label("ASTERION · CALENDARIO",22,true));
        root.addView(label("Navega por meses y años. Los símbolos de registro "
                +"son informativos; no generan entrenamientos ni obligaciones.",12,false));
        LinearLayout actions=horizontal();
        addNav(actions,"◀ Mes",()->shift(-1));
        addNav(actions,"Mes ▶",()->shift(1));
        root.addView(actions);
        LinearLayout years=horizontal();
        addNav(years,"◀ Año",()->shift(-12));
        addNav(years,"Año ▶",()->shift(12));
        addNav(years,"Elegir año",this::chooseYear);
        root.addView(years);
        Button monthHeading=button(month.format(MONTH_NAME).toUpperCase(
                Locale.forLanguageTag("es-PE")));
        monthHeading.setOnClickListener(v->chooseYear());
        root.addView(monthHeading,new LinearLayout.LayoutParams(-1,dp(48)));
        LinearLayout weekdays=horizontal();
        for(String day:new String[]{"L","M","X","J","V","S","D"}) {
            TextView label=label(day,13,true);
            label.setGravity(android.view.Gravity.CENTER);
            weekdays.addView(label,new LinearLayout.LayoutParams(0,dp(38),1));
        }
        root.addView(weekdays);
        List<LocalDate> slots=MonthCalendar.days(month);
        for(int row=0;row<6;row++) {
            LinearLayout week=horizontal();
            for(int column=0;column<7;column++) {
                LocalDate date=slots.get(row*7+column);
                if(date==null) {
                    TextView empty=label(" ",12,false);
                    week.addView(empty,new LinearLayout.LayoutParams(0,dp(50),1));
                    continue;
                }
                Set<String> done=MissionTrackingStore.completedToday(this,date);
                boolean completed=!done.isEmpty();
                boolean routine=RoutineExperienceStore.recorded(this,date);
                String display=date.getDayOfMonth()+(completed||routine?" •":"");
                Button button=button(display);
                button.setTextSize(12);
                button.setContentDescription(date.toString()
                        +(completed?", con misiones registradas":"")
                        +(routine?", con rutina registrada":""));
                if(date.equals(selected))button.setTypeface(Typeface.DEFAULT,Typeface.BOLD);
                if(date.equals(LocalDate.now()))button.setText("● "+display);
                button.setOnClickListener(v->{selected=date;render();});
                week.addView(button,new LinearLayout.LayoutParams(0,dp(50),1));
            }
            root.addView(week);
        }
        ScrollView scroller=new ScrollView(this);
        LinearLayout info=new LinearLayout(this);
        info.setOrientation(LinearLayout.VERTICAL);
        info.setPadding(dp(6),dp(14),dp(6),dp(8));
        MissionPlanner.Profile profile=MissionProfileAdapter.fromRegistration(
                ProfileStore.getRegistration(this));
        int category=profile.weekly.get(selected.getDayOfWeek().getValue()-1);
        info.addView(label("Día seleccionado: "+selected,16,true));
        info.addView(label("Plan semanal: "+(category==5?"Descanso":
                ExerciseCategoryMapper.displayName(category)),13,false));
        int count=MissionTrackingStore.completedToday(this,selected).size();
        info.addView(label("Misiones marcadas como realizadas: "+count,13,false));
        info.addView(label("Rutina registrada: "
                +(RoutineExperienceStore.recorded(this,selected)?"Sí":"No"),13,false));
        info.addView(label("El calendario es consultivo. No activa Monarca, "
                +"no modifica días anteriores y no genera metas para fechas futuras.",12,false));
        scroller.addView(info);
        root.addView(scroller,new LinearLayout.LayoutParams(-1,0,1));
        Button today=button("Ir a hoy");
        today.setOnClickListener(v->{selected=LocalDate.now();month=YearMonth.from(selected);render();});
        root.addView(today,new LinearLayout.LayoutParams(-1,dp(48)));
        root.addView(SimpleBottomNav.create(this,SimpleBottomNav.CALENDAR),
                new LinearLayout.LayoutParams(-1,dp(62)));
    }

    private void shift(int delta) {
        try {month=MonthCalendar.shift(month,delta);render();}
        catch(DateTimeException boundary) {
            new AlertDialog.Builder(this).setTitle("Límite de fecha")
                    .setMessage("Se alcanzó el límite de años admitido por el calendario del dispositivo.")
                    .setPositiveButton("Aceptar",null).show();
        }
    }

    private void chooseYear() {
        EditText input=new EditText(this);
        input.setSingleLine(true);
        input.setInputType(InputType.TYPE_CLASS_NUMBER|InputType.TYPE_NUMBER_FLAG_SIGNED);
        input.setText(String.valueOf(month.getYear()));
        new AlertDialog.Builder(this)
                .setTitle("Elegir año")
                .setMessage("Escribe un año para ir directamente a él, sin desplazarte mes a mes.")
                .setView(input)
                .setNegativeButton("Cancelar",null)
                .setPositiveButton("Ir al año",(d,w)->{
                    try {
                        int year=Integer.parseInt(input.getText().toString().trim());
                        month=MonthCalendar.chooseYear(month,year);
                        render();
                    }catch(RuntimeException invalid) {
                        new AlertDialog.Builder(this)
                                .setMessage("El año está fuera del rango admitido.")
                                .setPositiveButton("Aceptar",null).show();
                    }
                }).show();
    }

    private LinearLayout horizontal() {
        LinearLayout row=new LinearLayout(this);
        row.setOrientation(LinearLayout.HORIZONTAL);
        return row;
    }
    private void addNav(LinearLayout row,String title,Runnable action) {
        Button button=button(title);
        button.setTextSize(12);
        row.addView(button,new LinearLayout.LayoutParams(0,dp(46),1));
        button.setOnClickListener(v->action.run());
    }
    private int background(){return light?Color.WHITE:0xFF080808;}
    private int foreground(){return light?Color.BLACK:Color.WHITE;}
    private TextView label(String text,int size,boolean bold) {
        TextView view=new TextView(this);
        view.setText(text);view.setTextColor(foreground());view.setTextSize(size);
        if(bold)view.setTypeface(Typeface.DEFAULT,Typeface.BOLD);
        view.setPadding(dp(3),dp(7),dp(3),dp(7));
        return view;
    }
    private Button button(String text) {
        Button button=new Button(this);
        button.setText(text);button.setAllCaps(false);
        button.setTextColor(foreground());
        button.setMinHeight(dp(44));
        GradientDrawable shape=new GradientDrawable();
        shape.setColor(background());shape.setCornerRadius(dp(7));
        shape.setStroke(dp(1),light?0xFFB0B0B0:0xFF666666);
        button.setBackground(shape);
        return button;
    }
    private int dp(int value){return Math.round(value*density);}
}

package pe.asterionsystem.app;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.util.AttributeSet;
import android.view.View;
import java.time.LocalDate;

/** Seven real local activity totals; never fills the dashboard with demonstration data. */
public final class WeekProgressView extends View {
    private final Paint paint=new Paint(Paint.ANTI_ALIAS_FLAG);
    public WeekProgressView(Context c,AttributeSet attrs){super(c,attrs);setImportantForAccessibility(IMPORTANT_FOR_ACCESSIBILITY_YES);}
    @Override protected void onDraw(Canvas canvas){
        super.onDraw(canvas);
        boolean light=AppTheme.isLight(getContext());
        LocalDate today=LocalDate.now(),monday=today.minusDays(today.getDayOfWeek().getValue()-1);
        float cell=getWidth()/7f,d=getResources().getDisplayMetrics().density;
        int[] counts=new int[7];int max=1;
        StringBuilder description=new StringBuilder("Actividad semanal: ");
        for(int i=0;i<7;i++){
            LocalDate date=monday.plusDays(i);
            counts[i]=MissionTrackingStore.completedToday(getContext(),date).size()
                    +(RoutineExperienceStore.recorded(getContext(),date)?1:0);
            max=Math.max(max,counts[i]);
            description.append(date).append(": ").append(counts[i]).append(" registros. ");
        }
        setContentDescription(description);
        paint.setTypeface(Typeface.create("sans-serif",Typeface.BOLD));paint.setTextSize(11*d);paint.setTextAlign(Paint.Align.CENTER);
        String[] days={"L","M","X","J","V","S","D"};
        for(int i=0;i<7;i++){
            float x=cell*(i+.5f),base=getHeight()-20*d;
            float height=counts[i]==0?5*d:Math.max(10*d,(base-4*d)*counts[i]/max);
            paint.setColor(counts[i]>0?AppTheme.accent(light):AppTheme.border(light));
            canvas.drawRoundRect(x-9*d,base-height,x+9*d,base,4*d,4*d,paint);
            paint.setColor(AppTheme.muted(light));canvas.drawText(days[i],x,getHeight()-3*d,paint);
        }
    }
}

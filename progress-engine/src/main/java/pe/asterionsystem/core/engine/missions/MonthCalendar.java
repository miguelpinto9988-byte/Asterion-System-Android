package pe.asterionsystem.core.engine.missions;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

/** Calendar model, Monday-first. Uses java.time for real leap years and years. */
public final class MonthCalendar {
    private MonthCalendar() {}
    public static List<LocalDate> days(YearMonth month) {
        Objects.requireNonNull(month,"month");
        LocalDate first=month.atDay(1);
        int leading=first.getDayOfWeek().getValue()-DayOfWeek.MONDAY.getValue();
        ArrayList<LocalDate> result=new ArrayList<>();
        for(int i=0;i<42;i++) {
            int day=i-leading+1;
            result.add(day>=1 && day<=month.lengthOfMonth()
                    ?month.atDay(day):null);
        }
        return Collections.unmodifiableList(result);
    }

    public static YearMonth shift(YearMonth current,int months) {
        Objects.requireNonNull(current,"current");
        return current.plusMonths(months);
    }

    public static YearMonth chooseYear(YearMonth current,int year) {
        Objects.requireNonNull(current,"current");
        return YearMonth.of(year,current.getMonthValue());
    }
}

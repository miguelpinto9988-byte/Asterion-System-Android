package pe.asterionsystem.core.planning;

/** One date contains two half-open time intervals that intersect. */
public final class ScheduleConflictException extends RuntimeException {
    public ScheduleConflictException(String message) { super(message); }
    public ScheduleConflictException(String message, Throwable cause) { super(message, cause); }
}

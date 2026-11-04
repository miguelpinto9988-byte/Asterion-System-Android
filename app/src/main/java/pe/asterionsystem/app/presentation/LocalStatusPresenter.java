package pe.asterionsystem.app.presentation;

import android.app.Activity;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import java.util.Objects;
import pe.asterionsystem.app.R;
import pe.asterionsystem.app.data.AsterionDatabaseHelper;
import pe.asterionsystem.core.offline.BackgroundLocalStorageChecks;
import pe.asterionsystem.core.offline.LocalStorageReport;

/**
 * A read-only offline diagnostics screen. Retrying a check is user-initiated.
 * Neither this presenter nor its worker can clear or repair the database.
 */
public final class LocalStatusPresenter implements AutoCloseable {
    private final Activity activity;
    private final AsterionDatabaseHelper database;
    private final BackgroundLocalStorageChecks operations;
    private final Handler main = new Handler(Looper.getMainLooper());
    private View activeScreen;
    private long generation;
    private boolean closed;

    public LocalStatusPresenter(Activity activity, AsterionDatabaseHelper database,
                                BackgroundLocalStorageChecks operations) {
        this.activity = Objects.requireNonNull(activity, "activity");
        this.database = Objects.requireNonNull(database, "database");
        this.operations = Objects.requireNonNull(operations, "operations");
    }

    public void bind(View screen, Runnable back) {
        activeScreen = screen;
        ++generation;
        screen.<Button>findViewById(R.id.local_status_back)
                .setOnClickListener(v -> back.run());
        screen.<Button>findViewById(R.id.local_status_check)
                .setOnClickListener(v -> inspect(screen));
        inspect(screen);
    }

    private void inspect(View screen) {
        if (closed || activeScreen != screen) return;
        final long token = ++generation;
        Button button = screen.findViewById(R.id.local_status_check);
        TextView state = screen.findViewById(R.id.local_status_state);
        TextView integrity = screen.findViewById(R.id.local_status_integrity);
        TextView records = screen.findViewById(R.id.local_status_counts);
        button.setEnabled(false);
        state.setText(R.string.local_status_checking);
        integrity.setText(R.string.local_status_readonly);
        records.setText("");
        operations.inspect().whenComplete((report, error) -> main.post(() -> {
            if (closed || activeScreen != screen || generation != token) return;
            button.setEnabled(true);
            if (error != null) {
                state.setText(OfflineErrorMessages.forError(error));
                integrity.setText(R.string.local_status_no_repair);
                return;
            }
            state.setText(report.state() == LocalStorageReport.State.HEALTHY
                    ? R.string.local_status_healthy : R.string.local_status_attention);
            integrity.setText(activity.getString(R.string.local_status_checks,
                    report.quickCheckPassed()
                            ? activity.getString(R.string.local_status_pass)
                            : activity.getString(R.string.local_status_warn),
                    report.foreignKeysPassed()
                            ? activity.getString(R.string.local_status_pass)
                            : activity.getString(R.string.local_status_warn)));
            records.setText(activity.getString(R.string.local_status_records,
                    report.catalogCount(), report.scheduledCount(), report.outcomesCount()));
        }));
    }

    @Override public void close() {
        if (closed) return;
        closed = true;
        ++generation;
        activeScreen = null;
        operations.closeAfter(database::close);
    }
}

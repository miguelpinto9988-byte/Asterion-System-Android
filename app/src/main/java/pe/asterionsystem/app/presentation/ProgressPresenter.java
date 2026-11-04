package pe.asterionsystem.app.presentation;

import android.app.Activity;
import android.app.AlertDialog;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.format.TextStyle;
import java.time.temporal.TemporalAdjusters;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import pe.asterionsystem.app.R;
import pe.asterionsystem.app.data.AsterionDatabaseHelper;
import pe.asterionsystem.core.progress.BackgroundProgressOperations;
import pe.asterionsystem.core.progress.ProgressEntry;
import pe.asterionsystem.core.progress.ProgressSummary;
import pe.asterionsystem.core.progress.TrackingStatus;

/**
 * Binds phase-09 tracking and weekly summary to native Android XML.
 * A changed result is explicitly entered by the user; no automatic progress.
 */
public final class ProgressPresenter implements AutoCloseable {
    private static final String KEY_DAY = "asterion.progress.day.v1";
    private final Activity activity;
    private final AsterionDatabaseHelper helper;
    private final BackgroundProgressOperations operations;
    private final Handler main = new Handler(Looper.getMainLooper());
    private LocalDate selectedDay = LocalDate.now();
    private View screen;
    private long generation;
    private boolean closed;

    public ProgressPresenter(Activity activity, AsterionDatabaseHelper helper,
                             BackgroundProgressOperations operations) {
        this.activity = Objects.requireNonNull(activity, "activity");
        this.helper = Objects.requireNonNull(helper, "helper");
        this.operations = Objects.requireNonNull(operations, "operations");
    }

    public void restore(Bundle saved) {
        if (saved == null) return;
        try {
            String value = saved.getString(KEY_DAY, "");
            if (value != null && !value.isEmpty()) {
                LocalDate date = LocalDate.parse(value);
                if (date.getYear() >= 2000 && date.getYear() <= 2100) selectedDay = date;
            }
        } catch (RuntimeException ignored) {
            selectedDay = LocalDate.now();
        }
    }

    public void save(Bundle out) { out.putString(KEY_DAY, selectedDay.toString()); }

    private boolean current(View target, long token) {
        return !closed && screen == target && generation == token;
    }

    public void bindTracking(View view, Runnable back, Runnable summary) {
        screen = view;
        ++generation;
        view.<Button>findViewById(R.id.progress_back).setOnClickListener(v -> back.run());
        view.<Button>findViewById(R.id.progress_show_summary).setOnClickListener(v -> summary.run());
        view.<Button>findViewById(R.id.progress_prev_day).setOnClickListener(v -> {
            selectedDay = selectedDay.minusDays(1);
            refreshTracking(view);
        });
        view.<Button>findViewById(R.id.progress_next_day).setOnClickListener(v -> {
            selectedDay = selectedDay.plusDays(1);
            refreshTracking(view);
        });
        refreshTracking(view);
    }

    private void refreshTracking(View view) {
        if (closed || screen != view) return;
        final long token = ++generation;
        final LocalDate date = selectedDay;
        TextView dateText = view.findViewById(R.id.progress_day_label);
        dateText.setText(date.getDayOfWeek().getDisplayName(TextStyle.FULL, new Locale("es"))
                + " " + date);
        TextView status = view.findViewById(R.id.progress_status);
        ListView list = view.findViewById(R.id.progress_list);
        List<ProgressEntry> loaded = new ArrayList<>();
        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                activity, android.R.layout.simple_list_item_1, new ArrayList<String>());
        list.setAdapter(adapter);
        list.setOnItemClickListener((parent, row, position, id) -> {
            if (current(view, token) && position >= 0 && position < loaded.size()) {
                showOutcomeDialog(view, loaded.get(position));
            }
        });
        status.setText(R.string.progress_loading);
        operations.forDate(date).whenComplete((entries, error) -> main.post(() -> {
            if (!current(view, token)) return;
            if (error != null) {
                status.setText(R.string.progress_load_error);
                return;
            }
            loaded.clear();
            loaded.addAll(entries);
            adapter.clear();
            for (ProgressEntry e : entries) {
                adapter.add(String.format(Locale.ROOT, "%02d:%02d · %s\n%s",
                        e.schedule().startMinute() / 60, e.schedule().startMinute() % 60,
                        e.title(), label(e.status())));
            }
            status.setText(entries.isEmpty()
                    ? activity.getString(R.string.progress_no_plans)
                    : activity.getString(R.string.progress_results_count, entries.size()));
        }));
    }

    private String label(TrackingStatus status) {
        if (status == TrackingStatus.COMPLETED) return activity.getString(R.string.progress_done);
        if (status == TrackingStatus.SKIPPED) return activity.getString(R.string.progress_skipped);
        return activity.getString(R.string.progress_pending);
    }

    private void showOutcomeDialog(View parentScreen, ProgressEntry entry) {
        final long token = generation;
        final Spinner selection = new Spinner(activity);
        final EditText noteInput = new EditText(activity);
        selection.setContentDescription(activity.getString(R.string.progress_status_label));
        ArrayAdapter<String> labels = new ArrayAdapter<>(activity,
                android.R.layout.simple_spinner_item,
                new String[]{
                        activity.getString(R.string.progress_pending),
                        activity.getString(R.string.progress_done),
                        activity.getString(R.string.progress_skipped)
                });
        labels.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        selection.setAdapter(labels);
        int index = entry.status() == TrackingStatus.COMPLETED ? 1 :
                entry.status() == TrackingStatus.SKIPPED ? 2 : 0;
        selection.setSelection(index);
        noteInput.setHint(R.string.progress_note_hint);
        noteInput.setSingleLine(false);
        noteInput.setMaxLines(3);
        noteInput.setText(entry.note());
        noteInput.setFilters(new android.text.InputFilter[]{
                new android.text.InputFilter.LengthFilter(500)});
        LinearLayout content = new LinearLayout(activity);
        content.setOrientation(LinearLayout.VERTICAL);
        int margin = (int) (20 * activity.getResources().getDisplayMetrics().density);
        content.setPadding(margin, margin / 2, margin, 0);
        content.addView(selection);
        content.addView(noteInput);
        AlertDialog dialog = new AlertDialog.Builder(activity)
                .setTitle(entry.title())
                .setView(content)
                .setNegativeButton(android.R.string.cancel, (d, which) -> { })
                .setPositiveButton(R.string.progress_save, null)
                .create();
        dialog.setOnShowListener(ignored -> dialog.getButton(AlertDialog.BUTTON_POSITIVE)
                .setOnClickListener(v -> {
                    if (!current(parentScreen, token)) { dialog.dismiss(); return; }
                    TrackingStatus value = selection.getSelectedItemPosition() == 1
                            ? TrackingStatus.COMPLETED
                            : selection.getSelectedItemPosition() == 2
                                    ? TrackingStatus.SKIPPED : TrackingStatus.PENDING;
                    String note = noteInput.getText().toString().trim();
                    // A PENDING status is absence of a row, so no note can be persisted.
                    if (value == TrackingStatus.PENDING) note = "";
                    dialog.getButton(AlertDialog.BUTTON_POSITIVE).setEnabled(false);
                    operations.mark(entry.schedule().id(), value, note,
                            System.currentTimeMillis()).whenComplete((result, error) -> main.post(() -> {
                        if (!current(parentScreen, token)) return;
                        if (error == null) {
                            dialog.dismiss();
                            refreshTracking(parentScreen);
                        } else {
                            dialog.getButton(AlertDialog.BUTTON_POSITIVE).setEnabled(true);
                            ((TextView) parentScreen.findViewById(R.id.progress_status))
                                    .setText(R.string.progress_save_error);
                        }
                    }));
                }));
        dialog.show();
    }

    public void bindSummary(View view, Runnable back) {
        screen = view;
        ++generation;
        view.<Button>findViewById(R.id.progress_summary_back).setOnClickListener(v -> back.run());
        view.<Button>findViewById(R.id.progress_summary_prev).setOnClickListener(v -> {
            selectedDay = selectedDay.minusWeeks(1);
            refreshSummary(view);
        });
        view.<Button>findViewById(R.id.progress_summary_next).setOnClickListener(v -> {
            selectedDay = selectedDay.plusWeeks(1);
            refreshSummary(view);
        });
        refreshSummary(view);
    }

    private void refreshSummary(View view) {
        if (closed || screen != view) return;
        final long token = ++generation;
        LocalDate monday = selectedDay.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
        ((TextView) view.findViewById(R.id.progress_summary_week))
                .setText(monday + " — " + monday.plusDays(6));
        TextView status = view.findViewById(R.id.progress_summary_state);
        status.setText(R.string.progress_loading);
        operations.summaryForWeek(monday).whenComplete((data, error) -> main.post(() -> {
            if (!current(view, token)) return;
            if (error != null) {
                status.setText(R.string.progress_load_error);
                return;
            }
            ((TextView) view.findViewById(R.id.progress_summary_total))
                    .setText(Integer.toString(data.total()));
            ((TextView) view.findViewById(R.id.progress_summary_done))
                    .setText(Integer.toString(data.completed()));
            ((TextView) view.findViewById(R.id.progress_summary_skipped))
                    .setText(Integer.toString(data.skipped()));
            ((TextView) view.findViewById(R.id.progress_summary_pending))
                    .setText(Integer.toString(data.pending()));
            ProgressBar bar = view.findViewById(R.id.progress_summary_bar);
            bar.setMax(100);
            bar.setProgress(data.completionPercent());
            status.setText(data.hasEvaluated()
                    ? activity.getString(R.string.progress_summary_rate,
                            data.completionPercent(), data.evaluated())
                    : activity.getString(R.string.progress_summary_no_evaluated));
        }));
    }

    @Override public void close() {
        if (closed) return;
        closed = true;
        screen = null;
        ++generation;
        operations.closeAfter(helper::close);
    }
}

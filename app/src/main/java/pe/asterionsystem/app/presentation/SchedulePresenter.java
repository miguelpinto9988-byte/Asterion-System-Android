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
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.TextStyle;
import java.time.temporal.TemporalAdjusters;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;
import pe.asterionsystem.app.R;
import pe.asterionsystem.app.data.AsterionDatabaseHelper;
import pe.asterionsystem.core.catalog.CatalogItem;
import pe.asterionsystem.core.domain.EntityId;
import pe.asterionsystem.core.planning.BackgroundScheduleOperations;
import pe.asterionsystem.core.planning.ScheduleConflictException;
import pe.asterionsystem.core.planning.ScheduleListing;
import pe.asterionsystem.core.planning.ScheduledActivity;

/** Only Java binds native XML views; SQLite work runs on a dedicated worker. */
public final class SchedulePresenter implements AutoCloseable {
    private static final String KEY_DAY = "asterion.planning.day.v1";
    private static final String KEY_EDIT = "asterion.planning.edit.v1";
    private static final int[] DAY_BUTTONS = {
            R.id.plan_day_0, R.id.plan_day_1, R.id.plan_day_2, R.id.plan_day_3,
            R.id.plan_day_4, R.id.plan_day_5, R.id.plan_day_6
    };
    private final Activity activity;
    private final AsterionDatabaseHelper db;
    private final BackgroundScheduleOperations operations;
    private final Handler main = new Handler(Looper.getMainLooper());

    private LocalDate selectedDate = LocalDate.now();
    private EntityId editingId;
    private View activeScreen;
    private long generation;
    private boolean closed;

    private static final class FormData {
        final List<CatalogItem> activities;
        final Optional<ScheduledActivity> original;
        FormData(List<CatalogItem> activities, Optional<ScheduledActivity> original) {
            this.activities = activities;
            this.original = original;
        }
    }

    public SchedulePresenter(Activity activity, AsterionDatabaseHelper db,
                             BackgroundScheduleOperations operations) {
        this.activity = Objects.requireNonNull(activity, "activity");
        this.db = Objects.requireNonNull(db, "db");
        this.operations = Objects.requireNonNull(operations, "operations");
    }

    public void restore(Bundle saved) {
        if (saved == null) return;
        try {
            String date = saved.getString(KEY_DAY, "");
            if (date != null && !date.isEmpty()) selectedDate = LocalDate.parse(date);
            String id = saved.getString(KEY_EDIT, "");
            if (id != null && !id.isEmpty() && id.length() <= 64) editingId = new EntityId(id);
        } catch (RuntimeException invalid) {
            selectedDate = LocalDate.now();
            editingId = null;
        }
    }

    public void save(Bundle output) {
        output.putString(KEY_DAY, selectedDate.toString());
        output.putString(KEY_EDIT, editingId == null ? "" : editingId.value());
    }

    public void newEntry() { editingId = null; }
    public void editEntry(EntityId id) { editingId = Objects.requireNonNull(id, "id"); }

    private boolean current(View screen, long token) {
        return !closed && activeScreen == screen && generation == token;
    }

    public void bindCalendar(View screen, Runnable back, Runnable create,
                             Consumer<EntityId> edit) {
        activeScreen = screen;
        ++generation;
        screen.<Button>findViewById(R.id.plan_back).setOnClickListener(v -> back.run());
        screen.<Button>findViewById(R.id.plan_create).setOnClickListener(v -> create.run());
        screen.<Button>findViewById(R.id.plan_previous_week).setOnClickListener(v -> {
            selectedDate = selectedDate.minusWeeks(1);
            refreshCalendar(screen, edit);
        });
        screen.<Button>findViewById(R.id.plan_next_week).setOnClickListener(v -> {
            selectedDate = selectedDate.plusWeeks(1);
            refreshCalendar(screen, edit);
        });
        refreshCalendar(screen, edit);
    }

    private void refreshCalendar(View screen, Consumer<EntityId> edit) {
        if (closed || activeScreen != screen) return;
        final LocalDate monday = selectedDate.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
        ((TextView) screen.findViewById(R.id.plan_week_title))
                .setText(monday + " — " + monday.plusDays(6));
        for (int i = 0; i < DAY_BUTTONS.length; i++) {
            final LocalDate day = monday.plusDays(i);
            Button b = screen.findViewById(DAY_BUTTONS[i]);
            b.setText(day.getDayOfWeek().getDisplayName(TextStyle.NARROW, new Locale("es"))
                    + "\n" + day.getDayOfMonth());
            b.setEnabled(!day.equals(selectedDate));
            b.setOnClickListener(v -> {
                selectedDate = day;
                refreshCalendar(screen, edit);
            });
        }
        TextView dateLabel = screen.findViewById(R.id.plan_selected_day);
        dateLabel.setText(activity.getString(R.string.plan_selected_day, selectedDate.toString()));
        TextView status = screen.findViewById(R.id.plan_status);
        ListView list = screen.findViewById(R.id.plan_entries);
        List<ScheduleListing> shown = new ArrayList<>();
        ArrayAdapter<String> adapter = new PreferencesRowAdapter(activity);
        list.setAdapter(adapter);
        list.setOnItemClickListener((parent, view, position, rowId) -> {
            if (position >= 0 && position < shown.size() && !closed && activeScreen == screen) {
                edit.accept(shown.get(position).entry().id());
            }
        });
        final long token = ++generation;
        status.setText(R.string.plan_loading);
        operations.listWithTitles(selectedDate).whenComplete((entries, error) -> main.post(() -> {
            if (!current(screen, token)) return;
            if (error != null) {
                status.setText(R.string.plan_load_error);
                return;
            }
            shown.clear();
            shown.addAll(entries);
            adapter.clear();
            for (ScheduleListing listing : entries) {
                ScheduledActivity e = listing.entry();
                adapter.add(time(e.startMinute()) + " – " + time(e.endMinute())
                        + "\n" + listing.title());
            }
            status.setText(entries.isEmpty()
                    ? activity.getString(R.string.plan_empty)
                    : activity.getString(R.string.plan_count, entries.size()));
        }));
    }

    public void bindForm(View screen, Runnable back) {
        activeScreen = screen;
        final long token = ++generation;
        final EntityId requestedId = editingId;
        final Spinner chooser = screen.findViewById(R.id.plan_activity);
        final EditText dateInput = screen.findViewById(R.id.plan_date);
        final EditText timeInput = screen.findViewById(R.id.plan_time);
        final EditText durationInput = screen.findViewById(R.id.plan_duration);
        final TextView status = screen.findViewById(R.id.plan_form_status);
        final Button save = screen.findViewById(R.id.plan_save);
        final Button delete = screen.findViewById(R.id.plan_delete);
        screen.<Button>findViewById(R.id.plan_cancel).setOnClickListener(v -> back.run());
        save.setEnabled(false);
        delete.setVisibility(requestedId == null ? View.GONE : View.VISIBLE);
        delete.setEnabled(false);
        dateInput.setText(selectedDate.toString());
        timeInput.setText("09:00");
        durationInput.setText("30");
        status.setText(R.string.plan_loading);

        CompletableFuture<Optional<ScheduledActivity>> existing = requestedId == null
                ? CompletableFuture.completedFuture(Optional.empty())
                : operations.find(requestedId);
        operations.activities().thenCombine(existing, FormData::new)
                .whenComplete((data, error) -> main.post(() -> {
                    if (!current(screen, token)) return;
                    if (error != null || data == null || data.activities.isEmpty()
                            || (requestedId != null && !data.original.isPresent())) {
                        status.setText(R.string.plan_load_error);
                        return;
                    }
                    List<String> labels = new ArrayList<>();
                    for (CatalogItem item : data.activities) labels.add(item.title());
                    ArrayAdapter<String> adapter = new ArrayAdapter<>(
                            activity, android.R.layout.simple_spinner_item, labels);
                    adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                    chooser.setAdapter(adapter);
                    final ScheduledActivity original = data.original.orElse(null);
                    if (original != null
                            && data.activities.stream().noneMatch(a -> a.id().equals(original.activityId()))) {
                        status.setText(R.string.plan_load_error);
                        return;
                    }
                    if (original != null) {
                        dateInput.setText(original.date().toString());
                        timeInput.setText(time(original.startMinute()));
                        durationInput.setText(Integer.toString(original.durationMinutes()));
                        for (int i = 0; i < data.activities.size(); i++) {
                            if (data.activities.get(i).id().equals(original.activityId())) {
                                chooser.setSelection(i);
                                break;
                            }
                        }
                    }
                    save.setEnabled(true);
                    delete.setEnabled(original != null);
                    status.setText(original == null ? R.string.plan_new_hint : R.string.plan_edit_hint);
                    save.setOnClickListener(v -> {
                        try {
                            LocalDate day = LocalDate.parse(dateInput.getText().toString().trim());
                            LocalTime clock = LocalTime.parse(timeInput.getText().toString().trim(),
                                    DateTimeFormatter.ofPattern("HH:mm"));
                            int minutes = Integer.parseInt(durationInput.getText().toString().trim());
                            int start = clock.getHour() * 60 + clock.getMinute();
                            EntityId activityId = data.activities.get(chooser.getSelectedItemPosition()).id();
                            long now = System.currentTimeMillis();
                            ScheduledActivity entry = original == null
                                    ? new ScheduledActivity(new EntityId(UUID.randomUUID().toString()),
                                            activityId, day, start, minutes, now, now)
                                    : original.reschedule(activityId, day, start, minutes,
                                            Math.max(now, original.createdAtMs()));
                            save.setEnabled(false);
                            status.setText(R.string.plan_saving);
                            CompletableFuture<?> action = original == null
                                    ? operations.create(entry) : operations.update(entry);
                            action.whenComplete((result, problem) -> main.post(() -> {
                                if (!current(screen, token)) return;
                                if (problem != null || Boolean.FALSE.equals(result)) {
                                    save.setEnabled(true);
                                    Throwable why = unwrap(problem);
                                    status.setText(why instanceof ScheduleConflictException
                                            ? R.string.plan_conflict : R.string.plan_save_error);
                                } else {
                                    selectedDate = entry.date();
                                    editingId = null;
                                    back.run();
                                }
                            }));
                        } catch (RuntimeException invalid) {
                            status.setText(R.string.plan_invalid);
                        }
                    });
                    if (original != null) {
                        delete.setOnClickListener(v -> new AlertDialog.Builder(activity)
                                .setMessage(R.string.plan_confirm_delete)
                                .setNegativeButton(android.R.string.cancel, (d, w) -> { })
                                .setPositiveButton(R.string.plan_delete, (d, w) -> {
                                    delete.setEnabled(false);
                                    operations.delete(original.id()).whenComplete((ok, problem) ->
                                            main.post(() -> {
                                                if (!current(screen, token)) return;
                                                if (problem == null && Boolean.TRUE.equals(ok)) {
                                                    editingId = null;
                                                    back.run();
                                                } else {
                                                    delete.setEnabled(true);
                                                    status.setText(R.string.plan_save_error);
                                                }
                                            }));
                                }).show());
                    }
                }));
    }

    private static Throwable unwrap(Throwable value) {
        while (value instanceof java.util.concurrent.CompletionException
                || value instanceof java.util.concurrent.ExecutionException) {
            if (value.getCause() == null) break;
            value = value.getCause();
        }
        return value;
    }

    private static String time(int minute) {
        return String.format(Locale.ROOT, "%02d:%02d", minute / 60, minute % 60);
    }

    @Override public void close() {
        if (closed) return;
        closed = true;
        activeScreen = null;
        ++generation;
        operations.closeAfter(db::close);
    }
}

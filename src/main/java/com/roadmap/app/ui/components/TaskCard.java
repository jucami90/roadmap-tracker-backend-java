package com.roadmap.app.ui.components;

import com.roadmap.app.model.*;
import com.roadmap.app.repository.NoteRepository;
import com.roadmap.app.repository.ReminderRepository;
import com.roadmap.app.repository.TaskRepository;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;

import com.roadmap.app.service.MacCalendarService;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * TaskCard — one card per Task.
 *
 * Improvements applied:
 *  1. Each task has its own collapsible Notes panel (📝 button).
 *  2. Adding a subtask no longer collapses the subtask panel.
 *  3. Completed tasks are handled externally (WeekDetailView moves them).
 */
public class TaskCard extends VBox {

    private final Task task;
    private final String weekTitle;   // loaded eagerly to avoid LazyInitializationException
    private final Runnable onChanged;
    private final TaskRepository     taskRepo     = new TaskRepository();
    private final NoteRepository     noteRepo     = new NoteRepository();
    private final ReminderRepository reminderRepo = new ReminderRepository();

    // sub-tasks panel
    private VBox    subTasksContainer;
    private Button  subBtn;
    private boolean subTasksVisible = false;

    // notes panel
    private VBox    notesContainer;
    private boolean notesVisible = false;

    public TaskCard(Task task, Runnable onChanged) {
        this.task      = task;
        // Read week title while the session is still open (LAZY proxy safe window)
        String wt = "";
        try { if (task.getWeek() != null) wt = task.getWeek().getTitle(); }
        catch (Exception ignored) {}
        this.weekTitle = wt;
        this.onChanged = onChanged;
        setSpacing(0);
        buildCard();
    }

    // ── BUILD ────────────────────────────────────────────────────
    private void buildCard() {
        // Main row
        HBox mainRow = buildMainRow();

        // Sub-task progress bar (thin, always visible when subs exist)
        ProgressBar subProg = new ProgressBar(task.getSubTaskProgress());
        subProg.getStyleClass().add("progress-bar");
        subProg.setPrefWidth(Double.MAX_VALUE);
        subProg.setPrefHeight(3);
        subProg.setVisible(!task.getSubTasks().isEmpty());
        subProg.setManaged(!task.getSubTasks().isEmpty());
        if (task.getSubTaskProgress() == 1.0) subProg.getStyleClass().add("complete");

        // SubTasks panel (collapsed by default)
        subTasksContainer = new VBox(4);
        subTasksContainer.setPadding(new Insets(4, 14, 8, 44));
        subTasksContainer.setVisible(false);
        subTasksContainer.setManaged(false);
        buildSubTaskRows();

        // Task Notes panel (collapsed by default)
        notesContainer = new VBox(6);
        notesContainer.setPadding(new Insets(4, 14, 8, 44));
        notesContainer.setVisible(false);
        notesContainer.setManaged(false);
        buildTaskNotesPanel();

        getChildren().addAll(mainRow, subProg, subTasksContainer, notesContainer);
    }

    private HBox buildMainRow() {
        HBox row = new HBox(8);
        row.setAlignment(Pos.CENTER_LEFT);
        row.setPadding(new Insets(10, 12, 10, 12));
        row.getStyleClass().add("task-card");
        if (task.isCompleted()) row.getStyleClass().add("completed");

        String priClass = switch (task.getPriority()) {
            case HIGH -> "priority-high";
            case LOW  -> "priority-low";
            default   -> "priority-medium";
        };
        row.getStyleClass().add(priClass);

        // Checkbox
        CheckBox cb = new CheckBox();
        cb.setSelected(task.isCompleted());
        cb.setOnAction(e -> toggleComplete(cb.isSelected()));

        // Title
        Label titleLbl = new Label(task.getTitle());
        titleLbl.setWrapText(true);
        titleLbl.setMaxWidth(Double.MAX_VALUE);
        HBox.setHgrow(titleLbl, Priority.ALWAYS);
        titleLbl.setStyle(task.isCompleted()
            ? "-fx-text-fill:#666666; -fx-strikethrough:true;"
            : "-fx-text-fill:#E0E0E0;");

        // Badges
        Label catBadge = badge(task.getCategory().name().replace("_", " "), "badge-cat");
        String priBadgeClass = switch (task.getPriority()) {
            case HIGH -> "badge-high";
            case LOW  -> "badge-low";
            default   -> "badge-medium";
        };
        Label priBadge = badge(task.getPriority().name(), priBadgeClass);

        // Due date
        Label dueLbl = new Label();
        if (task.getDueDate() != null) {
            dueLbl.setText("📅 " + task.getDueDate().format(DateTimeFormatter.ofPattern("MMM dd")));
            dueLbl.setStyle("-fx-text-fill:#777777; -fx-font-size:10px;");
        }

        // Sub-tasks toggle button
        int subCount = task.getSubTasks().size();
        int subDone  = task.getCompletedSubTaskCount();
        subBtn = new Button("▶ " + subDone + "/" + subCount + " sub");
        subBtn.getStyleClass().add("btn-icon");
        subBtn.setStyle("-fx-text-fill:#9E9E9E; -fx-font-size:10px;");
        subBtn.setTooltip(new Tooltip("Show / hide subtasks"));
        subBtn.setOnAction(e -> togglePanel(subTasksContainer, subBtn, true));

        // Notes toggle button  ← IMPROVEMENT #1
        long noteCount = countTaskNotes();
        Button notesBtn = new Button("📝" + (noteCount > 0 ? " " + noteCount : ""));
        notesBtn.getStyleClass().add("btn-icon");
        notesBtn.setTooltip(new Tooltip("Show / hide task notes"));
        notesBtn.setOnAction(e -> {
            notesVisible = !notesVisible;
            notesContainer.setVisible(notesVisible);
            notesContainer.setManaged(notesVisible);
            notesBtn.setText("📝" + (noteCount > 0 ? (notesVisible ? " ▼" : " " + noteCount) : (notesVisible ? " ▼" : "")));
        });

        // Edit
        Button editBtn = iconBtn("✏️", "Edit task");
        editBtn.setOnAction(e -> showEditDialog());

        // Reminder
        Button remBtn = iconBtn("⏰", "Add reminder");
        remBtn.setOnAction(e -> showAddReminderDialog());

        // Delete
        Button delBtn = iconBtn("🗑", "Delete task");
        delBtn.setStyle("-fx-text-fill:#EF5350;");
        delBtn.setOnAction(e -> deleteTask());

        row.getChildren().addAll(cb, titleLbl, catBadge, priBadge, dueLbl,
                                 subBtn, notesBtn, editBtn, remBtn, delBtn);
        return row;
    }

    // ── SUBTASKS ─────────────────────────────────────────────────
    private void buildSubTaskRows() {
        subTasksContainer.getChildren().clear();
        for (SubTask sub : task.getSubTasks()) {
            subTasksContainer.getChildren().add(buildSubTaskRow(sub));
        }
        subTasksContainer.getChildren().add(buildAddSubTaskRow());
    }

    private HBox buildSubTaskRow(SubTask sub) {
        HBox row = new HBox(8);
        row.getStyleClass().add("subtask-row");
        row.setAlignment(Pos.CENTER_LEFT);

        CheckBox subCb = new CheckBox(sub.getTitle());
        subCb.setSelected(sub.isCompleted());
        subCb.setStyle(sub.isCompleted()
            ? "-fx-text-fill:#666666; -fx-strikethrough:true;"
            : "-fx-text-fill:#C0C0C0;");
        subCb.setMaxWidth(Double.MAX_VALUE);
        HBox.setHgrow(subCb, Priority.ALWAYS);
        subCb.setOnAction(e -> {
            if (subCb.isSelected()) sub.markCompleted();
            else sub.markIncomplete();
            taskRepo.saveSubTask(sub);
            // Refresh only the subtask rows, keep panel open  ← IMPROVEMENT #2
            refreshSubTasksInPlace();
            onChanged.run();
        });

        Button del = new Button("✕");
        del.getStyleClass().add("btn-icon");
        del.setStyle("-fx-font-size:10px; -fx-text-fill:#EF5350;");
        del.setOnAction(e -> {
            taskRepo.deleteSubTask(sub);
            refreshSubTasksInPlace();
            onChanged.run();
        });

        row.getChildren().addAll(subCb, del);
        return row;
    }

    private HBox buildAddSubTaskRow() {
        HBox addRow = new HBox(8);
        addRow.setAlignment(Pos.CENTER_LEFT);
        addRow.setPadding(new Insets(4, 0, 0, 0));

        TextField addField = new TextField();
        addField.setPromptText("Add subtask...");
        HBox.setHgrow(addField, Priority.ALWAYS);

        Button addBtn = new Button("＋");
        addBtn.getStyleClass().addAll("button", "btn-success");
        addBtn.setPrefWidth(36);

        Runnable doAdd = () -> {
            String txt = addField.getText().trim();
            if (!txt.isEmpty()) {
                SubTask sub = new SubTask(task, txt);
                sub.setOrderIndex(task.getSubTasks().size());
                taskRepo.saveSubTask(sub);
                addField.clear();
                // Reload sub-task rows and keep the panel OPEN  ← IMPROVEMENT #2
                refreshSubTasksInPlace();
                onChanged.run();
                // Re-focus the field so user can keep typing
                addField.requestFocus();
            }
        };
        addBtn.setOnAction(e -> doAdd.run());
        addField.setOnAction(e -> doAdd.run());

        addRow.getChildren().addAll(addField, addBtn);
        return addRow;
    }

    /**
     * Reload sub-task rows WITHOUT toggling visibility.
     * This is key: panel stays open after adding/completing a subtask.
     */
    private void refreshSubTasksInPlace() {
        // Reload fresh from DB via the task's existing list
        // We rebuild rows but keep subTasksVisible intact
        buildSubTaskRows();
        // Update the toggle button counter
        List<SubTask> subs = task.getSubTasks();
        int total = subs.size();
        int done  = task.getCompletedSubTaskCount();
        subBtn.setText((subTasksVisible ? "▼ " : "▶ ") + done + "/" + total + " sub");
    }

    private void togglePanel(VBox panel, Button btn, boolean isSubPanel) {
        if (isSubPanel) {
            subTasksVisible = !subTasksVisible;
            panel.setVisible(subTasksVisible);
            panel.setManaged(subTasksVisible);
            int total = task.getSubTasks().size();
            int done  = task.getCompletedSubTaskCount();
            btn.setText((subTasksVisible ? "▼ " : "▶ ") + done + "/" + total + " sub");
        }
    }

    // ── TASK NOTES  (Improvement #1) ─────────────────────────────
    private void buildTaskNotesPanel() {
        notesContainer.getChildren().clear();

        // Existing notes
        List<Note> notes = noteRepo.findByTaskId(task.getId());
        for (Note note : notes) {
            notesContainer.getChildren().add(buildTaskNoteRow(note));
        }

        // Add note area
        HBox addRow = new HBox(8);
        addRow.setAlignment(Pos.CENTER_LEFT);

        TextArea noteArea = new TextArea();
        noteArea.setPromptText("Add a note for this task...");
        noteArea.setPrefRowCount(2);
        noteArea.setPrefWidth(340);
        HBox.setHgrow(noteArea, Priority.ALWAYS);

        Button saveBtn = new Button("Save");
        saveBtn.getStyleClass().addAll("button", "btn-primary");
        saveBtn.setOnAction(e -> {
            String txt = noteArea.getText().trim();
            if (!txt.isEmpty()) {
                Note n = new Note();
                n.setTask(task);
                n.setTitle("Task note");
                n.setContent(txt);
                noteRepo.save(n);
                noteArea.clear();
                buildTaskNotesPanel();  // refresh panel in place
            }
        });

        addRow.getChildren().addAll(noteArea, saveBtn);
        notesContainer.getChildren().add(addRow);
    }

    private HBox buildTaskNoteRow(Note note) {
        HBox row = new HBox(8);
        row.setAlignment(Pos.TOP_LEFT);
        row.setPadding(new Insets(4, 0, 4, 0));
        row.setStyle("-fx-background-color:#1A1A1A; -fx-background-radius:4px; -fx-padding:6 8 6 8;");

        Label content = new Label(note.getContent());
        content.setWrapText(true);
        content.setStyle("-fx-text-fill:#C0C0C0; -fx-font-size:11px;");
        HBox.setHgrow(content, Priority.ALWAYS);
        content.setMaxWidth(Double.MAX_VALUE);

        Label dateLbl = new Label(note.getUpdatedAt().format(DateTimeFormatter.ofPattern("MM/dd HH:mm")));
        dateLbl.setStyle("-fx-text-fill:#555555; -fx-font-size:9px;");

        Button delBtn = new Button("✕");
        delBtn.getStyleClass().add("btn-icon");
        delBtn.setStyle("-fx-font-size:10px; -fx-text-fill:#EF5350;");
        delBtn.setOnAction(e -> {
            noteRepo.delete(note);
            buildTaskNotesPanel();
        });

        VBox meta = new VBox(2, dateLbl, delBtn);
        meta.setAlignment(Pos.TOP_RIGHT);

        row.getChildren().addAll(content, meta);
        return row;
    }

    private long countTaskNotes() {
        try { return noteRepo.countByTaskId(task.getId()); }
        catch (Exception e) { return 0; }
    }

    // ── COMPLETE / DELETE ────────────────────────────────────────
    private void toggleComplete(boolean completed) {
        if (completed) task.markCompleted();
        else           task.markIncomplete();
        taskRepo.save(task);
        onChanged.run();   // WeekDetailView will move card to Done section (#3)
    }

    private void deleteTask() {
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Delete Task");
        confirm.setHeaderText("Delete \"" + task.getTitle() + "\"?");
        confirm.setContentText("This will also delete all subtasks and reminders.");
        confirm.getDialogPane().getStylesheets().add(
            getClass().getResource("/css/dark-theme.css").toExternalForm());
        confirm.showAndWait().ifPresent(btn -> {
            if (btn == ButtonType.OK) { taskRepo.delete(task); onChanged.run(); }
        });
    }

    // ── EDIT DIALOG ──────────────────────────────────────────────
    private void showEditDialog() {
        Dialog<Task> dialog = new Dialog<>();
        dialog.setTitle("Edit Task");
        dialog.setHeaderText("Edit: " + task.getTitle());
        dialog.getDialogPane().getStylesheets().add(
            getClass().getResource("/css/dark-theme.css").toExternalForm());

        VBox form = new VBox(8);
        form.setPadding(new Insets(10));
        form.setPrefWidth(440);

        TextField titleField = new TextField(task.getTitle());
        TextArea  descArea   = new TextArea(task.getDescription() != null ? task.getDescription() : "");
        descArea.setPromptText("Description (optional)");
        descArea.setPrefRowCount(3);

        ComboBox<Task.Category> catCombo = new ComboBox<>();
        catCombo.getItems().addAll(Task.Category.values());
        catCombo.setValue(task.getCategory());

        ComboBox<Task.Priority> priCombo = new ComboBox<>();
        priCombo.getItems().addAll(Task.Priority.values());
        priCombo.setValue(task.getPriority());

        DatePicker duePicker = new DatePicker(task.getDueDate());

        form.getChildren().addAll(
            new Label("Title:"),       titleField,
            new Label("Description:"), descArea,
            new Label("Category:"),    catCombo,
            new Label("Priority:"),    priCombo,
            new Label("Due Date:"),    duePicker
        );
        dialog.getDialogPane().setContent(form);
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);
        dialog.setResultConverter(btn -> {
            if (btn == ButtonType.OK) {
                String t = titleField.getText().trim();
                task.setTitle(t.isEmpty() ? task.getTitle() : t);
                task.setDescription(descArea.getText().trim());
                task.setCategory(catCombo.getValue());
                task.setPriority(priCombo.getValue());
                task.setDueDate(duePicker.getValue());
                return task;
            }
            return null;
        });
        dialog.showAndWait().ifPresent(t -> { taskRepo.save(t); onChanged.run(); });
    }

    // ── REMINDER DIALOG ──────────────────────────────────────────
    private void showAddReminderDialog() {
        Dialog<Reminder> dialog = new Dialog<>();
        dialog.setTitle("Add Reminder");
        dialog.setHeaderText("Reminder for: " + task.getTitle());
        dialog.getDialogPane().getStylesheets().add(
            getClass().getResource("/css/dark-theme.css").toExternalForm());
        dialog.getDialogPane().setPrefWidth(420);

        VBox form = new VBox(10);
        form.setPadding(new Insets(12));

        TextArea msgArea = new TextArea();
        msgArea.setPromptText("Reminder message (optional)...");
        msgArea.setPrefRowCount(2);

        DatePicker datePicker = new DatePicker(
            task.getDueDate() != null ? task.getDueDate() : java.time.LocalDate.now().plusDays(1));
        TextField timeField = new TextField("09:00");
        timeField.setPromptText("HH:mm");

        // macOS Calendar checkbox — only shown on macOS
        CheckBox calendarCheckBox = null;
        if (MacCalendarService.isMacOS()) {
            calendarCheckBox = new CheckBox("Also add to macOS Calendar  🗓");
            calendarCheckBox.setSelected(true);
            calendarCheckBox.setStyle("-fx-text-fill:#C0C0C0;");
        }

        form.getChildren().addAll(
            new Label("Message:"), msgArea,
            new Label("Date:"),    datePicker,
            new Label("Time (HH:mm):"), timeField
        );
        if (calendarCheckBox != null) {
            Separator sep = new Separator();
            Label hint = new Label("A 30-min event with a 15-min alert will be created.");
            hint.setStyle("-fx-text-fill:#666666; -fx-font-size:10px;");
            form.getChildren().addAll(sep, calendarCheckBox, hint);
        }

        dialog.getDialogPane().setContent(form);
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

        final CheckBox finalCalCb = calendarCheckBox;
        dialog.setResultConverter(btn -> {
            if (btn == ButtonType.OK && datePicker.getValue() != null) {
                try {
                    String[] parts = timeField.getText().split(":");
                    LocalDateTime dt = datePicker.getValue().atTime(
                        Integer.parseInt(parts[0]), Integer.parseInt(parts[1]));
                    String msg = msgArea.getText().trim();
                    Reminder r = new Reminder(task,
                        msg.isEmpty() ? "Time to work on: " + task.getTitle() : msg, dt);
                    r.setAddToCalendar(finalCalCb != null && finalCalCb.isSelected());
                    return r;
                } catch (Exception ignored) { return null; }
            }
            return null;
        });

        dialog.showAndWait().ifPresent(r -> {
            reminderRepo.save(r);

            // ── Send to macOS Calendar if requested ───────────────
            if (r.isAddToCalendar() && MacCalendarService.isMacOS()) {
                String notes = "Task: " + task.getTitle()
                    + "\nWeek: " + weekTitle
                    + "\nCategory: " + task.getCategory()
                    + (r.getMessage().equals("Time to work on: " + task.getTitle())
                       ? "" : "\n\n" + r.getMessage());

                MacCalendarService.Result result = MacCalendarService.addEvent(
                    "📚 " + task.getTitle(),
                    notes,
                    r.getReminderDateTime(),
                    "Roadmap Tracker"
                );

                String feedback = switch (result) {
                    case SUCCESS          -> "Reminder saved!\n✅ Event added to macOS Calendar.";
                    case NOT_MACOS        -> "Reminder saved! ✅";
                    case PERMISSION_DENIED -> "Reminder saved! ✅\n⚠️ Calendar access was denied.\n"
                        + "Go to System Settings → Privacy → Automation to allow access.";
                    case ERROR            -> "Reminder saved! ✅\n⚠️ Could not add to Calendar.\n"
                        + "Check that Calendar.app is installed.";
                };
                Alert info = new Alert(Alert.AlertType.INFORMATION, feedback);
                info.setTitle("Reminder");
                info.setHeaderText(null);
                info.getDialogPane().getStylesheets().add(
                    getClass().getResource("/css/dark-theme.css").toExternalForm());
                info.showAndWait();
            } else {
                new Alert(Alert.AlertType.INFORMATION, "Reminder set! ✅").showAndWait();
            }
        });
    }

    // ── HELPERS ──────────────────────────────────────────────────
    private Label badge(String text, String styleClass) {
        Label l = new Label(text);
        l.getStyleClass().addAll("badge", styleClass);
        return l;
    }

    private Button iconBtn(String icon, String tip) {
        Button b = new Button(icon);
        b.getStyleClass().add("btn-icon");
        b.setTooltip(new Tooltip(tip));
        return b;
    }
}

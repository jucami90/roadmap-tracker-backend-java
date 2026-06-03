package com.roadmap.app.ui.views;

import com.roadmap.app.model.*;
import com.roadmap.app.repository.*;
import com.roadmap.app.ui.components.*;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;

import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

public class WeekDetailView extends VBox {

    private final Week week;
    private final MainView mainView;
    private final TaskRepository taskRepo = new TaskRepository();
    private final NoteRepository noteRepo = new NoteRepository();
    private final WeekRepository weekRepo = new WeekRepository();

    private VBox tasksContainer;
    private VBox completedContainer;
    private VBox notesContainer;
    private ProgressBar weekProgressBar;
    private Label progressLabel;
    private TitledPane completedPane;
    private TitledPane pendingPane;

    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("MMM dd, yyyy");

    public WeekDetailView(Week week, MainView mainView) {
        this.week     = week;
        this.mainView = mainView;
        setSpacing(0);
        setFillWidth(true);
        VBox.setVgrow(this, Priority.ALWAYS);
        buildUI();
    }

    private void buildUI() {
        getChildren().add(buildWeekHeader());

        TabPane tabs = new TabPane();
        tabs.getStyleClass().add("tab-pane");
        tabs.setTabClosingPolicy(TabPane.TabClosingPolicy.UNAVAILABLE);
        VBox.setVgrow(tabs, Priority.ALWAYS);

        Tab tasksTab = new Tab("📋  Tasks");
        Tab notesTab = new Tab("📝  Week Notes");
        Tab infoTab  = new Tab("ℹ️   Info");

        tasksTab.setContent(buildTasksPane());
        notesTab.setContent(buildNotesPane());
        infoTab.setContent(buildInfoPane());

        tabs.getTabs().addAll(tasksTab, notesTab, infoTab);
        getChildren().add(tabs);
    }

    // ── WEEK HEADER ──────────────────────────────────────────────
    private VBox buildWeekHeader() {
        VBox header = new VBox(10);
        header.setPadding(new Insets(20, 24, 16, 24));

        Rectangle colorBar = new Rectangle(4, 50);
        colorBar.setArcWidth(4); colorBar.setArcHeight(4);
        try { colorBar.setFill(Color.web(week.getMonthColor())); }
        catch (Exception e) { colorBar.setFill(Color.GRAY); }

        VBox titleBox = new VBox(3);
        Label weekNumLbl = new Label("WEEK " + week.getWeekNumber());
        weekNumLbl.setStyle("-fx-font-size:11px; -fx-text-fill:#777777; -fx-font-weight:bold;");
        Label titleLbl = new Label(week.getTitle());
        titleLbl.setStyle("-fx-font-size:20px; -fx-font-weight:bold; -fx-text-fill:#4FC3F7;");
        String dates = (week.getStartDate() != null && week.getEndDate() != null)
            ? week.getStartDate().format(DATE_FMT) + "  →  " + week.getEndDate().format(DATE_FMT)
            : "Dates not set";
        Label datesLbl = new Label("📅  " + dates);
        datesLbl.setStyle("-fx-text-fill:#9E9E9E; -fx-font-size:12px;");
        titleBox.getChildren().addAll(weekNumLbl, titleLbl, datesLbl);

        HBox topRow = new HBox(14);
        topRow.setAlignment(Pos.CENTER_LEFT);
        topRow.getChildren().addAll(colorBar, titleBox);

        // Progress row
        HBox progressRow = new HBox(12);
        progressRow.setAlignment(Pos.CENTER_LEFT);
        weekProgressBar = new ProgressBar(week.getProgress());
        weekProgressBar.getStyleClass().add("progress-bar");
        if (week.getProgress() == 1.0) weekProgressBar.getStyleClass().add("complete");
        weekProgressBar.setPrefWidth(300);
        weekProgressBar.setPrefHeight(10);

        progressLabel = new Label(progressText());
        progressLabel.setStyle("-fx-text-fill:#9E9E9E; -fx-font-size:12px;");

        Button editDatesBtn = new Button("✏️ Edit Dates");
        editDatesBtn.getStyleClass().add("button");
        editDatesBtn.setOnAction(e -> showEditDatesDialog());
        HBox.setMargin(editDatesBtn, new Insets(0, 0, 0, 20));

        progressRow.getChildren().addAll(weekProgressBar, progressLabel, editDatesBtn);
        header.getChildren().addAll(topRow, progressRow);
        header.setStyle("-fx-background-color:#1A1A1A; -fx-border-color:transparent transparent #333333 transparent; -fx-border-width:0 0 1 0;");
        return header;
    }

    // ── TASKS PANE ───────────────────────────────────────────────
    private ScrollPane buildTasksPane() {
        VBox outer = new VBox(12);
        outer.setPadding(new Insets(16, 20, 16, 20));

        // Add-task bar
        HBox addBar = new HBox(10);
        addBar.setAlignment(Pos.CENTER_LEFT);
        TextField addField = new TextField();
        addField.setPromptText("Add a new task...");
        HBox.setHgrow(addField, Priority.ALWAYS);

        ComboBox<Task.Category> catCombo = new ComboBox<>();
        catCombo.getItems().addAll(Task.Category.values());
        catCombo.setValue(Task.Category.OTHER);
        catCombo.setPrefWidth(140);

        ComboBox<Task.Priority> priCombo = new ComboBox<>();
        priCombo.getItems().addAll(Task.Priority.values());
        priCombo.setValue(Task.Priority.MEDIUM);
        priCombo.setPrefWidth(100);

        Button addBtn = new Button("＋ Add Task");
        addBtn.getStyleClass().addAll("button", "btn-primary");
        addBtn.setOnAction(e -> {
            String txt = addField.getText().trim();
            if (!txt.isEmpty()) {
                Task t = new Task(week, txt, catCombo.getValue(), priCombo.getValue());
                t.setDueDate(week.getEndDate());
                taskRepo.save(t);
                addField.clear();
                reloadTasks();
                mainView.refreshStats();
                mainView.refreshSidebar();
            }
        });
        addField.setOnAction(addBtn.getOnAction());
        addBar.getChildren().addAll(addField, catCombo, priCombo, addBtn);

        // ── Pending tasks — collapsible (Improvement #2) ────────
        tasksContainer = new VBox(8);
        pendingPane = new TitledPane("Pending", tasksContainer);
        pendingPane.setExpanded(true);
        pendingPane.setAnimated(true);

        // ── Completed tasks — collapsible (Improvement #3) ────────
        completedContainer = new VBox(8);
        completedPane = new TitledPane("", completedContainer);
        completedPane.setExpanded(false);
        completedPane.setAnimated(true);

        outer.getChildren().addAll(addBar, pendingPane, completedPane);
        reloadTasks();

        ScrollPane sp = new ScrollPane(outer);
        sp.setFitToWidth(true);
        sp.getStyleClass().add("scroll-pane");
        return sp;
    }

    private void reloadTasks() {
        List<Task> all = taskRepo.findByWeekIdWithSubTasks(week.getId());

        List<Task> pending   = all.stream().filter(t -> !t.isCompleted()).collect(Collectors.toList());
        List<Task> completed = all.stream().filter(Task::isCompleted).collect(Collectors.toList());

        // Pending tasks
        tasksContainer.getChildren().clear();
        if (pending.isEmpty()) {
            Label empty = new Label(completed.isEmpty()
                ? "No tasks yet. Add your first task above!"
                : "🎉 All tasks completed!");
            empty.setStyle("-fx-text-fill:#555555; -fx-padding:10 0 0 0;");
            tasksContainer.getChildren().add(empty);
        } else {
            for (Task t : pending) {
                tasksContainer.getChildren().add(new TaskCard(t, this::onTaskChanged));
            }
        }

        // Update pending pane title with count
        pendingPane.setText("⏳  Pending  (" + pending.size() + ")");

        // Completed tasks (collapsible at the bottom)
        completedContainer.getChildren().clear();
        completedPane.setText("✅  Completed  (" + completed.size() + ")");
        completedPane.setVisible(!completed.isEmpty());
        completedPane.setManaged(!completed.isEmpty());

        for (Task t : completed) {
            completedContainer.getChildren().add(new TaskCard(t, this::onTaskChanged));
        }

        refreshProgress(all);
    }

    private void onTaskChanged() {
        reloadTasks();
        mainView.refreshStats();
        mainView.refreshSidebar();
    }

    private void refreshProgress(List<Task> all) {
        if (weekProgressBar == null) return;
        long done  = all.stream().filter(Task::isCompleted).count();
        double pct = all.isEmpty() ? 0 : (double) done / all.size();
        weekProgressBar.setProgress(pct);
        progressLabel.setText(progressText(done, all.size(), pct));
        if (pct == 1.0) {
            if (!weekProgressBar.getStyleClass().contains("complete"))
                weekProgressBar.getStyleClass().add("complete");
        } else {
            weekProgressBar.getStyleClass().remove("complete");
        }
    }

    private String progressText() {
        List<Task> all = taskRepo.findByWeekIdWithSubTasks(week.getId());
        long done = all.stream().filter(Task::isCompleted).count();
        double pct = all.isEmpty() ? 0 : (double) done / all.size();
        return progressText(done, all.size(), pct);
    }

    private String progressText(long done, int total, double pct) {
        return String.format("%d / %d tasks  •  %.0f%%", (int) done, total, pct * 100);
    }

    // ── WEEK NOTES PANE ──────────────────────────────────────────
    private ScrollPane buildNotesPane() {
        VBox outer = new VBox(12);
        outer.setPadding(new Insets(16, 20, 16, 20));

        Button addNoteBtn = new Button("＋ New Note");
        addNoteBtn.getStyleClass().addAll("button", "btn-primary");
        addNoteBtn.setOnAction(e -> showAddNoteDialog());

        notesContainer = new VBox(10);
        reloadNotes();
        outer.getChildren().addAll(addNoteBtn, notesContainer);

        ScrollPane sp = new ScrollPane(outer);
        sp.setFitToWidth(true);
        sp.getStyleClass().add("scroll-pane");
        return sp;
    }

    private void reloadNotes() {
        notesContainer.getChildren().clear();
        List<Note> notes = noteRepo.findByWeekId(week.getId());
        if (notes.isEmpty()) {
            Label empty = new Label("No week notes yet. Click '+ New Note' to start!");
            empty.setStyle("-fx-text-fill:#555555; -fx-padding:10 0 0 0;");
            notesContainer.getChildren().add(empty);
            return;
        }
        for (Note note : notes) notesContainer.getChildren().add(buildNoteCard(note));
    }

    private VBox buildNoteCard(Note note) {
        VBox card = new VBox(8);
        card.getStyleClass().add("card");

        HBox titleRow = new HBox(10);
        titleRow.setAlignment(Pos.CENTER_LEFT);
        Label titleLbl = new Label(note.getTitle() != null ? note.getTitle() : "Note");
        titleLbl.setStyle("-fx-font-weight:bold; -fx-text-fill:#4FC3F7;");
        HBox.setHgrow(titleLbl, Priority.ALWAYS);

        Label dateLbl = new Label(note.getUpdatedAt().format(DateTimeFormatter.ofPattern("MMM dd HH:mm")));
        dateLbl.setStyle("-fx-text-fill:#666666; -fx-font-size:10px;");

        Button editBtn   = new Button("✏️"); editBtn.getStyleClass().add("btn-icon");
        Button deleteBtn = new Button("🗑");  deleteBtn.getStyleClass().add("btn-icon");

        editBtn.setOnAction(e -> showEditNoteDialog(note));
        deleteBtn.setOnAction(e -> { noteRepo.delete(note); reloadNotes(); });

        titleRow.getChildren().addAll(titleLbl, dateLbl, editBtn, deleteBtn);
        Label contentLbl = new Label(note.getContent());
        contentLbl.setWrapText(true);
        contentLbl.setStyle("-fx-text-fill:#C0C0C0;");
        card.getChildren().addAll(titleRow, contentLbl);
        return card;
    }

    private void showAddNoteDialog() {
        Dialog<Note> dialog = new Dialog<>();
        dialog.setTitle("New Week Note");
        dialog.setHeaderText("Add note for Week " + week.getWeekNumber());
        dialog.getDialogPane().getStylesheets().add(
            getClass().getResource("/css/dark-theme.css").toExternalForm());

        VBox form = new VBox(8);
        form.setPadding(new Insets(10));
        TextField titleField = new TextField();
        titleField.setPromptText("Title (optional)");
        TextArea contentArea = new TextArea();
        contentArea.setPromptText("Write your note here...");
        contentArea.setPrefRowCount(5);
        form.getChildren().addAll(new Label("Title:"), titleField, new Label("Content:"), contentArea);

        dialog.getDialogPane().setContent(form);
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);
        dialog.setResultConverter(btn -> {
            if (btn == ButtonType.OK && !contentArea.getText().trim().isEmpty())
                return new Note(week, titleField.getText().trim(), contentArea.getText().trim());
            return null;
        });
        dialog.showAndWait().ifPresent(n -> { noteRepo.save(n); reloadNotes(); });
    }

    private void showEditNoteDialog(Note note) {
        Dialog<Note> dialog = new Dialog<>();
        dialog.setTitle("Edit Note");
        dialog.getDialogPane().getStylesheets().add(
            getClass().getResource("/css/dark-theme.css").toExternalForm());

        VBox form = new VBox(8);
        form.setPadding(new Insets(10));
        TextField titleField = new TextField(note.getTitle() != null ? note.getTitle() : "");
        TextArea contentArea = new TextArea(note.getContent());
        contentArea.setPrefRowCount(5);
        form.getChildren().addAll(new Label("Title:"), titleField, new Label("Content:"), contentArea);

        dialog.getDialogPane().setContent(form);
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);
        dialog.setResultConverter(btn -> {
            if (btn == ButtonType.OK) {
                note.setTitle(titleField.getText().trim());
                note.updateContent(contentArea.getText().trim());
                return note;
            }
            return null;
        });
        dialog.showAndWait().ifPresent(n -> { noteRepo.save(n); reloadNotes(); });
    }

    // ── INFO PANE ────────────────────────────────────────────────
    private ScrollPane buildInfoPane() {
        VBox outer = new VBox(16);
        outer.setPadding(new Insets(20, 24, 20, 24));
        outer.getChildren().addAll(
            infoSection("📚 Topics",       week.getTopics()),
            infoSection("🛠️ Practice",     week.getPractice()),
            infoSection("🎯 Goal",          week.getGoal()),
            infoSection("⏰ Daily Hours",
                "Weekdays: " + week.getWeekdayHours() + "\nWeekends: " + week.getWeekendHours())
        );
        ScrollPane sp = new ScrollPane(outer);
        sp.setFitToWidth(true);
        sp.getStyleClass().add("scroll-pane");
        return sp;
    }

    private VBox infoSection(String title, String content) {
        VBox section = new VBox(8);
        section.getStyleClass().add("card");
        Label titleLbl = new Label(title);
        titleLbl.setStyle("-fx-font-weight:bold; -fx-font-size:13px; -fx-text-fill:#4FC3F7;");
        Label contentLbl = new Label(content != null ? content : "—");
        contentLbl.setWrapText(true);
        contentLbl.setStyle("-fx-text-fill:#C0C0C0; -fx-line-spacing:3;");
        section.getChildren().addAll(titleLbl, contentLbl);
        return section;
    }

    // ── EDIT DATES ───────────────────────────────────────────────
    private void showEditDatesDialog() {
        Dialog<Void> dialog = new Dialog<>();
        dialog.setTitle("Edit Week Dates");
        dialog.setHeaderText("Week " + week.getWeekNumber() + " — " + week.getTitle());
        dialog.getDialogPane().getStylesheets().add(
            getClass().getResource("/css/dark-theme.css").toExternalForm());

        VBox form = new VBox(10);
        form.setPadding(new Insets(10));
        DatePicker startPicker = new DatePicker(week.getStartDate());
        DatePicker endPicker   = new DatePicker(week.getEndDate());
        form.getChildren().addAll(new Label("Start Date:"), startPicker,
                                  new Label("End Date:"),   endPicker);
        dialog.getDialogPane().setContent(form);
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);
        dialog.showAndWait();

        if (startPicker.getValue() != null) week.setStartDate(startPicker.getValue());
        if (endPicker.getValue()   != null) week.setEndDate(endPicker.getValue());
        weekRepo.update(week);
        mainView.refreshSidebar();
        getChildren().clear();
        buildUI();
    }
}

package com.roadmap.app.ui.views;

import com.roadmap.app.model.*;
import com.roadmap.app.repository.*;
import com.roadmap.app.service.ReminderService;
import com.roadmap.app.ui.components.*;
import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.util.Duration;

import java.time.format.DateTimeFormatter;
import java.util.List;

public class MainView extends BorderPane {

    private final WeekRepository  weekRepo       = new WeekRepository();
    private final TaskRepository  taskRepo       = new TaskRepository();
    private final ReminderService reminderService = new ReminderService();

    private VBox      sidebarWeekList;
    private VBox      sidebarNode;          // the full sidebar VBox
    private StackPane contentArea;
    private Label     totalTasksValue, completedTasksValue, progressValue, weekCountValue;
    private WeekDetailView currentDetailView;
    private VBox      selectedWeekItem;

    // sidebar toggle state
    private boolean sidebarVisible = true;
    private static final double SIDEBAR_WIDTH   = 260;
    private static final double SIDEBAR_HIDDEN  = 0;

    public MainView() {
        getStyleClass().add("root");
        buildUI();
        startReminderService();
    }

    private void buildUI() {
        setCenter(buildCenter());   // must be first — sidebar auto-selects week 1
        setLeft(buildSidebar());
        refreshStats();
    }

    // ════════════════════════════════════════════════════════════
    //  SIDEBAR
    // ════════════════════════════════════════════════════════════
    private VBox buildSidebar() {
        sidebarNode = new VBox();
        sidebarNode.getStyleClass().add("sidebar");
        sidebarNode.setPrefWidth(SIDEBAR_WIDTH);
        sidebarNode.setMinWidth(SIDEBAR_WIDTH);
        sidebarNode.setMaxWidth(SIDEBAR_WIDTH);

        // App header
        VBox header = new VBox(4);
        header.getStyleClass().add("sidebar-header");
        Label appTitle = new Label("🚀 Roadmap Tracker");
        appTitle.setStyle("-fx-font-size:15px; -fx-font-weight:bold; -fx-text-fill:#4FC3F7;");
        Label appSub = new Label("Java Spring Boot — 90 Days");
        appSub.getStyleClass().add("label-secondary");
        header.getChildren().addAll(appTitle, appSub);

        Label sectionLbl = new Label("WEEKS");
        sectionLbl.getStyleClass().add("sidebar-section-label");

        sidebarWeekList = new VBox();
        ScrollPane sp = new ScrollPane(sidebarWeekList);
        sp.setFitToWidth(true);
        sp.getStyleClass().add("scroll-pane");
        VBox.setVgrow(sp, Priority.ALWAYS);

        loadSidebarWeeks();
        sidebarNode.getChildren().addAll(header, sectionLbl, sp);
        return sidebarNode;
    }

    public void loadSidebarWeeks() {
        sidebarWeekList.getChildren().clear();
        List<Week> weeks = weekRepo.findAll();

        String currentPhase = "";
        for (Week w : weeks) {
            if (!w.getMonthPhase().equals(currentPhase)) {
                currentPhase = w.getMonthPhase();
                Label phaseLabel = new Label(currentPhase.toUpperCase());
                phaseLabel.getStyleClass().add("sidebar-section-label");
                sidebarWeekList.getChildren().add(phaseLabel);
            }
            sidebarWeekList.getChildren().add(buildWeekSidebarItem(w));
        }

        // Auto-select first week
        for (var node : sidebarWeekList.getChildren()) {
            if (node instanceof VBox vb && vb.getUserData() instanceof Week) {
                selectWeekItem(vb, (Week) vb.getUserData());
                break;
            }
        }
    }

    private VBox buildWeekSidebarItem(Week week) {
        VBox item = new VBox(3);
        item.getStyleClass().add("week-item");
        item.setUserData(week);

        double prog = week.getProgress();
        String progStr = String.format("%.0f%%", prog * 100);

        HBox topRow = new HBox(6);
        topRow.setAlignment(Pos.CENTER_LEFT);

        Circle dot = new Circle(5, Color.web(week.getMonthColor()));

        Label numLbl = new Label("W" + week.getWeekNumber());
        numLbl.setStyle("-fx-font-size:10px; -fx-text-fill:#777777; -fx-min-width:22px;");

        Label titleLbl = new Label(week.getTitle());
        titleLbl.getStyleClass().add("week-title-label");
        titleLbl.setStyle("-fx-text-fill:#E0E0E0;");
        HBox.setHgrow(titleLbl, Priority.ALWAYS);
        titleLbl.setMaxWidth(Double.MAX_VALUE);

        // % right next to title, color changes when complete
        Label progLbl = new Label(progStr);
        progLbl.setStyle(prog == 1.0
            ? "-fx-font-size:10px; -fx-text-fill:#66BB6A; -fx-font-weight:bold;"
            : "-fx-font-size:10px; -fx-text-fill:#4FC3F7;");

        topRow.getChildren().addAll(dot, numLbl, titleLbl, progLbl);

        // Date row only — no progress bar
        HBox bottomRow = new HBox();
        bottomRow.setAlignment(Pos.CENTER_LEFT);
        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("MMM dd");
        String dateRange = (week.getStartDate() != null && week.getEndDate() != null)
            ? week.getStartDate().format(fmt) + " – " + week.getEndDate().format(fmt) : "";
        Label datesLbl = new Label(dateRange);
        datesLbl.getStyleClass().add("week-dates-label");
        bottomRow.getChildren().add(datesLbl);

        item.getChildren().addAll(topRow, bottomRow);
        item.setOnMouseClicked(e -> selectWeekItem(item, week));
        return item;
    }

    private void selectWeekItem(VBox item, Week week) {
        if (selectedWeekItem != null) selectedWeekItem.getStyleClass().remove("selected");
        selectedWeekItem = item;
        item.getStyleClass().add("selected");
        showWeekDetail(week);
    }

    // ════════════════════════════════════════════════════════════
    //  CENTER
    // ════════════════════════════════════════════════════════════
    private StackPane buildCenter() {
        VBox centerBox = new VBox();
        centerBox.getStyleClass().add("root");
        centerBox.getChildren().add(buildStatsBar());

        contentArea = new StackPane();
        contentArea.getStyleClass().add("root");
        VBox.setVgrow(contentArea, Priority.ALWAYS);
        centerBox.getChildren().add(contentArea);

        return new StackPane(centerBox);
    }

    private HBox buildStatsBar() {
        HBox bar = new HBox(12);
        bar.getStyleClass().add("stats-bar");
        bar.setAlignment(Pos.CENTER_LEFT);

        // ── Sidebar toggle button ← IMPROVEMENT #4 ───────────────
        Button toggleBtn = new Button("◀");
        toggleBtn.getStyleClass().addAll("button", "btn-sidebar-toggle");
        toggleBtn.setTooltip(new Tooltip("Collapse / expand sidebar"));
        toggleBtn.setOnAction(e -> toggleSidebar(toggleBtn));

        totalTasksValue     = new Label("…");
        completedTasksValue = new Label("…");
        progressValue       = new Label("…");
        weekCountValue      = new Label("12");

        bar.getChildren().addAll(
            toggleBtn,
            statBox("📋", "Total Tasks",  totalTasksValue),
            statBox("✅", "Completed",    completedTasksValue),
            statBox("📈", "Overall",      progressValue),
            statBox("📅", "Weeks",        weekCountValue)
        );
        return bar;
    }

    // ── Sidebar slide animation ───────────────────────────────────
    private void toggleSidebar(Button toggleBtn) {
        sidebarVisible = !sidebarVisible;
        double targetWidth = sidebarVisible ? SIDEBAR_WIDTH : SIDEBAR_HIDDEN;
        toggleBtn.setText(sidebarVisible ? "◀" : "▶");
        toggleBtn.setTooltip(new Tooltip(sidebarVisible ? "Collapse sidebar" : "Expand sidebar"));

        Timeline anim = new Timeline(
            new KeyFrame(Duration.millis(200),
                new KeyValue(sidebarNode.prefWidthProperty(), targetWidth),
                new KeyValue(sidebarNode.minWidthProperty(), targetWidth),
                new KeyValue(sidebarNode.maxWidthProperty(), targetWidth),
                new KeyValue(sidebarNode.opacityProperty(), sidebarVisible ? 1.0 : 0.0)
            )
        );
        anim.play();
        // After hiding, set managed=false so it takes no space
        if (!sidebarVisible) {
            anim.setOnFinished(ev -> {
                sidebarNode.setVisible(false);
                sidebarNode.setManaged(false);
            });
        } else {
            sidebarNode.setVisible(true);
            sidebarNode.setManaged(true);
        }
    }

    private VBox statBox(String icon, String label, Label valueLabel) {
        VBox box = new VBox(2);
        box.getStyleClass().add("stat-box");
        box.setAlignment(Pos.CENTER);
        Label iconLbl = new Label(icon + "  " + label);
        iconLbl.getStyleClass().add("stat-label");
        valueLabel.getStyleClass().add("stat-value");
        box.getChildren().addAll(iconLbl, valueLabel);
        return box;
    }

    private void showWeekDetail(Week week) {
        contentArea.getChildren().clear();
        currentDetailView = new WeekDetailView(week, this);
        contentArea.getChildren().add(currentDetailView);
    }

    // ── Stats ─────────────────────────────────────────────────────
    public void refreshStats() {
        long total     = taskRepo.countTotal();
        long completed = taskRepo.countCompleted();
        double pct     = total > 0 ? (double) completed / total : 0;
        totalTasksValue.setText(String.valueOf(total));
        completedTasksValue.setText(String.valueOf(completed));
        progressValue.setText(String.format("%.0f%%", pct * 100));
    }

    public void refreshSidebar() {
        Object sel = selectedWeekItem != null ? selectedWeekItem.getUserData() : null;
        loadSidebarWeeks();
        if (sel instanceof Week sw) {
            for (var node : sidebarWeekList.getChildren()) {
                if (node instanceof VBox vb && vb.getUserData() instanceof Week w
                        && w.getId().equals(sw.getId())) {
                    selectWeekItem(vb, w);
                    break;
                }
            }
        }
        refreshStats();
    }

    // ── Reminders ─────────────────────────────────────────────────
    private void startReminderService() {
        reminderService.start(this::showReminderAlert);
    }

    private void showReminderAlert(Reminder reminder) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("⏰ Reminder");
        alert.setHeaderText("Task: " + reminder.getTask().getTitle());
        alert.setContentText(reminder.getMessage());
        alert.getDialogPane().getStylesheets().add(
            getClass().getResource("/css/dark-theme.css").toExternalForm());
        alert.showAndWait();
    }

    public void stop() { reminderService.stop(); }
}

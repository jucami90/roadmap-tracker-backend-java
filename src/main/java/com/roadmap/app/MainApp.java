package com.roadmap.app;

import com.roadmap.app.service.DataSeeder;
import com.roadmap.app.ui.views.MainView;
import com.roadmap.app.util.JPAUtil;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Stage;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;

public class MainApp extends Application {

    private MainView mainView;

    @Override
    public void init() throws Exception {
        // ── macOS: set app name shown in the system menu bar ──────
        // Must be set BEFORE the JavaFX toolkit starts (init() runs on JavaFX launcher thread)
        System.setProperty("apple.awt.application.name", "Roadmap Tracker");
        System.setProperty("com.apple.mrj.application.apple.menu.about.name", "Roadmap Tracker");

        JPAUtil.getEntityManagerFactory();
        new DataSeeder().seedIfEmpty();
    }

    @Override
    public void start(Stage stage) {
        mainView = new MainView();

        // ── MenuBar — use system menu bar on macOS ────────────────
        MenuBar menuBar = buildMenuBar(stage);
        menuBar.setUseSystemMenuBar(true);   // ← moves it to the macOS top bar

        VBox root = new VBox(menuBar, mainView);
        VBox.setVgrow(mainView, Priority.ALWAYS);

        Scene scene = new Scene(root, 1240, 760);
        scene.getStylesheets().add(
            getClass().getResource("/css/dark-theme.css").toExternalForm());

        stage.setTitle("Roadmap Tracker");
        stage.setScene(scene);
        stage.setMinWidth(900);
        stage.setMinHeight(600);

        stage.setOnCloseRequest(e -> {
            mainView.stop();
            JPAUtil.close();
            Platform.exit();
        });

        stage.show();
    }

    // ── MENU BAR ─────────────────────────────────────────────────
    private MenuBar buildMenuBar(Stage stage) {
        MenuBar bar = new MenuBar();
        bar.setStyle("-fx-background-color:#141414; -fx-border-color:transparent transparent #333 transparent;");

        // ── File ──────────────────────────────────────────────────
        Menu fileMenu = new Menu("File");
        MenuItem exitItem = new MenuItem("Quit Roadmap Tracker");
        exitItem.setAccelerator(javafx.scene.input.KeyCombination.keyCombination("Shortcut+Q"));
        exitItem.setOnAction(e -> { mainView.stop(); JPAUtil.close(); Platform.exit(); });
        fileMenu.getItems().add(exitItem);

        // ── Help ──────────────────────────────────────────────────
        Menu helpMenu = new Menu("Help");

        MenuItem howToItem = new MenuItem("How to Use Roadmap Tracker");
        howToItem.setOnAction(e -> showHelpFile(stage));

        MenuItem aboutItem = new MenuItem("About");
        aboutItem.setOnAction(e -> showAboutDialog(stage));

        helpMenu.getItems().addAll(howToItem, new SeparatorMenuItem(), aboutItem);

        bar.getMenus().addAll(fileMenu, helpMenu);
        return bar;
    }

    // ── HELP FILE DIALOG ─────────────────────────────────────────
    private void showHelpFile(Stage owner) {
        String content = loadHelpText();

        Stage helpStage = new Stage();
        helpStage.initOwner(owner);
        helpStage.setTitle("Roadmap Tracker — Help");

        TextArea ta = new TextArea(content);
        ta.setEditable(false);
        ta.setWrapText(false);
        ta.setStyle(
            "-fx-font-family:'Menlo','Consolas','Monospace';" +
            "-fx-font-size:12px;" +
            "-fx-background-color:#1A1A1A;" +
            "-fx-text-fill:#C0C0C0;");

        VBox root = new VBox(ta);
        VBox.setVgrow(ta, Priority.ALWAYS);

        Scene scene = new Scene(root, 660, 640);
        scene.getStylesheets().add(
            getClass().getResource("/css/dark-theme.css").toExternalForm());

        helpStage.setScene(scene);
        helpStage.show();
    }

    private String loadHelpText() {
        try (InputStream is = getClass().getResourceAsStream("/help.txt")) {
            if (is == null) return "Help file not found.";
            return new String(is.readAllBytes(), StandardCharsets.UTF_8);
        } catch (Exception e) {
            return "Could not load help file: " + e.getMessage();
        }
    }

    // ── ABOUT DIALOG ─────────────────────────────────────────────
    private void showAboutDialog(Stage owner) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.initOwner(owner);
        alert.setTitle("About Roadmap Tracker");
        alert.setHeaderText("🚀  Roadmap Tracker  v1.0");
        alert.getDialogPane().getStylesheets().add(
            getClass().getResource("/css/dark-theme.css").toExternalForm());
        alert.setContentText("""
            A personal study agenda, checklist & reminder app
            pre-loaded with the 90-day Java Spring Boot Backend
            Engineer roadmap (June 3 – September 1, 2026).

            Stack:
              • JavaFX 21       — Desktop UI
              • Hibernate 6     — ORM / JPA
              • H2 Embedded DB  — Local persistence
              • Maven           — Build tool

            Data: ~/.roadmap-tracker/data/roadmapdb
            """);
        alert.showAndWait();
    }

    public static void main(String[] args) {
        launch(args);
    }
}

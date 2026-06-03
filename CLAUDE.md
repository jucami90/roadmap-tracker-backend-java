# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Build & Run

```bash
# Run in development (recommended)
mvn clean javafx:run

# Build fat JAR and run
mvn clean package -DskipTests
java -jar target/roadmap-app-1.0.0.jar

# Compile only
mvn compile
```

There are no tests in this project. The `javafx:run` approach is preferred during development; the fat-JAR path can have module issues.

To reset all data (wipes the embedded H2 database):
```bash
rm -rf ~/.roadmap-tracker/
```

## Architecture

This is a **JavaFX 21 desktop app** using Hibernate 6 + H2 embedded database. The stack is purely Java — no Spring, no web layer.

### Data flow

```
MainApp (entry point)
  └─ JPAUtil           — singleton EntityManagerFactory, H2 at ~/.roadmap-tracker/data/roadmapdb
  └─ DataSeeder        — seeds 12 weeks on first launch (skips if Week count > 0)
  └─ MainView          — root BorderPane: sidebar (week list) + content area + stats bar
       └─ WeekDetailView — tabs: Tasks / Notes / Info; owns TaskCard list
            └─ TaskCard  — single task row with inline subtask expansion
```

### Persistence pattern

All repositories (`WeekRepository`, `TaskRepository`, `NoteRepository`, `ReminderRepository`) call `JPAUtil.getEntityManager()` directly — there is no shared transaction scope across repositories. Each repository method opens, uses, and closes its own `EntityManager`.

Hibernate `hbm2ddl.auto=update` manages schema migrations automatically. For additive column changes (e.g., new nullable fields), update the entity and let Hibernate handle it. For non-null columns on existing rows, add a migration in `JPAUtil.migrateNullBooleans()` using native SQL.

### UI threading rule

All database work triggered by UI events happens on the JavaFX Application Thread (same thread). `ReminderService` polls on a background daemon thread and uses `Platform.runLater()` to deliver callbacks to the UI thread.

### Module system

The app uses Java modules (`module-info.java`). When adding a new package that needs Hibernate reflection or JavaFX property binding, add the appropriate `opens` declaration to `module-info.java`.

### Key enums

- `Task.Priority`: `LOW`, `MEDIUM`, `HIGH`
- `Task.Category`: `DSA`, `JAVA`, `SPRING_BOOT`, `SQL`, `SYSTEM_DESIGN`, `PROJECT`, `INTERVIEW`, `OTHER`
- `Reminder.ReminderStatus`: `PENDING`, `TRIGGERED`

### Styling

All visual styling lives in `src/main/resources/css/dark-theme.css`. Style classes are applied via `getStyleClass().add(...)` in Java code — there are no FXML files.
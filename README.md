# 🚀 Backend Roadmap Tracker
### JavaFX + H2 + Hibernate — Java Spring Boot 90-Day Study App

---

## 📋 What this app does

A desktop agenda/checklist/reminder app **pre-loaded with your complete 12-week Java Spring Boot backend roadmap** (June 3 – September 1, 2026).

### Features
- ✅ **12 Weeks pre-seeded** with all tasks from the roadmap
- 📋 **Task management** — add, edit, delete, reorder tasks per week
- 🔽 **Subtasks** — expand any task and add unlimited subtasks
- 📈 **Progress tracking** — per-task, per-week, and overall progress bars
- 📅 **Editable dates** — change any week's start/end date
- 📝 **Notes** — add rich notes per week (blockers, learnings, links)
- ⏰ **Reminders** — set date+time reminders for any task (polls every 60s)
- 🎨 **Dark theme** — modern dark UI color-coded by month phase
- 💾 **H2 embedded DB** — all data saved locally at `~/.roadmap-tracker/data/`

---

## 🛠️ Requirements

| Tool    | Version      |
|---------|--------------|
| Java    | 21 LTS+      |
| Maven   | 3.8+         |
| JavaFX  | Bundled via Maven |

---

## 🚀 How to run

### Option 1 — Maven JavaFX Plugin (recommended for development)
```bash
cd roadmap-app
mvn clean javafx:run
```

### Option 2 — Build and run fat JAR
```bash
mvn clean package -DskipTests
java -jar target/roadmap-app-1.0.0.jar
```

> ⚠️ If you get module errors with the fat JAR, use Option 1 during development.

---

## 📁 Project structure

```
roadmap-app/
├── pom.xml
└── src/main/
    ├── java/
    │   ├── module-info.java
    │   └── com/roadmap/app/
    │       ├── MainApp.java               ← Entry point
    │       ├── model/
    │       │   ├── Week.java
    │       │   ├── Task.java
    │       │   ├── SubTask.java
    │       │   ├── Note.java
    │       │   └── Reminder.java
    │       ├── repository/
    │       │   ├── WeekRepository.java
    │       │   ├── TaskRepository.java
    │       │   ├── NoteRepository.java
    │       │   └── ReminderRepository.java
    │       ├── service/
    │       │   ├── DataSeeder.java        ← Seeds 12 weeks on first launch
    │       │   └── ReminderService.java   ← Background reminder polling
    │       └── ui/
    │           ├── views/
    │           │   ├── MainView.java      ← Sidebar + stats bar layout
    │           │   └── WeekDetailView.java← Week detail with tabs
    │           └── components/
    │               └── TaskCard.java      ← Task row with subtasks
    └── resources/
        ├── META-INF/persistence.xml       ← JPA/Hibernate config
        └── css/dark-theme.css             ← Full dark theme
```

---

## 💾 Database

- Engine: **H2 embedded** (file-based, no setup needed)
- Location: `~/.roadmap-tracker/data/roadmapdb.*`
- Auto-created on first launch
- Schema auto-managed by Hibernate (`hbm2ddl.auto=update`)

To reset all data (fresh start):
```bash
rm -rf ~/.roadmap-tracker/
```

---

## 🎯 How to use

1. **Left sidebar** — shows all 12 weeks grouped by month. Click any week to open it.
2. **Tasks tab** — see all tasks for the week. Check ✅ to mark complete.
3. **Expand subtasks** — click `▶ 0/0 sub` button on any task.
4. **Add subtask** — type in the "Add subtask..." field inside expanded task.
5. **Edit task** — click ✏️ to change title, due date, category, priority.
6. **Set reminder** — click ⏰ to set a date+time alert for a task.
7. **Notes tab** — add freeform notes per week (blockers, resources, learnings).
8. **Info tab** — read the topics, practice tasks and goal for that week.
9. **Edit dates** — click "✏️ Edit Dates" in the week header to change dates.
10. **Stats bar** — top bar always shows total tasks, completed, and overall %.

---

## 🏗️ Tech Stack

| Layer      | Technology          |
|------------|---------------------|
| UI         | JavaFX 21           |
| ORM        | Hibernate 6 + JPA 3 |
| Database   | H2 2.x (embedded)   |
| Build      | Maven               |
| Java       | 21 LTS              |

---

*Built for your 90-day Java Spring Boot Backend Engineer roadmap — June 3 to September 1, 2026.*

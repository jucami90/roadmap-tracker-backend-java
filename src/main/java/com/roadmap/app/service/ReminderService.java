package com.roadmap.app.service;

import com.roadmap.app.model.Reminder;
import com.roadmap.app.repository.ReminderRepository;
import javafx.application.Platform;

import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

public class ReminderService {

    private final ReminderRepository repo = new ReminderRepository();
    private final ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor(r -> {
        Thread t = new Thread(r, "reminder-poller");
        t.setDaemon(true);
        return t;
    });

    private Consumer<Reminder> onReminderDue;

    public void start(Consumer<Reminder> callback) {
        this.onReminderDue = callback;
        scheduler.scheduleAtFixedRate(this::checkReminders, 0, 60, TimeUnit.SECONDS);
    }

    private void checkReminders() {
        try {
            List<Reminder> due = repo.findDueReminders();
            for (Reminder r : due) {
                r.setStatus(Reminder.ReminderStatus.TRIGGERED);
                repo.save(r);
                if (onReminderDue != null) {
                    Platform.runLater(() -> onReminderDue.accept(r));
                }
            }
        } catch (Exception e) {
            System.err.println("[ReminderService] Error checking reminders: " + e.getMessage());
        }
    }

    public void stop() {
        scheduler.shutdown();
    }
}

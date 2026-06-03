package com.roadmap.app.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "reminders")
public class Reminder {

    public enum ReminderStatus { PENDING, TRIGGERED, DISMISSED }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "task_id")
    private Task task;

    @Column(nullable = false)
    private String message;

    @Column(nullable = false)
    private LocalDateTime reminderDateTime;

    @Enumerated(EnumType.STRING)
    @Column
    private ReminderStatus status = ReminderStatus.PENDING;

    @Column
    private LocalDateTime createdAt;

    @Column(columnDefinition = "BOOLEAN DEFAULT FALSE")
    private Boolean addToCalendar = Boolean.FALSE;

    public Reminder() {
        this.createdAt = LocalDateTime.now();
    }

    public Reminder(Task task, String message, LocalDateTime reminderDateTime) {
        this();
        this.task             = task;
        this.message          = message;
        this.reminderDateTime = reminderDateTime;
    }

    public boolean isDue() {
        return status == ReminderStatus.PENDING &&
               LocalDateTime.now().isAfter(reminderDateTime);
    }

    public Long getId()                            { return id; }
    public Task getTask()                          { return task; }
    public void setTask(Task t)                    { this.task = t; }
    public String getMessage()                     { return message; }
    public void setMessage(String m)               { this.message = m; }
    public LocalDateTime getReminderDateTime()     { return reminderDateTime; }
    public void setReminderDateTime(LocalDateTime d){ this.reminderDateTime = d; }
    public ReminderStatus getStatus()              { return status; }
    public void setStatus(ReminderStatus s)        { this.status = s; }
    public LocalDateTime getCreatedAt()            { return createdAt; }
    public boolean isAddToCalendar()              { return addToCalendar != null && addToCalendar; }
    public void setAddToCalendar(Boolean b)        { this.addToCalendar = b != null ? b : Boolean.FALSE; }
}

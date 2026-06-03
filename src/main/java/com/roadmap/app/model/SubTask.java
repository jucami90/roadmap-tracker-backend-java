package com.roadmap.app.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "subtasks")
public class SubTask {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "task_id", nullable = false)
    private Task task;

    @Column(nullable = false)
    private String title;

    @Column(length = 1000)
    private String notes;

    @Column
    private boolean completed = false;

    @Column
    private LocalDateTime createdAt;

    @Column
    private LocalDateTime completedAt;

    @Column
    private int orderIndex = 0;

    public SubTask() {
        this.createdAt = LocalDateTime.now();
    }

    public SubTask(Task task, String title) {
        this();
        this.task  = task;
        this.title = title;
    }

    public void markCompleted() {
        this.completed   = true;
        this.completedAt = LocalDateTime.now();
    }

    public void markIncomplete() {
        this.completed   = false;
        this.completedAt = null;
    }

    // ── getters & setters ────────────────────────────────────────
    public Long getId()                         { return id; }
    public Task getTask()                       { return task; }
    public void setTask(Task t)                 { this.task = t; }
    public String getTitle()                    { return title; }
    public void setTitle(String t)              { this.title = t; }
    public String getNotes()                    { return notes; }
    public void setNotes(String n)              { this.notes = n; }
    public boolean isCompleted()                { return completed; }
    public void setCompleted(boolean c)         { this.completed = c; }
    public LocalDateTime getCreatedAt()         { return createdAt; }
    public LocalDateTime getCompletedAt()       { return completedAt; }
    public int getOrderIndex()                  { return orderIndex; }
    public void setOrderIndex(int i)            { this.orderIndex = i; }

    @Override
    public String toString() { return title; }
}

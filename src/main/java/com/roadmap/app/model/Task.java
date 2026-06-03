package com.roadmap.app.model;

import jakarta.persistence.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "tasks")
public class Task {

    public enum Priority { LOW, MEDIUM, HIGH }
    public enum Category { DSA, JAVA, SPRING_BOOT, SQL, SYSTEM_DESIGN, PROJECT, INTERVIEW, OTHER }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "week_id", nullable = false)
    private Week week;

    @Column(nullable = false)
    private String title;

    @Column(length = 2000)
    private String description;

    @Column
    private boolean completed = false;

    @Column
    private LocalDate dueDate;

    @Column
    private LocalDate completedDate;

    @Column
    private LocalDateTime createdAt;

    @Enumerated(EnumType.STRING)
    @Column
    private Priority priority = Priority.MEDIUM;

    @Enumerated(EnumType.STRING)
    @Column
    private Category category = Category.OTHER;

    @Column
    private int orderIndex = 0;

    @OneToMany(mappedBy = "task", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @OrderBy("orderIndex ASC")
    private List<SubTask> subTasks = new ArrayList<>();

    @OneToMany(mappedBy = "task", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<Reminder> reminders = new ArrayList<>();

    // ── constructors ────────────────────────────────────────────
    public Task() {
        this.createdAt = LocalDateTime.now();
    }

    public Task(Week week, String title, Category category, Priority priority) {
        this();
        this.week     = week;
        this.title    = title;
        this.category = category;
        this.priority = priority;
    }

    // ── progress ─────────────────────────────────────────────────
    public double getSubTaskProgress() {
        if (subTasks.isEmpty()) return completed ? 1.0 : 0.0;
        long done = subTasks.stream().filter(SubTask::isCompleted).count();
        return (double) done / subTasks.size();
    }

    public int getCompletedSubTaskCount() {
        return (int) subTasks.stream().filter(SubTask::isCompleted).count();
    }

    public void markCompleted() {
        this.completed     = true;
        this.completedDate = LocalDate.now();
    }

    public void markIncomplete() {
        this.completed     = false;
        this.completedDate = null;
    }

    // ── getters & setters ────────────────────────────────────────
    public Long getId()                          { return id; }
    public Week getWeek()                        { return week; }
    public void setWeek(Week w)                  { this.week = w; }
    public String getTitle()                     { return title; }
    public void setTitle(String t)               { this.title = t; }
    public String getDescription()               { return description; }
    public void setDescription(String d)         { this.description = d; }
    public boolean isCompleted()                 { return completed; }
    public void setCompleted(boolean c)          { this.completed = c; }
    public LocalDate getDueDate()                { return dueDate; }
    public void setDueDate(LocalDate d)          { this.dueDate = d; }
    public LocalDate getCompletedDate()          { return completedDate; }
    public void setCompletedDate(LocalDate d)    { this.completedDate = d; }
    public LocalDateTime getCreatedAt()          { return createdAt; }
    public Priority getPriority()                { return priority; }
    public void setPriority(Priority p)          { this.priority = p; }
    public Category getCategory()                { return category; }
    public void setCategory(Category c)          { this.category = c; }
    public int getOrderIndex()                   { return orderIndex; }
    public void setOrderIndex(int i)             { this.orderIndex = i; }
    public List<SubTask> getSubTasks()           { return subTasks; }
    public List<Reminder> getReminders()         { return reminders; }

    @Override
    public String toString() { return title; }
}

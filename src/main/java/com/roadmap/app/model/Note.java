package com.roadmap.app.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "notes")
public class Note {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "week_id")
    private Week week;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "task_id")
    private Task task;

    @Column(nullable = false, length = 5000)
    private String content;

    @Column
    private String title;

    @Column
    private LocalDateTime createdAt;

    @Column
    private LocalDateTime updatedAt;

    public Note() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    public Note(Week week, String title, String content) {
        this();
        this.week    = week;
        this.title   = title;
        this.content = content;
    }

    /** Constructor for task-level notes (no week association). */
    public Note(Task task, String title, String content) {
        this();
        this.task    = task;
        this.title   = title;
        this.content = content;
    }

    public void updateContent(String newContent) {
        this.content   = newContent;
        this.updatedAt = LocalDateTime.now();
    }

    public Long getId()                      { return id; }
    public Week getWeek()                    { return week; }
    public void setWeek(Week w)              { this.week = w; }
    public Task getTask()                    { return task; }
    public void setTask(Task t)              { this.task = t; }
    public String getContent()               { return content; }
    public void setContent(String c)         { this.content = c; }
    public String getTitle()                 { return title; }
    public void setTitle(String t)           { this.title = t; }
    public LocalDateTime getCreatedAt()      { return createdAt; }
    public LocalDateTime getUpdatedAt()      { return updatedAt; }
    public void setUpdatedAt(LocalDateTime d){ this.updatedAt = d; }
}

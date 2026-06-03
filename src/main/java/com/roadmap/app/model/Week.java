package com.roadmap.app.model;

import jakarta.persistence.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "weeks")
public class Week {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private int weekNumber;

    @Column(nullable = false)
    private String title;

    @Column(nullable = false)
    private String monthPhase;       // "MONTH 1 – Foundations" etc.

    @Column(nullable = false)
    private String monthColor;       // hex color for UI

    @Column
    private LocalDate startDate;

    @Column
    private LocalDate endDate;

    @Column(length = 1000)
    private String topics;

    @Column(length = 1000)
    private String practice;

    @Column(length = 500)
    private String goal;

    @Column
    private String weekdayHours;

    @Column
    private String weekendHours;

    @OneToMany(mappedBy = "week", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @OrderBy("orderIndex ASC")
    private List<Task> tasks = new ArrayList<>();

    @OneToMany(mappedBy = "week", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<Note> notes = new ArrayList<>();

    // ── constructors ────────────────────────────────────────────
    public Week() {}

    public Week(int weekNumber, String title, String monthPhase, String monthColor,
                LocalDate startDate, LocalDate endDate,
                String topics, String practice, String goal,
                String weekdayHours, String weekendHours) {
        this.weekNumber   = weekNumber;
        this.title        = title;
        this.monthPhase   = monthPhase;
        this.monthColor   = monthColor;
        this.startDate    = startDate;
        this.endDate      = endDate;
        this.topics       = topics;
        this.practice     = practice;
        this.goal         = goal;
        this.weekdayHours = weekdayHours;
        this.weekendHours = weekendHours;
    }

    // ── progress calculation ─────────────────────────────────────
    public double getProgress() {
        if (tasks.isEmpty()) return 0.0;
        long done = tasks.stream().filter(Task::isCompleted).count();
        return (double) done / tasks.size();
    }

    public int getCompletedTaskCount() {
        return (int) tasks.stream().filter(Task::isCompleted).count();
    }

    // ── getters & setters ────────────────────────────────────────
    public Long getId()                         { return id; }
    public int getWeekNumber()                  { return weekNumber; }
    public void setWeekNumber(int n)            { this.weekNumber = n; }
    public String getTitle()                    { return title; }
    public void setTitle(String t)              { this.title = t; }
    public String getMonthPhase()               { return monthPhase; }
    public void setMonthPhase(String m)         { this.monthPhase = m; }
    public String getMonthColor()               { return monthColor; }
    public void setMonthColor(String c)         { this.monthColor = c; }
    public LocalDate getStartDate()             { return startDate; }
    public void setStartDate(LocalDate d)       { this.startDate = d; }
    public LocalDate getEndDate()               { return endDate; }
    public void setEndDate(LocalDate d)         { this.endDate = d; }
    public String getTopics()                   { return topics; }
    public void setTopics(String t)             { this.topics = t; }
    public String getPractice()                 { return practice; }
    public void setPractice(String p)           { this.practice = p; }
    public String getGoal()                     { return goal; }
    public void setGoal(String g)               { this.goal = g; }
    public String getWeekdayHours()             { return weekdayHours; }
    public void setWeekdayHours(String h)       { this.weekdayHours = h; }
    public String getWeekendHours()             { return weekendHours; }
    public void setWeekendHours(String h)       { this.weekendHours = h; }
    public List<Task> getTasks()                { return tasks; }
    public List<Note> getNotes()                { return notes; }

    @Override
    public String toString() {
        return "Week " + weekNumber + " – " + title;
    }
}

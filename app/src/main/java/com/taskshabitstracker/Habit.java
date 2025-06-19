package com.taskshabitstracker;

public class Habit {
    private String id;
    private String title;
    private String description;
    private int streak;

    public Habit(String id, String title, String description, int streak) {
        this.id = id;
        this.title = title;
        this.description = description;
        this.streak = streak;
    }

    // Getters
    public String getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public String getDescription() {
        return description;
    }

    public int getStreak() {
        return streak;
    }
}
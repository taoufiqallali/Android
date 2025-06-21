package com.taskshabitstracker.model;

public class Habit {
    private int id;
    private String name;
    private String description;
    private int streak;
    private boolean completedToday;

    // Constructor that matches your parsing method
    public Habit(int id, String name, String description, int streak, boolean completedToday) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.streak = streak;
        this.completedToday = completedToday;
    }

    // Constructor for creating new habits (without id and completion status)
    public Habit(String name, String description) {
        this.name = name;
        this.description = description;
        this.streak = 0;
        this.completedToday = false;
        this.id = -1; // indicating it's not yet saved to server
    }

    // Getters
    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    // Keep getTitle() for compatibility with your adapter
    public String getTitle() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public int getStreak() {
        return streak;
    }

    public boolean isCompletedToday() {
        return completedToday;
    }

    // Setters
    public void setId(int id) {
        this.id = id;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void setStreak(int streak) {
        this.streak = streak;
    }

    public void setCompletedToday(boolean completedToday) {
        this.completedToday = completedToday;
    }

    // Utility methods
    public void toggleCompletedToday() {
        this.completedToday = !this.completedToday;
        if (this.completedToday) {
            this.streak++;
        } else {
            // Optionally decrease streak when unchecking
            this.streak = Math.max(0, this.streak - 1);
        }
    }

    public void incrementStreak() {
        this.streak++;
    }

    public void resetStreak() {
        this.streak = 0;
    }

    @Override
    public String toString() {
        return "Habit{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", description='" + description + '\'' +
                ", streak=" + streak +
                ", completedToday=" + completedToday +
                '}';
    }
}
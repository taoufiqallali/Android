package com.taskshabitstracker.model;

public class Habit {
    private String id;
    private String name;
    private String description;
    private int streak;
    private boolean completedToday;

    public Habit(String id, String name, String description, int streak, boolean completedToday) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.streak = streak;
        this.completedToday = completedToday;
    }

    public Habit(String name, String description) {
        this.name = name;
        this.description = description;
        this.streak = 0;
        this.completedToday = false;
        this.id = null;
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

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

    public void setId(String id) {
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

    public void toggleCompletedToday() {
        this.completedToday = !this.completedToday;
        if (this.completedToday) {
            this.streak++;
        } else {
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
                "id='" + id + '\'' +
                ", name='" + name + '\'' +
                ", description='" + description + '\'' +
                ", streak=" + streak +
                ", completedToday=" + completedToday +
                '}';
    }
}
package com.taskshabitstracker.model;

public class Task {
    private int id;
    private String title;
    private String description;
    private boolean completed;

    // Constructor that matches your parsing method
    public Task(int id, String title, String description, boolean completed) {
        this.id = id;
        this.title = title;
        this.description = description;
        this.completed = completed;
    }

    // Constructor for creating new tasks (without id and completed status)
    public Task(String title, String description) {
        this.title = title;
        this.description = description;
        this.completed = false;
        this.id = -1; // or 0, indicating it's not yet saved to server
    }

    // Getters
    public int getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public String getDescription() {
        return description;
    }

    public boolean isCompleted() {
        return completed;
    }

    // Setters
    public void setId(int id) {
        this.id = id;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void setCompleted(boolean completed) {
        this.completed = completed;
    }

    // Toggle completion status
    public void toggleCompleted() {
        this.completed = !this.completed;
    }

    @Override
    public String toString() {
        return "Task{" +
                "id=" + id +
                ", title='" + title + '\'' +
                ", description='" + description + '\'' +
                ", completed=" + completed +
                '}';
    }
}
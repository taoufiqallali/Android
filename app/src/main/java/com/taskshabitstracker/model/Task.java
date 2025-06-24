package com.taskshabitstracker.model;

public class Task {
    private String id;
    private String title;
    private String description;
    private boolean completed;
    private String dueDate;

    // Constructor for creating new tasks
    public Task(String title, String description, String dueDate) {
        this.id = java.util.UUID.randomUUID().toString();
        this.title = title;
        this.description = description;
        this.completed = false;
        this.dueDate = dueDate;
    }

    // Constructor for parsing backend data
    public Task(String id, String title, String description, boolean completed, String dueDate) {
        this.id = id;
        this.title = title;
        this.description = description;
        this.completed = completed;
        this.dueDate = dueDate;
    }

    // Getters and setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public boolean isCompleted() { return completed; }
    public void setCompleted(boolean completed) { this.completed = completed; }
    public String getDueDate() { return dueDate; }
    public void setDueDate(String dueDate) { this.dueDate = dueDate; }

    @Override
    public String toString() {
        return "Task{" +
                "id='" + id + '\'' +
                ", title='" + title + '\'' +
                ", description='" + description + '\'' +
                ", completed=" + completed +
                ", dueDate='" + dueDate + '\'' +
                '}';
    }
}
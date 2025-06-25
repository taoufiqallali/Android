package com.taskshabitstracker.model;

public class Task {
    private String id;
    private String userId; // Associate task with a user
    private String title;
    private String description;
    private boolean completed;
    private String dueDate; // Changed to String for API 24 compatibility
    private boolean enableDueDateNotifications; // Added for notification preferences
    private boolean enablePreDueNotifications; // Added for pre-due (e.g., 1 day before) notifications

    // Constructor for creating new tasks
    public Task(String title, String description, String dueDate) {
        this.id = java.util.UUID.randomUUID().toString();
        this.title = title;
        this.description = description;
        this.completed = false;
        this.dueDate = dueDate;
        this.enableDueDateNotifications = true; // Default: enable due date notifications
        this.enablePreDueNotifications = true; // Default: enable pre-due notifications
    }

    // Constructor for parsing backend data
    public Task(String id, String userId, String title, String description, boolean completed, String dueDate,
                boolean enableDueDateNotifications, boolean enablePreDueNotifications) {
        this.id = id;
        this.userId = userId;
        this.title = title;
        this.description = description;
        this.completed = completed;
        this.dueDate = dueDate;
        this.enableDueDateNotifications = enableDueDateNotifications;
        this.enablePreDueNotifications = enablePreDueNotifications;
    }

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
    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public boolean isCompleted() { return completed; }
    public void setCompleted(boolean completed) { this.completed = completed; }
    public String getDueDate() { return dueDate; }
    public void setDueDate(String dueDate) { this.dueDate = dueDate; }
    public boolean isEnableDueDateNotifications() { return enableDueDateNotifications; }
    public void setEnableDueDateNotifications(boolean enableDueDateNotifications) {
        this.enableDueDateNotifications = enableDueDateNotifications;
    }
    public boolean isEnablePreDueNotifications() { return enablePreDueNotifications; }
    public void setEnablePreDueNotifications(boolean enablePreDueNotifications) {
        this.enablePreDueNotifications = enablePreDueNotifications;
    }

    @Override
    public String toString() {
        return "Task{" +
                "id='" + id + '\'' +
                ", userId='" + userId + '\'' +
                ", title='" + title + '\'' +
                ", description='" + description + '\'' +
                ", completed=" + completed +
                ", dueDate='" + dueDate + '\'' +
                ", enableDueDateNotifications=" + enableDueDateNotifications +
                ", enablePreDueNotifications=" + enablePreDueNotifications +
                '}';
    }
}
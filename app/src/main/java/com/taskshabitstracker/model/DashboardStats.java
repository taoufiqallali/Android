// DashboardStats.java - Model for dashboard statistics
package com.taskshabitstracker.model;

public class DashboardStats {
    private final int points;
    private final int streak;
    private final int completedTasks;
    private final int totalTasks;
    private final int completedHabits;
    private final int totalHabits;

    public DashboardStats(int points, int streak, int completedTasks, int totalTasks,
                          int completedHabits, int totalHabits) {
        this.points = points;
        this.streak = streak;
        this.completedTasks = completedTasks;
        this.totalTasks = totalTasks;
        this.completedHabits = completedHabits;
        this.totalHabits = totalHabits;
    }

    public int getPoints() { return points; }
    public int getStreak() { return streak; }
    public int getCompletedTasks() { return completedTasks; }
    public int getTotalTasks() { return totalTasks; }
    public int getCompletedHabits() { return completedHabits; }
    public int getTotalHabits() { return totalHabits; }
}
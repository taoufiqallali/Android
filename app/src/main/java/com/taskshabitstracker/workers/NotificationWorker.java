package com.taskshabitstracker.workers;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.os.Build;
import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

public class NotificationWorker extends Worker {
    private static final String CHANNEL_ID = "TASK_NOTIFICATIONS";
    private static final int NOTIFICATION_ID = 100;

    public NotificationWorker(@NonNull Context context, @NonNull WorkerParameters params) {
        super(context, params);
    }

    @NonNull
    @Override
    public Result doWork() {
        // Get input data from WorkManager
        String taskId = getInputData().getString("taskId");
        String taskTitle = getInputData().getString("taskTitle");
        String notificationType = getInputData().getString("notificationType");
        int points = getInputData().getInt("points", 0);

        // Create notification channel for Android O and above
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    CHANNEL_ID,
                    "Task Notifications",
                    NotificationManager.IMPORTANCE_DEFAULT);
            NotificationManager manager = getApplicationContext().getSystemService(NotificationManager.class);
            manager.createNotificationChannel(channel);
        }

        // Build notification based on type
        NotificationCompat.Builder builder = new NotificationCompat.Builder(getApplicationContext(), CHANNEL_ID)
                .setSmallIcon(android.R.drawable.ic_dialog_info)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT);

        switch (notificationType != null ? notificationType : "") {
            case "DUE_DATE":
                builder.setContentTitle("Task Due Today")
                        .setContentText("Task '" + taskTitle + "' is due today!");
                break;
            case "PRE_DUE":
                builder.setContentTitle("Task Due Tomorrow")
                        .setContentText("Task '" + taskTitle + "' is due tomorrow!");
                break;
            case "OVERDUE":
                builder.setContentTitle("Task Overdue")
                        .setContentText("Task '" + taskTitle + "' is overdue!");
                break;
            case "TASK_CREATED":
                builder.setContentTitle("Task Created")
                        .setContentText("Task '" + taskTitle + "' has been created.");
                break;
            case "POINTS_EARNED":
                builder.setContentTitle("Points Earned")
                        .setContentText("You earned " + points + " points for completing '" + taskTitle + "'!");
                break;
            default:
                return Result.failure(); // Invalid notification type
        }

        // Show notification
        NotificationManager manager = (NotificationManager) getApplicationContext().getSystemService(Context.NOTIFICATION_SERVICE);
        manager.notify(taskId.hashCode(), builder.build()); // Use taskId hash to avoid conflicts

        return Result.success();
    }
}
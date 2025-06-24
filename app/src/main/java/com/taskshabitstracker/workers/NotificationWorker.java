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
    private static final String CHANNEL_ID = "task_reminders";
    private static final int NOTIFICATION_ID = 1000;

    // Required no-argument constructor
    public NotificationWorker() {
        super(null, null);
    }

    public NotificationWorker(@NonNull Context context, @NonNull WorkerParameters params) {
        super(context, params);
    }

    @NonNull
    @Override
    public Result doWork() {
        String taskId = getInputData().getString("taskId");
        String taskTitle = getInputData().getString("taskTitle");

        if (taskId == null || taskTitle == null) {
            return Result.failure();
        }

        createNotificationChannel();
        sendNotification(taskTitle);

        return Result.success();
    }

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    CHANNEL_ID,
                    "Task Reminders",
                    NotificationManager.IMPORTANCE_DEFAULT
            );
            NotificationManager manager = getApplicationContext().getSystemService(NotificationManager.class);
            manager.createNotificationChannel(channel);
        }
    }

    private void sendNotification(String taskTitle) {
        NotificationCompat.Builder builder = new NotificationCompat.Builder(getApplicationContext(), CHANNEL_ID)
                .setSmallIcon(android.R.drawable.ic_dialog_alert) // Replace with ic_notification
                .setContentTitle("Task Deadline")
                .setContentText("Reminder: '" + taskTitle + "' is due today!")
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setAutoCancel(true);

        NotificationManager manager = (NotificationManager) getApplicationContext().getSystemService(Context.NOTIFICATION_SERVICE);
        manager.notify(NOTIFICATION_ID + taskTitle.hashCode(), builder.build());
    }
}
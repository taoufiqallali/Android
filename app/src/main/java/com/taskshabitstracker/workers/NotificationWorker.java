package com.taskshabitstracker.workers;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.os.Build;
import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import com.taskshabitstracker.R;
import com.taskshabitstracker.utils.NotificationHelper;

public class NotificationWorker extends Worker {
    private static final String CHANNEL_ID = "TASK_NOTIFICATIONS";
    private static final int NOTIFICATION_ID = 100;

    public NotificationWorker(@NonNull Context context, @NonNull WorkerParameters params) {
        super(context, params);
    }

    @NonNull
    @Override
    public Result doWork() {
        // Ensure notification channel is created
        NotificationHelper.createNotificationChannel(getApplicationContext());

        // Retrieve data
        String title = getInputData().getString("title");
        String content = getInputData().getString("content");

        // Send notification
        NotificationCompat.Builder builder = new NotificationCompat.Builder(getApplicationContext(), "my_channel_id")
                .setSmallIcon(R.drawable.ic_notification)
                .setContentTitle(title)
                .setContentText(content)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setAutoCancel(true);

        NotificationManagerCompat manager = NotificationManagerCompat.from(getApplicationContext());
        try {
            manager.notify((int) System.currentTimeMillis(), builder.build());
            return Result.success();
        } catch (SecurityException e) {
            e.printStackTrace();
            return Result.failure();
        }
    }
}

package com.taskshabitstracker.viewmodel;

import android.app.Application;
import android.content.SharedPreferences;
import android.util.Log;
import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.android.volley.Request;
import com.android.volley.toolbox.JsonObjectRequest;
import com.taskshabitstracker.model.Task;
import com.taskshabitstracker.repository.TasksRepository;
import com.taskshabitstracker.network.VolleySingleton;

import org.json.JSONException;
import org.json.JSONObject;
import androidx.work.Data;
import androidx.work.OneTimeWorkRequest;
import androidx.work.PeriodicWorkRequest;
import androidx.work.WorkManager;
import com.taskshabitstracker.workers.NotificationWorker;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.TimeUnit;
import java.util.HashMap;
import java.util.Map;

public class TasksViewModel extends AndroidViewModel {
    private static final String TAG = "TasksViewModel";
    private final TasksRepository repository;
    private final MutableLiveData<List<Task>> tasks = new MutableLiveData<>();
    private final MutableLiveData<Boolean> isLoading = new MutableLiveData<>(false);
    private final MutableLiveData<String> errorMessage = new MutableLiveData<>();
    private static final int POINTS_PER_COMPLETION = 10;
    private static final String USER_URL = "http://10.0.2.2:8080/api/users";
    private static final String TIMELINE_URL = "http://10.0.2.2:8080/api/timeline";
    private final SharedPreferences prefs;

    public TasksViewModel(@NonNull Application application) {
        super(application);
        repository = new TasksRepository(application);
        prefs = application.getSharedPreferences("TasksPrefs", Application.MODE_PRIVATE);
        // Schedule daily streak and inactivity checks
        scheduleStreakAndInactivityCheck();
        // Schedule weekly summary
        scheduleWeeklySummary();
    }

    public void loadTasks(String userId) {
        Log.d(TAG, "Loading tasks from repository for user: " + userId);
        isLoading.setValue(true);
        repository.getTasks(userId, new TasksRepository.OnTasksFetched() {
            @Override
            public void onSuccess(List<Task> taskList) {
                isLoading.setValue(false);
                tasks.setValue(new ArrayList<>(taskList));
                // Schedule overdue notifications
                //scheduleOverdueNotifications(taskList);
                Log.d(TAG, "Tasks loaded: " + taskList.size() + " tasks");
            }

            @Override
            public void onError(String error) {
                isLoading.setValue(false);
                errorMessage.setValue(error);
                Log.e(TAG, "Error loading tasks: " + error);
            }
        });
    }

    public void toggleTaskCompletion(Task task) throws JSONException {
        List<Task> currentTasks = tasks.getValue();
        boolean wasCompleted = task.isCompleted();

        // Update local state optimistically
        if (currentTasks != null) {
            List<Task> updatedTasks = new ArrayList<>(currentTasks);
            for (Task t : updatedTasks) {
                if (t.getId().equals(task.getId())) {
                    t.setCompleted(!t.isCompleted());
                    break;
                }
            }
            tasks.setValue(updatedTasks);
            Log.d(TAG, "Task completion toggled locally: " + task.getId() + " to " + !wasCompleted);
        }

        isLoading.setValue(true);
        repository.toggleTaskCompletion(task,
                () -> {
                    isLoading.setValue(false);
                    Log.d(TAG, "Task completion toggled on server: " + task.getId());

                    // If task was just completed, update points and check milestones
                    if (!wasCompleted) {
                        //updateUserPoints(POINTS_PER_COMPLETION, task.getTitle());
                        //updateLocalStreakAndMilestones(task.getTitle());
                        cancelNotification(task.getId());
                        addTimelineEvent(task.getId(), "COMPLETED", "Task '" + task.getTitle() + "' completed");
                    }
                },
                error -> {
                    // Revert local changes on error
                    if (currentTasks != null) {
                        List<Task> revertedTasks = new ArrayList<>(currentTasks);
                        for (Task t : revertedTasks) {
                            if (t.getId().equals(task.getId())) {
                                t.setCompleted(wasCompleted);
                                break;
                            }
                        }
                        tasks.setValue(revertedTasks);
                        Log.d(TAG, "Task completion reverted: " + task.getId() + " back to " + wasCompleted);
                    }
                    isLoading.setValue(false);
                    errorMessage.setValue(error);
                    Log.e(TAG, "Error toggling task: " + error);
                }
        );
    }

    private void updateUserPoints(int pointsToAdd, String taskTitle) {
        String url = USER_URL + "/addPoints";
        JSONObject jsonBody = new JSONObject();
        try {
            jsonBody.put("points", pointsToAdd);
            jsonBody.put("userId", prefs.getString("user_id", "default-user"));
        } catch (Exception e) {
            Log.e(TAG, "Error creating JSON for points update", e);
            errorMessage.setValue("Error updating points");
            return;
        }

        JsonObjectRequest request = new JsonObjectRequest(
                Request.Method.PUT,
                url,
                jsonBody,
                response -> {
                    Log.d(TAG, "Points updated: " + pointsToAdd);
                    // Schedule points earned notification
                    schedulePointsNotification(taskTitle, pointsToAdd);
                },
                error -> {
                    String errorMsg = "Failed to update points";
                    if (error.networkResponse != null) {
                        Log.e(TAG, "Points update error - Status code: " + error.networkResponse.statusCode);
                        errorMsg += " (Status code: " + error.networkResponse.statusCode + ")";
                        if (error.networkResponse.data != null) {
                            String responseBody = new String(error.networkResponse.data);
                            Log.e(TAG, "Points update error response body: " + responseBody);
                            errorMsg += " - " + responseBody;
                        }
                    } else {
                        Log.e(TAG, "Points update error: " + error.toString(), error);
                        errorMsg += ": " + error.getMessage();
                    }
                    errorMessage.setValue(errorMsg);
                }
        ) {
            @Override
            public Map<String, String> getHeaders() {
                Map<String, String> headers = new HashMap<>();
                headers.put("Content-Type", "application/json");
                return headers;
            }
        };

        VolleySingleton.getInstance(getApplication()).getRequestQueue().add(request);
    }

    private void addTimelineEvent(String taskId, String eventType, String description) {
        JSONObject jsonBody = new JSONObject();
        try {
            jsonBody.put("taskId", taskId);
            jsonBody.put("eventType", eventType);
            jsonBody.put("description", description);
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.getDefault());
            jsonBody.put("timestamp", sdf.format(new java.util.Date()));
        } catch (Exception e) {
            Log.e(TAG, "Error creating JSON for timeline event", e);
            return;
        }

        JsonObjectRequest request = new JsonObjectRequest(
                Request.Method.POST,
                TIMELINE_URL,
                jsonBody,
                response -> Log.d(TAG, "Timeline event added: " + eventType),
                error -> {
                    String errorMsg = "Failed to add timeline event";
                    if (error.networkResponse != null) {
                        Log.e(TAG, "Timeline event error - Status code: " + error.networkResponse.statusCode);
                        errorMsg += " (Status code: " + error.networkResponse.statusCode + ")";
                        if (error.networkResponse.data != null) {
                            String responseBody = new String(error.networkResponse.data);
                            Log.e(TAG, "Timeline event error response body: " + responseBody);
                            errorMsg += " - " + responseBody;
                        }
                    } else {
                        Log.e(TAG, "Timeline event error: " + error.toString(), error);
                        errorMsg += ": " + error.getMessage();
                    }
                    errorMessage.setValue(errorMsg);
                }
        ) {
            @Override
            public Map<String, String> getHeaders() {
                Map<String, String> headers = new HashMap<>();
                headers.put("Content-Type", "application/json");
                return headers;
            }
        };

        VolleySingleton.getInstance(getApplication()).getRequestQueue().add(request);
    }

    public void deleteTask(Task task, Runnable onSuccess, TasksRepository.OnErrorCallback onError,String userId) {
        List<Task> currentTasks = tasks.getValue();
        List<Task> updatedTasks = currentTasks != null ? new ArrayList<>(currentTasks) : new ArrayList<>();
        updatedTasks.removeIf(t -> t.getId().equals(task.getId()));
        tasks.setValue(updatedTasks);
        Log.d(TAG, "Task removed locally: " + task.getId());

        isLoading.setValue(true);
        repository.deleteTask(task,
                () -> {
                    isLoading.setValue(false);
                    onSuccess.run();
                    cancelNotification(task.getId());
                    addTimelineEvent(task.getId(), "DELETED", "Task '" + task.getTitle() + "' deleted");
                    Log.d(TAG, "Task deleted successfully: " + task.getId());
                    loadTasks(userId);
                },
                error -> {
                    updatedTasks.add(task);
                    tasks.setValue(new ArrayList<>(updatedTasks));
                    Log.d(TAG, "Task deletion reverted: " + task.getId());
                    isLoading.setValue(false);
                    errorMessage.setValue(error);
                    Log.e(TAG, "Error deleting task: " + error);
                }
        );
    }

    public void addTask(Task task,String userId) {
        isLoading.setValue(true);
        repository.addTask(task, new TasksRepository.OnTaskAdded() {
            @Override
            public void onTaskAdded(Task newTask) {
                List<Task> currentTasks = tasks.getValue();
                List<Task> updatedTasks = currentTasks != null ? new ArrayList<>(currentTasks) : new ArrayList<>();
                updatedTasks.add(newTask);
                tasks.setValue(updatedTasks);
                Log.d(TAG, "Task added locally: " + newTask.getId());
                isLoading.setValue(false);
                // Schedule task creation and due date notifications
                scheduleTaskCreatedNotification(newTask);
                scheduleDueDateNotifications(newTask);
                addTimelineEvent(newTask.getId(), "CREATED", "Task '" + newTask.getTitle() + "' created");
            }

            @Override
            public void onError(String error) {
                isLoading.setValue(false);
                errorMessage.setValue(error);
                Log.e(TAG, "Error adding task: " + error);
            }
        }, userId);
    }

    private void scheduleTaskCreatedNotification(Task task) {
        Data inputData = new Data.Builder()
                .putString("taskId", task.getId())
                .putString("taskTitle", task.getTitle())
                .putString("notificationType", "TASK_CREATED")
                .build();

        OneTimeWorkRequest notificationWork = new OneTimeWorkRequest.Builder(NotificationWorker.class)
                .setInputData(inputData)
                .addTag(task.getId())
                .build();

        WorkManager.getInstance(getApplication()).enqueue(notificationWork);
        Log.d(TAG, "Task creation notification scheduled for task: " + task.getId());
    }

    private void schedulePointsNotification(String taskTitle, int points) {
        Data inputData = new Data.Builder()
                .putString("taskTitle", taskTitle)
                .putInt("points", points)
                .putString("notificationType", "POINTS_EARNED")
                .build();

        OneTimeWorkRequest notificationWork = new OneTimeWorkRequest.Builder(NotificationWorker.class)
                .setInputData(inputData)
                .addTag("points_" + taskTitle)
                .build();

        WorkManager.getInstance(getApplication()).enqueue(notificationWork);
        Log.d(TAG, "Points notification scheduled for: " + points + " points");
    }

    private void scheduleDueDateNotifications(Task task) {
        String dueDateStr = task.getDueDate();
        if (dueDateStr == null || dueDateStr.isEmpty()) return;

        try {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
            java.util.Date dueDate = sdf.parse(dueDateStr);
            Calendar dueCalendar = Calendar.getInstance();
            dueCalendar.setTime(dueDate);
            dueCalendar.set(Calendar.HOUR_OF_DAY, 9);
            dueCalendar.set(Calendar.MINUTE, 0);
            dueCalendar.set(Calendar.SECOND, 0);
            dueCalendar.set(Calendar.MILLISECOND, 0);

            // Schedule due date notification
            if (task.isEnableDueDateNotifications()) {
                long delay = dueCalendar.getTimeInMillis() - System.currentTimeMillis();
                if (delay > 0) {
                    Data inputData = new Data.Builder()
                            .putString("taskId", task.getId())
                            .putString("taskTitle", task.getTitle())
                            .putString("notificationType", "DUE_DATE")
                            .build();

                    OneTimeWorkRequest notificationWork = new OneTimeWorkRequest.Builder(NotificationWorker.class)
                            .setInitialDelay(delay, TimeUnit.MILLISECONDS)
                            .setInputData(inputData)
                            .addTag(task.getId() + "_due")
                            .build();

                    WorkManager.getInstance(getApplication()).enqueue(notificationWork);
                    Log.d(TAG, "Due date notification scheduled for task: " + task.getId());
                }
            }

            // Schedule pre-due notification (1 day before)
            if (task.isEnablePreDueNotifications()) {
                dueCalendar.setTime(dueDate); // Reset calendar to due date
                dueCalendar.add(Calendar.DAY_OF_MONTH, -1);
                long preDueDelay = dueCalendar.getTimeInMillis() - System.currentTimeMillis();
                if (preDueDelay > 0) {
                    Data inputData = new Data.Builder()
                            .putString("taskId", task.getId())
                            .putString("taskTitle", task.getTitle())
                            .putString("notificationType", "PRE_DUE")
                            .build();

                    OneTimeWorkRequest notificationWork = new OneTimeWorkRequest.Builder(NotificationWorker.class)
                            .setInitialDelay(preDueDelay, TimeUnit.MILLISECONDS)
                            .setInputData(inputData)
                            .addTag(task.getId() + "_pre_due")
                            .build();

                    WorkManager.getInstance(getApplication()).enqueue(notificationWork);
                    Log.d(TAG, "Pre-due notification scheduled for task: " + task.getId());
                }
            }
        } catch (Exception e) {
            Log.e(TAG, "Error scheduling notifications for task: " + task.getId(), e);
        }
    }

    private void scheduleOverdueNotifications(List<Task> tasks) {
        try {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
            Calendar today = Calendar.getInstance();
            today.set(Calendar.HOUR_OF_DAY, 0);
            today.set(Calendar.MINUTE, 0);
            today.set(Calendar.SECOND, 0);
            today.set(Calendar.MILLISECOND, 0);

            for (Task task : tasks) {
                String dueDateStr = task.getDueDate();
                if (dueDateStr == null || dueDateStr.isEmpty() || task.isCompleted() ||
                        !task.isEnableDueDateNotifications()) {
                    continue;
                }

                Calendar dueCalendar = Calendar.getInstance();
                dueCalendar.setTime(sdf.parse(dueDateStr));
                dueCalendar.set(Calendar.HOUR_OF_DAY, 0);
                dueCalendar.set(Calendar.MINUTE, 0);
                dueCalendar.set(Calendar.SECOND, 0);
                dueCalendar.set(Calendar.MILLISECOND, 0);

                if (dueCalendar.before(today)) {
                    // Schedule overdue notification for 9 AM today
                    Calendar notifyTime = Calendar.getInstance();
                    notifyTime.set(Calendar.HOUR_OF_DAY, 9);
                    notifyTime.set(Calendar.MINUTE, 0);
                    notifyTime.set(Calendar.SECOND, 0);
                    notifyTime.set(Calendar.MILLISECOND, 0);

                    long delay = notifyTime.getTimeInMillis() - System.currentTimeMillis();
                    if (delay < 0) delay += 24 * 60 * 60 * 1000; // Next day if past 9 AM

                    Data inputData = new Data.Builder()
                            .putString("taskId", task.getId())
                            .putString("taskTitle", task.getTitle())
                            .putString("notificationType", "OVERDUE")
                            .build();

                    OneTimeWorkRequest notificationWork = new OneTimeWorkRequest.Builder(NotificationWorker.class)
                            .setInitialDelay(delay, TimeUnit.MILLISECONDS)
                            .setInputData(inputData)
                            .addTag(task.getId() + "_overdue")
                            .build();

                    WorkManager.getInstance(getApplication()).enqueue(notificationWork);
                    Log.d(TAG, "Overdue notification scheduled for task: " + task.getId());
                }
            }
        } catch (Exception e) {
            Log.e(TAG, "Error scheduling overdue notifications", e);
        }
    }

    private void updateLocalStreakAndMilestones(String taskTitle) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        String today = sdf.format(new java.util.Date());
        String lastCompletion = prefs.getString("lastTaskCompletionDate", null);
        int streak = prefs.getInt("streak", 0);
        int totalTasksCompleted = prefs.getInt("totalTasksCompleted", 0) + 1;
        int weeklyCompletedTasks = prefs.getInt("weeklyCompletedTasks", 0) + 1;
        int weeklyPoints = prefs.getInt("weeklyPoints", 0) + POINTS_PER_COMPLETION;

        // Update streak
        if (lastCompletion != null && lastCompletion.equals(getYesterday(today))) {
            streak++;
        } else if (!today.equals(lastCompletion)) {
            streak = 1; // Reset streak if not consecutive
        }

        // Save streak, completion date, and weekly stats
        prefs.edit()
                .putInt("streak", streak)
                .putString("lastTaskCompletionDate", today)
                .putInt("totalTasksCompleted", totalTasksCompleted)
                .putInt("weeklyCompletedTasks", weeklyCompletedTasks)
                .putInt("weeklyPoints", weeklyPoints)
                .apply();

        // Schedule streak notification
        if (prefs.getBoolean("enableStreakNotifications", true)) {
            Data inputData = new Data.Builder()
                    .putString("taskTitle", taskTitle)
                    .putInt("streak", streak)
                    .putString("notificationType", "STREAK")
                    .build();

            OneTimeWorkRequest notificationWork = new OneTimeWorkRequest.Builder(NotificationWorker.class)
                    .setInputData(inputData)
                    .addTag("streak_" + today)
                    .build();

            WorkManager.getInstance(getApplication()).enqueue(notificationWork);
            Log.d(TAG, "Streak notification scheduled: " + streak + " days");
        }

        // Check milestones
        if (prefs.getBoolean("enableMilestoneNotifications", true) &&
                (totalTasksCompleted == 10 || totalTasksCompleted == 50 || totalTasksCompleted == 100)) {
            Data inputData = new Data.Builder()
                    .putString("taskTitle", taskTitle)
                    .putInt("totalTasksCompleted", totalTasksCompleted)
                    .putString("notificationType", "MILESTONE")
                    .build();

            OneTimeWorkRequest notificationWork = new OneTimeWorkRequest.Builder(NotificationWorker.class)
                    .setInputData(inputData)
                    .addTag("milestone_" + totalTasksCompleted)
                    .build();

            WorkManager.getInstance(getApplication()).enqueue(notificationWork);
            Log.d(TAG, "Milestone notification scheduled: " + totalTasksCompleted + " tasks");
        }
    }

    private void scheduleStreakAndInactivityCheck() {
        // Schedule daily check for streak at risk and inactivity
        PeriodicWorkRequest streakCheck = new PeriodicWorkRequest.Builder(NotificationWorker.class,
                24, TimeUnit.HOURS)
                .setInitialDelay(calculateDelayTo8AM(), TimeUnit.MILLISECONDS)
                .setInputData(new Data.Builder().putString("notificationType", "STREAK_CHECK").build())
                .addTag("streak_check")
                .build();

        PeriodicWorkRequest inactivityCheck = new PeriodicWorkRequest.Builder(NotificationWorker.class,
                24, TimeUnit.HOURS)
                .setInitialDelay(calculateDelayTo8AM(), TimeUnit.MILLISECONDS)
                .setInputData(new Data.Builder().putString("notificationType", "INACTIVITY").build())
                .addTag("inactivity_check")
                .build();

        WorkManager.getInstance(getApplication()).enqueue(streakCheck);
        WorkManager.getInstance(getApplication()).enqueue(inactivityCheck);
        Log.d(TAG, "Daily streak and inactivity checks scheduled");
    }

    private void scheduleWeeklySummary() {
        // Schedule weekly summary on Sunday at 9 AM
        PeriodicWorkRequest summaryWork = new PeriodicWorkRequest.Builder(NotificationWorker.class,
                7, TimeUnit.DAYS)
                .setInitialDelay(calculateDelayToSunday9AM(), TimeUnit.MILLISECONDS)
                .setInputData(new Data.Builder().putString("notificationType", "WEEKLY_SUMMARY").build())
                .addTag("weekly_summary")
                .build();

        WorkManager.getInstance(getApplication()).enqueue(summaryWork);
        Log.d(TAG, "Weekly summary scheduled");
    }

    private long calculateDelayTo8AM() {
        Calendar now = Calendar.getInstance();
        Calendar next8AM = Calendar.getInstance();
        next8AM.set(Calendar.HOUR_OF_DAY, 8);
        next8AM.set(Calendar.MINUTE, 0);
        next8AM.set(Calendar.SECOND, 0);
        next8AM.set(Calendar.MILLISECOND, 0);
        if (now.after(next8AM)) {
            next8AM.add(Calendar.DAY_OF_MONTH, 1);
        }
        return next8AM.getTimeInMillis() - now.getTimeInMillis();
    }

    private long calculateDelayToSunday9AM() {
        Calendar now = Calendar.getInstance();
        Calendar nextSunday9AM = Calendar.getInstance();
        nextSunday9AM.set(Calendar.DAY_OF_WEEK, Calendar.SUNDAY);
        nextSunday9AM.set(Calendar.HOUR_OF_DAY, 9);
        nextSunday9AM.set(Calendar.MINUTE, 0);
        nextSunday9AM.set(Calendar.SECOND, 0);
        nextSunday9AM.set(Calendar.MILLISECOND, 0);
        if (now.after(nextSunday9AM)) {
            nextSunday9AM.add(Calendar.WEEK_OF_YEAR, 1);
        }
        return nextSunday9AM.getTimeInMillis() - now.getTimeInMillis();
    }

    private String getYesterday(String today) {
        try {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
            Calendar cal = Calendar.getInstance();
            cal.setTime(sdf.parse(today));
            cal.add(Calendar.DAY_OF_MONTH, -1);
            return sdf.format(cal.getTime());
        } catch (Exception e) {
            return null;
        }
    }

    private void cancelNotification(String taskId) {
        WorkManager.getInstance(getApplication()).cancelAllWorkByTag(taskId);
        WorkManager.getInstance(getApplication()).cancelAllWorkByTag(taskId + "_due");
        WorkManager.getInstance(getApplication()).cancelAllWorkByTag(taskId + "_pre_due");
        WorkManager.getInstance(getApplication()).cancelAllWorkByTag(taskId + "_overdue");
        Log.d(TAG, "Notifications cancelled for task: " + taskId);
    }

    public LiveData<List<Task>> getTasks() { return tasks; }
    public LiveData<Boolean> getIsLoading() { return isLoading; }
    public LiveData<String> getErrorMessage() { return errorMessage; }


}
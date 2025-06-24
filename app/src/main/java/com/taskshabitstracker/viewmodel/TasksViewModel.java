package com.taskshabitstracker.viewmodel;

import android.app.Application;
import android.util.Log;
import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import com.taskshabitstracker.model.Task;
import com.taskshabitstracker.repository.TasksRepository;
import com.android.volley.Request;
import com.android.volley.toolbox.JsonObjectRequest;
import com.taskshabitstracker.network.VolleySingleton;
import org.json.JSONObject;
import androidx.work.Data;
import androidx.work.OneTimeWorkRequest;
import androidx.work.WorkManager;
import com.taskshabitstracker.workers.NotificationWorker;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

public class TasksViewModel extends AndroidViewModel {
    private static final String TAG = "TasksViewModel";
    private final TasksRepository repository;
    private final MutableLiveData<List<Task>> tasks = new MutableLiveData<>();
    private final MutableLiveData<Boolean> isLoading = new MutableLiveData<>(false);
    private final MutableLiveData<String> errorMessage = new MutableLiveData<>();
    private static final int POINTS_PER_COMPLETION = 10;
    private static final String USER_URL = "http://10.0.2.2:8080/api/users";
    private static final String TIMELINE_URL = "http://10.0.2.2:8080/api/timeline";

    public TasksViewModel(@NonNull Application application) {
        super(application);
        repository = new TasksRepository(application);
    }

    public void loadTasks() {
        Log.d(TAG, "Loading tasks from repository");
        isLoading.setValue(true);
        repository.getTasks(new TasksRepository.OnTasksFetched() {
            @Override
            public void onSuccess(List<Task> taskList) {
                isLoading.setValue(false);
                tasks.setValue(new ArrayList<>(taskList));
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

    public void toggleTaskCompletion(Task task) {
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

                    // Only update points and cancel notification if task was just completed
                    if (!wasCompleted) { // Task was just completed
                        updateUserPoints(POINTS_PER_COMPLETION);
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
                                t.setCompleted(wasCompleted); // Revert to original state
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

    private void updateUserPoints(int pointsToAdd) {
        String url = USER_URL + "/addPoints";
        JSONObject jsonBody = new JSONObject();
        try {
            jsonBody.put("points", pointsToAdd);
        } catch (Exception e) {
            Log.e(TAG, "Error creating JSON for points update", e);
            errorMessage.setValue("Error updating points");
            return;
        }

        JsonObjectRequest request = new JsonObjectRequest(
                Request.Method.PUT,
                url,
                jsonBody,
                response -> Log.d(TAG, "Points updated: " + pointsToAdd),
                error -> {
                    Log.e(TAG, "Error updating points: " + error.toString());
                    errorMessage.setValue("Failed to update points");
                }
        );

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
                error -> Log.e(TAG, "Error adding timeline event: " + error.toString())
        );

        VolleySingleton.getInstance(getApplication()).getRequestQueue().add(request);
    }

    public void deleteTask(Task task, Runnable onSuccess, TasksRepository.OnErrorCallback onError) {
        List<Task> currentTasks = tasks.getValue();
        Log.d(TAG, "Before removal: " + (currentTasks != null ? currentTasks.size() : "null"));
        List<Task> updatedTasks = currentTasks != null ? new ArrayList<>(currentTasks) : new ArrayList<>();
        updatedTasks.removeIf(t -> t.getId().equals(task.getId()));
        tasks.setValue(updatedTasks);
        Log.d(TAG, "Task removed locally: " + task.getId() + ", Updated tasks: " + updatedTasks.size());

        isLoading.setValue(true);
        repository.deleteTask(task,
                () -> {
                    isLoading.setValue(false);
                    onSuccess.run();
                    cancelNotification(task.getId());
                    addTimelineEvent(task.getId(), "DELETED", "Task '" + task.getTitle() + "' deleted");
                    Log.d(TAG, "Task deleted successfully: " + task.getId());
                    loadTasks();
                },
                error -> {
                    updatedTasks.add(task);
                    tasks.setValue(new ArrayList<>(updatedTasks));
                    Log.d(TAG, "Task deletion reverted: " + task.getId() + ", Reverted tasks: " + updatedTasks.size());
                    isLoading.setValue(false);
                    onError.onError(error);
                    Log.e(TAG, "Error deleting task: " + error);
                }
        );
    }

    public void addTask(Task task) {
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
                scheduleNotification(newTask);
                addTimelineEvent(newTask.getId(), "CREATED", "Task '" + newTask.getTitle() + "' created");
            }

            @Override
            public void onError(String error) {
                isLoading.setValue(false);
                errorMessage.setValue(error);
                Log.e(TAG, "Error adding task: " + error);
            }
        });
    }

    private void scheduleNotification(Task task) {
        if (task.getDueDate() == null || task.getDueDate().isEmpty()) return;

        try {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
            java.util.Date dueDate = sdf.parse(task.getDueDate());
            Calendar dueCalendar = Calendar.getInstance();
            dueCalendar.setTime(dueDate);
            dueCalendar.set(Calendar.HOUR_OF_DAY, 9); // Notify at 9 AM on due date
            dueCalendar.set(Calendar.MINUTE, 0);
            dueCalendar.set(Calendar.SECOND, 0);
            dueCalendar.set(Calendar.MILLISECOND, 0);

            long delay = dueCalendar.getTimeInMillis() - System.currentTimeMillis();

            // Only schedule if the notification time is in the future
            if (delay <= 0) {
                Log.d(TAG, "Due date is in the past, not scheduling notification for task: " + task.getId());
                return;
            }

            Data inputData = new Data.Builder()
                    .putString("taskId", task.getId())
                    .putString("taskTitle", task.getTitle())
                    .build();

            OneTimeWorkRequest notificationWork = new OneTimeWorkRequest.Builder(NotificationWorker.class)
                    .setInitialDelay(delay, TimeUnit.MILLISECONDS)
                    .setInputData(inputData)
                    .addTag(task.getId())
                    .build();

            WorkManager.getInstance(getApplication()).enqueue(notificationWork);
            Log.d(TAG, "Notification scheduled for task: " + task.getId() + " in " + delay + "ms");
        } catch (Exception e) {
            Log.e(TAG, "Error scheduling notification for task: " + task.getId(), e);
        }
    }

    private void cancelNotification(String taskId) {
        WorkManager.getInstance(getApplication()).cancelAllWorkByTag(taskId);
        Log.d(TAG, "Notification cancelled for task: " + taskId);
    }

    public LiveData<List<Task>> getTasks() { return tasks; }
    public LiveData<Boolean> getIsLoading() { return isLoading; }
    public LiveData<String> getErrorMessage() { return errorMessage; }
}
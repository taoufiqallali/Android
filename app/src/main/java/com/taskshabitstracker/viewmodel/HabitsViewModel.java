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
import com.taskshabitstracker.model.Habit;
import com.taskshabitstracker.repository.HabitsRepository;
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
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

public class HabitsViewModel extends AndroidViewModel {
    private static final String TAG = "HabitsViewModel";
    private final HabitsRepository repository;
    private final MutableLiveData<List<Habit>> habits = new MutableLiveData<>();
    private final MutableLiveData<Boolean> isLoading = new MutableLiveData<>(false);
    private final MutableLiveData<String> errorMessage = new MutableLiveData<>();
    private static final int POINTS_PER_COMPLETION = 10;
    private static final String USER_URL = "http://10.0.2.2:8080/api/users";
    private static final String TIMELINE_URL = "http://10.0.2.2:8080/api/timeline";
    private final SharedPreferences prefs;

    public HabitsViewModel(@NonNull Application application) {
        super(application);
        repository = new HabitsRepository(application);
        prefs = application.getSharedPreferences("HabitsPrefs", Application.MODE_PRIVATE);
        // Schedule daily streak and inactivity checks
        //scheduleStreakAndInactivityCheck();
        // Schedule weekly summary
        scheduleWeeklySummary();
    }

    public void loadHabits(String userId) throws JSONException {
        Log.d(TAG, "Loading habits from repository for user: " + userId);
        isLoading.setValue(true);
        repository.getHabits(userId, new HabitsRepository.OnHabitsFetched() {
            @Override
            public void onSuccess(List<Habit> habitList) {
                isLoading.setValue(false);
                habits.setValue(new ArrayList<>(habitList));
                Log.d(TAG, "Habits loaded: " + habitList.size() + " habits");
            }

            @Override
            public void onError(String error) {
                isLoading.setValue(false);
                errorMessage.setValue(error);
                Log.e(TAG, "Error loading habits: " + error);
            }
        });
    }

    public void toggleHabitCompletion(Habit habit) throws JSONException {
        List<Habit> currentHabits = habits.getValue();
        boolean wasCompleted = habit.isCompletedToday();

        // Update local state optimistically
        if (currentHabits != null) {
            List<Habit> updatedHabits = new ArrayList<>(currentHabits);
            for (Habit h : updatedHabits) {
                if (h.getId().equals(habit.getId())) {
                    h.setCompletedToday(!h.isCompletedToday());
                    if(!h.isCompletedToday()){
                    h.setStreak(h.getStreak()-1);}
                    else{
                        h.setStreak(h.getStreak()+1);
                    }
                    break;
                }
            }
            habits.setValue(updatedHabits);
            Log.d(TAG, "Habit completion toggled locally: " + habit.getId() + " to " + !wasCompleted);
        }

        isLoading.setValue(true);
        repository.toggleHabitCompletion(habit,
                () -> {
                    isLoading.setValue(false);
                    Log.d(TAG, "Habit completion toggled on server: " + habit.getId());

                    // If habit was just completed, update points and check milestones
                    if (!wasCompleted) {
                        //updateUserPoints(POINTS_PER_COMPLETION, habit.getName());
                        //updateLocalStreakAndMilestones(habit.getName());
                        cancelNotification(habit.getId());
                        addTimelineEvent(habit.getId(), "COMPLETED", "Habit '" + habit.getName() + "' completed");
                    }
                },
                error -> {
                    // Revert local changes on error
                    if (currentHabits != null) {
                        List<Habit> revertedHabits = new ArrayList<>(currentHabits);
                        for (Habit h : revertedHabits) {
                            if (h.getId().equals(habit.getId())) {
                                h.setCompletedToday(wasCompleted);
                                break;
                            }
                        }
                        habits.setValue(revertedHabits);
                        Log.d(TAG, "Habit completion reverted: " + habit.getId() + " back to " + wasCompleted);
                    }
                    isLoading.setValue(false);
                    errorMessage.setValue(error);
                    Log.e(TAG, "Error toggling habit: " + error);
                }
        );
    }

    private void updateUserPoints(int pointsToAdd, String habitName) {
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
                    schedulePointsNotification(habitName, pointsToAdd);
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

    private void addTimelineEvent(String habitId, String eventType, String description) {
        JSONObject jsonBody = new JSONObject();
        try {
            jsonBody.put("habitId", habitId);
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

    public void deleteHabit(Habit habit, Runnable onSuccess, HabitsRepository.OnErrorCallback onError, String userId) {
        List<Habit> currentHabits = habits.getValue();
        List<Habit> updatedHabits = currentHabits != null ? new ArrayList<>(currentHabits) : new ArrayList<>();
        updatedHabits.removeIf(h -> h.getId().equals(habit.getId()));
        habits.setValue(updatedHabits);
        Log.d(TAG, "Habit removed locally: " + habit.getId());

        isLoading.setValue(true);
        repository.deleteHabit(habit,
                () -> {
                    isLoading.setValue(false);
                    onSuccess.run();
                    cancelNotification(habit.getId());
                    addTimelineEvent(habit.getId(), "DELETED", "Habit '" + habit.getName() + "' deleted");
                    Log.d(TAG, "Habit deleted successfully: " + habit.getId());
                    try {
                        loadHabits(userId);
                    } catch (JSONException e) {
                        throw new RuntimeException(e);
                    }
                },
                error -> {
                    updatedHabits.add(habit);
                    habits.setValue(new ArrayList<>(updatedHabits));
                    Log.d(TAG, "Habit deletion reverted: " + habit.getId());
                    isLoading.setValue(false);
                    errorMessage.setValue(error);
                    Log.e(TAG, "Error deleting habit: " + error);
                }
        );
    }

    public void addHabit(Habit habit, String userId) {
        isLoading.setValue(true);
        repository.addHabit(habit, new HabitsRepository.OnHabitAdded() {
            @Override
            public void onTaskAdded(Habit newHabit) {
                List<Habit> currentHabits = habits.getValue();
                List<Habit> updatedHabits = currentHabits != null ? new ArrayList<>(currentHabits) : new ArrayList<>();
                updatedHabits.add(newHabit);
                habits.setValue(updatedHabits);
                Log.d(TAG, "Habit added locally: " + newHabit.getId());
                isLoading.setValue(false);
                // Schedule habit creation notification
                scheduleHabitCreatedNotification(newHabit);
                addTimelineEvent(newHabit.getId(), "CREATED", "Habit '" + newHabit.getName() + "' created");
            }

            @Override
            public void onError(String error) {
                isLoading.setValue(false);
                errorMessage.setValue(error);
                Log.e(TAG, "Error adding habit: " + error);
            }
        }, userId);
    }

    private void scheduleHabitCreatedNotification(Habit habit) {
        Data inputData = new Data.Builder()
                .putString("habitId", habit.getId())
                .putString("habitName", habit.getName())
                .putString("notificationType", "HABIT_CREATED")
                .build();

        OneTimeWorkRequest notificationWork = new OneTimeWorkRequest.Builder(NotificationWorker.class)
                .setInputData(inputData)
                .addTag(habit.getId())
                .build();

        WorkManager.getInstance(getApplication()).enqueue(notificationWork);
        Log.d(TAG, "Habit creation notification scheduled for habit: " + habit.getId());
    }

    private void schedulePointsNotification(String habitName, int points) {
        Data inputData = new Data.Builder()
                .putString("habitName", habitName)
                .putInt("points", points)
                .putString("notificationType", "POINTS_EARNED")
                .build();

        OneTimeWorkRequest notificationWork = new OneTimeWorkRequest.Builder(NotificationWorker.class)
                .setInputData(inputData)
                .addTag("points_" + habitName)
                .build();

        WorkManager.getInstance(getApplication()).enqueue(notificationWork);
        Log.d(TAG, "Points notification scheduled for: " + points + " points");
    }

    private void updateLocalStreakAndMilestones(String habitName) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        String today = sdf.format(new java.util.Date());
        String lastCompletion = prefs.getString("lastHabitCompletionDate", null);
        int streak = prefs.getInt("streak", 0);
        int totalHabitsCompleted = prefs.getInt("totalHabitsCompleted", 0) + 1;
        int weeklyCompletedHabits = prefs.getInt("weeklyCompletedHabits", 0) + 1;
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
                .putString("lastHabitCompletionDate", today)
                .putInt("totalHabitsCompleted", totalHabitsCompleted)
                .putInt("weeklyCompletedHabits", weeklyCompletedHabits)
                .putInt("weeklyPoints", weeklyPoints)
                .apply();

        // Schedule streak notification
        if (prefs.getBoolean("enableStreakNotifications", true)) {
            Data inputData = new Data.Builder()
                    .putString("habitName", habitName)
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
                (totalHabitsCompleted == 10 || totalHabitsCompleted == 50 || totalHabitsCompleted == 100)) {
            Data inputData = new Data.Builder()
                    .putString("habitName", habitName)
                    .putInt("totalHabitsCompleted", totalHabitsCompleted)
                    .putString("notificationType", "MILESTONE")
                    .build();

            OneTimeWorkRequest notificationWork = new OneTimeWorkRequest.Builder(NotificationWorker.class)
                    .setInputData(inputData)
                    .addTag("milestone_" + totalHabitsCompleted)
                    .build();

            WorkManager.getInstance(getApplication()).enqueue(notificationWork);
            Log.d(TAG, "Milestone notification scheduled: " + totalHabitsCompleted + " habits");
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

    private void cancelNotification(String habitId) {
        WorkManager.getInstance(getApplication()).cancelAllWorkByTag(habitId);
        Log.d(TAG, "Notifications cancelled for habit: " + habitId);
    }

    public LiveData<List<Habit>> getHabits() { return habits; }
    public LiveData<Boolean> getIsLoading() { return isLoading; }
    public LiveData<String> getErrorMessage() { return errorMessage; }
}
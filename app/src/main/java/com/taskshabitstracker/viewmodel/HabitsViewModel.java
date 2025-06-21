package com.taskshabitstracker.viewmodel;

import android.app.Application;
import android.util.Log;
import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import com.taskshabitstracker.model.Habit;
import com.taskshabitstracker.repository.DashboardRepository;
import com.taskshabitstracker.repository.HabitsRepository;
import java.util.ArrayList;
import java.util.List;

public class HabitsViewModel extends AndroidViewModel {
    private static final String TAG = "HabitsViewModel";
    private final HabitsRepository repository;
    private final MutableLiveData<List<Habit>> habits = new MutableLiveData<>();
    private final MutableLiveData<Boolean> isLoading = new MutableLiveData<>(false);
    private final MutableLiveData<String> errorMessage = new MutableLiveData<>();

    public HabitsViewModel(@NonNull Application application) {
        super(application);
        repository = new HabitsRepository(application);
    }

    public void loadHabits() {
        Log.d(TAG, "Loading habits from repository");
        isLoading.setValue(true);
        repository.getHabits(
                habitList -> {
                    isLoading.setValue(false);
                    habits.setValue(new ArrayList<>(habitList));
                    Log.d(TAG, "Habits loaded: " + habitList.toString());
                },
                error -> {
                    isLoading.setValue(false);
                    errorMessage.setValue(error);
                    Log.e(TAG, "Error loading habits: " + error);
                }
        );
    }

    public void toggleHabitCompletion(Habit habit) {
        List<Habit> currentHabits = habits.getValue();
        if (currentHabits != null) {
            List<Habit> updatedHabits = new ArrayList<>(currentHabits);
            for (Habit h : updatedHabits) {
                if (h.getId().equals(habit.getId())) {
                    h.toggleCompletedToday();
                    break;
                }
            }
            habits.setValue(updatedHabits);
            Log.d(TAG, "Habit completion toggled locally: " + habit.getId());
        }

        isLoading.setValue(true);
        repository.toggleHabitCompletion(habit,
                () -> {
                    isLoading.setValue(false);
                    Log.d(TAG, "Habit completion toggled on server: " + habit.getId());
                },
                error -> {
                    if (currentHabits != null) {
                        List<Habit> updatedHabits = new ArrayList<>(currentHabits);
                        for (Habit h : updatedHabits) {
                            if (h.getId().equals(habit.getId())) {
                                h.toggleCompletedToday(); // Revert
                                break;
                            }
                        }
                        habits.setValue(updatedHabits);
                        Log.d(TAG, "Habit completion reverted: " + habit.getId());
                    }
                    isLoading.setValue(false);
                    errorMessage.setValue(error);
                    Log.e(TAG, "Error toggling habit: " + error);
                }
        );
    }

    public void deleteHabit(Habit habit, Runnable onSuccess, DashboardRepository.OnErrorCallback onError) {
        List<Habit> currentHabits = habits.getValue();
        Log.d(TAG, "Before removal: " + (currentHabits != null ? currentHabits.toString() : "null"));
        List<Habit> updatedHabits = currentHabits != null ? new ArrayList<>(currentHabits) : new ArrayList<>();
        updatedHabits.removeIf(h -> h.getId().equals(habit.getId()));
        habits.setValue(updatedHabits);
        Log.d(TAG, "Habit removed locally: " + habit.getId() + ", Updated habits: " + updatedHabits.toString());

        isLoading.setValue(true);
        repository.deleteHabit(habit,
                () -> {
                    isLoading.setValue(false);
                    onSuccess.run();
                    Log.d(TAG, "Habit deleted successfully: " + habit.getId());
                    loadHabits();
                },
                error -> {
                    updatedHabits.add(habit);
                    habits.setValue(new ArrayList<>(updatedHabits));
                    Log.d(TAG, "Habit deletion reverted: " + habit.getId() + ", Reverted habits: " + updatedHabits.toString());
                    isLoading.setValue(false);
                    onError.onError(error);
                    Log.e(TAG, "Error deleting habit: " + error);
                }
        );
    }

    public void addHabit(Habit habit) {
        isLoading.setValue(true);
        repository.addHabit(habit,
                newHabit -> {
                    List<Habit> currentHabits = habits.getValue();
                    List<Habit> updatedHabits = currentHabits != null ? new ArrayList<>(currentHabits) : new ArrayList<>();
                    updatedHabits.add(newHabit);
                    habits.setValue(updatedHabits);
                    Log.d(TAG, "Habit added locally: " + newHabit.getId());
                    isLoading.setValue(false);
                },
                error -> {
                    isLoading.setValue(false);
                    errorMessage.setValue(error);
                    Log.e(TAG, "Error adding habit: " + error);
                }
        );
    }

    public LiveData<List<Habit>> getHabits() { return habits; }
    public LiveData<Boolean> getIsLoading() { return isLoading; }
    public LiveData<String> getErrorMessage() { return errorMessage; }
}
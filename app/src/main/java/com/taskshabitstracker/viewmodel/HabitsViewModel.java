// HabitsViewModel.java - For habits business logic
package com.taskshabitstracker.viewmodel;

import android.app.Application;
import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import com.taskshabitstracker.model.Habit;
import com.taskshabitstracker.repository.HabitsRepository;
import java.util.List;

public class HabitsViewModel extends AndroidViewModel {
    private final HabitsRepository repository;
    private final MutableLiveData<List<Habit>> habits = new MutableLiveData<>();
    private final MutableLiveData<Boolean> isLoading = new MutableLiveData<>(false);
    private final MutableLiveData<String> errorMessage = new MutableLiveData<>();

    public HabitsViewModel(@NonNull Application application) {
        super(application);
        repository = new HabitsRepository(application);
    }

    public void loadHabits() {
        isLoading.setValue(true);

        repository.getHabits(
                habitList -> {
                    isLoading.setValue(false);
                    habits.setValue(habitList);
                },
                error -> {
                    isLoading.setValue(false);
                    errorMessage.setValue(error);
                }
        );
    }

    public void toggleHabitCompletion(Habit habit) {
        repository.toggleHabitCompletion(habit,
                () -> loadHabits(),
                error -> errorMessage.setValue(error)
        );
    }

    public void deleteHabit(Habit habit) {
        repository.deleteHabit(habit,
                () -> loadHabits(),
                error -> errorMessage.setValue(error)
        );
    }

    public LiveData<List<Habit>> getHabits() { return habits; }
    public LiveData<Boolean> getIsLoading() { return isLoading; }
    public LiveData<String> getErrorMessage() { return errorMessage; }
}
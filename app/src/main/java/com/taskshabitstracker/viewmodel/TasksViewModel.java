// TasksViewModel.java - For tasks business logic
package com.taskshabitstracker.viewmodel;

import android.app.Application;
import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import com.taskshabitstracker.model.Task;
import com.taskshabitstracker.repository.TasksRepository;
import java.util.List;

public class TasksViewModel extends AndroidViewModel {
    private final TasksRepository repository;
    private final MutableLiveData<List<Task>> tasks = new MutableLiveData<>();
    private final MutableLiveData<Boolean> isLoading = new MutableLiveData<>(false);
    private final MutableLiveData<String> errorMessage = new MutableLiveData<>();

    public TasksViewModel(@NonNull Application application) {
        super(application);
        repository = new TasksRepository(application);
    }

    public void loadTasks() {
        isLoading.setValue(true);

        repository.getTasks(
                taskList -> {
                    isLoading.setValue(false);
                    tasks.setValue(taskList);
                },
                error -> {
                    isLoading.setValue(false);
                    errorMessage.setValue(error);
                }
        );
    }

    public void toggleTaskCompletion(Task task) {
        repository.toggleTaskCompletion(task,
                () -> loadTasks(), // Reload tasks on success
                error -> errorMessage.setValue(error)
        );
    }

    public void deleteTask(Task task) {
        repository.deleteTask(task,
                () -> loadTasks(), // Reload tasks on success
                error -> errorMessage.setValue(error)
        );
    }

    public LiveData<List<Task>> getTasks() { return tasks; }
    public LiveData<Boolean> getIsLoading() { return isLoading; }
    public LiveData<String> getErrorMessage() { return errorMessage; }
}
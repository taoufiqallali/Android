package com.taskshabitstracker.viewmodel;

import android.app.Application;
import android.util.Log;
import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import com.taskshabitstracker.model.Task;
import com.taskshabitstracker.repository.DashboardRepository;
import com.taskshabitstracker.repository.TasksRepository;
import java.util.ArrayList;
import java.util.List;

public class TasksViewModel extends AndroidViewModel {
    private static final String TAG = "TasksViewModel";
    private final TasksRepository repository;
    private final MutableLiveData<List<Task>> tasks = new MutableLiveData<>();
    private final MutableLiveData<Boolean> isLoading = new MutableLiveData<>(false);
    private final MutableLiveData<String> errorMessage = new MutableLiveData<>();

    public TasksViewModel(@NonNull Application application) {
        super(application);
        repository = new TasksRepository(application);
    }

    public void loadTasks() {
        Log.d(TAG, "Loading tasks from repository");
        isLoading.setValue(true);
        repository.getTasks(
                taskList -> {
                    isLoading.setValue(false);
                    tasks.setValue(new ArrayList<>(taskList));
                    Log.d(TAG, "Tasks loaded: " + taskList.toString());
                },
                error -> {
                    isLoading.setValue(false);
                    errorMessage.setValue(error);
                    Log.e(TAG, "Error loading tasks: " + error);
                }
        );
    }

    public void toggleTaskCompletion(Task task) {
        List<Task> currentTasks = tasks.getValue();
        if (currentTasks != null) {
            List<Task> updatedTasks = new ArrayList<>(currentTasks);
            for (Task t : updatedTasks) {
                if (t.getId().equals(task.getId())) {
                    t.setCompleted(!t.isCompleted());
                    break;
                }
            }
            tasks.setValue(updatedTasks);
            Log.d(TAG, "Task completion toggled locally: " + task.getId());
        }

        isLoading.setValue(true);
        repository.toggleTaskCompletion(task,
                () -> {
                    isLoading.setValue(false);
                    Log.d(TAG, "Task completion toggled on server: " + task.getId());
                },
                error -> {
                    if (currentTasks != null) {
                        List<Task> updatedTasks = new ArrayList<>(currentTasks);
                        for (Task t : updatedTasks) {
                            if (t.getId().equals(task.getId())) {
                                t.setCompleted(!t.isCompleted());
                                break;
                            }
                        }
                        tasks.setValue(updatedTasks);
                        Log.d(TAG, "Task completion reverted: " + task.getId());
                    }
                    isLoading.setValue(false);
                    errorMessage.setValue(error);
                    Log.e(TAG, "Error toggling task: " + error);
                }
        );
    }

    public void deleteTask(Task task, Runnable onSuccess, DashboardRepository.OnErrorCallback onError) {
        List<Task> currentTasks = tasks.getValue();
        Log.d(TAG, "Before removal: " + (currentTasks != null ? currentTasks.toString() : "null"));
        List<Task> updatedTasks = currentTasks != null ? new ArrayList<>(currentTasks) : new ArrayList<>();
        updatedTasks.removeIf(t -> t.getId().equals(task.getId()));
        tasks.setValue(updatedTasks);
        Log.d(TAG, "Task removed locally: " + task.getId() + ", Updated tasks: " + updatedTasks.toString());

        isLoading.setValue(true);
        repository.deleteTask(task,
                () -> {
                    isLoading.setValue(false);
                    onSuccess.run();
                    Log.d(TAG, "Task deleted successfully: " + task.getId());
                    loadTasks();
                },
                error -> {
                    updatedTasks.add(task);
                    tasks.setValue(new ArrayList<>(updatedTasks));
                    Log.d(TAG, "Task deletion reverted: " + task.getId() + ", Reverted tasks: " + updatedTasks.toString());
                    isLoading.setValue(false);
                    onError.onError(error);
                    Log.e(TAG, "Error deleting task: " + error);
                }
        );
    }

    public void addTask(Task task) {
        isLoading.setValue(true);
        repository.addTask(task,
                newTask -> {
                    List<Task> currentTasks = tasks.getValue();
                    List<Task> updatedTasks = currentTasks != null ? new ArrayList<>(currentTasks) : new ArrayList<>();
                    updatedTasks.add(newTask);
                    tasks.setValue(updatedTasks);
                    Log.d(TAG, "Task added locally: " + newTask.getId());
                    isLoading.setValue(false);
                },
                error -> {
                    isLoading.setValue(false);
                    errorMessage.setValue(error);
                    Log.e(TAG, "Error adding task: " + error);
                }
        );
    }

    public LiveData<List<Task>> getTasks() { return tasks; }
    public LiveData<Boolean> getIsLoading() { return isLoading; }
    public LiveData<String> getErrorMessage() { return errorMessage; }
}
// DashboardViewModel.java - For dashboard business logic
package com.taskshabitstracker.viewmodel;

import android.app.Application;
import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import com.taskshabitstracker.model.DashboardStats;
import com.taskshabitstracker.repository.DashboardRepository;

public class DashboardViewModel extends AndroidViewModel {
    private final DashboardRepository repository;
    private final MutableLiveData<DashboardStats> dashboardStats = new MutableLiveData<>();
    private final MutableLiveData<Boolean> isLoading = new MutableLiveData<>(false);
    private final MutableLiveData<String> errorMessage = new MutableLiveData<>();

    public DashboardViewModel(@NonNull Application application) {
        super(application);
        repository = new DashboardRepository(application);
    }

    public void loadDashboardData() {
        isLoading.setValue(true);

        repository.getDashboardStats(
                stats -> {
                    isLoading.setValue(false);
                    dashboardStats.setValue(stats);
                },
                error -> {
                    isLoading.setValue(false);
                    errorMessage.setValue(error);
                }
        );
    }

    public LiveData<DashboardStats> getDashboardStats() { return dashboardStats; }
    public LiveData<Boolean> getIsLoading() { return isLoading; }
    public LiveData<String> getErrorMessage() { return errorMessage; }
}
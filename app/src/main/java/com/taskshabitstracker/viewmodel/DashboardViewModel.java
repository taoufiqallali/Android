package com.taskshabitstracker.viewmodel;

import android.app.Application;
import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import com.taskshabitstracker.model.DashboardStats;
import com.taskshabitstracker.repository.DashboardRepository;

/**
 * DashboardViewModel - Handles dashboard business logic
 * Manages data fetching and state for the DashboardFragment
 */
public class DashboardViewModel extends AndroidViewModel {
    private final DashboardRepository repository;
    private final MutableLiveData<DashboardStats> dashboardStats = new MutableLiveData<>();
    private final MutableLiveData<Boolean> isLoading = new MutableLiveData<>(false);
    private final MutableLiveData<String> errorMessage = new MutableLiveData<>();

    public DashboardViewModel(@NonNull Application application) {
        super(application);
        repository = new DashboardRepository(application);
    }

    /**
     * Loads dashboard data for the specified user
     * @param userId The ID of the user to fetch stats for
     */
    public void loadDashboardData(String userId) {
        if (userId == null || userId.isEmpty()) {
            isLoading.setValue(false);
            errorMessage.setValue("Invalid user ID. Please log in.");
            dashboardStats.setValue(new DashboardStats(0, 0, 0, 0, 0, 0)); // Fallback stats
            return;
        }

        // Clear previous error message
        errorMessage.setValue(null);
        // Set loading state
        isLoading.setValue(true);

        repository.getDashboardStats(
                userId,
                stats -> {
                    // Success: Update stats and clear loading state
                    isLoading.setValue(false);
                    dashboardStats.setValue(stats);
                },
                error -> {
                    // Error: Update error message and clear loading state
                    isLoading.setValue(false);
                    errorMessage.setValue(error);
                    dashboardStats.setValue(new DashboardStats(0, 0, 0, 0, 0, 0)); // Fallback stats
                }
        );
    }

    public LiveData<DashboardStats> getDashboardStats() {
        return dashboardStats;
    }

    public LiveData<Boolean> getIsLoading() {
        return isLoading;
    }

    public LiveData<String> getErrorMessage() {
        return errorMessage;
    }
}
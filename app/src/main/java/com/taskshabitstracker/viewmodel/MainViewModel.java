// MainViewModel.java - Handles business logic and state management
package com.taskshabitstracker.viewmodel;

import android.app.Application;
import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import com.taskshabitstracker.repository.AuthRepository;
import com.taskshabitstracker.utils.SessionManager;

/**
 * MainViewModel - Manages UI state and business logic for MainActivity
 * Extends AndroidViewModel to access Application context
 * Survives configuration changes (screen rotation, etc.)
 */
public class MainViewModel extends AndroidViewModel {
    private static final String TAG = "MainViewModel";

    // Repository for authentication operations
    private final AuthRepository authRepository;

    // Session manager for local session handling
    private final SessionManager sessionManager;

    // LiveData for observing logout state
    private final MutableLiveData<Boolean> logoutState = new MutableLiveData<>();

    // LiveData for error messages
    private final MutableLiveData<String> errorMessage = new MutableLiveData<>();

    // LiveData for loading state
    private final MutableLiveData<Boolean> isLoading = new MutableLiveData<>(false);

    public MainViewModel(@NonNull Application application) {
        super(application);

        // Initialize dependencies
        authRepository = new AuthRepository(application);
        sessionManager = new SessionManager(application);
    }

    /**
     * Perform logout operation
     * Handles both server logout and local session clearing
     */
    public void logout() {
        isLoading.setValue(true);

        // Perform logout via repository
        authRepository.logout(
                // Success callback
                () -> {
                    isLoading.setValue(false);
                    sessionManager.clearSession();
                    logoutState.setValue(true);
                },
                // Error callback
                (error) -> {
                    isLoading.setValue(false);
                    // Even if server logout fails, clear local session
                    sessionManager.clearSession();
                    logoutState.setValue(true);
                    errorMessage.setValue("Logout completed (some errors occurred)");
                }
        );
    }

    // Getters for LiveData - allows UI to observe state changes
    public LiveData<Boolean> getLogoutState() {
        return logoutState;
    }

    public LiveData<String> getErrorMessage() {
        return errorMessage;
    }

    public LiveData<Boolean> getIsLoading() {
        return isLoading;
    }
}
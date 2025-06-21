package com.taskshabitstracker.viewmodel;

import android.app.Application;
import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import com.taskshabitstracker.model.UserProfile;
import com.taskshabitstracker.repository.ProfileRepository;

public class ProfileViewModel extends AndroidViewModel {
    private final ProfileRepository repository;
    private final MutableLiveData<UserProfile> userProfile = new MutableLiveData<>();
    private final MutableLiveData<Boolean> isLoading = new MutableLiveData<>(false);
    private final MutableLiveData<String> errorMessage = new MutableLiveData<>();

    public ProfileViewModel(@NonNull Application application) {
        super(application);
        repository = new ProfileRepository(application);
    }

    public void loadUserProfile() {
        isLoading.setValue(true);

        repository.getUserProfile(
                profile -> {
                    isLoading.setValue(false);
                    userProfile.setValue(profile);
                },
                error -> {
                    isLoading.setValue(false);
                    errorMessage.setValue(error);
                }
        );
    }

    public LiveData<UserProfile> getUserProfile() { return userProfile; }
    public LiveData<Boolean> getIsLoading() { return isLoading; }
    public LiveData<String> getErrorMessage() { return errorMessage; }
}

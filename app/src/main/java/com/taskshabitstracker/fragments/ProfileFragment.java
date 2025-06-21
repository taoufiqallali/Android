// ProfileFragment.java - For the profile tab
package com.taskshabitstracker.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import com.taskshabitstracker.databinding.FragmentProfileBinding;
import com.taskshabitstracker.viewmodel.ProfileViewModel;

public class ProfileFragment extends Fragment {
    private FragmentProfileBinding binding;
    private ProfileViewModel viewModel;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentProfileBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        viewModel = new ViewModelProvider(this).get(ProfileViewModel.class);
        setupObservers();
        setupClickListeners();
        viewModel.loadUserProfile();
    }

    private void setupObservers() {
        viewModel.getUserProfile().observe(getViewLifecycleOwner(), profile -> {
            if (profile != null) {
                binding.userEmailText.setText(profile.getEmail());
                binding.userNameText.setText(profile.getName());
                // Set other profile data
            }
        });
    }

    private void setupClickListeners() {
        binding.editProfileButton.setOnClickListener(v -> {
            Toast.makeText(getContext(), "Edit Profile - Coming soon!", Toast.LENGTH_SHORT).show();
        });

        binding.settingsButton.setOnClickListener(v -> {
            Toast.makeText(getContext(), "Settings - Coming soon!", Toast.LENGTH_SHORT).show();
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}

package com.taskshabitstracker.fragments;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import com.taskshabitstracker.LoginActivity;
import com.taskshabitstracker.R;
import com.taskshabitstracker.databinding.FragmentDashboardBinding;
import com.taskshabitstracker.repository.AuthRepository;
import com.taskshabitstracker.repository.UserRepository;
import com.taskshabitstracker.viewmodel.DashboardViewModel;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/**
 * DashboardFragment - Main dashboard screen
 * Shows overview of user's tasks, habits, and statistics
 * Uses Fragment instead of manual view management
 */
public class DashboardFragment extends Fragment {
    private static final String TAG = "DashboardFragment";

    // ViewBinding for this fragment
    private FragmentDashboardBinding binding;

    // ViewModel for dashboard-specific logic
    private DashboardViewModel viewModel;

    // NavController for navigation
    private NavController navController;

    // Repositories for auth and user data
    private AuthRepository authRepository;
    private UserRepository userRepository;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate layout using ViewBinding
        binding = FragmentDashboardBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Initialize NavController
        navController = Navigation.findNavController(view);

        // Initialize ViewModel
        viewModel = new ViewModelProvider(this).get(DashboardViewModel.class);

        // Initialize Repositories
        authRepository = new AuthRepository(requireContext());
        userRepository = new UserRepository(requireContext());

        // Set up UI
        setupDateDisplay();
        setupClickListeners();
        setupObservers();

        // Load user data and dashboard stats
        String userId = getCurrentUserId();
        if (userId == null || userId.isEmpty()) {
            Toast.makeText(getContext(), "Please log in to view your dashboard", Toast.LENGTH_LONG).show();
            // Navigate to LoginActivity
            Intent intent = new Intent(getContext(), LoginActivity.class);
            startActivity(intent);
            requireActivity().finish(); // Close the current activity
        } else {
            setupWelcomeText(userId);
            viewModel.loadDashboardData(userId);
        }
    }

    /**
     * Set up current date display
     */
    private void setupDateDisplay() {
        SimpleDateFormat sdf = new SimpleDateFormat("EEEE, MMMM d, yyyy", Locale.getDefault());
        binding.dateText.setText(sdf.format(new Date()));
    }

    /**
     * Set up welcome text with user's name
     */
    private void setupWelcomeText(String userId) {
        String userName = getUserName();
        if (userName == null || userName.isEmpty()) {
            userRepository.getUserName(userId,
                    name -> {
                        binding.welcomeText.setText("Welcome back, " + name + "!");
                        // Cache the name in SharedPreferences
                        SharedPreferences prefs = requireContext().getSharedPreferences("app_prefs", Context.MODE_PRIVATE);
                        prefs.edit().putString("userName", name).apply();
                    },
                    error -> {
                        Toast.makeText(getContext(), error, Toast.LENGTH_LONG).show();
                        binding.welcomeText.setText("Welcome back!");
                    }
            );
        } else {
            binding.welcomeText.setText("Welcome back, " + userName + "!");
        }
    }

    /**
     * Set up click listeners for dashboard buttons
     */
    private void setupClickListeners() {
        binding.addTaskButton.setOnClickListener(v -> {
            if (navController.getCurrentDestination().getId() == R.id.dashboardFragment) {
                navController.navigate(R.id.action_dashboard_to_tasks);
            } else {
                Toast.makeText(getContext(), "Navigation to Add Task failed", Toast.LENGTH_SHORT).show();
            }
        });

        binding.addHabitButton.setOnClickListener(v -> {
            if (navController.getCurrentDestination().getId() == R.id.dashboardFragment) {
                navController.navigate(R.id.action_dashboard_to_habits);
            } else {
                Toast.makeText(getContext(), "Navigation to Add Habit failed", Toast.LENGTH_SHORT).show();
            }
        });

        binding.logoutButton.setOnClickListener(v -> {
            authRepository.logout(
                    () -> {
                        // Clear SharedPreferences
                        SharedPreferences prefs = requireContext().getSharedPreferences("app_prefs", Context.MODE_PRIVATE);
                        prefs.edit().clear().apply();
                        // Navigate to LoginActivity
                        Intent intent = new Intent(getContext(), LoginActivity.class);
                        startActivity(intent);
                        requireActivity().finish(); // Close the current activity
                    },
                    error -> Toast.makeText(getContext(), "Logout failed: " + error, Toast.LENGTH_LONG).show()
            );
        });
    }

    /**
     * Set up observers for ViewModel LiveData
     */
    private void setupObservers() {
        // Observe dashboard statistics
        viewModel.getDashboardStats().observe(getViewLifecycleOwner(), stats -> {
            if (stats != null) {
                binding.pointsValue.setText(String.valueOf(stats.getPoints()));
                binding.streakValue.setText(String.valueOf(stats.getStreak()));
                binding.tasksCompletedText.setText(stats.getCompletedTasks() + "/" + stats.getTotalTasks());
                binding.habitsCompletedText.setText(stats.getCompletedHabits() + "/" + stats.getTotalHabits());
            } else {
                // Fallback UI state
                binding.pointsValue.setText("0");
                binding.streakValue.setText("0");
                binding.tasksCompletedText.setText("0/0");
                binding.habitsCompletedText.setText("0/0");
            }
        });

        // Observe loading state
        viewModel.getIsLoading().observe(getViewLifecycleOwner(), isLoading -> {
            binding.progressBar.setVisibility(isLoading ? View.VISIBLE : View.GONE);
        });

        // Observe error messages
        viewModel.getErrorMessage().observe(getViewLifecycleOwner(), errorMessage -> {
            if (errorMessage != null && !errorMessage.isEmpty()) {
                Toast.makeText(getContext(), errorMessage, Toast.LENGTH_LONG).show();
            }
        });
    }

    /**
     * Retrieve userId from SharedPreferences
     */
    private String getCurrentUserId() {
        SharedPreferences prefs = requireContext().getSharedPreferences("app_prefs", Context.MODE_PRIVATE);
        String userId = prefs.getString("userId", null);
        if (userId == null) {
            Log.e(TAG, "No user ID found in SharedPreferences");
        }
        return userId;
    }

    /**
     * Retrieve user name from SharedPreferences
     */
    private String getUserName() {
        SharedPreferences prefs = requireContext().getSharedPreferences("app_prefs", Context.MODE_PRIVATE);
        return prefs.getString("userName", "");
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        // Clean up ViewBinding to prevent memory leaks
        binding = null;
    }
}
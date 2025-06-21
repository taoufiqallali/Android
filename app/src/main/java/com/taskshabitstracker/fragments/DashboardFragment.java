// DashboardFragment.java - Dashboard screen as a fragment
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
import com.taskshabitstracker.databinding.FragmentDashboardBinding;
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

        // Initialize ViewModel
        viewModel = new ViewModelProvider(this).get(DashboardViewModel.class);

        // Set up UI
        setupDateDisplay();
        setupClickListeners();
        setupObservers();

        // Load dashboard data
        viewModel.loadDashboardData();
    }

    /**
     * Set up current date display
     */
    private void setupDateDisplay() {
        SimpleDateFormat sdf = new SimpleDateFormat("EEEE, MMMM d, yyyy", Locale.getDefault());
        binding.dateText.setText(sdf.format(new Date()));
    }

    /**
     * Set up click listeners for dashboard buttons
     */
    private void setupClickListeners() {
        binding.addTaskButton.setOnClickListener(v -> {
            Toast.makeText(getContext(), "Add Task clicked", Toast.LENGTH_SHORT).show();
            // TODO: Navigate to add task screen
            // findNavController().navigate(R.id.action_dashboard_to_addTask);
        });

        binding.addHabitButton.setOnClickListener(v -> {
            Toast.makeText(getContext(), "Add Habit clicked", Toast.LENGTH_SHORT).show();
            // TODO: Navigate to add habit screen
            // findNavController().navigate(R.id.action_dashboard_to_addHabit);
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
            }
        });

        // Observe loading state
        viewModel.getIsLoading().observe(getViewLifecycleOwner(), isLoading -> {
            // Show/hide loading indicator
            // binding.progressBar.setVisibility(isLoading ? View.VISIBLE : View.GONE);
        });

        // Observe error messages
        viewModel.getErrorMessage().observe(getViewLifecycleOwner(), errorMessage -> {
            if (errorMessage != null && !errorMessage.isEmpty()) {
                Toast.makeText(getContext(), errorMessage, Toast.LENGTH_LONG).show();
            }
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        // Clean up ViewBinding to prevent memory leaks
        binding = null;
    }
}
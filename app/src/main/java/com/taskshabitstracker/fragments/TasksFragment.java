// TasksFragment.java - Tasks screen as a fragment
package com.taskshabitstracker.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import com.taskshabitstracker.adapters.TaskAdapter;
import com.taskshabitstracker.databinding.FragmentTasksBinding;
import com.taskshabitstracker.viewmodel.TasksViewModel;

/**
 * TasksFragment - Displays list of user's tasks
 * Uses RecyclerView with proper adapter pattern
 * Handles task operations through ViewModel
 */
public class TasksFragment extends Fragment {
    private static final String TAG = "TasksFragment";

    private FragmentTasksBinding binding;
    private TasksViewModel viewModel;
    private TaskAdapter taskAdapter;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentTasksBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Initialize ViewModel
        viewModel = new ViewModelProvider(this).get(TasksViewModel.class);

        // Set up RecyclerView
        setupRecyclerView();

        // Set up observers
        setupObservers();

        // Set up FAB
        setupFab();

        // Load tasks
        viewModel.loadTasks();
    }

    /**
     * Set up RecyclerView with adapter and layout manager
     */
    private void setupRecyclerView() {
        taskAdapter = new TaskAdapter(
                task -> viewModel.toggleTaskCompletion(task),
                task -> viewModel.deleteTask(task)
        );

        binding.tasksRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        binding.tasksRecyclerView.setAdapter(taskAdapter);
    }

    /**
     * Set up observers for ViewModel LiveData
     */
    private void setupObservers() {
        // Observe tasks list
        viewModel.getTasks().observe(getViewLifecycleOwner(), tasks -> {
            taskAdapter.updateTasks(tasks);

            // Show/hide empty state
            if (tasks.isEmpty()) {
                binding.emptyTextView.setVisibility(View.VISIBLE);
                binding.tasksRecyclerView.setVisibility(View.GONE);
            } else {
                binding.emptyTextView.setVisibility(View.GONE);
                binding.tasksRecyclerView.setVisibility(View.VISIBLE);
            }
        });

        // Observe loading state
        viewModel.getIsLoading().observe(getViewLifecycleOwner(), isLoading -> {
            // Handle loading state
        });
    }

    /**
     * Set up Floating Action Button
     */
    private void setupFab() {
        binding.fabAddTask.setOnClickListener(v -> {
            // TODO: Open add task dialog or navigate to add task screen
            // AddTaskDialogFragment.newInstance().show(getChildFragmentManager(), "AddTaskDialog");
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
package com.taskshabitstracker.fragments;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import com.taskshabitstracker.adapters.TaskAdapter;
import com.taskshabitstracker.databinding.FragmentTasksBinding;
import com.taskshabitstracker.viewmodel.TasksViewModel;

import org.json.JSONException;

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
        Log.d(TAG, "onViewCreated: Initializing");

        viewModel = new ViewModelProvider(this).get(TasksViewModel.class);
        setupRecyclerView();
        setupObservers();
        setupFab();

        viewModel.loadTasks(getCurrentUserId());
    }

    private void setupRecyclerView() {
        taskAdapter = new TaskAdapter(
                task -> {
                    try {
                        viewModel.toggleTaskCompletion(task);
                    } catch (JSONException e) {
                        throw new RuntimeException(e);
                    }
                },
                task -> {
                    new AlertDialog.Builder(requireContext())
                            .setTitle("Delete Task")
                            .setMessage("Are you sure you want to delete \"" + task.getTitle() + "\"?")
                            .setPositiveButton("Delete", (dialog, which) -> {
                                viewModel.deleteTask(task, () -> {
                                    Toast.makeText(getContext(), "Task deleted", Toast.LENGTH_SHORT).show();
                                }, error -> {
                                    Toast.makeText(getContext(), error, Toast.LENGTH_SHORT).show();
                                },getCurrentUserId());
                            })
                            .setNegativeButton("Cancel", null)
                            .show();
                }
        );
        binding.tasksRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        binding.tasksRecyclerView.setAdapter(taskAdapter);
    }

    private void setupObservers() {
        viewModel.getTasks().observe(getViewLifecycleOwner(), tasks -> {
            Log.d(TAG, "Tasks LiveData updated: " + (tasks != null ? tasks.toString() : "null"));
            taskAdapter.updateTasks(tasks);
            binding.tasksRecyclerView.setVisibility(tasks != null && !tasks.isEmpty() ? View.VISIBLE : View.GONE);
            binding.emptyTextView.setVisibility(tasks != null && !tasks.isEmpty() ? View.GONE : View.VISIBLE);
        });

        viewModel.getIsLoading().observe(getViewLifecycleOwner(), isLoading -> {
            if (isLoading && (viewModel.getTasks().getValue() == null || viewModel.getTasks().getValue().isEmpty())) {
                binding.progressBar.setVisibility(View.VISIBLE);
            } else {
                binding.progressBar.setVisibility(View.GONE);
            }
            Log.d(TAG, "Loading state: " + isLoading);
        });

        viewModel.getErrorMessage().observe(getViewLifecycleOwner(), error -> {
            if (error != null && !error.isEmpty()) {
                Toast.makeText(getContext(), error, Toast.LENGTH_SHORT).show();
                Log.e(TAG, "Error: " + error);
            }
        });
    }

    private void setupFab() {
        binding.fabAddTask.setOnClickListener(v -> {
            AddTaskDialogFragment dialog = AddTaskDialogFragment.newInstance();
            dialog.setOnTaskAddedListener(task -> viewModel.addTask(task,getCurrentUserId()));
            dialog.show(getChildFragmentManager(), "AddTaskDialog");
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

    private String getCurrentUserId() {
        SharedPreferences prefs = requireContext().getSharedPreferences("app_prefs", Context.MODE_PRIVATE);
        String userId = prefs.getString("userId", null);
        if (userId == null) {
            Log.e(TAG, "No user ID found in SharedPreferences");
            // Optionally, trigger a navigation to login screen
            // For now, return null to let caller handle it
        }
        return userId;
    }
}
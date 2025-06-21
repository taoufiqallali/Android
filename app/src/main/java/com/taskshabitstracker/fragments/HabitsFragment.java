package com.taskshabitstracker.fragments;

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
import com.taskshabitstracker.adapters.HabitsAdapter;
import com.taskshabitstracker.databinding.FragmentHabitsBinding;
import com.taskshabitstracker.model.Habit;
import com.taskshabitstracker.viewmodel.HabitsViewModel;

public class HabitsFragment extends Fragment {
    private static final String TAG = "HabitsFragment";

    private FragmentHabitsBinding binding;
    private HabitsViewModel viewModel;
    private HabitsAdapter habitsAdapter;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentHabitsBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        Log.d(TAG, "onViewCreated: Initializing");

        viewModel = new ViewModelProvider(this).get(HabitsViewModel.class);
        setupRecyclerView();
        setupObservers();
        setupFab();
        viewModel.loadHabits();
    }

    private void setupRecyclerView() {
        habitsAdapter = new HabitsAdapter(
                habit -> viewModel.toggleHabitCompletion(habit),
                habit -> {
                    new AlertDialog.Builder(requireContext())
                            .setTitle("Delete Habit")
                            .setMessage("Are you sure you want to delete \"" + habit.getTitle() + "\"?")
                            .setPositiveButton("Delete", (dialog, which) -> {
                                viewModel.deleteHabit(habit, () -> {
                                    Toast.makeText(getContext(), "Habit deleted", Toast.LENGTH_SHORT).show();
                                }, error -> {
                                    Toast.makeText(getContext(), error, Toast.LENGTH_SHORT).show();
                                });
                            })
                            .setNegativeButton("Cancel", null)
                            .show();
                }
        );
        binding.habitsRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        binding.habitsRecyclerView.setAdapter(habitsAdapter);
    }

    private void setupObservers() {
        viewModel.getHabits().observe(getViewLifecycleOwner(), habits -> {
            Log.d(TAG, "Habits LiveData updated: " + (habits != null ? habits.toString() : "null"));
            habitsAdapter.updateHabits(habits);
            binding.habitsRecyclerView.setVisibility(habits != null && !habits.isEmpty() ? View.VISIBLE : View.GONE);
            binding.emptyHabitsTextView.setVisibility(habits != null && !habits.isEmpty() ? View.GONE : View.VISIBLE);
        });

        viewModel.getIsLoading().observe(getViewLifecycleOwner(), isLoading -> {
            if (isLoading && (viewModel.getHabits().getValue() == null || viewModel.getHabits().getValue().isEmpty())) {
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
        binding.fabAddHabit.setOnClickListener(v -> {
            AddHabitDialogFragment dialog = AddHabitDialogFragment.newInstance();
            dialog.setOnHabitAddedListener(habit -> viewModel.addHabit(habit));
            dialog.show(getChildFragmentManager(), "AddHabitDialog");
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
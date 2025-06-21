// HabitsFragment.java - You mentioned you have DashboardFragment and TasksFragment, but need HabitsFragment
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
import com.taskshabitstracker.adapters.HabitsAdapter;
import com.taskshabitstracker.databinding.FragmentHabitsBinding;
import com.taskshabitstracker.viewmodel.HabitsViewModel;

public class HabitsFragment extends Fragment {
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

        viewModel = new ViewModelProvider(this).get(HabitsViewModel.class);
        setupRecyclerView();
        setupObservers();
        setupFab();
        viewModel.loadHabits();
    }

    private void setupRecyclerView() {
        habitsAdapter = new HabitsAdapter(
                habit -> viewModel.toggleHabitCompletion(habit),
                habit -> viewModel.deleteHabit(habit)
        );

        binding.habitsRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        binding.habitsRecyclerView.setAdapter(habitsAdapter);
    }

    private void setupObservers() {
        viewModel.getHabits().observe(getViewLifecycleOwner(), habits -> {
            habitsAdapter.updateHabits(habits);

            if (habits.isEmpty()) {
                binding.emptyHabitsTextView.setVisibility(View.VISIBLE);
                binding.habitsRecyclerView.setVisibility(View.GONE);
            } else {
                binding.emptyHabitsTextView.setVisibility(View.GONE);
                binding.habitsRecyclerView.setVisibility(View.VISIBLE);
            }
        });
    }

    private void setupFab() {
        binding.fabAddHabit.setOnClickListener(v -> {
            // TODO: Open add habit dialog
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
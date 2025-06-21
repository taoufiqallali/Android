package com.taskshabitstracker.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.taskshabitstracker.databinding.DialogAddHabitBinding;
import com.taskshabitstracker.model.Habit;

public class AddHabitDialogFragment extends BottomSheetDialogFragment {
    private DialogAddHabitBinding binding;
    private OnHabitAddedListener onHabitAddedListener;

    public interface OnHabitAddedListener {
        void onHabitAdded(Habit habit);
    }

    public static AddHabitDialogFragment newInstance() {
        return new AddHabitDialogFragment();
    }

    public void setOnHabitAddedListener(OnHabitAddedListener listener) {
        this.onHabitAddedListener = listener;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = DialogAddHabitBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        binding.btnSave.setOnClickListener(v -> {
            String name = binding.etHabitName.getText().toString().trim();
            String description = binding.etHabitDescription.getText().toString().trim();

            if (name.isEmpty()) {
                binding.etHabitName.setError("Habit name is required");
                return;
            }

            Habit habit = new Habit(name, description);
            if (onHabitAddedListener != null) {
                onHabitAddedListener.onHabitAdded(habit);
            }
            dismiss();
        });

        binding.btnCancel.setOnClickListener(v -> dismiss());
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
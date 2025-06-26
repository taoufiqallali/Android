package com.taskshabitstracker.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.taskshabitstracker.model.Habit;
import com.taskshabitstracker.R;

public class AddHabitDialogFragment extends BottomSheetDialogFragment {
    private OnHabitAddedListener listener;

    public interface OnHabitAddedListener {
        void onHabitAdded(Habit habit);
    }

    public static AddHabitDialogFragment newInstance() {
        return new AddHabitDialogFragment();
    }

    public void setOnHabitAddedListener(OnHabitAddedListener listener) {
        this.listener = listener;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.dialog_add_habit, container, false);
        EditText nameInput = view.findViewById(R.id.habit_name_input);
        EditText descriptionInput = view.findViewById(R.id.habit_description_input);
        Button saveButton = view.findViewById(R.id.save_habit_button);

        saveButton.setOnClickListener(v -> {
            String name = nameInput.getText().toString().trim();
            String description = descriptionInput.getText().toString().trim();
            if (!name.isEmpty()) {
                Habit habit = new Habit("", name, description, 0, false);
                listener.onHabitAdded(habit);
                dismiss();
            }
        });

        return view;
    }
}
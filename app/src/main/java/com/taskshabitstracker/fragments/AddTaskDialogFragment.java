package com.taskshabitstracker.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;
import com.taskshabitstracker.databinding.DialogAddTaskBinding;
import com.taskshabitstracker.model.Task;

/**
 * Dialog for adding a new task
 */
public class AddTaskDialogFragment extends DialogFragment {
    private DialogAddTaskBinding binding;
    private OnTaskAddedListener listener;

    // Interface for callback to Fragment
    public interface OnTaskAddedListener {
        void onTaskAdded(Task task);
    }

    public static AddTaskDialogFragment newInstance() {
        return new AddTaskDialogFragment();
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setStyle(STYLE_NORMAL, android.R.style.Theme_Material_Light_Dialog);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = DialogAddTaskBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Set up buttons
        binding.btnSave.setOnClickListener(v -> saveTask());
        binding.btnCancel.setOnClickListener(v -> dismiss());
    }

    private void saveTask() {
        String title = binding.etTaskTitle.getText().toString().trim();
        String description = binding.etTaskDescription.getText().toString().trim();

        if (title.isEmpty()) {
            binding.etTaskTitle.setError("Title is required");
            return;
        }

        Task task = new Task(title, description);
        if (listener != null) {
            listener.onTaskAdded(task);
        }
        dismiss();
    }

    public void setOnTaskAddedListener(OnTaskAddedListener listener) {
        this.listener = listener;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
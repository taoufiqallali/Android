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
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

public class AddTaskDialogFragment extends DialogFragment {
    private DialogAddTaskBinding binding;
    private OnTaskAddedListener listener;

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
        binding.btnSave.setOnClickListener(v -> saveTask());
        binding.btnCancel.setOnClickListener(v -> dismiss());
        binding.etDueDate.setOnClickListener(v -> showDatePicker());
    }

    private void showDatePicker() {
        Calendar calendar = Calendar.getInstance();
        new android.app.DatePickerDialog(requireContext(),
                (view, year, month, dayOfMonth) -> {
                    Calendar selectedDate = Calendar.getInstance();
                    selectedDate.set(year, month, dayOfMonth);
                    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
                    binding.etDueDate.setText(sdf.format(selectedDate.getTime()));
                },
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH)).show();
    }

    private void saveTask() {
        String title = binding.etTaskTitle.getText().toString().trim();
        String description = binding.etTaskDescription.getText().toString().trim();
        String dueDateStr = binding.etDueDate.getText().toString().trim();
        boolean enableDueDateNotifications = binding.cbDueDateNotifications.isChecked();
        boolean enablePreDueNotifications = binding.cbPreDueNotifications.isChecked();

        if (title.isEmpty()) {
            binding.etTaskTitle.setError("Title is required");
            return;
        }

        if (!dueDateStr.isEmpty()) {
            try {
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
                Calendar selectedDate = Calendar.getInstance();
                selectedDate.setTime(sdf.parse(dueDateStr));
                Calendar today = Calendar.getInstance();
                today.set(Calendar.HOUR_OF_DAY, 0);
                today.set(Calendar.MINUTE, 0);
                today.set(Calendar.SECOND, 0);
                today.set(Calendar.MILLISECOND, 0);
                selectedDate.set(Calendar.HOUR_OF_DAY, 0);
                selectedDate.set(Calendar.MINUTE, 0);
                selectedDate.set(Calendar.SECOND, 0);
                selectedDate.set(Calendar.MILLISECOND, 0);
                if (selectedDate.before(today)) {
                    binding.etDueDate.setError("Due date cannot be in the past");
                    return;
                }
            } catch (Exception e) {
                binding.etDueDate.setError("Invalid date format");
                return;
            }
        }

        Task task = new Task(title, description, dueDateStr.isEmpty() ? null : dueDateStr);
        task.setEnableDueDateNotifications(enableDueDateNotifications);
        task.setEnablePreDueNotifications(enablePreDueNotifications);
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
package com.taskshabitstracker.adapters;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;
import com.taskshabitstracker.databinding.ItemTaskBinding;
import com.taskshabitstracker.model.Task;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

public class TaskAdapter extends RecyclerView.Adapter<TaskAdapter.TaskViewHolder> {
    private static final String TAG = "TaskAdapter";
    private List<Task> tasks = new ArrayList<>();
    private final OnTaskToggleListener toggleListener;
    private final OnTaskDeleteListener deleteListener;

    public interface OnTaskToggleListener {
        void onToggle(Task task);
    }

    public interface OnTaskDeleteListener {
        void onDelete(Task task);
    }

    public TaskAdapter(OnTaskToggleListener toggleListener, OnTaskDeleteListener deleteListener) {
        this.toggleListener = toggleListener;
        this.deleteListener = deleteListener;
    }

    public void updateTasks(List<Task> newTasks) {
        this.tasks = newTasks != null ? new ArrayList<>(newTasks) : new ArrayList<>();
        notifyDataSetChanged();
        Log.d(TAG, "Adapter updated with " + tasks.size() + " tasks");
    }

    @NonNull
    @Override
    public TaskViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        ItemTaskBinding binding = ItemTaskBinding.inflate(inflater, parent, false);
        return new TaskViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull TaskViewHolder holder, int position) {
        holder.bind(tasks.get(position));
    }

    @Override
    public int getItemCount() {
        return tasks.size();
    }

    class TaskViewHolder extends RecyclerView.ViewHolder {
        private final ItemTaskBinding binding;

        TaskViewHolder(ItemTaskBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        void bind(Task task) {
            binding.tvTaskTitle.setText(task.getTitle());
            binding.tvTaskDescription.setText(task.getDescription());
            binding.tvTaskDescription.setVisibility(task.getDescription().isEmpty() ? View.GONE : View.VISIBLE);
            binding.cbTaskCompleted.setChecked(task.isCompleted());

            // Display due date
            String dueDateStr = task.getDueDate();
            if (dueDateStr != null && !dueDateStr.isEmpty()) {
                binding.tvDueDate.setText("Due: " + dueDateStr);
                binding.tvDueDate.setVisibility(View.VISIBLE);

                // Check if task is overdue
                try {
                    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
                    Calendar dueCalendar = Calendar.getInstance();
                    dueCalendar.setTime(sdf.parse(dueDateStr));
                    dueCalendar.set(Calendar.HOUR_OF_DAY, 0);
                    dueCalendar.set(Calendar.MINUTE, 0);
                    dueCalendar.set(Calendar.SECOND, 0);
                    dueCalendar.set(Calendar.MILLISECOND, 0);

                    Calendar today = Calendar.getInstance();
                    today.set(Calendar.HOUR_OF_DAY, 0);
                    today.set(Calendar.MINUTE, 0);
                    today.set(Calendar.SECOND, 0);
                    today.set(Calendar.MILLISECOND, 0);

                    if (!task.isCompleted() && dueCalendar.before(today)) {
                        binding.tvDueDate.setTextColor(ContextCompat.getColor(binding.getRoot().getContext(), android.R.color.holo_red_dark));
                    } else {
                        binding.tvDueDate.setTextColor(ContextCompat.getColor(binding.getRoot().getContext(), android.R.color.black));
                    }
                } catch (Exception e) {
                    Log.e(TAG, "Error parsing due date for task ID: " + task.getId(), e);
                    binding.tvDueDate.setVisibility(View.GONE);
                }
            } else {
                binding.tvDueDate.setVisibility(View.GONE);
            }

            binding.cbTaskCompleted.setOnClickListener(v -> {
                Log.d(TAG, "Toggling task ID: " + task.getId());
                if (toggleListener != null) {
                    toggleListener.onToggle(task);
                }
            });

            binding.btnDeleteTask.setOnClickListener(v -> {
                int position = getBindingAdapterPosition();
                if (position != RecyclerView.NO_POSITION && deleteListener != null) {
                    Log.d(TAG, "Deleting task ID: " + task.getId() + " at position: " + position);
                    deleteListener.onDelete(tasks.get(position));
                }
            });
        }
    }
}
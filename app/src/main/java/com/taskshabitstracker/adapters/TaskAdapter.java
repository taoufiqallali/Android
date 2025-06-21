package com.taskshabitstracker.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.taskshabitstracker.R;
import com.taskshabitstracker.model.Task;

import java.util.ArrayList;
import java.util.List;

public class TaskAdapter extends RecyclerView.Adapter<TaskAdapter.TaskViewHolder> {
    private List<Task> tasks;
    private OnTaskClickListener onTaskClickListener;
    private OnTaskDeleteListener onTaskDeleteListener;

    // Callback interfaces
    public interface OnTaskClickListener {
        void onTaskClick(Task task);
    }

    public interface OnTaskDeleteListener {
        void onTaskDelete(Task task);
    }

    // Updated constructor to accept callback interfaces
    public TaskAdapter(OnTaskClickListener onTaskClickListener, OnTaskDeleteListener onTaskDeleteListener) {
        this.tasks = new ArrayList<>();
        this.onTaskClickListener = onTaskClickListener;
        this.onTaskDeleteListener = onTaskDeleteListener;
    }

    public void updateTasks(List<Task> newTasks) {
        this.tasks = newTasks != null ? newTasks : new ArrayList<>();
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public TaskViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_task, parent, false);
        return new TaskViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull TaskViewHolder holder, int position) {
        Task task = tasks.get(position);
        holder.titleTextView.setText(task.getTitle());
        holder.descriptionTextView.setText(task.getDescription());

        // Set click listeners
        holder.itemView.setOnClickListener(v -> {
            if (onTaskClickListener != null) {
                onTaskClickListener.onTaskClick(task);
            }
        });

        // You can add a delete button or long press listener for delete functionality
        holder.itemView.setOnLongClickListener(v -> {
            if (onTaskDeleteListener != null) {
                onTaskDeleteListener.onTaskDelete(task);
            }
            return true;
        });
    }

    @Override
    public int getItemCount() {
        return tasks.size();
    }

    static class TaskViewHolder extends RecyclerView.ViewHolder {
        TextView titleTextView, descriptionTextView;

        TaskViewHolder(View itemView) {
            super(itemView);
            titleTextView = itemView.findViewById(R.id.taskTitle);
            descriptionTextView = itemView.findViewById(R.id.taskDescription);
        }
    }
}
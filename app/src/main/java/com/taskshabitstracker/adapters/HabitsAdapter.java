package com.taskshabitstracker.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.taskshabitstracker.model.Habit;
import com.taskshabitstracker.R;

import java.util.ArrayList;
import java.util.List;

public class HabitsAdapter extends RecyclerView.Adapter<HabitsAdapter.HabitViewHolder> {
    private List<Habit> habits;
    private OnHabitClickListener onHabitClickListener;
    private OnHabitDeleteListener onHabitDeleteListener;

    // Callback interfaces
    public interface OnHabitClickListener {
        void onHabitClick(Habit habit);
    }

    public interface OnHabitDeleteListener {
        void onHabitDelete(Habit habit);
    }

    // Updated constructor to accept callback interfaces
    public HabitsAdapter(OnHabitClickListener onHabitClickListener, OnHabitDeleteListener onHabitDeleteListener) {
        this.habits = new ArrayList<>();
        this.onHabitClickListener = onHabitClickListener;
        this.onHabitDeleteListener = onHabitDeleteListener;
    }

    public void updateHabits(List<Habit> newHabits) {
        this.habits = newHabits != null ? newHabits : new ArrayList<>();
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public HabitViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_habit, parent, false);
        return new HabitViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull HabitViewHolder holder, int position) {
        Habit habit = habits.get(position);
        holder.titleTextView.setText(habit.getTitle());
        holder.descriptionTextView.setText(habit.getDescription());

        // Set click listeners
        holder.itemView.setOnClickListener(v -> {
            if (onHabitClickListener != null) {
                onHabitClickListener.onHabitClick(habit);
            }
        });

        // You can add a delete button or long press listener for delete functionality
        holder.itemView.setOnLongClickListener(v -> {
            if (onHabitDeleteListener != null) {
                onHabitDeleteListener.onHabitDelete(habit);
            }
            return true;
        });
    }

    @Override
    public int getItemCount() {
        return habits.size();
    }

    static class HabitViewHolder extends RecyclerView.ViewHolder {
        TextView titleTextView, descriptionTextView;

        HabitViewHolder(View itemView) {
            super(itemView);
            titleTextView = itemView.findViewById(R.id.habitTitle);
            descriptionTextView = itemView.findViewById(R.id.habitDescription);
        }
    }
}
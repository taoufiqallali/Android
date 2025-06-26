package com.taskshabitstracker.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.taskshabitstracker.model.Habit;
import java.util.ArrayList;
import java.util.List;

public class HabitAdapter extends RecyclerView.Adapter<HabitAdapter.HabitViewHolder> {
    private List<Habit> habits = new ArrayList<>();
    private final OnHabitClickListener onToggleListener;
    private final OnHabitClickListener onDeleteListener;

    public interface OnHabitClickListener {
        void onClick(Habit habit);
    }

    public HabitAdapter(OnHabitClickListener onToggleListener, OnHabitClickListener onDeleteListener) {
        this.onToggleListener = onToggleListener;
        this.onDeleteListener = onDeleteListener;
    }

    public void updateHabits(List<Habit> newHabits) {
        this.habits = newHabits != null ? new ArrayList<>(newHabits) : new ArrayList<>();
        // Sort by streak descending
        this.habits.sort((h1, h2) -> Integer.compare(h2.getStreak(), h1.getStreak()));
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public HabitViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(android.R.layout.simple_list_item_2, parent, false);
        return new HabitViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull HabitViewHolder holder, int position) {
        Habit habit = habits.get(position);
        holder.nameText.setText(habit.getName());
        holder.descriptionText.setText("Streak: " + habit.getStreak() + " | Completed Today: " + (habit.isCompletedToday() ? "Yes" : "No"));
        holder.itemView.setOnClickListener(v -> onToggleListener.onClick(habit));
        holder.itemView.setOnLongClickListener(v -> {
            onDeleteListener.onClick(habit);
            return true;
        });
    }

    @Override
    public int getItemCount() {
        return habits.size();
    }

    static class HabitViewHolder extends RecyclerView.ViewHolder {
        TextView nameText, descriptionText;

        HabitViewHolder(View itemView) {
            super(itemView);
            nameText = itemView.findViewById(android.R.id.text1);
            descriptionText = itemView.findViewById(android.R.id.text2);
        }
    }
}
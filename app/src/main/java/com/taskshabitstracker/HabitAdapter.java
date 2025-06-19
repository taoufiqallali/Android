package com.taskshabitstracker;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class HabitAdapter extends RecyclerView.Adapter<HabitAdapter.HabitViewHolder> {
    private List<Habit> habits;
    private TextView emptyTextView;
    private RecyclerView recyclerView;

    public HabitAdapter(List<Habit> habits, TextView emptyTextView, RecyclerView recyclerView) {
        this.habits = habits;
        this.emptyTextView = emptyTextView;
        this.recyclerView = recyclerView;
    }

    public void updateHabits(List<Habit> newHabits) {
        this.habits = newHabits;
        notifyDataSetChanged();
        if (newHabits.isEmpty()) {
            recyclerView.setVisibility(View.GONE);
            emptyTextView.setVisibility(View.VISIBLE);
        } else {
            recyclerView.setVisibility(View.VISIBLE);
            emptyTextView.setVisibility(View.GONE);
        }
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
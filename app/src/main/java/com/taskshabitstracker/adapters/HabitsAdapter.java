package com.taskshabitstracker.adapters;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.taskshabitstracker.databinding.ItemHabitBinding;
import com.taskshabitstracker.model.Habit;
import java.util.ArrayList;
import java.util.List;

public class HabitsAdapter extends RecyclerView.Adapter<HabitsAdapter.HabitViewHolder> {
    private static final String TAG = "HabitsAdapter";
    private List<Habit> habits = new ArrayList<>();
    private final OnHabitClickListener onHabitClickListener;
    private final OnHabitDeleteListener onHabitDeleteListener;

    public interface OnHabitClickListener {
        void onHabitClick(Habit habit);
    }

    public interface OnHabitDeleteListener {
        void onHabitDelete(Habit habit);
    }

    public HabitsAdapter(OnHabitClickListener onHabitClickListener, OnHabitDeleteListener onHabitDeleteListener) {
        this.onHabitClickListener = onHabitClickListener;
        this.onHabitDeleteListener = onHabitDeleteListener;
    }

    public void updateHabits(List<Habit> newHabits) {
        this.habits = newHabits != null ? new ArrayList<>(newHabits) : new ArrayList<>();
        notifyDataSetChanged();
        Log.d(TAG, "Adapter updated with " + habits.size() + " habits");
    }

    @NonNull
    @Override
    public HabitViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        ItemHabitBinding binding = ItemHabitBinding.inflate(inflater, parent, false);
        return new HabitViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull HabitViewHolder holder, int position) {
        holder.bind(habits.get(position));
    }

    @Override
    public int getItemCount() {
        return habits.size();
    }

    class HabitViewHolder extends RecyclerView.ViewHolder {
        private final ItemHabitBinding binding;

        HabitViewHolder(ItemHabitBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        void bind(Habit habit) {
            binding.habitTitle.setText(habit.getTitle());
            binding.habitDescription.setText(habit.getDescription());
            binding.habitDescription.setVisibility(habit.getDescription().isEmpty() ? View.GONE : View.VISIBLE);
            binding.cbHabitCompleted.setChecked(habit.isCompletedToday());
            binding.tvHabitStreak.setText("Streak: " + habit.getStreak());

            // Click listener for checkbox to toggle completion
            binding.cbHabitCompleted.setOnClickListener(v -> {
                Log.d(TAG, "Toggling habit ID: " + habit.getId());
                if (onHabitClickListener != null) {
                    onHabitClickListener.onHabitClick(habit);
                }
            });

            // Click listener for delete button
            binding.btnDeleteHabit.setOnClickListener(v -> {
                int position = getAdapterPosition();
                if (position != RecyclerView.NO_POSITION && onHabitDeleteListener != null) {
                    Log.d(TAG, "Deleting habit ID: " + habit.getId() + " at position: " + position);
                    onHabitDeleteListener.onHabitDelete(habits.get(position));
                }
            });
        }
    }
}
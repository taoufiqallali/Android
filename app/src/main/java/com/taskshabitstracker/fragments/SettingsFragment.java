package com.taskshabitstracker.fragments;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import com.taskshabitstracker.databinding.FragmentSettingsBinding;

public class SettingsFragment extends Fragment {
    private FragmentSettingsBinding binding;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = FragmentSettingsBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Load settings from SharedPreferences
        SharedPreferences prefs = requireContext().getSharedPreferences("TasksPrefs", requireContext().MODE_PRIVATE);
        binding.cbStreaks.setChecked(prefs.getBoolean("enableStreakNotifications", true));
        binding.cbMilestones.setChecked(prefs.getBoolean("enableMilestoneNotifications", true));
        binding.cbWeeklySummary.setChecked(prefs.getBoolean("enableWeeklySummary", true));
        binding.cbInactivity.setChecked(prefs.getBoolean("enableInactivityNotifications", true));

        // Save settings
        binding.btnSaveSettings.setOnClickListener(v -> saveSettings());
    }

    private void saveSettings() {
        SharedPreferences prefs = requireContext().getSharedPreferences("TasksPrefs", requireContext().MODE_PRIVATE);
        prefs.edit()
                .putBoolean("enableStreakNotifications", binding.cbStreaks.isChecked())
                .putBoolean("enableMilestoneNotifications", binding.cbMilestones.isChecked())
                .putBoolean("enableWeeklySummary", binding.cbWeeklySummary.isChecked())
                .putBoolean("enableInactivityNotifications", binding.cbInactivity.isChecked())
                .apply();
        android.widget.Toast.makeText(getContext(), "Settings saved", android.widget.Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
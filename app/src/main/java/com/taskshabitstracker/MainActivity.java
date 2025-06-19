package com.taskshabitstracker;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.JsonObjectRequest;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = "MainActivity";
    private static final String PREFS_NAME = "TasksHabitsPrefs";
    private static final String EMAIL_KEY = "user_email";
    private static final String SESSION_KEY = "user_session";

    private SharedPreferences sharedPreferences;
    private TextView emptyTextView;
    private RecyclerView tasksRecyclerView;

    private RecyclerView habitsRecyclerView;
    private TaskAdapter taskAdapter;
    private FloatingActionButton fabAddTask;
    private MaterialToolbar topAppBar;
    private BottomNavigationView bottomNav;

    private View dashboardView;

    private View tasksView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Initialize SharedPreferences
        sharedPreferences = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);

        // Check if user is logged in
        if (!isUserLoggedIn()) {
            redirectToLogin();
            return;
        }

        // Initialize UI elements
        topAppBar = findViewById(R.id.topAppBar);
        emptyTextView = findViewById(R.id.emptyTextView);
        tasksRecyclerView = findViewById(R.id.tasksRecyclerView);
        fabAddTask = findViewById(R.id.fabAddTask);
        bottomNav = findViewById(R.id.bottomNav);

        initializeViews();

        showDashboard();

        // Initialize RecyclerView
        taskAdapter = new TaskAdapter(new ArrayList<>(), emptyTextView, tasksRecyclerView);
        tasksRecyclerView.setAdapter(taskAdapter);

        // Set up toolbar
        setSupportActionBar(topAppBar);

        // Set up FAB
        fabAddTask.setOnClickListener(v -> {
            Toast.makeText(this, "Add task clicked", Toast.LENGTH_SHORT).show();
            // TODO: Start activity or dialog to add task
        });

        // Set up bottom navigation
        bottomNav.setOnItemSelectedListener(item -> {
            int itemId = item.getItemId();
            if (itemId == R.id.nav_dashboard) {
                showDashboard();
                topAppBar.setTitle("Dashboard");
                return true;
            } else if (itemId == R.id.nav_tasks) {
                showTasks();
                topAppBar.setTitle("Tasks");
                return true;
            } else if (itemId == R.id.nav_habits) {
                showHabits();
                topAppBar.setTitle("Habits");
                return true;
            } else if (itemId == R.id.nav_profile) {
                topAppBar.setTitle("Profile");
                return true;
            }
            return false;
        });

        bottomNav.setSelectedItemId(R.id.nav_dashboard);

    }

    private void initializeViews() {
        // Inflate dashboard layout
        LayoutInflater inflater = getLayoutInflater();
        dashboardView = inflater.inflate(R.layout.layout_dashboard, null);

        // Create tasks view (your existing RecyclerView setup)
        tasksView  = inflater.inflate(R.layout.layout_dashboard, null);
    }
    private void showDashboard() {
        // Hide other views
        findViewById(R.id.main_container).setVisibility(View.GONE);

        // Show dashboard
        if (dashboardView.getParent() != null) {
            ((ViewGroup) dashboardView.getParent()).removeView(dashboardView);
        }
        // Add dashboard to the main layout
        androidx.constraintlayout.widget.ConstraintLayout mainLayout = findViewById(R.id.activity_main);
        androidx.constraintlayout.widget.ConstraintLayout.LayoutParams params =
                new androidx.constraintlayout.widget.ConstraintLayout.LayoutParams(
                        androidx.constraintlayout.widget.ConstraintLayout.LayoutParams.MATCH_PARENT,
                        androidx.constraintlayout.widget.ConstraintLayout.LayoutParams.MATCH_CONSTRAINT);

        params.topToBottom = R.id.topAppBar;
        params.bottomToTop = R.id.bottomNav;
        params.startToStart = androidx.constraintlayout.widget.ConstraintLayout.LayoutParams.PARENT_ID;
        params.endToEnd = androidx.constraintlayout.widget.ConstraintLayout.LayoutParams.PARENT_ID;

        mainLayout.addView(dashboardView, params);

        // Initialize dashboard data
        initializeDashboardData();
    }

    private void showTasks() {
        // Remove dashboard if present
        if (dashboardView.getParent() != null) {
            ((ViewGroup) dashboardView.getParent()).removeView(dashboardView);
        }

        // Inflate tasks layout
        LayoutInflater inflater = getLayoutInflater();
        View tasksView = inflater.inflate(R.layout.tasks_layout, null);

        // Add tasks view to the main layout
        androidx.constraintlayout.widget.ConstraintLayout mainLayout = findViewById(R.id.activity_main);
        androidx.constraintlayout.widget.ConstraintLayout.LayoutParams params =
                new androidx.constraintlayout.widget.ConstraintLayout.LayoutParams(
                        androidx.constraintlayout.widget.ConstraintLayout.LayoutParams.MATCH_PARENT,
                        androidx.constraintlayout.widget.ConstraintLayout.LayoutParams.MATCH_CONSTRAINT);
        params.topToBottom = R.id.topAppBar;
        params.bottomToTop = R.id.bottomNav;
        params.startToStart = androidx.constraintlayout.widget.ConstraintLayout.LayoutParams.PARENT_ID;
        params.endToEnd = androidx.constraintlayout.widget.ConstraintLayout.LayoutParams.PARENT_ID;

        // Remove any existing tasks view to avoid duplicates
        View existingTasksView = findViewById(R.id.main_container);
        if (existingTasksView != null && existingTasksView.getParent() != null) {
            ((ViewGroup) existingTasksView.getParent()).removeView(existingTasksView);
        }

        mainLayout.addView(tasksView, params);

        // Initialize RecyclerView and TextView from the new layout
        tasksRecyclerView = tasksView.findViewById(R.id.tasksRecyclerView);
        emptyTextView = tasksView.findViewById(R.id.emptyTextView);

        // Re-attach adapter to the new RecyclerView
        tasksRecyclerView.setAdapter(taskAdapter);
    }

    private void showHabits() {
        // Remove dashboard if present
        if (dashboardView.getParent() != null) {
            ((ViewGroup) dashboardView.getParent()).removeView(dashboardView);
        }

        // Remove tasks view if present
        View existingTasksView = findViewById(R.id.main_container);
        if (existingTasksView != null && existingTasksView.getParent() != null) {
            ((ViewGroup) existingTasksView.getParent()).removeView(existingTasksView);
        }

        // Inflate habits layout
        LayoutInflater inflater = getLayoutInflater();
        View habitsView;
        try {
            habitsView = inflater.inflate(R.layout.habits_layout, null);
        } catch (Exception e) {
            Log.e(TAG, "Failed to inflate habits_layout.xml: " + e.getMessage());
            Toast.makeText(this, "Error loading habits view", Toast.LENGTH_SHORT).show();
            return;
        }

        // Add habits view to the main layout
        androidx.constraintlayout.widget.ConstraintLayout mainLayout = findViewById(R.id.activity_main);
        if (mainLayout == null) {
            Log.e(TAG, "Main layout (R.id.activity_main) not found");
            Toast.makeText(this, "Error loading main layout", Toast.LENGTH_SHORT).show();
            return;
        }

        androidx.constraintlayout.widget.ConstraintLayout.LayoutParams params =
                new androidx.constraintlayout.widget.ConstraintLayout.LayoutParams(
                        androidx.constraintlayout.widget.ConstraintLayout.LayoutParams.MATCH_PARENT,
                        androidx.constraintlayout.widget.ConstraintLayout.LayoutParams.MATCH_CONSTRAINT);
        params.topToBottom = R.id.topAppBar;
        params.bottomToTop = R.id.bottomNav;
        params.startToStart = androidx.constraintlayout.widget.ConstraintLayout.LayoutParams.PARENT_ID;
        params.endToEnd = androidx.constraintlayout.widget.ConstraintLayout.LayoutParams.PARENT_ID;

        mainLayout.addView(habitsView, params);

        // Initialize RecyclerView and TextView from the new layout
        RecyclerView habitsRecyclerViewLocal = habitsView.findViewById(R.id.habitsRecyclerView);
        TextView emptyHabitsTextView = habitsView.findViewById(R.id.emptyHabitsTextView);

        if (habitsRecyclerViewLocal == null || emptyHabitsTextView == null) {
            Log.e(TAG, "Failed to find habitsRecyclerView or emptyHabitsTextView in habits_layout.xml");
            Toast.makeText(this, "Error initializing habits view", Toast.LENGTH_SHORT).show();
            return;
        }

        // Initialize HabitAdapter
        HabitAdapter habitAdapter = new HabitAdapter(new ArrayList<>(), emptyHabitsTextView, habitsRecyclerViewLocal);
        habitsRecyclerViewLocal.setAdapter(habitAdapter);

        Log.d(TAG, "Habits view successfully initialized");
    }

    private void showProfile() {
        // Remove dashboard if present
        if (dashboardView.getParent() != null) {
            ((ViewGroup) dashboardView.getParent()).removeView(dashboardView);
        }

        // Hide tasks container
        findViewById(R.id.main_container).setVisibility(View.GONE);

        Toast.makeText(this, "Profile view - Coming soon!", Toast.LENGTH_SHORT).show();
    }

    private void initializeDashboardData() {
        // Initialize dashboard components
        TextView welcomeText = dashboardView.findViewById(R.id.welcomeText);
        TextView dateText = dashboardView.findViewById(R.id.dateText);
        TextView pointsValue = dashboardView.findViewById(R.id.pointsValue);
        TextView streakValue = dashboardView.findViewById(R.id.streakValue);
        TextView tasksCompletedText = dashboardView.findViewById(R.id.tasksCompletedText);
        TextView habitsCompletedText = dashboardView.findViewById(R.id.habitsCompletedText);

        // Set current date
        java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("EEEE, MMMM d, yyyy", java.util.Locale.getDefault());
        dateText.setText(sdf.format(new java.util.Date()));

        // Set initial values (you'll replace these with actual data)
        pointsValue.setText("0");
        streakValue.setText("0");
        tasksCompletedText.setText("0/0");
        habitsCompletedText.setText("0/0");

        // Set up quick action buttons
        MaterialButton addTaskButton = dashboardView.findViewById(R.id.addTaskButton);
        MaterialButton addHabitButton = dashboardView.findViewById(R.id.addHabitButton);

        addTaskButton.setOnClickListener(v -> {
            Toast.makeText(this, "Add Task clicked", Toast.LENGTH_SHORT).show();
            // TODO: Open add task dialog/activity
        });

        addHabitButton.setOnClickListener(v -> {
            Toast.makeText(this, "Add Habit clicked", Toast.LENGTH_SHORT).show();
            // TODO: Open add habit dialog/activity
        });
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.top_app_bar_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == R.id.action_logout) {
            logout();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }


    private boolean isUserLoggedIn() {
        String email = sharedPreferences.getString(EMAIL_KEY, null);
        String session = sharedPreferences.getString(SESSION_KEY, null);
        return email != null && !email.isEmpty() && session != null && !session.isEmpty();
    }

    private void logout() {
        RequestQueue queue = VolleySingleton.getInstance(this).getRequestQueue();
        String url = "http://10.0.2.2:8080/api/auth/logout";

        JsonObjectRequest request = new JsonObjectRequest(
                Request.Method.POST,
                url,
                null,
                response -> {
                    Log.d(TAG, "Logout successful: " + response.toString());
                    clearSession();
                    redirectToLogin();
                },
                error -> {
                    Log.e(TAG, "Logout error: " + error.toString());
                    clearSession();
                    redirectToLogin();
                }
        );

        queue.add(request);
    }

    private void clearSession() {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.remove(EMAIL_KEY);
        editor.remove(SESSION_KEY);
        editor.apply();
    }

    private void redirectToLogin() {
        startActivity(new Intent(this, LoginActivity.class));
        finish();
    }








}
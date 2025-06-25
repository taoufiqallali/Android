package com.taskshabitstracker;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;
import androidx.navigation.ui.NavigationUI;
import com.taskshabitstracker.databinding.ActivityMainBinding;
import com.taskshabitstracker.viewmodel.MainViewModel;

import android.content.pm.PackageManager;

/**
 * MainActivity - Entry point of the application
 * Responsibilities:
 * - Handle authentication check
 * - Set up navigation between fragments (Dashboard, Tasks, Habits, Profile, Settings)
 * - Manage toolbar and bottom navigation
 * - Handle logout functionality
 * - Request notification permissions for task deadline reminders and other local notifications
 */
public class MainActivity extends AppCompatActivity {
    private static final String TAG = "MainActivity";
    private static final int NOTIFICATION_PERMISSION_REQUEST_CODE = 100;

    // ViewBinding eliminates findViewById calls and provides null safety
    private ActivityMainBinding binding;

    // ViewModel handles business logic and survives configuration changes
    private MainViewModel viewModel;

    // Navigation controller manages fragment transactions
    private NavController navController;

    // Session manager handles authentication state

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Initialize ViewBinding - safer than findViewById
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // Initialize dependencies
        initializeDependencies();




        // Request notification permissions for API 33+ (required for WorkManager notifications)
        requestNotificationPermission();

        // Set up UI components
        setupToolbar();
        setupNavigation();
        setupObservers();
    }

    @Override
    protected void onResume() {
        super.onResume();

    }

    /**
     * Initialize all dependencies using proper dependency injection pattern
     * In a real app, you'd use Dagger/Hilt for this
     */
    private void initializeDependencies() {


        // ViewModelProvider ensures ViewModel survives configuration changes
        viewModel = new ViewModelProvider(this).get(MainViewModel.class);
    }

    /**
     * Request POST_NOTIFICATIONS permission for Android 13+ (API 33+)
     */
    private void requestNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.POST_NOTIFICATIONS)
                    != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this,
                        new String[]{android.Manifest.permission.POST_NOTIFICATIONS},
                        NOTIFICATION_PERMISSION_REQUEST_CODE);
            }
        }
    }

    /**
     * Set up the toolbar with proper configuration
     */
    private void setupToolbar() {
        setSupportActionBar(binding.topAppBar);

        // Set initial title
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayShowTitleEnabled(true);
        }
    }

    /**
     * Set up navigation using Navigation Component
     * Connects bottom navigation and toolbar actions to fragments
     */
    private void setupNavigation() {
        // Get NavHostFragment - this hosts all our fragments
        NavHostFragment navHostFragment = (NavHostFragment) getSupportFragmentManager()
                .findFragmentById(R.id.nav_host_fragment);

        if (navHostFragment != null) {
            navController = navHostFragment.getNavController();

            // Connect bottom navigation with NavController
            NavigationUI.setupWithNavController(binding.bottomNav, navController);

            // Listen for destination changes to update toolbar title
            navController.addOnDestinationChangedListener((controller, destination, arguments) -> {
                updateToolbarTitle(destination.getId());

            });
        }
    }

    /**
     * Set up observers for ViewModel LiveData
     * Follows MVVM pattern for reactive UI updates
     */
    private void setupObservers() {
        // Observe logout state
        viewModel.getLogoutState().observe(this, isLoggingOut -> {
            if (isLoggingOut != null && isLoggingOut) {
                performLogout();
            }
        });

        // Observe error messages
        viewModel.getErrorMessage().observe(this, errorMessage -> {
            if (errorMessage != null && !errorMessage.isEmpty()) {
                // Check if error is session-related
                if (errorMessage.toLowerCase().contains("session") ||
                        errorMessage.toLowerCase().contains("unauthorized") ||
                        errorMessage.toLowerCase().contains("expired")) {
                    Log.w(TAG, "Session-related error detected: " + errorMessage);
                    performLogout();
                } else {
                    // Show error to user (consider using Snackbar for better UX)
                    android.widget.Toast.makeText(this, errorMessage, android.widget.Toast.LENGTH_LONG).show();
                }
            }
        });
    }

    /**
     * Update toolbar title based on current destination
     * @param destinationId The current navigation destination ID
     */
    private void updateToolbarTitle(int destinationId) {
        String title;
        if (destinationId == R.id.dashboardFragment) {
            title = "Dashboard";
        } else if (destinationId == R.id.tasksFragment) {
            title = "Tasks";
        } else if (destinationId == R.id.habitsFragment) {
            title = "Habits";
        } else if (destinationId == R.id.settingsFragment) {
            title = "Settings";
        } else {
            title = "Tasks & Habits Tracker";
        }

        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle(title);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.top_app_bar_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int itemId = item.getItemId();
        if (itemId == R.id.action_logout) {
            // Delegate logout to ViewModel
            viewModel.logout();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    /**
     * Perform logout operations
     */
    private void performLogout() {
        Log.d(TAG, "Performing logout");
        redirectToLogin();
    }

    /**
     * Redirect to login activity and clear current activity stack
     */
    private void redirectToLogin() {
        Log.d(TAG, "Redirecting to login");
        Intent intent = new Intent(this, LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // Clean up ViewBinding to prevent memory leaks
        binding = null;
    }
}
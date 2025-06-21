// MainActivity.java - Simplified main activity that only handles navigation
package com.taskshabitstracker;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;
import androidx.navigation.ui.NavigationUI;
import com.taskshabitstracker.databinding.ActivityMainBinding;
import com.taskshabitstracker.viewmodel.MainViewModel;
import com.taskshabitstracker.utils.SessionManager;

/**
 * MainActivity - Entry point of the application
 * Responsibilities:
 * - Handle authentication check
 * - Set up navigation between fragments
 * - Manage toolbar and bottom navigation
 * - Handle logout functionality
 */
public class MainActivity extends AppCompatActivity {
    private static final String TAG = "MainActivity";

    // ViewBinding eliminates findViewById calls and provides null safety
    private ActivityMainBinding binding;

    // ViewModel handles business logic and survives configuration changes
    private MainViewModel viewModel;

    // Navigation controller manages fragment transactions
    private NavController navController;

    // Session manager handles authentication state
    private SessionManager sessionManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Initialize ViewBinding - safer than findViewById
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // Initialize dependencies
        initializeDependencies();

        // Check authentication before proceeding
        if (!sessionManager.isUserLoggedIn()) {
            redirectToLogin();
            return;
        }

        // Set up UI components
        setupToolbar();
        setupNavigation();
        setupObservers();
    }

    /**
     * Initialize all dependencies using proper dependency injection pattern
     * In a real app, you'd use Dagger/Hilt for this
     */
    private void initializeDependencies() {
        sessionManager = new SessionManager(this);

        // ViewModelProvider ensures ViewModel survives configuration changes
        viewModel = new ViewModelProvider(this).get(MainViewModel.class);
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
     * This replaces the manual view management with proper fragment navigation
     */
    private void setupNavigation() {
        // Get NavHostFragment - this hosts all our fragments
        NavHostFragment navHostFragment = (NavHostFragment) getSupportFragmentManager()
                .findFragmentById(R.id.nav_host_fragment);

        if (navHostFragment != null) {
            navController = navHostFragment.getNavController();

            // Connect bottom navigation with NavController
            // This automatically handles fragment switching
            NavigationUI.setupWithNavController(binding.bottomNav, navController);

            // Listen for destination changes to update toolbar title
            navController.addOnDestinationChangedListener((controller, destination, arguments) -> {
                updateToolbarTitle(destination.getId());
            });
        }
    }

    /**
     * Set up observers for ViewModel LiveData
     * This follows MVVM pattern for reactive UI updates
     */
    private void setupObservers() {
        // Observe logout state
        viewModel.getLogoutState().observe(this, isLoggingOut -> {
            if (isLoggingOut != null && isLoggingOut) {
                redirectToLogin();
            }
        });

        // Observe error messages
        viewModel.getErrorMessage().observe(this, errorMessage -> {
            if (errorMessage != null && !errorMessage.isEmpty()) {
                // Show error to user (you can use Snackbar here)
                // Snackbar.make(binding.getRoot(), errorMessage, Snackbar.LENGTH_LONG).show();
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
        } else if (destinationId == R.id.profileFragment) {
            title = "Profile";
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
        if (item.getItemId() == R.id.action_logout) {
            // Delegate logout to ViewModel
            viewModel.logout();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    /**
     * Redirect to login activity and clear current activity stack
     */
    private void redirectToLogin() {
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

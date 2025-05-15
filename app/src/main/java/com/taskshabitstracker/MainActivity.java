package com.taskshabitstracker;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;

public class MainActivity extends AppCompatActivity {

    private FirebaseAuth mAuth;
    private TextView mainText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mAuth = FirebaseAuth.getInstance();
        mainText = findViewById(R.id.mainText); // Make sure this exists in your layout

        // Set up the top app bar
        Toolbar toolbar = findViewById(R.id.topAppBar);
        setSupportActionBar(toolbar);

        // Do NOT set the click listener here as it interferes with the default menu handling
        // toolbar.setOnMenuItemClickListener(this::onOptionsItemSelected);

        // Set up bottom navigation
        BottomNavigationView bottomNav = findViewById(R.id.bottomNav);
        bottomNav.setOnItemSelectedListener(this::onNavigationItemSelected);

        // Set default text
        mainText.setText("Welcome to your dashboard!");
    }

    // This method is needed to inflate the menu
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.top_app_bar_menu, menu);
        return true;
    }

    // Handle bottom navigation selection
    private boolean onNavigationItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.nav_dashboard) {
            mainText.setText("Welcome to your dashboard!");
            return true;
        } else if (id == R.id.nav_tasks) {
            mainText.setText("Tasks will be shown here.");
            return true;
        } else if (id == R.id.nav_habits) {
            mainText.setText("Habits will be tracked here.");
            return true;
        } else if (id == R.id.nav_profile) {
            mainText.setText("User profile page.");
            return true;
        }
        return false;
    }

    // Handle logout
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == R.id.action_logout) {
            mAuth.signOut();
            startActivity(new Intent(MainActivity.this, LoginActivity.class));
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
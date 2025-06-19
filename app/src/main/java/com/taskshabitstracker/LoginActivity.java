package com.taskshabitstracker;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.JsonObjectRequest;

import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class LoginActivity extends AppCompatActivity {
    private static final String TAG = "LoginActivity";
    private static final String PREFS_NAME = "TasksHabitsPrefs";
    private static final String EMAIL_KEY = "user_email";
    private static final String SESSION_KEY = "user_session";

    EditText emailEditText, passwordEditText;
    Button loginButton;
    TextView goToRegisterText;
    private SharedPreferences sharedPreferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        sharedPreferences = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);

        // Check if user is already logged in
        if (isUserLoggedIn()) {
            redirectToMain();
            return;
        }

        emailEditText = findViewById(R.id.emailEditText);
        passwordEditText = findViewById(R.id.passwordEditText);
        loginButton = findViewById(R.id.loginButton);
        goToRegisterText = findViewById(R.id.goToRegisterText);

        RequestQueue queue = VolleySingleton.getInstance(this).getRequestQueue();
        String url = "http://10.0.2.2:8080/api/auth/login";

        loginButton.setOnClickListener(v -> {
            String email = emailEditText.getText().toString().trim();
            String password = passwordEditText.getText().toString().trim();

            if (email.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "Please fill in all fields", Toast.LENGTH_SHORT).show();
                return;
            }

            // Disable button during login
            loginButton.setEnabled(false);
            loginButton.setText("Logging in...");

            try {
                JSONObject jsonBody = new JSONObject();
                jsonBody.put("email", email);
                jsonBody.put("password", password);

                JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(
                        Request.Method.POST,
                        url,
                        jsonBody,
                        response -> {
                            loginButton.setEnabled(true);
                            loginButton.setText("Login");

                            String message = response.optString("message", "Login successful");
                            String sessionId = response.optString("sessionId", null);
                            if (sessionId != null) {
                                SharedPreferences.Editor editor = sharedPreferences.edit();
                                editor.putString(SESSION_KEY, sessionId);
                                editor.apply();
                            }

                            Log.d(TAG, "Login success: " + message);
                            Toast.makeText(LoginActivity.this, message, Toast.LENGTH_SHORT).show();
                            saveUserSession(email);
                            redirectToMain();
                        },
                        error -> {
                            loginButton.setEnabled(true);
                            loginButton.setText("Login");

                            String errorMessage = "Login failed";
                            if (error.networkResponse != null && error.networkResponse.data != null) {
                                try {
                                    String responseBody = new String(error.networkResponse.data);
                                    JSONObject errorJson = new JSONObject(responseBody);
                                    errorMessage = errorJson.optString("message", errorMessage);
                                } catch (Exception e) {
                                    Log.e(TAG, "Error parsing error response: " + e.getMessage());
                                }
                            }
                            Log.e(TAG, "Login error: " + errorMessage + ", Status Code: " +
                                    (error.networkResponse != null ? error.networkResponse.statusCode : "N/A"));
                            Toast.makeText(LoginActivity.this, errorMessage, Toast.LENGTH_LONG).show();
                        }
                ) {
                    @Override
                    public Map<String, String> getHeaders() {
                        Map<String, String> headers = new HashMap<>();
                        headers.put("Content-Type", "application/json");
                        return headers;
                    }
                };

                queue.add(jsonObjectRequest);
            } catch (Exception e) {
                loginButton.setEnabled(true);
                loginButton.setText("Login");

                Log.e(TAG, "Exception: " + e.getMessage());
                Toast.makeText(LoginActivity.this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });

        goToRegisterText.setOnClickListener(v -> {
            startActivity(new Intent(LoginActivity.this, RegisterActivity.class));
        });
    }

    private boolean isUserLoggedIn() {
        String email = sharedPreferences.getString(EMAIL_KEY, null);
        String session = sharedPreferences.getString(SESSION_KEY, null);
        return email != null && !email.isEmpty() && session != null && !session.isEmpty();
    }

    private void saveUserSession(String email) {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(EMAIL_KEY, email);
        editor.apply();
    }

    private void extractSessionFromHeaders() {
        Log.d(TAG, "Session cookies handled by VolleySingleton's CookieManager");
    }

    private void redirectToMain() {
        startActivity(new Intent(LoginActivity.this, MainActivity.class));
        finish();
    }
}
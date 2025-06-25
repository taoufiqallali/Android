package com.taskshabitstracker;

import android.content.Intent;
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
import com.taskshabitstracker.network.VolleySingleton;
import com.taskshabitstracker.utils.SessionManager;

import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class LoginActivity extends AppCompatActivity {
    private static final String TAG = "LoginActivity";

    EditText emailEditText, passwordEditText;
    Button loginButton;
    TextView goToRegisterText;
    private SessionManager sessionManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        // Use SessionManager instead of direct SharedPreferences
        sessionManager = new SessionManager(this);

        // Check if user is already logged in
        if (sessionManager.isUserLoggedIn()) {
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

                            if (sessionId != null && !sessionId.isEmpty()) {
                                // Use SessionManager to save session consistently
                                sessionManager.saveSession(email, sessionId);

                                Log.d(TAG, "Login success: " + message);
                                Log.d(TAG, "Session saved for email: " + email);
                                Toast.makeText(LoginActivity.this, message, Toast.LENGTH_SHORT).show();
                                redirectToMain();
                            } else {
                                Log.e(TAG, "No session ID received from server");
                                Toast.makeText(LoginActivity.this, "Login failed: No session received", Toast.LENGTH_LONG).show();
                            }
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

    private void redirectToMain() {
        Intent intent = new Intent(LoginActivity.this, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }
}
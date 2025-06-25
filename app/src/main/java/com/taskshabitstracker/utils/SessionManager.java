// SessionManager.java - Enhanced session management with validation
package com.taskshabitstracker.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

/**
 * SessionManager - Centralized session management
 * Handles all SharedPreferences operations related to user authentication
 * Provides clean API for session operations with enhanced validation
 */
public class SessionManager {
    private static final String TAG = "SessionManager";
    private static final String PREFS_NAME = "TasksHabitsPrefs";
    private static final String EMAIL_KEY = "user_email";
    private static final String SESSION_KEY = "user_session";
    private static final String LOGIN_TIME_KEY = "login_time";

    private final SharedPreferences sharedPreferences;

    public SessionManager(Context context) {
        sharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
    }

    /**
     * Check if user is currently logged in with enhanced validation
     * @return true if user has valid session, false otherwise
     */
    public boolean isUserLoggedIn() {
        String email = sharedPreferences.getString(EMAIL_KEY, null);
        String session = sharedPreferences.getString(SESSION_KEY, null);

        boolean isLoggedIn = email != null && !email.isEmpty() &&
                session != null && !session.isEmpty();

        Log.d(TAG, "Session check - Email: " + (email != null ? "present" : "null") +
                ", Session: " + (session != null ? "present" : "null") +
                ", IsLoggedIn: " + isLoggedIn);

        return isLoggedIn;
    }

    /**
     * Save user session after successful login
     * @param email User's email
     * @param sessionToken Session token from server
     */
    public void saveSession(String email, String sessionToken) {
        if (email == null || email.isEmpty() || sessionToken == null || sessionToken.isEmpty()) {
            Log.e(TAG, "Cannot save session - email or token is null/empty");
            return;
        }

        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(EMAIL_KEY, email);
        editor.putString(SESSION_KEY, sessionToken);
        editor.putLong(LOGIN_TIME_KEY, System.currentTimeMillis());

        boolean success = editor.commit(); // Use commit() to ensure immediate write

        Log.d(TAG, "Session save " + (success ? "successful" : "failed") +
                " for email: " + email + ", token length: " + sessionToken.length());
    }

    /**
     * Get current user's email
     * @return User's email or null if not logged in
     */
    public String getUserEmail() {
        return sharedPreferences.getString(EMAIL_KEY, null);
    }

    /**
     * Get current session token
     * @return Session token or null if not logged in
     */
    public String getSessionToken() {
        return sharedPreferences.getString(SESSION_KEY, null);
    }

    /**
     * Get login timestamp
     * @return Login time in milliseconds or 0 if not available
     */
    public long getLoginTime() {
        return sharedPreferences.getLong(LOGIN_TIME_KEY, 0);
    }

    /**
     * Clear all session data (logout)
     */
    public void clearSession() {
        Log.d(TAG, "Clearing session for user: " + getUserEmail());

        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.remove(EMAIL_KEY);
        editor.remove(SESSION_KEY);
        editor.remove(LOGIN_TIME_KEY);

        boolean success = editor.commit(); // Use commit() to ensure immediate clear

        Log.d(TAG, "Session clear " + (success ? "successful" : "failed"));
    }

    /**
     * Validate session integrity - checks if all required session data is present
     * @return true if session data is complete and valid
     */
    public boolean validateSession() {
        String email = getUserEmail();
        String token = getSessionToken();
        long loginTime = getLoginTime();

        boolean isValid = email != null && !email.isEmpty() &&
                token != null && !token.isEmpty() &&
                loginTime > 0;

        Log.d(TAG, "Session validation: " + isValid);

        if (!isValid) {
            Log.w(TAG, "Invalid session detected - clearing session data");
            clearSession();
        }

        return isValid;
    }

    /**
     * Debug method to log current session state
     */
    public void logSessionState() {
        Log.d(TAG, "=== Session State ===");
        Log.d(TAG, "Email: " + (getUserEmail() != null ? "present" : "null"));
        Log.d(TAG, "Token: " + (getSessionToken() != null ? "present" : "null"));
        Log.d(TAG, "Login time: " + getLoginTime());
        Log.d(TAG, "Is logged in: " + isUserLoggedIn());
        Log.d(TAG, "===================");
    }
}
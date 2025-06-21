// SessionManager.java - Handles user session and authentication state
package com.taskshabitstracker.utils;

import android.content.Context;
import android.content.SharedPreferences;

/**
 * SessionManager - Centralized session management
 * Handles all SharedPreferences operations related to user authentication
 * Provides clean API for session operations
 */
public class SessionManager {
    private static final String PREFS_NAME = "TasksHabitsPrefs";
    private static final String EMAIL_KEY = "user_email";
    private static final String SESSION_KEY = "user_session";

    private final SharedPreferences sharedPreferences;

    public SessionManager(Context context) {
        sharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
    }

    /**
     * Check if user is currently logged in
     * @return true if user has valid session, false otherwise
     */
    public boolean isUserLoggedIn() {
        String email = sharedPreferences.getString(EMAIL_KEY, null);
        String session = sharedPreferences.getString(SESSION_KEY, null);

        return email != null && !email.isEmpty() &&
                session != null && !session.isEmpty();
    }

    /**
     * Save user session after successful login
     * @param email User's email
     * @param sessionToken Session token from server
     */
    public void saveSession(String email, String sessionToken) {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(EMAIL_KEY, email);
        editor.putString(SESSION_KEY, sessionToken);
        editor.apply(); // Use apply() instead of commit() for better performance
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
     * Clear all session data (logout)
     */
    public void clearSession() {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.remove(EMAIL_KEY);
        editor.remove(SESSION_KEY);
        editor.apply();
    }
}
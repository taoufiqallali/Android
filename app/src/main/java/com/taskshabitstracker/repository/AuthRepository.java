// AuthRepository.java - Handles authentication API calls
package com.taskshabitstracker.repository;

import android.content.Context;
import android.util.Log;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.JsonObjectRequest;
import com.taskshabitstracker.network.VolleySingleton;

/**
 * AuthRepository - Handles all authentication-related API calls
 * Separates network logic from UI components
 * Provides clean API for authentication operations
 */
public class AuthRepository {
    private static final String TAG = "AuthRepository";
    private static final String BASE_URL = "http://10.0.2.2:8080/api/auth";

    private final RequestQueue requestQueue;

    public AuthRepository(Context context) {
        requestQueue = VolleySingleton.getInstance(context).getRequestQueue();
    }

    /**
     * Perform logout operation
     * @param onSuccess Callback for successful logout
     * @param onError Callback for logout error
     */
    public void logout(Runnable onSuccess, OnErrorCallback onError) {
        String url = BASE_URL + "/logout";

        JsonObjectRequest request = new JsonObjectRequest(
                Request.Method.POST,
                url,
                null,
                response -> {
                    Log.d(TAG, "Logout successful: " + response.toString());
                    onSuccess.run();
                },
                error -> {
                    Log.e(TAG, "Logout error: " + error.toString());
                    onError.onError(error.getMessage());
                }
        );

        requestQueue.add(request);
    }

    /**
     * Interface for error callbacks
     */
    public interface OnErrorCallback {
        void onError(String errorMessage);
    }
}

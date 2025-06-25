package com.taskshabitstracker.repository;

import android.content.Context;
import android.util.Log;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.JsonObjectRequest;
import com.taskshabitstracker.network.VolleySingleton;
import org.json.JSONObject;

public class UserRepository {
    private static final String TAG = "UserRepository";
    private static final String BASE_URL = "http://10.0.2.2:8080/api/users";

    private final RequestQueue requestQueue;

    public UserRepository(Context context) {
        requestQueue = VolleySingleton.getInstance(context).getRequestQueue();
    }

    public void getUserName(String userId, OnSuccessCallback<String> onSuccess, OnErrorCallback onError) {
        String url = BASE_URL + "/" + userId;

        JsonObjectRequest request = new JsonObjectRequest(
                Request.Method.GET,
                url,
                null,
                response -> {
                    try {
                        String name = response.optString("name", "User");
                        onSuccess.onSuccess(name);
                    } catch (Exception e) {
                        Log.e(TAG, "Error parsing user data", e);
                        onError.onError("Error parsing user data: " + e.getMessage());
                    }
                },
                error -> {
                    String errorMessage = "Failed to load user data";
                    if (error.networkResponse != null) {
                        int statusCode = error.networkResponse.statusCode;
                        if (statusCode == 404) {
                            errorMessage = "User not found";
                        } else {
                            errorMessage += " (Status Code: " + statusCode + ")";
                        }
                    }
                    Log.e(TAG, "User data error: " + error.toString());
                    onError.onError(errorMessage);
                }
        );

        requestQueue.add(request);
    }

    public interface OnSuccessCallback<T> {
        void onSuccess(T result);
    }

    public interface OnErrorCallback {
        void onError(String errorMessage);
    }
}
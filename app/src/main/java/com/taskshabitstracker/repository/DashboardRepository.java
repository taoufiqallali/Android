package com.taskshabitstracker.repository;

import android.content.Context;
import android.util.Log;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.JsonObjectRequest;
import com.taskshabitstracker.model.DashboardStats;
import com.taskshabitstracker.network.VolleySingleton;
import org.json.JSONObject;

public class DashboardRepository {
    private static final String TAG = "DashboardRepository";
    private static final String BASE_URL = "http://10.0.2.2:8080/api/dashboard";

    private final RequestQueue requestQueue;

    public DashboardRepository(Context context) {
        requestQueue = VolleySingleton.getInstance(context).getRequestQueue();
    }

    public void getDashboardStats(String userId, OnSuccessCallback<DashboardStats> onSuccess, OnErrorCallback onError) {
        String url = BASE_URL + "/" + userId + "/stats";

        JsonObjectRequest request = new JsonObjectRequest(
                Request.Method.GET,
                url,
                null,
                response -> {
                    try {
                        DashboardStats stats = parseDashboardStats(response);
                        onSuccess.onSuccess(stats);
                    } catch (Exception e) {
                        Log.e(TAG, "Error parsing dashboard stats", e);
                        onError.onError("Error parsing dashboard data: " + e.getMessage());
                    }
                },
                error -> {
                    String errorMessage = "Failed to load dashboard data";
                    if (error.networkResponse != null) {
                        errorMessage += " (Status Code: " + error.networkResponse.statusCode + ")";
                    }
                    Log.e(TAG, "Dashboard stats error: " + error.toString());
                    onError.onError(errorMessage);
                }
        );

        requestQueue.add(request);
    }

    private DashboardStats parseDashboardStats(JSONObject response) throws Exception {
        return new DashboardStats(
                response.optInt("points", 0),
                response.optInt("streak", 0),
                response.optInt("completedTasks", 0),
                response.optInt("totalTasks", 0),
                response.optInt("completedHabits", 0),
                response.optInt("totalHabits", 0)
        );
    }

    public interface OnSuccessCallback<T> {
        void onSuccess(T result);
    }

    public interface OnErrorCallback {
        void onError(String errorMessage);
    }
}
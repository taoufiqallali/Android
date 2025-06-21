// ProfileRepository.java - For profile data
package com.taskshabitstracker.repository;

import android.content.Context;
import android.util.Log;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.JsonObjectRequest;
import com.taskshabitstracker.model.UserProfile;
import com.taskshabitstracker.network.VolleySingleton;
import com.taskshabitstracker.utils.SessionManager;
import org.json.JSONObject;

public class ProfileRepository {
    private static final String TAG = "ProfileRepository";
    private static final String BASE_URL = "http://10.0.2.2:8080/api/profile";

    private final RequestQueue requestQueue;
    private final SessionManager sessionManager;

    public ProfileRepository(Context context) {
        requestQueue = VolleySingleton.getInstance(context).getRequestQueue();
        sessionManager = new SessionManager(context);
    }

    public void getUserProfile(DashboardRepository.OnSuccessCallback<UserProfile> onSuccess,
                               DashboardRepository.OnErrorCallback onError) {
        JsonObjectRequest request = new JsonObjectRequest(
                Request.Method.GET,
                BASE_URL,
                null,
                response -> {
                    try {
                        UserProfile profile = parseUserProfile(response);
                        onSuccess.onSuccess(profile);
                    } catch (Exception e) {
                        Log.e(TAG, "Error parsing profile", e);
                        onError.onError("Error parsing profile data");
                    }
                },
                error -> {
                    Log.e(TAG, "Profile error: " + error.toString());
                    onError.onError("Failed to load profile");
                }
        );

        requestQueue.add(request);
    }

    private UserProfile parseUserProfile(JSONObject response) throws Exception {
        return new UserProfile(
                response.getString("email"),
                response.optString("name", ""),
                response.optString("profilePicture", "")
        );
    }
}

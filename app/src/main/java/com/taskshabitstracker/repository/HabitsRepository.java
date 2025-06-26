package com.taskshabitstracker.repository;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;
import com.android.volley.NetworkResponse;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.StringRequest;
import com.taskshabitstracker.model.Habit;
import com.taskshabitstracker.network.VolleySingleton;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class HabitsRepository {
    private static final String TAG = "HabitsRepository";
    private static final String BASE_URL = "http://10.0.2.2:8080/api/habits"; // Adjusted for habits endpoint
    private final RequestQueue requestQueue;
    private final SharedPreferences prefs;

    public HabitsRepository(Context context) {
        requestQueue = VolleySingleton.getInstance(context).getRequestQueue();
        prefs = context.getSharedPreferences("HabitsPrefs", Context.MODE_PRIVATE); // Changed to HabitsPrefs
    }

    private Map<String, String> getSessionHeaders() {
        Map<String, String> headers = new HashMap<>();
        String sessionId = prefs.getString("jsessionid", null);
        if (sessionId != null) {
            headers.put("Cookie", "JSESSIONID=" + sessionId);
            Log.d(TAG, "Using session cookie: JSESSIONID=" + sessionId);
        } else {
            Log.w(TAG, "No session ID found in SharedPreferences");
        }
        return headers;
    }

    // Method to save session ID after login
    public void saveSessionId(String sessionId) {
        prefs.edit().putString("jsessionid", sessionId).apply();
        Log.d(TAG, "Saved session ID: " + sessionId);
    }

    public void getHabits(String userId, OnHabitsFetched callback) throws JSONException {
        String url=BASE_URL+"?userId="+userId;
        JsonArrayRequest request = new JsonArrayRequest(
                Request.Method.GET,
                url,null
                ,
                response -> {
                    try {
                        Log.d(TAG, "GET habits response: " + response.toString());
                        List<Habit> habits = parseHabitsArray(response);
                        Log.d(TAG, "Habits fetched: " + habits.size());
                        callback.onSuccess(habits);
                    } catch (Exception e) {
                        Log.e(TAG, "Error parsing habits response: " + response.toString(), e);
                        callback.onError("Error parsing habits data: " + e.getMessage());
                    }
                },
                error -> {
                    String errorMsg = "Failed to load habits";
                    if (error.networkResponse != null) {
                        Log.e(TAG, "GET habits error - Status code: " + error.networkResponse.statusCode);
                        errorMsg += " (Status code: " + error.networkResponse.statusCode + ")";
                        if (error.networkResponse.data != null) {
                            String responseBody = new String(error.networkResponse.data);
                            Log.e(TAG, "Error response body: " + responseBody);
                            errorMsg += " - " + responseBody;
                            if (responseBody.contains("User not found") || error.networkResponse.statusCode == 401 || error.networkResponse.statusCode == 403) {
                                errorMsg = "Session expired. Please log in again.";
                                prefs.edit().remove("jsessionid").apply();
                            }
                        }
                    } else {
                        Log.e(TAG, "GET habits error: " + error.toString(), error);
                        errorMsg += ": " + error.getMessage();
                    }
                    callback.onError(errorMsg);
                }
        ) {
            @Override
            public Map<String, String> getHeaders() {
                return getSessionHeaders();
            }

            @Override
            protected Response<JSONArray> parseNetworkResponse(NetworkResponse response) {
                // Extract and save JSESSIONID from response headers
                String sessionId = response.headers.get("Set-Cookie");
                if (sessionId != null && sessionId.contains("JSESSIONID")) {
                    String jsessionId = sessionId.split("JSESSIONID=")[1].split(";")[0];
                    saveSessionId(jsessionId);
                }
                return super.parseNetworkResponse(response);
            }
        };
        requestQueue.add(request);
    }

    public void toggleHabitCompletion(Habit habit, Runnable onSuccess, OnErrorCallback onError) throws JSONException {
        String url = BASE_URL + "/" + habit.getId() + "/toggle";
        JSONObject jsonBody = new JSONObject();

        JsonObjectRequest request = new JsonObjectRequest(
                Request.Method.PUT,
                url,
                null,
                response -> {
                    Log.d(TAG, "Toggle response: " + response.toString());
                    onSuccess.run();
                },
                error -> {
                    String errorMsg = "Failed to update habit";
                    if (error.networkResponse != null) {
                        Log.e(TAG, "Toggle error - Status code: " + error.networkResponse.statusCode);
                        errorMsg += " (Status code: " + error.networkResponse.statusCode + ")";
                        if (error.networkResponse.data != null) {
                            String responseBody = new String(error.networkResponse.data);
                            Log.e(TAG, "Toggle error response body: " + responseBody);
                            errorMsg += " - " + responseBody;
                            if (responseBody.contains("User not found") || error.networkResponse.statusCode == 401 || error.networkResponse.statusCode == 403) {
                                errorMsg = "Session expired. Please log in again.";
                                prefs.edit().remove("jsessionid").apply();
                            }
                        }
                    } else {
                        Log.e(TAG, "Toggle error: " + error.toString(), error);
                        errorMsg += ": " + error.getMessage();
                    }
                    onError.onError(errorMsg);
                }
        ) {
            @Override
            public Map<String, String> getHeaders() {
                return getSessionHeaders();
            }

            @Override
            protected Response<JSONObject> parseNetworkResponse(NetworkResponse response) {
                // Extract and save JSESSIONID from response headers
                String sessionId = response.headers.get("Set-Cookie");
                if (sessionId != null && sessionId.contains("JSESSIONID")) {
                    String jsessionId = sessionId.split("JSESSIONID=")[1].split(";")[0];
                    saveSessionId(jsessionId);
                }
                return super.parseNetworkResponse(response);
            }
        };

        requestQueue.add(request);
    }

    public void deleteHabit(Habit habit, Runnable onSuccess, OnErrorCallback onError) {
        String url = BASE_URL + "/" + habit.getId();

        StringRequest request = new StringRequest(
                Request.Method.DELETE,
                url,
                response -> {
                    Log.d(TAG, "Delete response: " + (response != null ? response : "No content"));
                    onSuccess.run();
                },
                error -> {
                    String errorMsg = "Failed to delete habit";
                    if (error.networkResponse != null) {
                        Log.e(TAG, "Delete error - Status code: " + error.networkResponse.statusCode);
                        errorMsg += " (Status code: " + error.networkResponse.statusCode + ")";
                        if (error.networkResponse.data != null) {
                            String responseBody = new String(error.networkResponse.data);
                            Log.e(TAG, "Delete error response body: " + responseBody);
                            errorMsg += " - " + responseBody;
                            if (responseBody.contains("User not found") || error.networkResponse.statusCode == 401 || error.networkResponse.statusCode == 403) {
                                errorMsg = "Session expired. Please log in again.";
                                prefs.edit().remove("jsessionid").apply();
                            }
                        }
                    } else {
                        Log.e(TAG, "Delete error: " + error.toString(), error);
                        errorMsg += ": " + error.getMessage();
                    }
                    onError.onError(errorMsg);
                }
        ) {
            @Override
            public Map<String, String> getHeaders() {
                return getSessionHeaders();
            }

            @Override
            protected Response<String> parseNetworkResponse(NetworkResponse response) {
                // Extract and save JSESSIONID from response headers
                String sessionId = response.headers.get("Set-Cookie");
                if (sessionId != null && sessionId.contains("JSESSIONID")) {
                    String jsessionId = sessionId.split("JSESSIONID=")[1].split(";")[0];
                    saveSessionId(jsessionId);
                }
                return super.parseNetworkResponse(response);
            }
        };

        requestQueue.add(request);
    }

    public void addHabit(Habit habit, OnHabitAdded callback, String userId) {
        if (habit == null) {
            callback.onError("Habit cannot be null");
            return;
        }

        String url = BASE_URL;

        try {
            JSONObject jsonHabit = new JSONObject();
            jsonHabit.put("name", habit.getName() != null ? habit.getName() : "");
            jsonHabit.put("description", habit.getDescription() != null ? habit.getDescription() : "");
            jsonHabit.put("streak", habit.getStreak());
            jsonHabit.put("completedToday", habit.isCompletedToday());
            jsonHabit.put("userId", userId);

            Log.d(TAG, "POST habit request JSON: " + jsonHabit.toString());
            Log.d(TAG, "userId being sent: " + userId);
            JsonObjectRequest request = new JsonObjectRequest(
                    Request.Method.POST,
                    url,
                    jsonHabit,
                    response -> {
                        try {
                            Log.d(TAG, "POST habit response: " + response.toString());
                            Habit newHabit = new Habit(
                                    response.getString("id"),
                                    response.getString("name"),
                                    response.optString("description", ""),
                                    response.getInt("streak"),
                                    response.getBoolean("completedToday")
                            );
                            Log.d(TAG, "Habit added: " + newHabit.getId());
                            callback.onTaskAdded(newHabit);
                        } catch (Exception e) {
                            Log.e(TAG, "Error parsing new habit response: " + response.toString(), e);
                            callback.onError("Error parsing habit data: " + e.getMessage());
                        }
                    },
                    error -> {
                        String errorMsg = "Failed to add habit - please check your connection";
                        if (error.networkResponse != null) {
                            Log.e(TAG, "POST habit error - Status code: " + error.networkResponse.statusCode);
                            errorMsg += " (Status code: " + error.networkResponse.statusCode + ")";
                            if (error.networkResponse.data != null) {
                                String responseBody = new String(error.networkResponse.data);
                                Log.e(TAG, "POST error response body: " + responseBody);
                                errorMsg += " - " + responseBody;
                                if (responseBody.contains("User not found") || error.networkResponse.statusCode == 401 || error.networkResponse.statusCode == 403) {
                                    errorMsg = "Session expired. Please log in again.";
                                    prefs.edit().remove("jsessionid").apply();
                                }
                            }
                        } else {
                            Log.e(TAG, "POST habit error: " + error.toString(), error);
                            errorMsg += ": " + error.getMessage();
                        }
                        callback.onError(errorMsg);
                    }
            ) {
                @Override
                public Map<String, String> getHeaders() {
                    return getSessionHeaders();
                }

                @Override
                protected Response<JSONObject> parseNetworkResponse(NetworkResponse response) {
                    // Extract and save JSESSIONID from response headers
                    String sessionId = response.headers.get("Set-Cookie");
                    if (sessionId != null && sessionId.contains("JSESSIONID")) {
                        String jsessionId = sessionId.split("JSESSIONID=")[1].split(";")[0];
                        saveSessionId(jsessionId);
                    }
                    return super.parseNetworkResponse(response);
                }
            };

            requestQueue.add(request);
        } catch (Exception e) {
            Log.e(TAG, "Error creating JSON for habit", e);
            callback.onError("Error preparing habit data: " + e.getMessage());
        }
    }

    private List<Habit> parseHabitsArray(JSONArray response) throws Exception {
        List<Habit> habits = new ArrayList<>();
        for (int i = 0; i < response.length(); i++) {
            JSONObject habitJson = response.getJSONObject(i);
            Habit habit = new Habit(
                    habitJson.getString("id"),
                    habitJson.getString("name"),
                    habitJson.optString("description", ""),
                    habitJson.getInt("streak"),
                    habitJson.getBoolean("completedToday")
            );
            habits.add(habit);
        }
        Log.d(TAG, "Parsed " + habits.size() + " habits from response");
        return habits;
    }

    public interface OnHabitsFetched {
        void onSuccess(List<Habit> habits);
        void onError(String error);
    }

    public interface OnHabitAdded {
        void onTaskAdded(Habit habit); // Note: Renamed for consistency, but consider renaming to onHabitAdded
        void onError(String error);
    }

    public interface OnErrorCallback {
        void onError(String error);
    }
}
// HabitsRepository.java - For habits data
package com.taskshabitstracker.repository;

import android.content.Context;
import android.util.Log;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.JsonObjectRequest;
import com.taskshabitstracker.model.Habit;
import com.taskshabitstracker.network.VolleySingleton;
import org.json.JSONArray;
import org.json.JSONObject;
import java.util.ArrayList;
import java.util.List;

public class HabitsRepository {
    private static final String TAG = "HabitsRepository";
    private static final String BASE_URL = "http://10.0.2.2:8080/api/habits";

    private final RequestQueue requestQueue;

    public HabitsRepository(Context context) {
        requestQueue = VolleySingleton.getInstance(context).getRequestQueue();
    }

    public void getHabits(DashboardRepository.OnSuccessCallback<List<Habit>> onSuccess,
                          DashboardRepository.OnErrorCallback onError) {
        JsonArrayRequest request = new JsonArrayRequest(
                Request.Method.GET,
                BASE_URL,
                null,
                response -> {
                    try {
                        List<Habit> habits = parseHabitsArray(response);
                        onSuccess.onSuccess(habits);
                    } catch (Exception e) {
                        Log.e(TAG, "Error parsing habits", e);
                        onError.onError("Error parsing habits data");
                    }
                },
                error -> {
                    Log.e(TAG, "Habits error: " + error.toString());
                    onError.onError("Failed to load habits");
                }
        );

        requestQueue.add(request);
    }

    public void toggleHabitCompletion(Habit habit, Runnable onSuccess, DashboardRepository.OnErrorCallback onError) {
        String url = BASE_URL + "/" + habit.getId() + "/toggle";

        JsonObjectRequest request = new JsonObjectRequest(
                Request.Method.PUT,
                url,
                null,
                response -> onSuccess.run(),
                error -> onError.onError("Failed to update habit")
        );

        requestQueue.add(request);
    }

    public void deleteHabit(Habit habit, Runnable onSuccess, DashboardRepository.OnErrorCallback onError) {
        String url = BASE_URL + "/" + habit.getId();

        JsonObjectRequest request = new JsonObjectRequest(
                Request.Method.DELETE,
                url,
                null,
                response -> onSuccess.run(),
                error -> onError.onError("Failed to delete habit")
        );

        requestQueue.add(request);
    }

    private List<Habit> parseHabitsArray(JSONArray response) throws Exception {
        List<Habit> habits = new ArrayList<>();

        for (int i = 0; i < response.length(); i++) {
            JSONObject habitJson = response.getJSONObject(i);
            Habit habit = new Habit(
                    habitJson.getInt("id"),
                    habitJson.getString("name"),
                    habitJson.optString("description", ""),
                    habitJson.optInt("streak", 0),
                    habitJson.optBoolean("completedToday", false)
            );
            habits.add(habit);
        }

        return habits;
    }
}

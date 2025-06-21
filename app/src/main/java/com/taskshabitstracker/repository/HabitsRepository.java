package com.taskshabitstracker.repository;

import android.content.Context;
import android.util.Log;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.StringRequest;
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
                        Log.d(TAG, "Habits fetched: " + habits.size());
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
                response -> {
                    Log.d(TAG, "Toggle response: " + response.toString());
                    onSuccess.run();
                },
                error -> {
                    Log.e(TAG, "Toggle error: " + error.toString());
                    onError.onError("Failed to update habit");
                }
        );

        requestQueue.add(request);
    }

    public void deleteHabit(Habit habit, Runnable onSuccess, DashboardRepository.OnErrorCallback onError) {
        String url = BASE_URL + "/" + habit.getId();

        StringRequest request = new StringRequest(
                Request.Method.DELETE,
                url,
                response -> {
                    Log.d(TAG, "Delete response: " + (response != null ? response : "No content"));
                    onSuccess.run();
                },
                error -> {
                    Log.e(TAG, "Delete error: ", error);
                    if (error.networkResponse != null) {
                        Log.e(TAG, "Status code: " + error.networkResponse.statusCode);
                        Log.e(TAG, "Response data: " + new String(error.networkResponse.data));
                    }
                    onError.onError("Failed to delete habit");
                }
        );

        requestQueue.add(request);
    }

    public void addHabit(Habit habit, DashboardRepository.OnSuccessCallback<Habit> onSuccess,
                         DashboardRepository.OnErrorCallback onError) {
        String url = BASE_URL;

        try {
            JSONObject jsonHabit = new JSONObject();
            jsonHabit.put("name", habit.getName());
            jsonHabit.put("description", habit.getDescription());
            jsonHabit.put("streak", habit.getStreak());
            jsonHabit.put("completedToday", habit.isCompletedToday());

            JsonObjectRequest request = new JsonObjectRequest(
                    Request.Method.POST,
                    url,
                    jsonHabit,
                    response -> {
                        try {
                            Habit newHabit = new Habit(
                                    response.getString("id"),
                                    response.getString("name"),
                                    response.optString("description", ""),
                                    response.optInt("streak", 0),
                                    response.optBoolean("completedToday", false)
                            );
                            Log.d(TAG, "Habit added: " + newHabit.getId());
                            onSuccess.onSuccess(newHabit);
                        } catch (Exception e) {
                            Log.e(TAG, "Error parsing new habit", e);
                            onError.onError("Error parsing new habit");
                        }
                    },
                    error -> {
                        Log.e(TAG, "Add habit error: " + error.toString());
                        onError.onError("Failed to add habit");
                    }
            );

            requestQueue.add(request);
        } catch (Exception e) {
            Log.e(TAG, "Error creating JSON for habit", e);
            onError.onError("Error creating habit data");
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
                    habitJson.optInt("streak", 0),
                    habitJson.optBoolean("completedToday", false)
            );
            habits.add(habit);
        }
        return habits;
    }
}
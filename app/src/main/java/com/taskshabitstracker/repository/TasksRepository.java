package com.taskshabitstracker.repository;

import android.content.Context;
import android.util.Log;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.StringRequest;
import com.taskshabitstracker.model.Task;
import com.taskshabitstracker.network.VolleySingleton;
import org.json.JSONArray;
import org.json.JSONObject;
import java.util.ArrayList;
import java.util.List;

public class TasksRepository {
    private static final String TAG = "TasksRepository";
    private static final String BASE_URL = "http://10.0.2.2:8080/api/tasks";

    private final RequestQueue requestQueue;

    public TasksRepository(Context context) {
        requestQueue = VolleySingleton.getInstance(context).getRequestQueue();
    }

    public void getTasks(OnTasksFetched callback) {
        JsonArrayRequest request = new JsonArrayRequest(
                Request.Method.GET,
                BASE_URL,
                null,
                response -> {
                    try {
                        List<Task> tasks = parseTasksArray(response);
                        Log.d(TAG, "Tasks fetched: " + tasks.size());
                        callback.onSuccess(tasks);
                    } catch (Exception e) {
                        Log.e(TAG, "Error parsing tasks", e);
                        callback.onError("Error parsing tasks data");
                    }
                },
                error -> {
                    Log.e(TAG, "Tasks error: " + error.toString());
                    callback.onError("Failed to load tasks");
                }
        );
        requestQueue.add(request);
    }

    public void toggleTaskCompletion(Task task, Runnable onSuccess, OnErrorCallback onError) {
        String url = BASE_URL + "/" + task.getId() + "/toggle";

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
                    onError.onError("Failed to update task");
                }
        );

        requestQueue.add(request);
    }

    public void deleteTask(Task task, Runnable onSuccess, OnErrorCallback onError) {
        String url = BASE_URL + "/" + task.getId();

        StringRequest request = new StringRequest(
                Request.Method.DELETE,
                url,
                response -> {
                    Log.d(TAG, "Delete response: " + (response != null ? response : "No content"));
                    onSuccess.run();
                },
                error -> {
                    Log.e(TAG, "Delete error: " + error.toString());
                    if (error.networkResponse != null) {
                        Log.e(TAG, "Status code: " + error.networkResponse.statusCode);
                    }
                    onError.onError("Failed to delete task");
                }
        );

        requestQueue.add(request);
    }

    // FIXED: Improved error handling and null safety
    public void addTask(Task task, OnTaskAdded callback) {
        if (task == null) {
            callback.onError("Task cannot be null");
            return;
        }

        String url = BASE_URL;

        try {
            JSONObject jsonTask = new JSONObject();
            jsonTask.put("title", task.getTitle() != null ? task.getTitle() : "");
            jsonTask.put("description", task.getDescription() != null ? task.getDescription() : "");
            jsonTask.put("completed", task.isCompleted());
            if (task.getDueDate() != null && !task.getDueDate().trim().isEmpty()) {
                jsonTask.put("dueDate", task.getDueDate().trim());
            }

            JsonObjectRequest request = new JsonObjectRequest(
                    Request.Method.POST,
                    url,
                    jsonTask,
                    response -> {
                        try {
                            Task newTask = new Task(
                                    response.getString("id"),
                                    response.getString("title"),
                                    response.optString("description", ""),
                                    response.optBoolean("completed", false),
                                    response.optString("dueDate", "")
                            );
                            Log.d(TAG, "Task added: " + newTask.getId());
                            callback.onTaskAdded(newTask);
                        } catch (Exception e) {
                            Log.e(TAG, "Error parsing new task response", e);
                            callback.onError("Error parsing server response");
                        }
                    },
                    error -> {
                        Log.e(TAG, "Add task network error: " + error.toString());
                        if (error.networkResponse != null) {
                            Log.e(TAG, "Status code: " + error.networkResponse.statusCode);
                        }
                        callback.onError("Failed to add task - please check your connection");
                    }
            );

            requestQueue.add(request);
        } catch (Exception e) {
            Log.e(TAG, "Error creating JSON for task", e);
            callback.onError("Error preparing task data");
        }
    }

    private List<Task> parseTasksArray(JSONArray response) throws Exception {
        List<Task> tasks = new ArrayList<>();
        for (int i = 0; i < response.length(); i++) {
            JSONObject taskJson = response.getJSONObject(i);
            Task task = new Task(
                    taskJson.getString("id"),
                    taskJson.getString("title"),
                    taskJson.optString("description", ""),
                    taskJson.optBoolean("completed", false),
                    taskJson.optString("dueDate", "")
            );
            tasks.add(task);
        }
        return tasks;
    }

    public interface OnTasksFetched {
        void onSuccess(List<Task> tasks);
        void onError(String error);
    }

    public interface OnTaskAdded {
        void onTaskAdded(Task task);
        void onError(String error);
    }

    public interface OnErrorCallback {
        void onError(String error);
    }
}
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
import com.taskshabitstracker.model.Task;
import com.taskshabitstracker.network.VolleySingleton;
import org.json.JSONArray;
import org.json.JSONObject;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TasksRepository {
    private static final String TAG = "TasksRepository";
    private static final String BASE_URL = "http://10.0.2.2:8080/api/tasks";
    private final RequestQueue requestQueue;
    private final SharedPreferences prefs;

    public TasksRepository(Context context) {
        requestQueue = VolleySingleton.getInstance(context).getRequestQueue();
        prefs = context.getSharedPreferences("TasksPrefs", Context.MODE_PRIVATE);
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

    public void getTasks(OnTasksFetched callback) {
        JsonArrayRequest request = new JsonArrayRequest(
                Request.Method.GET,
                BASE_URL,
                null,
                response -> {
                    try {
                        Log.d(TAG, "GET tasks response: " + response.toString());
                        List<Task> tasks = parseTasksArray(response);
                        Log.d(TAG, "Tasks fetched: " + tasks.size());
                        callback.onSuccess(tasks);
                    } catch (Exception e) {
                        Log.e(TAG, "Error parsing tasks response: " + response.toString(), e);
                        callback.onError("Error parsing tasks data: " + e.getMessage());
                    }
                },
                error -> {
                    String errorMsg = "Failed to load tasks";
                    if (error.networkResponse != null) {
                        Log.e(TAG, "GET tasks error - Status code: " + error.networkResponse.statusCode);
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
                        Log.e(TAG, "GET tasks error: " + error.toString(), error);
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
                    String errorMsg = "Failed to update task";
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
                    String errorMsg = "Failed to delete task";
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
            if (task.getDueDate() != null && !task.getDueDate().isEmpty()) {
                jsonTask.put("dueDate", task.getDueDate());
            }
            jsonTask.put("enableDueDateNotifications", task.isEnableDueDateNotifications());
            jsonTask.put("enablePreDueNotifications", task.isEnablePreDueNotifications());

            Log.d(TAG, "POST task request JSON: " + jsonTask.toString());

            JsonObjectRequest request = new JsonObjectRequest(
                    Request.Method.POST,
                    url,
                    jsonTask,
                    response -> {
                        try {
                            Log.d(TAG, "POST task response: " + response.toString());
                            Task newTask = new Task(
                                    response.getString("id"),
                                    response.optString("userId", "default-user"),
                                    response.getString("title"),
                                    response.optString("description", ""),
                                    response.optBoolean("completed", false),
                                    response.has("dueDate") ? response.getString("dueDate") : null,
                                    response.optBoolean("enableDueDateNotifications", true),
                                    response.optBoolean("enablePreDueNotifications", true)
                            );
                            Log.d(TAG, "Task added: " + newTask.getId());
                            callback.onTaskAdded(newTask);
                        } catch (Exception e) {
                            Log.e(TAG, "Error parsing new task response: " + response.toString(), e);
                            callback.onError("Error parsing task data: " + e.getMessage());
                        }
                    },
                    error -> {
                        String errorMsg = "Failed to add task - please check your connection";
                        if (error.networkResponse != null) {
                            Log.e(TAG, "POST task error - Status code: " + error.networkResponse.statusCode);
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
                            Log.e(TAG, "POST task error: " + error.toString(), error);
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
            Log.e(TAG, "Error creating JSON for task", e);
            callback.onError("Error preparing task data: " + e.getMessage());
        }
    }

    private List<Task> parseTasksArray(JSONArray response) throws Exception {
        List<Task> tasks = new ArrayList<>();
        for (int i = 0; i < response.length(); i++) {
            JSONObject taskJson = response.getJSONObject(i);
            Task task = new Task(
                    taskJson.getString("id"),
                    taskJson.optString("userId", "default-user"),
                    taskJson.getString("title"),
                    taskJson.optString("description", ""),
                    taskJson.optBoolean("completed", false),
                    taskJson.has("dueDate") ? taskJson.getString("dueDate") : null,
                    taskJson.optBoolean("enableDueDateNotifications", true),
                    taskJson.optBoolean("enablePreDueNotifications", true)
            );
            tasks.add(task);
        }
        Log.d(TAG, "Parsed " + tasks.size() + " tasks from response");
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
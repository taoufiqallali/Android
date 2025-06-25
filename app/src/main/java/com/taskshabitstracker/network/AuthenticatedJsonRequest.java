
package com.taskshabitstracker.network;

import android.content.Context;
import com.android.volley.AuthFailureError;
import com.android.volley.Response;
import com.android.volley.toolbox.JsonObjectRequest;
import com.taskshabitstracker.utils.SessionManager;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

/**
 * Authenticated JSON Request that automatically includes session token
 */
public class AuthenticatedJsonRequest extends JsonObjectRequest {
    private final SessionManager sessionManager;

    public AuthenticatedJsonRequest(Context context, int method, String url, JSONObject jsonRequest,
                                    Response.Listener<JSONObject> listener, Response.ErrorListener errorListener) {
        super(method, url, jsonRequest, listener, errorListener);
        this.sessionManager = new SessionManager(context);
    }

    @Override
    public Map<String, String> getHeaders() throws AuthFailureError {
        Map<String, String> headers = new HashMap<>();
        headers.put("Content-Type", "application/json");

        // Add session token if available
        String sessionToken = sessionManager.getSessionToken();
        if (sessionToken != null && !sessionToken.isEmpty()) {
            headers.put("Authorization", "Bearer " + sessionToken);
            // or use session ID header if your backend expects it differently
            // headers.put("X-Session-ID", sessionToken);
        }

        return headers;
    }
}
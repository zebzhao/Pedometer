package com.pedometrak.network;

import android.content.Context;
import android.content.SharedPreferences;
import android.provider.Settings;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.JsonRequest;
import com.android.volley.toolbox.Volley;
import com.pedometrak.util.Logger;
import org.json.JSONException;
import org.json.JSONObject;

public class ServerConnector {
    private static ServerConnector mInstance;

    public static final String API_HOST = "http://pedometrak.adhockish.com/api";
    public static final String URL_REGISTER ="/register";
    public static final String URL_SESSION ="/session";
    public static final String URL_RANK ="/rank";

    private RequestQueue mRequestQueue;
    private static Context mCtx;


    public static synchronized ServerConnector getInstance(Context context) {
        if (mInstance == null) {
            mInstance = new ServerConnector(context);
        }
        return mInstance;
    }


    private ServerConnector(Context context) {
        mCtx = context;
        mRequestQueue = getRequestQueue();

    }

    public RequestQueue getRequestQueue() {
        if (mRequestQueue == null) {
            // getApplicationContext() is key, it keeps you from leaking the
            // Activity or BroadcastReceiver if someone passes one in.
            mRequestQueue = Volley.newRequestQueue(mCtx.getApplicationContext());
        }
        return mRequestQueue;
    }

    public void sendRequest(String url, int method, JSONObject payload, final JsonRequestCallback callback) {
        JsonObjectRequest jsonRequest = new JsonObjectRequest
                (method, url, payload, new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        // the response is already constructed as a JSONObject!
                        try {
                            response = response.getJSONObject("args");
                            callback.onSuccess(response);
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                }, new Response.ErrorListener() {

                    @Override
                    public void onErrorResponse(VolleyError error) {
                        callback.onError();
                        error.printStackTrace();
                    }
                });

        // Access the RequestQueue through your singleton class.
        mRequestQueue.add(jsonRequest);
    }

    private JSONObject sessionPayload(String uuid, long start, long end, int steps, float distance, float calories) {
        // Create payload for session
        JSONObject payload = new JSONObject();
        addKey(payload, "UUID", uuid);
        addKey(payload, "startTime", (start/1000));
        addKey(payload, "endTime", (end/1000));
        addKey(payload, "stepsTaken", steps);
        addKey(payload, "distanceTravelled", distance);
        addKey(payload, "caloriesBurned", calories);
        return payload;
    }

    public void sendSession(final long start, final long end, final int steps, final float distance, final float calories, final JsonRequestCallback callback) {
        final SharedPreferences prefs = mCtx.getSharedPreferences("network", Context.MODE_PRIVATE);
        String uuid = prefs.getString("uuid", "");

        if (uuid.length() == 0) {
            register(new JsonRequestCallback() {
                @Override
                public void onSuccess(JSONObject response) {
                    sendSession(start, end, steps, distance, calories, callback);
                }
                @Override
                public void onError() {
                }
            });
        }
        else {
            sendRequest(API_HOST + URL_SESSION, Request.Method.POST,
                    sessionPayload(uuid, start, end, steps, distance, calories), new JsonRequestCallback() {
                @Override
                public void onSuccess(JSONObject response) {
                    callback.onSuccess(response);
                }
                @Override
                public void onError() {
                    callback.onError();
                }
            });
        }
    }

    private JSONObject registerPayload() {
        // UUID doesn't exist, fetch it from the server
        String androidId = Settings.Secure.getString(mCtx.getContentResolver(), Settings.Secure.ANDROID_ID);
        JSONObject payload = new JSONObject();
        addKey(payload, "AndroidID", androidId);
        return payload;
    }

    private void register(final JsonRequestCallback callback) {
        final SharedPreferences prefs = mCtx.getSharedPreferences("network", Context.MODE_PRIVATE);
        String uuid = prefs.getString("uuid", "");

        Logger.log("Fetched UUID: " + uuid);

        if (uuid.length() == 0) {
            // Send POST request to server to register
            sendRequest(API_HOST + URL_REGISTER, Request.Method.POST, registerPayload(), new JsonRequestCallback() {
                @Override
                public void onSuccess(JSONObject response) {
                    prefs.edit().putString("uuid", response.optString("UUID", "")).commit();
                    callback.onSuccess(response);
                }

                @Override
                public void onError() {
                    callback.onError();
                    Logger.log("Failed to register device with server.");
                }
            });
        }
    }

    private void addKey(JSONObject payload, String key, Object value) {
        try {
            payload.put(key, value);
        }
        catch(JSONException error) {
            error.printStackTrace();
        }
    }
}

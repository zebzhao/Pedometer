package de.j4velin.pedometer;

import android.content.Context;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import org.json.JSONObject;

public class ServerConnector {
    private static ServerConnector mInstance;

    public static final String url ="http://www.google.com";

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

    public void makeSampleRequest(Response.Listener<JSONObject> onSuccess, Response.ErrorListener onError) {
        JsonObjectRequest jsObjRequest = new JsonObjectRequest
                (Request.Method.GET, url, null, onSuccess, onError);
        // Access the RequestQueue through your singleton class.
        mRequestQueue.add(jsObjRequest);
    }
}

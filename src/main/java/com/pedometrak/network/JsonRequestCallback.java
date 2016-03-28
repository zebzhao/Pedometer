package com.pedometrak.network;

import org.json.JSONObject;

public interface JsonRequestCallback {
    public void onSuccess(JSONObject response);
    public void onError();
}

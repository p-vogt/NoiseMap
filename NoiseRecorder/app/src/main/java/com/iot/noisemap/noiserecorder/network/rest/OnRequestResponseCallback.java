package com.iot.noisemap.noiserecorder.network.rest;

import org.json.JSONObject;

/**
 * Interface for HTTP response clients.
 */
public interface OnRequestResponseCallback {
    /**
     * Gets called when a response is received.
     * @param response The received JSON object.
     */
    void onRequestResponseCallback(JSONObject response);
}

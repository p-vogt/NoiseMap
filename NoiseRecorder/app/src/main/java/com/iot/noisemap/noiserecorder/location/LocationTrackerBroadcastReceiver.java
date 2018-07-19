package com.iot.noisemap.noiserecorder.location;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.os.Bundle;
import android.support.annotation.Nullable;

import com.iot.noisemap.noiserecorder.audioprocessing.AudioRecorder;
import com.iot.noisemap.noiserecorder.MainActivity;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Broadcast receiver for location requests. Sends location updates to the calling activity
 * whenever a new measurement is done.
 */
public class LocationTrackerBroadcastReceiver extends BroadcastReceiver {

    AudioRecorder recording;
    MainActivity callingActivity;

    /**
     * Creates a new LocationTrackerBroadcastReceiver.
     * @param callingActivity The calling activity.
     * @param recording Corresponding AudioRecorder.
     */
    public LocationTrackerBroadcastReceiver(MainActivity callingActivity, AudioRecorder recording) {
        this.recording = recording;
        this.callingActivity = callingActivity;
    }

    /**
     * Gets called whenever a new message is received. Forms the JSONBody and sends it to the
     * corresponding activity.
     * @param context Corresponding context.
     * @param intent Corresponding intent.
     */
    @Override
    public void onReceive(Context context, Intent intent) {

        Location curLocation = getLocation(intent);
        JSONObject jsonBody = getJsonBody(curLocation);
        if(jsonBody.length() > 0) {
            callingActivity.onNewMeasurementDone(jsonBody);
        }
    }

    /**
     * Builds the needed JsonBody consisting of the timestamp and position/speed data.
     * @param location received location.
     * @return The JSONBody message.
     */
    private JSONObject getJsonBody(Location location) {

        JSONObject jsonBody = new JSONObject();
        if(location != null) {
            String timestamp = this.recording.getTimestampOfLastAverageDbA();

            try {
                double lastAverageDb = this.recording.getLastAverageDbA();
                if(Double.isNaN(lastAverageDb)) {
                    lastAverageDb = -1.0d;
                }
                jsonBody.put("timestamp", timestamp);
                jsonBody.put("noiseValue", lastAverageDb);
                jsonBody.put("longitude", location.getLongitude());
                jsonBody.put("latitude",location.getLatitude());
                jsonBody.put("accuracy", location.getAccuracy());
                jsonBody.put("speed", location.getSpeed());
            } catch (JSONException e) {}
        }
        return jsonBody;
    }

    /**
     * Extracts the location out of the intent.
     * @param intent Corresponding intent.
     * @return The extracted location.
     */
    @Nullable
    private Location getLocation(Intent intent) {
        Location curLocation = null;
        Bundle extras = intent.getExtras();
        if (intent.hasExtra("location")) {

            Object locationObject = extras.get("location");
            if(locationObject instanceof Location) {
                curLocation = (Location) locationObject;
            }
        }
        return curLocation;
    }
}

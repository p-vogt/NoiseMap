package com.example.patrick.noiserecorder.location;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.os.Bundle;
import android.support.annotation.Nullable;

import com.example.patrick.noiserecorder.audioprocessing.AudioRecorder;
import com.example.patrick.noiserecorder.MainActivity;

import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.Calendar;

public class LocationTrackerBroadcastReceiver extends BroadcastReceiver {

    AudioRecorder recording;
    MainActivity callingActivity;
    public LocationTrackerBroadcastReceiver(MainActivity callingActivity, AudioRecorder recording) {
        this.recording = recording;
        this.callingActivity = callingActivity;
    }
    @Override
    public void onReceive(Context context, Intent intent) {

        Location curLocation = getLocation(intent);
        JSONObject jsonBody = getJsonBody(curLocation);
        if(jsonBody.length() > 0) {
            callingActivity.onNewMeasurementDone(jsonBody);
        }
    }

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

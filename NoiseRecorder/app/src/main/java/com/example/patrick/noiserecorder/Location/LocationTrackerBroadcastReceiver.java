package com.example.patrick.noiserecorder.location;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.os.Bundle;

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

        JSONObject jsonBody = getJSONLocationMessageFromIntent(intent);
        if(jsonBody.length() > 0) {
            callingActivity.postNewSample(jsonBody);
        }
    }

    private JSONObject getJSONLocationMessageFromIntent(Intent intent) {

        JSONObject jsonBody = new JSONObject();
        Location curLocation = null;
        Bundle extras = intent.getExtras();
        if (intent.hasExtra("location")) {

            Object locationObject = extras.get("location");
            if(locationObject instanceof Location) {
                curLocation = (Location) locationObject;
            }
        }

        if(curLocation != null) {
            Calendar calendar = Calendar.getInstance();
            SimpleDateFormat timestampFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
            String timestamp = timestampFormat.format(calendar.getTime());

            try {
                double lastAverageDb = this.recording.getLastAverageDb();
                if(Double.isNaN(lastAverageDb)) {
                    lastAverageDb = -1.0d;
                }
                jsonBody.put("timestamp", timestamp);
                jsonBody.put("noiseValue", lastAverageDb);
                jsonBody.put("longitude", curLocation.getLongitude());
                jsonBody.put("latitude",curLocation.getLatitude());
                jsonBody.put("accuracy", curLocation.getAccuracy());
                jsonBody.put("speed", curLocation.getSpeed());
                jsonBody.put("version", "AAAAAAAAB9g="); // TODO ??
                jsonBody.put("createdAt", timestamp);
                jsonBody.put("updatedAt", timestamp);
                jsonBody.put("deleted", false);

            } catch (JSONException e) {}
        }
        return jsonBody;
    }
}

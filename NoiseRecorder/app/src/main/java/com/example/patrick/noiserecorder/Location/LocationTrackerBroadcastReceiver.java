package com.example.patrick.noiserecorder.location;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

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
        callingActivity.postNewSample(jsonBody);
    }

    private JSONObject getJSONLocationMessageFromIntent(Intent intent) {
        JSONObject message;
        try {
            message = new JSONObject(intent.getStringExtra("message"));
        } catch (JSONException e) {
            e.printStackTrace(); // TODO
            return new JSONObject();
        }
        // TODO use time offset from location
        Calendar calendar = Calendar.getInstance();
        SimpleDateFormat timestampFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
        String timestamp = timestampFormat.format(calendar.getTime());
        double speed = -1.0d;
        double longitude = -1.0d;
        double latitude = -1.0d;
        double accuracy = -1.0d;
        try {
            longitude = message.getDouble("longitude");
        } catch (JSONException e) { }
        try {
            latitude = message.getDouble("latitude");
        } catch (JSONException e) { }
        try {
            accuracy = message.getDouble("accuracy");
        } catch (JSONException e) { }
        try {
            speed = message.getDouble("speed");
        } catch (JSONException e) { }

        if(speed == -1.0d ||longitude == -1.0d ||latitude == -1.0d ||accuracy == -1.0d ) {
            int i = 0;
        }
        JSONObject jsonBody = new JSONObject();
        try {
            double lastAverageDb = this.recording.getLastAverageDb();
            if(Double.isNaN(lastAverageDb)) {
                lastAverageDb = -1.0d;
            }
            jsonBody.put("timestamp", timestamp);
            jsonBody.put("noiseValue", lastAverageDb);
            jsonBody.put("longitude", longitude);
            jsonBody.put("latitude",latitude);
            jsonBody.put("accuracy", accuracy);
            jsonBody.put("speed", speed);
            jsonBody.put("version", "AAAAAAAAB9g="); // TODO ??
            jsonBody.put("createdAt", timestamp);
            jsonBody.put("updatedAt", timestamp);
            jsonBody.put("deleted", false);

        } catch (JSONException e) {

            return new JSONObject(); //TODO
        }
        return jsonBody;
    }
}

package com.example.patrick.noiserecorder.Location;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.example.patrick.noiserecorder.AudioProcessing.AudioRecorder;
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

        JSONObject jsonBody = new JSONObject();
        try {
            jsonBody.put("timestamp", timestamp);
            jsonBody.put("noiseValue", this.recording.getLastAverageDb());
            jsonBody.put("longitude", message.getDouble("longitude"));
            jsonBody.put("latitude", message.getDouble("latitude"));
            jsonBody.put("accuracy", message.getDouble("accuracy"));
            jsonBody.put("speed", message.getDouble("speed"));
            jsonBody.put("version", "AAAAAAAAB9g="); // TODO ??
            jsonBody.put("createdAt", timestamp);
            jsonBody.put("updatedAt", timestamp);
            jsonBody.put("deleted", false);

        } catch (JSONException e) {
            e.printStackTrace(); // TODO
            return new JSONObject();
        }
        return jsonBody;
    }
}

package com.example.patrick.noiserecorder;

import android.content.BroadcastReceiver;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import java.text.DecimalFormat;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.ListView;
import android.widget.Switch;
import android.widget.Toast;

import com.android.volley.RequestQueue;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.example.patrick.noiserecorder.audioprocessing.AudioRecorder;
import com.example.patrick.noiserecorder.location.LocationTrackerBroadcastReceiver;
import com.example.patrick.noiserecorder.network.mqtt.INoiseMapMqttConsumer;
import com.example.patrick.noiserecorder.network.mqtt.MqttNoiseMapClient;
import com.example.patrick.noiserecorder.network.rest.RestCallFactory;

import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

public class MainActivity extends AppCompatActivity implements INoiseMapMqttConsumer {
    private static final String TAG = "MainActivity";
    final ArrayList<String> listItems = new ArrayList<>();
    ArrayAdapter<String> adapter;
    private AudioRecorder audioRecorder;
    private String accessToken;
    private String username;
    private String password;
    private RequestQueue requestQueue;
    private BroadcastReceiver messageReceiver;
    private Switch switchOfflineMode;
    MqttNoiseMapClient mqttClient;

    private BottomNavigationView.OnNavigationItemSelectedListener onNavigationItemSelectedListener
            = new BottomNavigationView.OnNavigationItemSelectedListener() {

        /**
         * Gets called when an item of the navigation bar gets clicked.
         * Opens the corresponding "page".
         * @param item The clicked menu item.
         * @return whether the item was valid or not.
         */
        @Override
        public boolean onNavigationItemSelected(@NonNull MenuItem item) {
            Intent intent;
            switch (item.getItemId()) {
                case R.id.navigation_home:
                    return true;

                // open the navigation map
                case R.id.navigation_map:
                    if(audioRecorder != null && audioRecorder.isRecording()) {
                        audioRecorder.stopRecording();
                    }
                    Bundle b = new Bundle();
                    // pass the accessToken to the new MapActivity
                    intent = new Intent(MainActivity.this, MapsActivity.class);
                    b.putString("accessToken", accessToken);
                    b.putString("username", username);
                    b.putString("password", password);
                    intent.putExtras(b);

                    startActivity(intent);
                    return true;

                case R.id.navigation_notifications:
                    intent = new Intent(MainActivity.this, SettingsActivity.class);
                    startActivity(intent);
                    return true;
            }
            return false;
        }
    };

    /**
     * Gets called by the AudioRecorder when a whole measurement finished.
     * @param jsonSample JSONObject that contains the sample data.
     */
    public void onNewMeasurementDone(JSONObject jsonSample) {

        boolean offline_mode = switchOfflineMode.isChecked();
        boolean isRecording = audioRecorder.isRecording();
        try {
            if(!jsonSample.isNull("noiseValue")) {
                if(jsonSample.getDouble("noiseValue") > 0.0d) {
                    // TODO
                }
            }
            if(!offline_mode && isRecording) {
                postNewSample(jsonSample);
            }
        } catch(JSONException ex) {
            Log.d("onNewMeasurementDone","invalid noise value");
        }

        String output = "";
        try {
            String timestamp = jsonSample.getString("timestamp");
            DateFormat format = new SimpleDateFormat("yyyy-MM-dd'T'hh:mm:ss", Locale.ENGLISH);
            Date date = format.parse(timestamp);
            DecimalFormat df = new DecimalFormat("#.00");
            output +=   "Time:           " + timestamp.substring(11); // remove date
            output += "\nNoise:          " + df.format(jsonSample.getDouble("noiseValue"));
            output += "\nLongitude:  " + df.format(jsonSample.getDouble("longitude"));
            output += "\nLatitude:      " + df.format(jsonSample.getDouble("latitude"));
            output += "\n";
        } catch (ParseException | JSONException e) {
            Log.e("MainActivity", "invalid measurement json: " + jsonSample.toString());
            return;
        }
        adapter.insert(output, 0);
        adapter.notifyDataSetChanged();
    }

    /**
     * Sends a POST request to the rest api. Adds the sample to the database.
     * @param sampleBody JSONObject that contains the sample values.
     */
    private void postNewSample(JSONObject sampleBody) {
        String SERVER_API_URL = Config.API_BASE_URL;
        String POST_SAMPLE_URL = SERVER_API_URL + "Sample";

        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(this);
        boolean useMqtt = sharedPref.getBoolean("noisemap_general_useMqtt", true);

        if(useMqtt) {
            mqttClient.postSample(sampleBody.toString());
        } else {
            JsonObjectRequest postSample = RestCallFactory.createPostSampleRequest(sampleBody, POST_SAMPLE_URL, this.accessToken);
            requestQueue.add(postSample);
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Bundle b = getIntent().getExtras();
        if (b != null) {
            accessToken = b.getString("accessToken");
            username = b.getString("username");
            password = b.getString("password");
        } else {
            throw new IllegalArgumentException("MainActivity:onCreate");
        }
        String clientId = "AndroidNoiseMapClient" + System.currentTimeMillis();
        this.mqttClient = new MqttNoiseMapClient(clientId,username,password, this, getApplicationContext());

        initServices();
        requestQueue = Volley.newRequestQueue(this);
        switchOfflineMode = (Switch) findViewById(R.id.switchOfflineMode);

        SharedPreferences settings = getSharedPreferences("UserData", 0);
        boolean offlineMode = settings.getBoolean("OfflineMode", false);
        switchOfflineMode.setChecked(offlineMode);
        switchOfflineMode.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                // save settings
                SharedPreferences settings = MainActivity.this.getSharedPreferences("UserData", 0);
                SharedPreferences.Editor editor = settings.edit();
                editor.putBoolean("OfflineMode",isChecked);
                editor.commit();
            }
        });
        BottomNavigationView navigation = (BottomNavigationView) findViewById(R.id.navigation);
        navigation.setOnNavigationItemSelectedListener(onNavigationItemSelectedListener);

        final Button btnStartStop = findViewById(R.id.btnStartStop);
        btnStartStop.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(audioRecorder== null) {
                    return;
                }
                if(audioRecorder.isRecording()) {
                    audioRecorder.stopRecording();
                    btnStartStop.setText("Start");
                    btnStartStop.setBackgroundColor(Color.parseColor("#33cc33"));
                    mqttClient.disconnect();
                    getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
                    switchOfflineMode.setEnabled(true);
                } else {
                    audioRecorder.startRecording();
                    btnStartStop.setText("Stop");
                    btnStartStop.setBackgroundColor(Color.parseColor("#cc0000"));
                    getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
                    if(switchOfflineMode.isChecked()) {
                        Toast.makeText(MainActivity.this,"!!!OFFLINE MODE ACTIVATED!!!",Toast.LENGTH_LONG).show();
                        btnStartStop.setText("OFFLINE");
                        btnStartStop.setBackgroundColor(Color.parseColor("#ffff00"));
                        getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
                    } else {
                        mqttClient.connect();
                    }
                    switchOfflineMode.setEnabled(false);
                }
            }
        });

        // Connect the adapter with the ListView
        adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, listItems);
        final ListView lView = findViewById(R.id.lViewPositions);
        lView.setAdapter(adapter);
    }
    @Override
    public void onResume() {
        super.onResume();
        ((BottomNavigationView)findViewById(R.id.navigation)).setSelectedItemId(R.id.navigation_home);
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(this);
        boolean useMqtt = sharedPref.getBoolean("noisemap_general_useMqtt", true);
        String calibrationOffsetInDbStr = sharedPref.getString("noisemap_measurement_calibrationOffset", "-1.75f");
        double calibrationOffsetInDb = Double.parseDouble(calibrationOffsetInDbStr);
        String timeBetweenMeasurementsString= sharedPref.getString("noisemap_measurement_timeBetweenMeasurementsS", "5");
        int timeBetweenMeasurementsS = Integer.parseInt(timeBetweenMeasurementsString);
        this.audioRecorder.setCalibrationOffset(calibrationOffsetInDb);
        this.audioRecorder.setTimeBetweenMeasurements(timeBetweenMeasurementsS * 1000);
        if(audioRecorder.isRecording() && useMqtt && !switchOfflineMode.isChecked()) {
            mqttClient.connect();
        }
    }
    private void initServices() {
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(this);
        String calibrationOffsetInDbStr = sharedPref.getString("noisemap_measurement_calibrationOffset", "-1.75f");
        double calibrationOffsetInDb = Double.parseDouble(calibrationOffsetInDbStr);
        String timeBetweenMeasurementsString= sharedPref.getString("noisemap_measurement_timeBetweenMeasurementsS", "5");
        int timeBetweenMeasurementsS = Integer.parseInt(timeBetweenMeasurementsString);
        audioRecorder = new AudioRecorder(this, calibrationOffsetInDb, timeBetweenMeasurementsS * 1000);
        messageReceiver = new LocationTrackerBroadcastReceiver(this, audioRecorder);
        // Register to receive messages.
        LocalBroadcastManager.getInstance(this).registerReceiver(
                messageReceiver, new IntentFilter("new-location"));
    }

    @Override
    public void onMessageArrived(String topic, MqttMessage message) {

    }

    @Override
    public void onConnected() {
        Toast.makeText(this,
                "MQTT connected",
                Toast.LENGTH_LONG)
                .show();
    }

    @Override
    public void onConnectionFailed() {
        Toast.makeText(this,
                "MQTT connection failed",
                Toast.LENGTH_LONG)
                .show();
    }

    @Override
    public void onConnectionLost() {
        Toast.makeText(this,
                "MQTT disconnected",
                Toast.LENGTH_LONG)
                .show();
    }
}

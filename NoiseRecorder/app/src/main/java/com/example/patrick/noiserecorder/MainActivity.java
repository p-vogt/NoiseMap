package com.example.patrick.noiserecorder;

import android.content.BroadcastReceiver;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.ListView;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.RequestQueue;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.example.patrick.noiserecorder.audioprocessing.AudioRecorder;
import com.example.patrick.noiserecorder.location.LocationTrackerBroadcastReceiver;
import com.example.patrick.noiserecorder.network.RestCallFactory;

import org.json.JSONObject;

import java.util.ArrayList;
public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";
    final ArrayList<String> listItems = new ArrayList<>();
    ArrayAdapter<String> adapter;
    private AudioRecorder audioRecorder;
    private TextView textMessage;
    private String accessToken;
    private RequestQueue requestQueue;
    private BroadcastReceiver messageReceiver;
    private Switch switchOfflineMode;
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
            switch (item.getItemId()) {
                case R.id.navigation_home:
                    textMessage.setText(R.string.title_home);
                    return true;

                // open the navigation map
                case R.id.navigation_map:
                    Bundle b = new Bundle();
                    // pass the accessToken to the new MapActivity
                    Intent intent = new Intent(MainActivity.this, MapsActivity.class);
                    b.putString("accessToken", accessToken);
                    intent.putExtras(b);

                    startActivity(intent);
                    return true;

                case R.id.navigation_notifications:
                    textMessage.setText(R.string.title_notifications);
                    return true;
            }
            return false;
        }
    };

    /**
     * Gets called by the AudioRecorder when a whole measurement finished.
     * @param newMeanDBA Mean value of the noise in dBA.
     */
    public void onNewMeasurementDone(double newMeanDBA) {
        // plot output TODO move and change output
        String dbOutput = "" + newMeanDBA;
        adapter.insert(dbOutput,0);
        adapter.notifyDataSetChanged();
    }

    /**
     * Sends a POST request to the rest api. Adds the sample to the database.
     * @param sampleBody JSONObject that contains the sample values.
     */
    public void postNewSample(JSONObject sampleBody) {

        boolean offline_mode = switchOfflineMode.isChecked();
        if(!offline_mode) {
            //TODO move
            String SERVER_API_URL = "http://noisemaprestapi.azurewebsites.net/api/"; // TODO HTTPS
            String POST_SAMPLE_URL = SERVER_API_URL + "Sample";
            JsonObjectRequest postSample = RestCallFactory.createPostSampleRequest(sampleBody, POST_SAMPLE_URL, this.accessToken);
            requestQueue.add(postSample);
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON); //TODO

        Bundle b = getIntent().getExtras();
        if (b != null) {
            accessToken = b.getString("accessToken");
        } else {
            //TODO
            return;
        }

        initServices();
        requestQueue = Volley.newRequestQueue(this);
        textMessage = (TextView) findViewById(R.id.message);
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
                } else {
                    audioRecorder.startRecording();
                    btnStartStop.setText("Stop");
                    btnStartStop.setBackgroundColor(Color.parseColor("#cc0000"));
                    if(switchOfflineMode.isChecked()) {
                        Toast.makeText(MainActivity.this,"!!!OFFLINE MODE ACTIVATED!!!",Toast.LENGTH_LONG).show();
                        btnStartStop.setText("OFFLINE");
                        btnStartStop.setBackgroundColor(Color.parseColor("#ffff00"));
                    }
                }
            }
        });

        // Connect the adapter with the ListView
        adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, listItems);
        final ListView lView = findViewById(R.id.lViewPositions);
        lView.setAdapter(adapter);
    }

    private void initServices() {
        audioRecorder = new AudioRecorder(this);
        messageReceiver = new LocationTrackerBroadcastReceiver(this, audioRecorder);
        // Register to receive messages.
        LocalBroadcastManager.getInstance(this).registerReceiver(
                messageReceiver, new IntentFilter("new-location"));
    }

}

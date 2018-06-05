package com.example.patrick.noiserecorder;

import android.content.BroadcastReceiver;
import android.content.Intent;
import android.content.IntentFilter;
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
import android.widget.ListView;
import android.widget.TextView;

import com.android.volley.RequestQueue;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.example.patrick.noiserecorder.AudioProcessing.AudioRecorder;
import com.example.patrick.noiserecorder.Location.LocationTrackerBroadcastReceiver;
import com.example.patrick.noiserecorder.Network.RestCallFactory;

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

        //TODO move
        String SERVER_API_URL = "http://noisemaprestapi.azurewebsites.net/api/"; // TODO HTTPS
        String POST_SAMPLE_URL = SERVER_API_URL + "Sample";

        JsonObjectRequest postSample = RestCallFactory.createPostSampleRequest(sampleBody, POST_SAMPLE_URL, this.accessToken);
        requestQueue.add(postSample);
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

        BottomNavigationView navigation = (BottomNavigationView) findViewById(R.id.navigation);
        navigation.setOnNavigationItemSelectedListener(onNavigationItemSelectedListener);

        final Button btnStartStop = findViewById(R.id.btnStartStop);
        btnStartStop.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                //TODO delete
                adapter.clear();
                adapter.notifyDataSetChanged();

                btnStartStop.setText(btnStartStop.getText() == "Start" ? "Stop" : "Start");
                btnStartStop.setBackgroundColor(btnStartStop.getText() == "Start" ? Color.parseColor("#33cc33") : Color.parseColor("#cc0000"));
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

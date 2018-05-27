package com.example.patrick.noiserecorder;

import android.Manifest;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.LocationManager;
import android.media.AudioRecord;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;


import java.io.UnsupportedEncodingException;
import java.util.Calendar;
import java.util.Date;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
public class MainActivity extends AppCompatActivity {

    private FFTUtil fft = new FFTUtil();
    private boolean isBound = false;
    private Messenger service;
    private double lastAverageDb = -1.0d;
    boolean isRecording = false;
    private long timeStartedRecordingInMs;

    AudioRecord audioRecorder = new AudioRecord(
                                        RecordingConfig.AUDIO_SOURCE,
                                        RecordingConfig.SAMPLE_RATE_IN_HZ,
                                        RecordingConfig.CHANNEL_CONFIG,
                                        RecordingConfig.AUDIO_FORMAT,
                                        RecordingConfig.BUFFER_SIZE_IN_BYTES);


    private static final String TAG = "MainActivity";
    final ArrayList<String> listItems = new ArrayList<>();
    ArrayAdapter<String> adapter;

    private TextView textMessage;
    private String accessToken;
    private RequestQueue requestQueue;
    private BroadcastReceiver messageReceiver;

    private BottomNavigationView.OnNavigationItemSelectedListener onNavigationItemSelectedListener
            = new BottomNavigationView.OnNavigationItemSelectedListener() {

        @Override
        public boolean onNavigationItemSelected(@NonNull MenuItem item) {
            switch (item.getItemId()) {
                case R.id.navigation_home:
                    textMessage.setText(R.string.title_home);
                    return true;
                case R.id.navigation_map:
                    Bundle b = new Bundle();
                    textMessage.setText(R.string.title_dashboard);
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

    private void startRecording() {

        isRecording = true;
        Calendar calendar = Calendar.getInstance();
        timeStartedRecordingInMs = calendar.getTimeInMillis();
        audioRecorder.startRecording();

    }
    private int recordData() {
        short[] valueBuffer = new short[RecordingConfig.BLOCK_SIZE_FFT];
        int read;
        read = audioRecorder.read(valueBuffer, 0, RecordingConfig.BLOCK_SIZE_FFT);
        if(read < 0) {
            return 1; // TODO
        }

        fft.process(valueBuffer);
        Calendar calendar = Calendar.getInstance();
        Date curTime = calendar.getTime();
        long currentRecordingTimeInMs = curTime.getTime() - timeStartedRecordingInMs;
        if(currentRecordingTimeInMs >= RecordingConfig.RECORDING_DURATION_IN_MS) {
            stopRecording();
            lastAverageDb = fft.finishProcess();
            requestLocation();
            // plot output TODO move and change output
            String dbOutput = "" + MainActivity.this.lastAverageDb;
            adapter.insert(dbOutput,0);
            adapter.notifyDataSetChanged();
            return RecordingConfig.DELAY_BETWEEN_MEASUREMENTS_IN_MS;
        }
        // TODO
        return 1;
    }

    private void stopRecording() {
        audioRecorder.stop();
        isRecording = false;
    }
    public double getLastAverageDb() {
        return lastAverageDb;
    }
    public void postNewSample(JSONObject sampleBody) {

        //TODO move
        String SERVER_API_URL = "http://noisemaprestapi.azurewebsites.net/api/"; // TODO HTTPS
        String POST_SAMPLE_URL = SERVER_API_URL + "Sample";


        JsonObjectRequest postSample =  createPostSample(sampleBody, POST_SAMPLE_URL);
        // requestQueue.add(postSample);

        String msgStr = ""; // TODO
        Log.d("receiver", "Got message: " + msgStr);
    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON); //TODO
        // Bind to LocalService
        Intent locationIntent = new Intent(this, LocationTrackerService.class);
        bindService(locationIntent, connection, Context.BIND_AUTO_CREATE);
        messageReceiver = new LocationTrackerBroadcastReceiver(this);
        // Register to receive messages.
        LocalBroadcastManager.getInstance(this).registerReceiver(
                messageReceiver, new IntentFilter("new-location"));

        adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, listItems);
        setContentView(R.layout.activity_main);
        requestQueue = Volley.newRequestQueue(this);
        textMessage = (TextView) findViewById(R.id.message);
        BottomNavigationView navigation = (BottomNavigationView) findViewById(R.id.navigation);
        navigation.setOnNavigationItemSelectedListener(onNavigationItemSelectedListener);

        Bundle b = getIntent().getExtras();
        if (b != null) {
            accessToken = b.getString("accessToken");
        }
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
        final ListView lView = findViewById(R.id.lViewPositions);
        lView.setAdapter(adapter);

        //==============TEST==========================================================================================================
        if (audioRecorder.getState() == AudioRecord.STATE_UNINITIALIZED) {
            Toast.makeText(MainActivity.this, "rec not initialized", Toast.LENGTH_SHORT).show();
            return;
        }
        //========================================================================================================================

        final Handler handler = new Handler();
        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                if (ActivityCompat.checkSelfPermission(MainActivity.this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(MainActivity.this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                    // TODO: Consider calling
                    //    ActivityCompat#requestPermissions
                    // here to request the missing permissions, and then overriding
                    //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                    //                                          int[] grantResults)
                    // to handle the case where the user grants the permission. See the documentation
                    // for ActivityCompat#requestPermissions for more details.
                    return;
                }

                if(!isRecording) {
                    startRecording();
                }
                final int delayTimeToNextCall = recordData();
                handler.postDelayed(this,delayTimeToNextCall);
            }
        };

        //Start
        handler.post(runnable);
    }

    // ----------------------------------------------------------- TEST AREA --------------------------------------------------------
    /**
     * Class for interacting with the main interface of the service.
     */
    private ServiceConnection connection = new ServiceConnection() {
        public void onServiceConnected(ComponentName className, IBinder service) {
            // This is called when the connection with the service has been
            // established, giving us the object we can use to
            // interact with the service.  We are communicating with the
            // service using a Messenger, so here we get a client-side
            // representation of that from the raw IBinder object.
            MainActivity.this.service = new Messenger(service);
            isBound = true;
        }

        public void onServiceDisconnected(ComponentName className) {
            // This is called when the connection with the service has been
            // unexpectedly disconnected -- that is, its process crashed.
            service = null;
            isBound = false;
        }
    };

    public void requestLocation() {
        if (!isBound) return;
        // send request location message to the LocationTrackerService
        Message msg = Message.obtain(null, LocationTrackerService.MSG_REQUEST_LOCATION, 0, 0);
        try {
            service.send(msg);
        } catch (RemoteException e) {
            e.printStackTrace(); // TODO
        }
    }

    // TODO extra class ?

    private StringRequest createGetRequest(final String url, final String accessToken) {
        return new StringRequest(Request.Method.GET, url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        JSONArray resp;
                        try {
                            resp = new JSONArray(response);
                            int i = 0;
                        } catch (JSONException e) {
                            e.printStackTrace();
                            // TODO invalid response
                        }
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                // TODO this is a dummy from LoginActivity
                String msg = "";
                JSONObject obj;
                String errorMsg = "unbekannter Fehler";
                try {
                    if(error != null && error.networkResponse != null) {
                        msg = new String(error.networkResponse.data, "UTF-8");
                        obj = new JSONObject(msg);
                        errorMsg = obj.getString("error_description");
                    }
                    // TODO
                } catch (UnsupportedEncodingException | JSONException e) {
                    e.printStackTrace(); //TODO
                    return;

                }
                //mPasswordView.setError(errorMsg); //TODO

            }
        })
        {
            @Override
            public Map<String, String> getHeaders() {
                HashMap<String, String> headers = new HashMap<String, String>();
                headers.put("Authorization", "Bearer " + accessToken);
                return headers;
            }
        };
    }


    private JsonObjectRequest createPostSample(JSONObject jsonBody, String url) {
        return new JsonObjectRequest(url, jsonBody,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        // TODO
                        int i = 0;
                    }

                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                // TODO is a dummy, also gets here when no response is beeing sent
                String msg = "";
                try {
                    if(error != null && error.networkResponse != null) {
                        msg = new String(error.networkResponse.data, "UTF-8");
                    }
                } catch (UnsupportedEncodingException e) {
                    e.printStackTrace(); //TODO
                    return;
                }
            }
        }) {
            @Override
            public Map<String, String> getHeaders() {
                HashMap<String, String> headers = new HashMap<String, String>();
                headers.put("Authorization", "Bearer " + accessToken);
                return headers;
            }
            @Override
            public String getBodyContentType()
            {
                return "application/json; charset=utf-8";
            }
        };
    }
}

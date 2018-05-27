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
import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaRecorder;
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
import org.jtransforms.fft.DoubleFFT_1D;

import java.io.UnsupportedEncodingException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
public class MainActivity extends AppCompatActivity {

    private boolean isBound = false;
    private Messenger service;

    final int SAMPLE_RATE_IN_HZ = 44100;
    final int FFTS_PER_SECOND = 20;

    final int TIME_BETWEEN_MEASUREMENTS_IN_MS = 5000;
    final int RECORDING_DURATION_IN_MS = 1000;

    final int DELAY_BETWEEN_MEASUREMENTS_IN_MS = TIME_BETWEEN_MEASUREMENTS_IN_MS - RECORDING_DURATION_IN_MS;

    final int BLOCK_SIZE_FFT = SAMPLE_RATE_IN_HZ / FFTS_PER_SECOND;

    // VOICE_RECOGNITION avoids some audio preprocessing
    final int AUDIO_SOURCE = MediaRecorder.AudioSource.VOICE_RECOGNITION;
    final int CHANNEL_CONFIG = AudioFormat.CHANNEL_IN_MONO;
    final int AUDIO_FORMAT = AudioFormat.ENCODING_PCM_16BIT;
    final int BUFFER_SIZE_IN_BYTES = AudioRecord.getMinBufferSize(SAMPLE_RATE_IN_HZ, CHANNEL_CONFIG, AUDIO_FORMAT);
    final double FREQUENCY_RESOLUTION = SAMPLE_RATE_IN_HZ / (double) BLOCK_SIZE_FFT;
    final DoubleFFT_1D fft = new DoubleFFT_1D(BLOCK_SIZE_FFT);
    private double lastAverageDb = -1.0d;
    boolean isRecording = false;
    AudioRecord audioRecorder = new AudioRecord(
                                        AUDIO_SOURCE,
                                        SAMPLE_RATE_IN_HZ,
                                        CHANNEL_CONFIG,
                                        AUDIO_FORMAT,
                                        BUFFER_SIZE_IN_BYTES);


    private static final String TAG = "MainActivity";
    private Intent locationIntent = null;
    private int numberOfFFTs = 0;
    private double averageDB = 0;
    private double[] a_weighting = new double[BLOCK_SIZE_FFT];
    double window_function[] = new double[BLOCK_SIZE_FFT];
    final ArrayList<String> listItems = new ArrayList<>();
    ArrayAdapter<String> adapter;

    private TextView textMessage;
    private String accessToken;
    private RequestQueue requestQueue;
    private LocationManager locationManager;
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

    private void calculateHannWindow() {
        for(int i = 0; i < BLOCK_SIZE_FFT; i++) {
            double hanningTemp = (2*Math.PI * i) / (BLOCK_SIZE_FFT - 1 );
            window_function[i] = (1 - Math.cos(hanningTemp)) * 0.5;
        }
    }

    private void calculateAWeighting() {
        for (int i = 0; i< BLOCK_SIZE_FFT /2; i++) {
            double f = i * FREQUENCY_RESOLUTION;
            double f2 = f * f;
            double f4 = f2 * f2;
            a_weighting[i] = (12200 * 12200 * f4) / ((f2 + 20.6 * 20.6) * (f2 + 12200 * 12200) * Math.sqrt(f2 + 107.7 * 107.7) * Math.sqrt(f2 + 737.9 * 737.9));
        }
    }
    private long timeStartedRecordingInMs;
    private void startRecording() {
        isRecording = true;
        Calendar calendar = Calendar.getInstance();
        timeStartedRecordingInMs = calendar.getTimeInMillis();
        audioRecorder.startRecording();

    }
    private int recordData() {
        short[] valueBuffer = new short[BLOCK_SIZE_FFT];
        int read;
        read = audioRecorder.read(valueBuffer, 0, BLOCK_SIZE_FFT);
        if(read < 0) {
            return 1; // TODO
        }

        double vals[] = new double[BLOCK_SIZE_FFT];

        for (int i = 0; i < BLOCK_SIZE_FFT; i++) {
            double normalized =  valueBuffer[i] / (double) Short.MAX_VALUE;
            vals[i] = normalized * window_function[i];
        }
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON); //TODO

        //==================
        //calculate fft
        //==================
        fft.realForward(vals);

        double sumOfAmplitudes = 0;

        // only half of the fft block size, because we now have real + imag values
        for (int i = 0; i < BLOCK_SIZE_FFT/2; i++) {

            double real = vals[2*i];
            double imag = vals[2*i+1];
            // TODO setting + configurable
            double calibration_offset = -1.75; // in dBA
            // TODO Window_sample_count/2 correct?
            //763
            double magn = Math.sqrt(real*real+imag*imag);

            // threshold of the human hearing = reference for the db calculation
            final double amplitudeRef = 0.00002;
            if(magn > 0.0 ) {
                // 5
                double dbFreqA = (10.0 * Math.log10(magn*magn*a_weighting[i]/amplitudeRef) + calibration_offset);
                //6
                dbFreqA = (10.0 * Math.pow(10,dbFreqA/10));
                // 7a sum in log
                sumOfAmplitudes += dbFreqA;
            }
            else {
                continue; // invalid magnitude, f.e. when the audio recorder still starting the recording
            }
        }
        // 7b
        double curAverage_dB;
        if(sumOfAmplitudes > 0) {
            curAverage_dB = 10*Math.log10(sumOfAmplitudes);
            numberOfFFTs++;
        } else {
            return 1;
        }
        averageDB += curAverage_dB;
        // TODO außerhalb init

        //if(numberOfFFTs >= FFTS_PER_SECOND * RECORDING_DURATION_IN_MS/1000) {
        Calendar calendar = Calendar.getInstance();
        Date curTime = calendar.getTime();
        long currentRecordingTimeInMs = curTime.getTime() - timeStartedRecordingInMs;
        if(currentRecordingTimeInMs >= RECORDING_DURATION_IN_MS) {
            stopRecording();
            averageDB = averageDB / numberOfFFTs ;
            lastAverageDb = averageDB;
            requestLocation();
            numberOfFFTs = 0;
            averageDB = 0;
            return DELAY_BETWEEN_MEASUREMENTS_IN_MS;
        }
        return 1;
    }

    private void stopRecording() {
        audioRecorder.stop();
        isRecording = false;
    }
    private void postNewSample(JSONObject sampleBody) {

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

        // Bind to LocalService
        Intent locationIntent = new Intent(this, LocationTrackerService.class);
        bindService(locationIntent, connection, Context.BIND_AUTO_CREATE);
        messageReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {

                JSONObject jsonBody = getJSONLocationMessageFromIntent(intent);
                postNewSample(jsonBody);

                // plot output TODO
                String dbOutput = "" + MainActivity.this.lastAverageDb;
                adapter.insert(dbOutput,0);
                adapter.notifyDataSetChanged();
            }
        };
        // Register to receive messages.
        LocalBroadcastManager.getInstance(this).registerReceiver(
                messageReceiver, new IntentFilter("new-location"));

        initCalculations();
        adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, listItems);
        setContentView(R.layout.activity_main);
        requestQueue = Volley.newRequestQueue(this);
        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
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
        /*
        //TODO move
        String SERVER_API_URL = "http://noisemaprestapi.azurewebsites.net/api/";
        String GET_ALL_SAMPLES_URL = SERVER_API_URL + "Sample";
        StringRequest stringRequest = createGetRequest(GET_ALL_SAMPLES_URL,accessToken);
        requestQueue.add(stringRequest);*/
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
            jsonBody.put("noiseValue", MainActivity.this.lastAverageDb);
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

    private void initCalculations() {
        calculateHannWindow();
        calculateAWeighting();
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

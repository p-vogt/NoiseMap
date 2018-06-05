package com.example.patrick.noiserecorder.Location;

import android.Manifest;
import android.app.Service;
import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Binder;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.os.Messenger;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnSuccessListener;

import org.json.JSONException;
import org.json.JSONObject;

public class LocationTrackerService extends Service implements ServiceConnection {


    static final int MSG_REQUEST_LOCATION = 1;

    public LocationTrackerService() {
        createLocationRequest();
    }


    private LocationCallback locationCallback = new LocationCallback() {
        @Override
        public void onLocationResult(LocationResult locationResult) {
            //logPosition();
        }
    };


    /**
     * Handler of incoming messages from clients.
     */
    class IncomingHandler extends Handler {
        @Override
        public void handleMessage(Message msg) {

            switch (msg.what) {
                case MSG_REQUEST_LOCATION:
                    try {
                        fusedLocationProviderClient.getLastLocation().addOnSuccessListener(new OnSuccessListener<Location>() {
                            @Override
                            public void onSuccess(Location location) {
                                emitLocation(location);
                            }
                        });
                    } catch (SecurityException ex) {
                        // TODO
                        Log.e("IncomingHandler", ex.getMessage());
                    }

                    break;
                default:
                    super.handleMessage(msg);
            }
        }
    }
    private void emitLocation(Location location) {
        currentLocation = location;
        if (currentLocation != null) {

            JSONObject obj = new JSONObject();

            try {
                obj.put("accuracy", currentLocation.getAccuracy());
                obj.put("longitude", currentLocation.getLongitude());
                obj.put("latitude", currentLocation.getLatitude());
                obj.put("altitude", currentLocation.getAltitude());
                obj.put("speed", currentLocation.getSpeed());
                obj.put("provider", currentLocation.getProvider());
                obj.put("time", currentLocation.getTime());
            } catch (JSONException e) {
                e.printStackTrace(); // TODO
            }
            // TODO

            Log.d("sender", "emitting location");
            Intent intent = new Intent("new-location");
            intent.putExtra("message", obj.toString());
            LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
        }
    }


    /**
     * Target we publish for clients to send messages to IncomingHandler.
     */
    final Messenger messenger = new Messenger(new IncomingHandler());

    private final IBinder binder = new  LocationTrackerBinder();

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        return Service.START_NOT_STICKY;
    }
    @Override
    public IBinder onBind(Intent intent) {
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(getApplicationContext());
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return null;
        }
        fusedLocationProviderClient.requestLocationUpdates(locationRequest, locationCallback, Looper.myLooper());

        return messenger.getBinder();
    }

    @Override
    public void onServiceConnected(ComponentName className, IBinder service) {
        //TODO
    }

    @Override
    public void onServiceDisconnected(ComponentName className) {
        //TODO
    }

    @Override
    public void onBindingDied(ComponentName name) {
            //TODO
    }

    /**
     * Class used for the client Binder.  Because we know this service always
     * runs in the same process as its clients, we don't need to deal with IPC.
     */
    public class  LocationTrackerBinder extends Binder {
        LocationTrackerService getService() {
            // Return this instance of LocalService so clients can call public methods
            return LocationTrackerService.this;
        }
    }

    private Location currentLocation;
    private static final long INTERVAL = 1000 * 10;
    private static final long FASTEST_INTERVAL = 1000 * 5;
    private LocationRequest locationRequest;

    private FusedLocationProviderClient fusedLocationProviderClient;

    protected void createLocationRequest() {
        locationRequest = new LocationRequest();
        locationRequest.setInterval(INTERVAL);
        locationRequest.setFastestInterval(FASTEST_INTERVAL);
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
    }
}

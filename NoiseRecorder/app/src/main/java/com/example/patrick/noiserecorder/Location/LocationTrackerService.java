package com.example.patrick.noiserecorder.location;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Binder;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.os.Messenger;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.text.TextUtils;
import android.util.Log;

import com.example.patrick.noiserecorder.LoginActivity;
import com.google.android.gms.location.FusedLocationProviderApi;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class LocationTrackerService extends Service implements ServiceConnection {


    static final int MSG_REQUEST_LOCATION = 1;
    private BroadcastReceiver messageReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String LOCATION_EXTRA_NAME = "com.google.android.location.LOCATION";
            if(intent.hasExtra(LOCATION_EXTRA_NAME)) {
                Object locObject = intent.getExtras().get(LOCATION_EXTRA_NAME);

                if(locObject instanceof Location) {
                    currentLocation = (Location) locObject;
                    Log.d("LocationTrackerService", "New Location: " + currentLocation);
                }

            }

        }
    };
    public LocationTrackerService() {

        createLocationRequest();
    }

    private LocationCallback locationCallback = new LocationCallback() {
        @Override
        public void onLocationResult(LocationResult locationResult) {
            Location newLocation = null;
            List<Location> locations = locationResult.getLocations();
            if (locations != null && locations.size() > 0) {
                newLocation = locations.get(0);
                Log.d("locationCallback", "currentLocation: " + currentLocation);
            }
            if (newLocation != null) {
                currentLocation = newLocation;
            }
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
                    emitLocation(); // Send currentLocation to the caller
                    break;
                default:
                    super.handleMessage(msg);
            }
        }
    }

    private void emitLocation() {
        if (currentLocation != null) {

            Log.d("sender", "emitting location");
            Intent intent = new Intent("new-location");
            intent.putExtra("location", currentLocation);
            intent.setFlags(Intent.FLAG_RECEIVER_FOREGROUND);
            LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
        }
    }


    /**
     * Target we publish for clients to send messages to IncomingHandler.
     */
    final Messenger messenger = new Messenger(new IncomingHandler());

    private final IBinder binder = new LocationTrackerBinder();

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        return Service.START_REDELIVER_INTENT;
    }

    // Gets checked before
    @SuppressLint("MissingPermission")
    @Override
    public IBinder onBind(Intent intent) {
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(getApplicationContext());
        fusedLocationProviderClient.getLastLocation().addOnSuccessListener(new OnSuccessListener<Location>() {
            @Override
            public void onSuccess(Location location) {
                currentLocation = location;
            }
        });
        String actionName = "location-update";
        IntentFilter filter = new IntentFilter(actionName);
        registerReceiver(messageReceiver, filter);
        Intent intentLocation = new Intent(actionName);
        PendingIntent locationUpdateIntent = PendingIntent.getBroadcast(this, 0,
                intentLocation, PendingIntent.FLAG_CANCEL_CURRENT);
        fusedLocationProviderClient.requestLocationUpdates(locationRequest, locationUpdateIntent);
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
    private static final long INTERVAL = 1000 * 5;
    private static final long FASTEST_INTERVAL = 1000 * 3;
    private LocationRequest locationRequest;

    private FusedLocationProviderClient fusedLocationProviderClient;

    protected void createLocationRequest() {
        locationRequest = new LocationRequest();
        locationRequest.setInterval(INTERVAL);
        locationRequest.setSmallestDisplacement(0);
        locationRequest.setFastestInterval(FASTEST_INTERVAL);
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
    }
}

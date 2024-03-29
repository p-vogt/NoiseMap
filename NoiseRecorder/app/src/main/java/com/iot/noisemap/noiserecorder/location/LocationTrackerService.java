package com.iot.noisemap.noiserecorder.location;

import android.annotation.SuppressLint;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.location.Location;
import android.os.Binder;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnSuccessListener;

import java.util.List;

/**
 * Service that periodically updates the location and provides it for other objects.
 */
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

    /**
     * Creates a new LocationTrackerService.
     */
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

    /**
     * Emits the current location  (if not null) to the local broadcast manager.
     */
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

    /**
     * Returns Service.START_REDELIVER_INTENT.
     * @param intent Corresponding intent.
     * @param flags Service flags.
     * @param startId Init ID.
     * @return Service.START_REDELIVER_INTENT
     */
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        return Service.START_REDELIVER_INTENT;
    }

    /**
     * Gets called when the service binding is done.
     * @param intent Corresponding intent.
     * @return The binding.
     */
    // Gets checked before
    @SuppressLint("MissingPermission")
    @Override
    public IBinder onBind(Intent intent) {
        FusedLocationProviderClient fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(getApplicationContext());
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

    /**
     * Gets called when the service connects.
     * @param className Corresponding class name.
     * @param service Corresponding service.
     */
    @Override
    public void onServiceConnected(ComponentName className, IBinder service) { }

    /**
     * Gets called when the service disconnects.
     * @param className Corresponding class name.
     */
    @Override
    public void onServiceDisconnected(ComponentName className) { }

    /**
     * Gets called when the binding dies.
     * @param name Corresponding component name.
     */
    @Override
    public void onBindingDied(ComponentName name) { }

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

    /**
     * Creates the location request.
     */
    protected void createLocationRequest() {
        locationRequest = new LocationRequest();
        locationRequest.setInterval(INTERVAL);
        locationRequest.setSmallestDisplacement(0);
        locationRequest.setFastestInterval(FASTEST_INTERVAL);
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
    }
}

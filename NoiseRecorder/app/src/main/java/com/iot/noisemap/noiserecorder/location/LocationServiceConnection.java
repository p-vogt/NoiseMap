package com.iot.noisemap.noiserecorder.location;

import android.content.ComponentName;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.util.Log;

/**
 * Manages the service connection that's used to transmit the current location.
 */
public class LocationServiceConnection implements ServiceConnection {
    private boolean isBound = false;
    private Messenger service;

    /**
     * Gets called when the service connets.
     * @param className Component class name.
     * @param service corresponding service.
     */
    public void onServiceConnected(ComponentName className, IBinder service) {
        // This is called when the connection with the service has been
        // established, giving us the object we can use to
        // interact with the service.  We are communicating with the
        // service using a Messenger, so here we get a client-side
        // representation of that from the raw IBinder object.
        this.service = new Messenger(service);
        this.isBound = true;
    }
    /**
     * Gets called when the service disconnets.
     * @param className Component class name.
     */
    public void onServiceDisconnected(ComponentName className) {
        // This is called when the connection with the service has been
        // unexpectedly disconnected -- that is, its process crashed.
        this.service = null;
        this.isBound = false;
    }

    /**
     * Requests the current location.
     */
    public void requestLocation() {
        if (!isBound) return;
        // send request location message to the LocationTrackerService
        Message msg = Message.obtain(null, LocationTrackerService.MSG_REQUEST_LOCATION, 0, 0);
        try {
            service.send(msg);
        } catch (RemoteException e) {
            Log.e("LocationServiceConnection", "" + e.getStackTrace());
        }
    }
};

package com.example.patrick.noiserecorder;

import android.content.ComponentName;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.support.v7.app.AppCompatActivity;

class LocationServiceConnection implements ServiceConnection {
    private boolean isBound = false;
    private Messenger service;

    public void onServiceConnected(ComponentName className, IBinder service) {
        // This is called when the connection with the service has been
        // established, giving us the object we can use to
        // interact with the service.  We are communicating with the
        // service using a Messenger, so here we get a client-side
        // representation of that from the raw IBinder object.
        this.service = new Messenger(service);
        this.isBound = true;
    }

    public void onServiceDisconnected(ComponentName className) {
        // This is called when the connection with the service has been
        // unexpectedly disconnected -- that is, its process crashed.
        this.service = null;
        this.isBound = false;
    }

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
};

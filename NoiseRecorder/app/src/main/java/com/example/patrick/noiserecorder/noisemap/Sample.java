package com.example.patrick.noiserecorder.noisemap;

import com.google.android.gms.maps.model.LatLng;
import java.util.Date;

public class Sample {
    public LatLng position;
    public double noise;
    public Date timestamp;

    public Sample() { }
    public Sample(LatLng position, double noise, Date timestamp) {
        this.position = position;
        this.noise = noise;
        this.timestamp = timestamp;

    }

}

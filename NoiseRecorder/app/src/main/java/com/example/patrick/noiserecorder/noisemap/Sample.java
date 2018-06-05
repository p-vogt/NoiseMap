package com.example.patrick.noiserecorder.noisemap;

import com.google.android.gms.maps.model.LatLng;

//TODO
public class Sample {
    public LatLng position;
    public double noise;

    public Sample(LatLng position, double noise) {
        this.position = position;
        this.noise = noise;

    }
}

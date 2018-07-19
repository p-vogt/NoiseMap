package com.iot.noisemap.noiserecorder.noisemap;

import com.google.android.gms.maps.model.LatLng;
import java.util.Date;

/**
 * Represents a sample measurement.
 */
public class Sample {
    public LatLng position;
    public double noise;
    public Date timestamp;

    /**
     * Creates a new sample.
     */
    public Sample() { }

    /**
     * Creates a new sample.
     * @param position Position.
     * @param noise Noise value.
     * @param timestamp Timestamp.
     */
    public Sample(LatLng position, double noise, Date timestamp) {
        this.position = position;
        this.noise = noise;
        this.timestamp = timestamp;

    }

}

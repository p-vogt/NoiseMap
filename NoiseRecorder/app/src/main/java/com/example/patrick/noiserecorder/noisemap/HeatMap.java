package com.example.patrick.noiserecorder.noisemap;

import android.os.Handler;
import android.os.SystemClock;

import com.example.patrick.noiserecorder.MapsActivity;
import com.google.android.gms.maps.model.Polygon;

public class HeatMap {

    // used to stop the animation of a clicked polygon when another polygon has been clicked
    private Polygon lastClickedPolygon ;
    public void setLastClickedPolygon(Polygon poly) {
        lastClickedPolygon = poly;
    };
    public Polygon getLastClickedPolygon() {
        return lastClickedPolygon;
    }

    public static int applyAlphaToColor(double alpha, final int color) {
        int alphaColor = color & 0x00ffffff;
        alphaColor = (int)(alpha * 255) << 24 | alphaColor;
        return alphaColor;
    }
    public void addPolygonBorderAnimation(final Polygon polygon, final MapsActivity activity) {
        final long start = SystemClock.uptimeMillis();
        final long animationDurationInMs = 1000;
        final long animationIntervalInMs = 100;
        final Handler handler = new Handler();
        handler.post(new Runnable() {
            @Override
            public void run() {
                long elapsed = SystemClock.uptimeMillis() - start;
                boolean hasTimeElapsed = elapsed >= animationDurationInMs;

                //toggle visibility
                polygon.setVisible(!polygon.isVisible());

                boolean hasAnotherPolygonBeenClicked = getLastClickedPolygon() != polygon;

                if (hasTimeElapsed || hasAnotherPolygonBeenClicked) {
                    // animation stopped
                    polygon.setVisible(true);
                } else {
                    // call again (delayed)
                    handler.postDelayed(this, animationIntervalInMs);
                }
            }
        });
    }
}

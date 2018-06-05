package com.example.patrick.noiserecorder.noisemap;

import android.os.Handler;
import android.os.SystemClock;
import android.support.annotation.NonNull;

import com.example.patrick.noiserecorder.MainActivity;
import com.example.patrick.noiserecorder.MapsActivity;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Polygon;
import com.google.maps.android.SphericalUtil;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class HeatMap {

    // used to stop the animation of a clicked polygon when another polygon has been clicked
    private Polygon lastClickedPolygon ;
    public void setLastClickedPolygon(Polygon poly) {
        lastClickedPolygon = poly;
    };

    private List<List<List<Double>>> noiseMatrix = new ArrayList<>();
    private List<Sample> samples = new ArrayList<>();
    public static int applyAlphaToColor(double alpha, final int color) {
        int alphaColor = color & 0x00ffffff;
        alphaColor = (int)(alpha * 255) << 24 | alphaColor;
        return alphaColor;
    }

    public Polygon getLastClickedPolygon() {
        return lastClickedPolygon;
    }
    public double getMeanNoise(int heightCounter, int widthCounter) {
        double sum = 0.0d;
        List<Double> samplesInArea = noiseMatrix.get(heightCounter).get(widthCounter);
        for (double curValue : samplesInArea) {
            sum += curValue;
        }
        return sum/samplesInArea.size();
    }

    public void clusterSamples(LatLng northWestVisible, double offsetLong, double offsetLat, GoogleMap map) {
        for(Sample sample: samples) {
            //map.addMarker(new MarkerOptions().position(sample.position));

            // check if value is in visible area
            LatLng curPos = sample.position;
            if(map.getProjection().getVisibleRegion().latLngBounds.contains(curPos)) {
                // calculate matrix indices
                // Index = floor[(Value-FirstPositionValue)/offset]
                int i = (int) Math.floor((curPos.latitude-northWestVisible.latitude)/offsetLat);
                int j = (int) Math.floor((curPos.longitude-northWestVisible.longitude)/offsetLong);
                noiseMatrix.get(i).get(j).add(sample.noise);

            }
        }
    }

    public void parseSamples(String jsonString) {
        JSONArray responseArray;
        try {
            responseArray = new JSONArray(jsonString);
            for(int i = 0; i < responseArray.length(); i++) {
                if (responseArray.get(i) instanceof JSONObject) {

                    //TODO convert to Sample, extend sample class
                    double longitude = (double)((JSONObject)responseArray.get(i)).get("longitude");
                    double latitude = (double)((JSONObject)responseArray.get(i)).get("latitude");
                    double noise = (double)((JSONObject)responseArray.get(i)).get("noiseValue");
                    LatLng position = new LatLng(latitude, longitude);
                    samples.add(new Sample(position, noise));
                }
            }
        } catch (JSONException e) {
            e.printStackTrace();
            return; // TODO
        }
    }
    public void initMatrix(double NUM_OF_RECTS_WIDTH, double numOfRectsHeight) {
        noiseMatrix.clear();
        for(int i = 0; i < numOfRectsHeight; i++) {
            List<List<Double>> row = new ArrayList<>();
            for(int j = 0; j < NUM_OF_RECTS_WIDTH;j++) {
                List<Double> column = new ArrayList<>();
                row.add(column);
            }
            noiseMatrix.add(row);
        }
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

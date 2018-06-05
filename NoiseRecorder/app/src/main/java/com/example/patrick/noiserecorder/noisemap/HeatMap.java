package com.example.patrick.noiserecorder.noisemap;

import android.graphics.Color;
import android.os.Handler;
import android.os.SystemClock;
import android.support.annotation.NonNull;
import android.widget.Toast;

import com.example.patrick.noiserecorder.MapsActivity;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Polygon;
import com.google.android.gms.maps.model.PolygonOptions;
import com.google.android.gms.maps.model.TileOverlayOptions;
import com.google.maps.android.SphericalUtil;
import com.google.maps.android.heatmaps.HeatmapTileProvider;
import com.google.maps.android.heatmaps.WeightedLatLng;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;



public class HeatMap {
    public enum OverlayType {
        OVERLAY_TILES,
        OVERLAY_HEATMAP
    }

    //TODO
    final int DIRECTION_NORTHEAST = 45;
    final int DIRECTION_SOUTHEAST = 135;
    final int DIRECTION_SOUTHWEST = 225;
    final int DIRECTION_NORTHWEST = 315;
    private final GoogleMap map;
    private final MapsActivity activity;

    public HeatMap(GoogleMap map,MapsActivity activity) {
        this.map = map;
        this.activity = activity;
    }
    private List<Polygon> polygons = new ArrayList<Polygon>();
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

    public void clusterSamples(LatLng northWestVisible, double offsetLong, double offsetLat) {
        for(Sample sample: samples) {
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

    public void addPolygonBorderAnimation(final Polygon polygon) {
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

    public void refresh(OverlayType overlayType) {
        map.clear();
        polygons.clear();

        LatLng northEastVisible = map.getProjection().getVisibleRegion().latLngBounds.northeast;
        LatLng southWestVisible = map.getProjection().getVisibleRegion().latLngBounds.southwest;
        LatLng northWestVisible = new LatLng(northEastVisible.latitude,southWestVisible.longitude);
        double NUM_OF_RECTS_WIDTH = 20; // TODO configurable

        double radius = SphericalUtil.computeDistanceBetween(northWestVisible,northEastVisible) / NUM_OF_RECTS_WIDTH / 2;
        double numOfRectsHeight =SphericalUtil.computeDistanceBetween(northWestVisible, southWestVisible) / radius / 2;
        LatLng start = SphericalUtil.computeOffset(northWestVisible, radius * Math.sqrt(2), DIRECTION_SOUTHEAST);

        initMatrix(NUM_OF_RECTS_WIDTH, numOfRectsHeight);
        double offsetLong =  2 * (start.longitude - northWestVisible.longitude);
        double offsetLat =  2 * (start.latitude - northWestVisible.latitude);

        clusterSamples(northWestVisible, offsetLong, offsetLat);

        double lat1 = 52.0382444;
        double long1 = 8.5257916;
        LatLng bielefeld1 = new LatLng(lat1, long1);

        LatLng bielefeld2 = new LatLng(52.0392444, 8.5257916);

        Collection<WeightedLatLng> weightedSamples = new ArrayList<>();

        // run through the whole map grid (vertically)
        //  ^
        //  |
        //  v
        for(int heightCounter = 0; heightCounter < numOfRectsHeight; heightCounter++) {
            // run through the whole map grid (horizontally)
            //
            // <--->
            //
            for(int widthCounter = 0; widthCounter < NUM_OF_RECTS_WIDTH; widthCounter++) {

                double meanNoise = getMeanNoise(heightCounter, widthCounter);

                LatLng center = new LatLng(start.latitude + heightCounter*offsetLat,start.longitude+widthCounter*offsetLong);
                //TODO customizable range
                double normalizedNoise = (meanNoise - 30) * 75 / 25.0d / 100.0d;
                if(meanNoise > 0.0) {
                    weightedSamples.add(new WeightedLatLng(center, normalizedNoise));
                }
                // Draw tiles if the tiles overlay is selected
                if(overlayType == OverlayType.OVERLAY_TILES) {

                    if(normalizedNoise > 1) {
                        normalizedNoise = 1;
                    }
                    double alpha = activity.getAlpha();
                    Polygon poly = createPolygon(radius, center, meanNoise, normalizedNoise, alpha);
                    polygons.add(poly);

                    map.setOnPolygonClickListener(new GoogleMap.OnPolygonClickListener() {
                        public void onPolygonClick(final Polygon polygon) {
                            String displayText = "" + polygon.getTag();
                            Toast.makeText(activity,
                                    displayText,
                                    Toast.LENGTH_LONG)
                                    .show();

                            setLastClickedPolygon(polygon);
                            addPolygonBorderAnimation(polygon);
                        }
                    });
                }
            }
        }

        if(overlayType == OverlayType.OVERLAY_HEATMAP) {
            HeatmapTileProvider provider = new HeatmapTileProvider.Builder()
                    .weightedData(weightedSamples)
                    .radius(50)
                    .build();

            // TODO own gradient?
            // Create the gradient.
            int[] colors = {
                    Color.rgb(102, 225, 0), // green
                    Color.rgb(255, 0, 0)    // red
            };

            map.addTileOverlay(new TileOverlayOptions().tileProvider(provider));
        }
    }
    private static int getArgbColor(double normalizedValue, double alpha) {
        int fillColor;
        int red = (int)(510 * normalizedValue);
        if (red > 255) {
            red = 255;
        } else if (red < 0) {
            red = 0;
        }
        int green =(int)( -510 * normalizedValue + 510);
        if(green < 0) {
            green = 0;
        } else if (green > 255) {
            green = 255;
        }
        int blue = 0;
        int a = (int)(255*alpha);
        // convert rgb to argb integer
        fillColor = (a << 24) | (red << 16 ) | (green<<8) | blue;
        return fillColor;
    }
    @NonNull
    private Polygon createPolygon(double radius,LatLng center, double meanNoise, double normalizedNoise,double alpha) {
        Polygon poly;// calculate the center of the current grid tile

        // The diagonal length of the tile from the center to an edge (diag) is sqrt(2)*radius
        // since r^2 + r^2 = diag^2
        LatLng targetNorthWest = SphericalUtil.computeOffset(center, radius * Math.sqrt(2), DIRECTION_NORTHWEST);
        LatLng targetNorthEast = SphericalUtil.computeOffset(center, radius * Math.sqrt(2), DIRECTION_NORTHEAST);
        LatLng targetSouthWest = SphericalUtil.computeOffset(center, radius * Math.sqrt(2), DIRECTION_SOUTHWEST);
        LatLng targetSouthEast = SphericalUtil.computeOffset(center, radius * Math.sqrt(2), DIRECTION_SOUTHEAST);
        int fillColor = 0;
        if(meanNoise > 0.0) {
            fillColor = getArgbColor(normalizedNoise, alpha);
        }
        PolygonOptions rectOptions = new PolygonOptions()
                .add(targetSouthWest)
                .add(targetSouthEast)
                .add(targetNorthEast)
                .add(targetNorthWest)
                .fillColor(fillColor)
                .strokeWidth(0f); // the width gets set later, depending on whether the grid is on or off
        poly = map.addPolygon(rectOptions);
        if(meanNoise > 0d) {
            poly.setTag(String.format("%.2f",  meanNoise) + " db(A)");
            poly.setClickable(true);
        }
        return poly;
    }

    public void setGridVisible(boolean showGrid) {
        for(Polygon poly : polygons) {
            if(showGrid) {
                poly.setStrokeWidth(1.0f);
                poly.setVisible(true);
            } else {
                poly.setStrokeWidth(0f);
                if(poly.getFillColor() == 0) {
                    poly.setVisible(false);
                }
            }
        }
    }

    public void setPolygonAlpha(double alpha) {
        for(Polygon poly : polygons) {
            int curColor = poly.getFillColor();
            if(curColor != 0) {
                curColor = HeatMap.applyAlphaToColor(alpha, curColor);
                poly.setFillColor(curColor);
            }

        }
    }
}

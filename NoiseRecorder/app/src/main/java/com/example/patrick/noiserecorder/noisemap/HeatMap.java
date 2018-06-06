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
    private boolean isGridVisible = false;
    //TODO
    final int DIRECTION_NORTHEAST = 45;
    final int DIRECTION_SOUTHEAST = DIRECTION_NORTHEAST + 90;
    final int DIRECTION_SOUTHWEST = DIRECTION_SOUTHEAST + 90;
    final int DIRECTION_NORTHWEST = DIRECTION_SOUTHWEST + 90;
    private double alpha;
    private final GoogleMap map;

    public HeatMap(GoogleMap map, double initAlpha, final MapsActivity activity) {
        this.map = map;
        alpha = initAlpha;

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


    private List<Polygon> polygons = new ArrayList<>();
    private List<PolygonOptions> cachedPolygonOptions = new ArrayList<>();
    // used to stop the animation of a clicked polygon when another polygon has been clicked
    private Polygon lastClickedPolygon ;
    private List<List<List<Double>>> noiseMatrix = new ArrayList<>();
    private List<Sample> samples = new ArrayList<>();
    private List<WeightedLatLng> cachedWeightedSamples = new ArrayList<>();
    private List<Double> cachedMeanNoise = new ArrayList<>();
    private void setLastClickedPolygon(Polygon poly) {
        lastClickedPolygon = poly;
    };

    private static int applyAlphaToColor(double alpha, final int color) {
        int alphaColor = color & 0x00ffffff;
        alphaColor = (int)(alpha * 255) << 24 | alphaColor;
        return alphaColor;
    }

    private Polygon getLastClickedPolygon() {
        return lastClickedPolygon;
    }
    public double getMeanNoise(int heightCounter, int widthCounter) {
        double sum = 0.0d;
        List<Double> samplesInArea = noiseMatrix.get(heightCounter).get(widthCounter);
        for (double curValue : samplesInArea) {
            if(curValue != Double.NaN && curValue != Double.POSITIVE_INFINITY && curValue != Double.NEGATIVE_INFINITY) {
                sum += curValue;
            }
        }
        if(samplesInArea.size() == 0) {
            return -1.0;
        }
        return sum/samplesInArea.size();
    }

    private void clusterSamples(LatLng northWestVisible, double offsetLong, double offsetLat) {
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

    public boolean parseSamples(JSONObject json) {
        JSONArray sampleArray;
        try {
            sampleArray = (JSONArray) json.get("samples");
            int arrayLen = sampleArray.length();
            for(int i = 0; i < arrayLen; i++) {
                if (sampleArray.get(i) instanceof JSONObject) {

                    //TODO convert to Sample, extend sample class
                    JSONObject curObject = (JSONObject)sampleArray.get(i);
                    if(curObject != null) {
                        if(!curObject.isNull(("longitude"))) {
                            double longitude = curObject.getDouble("longitude");
                            if(!curObject.isNull(("latitude"))) {
                                double latitude = curObject.getDouble("latitude");
                                if(!curObject.isNull(("noiseValue"))) {
                                    double noise = curObject.getDouble("noiseValue");
                                    LatLng position = new LatLng(latitude, longitude);
                                    samples.add(new Sample(position, noise));
                                }
                            }
                        }
                    }
                }
            }
            boolean breakpoint = true;
        } catch (JSONException e) {

            return false; // TODO invalid json response
        }
        return true;
    }
    private void initMatrix(double NUM_OF_RECTS_WIDTH, double numOfRectsHeight) {
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

    private void addPolygonBorderAnimation(final Polygon polygon) {
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
    static int refreshCounter = 0;
    //TODO refactor
    public void refresh(OverlayType overlayType, boolean fullRefresh) {
        map.clear(); // TODO check: does this clear the onClickListener for the polygons?
        refreshCounter++;
        LatLng northEastVisible = map.getProjection().getVisibleRegion().latLngBounds.northeast;
        LatLng southWestVisible = map.getProjection().getVisibleRegion().latLngBounds.southwest;
        LatLng northWestVisible = new LatLng(northEastVisible.latitude,southWestVisible.longitude);
        int NUM_OF_TILES_WIDTH = 20; // TODO configurable

        double radius = SphericalUtil.computeDistanceBetween(northWestVisible,northEastVisible) / NUM_OF_TILES_WIDTH / 2;
        double numOfRectsHeight =SphericalUtil.computeDistanceBetween(northWestVisible, southWestVisible) / (2*radius);
        LatLng start = SphericalUtil.computeOffset(northWestVisible, radius * Math.sqrt(2), DIRECTION_SOUTHEAST);
        double offsetLong =  2 * (start.longitude - northWestVisible.longitude);
        double offsetLat =  2 * (start.latitude - northWestVisible.latitude);
        if(fullRefresh) {
            polygons.clear();
            cachedPolygonOptions.clear();
            cachedWeightedSamples.clear();
            cachedMeanNoise.clear();
            initMatrix(NUM_OF_TILES_WIDTH, numOfRectsHeight);
            clusterSamples(northWestVisible, offsetLong, offsetLat);


            double lat1 = 52.0382444;
            double long1 = 8.5257916;
            LatLng bielefeld1 = new LatLng(lat1, long1);

            LatLng bielefeld2 = new LatLng(52.0392444, 8.5257916);

            // run through the whole map grid (vertically)
            //  ^
            //  |
            //  v
            for (int heightCounter = 0; heightCounter < numOfRectsHeight; heightCounter++) {
                // run through the whole map grid (horizontally)
                //
                // <--->
                //
                for (int widthCounter = 0; widthCounter < NUM_OF_TILES_WIDTH; widthCounter++) {

                    LatLng center = new LatLng(start.latitude + heightCounter * offsetLat, start.longitude + widthCounter * offsetLong);

                    // TODO cache
                    double meanNoise = -1.0d;
                    double normalizedNoise = -1.0d;
                    if (fullRefresh) {
                        meanNoise = getMeanNoise(heightCounter, widthCounter);
                        double max = 80.0d;
                        double min = 45.0d;
                        normalizedNoise = getNormalizedNoise(meanNoise, max, min);
                        WeightedLatLng curWLatLng = new WeightedLatLng(center, normalizedNoise);
                        cachedWeightedSamples.add(curWLatLng);
                        cachedMeanNoise.add(meanNoise);
                    } else {
                        int index = heightCounter * NUM_OF_TILES_WIDTH + widthCounter;
                        if (index < cachedWeightedSamples.size()) {
                            normalizedNoise = cachedWeightedSamples.get(index).getIntensity();
                            meanNoise = cachedMeanNoise.get(index);
                        }

                    }

                    // Draw tiles if the tiles overlay is selected
                    if (overlayType == OverlayType.OVERLAY_TILES) {
                        PolygonOptions polyOptions = createPolygonOptions(radius, center, meanNoise, normalizedNoise, alpha);
                        cachedPolygonOptions.add(polyOptions);
                    }
                }
            }
        }
        if(overlayType == OverlayType.OVERLAY_HEATMAP) {
            if(cachedWeightedSamples != null && cachedWeightedSamples.size() > 0) {
                Collection weightedSamples = new ArrayList();
                for(WeightedLatLng sample : cachedWeightedSamples) {
                    Double curVal = sample.getIntensity();
                    if(!Double.isNaN(curVal) && curVal > 0) {
                        weightedSamples.add(sample);
                    }

                }
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
        } else if(overlayType == OverlayType.OVERLAY_TILES) {
            int counter = 0;
            polygons.clear();
            for(PolygonOptions option : cachedPolygonOptions) {
                double meanNoise = cachedMeanNoise.get(counter);
                if(this.isGridVisible || meanNoise > 0.0d) {
                    Polygon poly = addPolygonToMap(meanNoise, option);
                    polygons.add(poly);
                }
                counter++;
            }
        }

    }

    private double getNormalizedNoise(double meanNoise, double max, double min) {
        double normalizedNoise = (meanNoise - min) * 1/(max-min);
        if(normalizedNoise > 1) {
            normalizedNoise = 1;
        } else if(normalizedNoise < 0) {
            normalizedNoise = 0;
        }
        return normalizedNoise;
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
    private PolygonOptions createPolygonOptions(double radius, LatLng center, double meanNoise, double normalizedNoise, double alpha) {

        // The diagonal length of the tile from the center to an edge (diag) is sqrt(2)*radius
        // since r^2 + r^2 = diag^2
        LatLng targetNorthWest = SphericalUtil.computeOffset(center, radius * Math.sqrt(2), DIRECTION_NORTHWEST);
        LatLng targetNorthEast = SphericalUtil.computeOffset(center, radius * Math.sqrt(2), DIRECTION_NORTHEAST);
        LatLng targetSouthWest = SphericalUtil.computeOffset(center, radius * Math.sqrt(2), DIRECTION_SOUTHWEST);
        LatLng targetSouthEast = SphericalUtil.computeOffset(center, radius * Math.sqrt(2), DIRECTION_SOUTHEAST);
        int fillColor = 0;
        boolean clickable= false;
        if(meanNoise > 0.0) {
            fillColor = getArgbColor(normalizedNoise, alpha);
            clickable = true;
        }
        PolygonOptions polygonOptions = new PolygonOptions()
                .add(targetSouthWest)
                .add(targetSouthEast)
                .add(targetNorthEast)
                .add(targetNorthWest)
                .fillColor(fillColor)
                .clickable(clickable)
                .strokeWidth(1.0f);
        return polygonOptions;
    }

    @NonNull
    private Polygon addPolygonToMap(double meanNoise, PolygonOptions polygonOptions) {
        Polygon poly = map.addPolygon(polygonOptions);
        if(meanNoise > 0d) {
           poly.setTag(String.format("%.2f",  meanNoise) + " db(A)");
       }
        return poly;
    }

    public void setGridVisible(boolean showGrid) {
        isGridVisible = showGrid;
    }

    public void setAlpha(double alpha) {
        // update polygons
        for(Polygon poly : polygons) {
            int curColor = poly.getFillColor();
            if(curColor != 0) {
                curColor = HeatMap.applyAlphaToColor(alpha, curColor);
                poly.setFillColor(curColor);
            }
        }
        this.alpha = alpha;
    }
}

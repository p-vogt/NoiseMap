package com.example.patrick.noiserecorder.noisemap;

import android.content.SharedPreferences;
import android.os.Handler;
import android.os.SystemClock;
import android.preference.PreferenceManager;
import android.util.Log;
import android.widget.Toast;

import com.android.volley.RequestQueue;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.example.patrick.noiserecorder.Config;
import com.example.patrick.noiserecorder.MapsActivity;
import com.example.patrick.noiserecorder.network.mqtt.INoiseMapMqttConsumer;
import com.example.patrick.noiserecorder.network.mqtt.MqttNoiseMapClient;
import com.example.patrick.noiserecorder.network.rest.OnRequestResponseCallback;
import com.example.patrick.noiserecorder.network.rest.RequestSamplesOptions;
import com.example.patrick.noiserecorder.network.rest.RestCallFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Polygon;
import com.google.android.gms.maps.model.PolygonOptions;
import com.google.android.gms.maps.model.TileOverlay;
import com.google.android.gms.maps.model.TileOverlayOptions;
import com.google.maps.android.SphericalUtil;
import com.google.maps.android.heatmaps.HeatmapTileProvider;
import com.google.maps.android.heatmaps.WeightedLatLng;
import com.google.protobuf.InvalidProtocolBufferException;

import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Locale;

import noisemap.NoiseMap;


public class HeatMap implements OnRequestResponseCallback, INoiseMapMqttConsumer {

    public enum OverlayType {
        OVERLAY_TILES,
        OVERLAY_HEATMAP
    }
    public static class TimePoint {
        public int hour;
        public int minute;
        public TimePoint(int hour, int minute) {
            this.hour = hour;
            this.minute = minute;
        }

        public boolean isGreaterThan(TimePoint time) {
            return this.hour > time.hour
                    || this.hour == time.hour && this.minute > time.minute;
        }

        public boolean equals(TimePoint time) {
            return this.minute == time.minute && this.hour == time.hour;
        }

        @Override
        public String toString() {
            String text = this.hour >= 10 ? "" + this.hour : "0" + this.hour;
            text +=":";
            text += this.minute >= 10 ? "" + this.minute : "0" + this.minute;
            return text;
        }
    }

    private class Direction {
        public final static int NORTHEAST = 45;
        public final static int SOUTHEAST = NORTHEAST + 90;
        public final static int SOUTHWEST = SOUTHEAST + 90;
        public final static int NORTHWEST = SOUTHWEST + 90;
    }

    private TimePoint startTime = new TimePoint(0,0);
    private TimePoint endTime = new TimePoint(0, 0);

    private MapsActivity activity;
    private RequestQueue requestQueue;
    private String weekdayFilter = "No Filter";
    private TileOverlay heatmapOverlay;
    private HeatmapTileProvider provider;
    private MqttNoiseMapClient mqttClient;
    private OverlayType overlayType = OverlayType.OVERLAY_TILES;
    private String accessToken;
    private double alpha;
    private final GoogleMap map;
    private double prevZoom = 0;
    private boolean useMqtt;
    private List<Polygon> polygons = new ArrayList<>();
    private List<PolygonOptions> cachedPolygonOptions = new ArrayList<>();
    // used to stop the animation of a clicked polygon when another polygon has been clicked
    private Polygon lastClickedPolygon;

    private List<List<List<Double>>> noiseMatrix = new ArrayList<>();
    private List<Sample> samples = new ArrayList<>();

    private List<WeightedLatLng> cachedWeightedSamples = new ArrayList<>();
    private List<Double> cachedMeanNoise = new ArrayList<>();

    public HeatMap(final GoogleMap map, double initAlpha, String accessToken, String username, String password, final boolean useMqtt, final MapsActivity activity) {
        this.map = map;
        map.setOnCameraMoveListener(new GoogleMap.OnCameraMoveListener() {
            @Override
            public void onCameraMove() {
                double curZoom = map.getCameraPosition().zoom;
                if(curZoom != prevZoom) {
                    prevZoom = curZoom;
                    calculateBlur(map);
                }
            }
        });
        alpha = initAlpha;
        this.useMqtt = useMqtt;
        this.accessToken = accessToken;
        this.activity = activity;
        String clientId = "AndroidNoiseMapClient" + System.currentTimeMillis();

        this.mqttClient = new MqttNoiseMapClient(clientId,username,password, this, activity.getApplicationContext());
        if(useMqtt) {
            this.mqttClient.connect();
        }
        requestQueue = Volley.newRequestQueue(activity);

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

    public void setOverlayType(OverlayType overlayType) {
        this.overlayType = overlayType;
    }

    @Override
    public void onRequestResponseCallback(JSONObject response) {
       boolean success = parseSamples(response);
        if(success) {
            refresh(true);
        } else {
            Toast.makeText(activity,"Error while parsing the response",Toast.LENGTH_LONG).show();

        }
    }
    public void requestSamplesForVisibleArea() {
        LatLng northEastVisible = map.getProjection().getVisibleRegion().latLngBounds.northeast;
        LatLng southWestVisible = map.getProjection().getVisibleRegion().latLngBounds.southwest;

        double latitudeStart = southWestVisible.latitude;
        double latitudeEnd = northEastVisible.latitude;

        double longitudeStart = southWestVisible.longitude;
        double longitudeEnd = northEastVisible.longitude;
        if(useMqtt) {
            requestSamplesViaMqtt(latitudeStart, latitudeEnd, longitudeStart, longitudeEnd, this.getStartTime(), this.getEndTime());
        } else {
            requestSamplesViaHttp(latitudeStart, latitudeEnd, longitudeStart, longitudeEnd, this.getStartTime(), this.getEndTime());
        }

    }

    private void requestSamplesViaHttp(double latitudeStart, double latitudeEnd, double longitudeStart, double longitudeEnd, TimePoint start, TimePoint end) {
        String apiUrl = Config.API_BASE_URL + "Sample";
        apiUrl += "?latitudeStart=" + latitudeStart;
        apiUrl += "&latitudeEnd=" + latitudeEnd;
        apiUrl += "&longitudeStart=" + longitudeStart;
        apiUrl += "&longitudeEnd=" + longitudeEnd;
        StringRequest apiRequest = RestCallFactory.createGetRequest(apiUrl,accessToken, this);
        requestQueue.add(apiRequest);
    }

    private JSONObject protobufSamplesToJSONObject(NoiseMap.Samples samples) throws JSONException {

        JSONObject json = new JSONObject();
        json.put("samples",new JSONArray());
        for(NoiseMap.Samples.Sample sample : samples.getSamplesList()) {
            JSONObject jsonSample = new JSONObject();
            try {
                jsonSample.put("latitude", sample.getLatitude());
                jsonSample.put("longitude", sample.getLongitude());
                jsonSample.put("timestamp", sample.getTimestamp());
                jsonSample.put("noiseValue", sample.getNoiseValue());
            } catch(JSONException e) {
                continue;
            }
            json.getJSONArray("samples").put(jsonSample);
        }
        return json;
    }
    @Override
    public void onMessageArrived(String topic, MqttMessage message) {
        byte[] payload = message.getPayload();
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(activity);
        boolean useProto = sharedPref.getBoolean("noisemap_general_useProtocolBuffers", true);
        Log.i("onMessageArrived", "msg size: " + payload.length);
        JSONObject json = null;
        try {
            if(useProto) {
                    NoiseMap.Samples samples = NoiseMap.Samples.parseFrom(payload);
                    json = protobufSamplesToJSONObject(samples);
            } else {
                    String samples = new String(payload, "UTF-8");
                    json = new JSONObject(samples);
            }
        } catch (JSONException | InvalidProtocolBufferException | UnsupportedEncodingException  e ) {
            Log.e("HeatMap", e.getMessage());
            Toast.makeText(activity,"Invalid response!",Toast.LENGTH_LONG).show();
        }
        parseSamples(json);
        refresh(true);
        Log.d("HeatMap", "messageArrived");
    }
    public void setStartTime(TimePoint startTime) {
        this.startTime = startTime;
    }
    public void setEndTime(TimePoint endTime) {
        this.endTime = endTime;
    }
    public TimePoint getStartTime() {
        return startTime;
    }
    public TimePoint getEndTime() {
        return endTime;
    }
    @Override
    public void onConnected() {
        Toast.makeText(activity,
                "MQTT connected",
                Toast.LENGTH_LONG)
                .show();
    }

    @Override
    public void onConnectionFailed() {
        Toast.makeText(activity,
                "MQTT connection failed",
                Toast.LENGTH_LONG)
                .show();
    }

    @Override
    public void onConnectionLost() {
        Toast.makeText(activity,
                "MQTT disconnected",
                Toast.LENGTH_LONG)
                .show();
    }


    public void setUseMqtt(boolean val) {
        this.useMqtt = val;
        if(useMqtt) {
            this.mqttClient.connect();
        } else {
            this.mqttClient.disconnect();
        }
    }
    private void calculateBlur(GoogleMap map) {
        if(provider != null) {
            SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(activity);
            String blurFactorStr = sharedPref.getString("noisemap_heatmap_blur", "3");
            int blurFactor = Integer.parseInt(blurFactorStr);
            double curZoom = map.getCameraPosition().zoom;
            int blurRadius = (int) (curZoom*blurFactor);
            if(blurRadius > 50) blurRadius = 50;
            else if(blurRadius < 10) blurRadius = 10;
            provider.setRadius(blurRadius);
            if(heatmapOverlay != null) {
                heatmapOverlay.clearTileCache();
            }
        }
    }


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

                // check week day filter
                String weekDayOfSample = new SimpleDateFormat("EE", Locale.ENGLISH).format(sample.timestamp.getTime());
                if(weekdayFilter.equals("No Filter") || weekDayOfSample.equals(weekdayFilter)) {

                    // calculate matrix indices
                    // Index = floor[(Value-FirstPositionValue)/
                    int i = (int) Math.floor((curPos.latitude-northWestVisible.latitude)/offsetLat);
                    int j = (int) Math.floor((curPos.longitude-northWestVisible.longitude)/offsetLong);
                    noiseMatrix.get(i).get(j).add(sample.noise);
                }

            }
        }
    }

    private void requestSamplesViaMqtt(final double latitudeStart, final double latitudeEnd, final double longitudeStart, final double longitudeEnd, TimePoint start, final TimePoint end) {
        MqttMessage msg = new MqttMessage();
        RequestSamplesOptions options = new RequestSamplesOptions(longitudeStart,longitudeEnd,latitudeStart,latitudeEnd, start, end);
        msg.setRetained(true);
        msg.setQos(1);
        msg.setPayload(options.toJSONString().getBytes());
        try {
             mqttClient.request(msg);
        } catch (MqttException e) {
            Log.d("MQTT: could not publish:", e.getMessage());
        }

    }
    public boolean parseSamples(final JSONObject json) {
        samples.clear();
        JSONArray sampleArray;
        try {
            sampleArray = (JSONArray) json.get("samples");
            int arrayLen = sampleArray.length();
            for(int i = 0; i < arrayLen; i++) {
                if (sampleArray.get(i) instanceof JSONObject) {
                    boolean isJsonValid = true;
                    JSONObject curObject = (JSONObject) sampleArray.get(i);
                    if (curObject != null) {
                        Sample newSample = new Sample();
                        double longitude = -1.0;
                        double latitude = -1.0;
                        if (!curObject.isNull(("longitude"))) {
                            longitude = curObject.getDouble("longitude");
                        } else {
                            isJsonValid = false;
                        }
                        if (isJsonValid && !curObject.isNull(("latitude"))) {
                            latitude = curObject.getDouble("latitude");
                        } else {
                            isJsonValid = false;
                        }
                        if (isJsonValid && !curObject.isNull(("noiseValue"))) {
                            newSample.noise = curObject.getDouble("noiseValue");
                        } else {
                            isJsonValid = false;
                        }
                        if (isJsonValid && !curObject.isNull(("timestamp"))) {
                            String timestamp = curObject.getString("timestamp");
                            DateFormat format = new SimpleDateFormat("yyyy-MM-dd'T'hh:mm:ss", Locale.ENGLISH);
                            try {
                                newSample.timestamp = format.parse(timestamp);
                            } catch (ParseException e) {
                                isJsonValid = false;
                            }
                        } else {
                            isJsonValid = false;
                        }
                        if (!isJsonValid) {
                            // ignore sample since it is invalid
                            continue;
                        } else {
                            newSample.position = new LatLng(latitude, longitude);
                            samples.add(newSample);
                        }
                    }
                }
            }
        } catch (JSONException e) {
            return false; // invalid response
        }
        return true;
    }
    private void initMatrix(final double NUM_OF_RECTS_WIDTH, final double numOfRectsHeight) {
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

    public void refresh(boolean fullRefresh) {

        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(activity);
        boolean isGridVisible = sharedPref.getBoolean("noisemap_tiles_show_grid", false);
        map.clear();
        if(fullRefresh) {
            performFullMapRefresh(fullRefresh);
        }
        if(overlayType == OverlayType.OVERLAY_HEATMAP) {
            updateHeatmapOverlay();
        } else if(overlayType == OverlayType.OVERLAY_TILES) {
            updateTileOverlay(isGridVisible);
        }
        if(isNoiseMatrixEmpty()) {
            Toast.makeText(activity,"No data!",Toast.LENGTH_LONG).show();
        }
    }

    private void performFullMapRefresh(boolean fullRefresh) {

        // load preferences
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(activity);
        String tilesCount = sharedPref.getString("noisemap_general_tileCountWidth", "20");
        String minStr = sharedPref.getString("noisemap_general_minNoise", "45");
        String maxStr = sharedPref.getString("noisemap_general_maxNoise", "80");
        float minNoise = Integer.parseInt(minStr);
        float maxNoise = Integer.parseInt(maxStr);
        int numberOfTilesWidth = Integer.parseInt(tilesCount);

        // geo. calculations
        LatLng northEastVisible = map.getProjection().getVisibleRegion().latLngBounds.northeast;
        LatLng southWestVisible = map.getProjection().getVisibleRegion().latLngBounds.southwest;
        LatLng northWestVisible = new LatLng(northEastVisible.latitude,southWestVisible.longitude);
        double radius = SphericalUtil.computeDistanceBetween(northWestVisible,northEastVisible) / numberOfTilesWidth / 2;
        LatLng start = SphericalUtil.computeOffset(northWestVisible, radius * Math.sqrt(2), Direction.SOUTHEAST);
        double offsetLong =  2 * (start.longitude - northWestVisible.longitude);
        double offsetLat =  2 * (start.latitude - northWestVisible.latitude);
        double numOfRectanglesHeight = SphericalUtil.computeDistanceBetween(northWestVisible, southWestVisible) / (2*radius);

        initMapCache(numberOfTilesWidth, numOfRectanglesHeight);
        clusterSamples(northWestVisible, offsetLong, offsetLat);

        // run through the whole map grid (vertically)
        //  ^
        //  |
        //  v
        for (int heightCounter = 0; heightCounter < numOfRectanglesHeight; heightCounter++) {
            // run through the whole map grid (horizontally)
            //
            // <--->
            //
            for (int widthCounter = 0; widthCounter < numberOfTilesWidth; widthCounter++) {

                LatLng center = new LatLng(start.latitude + heightCounter * offsetLat, start.longitude + widthCounter * offsetLong);

                double meanNoise = -1.0d;
                double normalizedNoise = -1.0d;
                if (fullRefresh) {
                    meanNoise = getMeanNoise(heightCounter, widthCounter);
                    normalizedNoise = getNormalizedNoise(meanNoise, maxNoise, minNoise);
                    WeightedLatLng curWLatLng = new WeightedLatLng(center, normalizedNoise);
                    cachedWeightedSamples.add(curWLatLng);
                    cachedMeanNoise.add(meanNoise);
                } else {
                    int index = heightCounter * numberOfTilesWidth + widthCounter;
                    if (index < cachedWeightedSamples.size()) {
                        normalizedNoise = cachedWeightedSamples.get(index).getIntensity();
                        meanNoise = cachedMeanNoise.get(index);
                    }
                }

                // add tiles
                PolygonOptions polyOptions = createPolygonOptions(radius, center, meanNoise, normalizedNoise, alpha);
                cachedPolygonOptions.add(polyOptions);
            }
        }
    }

    private void initMapCache(int numberOfTilesWidth, double numOfRectsHeight) {
        polygons.clear();
        cachedPolygonOptions.clear();
        cachedWeightedSamples.clear();
        cachedMeanNoise.clear();
        initMatrix(numberOfTilesWidth, numOfRectsHeight);
    }

    private void updateTileOverlay(boolean isGridVisible) {
        int counter = 0;
        polygons.clear();
        for(PolygonOptions option : cachedPolygonOptions) {
            double meanNoise = cachedMeanNoise.get(counter);
            if(isGridVisible || meanNoise > 0.0d) {
                Polygon poly = addPolygonToMap(meanNoise, option);
                polygons.add(poly);
            }
            counter++;
        }
    }

    private void updateHeatmapOverlay() {
        if(cachedWeightedSamples != null && cachedWeightedSamples.size() > 0) {
            Collection weightedSamples = new ArrayList();
            for(WeightedLatLng sample : cachedWeightedSamples) {
                Double curVal = sample.getIntensity();
                if(!Double.isNaN(curVal) && curVal > 0) {
                    weightedSamples.add(sample);
                }
            }
            if(weightedSamples.size() > 0) {
                provider = new HeatmapTileProvider.Builder()
                        .weightedData(weightedSamples)
                        .build();
                heatmapOverlay = map.addTileOverlay(new TileOverlayOptions().tileProvider(provider));
            }
            calculateBlur(map);
        }
    }

    private boolean isNoiseMatrixEmpty() {
        int sizeOfNoiseMatrix = noiseMatrix.size();

        for(int i = 0; i < sizeOfNoiseMatrix; i++) {
            for (int j = 0; j < noiseMatrix.get(i).size(); j++){
                if(noiseMatrix.get(i).get(j).size() != 0) {
                    return false;
                }
            }
        }
        return true;
    }

    private double getNormalizedNoise(double meanNoise, double max, double min) {
        if(Double.compare(meanNoise, -1.0) == 0) {
            return 0.0;
        }
        double normalizedNoise = (meanNoise - min) * 1/(max-min);
        if(normalizedNoise > 1) {
            normalizedNoise = 1;
        } else if(normalizedNoise <= 0.05) {
            normalizedNoise = 0.05;
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

    private PolygonOptions createPolygonOptions(double radius, LatLng center, double meanNoise, double normalizedNoise, double alpha) {

        // The diagonal length of the tile from the center to an edge (diag) is sqrt(2)*radius
        // since r^2 + r^2 = diag^2
        LatLng targetNorthWest = SphericalUtil.computeOffset(center, radius * Math.sqrt(2), Direction.NORTHWEST);
        LatLng targetNorthEast = SphericalUtil.computeOffset(center, radius * Math.sqrt(2), Direction.NORTHEAST);
        LatLng targetSouthWest = SphericalUtil.computeOffset(center, radius * Math.sqrt(2), Direction.SOUTHWEST);
        LatLng targetSouthEast = SphericalUtil.computeOffset(center, radius * Math.sqrt(2), Direction.SOUTHEAST);
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

    private Polygon addPolygonToMap(double meanNoise, PolygonOptions polygonOptions) {
        Polygon poly = map.addPolygon(polygonOptions);
        if(meanNoise > 0d) {
           poly.setTag(String.format("%.2f",  meanNoise) + " db(A)");
       }
        return poly;
    }

    public void setWeekdayFilter(String weekday) {
        weekdayFilter = weekday;
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

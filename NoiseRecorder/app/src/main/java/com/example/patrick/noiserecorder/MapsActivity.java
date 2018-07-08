package com.example.patrick.noiserecorder;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;

import com.example.patrick.noiserecorder.noisemap.HeatMap;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;

import java.util.ArrayList;
import java.util.List;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback {

    private String accessToken;
    private String username;
    private String password;

    private BottomNavigationView.OnNavigationItemSelectedListener onNavigationItemSelectedListener
            = new BottomNavigationView.OnNavigationItemSelectedListener() {

        @Override
        public boolean onNavigationItemSelected(@NonNull MenuItem item) {
            Intent intent;
            switch (item.getItemId()) {
                case R.id.navigation_home:
                    Bundle b = new Bundle();
                    b.putString("accessToken", accessToken);
                    b.putString("username", username);
                    b.putString("password", password);
                    intent = new Intent(MapsActivity.this, MainActivity.class);
                    intent.putExtras(b);
                    startActivity(intent);
                    return true;
                case R.id.navigation_map:
                    // do nothing
                    return true;
                case R.id.navigation_notifications:
                    intent = new Intent(MapsActivity.this, SettingsActivity.class);
                    startActivity(intent);
                    return true;
            }
            return false;
        }
    };
    private GoogleMap map;
    private HeatMap heatmap;

    private HeatMap.OverlayType overlayType = HeatMap.OverlayType.OVERLAY_TILES;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        Bundle b = getIntent().getExtras();
        if (b != null) {
            accessToken = b.getString("accessToken");
            username = b.getString("username");
            password = b.getString("password");
        }
        BottomNavigationView navigation = (BottomNavigationView) findViewById(R.id.navigation);
        navigation.setSelectedItemId(R.id.navigation_map);
        navigation.setOnNavigationItemSelectedListener(onNavigationItemSelectedListener);

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        final Button btnRefresh = (Button) findViewById(R.id.refresh_map);
        btnRefresh.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(heatmap != null) {
                    refreshData();
                }
            }
        });
        final Button btnToggleMapOverlay = (Button) findViewById(R.id.toggleMapOverlay);
        btnToggleMapOverlay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                toggleMapOverlay();
            }
        });

        Spinner spinnerWeekdayFilter = (Spinner) findViewById(R.id.weekdayFilterSpinner);
        spinnerWeekdayFilter.setBackgroundResource(android.R.drawable.btn_dropdown);
        final List<String> filterList = new ArrayList<>();
        filterList.add("No Filter");
        filterList.add("Mon");
        filterList.add("Tue");
        filterList.add("Wed");
        filterList.add("Thu");
        filterList.add("Fri");
        filterList.add("Sat");
        filterList.add("Sun");

        ArrayAdapter<String> filterDataAdapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item, filterList);
        filterDataAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerWeekdayFilter.setAdapter(filterDataAdapter);
        spinnerWeekdayFilter.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            boolean isFirstSelect = true;
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if(heatmap != null) {
                    heatmap.setWeekdayFilter(filterList.get(position));
                    if(!isFirstSelect) {
                        heatmap.refresh(true);
                    }
                }
                isFirstSelect = false;
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });


    }
    private void setMapType() {
        if(map == null) {
            return;
        }
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(this);
        String mapStyle = sharedPref.getString("map_style", "");
        int mapTypeId = 1;
        switch (mapStyle) {
            case "Normal":
                mapTypeId = 1;
                break;
            case "Satellite":
                mapTypeId = 2;
                break;
            case "Terrain":
                mapTypeId = 3;
                break;
            case "Hybrid":
                mapTypeId = 4;
                break;
            default:
                Log.e("MapsActivitiy","Invalid Map Type: " + mapStyle);
                break;
        }
        map.setMapType(mapTypeId);

    }
    @Override
    public void onResume() {
        super.onResume();
        ((BottomNavigationView)findViewById(R.id.navigation)).setSelectedItemId(R.id.navigation_map);
        setMapType();
        if(heatmap != null) {
            SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(this);
            String transparency = sharedPref.getString("noisemap_tiles_transparency", "0.2");
            heatmap.refresh(false);
            heatmap.setAlpha(1.0 - Float.parseFloat(transparency));
        }

    }
    private void toggleMapOverlay() {
        if(overlayType == HeatMap.OverlayType.OVERLAY_HEATMAP) {
            overlayType = HeatMap.OverlayType.OVERLAY_TILES;
        } else {
            overlayType = HeatMap.OverlayType.OVERLAY_HEATMAP;
        }
        heatmap.setOverlayType(overlayType);
        heatmap.refresh(false);
    }

    private void refreshData() {
        if(heatmap != null) {
            heatmap.requestSamplesForVisibleArea();
        }
    }

    //TODO
    LatLng bielefeld = new LatLng(52.0382444, 8.5257916);

    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(this);
        String transparency = sharedPref.getString("noisemap_tiles_transparency", "0.2");
        map = googleMap;
        setMapType();
        // TODO
        map.moveCamera(CameraUpdateFactory.newLatLngZoom(bielefeld,16.0f));
        heatmap = new HeatMap(map, 1.0 - Float.parseFloat(transparency), accessToken, username, password, this);
        heatmap.setWeekdayFilter("No Filter");
        heatmap.requestSamplesForVisibleArea();
    }
}

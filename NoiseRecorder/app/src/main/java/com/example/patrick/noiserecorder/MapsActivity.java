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
import android.widget.CompoundButton;
import android.widget.SeekBar;
import android.widget.Spinner;
import android.widget.Switch;
import android.widget.Toast;

import com.android.volley.RequestQueue;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.example.patrick.noiserecorder.network.OnRequestResponseCallback;
import com.example.patrick.noiserecorder.network.RestCallFactory;
import com.example.patrick.noiserecorder.noisemap.HeatMap;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback {

    private String accessToken;
    private BottomNavigationView.OnNavigationItemSelectedListener onNavigationItemSelectedListener
            = new BottomNavigationView.OnNavigationItemSelectedListener() {

        @Override
        public boolean onNavigationItemSelected(@NonNull MenuItem item) {
            Intent intent;
            switch (item.getItemId()) {
                case R.id.navigation_home:
                    Bundle b = new Bundle();
                    b.putString("accessToken", accessToken);
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
    private Switch switchGrid;
    private SeekBar seekbarAlpha;

    private HeatMap.OverlayType overlayType = HeatMap.OverlayType.OVERLAY_TILES;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        Bundle b = getIntent().getExtras();
        if (b != null) {
            accessToken = b.getString("accessToken");
        }
        BottomNavigationView navigation = (BottomNavigationView) findViewById(R.id.navigation);
        navigation.setSelectedItemId(R.id.navigation_map);
        navigation.setOnNavigationItemSelectedListener(onNavigationItemSelectedListener);

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
        switchGrid = (Switch) findViewById(R.id.switchGrid);
        seekbarAlpha= (SeekBar) findViewById(R.id.seekbarAlpha);

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
        final Switch switchGrid = (Switch) findViewById(R.id.switchGrid);
        switchGrid.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean showGrid) {
                heatmap.setGridVisible(showGrid);
                if(heatmap != null) {
                    heatmap.refresh(true);
                }
            }
        });
        seekbarAlpha.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                double a = progress/100.d;
                heatmap.setAlpha(a);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });

        Spinner spinnerWeekdayFilter = (Spinner) findViewById(R.id.weekdayFilterSpinner);
        spinnerWeekdayFilter.setBackgroundResource(android.R.drawable.btn_dropdown);
        final List<String> filterList = new ArrayList<>();
        filterList.add("No Filter");
        filterList.add("Mo");
        filterList.add("Tue");
        filterList.add("Wed");
        filterList.add("Thu");
        filterList.add("Fri");
        filterList.add("Sa");
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
    }
    private void toggleMapOverlay() {
        if(overlayType == HeatMap.OverlayType.OVERLAY_HEATMAP) {
            overlayType = HeatMap.OverlayType.OVERLAY_TILES;
            switchGrid.setVisibility(View.VISIBLE);
            seekbarAlpha.setVisibility(View.VISIBLE);
        } else {
            overlayType = HeatMap.OverlayType.OVERLAY_HEATMAP;
            switchGrid.setVisibility(View.INVISIBLE);
            seekbarAlpha.setVisibility(View.INVISIBLE);
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
        map = googleMap;
        setMapType();
        // TODO
        map.moveCamera(CameraUpdateFactory.newLatLngZoom(bielefeld,16.0f));
        heatmap = new HeatMap(map, getAlpha(), accessToken, this);
        heatmap.requestSamplesForVisibleArea();
    }

    private double getAlpha() {
        return seekbarAlpha.getProgress()/100.0d;
    }
}

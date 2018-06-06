package com.example.patrick.noiserecorder;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.v4.app.FragmentActivity;
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

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback, GoogleMap.OnMapLoadedCallback,OnRequestResponseCallback {

    private RequestQueue requestQueue;
    private String accessToken;
    private BottomNavigationView.OnNavigationItemSelectedListener onNavigationItemSelectedListener
            = new BottomNavigationView.OnNavigationItemSelectedListener() {

        @Override
        public boolean onNavigationItemSelected(@NonNull MenuItem item) {
            switch (item.getItemId()) {
                case R.id.navigation_home:
                    Bundle b = new Bundle();
                    b.putString("accessToken", accessToken);
                    Intent intent = new Intent(MapsActivity.this, MainActivity.class);
                    intent.putExtras(b);
                    startActivity(intent);
                    return true;
                case R.id.navigation_map:
                    // do nothing
                    return true;
                case R.id.navigation_notifications:

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
        requestQueue = Volley.newRequestQueue(this);
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
                refresh(true);
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
                refresh(false);
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

        Spinner spinnerMapType = (Spinner) findViewById(R.id.mapTypeSpinner);
        spinnerMapType.setBackgroundResource(android.R.drawable.btn_dropdown);
        List<String> list = new ArrayList<String>();
        list.add("Normal");
        list.add("Satellit");
        list.add("Terrain");
        list.add("Hybrid");

        ArrayAdapter<String> dataAdapter = new ArrayAdapter<String>(this,
                android.R.layout.simple_spinner_item, list);
        dataAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerMapType.setAdapter(dataAdapter);
        spinnerMapType.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if(map != null) {
                    map.setMapType((int)id+1);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

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
        refresh(false);
    }

    private void refresh(boolean fullRefresh) {
        if(heatmap != null) {
            heatmap.refresh(overlayType, fullRefresh);
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
        map.moveCamera(CameraUpdateFactory.newLatLngZoom(bielefeld,16.0f));

        heatmap = new HeatMap(map, getAlpha(), this);
        String apiUrl = "http://noisemaprestapi.azurewebsites.net/api/Sample";
        StringRequest apiRequest = RestCallFactory.createGetRequest(apiUrl,accessToken, this);
        requestQueue.add(apiRequest);
    }

    @Override
    public void onMapLoaded() {

    }
    @Override
    public void onRequestResponseCallback(JSONObject response) {
        boolean success = heatmap.parseSamples(response);
        if(success) {
            refresh(true);
        } else {
            Toast.makeText(this,"Error parsing the JSON sample response",Toast.LENGTH_LONG).show();

        }

    }
    private double getAlpha() {
        return seekbarAlpha.getProgress()/100.0d;
    }
}

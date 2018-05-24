package com.example.patrick.noiserecorder;

import android.graphics.Color;
import android.os.SystemClock;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.SeekBar;
import android.widget.Spinner;
import android.widget.Switch;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MapStyleOptions;
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

//TODO
class Sample {
    public LatLng position;
    public double noise;

    public Sample(LatLng position, double noise) {
        this.position = position;
        this.noise = noise;

    }
}
public class MapsActivity extends FragmentActivity implements OnMapReadyCallback, GoogleMap.OnMapLoadedCallback, GoogleMap.OnCameraIdleListener {

    final int DIRECTION_NORTHEAST = 45;
    final int DIRECTION_SOUTHEAST = 135;
    final int DIRECTION_SOUTHWEST = 225;
    final int DIRECTION_NORTHWEST = 315;

    private GoogleMap map;
    private String testResponse = "[{\"$id\":\"559\",\"id\":558,\"timestamp\":\"2018-04-28T11:49:32\",\"noiseValue\":47.36132,\"longitude\":8.525791,\"latitude\":52.038246,\"accuracy\":20.052,\"version\":\"AAAAAAAAB9g=\",\"createdAt\":\"2018-04-28T11:49:32\",\"updatedAt\":\"2018-04-28T11:49:32\",\"deleted\":false,\"speed\":0,\"userName\":null},{\"$id\":\"560\",\"id\":559,\"timestamp\":\"2018-04-28T11:49:42\",\"noiseValue\":50.470062,\"longitude\":8.525819,\"latitude\":52.038204,\"accuracy\":20.646,\"version\":\"AAAAAAAAB9g=\",\"createdAt\":\"2018-04-28T11:49:42\",\"updatedAt\":\"2018-04-28T11:49:42\",\"deleted\":false,\"speed\":0,\"userName\":null},{\"$id\":\"561\",\"id\":560,\"timestamp\":\"2018-04-28T11:49:52\",\"noiseValue\":60.75827,\"longitude\":8.525695,\"latitude\":52.038242,\"accuracy\":19.801,\"version\":\"AAAAAAAAB9g=\",\"createdAt\":\"2018-04-28T11:49:52\",\"updatedAt\":\"2018-04-28T11:49:52\",\"deleted\":false,\"speed\":0,\"userName\":null},{\"$id\":\"562\",\"id\":561,\"timestamp\":\"2018-04-28T11:50:02\",\"noiseValue\":57.878666,\"longitude\":8.525689,\"latitude\":52.038242,\"accuracy\":19.974,\"version\":\"AAAAAAAAB9g=\",\"createdAt\":\"2018-04-28T11:50:02\",\"updatedAt\":\"2018-04-28T11:50:02\",\"deleted\":false,\"speed\":0,\"userName\":null},{\"$id\":\"563\",\"id\":562,\"timestamp\":\"2018-04-28T11:50:12\",\"noiseValue\":49.84938,\"longitude\":8.525694,\"latitude\":52.03824,\"accuracy\":19.815,\"version\":\"AAAAAAAAB9g=\",\"createdAt\":\"2018-04-28T11:50:12\",\"updatedAt\":\"2018-04-28T11:50:12\",\"deleted\":false,\"speed\":0,\"userName\":null},{\"$id\":\"564\",\"id\":563,\"timestamp\":\"2018-04-28T11:50:22\",\"noiseValue\":51.52633,\"longitude\":8.525689,\"latitude\":52.038242,\"accuracy\":19.974,\"version\":\"AAAAAAAAB9g=\",\"createdAt\":\"2018-04-28T11:50:22\",\"updatedAt\":\"2018-04-28T11:50:22\",\"deleted\":false,\"speed\":0,\"userName\":null},{\"$id\":\"565\",\"id\":564,\"timestamp\":\"2018-04-28T11:50:32\",\"noiseValue\":48.2754,\"longitude\":8.525694,\"latitude\":52.03824,\"accuracy\":19.815,\"version\":\"AAAAAAAAB9g=\",\"createdAt\":\"2018-04-28T11:50:32\",\"updatedAt\":\"2018-04-28T11:50:32\",\"deleted\":false,\"speed\":0,\"userName\":null},{\"$id\":\"566\",\"id\":565,\"timestamp\":\"2018-04-28T11:50:42\",\"noiseValue\":58.231964,\"longitude\":8.525694,\"latitude\":52.03824,\"accuracy\":19.729,\"version\":\"AAAAAAAAB9g=\",\"createdAt\":\"2018-04-28T11:50:42\",\"updatedAt\":\"2018-04-28T11:50:42\",\"deleted\":false,\"speed\":0,\"userName\":null},{\"$id\":\"567\",\"id\":566,\"timestamp\":\"2018-04-28T11:50:52\",\"noiseValue\":62.152008,\"longitude\":8.525692,\"latitude\":52.038242,\"accuracy\":19.88,\"version\":\"AAAAAAAAB9g=\",\"createdAt\":\"2018-04-28T11:50:52\",\"updatedAt\":\"2018-04-28T11:50:52\",\"deleted\":false,\"speed\":0,\"userName\":null},{\"$id\":\"568\",\"id\":567,\"timestamp\":\"2018-04-28T11:51:02\",\"noiseValue\":60.988823,\"longitude\":8.52574,\"latitude\":52.03812,\"accuracy\":20.895,\"version\":\"AAAAAAAAB9g=\",\"createdAt\":\"2018-04-28T11:51:02\",\"updatedAt\":\"2018-04-28T11:51:02\",\"deleted\":false,\"speed\":0,\"userName\":null},{\"$id\":\"569\",\"id\":568,\"timestamp\":\"2018-04-28T11:51:12\",\"noiseValue\":54.315258,\"longitude\":8.525685,\"latitude\":52.03821,\"accuracy\":21.48,\"version\":\"AAAAAAAAB9g=\",\"createdAt\":\"2018-04-28T11:51:12\",\"updatedAt\":\"2018-04-28T11:51:12\",\"deleted\":false,\"speed\":0,\"userName\":null},{\"$id\":\"570\",\"id\":569,\"timestamp\":\"2018-04-28T11:51:22\",\"noiseValue\":54.96376,\"longitude\":8.525761,\"latitude\":52.038113,\"accuracy\":20.263,\"version\":\"AAAAAAAAB9g=\",\"createdAt\":\"2018-04-28T11:51:22\",\"updatedAt\":\"2018-04-28T11:51:22\",\"deleted\":false,\"speed\":0,\"userName\":null},{\"$id\":\"571\",\"id\":570,\"timestamp\":\"2018-04-28T11:51:32\",\"noiseValue\":53.13754,\"longitude\":8.525922,\"latitude\":52.03809,\"accuracy\":35.129,\"version\":\"AAAAAAAAB9g=\",\"createdAt\":\"2018-04-28T11:51:32\",\"updatedAt\":\"2018-04-28T11:51:32\",\"deleted\":false,\"speed\":0,\"userName\":null},{\"$id\":\"572\",\"id\":571,\"timestamp\":\"2018-04-28T11:51:42\",\"noiseValue\":67.08051,\"longitude\":8.525922,\"latitude\":52.03809,\"accuracy\":35.129,\"version\":\"AAAAAAAAB9g=\",\"createdAt\":\"2018-04-28T11:51:42\",\"updatedAt\":\"2018-04-28T11:51:42\",\"deleted\":false,\"speed\":0,\"userName\":null},{\"$id\":\"573\",\"id\":572,\"timestamp\":\"2018-04-28T11:51:52\",\"noiseValue\":53.483074,\"longitude\":8.525561,\"latitude\":52.03818,\"accuracy\":26.404,\"version\":\"AAAAAAAAB9g=\",\"createdAt\":\"2018-04-28T11:51:52\",\"updatedAt\":\"2018-04-28T11:51:52\",\"deleted\":false,\"speed\":0,\"userName\":null},{\"$id\":\"574\",\"id\":573,\"timestamp\":\"2018-04-28T11:52:02\",\"noiseValue\":52.925674,\"longitude\":8.525654,\"latitude\":52.038216,\"accuracy\":25.267,\"version\":\"AAAAAAAAB9g=\",\"createdAt\":\"2018-04-28T11:52:02\",\"updatedAt\":\"2018-04-28T11:52:02\",\"deleted\":false,\"speed\":0,\"userName\":null},{\"$id\":\"575\",\"id\":574,\"timestamp\":\"2018-04-28T11:52:12\",\"noiseValue\":51.433002,\"longitude\":8.525654,\"latitude\":52.038216,\"accuracy\":25.267,\"version\":\"AAAAAAAAB9g=\",\"createdAt\":\"2018-04-28T11:52:12\",\"updatedAt\":\"2018-04-28T11:52:12\",\"deleted\":false,\"speed\":0,\"userName\":null},{\"$id\":\"576\",\"id\":575,\"timestamp\":\"2018-04-28T11:52:22\",\"noiseValue\":50.9081,\"longitude\":8.52556,\"latitude\":52.038143,\"accuracy\":39.614,\"version\":\"AAAAAAAAB9g=\",\"createdAt\":\"2018-04-28T11:52:22\",\"updatedAt\":\"2018-04-28T11:52:22\",\"deleted\":false,\"speed\":0,\"userName\":null},{\"$id\":\"577\",\"id\":576,\"timestamp\":\"2018-04-28T11:52:32\",\"noiseValue\":60.75827,\"longitude\":8.525603,\"latitude\":52.03797,\"accuracy\":21.102,\"version\":\"AAAAAAAAB9g=\",\"createdAt\":\"2018-04-28T11:52:32\",\"updatedAt\":\"2018-04-28T11:52:32\",\"deleted\":false,\"speed\":0,\"userName\":null},{\"$id\":\"578\",\"id\":577,\"timestamp\":\"2018-04-28T11:52:42\",\"noiseValue\":55.462463,\"longitude\":8.525614,\"latitude\":52.038216,\"accuracy\":44.114,\"version\":\"AAAAAAAAB9g=\",\"createdAt\":\"2018-04-28T11:52:42\",\"updatedAt\":\"2018-04-28T11:52:42\",\"deleted\":false,\"speed\":0,\"userName\":null},{\"$id\":\"579\",\"id\":578,\"timestamp\":\"2018-04-28T11:52:52\",\"noiseValue\":65.163574,\"longitude\":8.525603,\"latitude\":52.03797,\"accuracy\":21.102,\"version\":\"AAAAAAAAB9g=\",\"createdAt\":\"2018-04-28T11:52:52\",\"updatedAt\":\"2018-04-28T11:52:52\",\"deleted\":false,\"speed\":0,\"userName\":null},{\"$id\":\"580\",\"id\":579,\"timestamp\":\"2018-04-28T11:53:02\",\"noiseValue\":54.13055,\"longitude\":8.525637,\"latitude\":52.03798,\"accuracy\":19.972,\"version\":\"AAAAAAAAB9g=\",\"createdAt\":\"2018-04-28T11:53:02\",\"updatedAt\":\"2018-04-28T11:53:02\",\"deleted\":false,\"speed\":0,\"userName\":null},{\"$id\":\"581\",\"id\":580,\"timestamp\":\"2018-04-28T11:53:12\",\"noiseValue\":58.077408,\"longitude\":8.525638,\"latitude\":52.037983,\"accuracy\":20.303,\"version\":\"AAAAAAAAB9g=\",\"createdAt\":\"2018-04-28T11:53:12\",\"updatedAt\":\"2018-04-28T11:53:12\",\"deleted\":false,\"speed\":0,\"userName\":null},{\"$id\":\"582\",\"id\":581,\"timestamp\":\"2018-04-28T11:53:22\",\"noiseValue\":60.556667,\"longitude\":8.525636,\"latitude\":52.03798,\"accuracy\":19.975,\"version\":\"AAAAAAAAB9g=\",\"createdAt\":\"2018-04-28T11:53:22\",\"updatedAt\":\"2018-04-28T11:53:22\",\"deleted\":false,\"speed\":0,\"userName\":null},{\"$id\":\"583\",\"id\":582,\"timestamp\":\"2018-04-28T11:53:32\",\"noiseValue\":63.883297,\"longitude\":8.525637,\"latitude\":52.03798,\"accuracy\":19.972,\"version\":\"AAAAAAAAB9g=\",\"createdAt\":\"2018-04-28T11:53:32\",\"updatedAt\":\"2018-04-28T11:53:32\",\"deleted\":false,\"speed\":0,\"userName\":null},{\"$id\":\"584\",\"id\":583,\"timestamp\":\"2018-04-28T11:53:42\",\"noiseValue\":63.24845,\"longitude\":8.525637,\"latitude\":52.03798,\"accuracy\":19.966,\"version\":\"AAAAAAAAB9g=\",\"createdAt\":\"2018-04-28T11:53:42\",\"updatedAt\":\"2018-04-28T11:53:42\",\"deleted\":false,\"speed\":0,\"userName\":null},{\"$id\":\"585\",\"id\":584,\"timestamp\":\"2018-04-28T11:53:52\",\"noiseValue\":65.25141,\"longitude\":8.525639,\"latitude\":52.037983,\"accuracy\":20.269,\"version\":\"AAAAAAAAB9g=\",\"createdAt\":\"2018-04-28T11:53:52\",\"updatedAt\":\"2018-04-28T11:53:52\",\"deleted\":false,\"speed\":0,\"userName\":null},{\"$id\":\"586\",\"id\":585,\"timestamp\":\"2018-04-28T11:54:02\",\"noiseValue\":71.38731,\"longitude\":8.525639,\"latitude\":52.037983,\"accuracy\":20.269,\"version\":\"AAAAAAAAB9g=\",\"createdAt\":\"2018-04-28T11:54:02\",\"updatedAt\":\"2018-04-28T11:54:02\",\"deleted\":false,\"speed\":0,\"userName\":null},{\"$id\":\"587\",\"id\":586,\"timestamp\":\"2018-04-28T11:54:12\",\"noiseValue\":60.08952,\"longitude\":8.525635,\"latitude\":52.03797,\"accuracy\":20.388,\"version\":\"AAAAAAAAB9g=\",\"createdAt\":\"2018-04-28T11:54:12\",\"updatedAt\":\"2018-04-28T11:54:12\",\"deleted\":false,\"speed\":0,\"userName\":null},{\"$id\":\"588\",\"id\":587,\"timestamp\":\"2018-04-28T11:54:22\",\"noiseValue\":59.15814,\"longitude\":8.525636,\"latitude\":52.037968,\"accuracy\":20.058,\"version\":\"AAAAAAAAB9g=\",\"createdAt\":\"2018-04-28T11:54:22\",\"updatedAt\":\"2018-04-28T11:54:22\",\"deleted\":false,\"speed\":0,\"userName\":null},{\"$id\":\"589\",\"id\":588,\"timestamp\":\"2018-04-28T11:54:32\",\"noiseValue\":62.57096,\"longitude\":8.525638,\"latitude\":52.03798,\"accuracy\":20.314,\"version\":\"AAAAAAAAB9g=\",\"createdAt\":\"2018-04-28T11:54:32\",\"updatedAt\":\"2018-04-28T11:54:32\",\"deleted\":false,\"speed\":0,\"userName\":null},{\"$id\":\"590\",\"id\":589,\"timestamp\":\"2018-04-28T11:54:42\",\"noiseValue\":63.379864,\"longitude\":8.525636,\"latitude\":52.037968,\"accuracy\":20.058,\"version\":\"AAAAAAAAB9g=\",\"createdAt\":\"2018-04-28T11:54:42\",\"updatedAt\":\"2018-04-28T11:54:42\",\"deleted\":false,\"speed\":0,\"userName\":null},{\"$id\":\"591\",\"id\":590,\"timestamp\":\"2018-04-28T11:54:52\",\"noiseValue\":61.87649,\"longitude\":8.525637,\"latitude\":52.037983,\"accuracy\":19.95,\"version\":\"AAAAAAAAB9g=\",\"createdAt\":\"2018-04-28T11:54:52\",\"updatedAt\":\"2018-04-28T11:54:52\",\"deleted\":false,\"speed\":0,\"userName\":null},{\"$id\":\"592\",\"id\":591,\"timestamp\":\"2018-04-28T11:55:02\",\"noiseValue\":61.351425,\"longitude\":8.525634,\"latitude\":52.03798,\"accuracy\":20.363,\"version\":\"AAAAAAAAB9g=\",\"createdAt\":\"2018-04-28T11:55:02\",\"updatedAt\":\"2018-04-28T11:55:02\",\"deleted\":false,\"speed\":0,\"userName\":null},{\"$id\":\"593\",\"id\":592,\"timestamp\":\"2018-04-28T11:55:12\",\"noiseValue\":58.00223,\"longitude\":8.525634,\"latitude\":52.03798,\"accuracy\":20.363,\"version\":\"AAAAAAAAB9g=\",\"createdAt\":\"2018-04-28T11:55:12\",\"updatedAt\":\"2018-04-28T11:55:12\",\"deleted\":false,\"speed\":0,\"userName\":null},{\"$id\":\"594\",\"id\":593,\"timestamp\":\"2018-04-28T11:55:22\",\"noiseValue\":67.67786,\"longitude\":8.525634,\"latitude\":52.03798,\"accuracy\":20.363,\"version\":\"AAAAAAAAB9g=\",\"createdAt\":\"2018-04-28T11:55:22\",\"updatedAt\":\"2018-04-28T11:55:22\",\"deleted\":false,\"speed\":0,\"userName\":null},{\"$id\":\"595\",\"id\":594,\"timestamp\":\"2018-04-28T11:55:32\",\"noiseValue\":62.10113,\"longitude\":8.525634,\"latitude\":52.03798,\"accuracy\":20.036,\"version\":\"AAAAAAAAB9g=\",\"createdAt\":\"2018-04-28T11:55:32\",\"updatedAt\":\"2018-04-28T11:55:32\",\"deleted\":false,\"speed\":0,\"userName\":null},{\"$id\":\"596\",\"id\":595,\"timestamp\":\"2018-04-28T11:55:42\",\"noiseValue\":60.55198,\"longitude\":8.525639,\"latitude\":52.03797,\"accuracy\":19.742,\"version\":\"AAAAAAAAB9g=\",\"createdAt\":\"2018-04-28T11:55:42\",\"updatedAt\":\"2018-04-28T11:55:42\",\"deleted\":false,\"speed\":0,\"userName\":null},{\"$id\":\"597\",\"id\":596,\"timestamp\":\"2018-04-28T11:55:52\",\"noiseValue\":56.978394,\"longitude\":8.525636,\"latitude\":52.037968,\"accuracy\":19.701,\"version\":\"AAAAAAAAB9g=\",\"createdAt\":\"2018-04-28T11:55:52\",\"updatedAt\":\"2018-04-28T11:55:52\",\"deleted\":false,\"speed\":0,\"userName\":null},{\"$id\":\"598\",\"id\":597,\"timestamp\":\"2018-04-28T11:56:02\",\"noiseValue\":72.55675,\"longitude\":8.525636,\"latitude\":52.03797,\"accuracy\":19.887,\"version\":\"AAAAAAAAB9g=\",\"createdAt\":\"2018-04-28T11:56:02\",\"updatedAt\":\"2018-04-28T11:56:02\",\"deleted\":false,\"speed\":0,\"userName\":null},{\"$id\":\"599\",\"id\":598,\"timestamp\":\"2018-04-28T11:56:12\",\"noiseValue\":62.028217,\"longitude\":8.525637,\"latitude\":52.03797,\"accuracy\":19.845,\"version\":\"AAAAAAAAB9g=\",\"createdAt\":\"2018-04-28T11:56:12\",\"updatedAt\":\"2018-04-28T11:56:12\",\"deleted\":false,\"speed\":0,\"userName\":null},{\"$id\":\"600\",\"id\":599,\"timestamp\":\"2018-04-28T11:56:22\",\"noiseValue\":58.31126,\"longitude\":8.525255,\"latitude\":52.037838,\"accuracy\":24.807,\"version\":\"AAAAAAAAB9g=\",\"createdAt\":\"2018-04-28T11:56:22\",\"updatedAt\":\"2018-04-28T11:56:22\",\"deleted\":false,\"speed\":0,\"userName\":null},{\"$id\":\"601\",\"id\":600,\"timestamp\":\"2018-04-28T11:56:30\",\"noiseValue\":56.56151,\"longitude\":8.524998,\"latitude\":52.037895,\"accuracy\":8,\"version\":\"AAAAAAAAB9g=\",\"createdAt\":\"2018-04-28T11:56:30\",\"updatedAt\":\"2018-04-28T11:56:30\",\"deleted\":false,\"speed\":0.8539869,\"userName\":null},{\"$id\":\"602\",\"id\":601,\"timestamp\":\"2018-04-28T11:56:32\",\"noiseValue\":56.174225,\"longitude\":8.525637,\"latitude\":52.03797,\"accuracy\":19.845,\"version\":\"AAAAAAAAB9g=\",\"createdAt\":\"2018-04-28T11:56:32\",\"updatedAt\":\"2018-04-28T11:56:32\",\"deleted\":false,\"speed\":0,\"userName\":null},{\"$id\":\"603\",\"id\":602,\"timestamp\":\"2018-04-28T11:56:33\",\"noiseValue\":60.930634,\"longitude\":8.524943,\"latitude\":52.037918,\"accuracy\":8,\"version\":\"AAAAAAAAB9g=\",\"createdAt\":\"2018-04-28T11:56:33\",\"updatedAt\":\"2018-04-28T11:56:33\",\"deleted\":false,\"speed\":1.0995513,\"userName\":null},{\"$id\":\"604\",\"id\":603,\"timestamp\":\"2018-04-28T11:56:42\",\"noiseValue\":59.67142,\"longitude\":8.524806,\"latitude\":52.038006,\"accuracy\":8,\"version\":\"AAAAAAAAB9g=\",\"createdAt\":\"2018-04-28T11:56:42\",\"updatedAt\":\"2018-04-28T11:56:42\",\"deleted\":false,\"speed\":0.6904297,\"userName\":null},{\"$id\":\"605\",\"id\":604,\"timestamp\":\"2018-04-28T11:56:52\",\"noiseValue\":78.95051,\"longitude\":8.524684,\"latitude\":52.038044,\"accuracy\":6,\"version\":\"AAAAAAAAB9g=\",\"createdAt\":\"2018-04-28T11:56:52\",\"updatedAt\":\"2018-04-28T11:56:52\",\"deleted\":false,\"speed\":1.0809401,\"userName\":null},{\"$id\":\"606\",\"id\":605,\"timestamp\":\"2018-04-28T11:58:53\",\"noiseValue\":55.898083,\"longitude\":8.524527,\"latitude\":52.036514,\"accuracy\":8,\"version\":\"AAAAAAAAB9g=\",\"createdAt\":\"2018-04-28T11:58:53\",\"updatedAt\":\"2018-04-28T11:58:53\",\"deleted\":false,\"speed\":0.9417844,\"userName\":null},{\"$id\":\"607\",\"id\":606,\"timestamp\":\"2018-04-28T11:59:02\",\"noiseValue\":61.783356,\"longitude\":8.52477,\"latitude\":52.036476,\"accuracy\":8,\"version\":\"AAAAAAAAB9g=\",\"createdAt\":\"2018-04-28T11:59:02\",\"updatedAt\":\"2018-04-28T11:59:02\",\"deleted\":false,\"speed\":1.5994666,\"userName\":null},{\"$id\":\"608\",\"id\":607,\"timestamp\":\"2018-04-28T11:59:12\",\"noiseValue\":71.796585,\"longitude\":8.52497,\"latitude\":52.03642,\"accuracy\":6,\"version\":\"AAAAAAAAB9g=\",\"createdAt\":\"2018-04-28T11:59:12\",\"updatedAt\":\"2018-04-28T11:59:12\",\"deleted\":false,\"speed\":1.2886195,\"userName\":null},{\"$id\":\"609\",\"id\":608,\"timestamp\":\"2018-04-28T11:58:42\",\"noiseValue\":60.462383,\"longitude\":8.524425,\"latitude\":52.03662,\"accuracy\":8,\"version\":\"AAAAAAAAB9g=\",\"createdAt\":\"2018-04-28T11:58:42\",\"updatedAt\":\"2018-04-28T11:58:42\",\"deleted\":false,\"speed\":1.1426438,\"userName\":null},{\"$id\":\"610\",\"id\":609,\"timestamp\":\"2018-04-28T11:58:12\",\"noiseValue\":60.84488,\"longitude\":8.524474,\"latitude\":52.0373,\"accuracy\":6,\"version\":\"AAAAAAAAB9g=\",\"createdAt\":\"2018-04-28T11:58:12\",\"updatedAt\":\"2018-04-28T11:58:12\",\"deleted\":false,\"speed\":0.71019137,\"userName\":null},{\"$id\":\"611\",\"id\":610,\"timestamp\":\"2018-04-28T11:58:52\",\"noiseValue\":59.15814,\"longitude\":8.524408,\"latitude\":52.036964,\"accuracy\":8,\"version\":\"AAAAAAAAB9g=\",\"createdAt\":\"2018-04-28T11:58:52\",\"updatedAt\":\"2018-04-28T11:58:52\",\"deleted\":false,\"speed\":1.0970953,\"userName\":null},{\"$id\":\"612\",\"id\":611,\"timestamp\":\"2018-04-29T21:29:54\",\"noiseValue\":39.03611,\"longitude\":8.525386,\"latitude\":52.036198,\"accuracy\":15.995,\"version\":\"AAAAAAAAB9g=\",\"createdAt\":\"2018-04-29T21:29:54\",\"updatedAt\":\"2018-04-29T21:29:54\",\"deleted\":false,\"speed\":0,\"userName\":null},{\"$id\":\"613\",\"id\":612,\"timestamp\":\"2018-04-29T21:29:54\",\"noiseValue\":39.03611,\"longitude\":8.525386,\"latitude\":52.036198,\"accuracy\":15.995,\"version\":\"AAAAAAAAB9g=\",\"createdAt\":\"2018-04-29T21:29:54\",\"updatedAt\":\"2018-04-29T21:29:54\",\"deleted\":false,\"speed\":0,\"userName\":null},{\"$id\":\"614\",\"id\":613,\"timestamp\":\"2018-04-29T21:34:02\",\"noiseValue\":49.4545,\"longitude\":8.525369,\"latitude\":52.036198,\"accuracy\":18.317,\"version\":\"AAAAAAAAB9g=\",\"createdAt\":\"2018-04-29T21:34:02\",\"updatedAt\":\"2018-04-29T21:34:02\",\"deleted\":false,\"speed\":0,\"userName\":null},{\"$id\":\"615\",\"id\":614,\"timestamp\":\"2018-04-29T21:34:12\",\"noiseValue\":30.691843,\"longitude\":8.525384,\"latitude\":52.036198,\"accuracy\":18.973,\"version\":\"AAAAAAAAB9g=\",\"createdAt\":\"2018-04-29T21:34:12\",\"updatedAt\":\"2018-04-29T21:34:12\",\"deleted\":false,\"speed\":0,\"userName\":null},{\"$id\":\"616\",\"id\":615,\"timestamp\":\"2018-04-29T21:34:22\",\"noiseValue\":28.916338,\"longitude\":8.525384,\"latitude\":52.036198,\"accuracy\":18.973,\"version\":\"AAAAAAAAB9g=\",\"createdAt\":\"2018-04-29T21:34:22\",\"updatedAt\":\"2018-04-29T21:34:22\",\"deleted\":false,\"speed\":0,\"userName\":null},{\"$id\":\"617\",\"id\":616,\"timestamp\":\"2018-04-29T21:34:32\",\"noiseValue\":21.850273,\"longitude\":8.525361,\"latitude\":52.03623,\"accuracy\":15.951,\"version\":\"AAAAAAAAB9g=\",\"createdAt\":\"2018-04-29T21:34:32\",\"updatedAt\":\"2018-04-29T21:34:32\",\"deleted\":false,\"speed\":0,\"userName\":null},{\"$id\":\"618\",\"id\":617,\"timestamp\":\"2018-04-29T21:34:42\",\"noiseValue\":28.168703,\"longitude\":8.525347,\"latitude\":52.03623,\"accuracy\":16.819,\"version\":\"AAAAAAAAB9g=\",\"createdAt\":\"2018-04-29T21:34:42\",\"updatedAt\":\"2018-04-29T21:34:42\",\"deleted\":false,\"speed\":0,\"userName\":null},{\"$id\":\"619\",\"id\":618,\"timestamp\":\"2018-04-29T21:34:52\",\"noiseValue\":20.062265,\"longitude\":8.525343,\"latitude\":52.03623,\"accuracy\":16.842,\"version\":\"AAAAAAAAB9g=\",\"createdAt\":\"2018-04-29T21:34:52\",\"updatedAt\":\"2018-04-29T21:34:52\",\"deleted\":false,\"speed\":0,\"userName\":null},{\"$id\":\"620\",\"id\":619,\"timestamp\":\"2018-04-29T21:37:03\",\"noiseValue\":43.60062,\"longitude\":8.525385,\"latitude\":52.036198,\"accuracy\":15.762,\"version\":\"AAAAAAAAB9g=\",\"createdAt\":\"2018-04-29T21:37:03\",\"updatedAt\":\"2018-04-29T21:37:03\",\"deleted\":false,\"speed\":0,\"userName\":null},{\"$id\":\"621\",\"id\":620,\"timestamp\":\"2018-04-29T21:48:07\",\"noiseValue\":33.2633,\"longitude\":8.525384,\"latitude\":52.036198,\"accuracy\":16.855,\"version\":\"AAAAAAAAB9g=\",\"createdAt\":\"2018-04-29T21:48:07\",\"updatedAt\":\"2018-04-29T21:48:07\",\"deleted\":false,\"speed\":0,\"userName\":\"tester@test.com\"},{\"$id\":\"622\",\"id\":621,\"timestamp\":\"2018-04-30T08:40:43\",\"noiseValue\":38.092987,\"longitude\":8.523598,\"latitude\":52.037323,\"accuracy\":35.165,\"version\":\"AAAAAAAAB9g=\",\"createdAt\":\"2018-04-30T08:40:43\",\"updatedAt\":\"2018-04-30T08:40:43\",\"deleted\":false,\"speed\":0,\"userName\":\"tester@test.com\"},{\"$id\":\"623\",\"id\":622,\"timestamp\":\"2018-04-30T08:40:43\",\"noiseValue\":38.092987,\"longitude\":8.523598,\"latitude\":52.037323,\"accuracy\":35.165,\"version\":\"AAAAAAAAB9g=\",\"createdAt\":\"2018-04-30T08:40:43\",\"updatedAt\":\"2018-04-30T08:40:43\",\"deleted\":false,\"speed\":0,\"userName\":\"tester@test.com\"},{\"$id\":\"624\",\"id\":623,\"timestamp\":\"2018-04-30T08:40:53\",\"noiseValue\":33.941826,\"longitude\":8.523747,\"latitude\":52.037205,\"accuracy\":110,\"version\":\"AAAAAAAAB9g=\",\"createdAt\":\"2018-04-30T08:40:53\",\"updatedAt\":\"2018-04-30T08:40:53\",\"deleted\":false,\"speed\":0,\"userName\":\"tester@test.com\"},{\"$id\":\"625\",\"id\":624,\"timestamp\":\"2018-04-30T08:44:55\",\"noiseValue\":52.54496,\"longitude\":8.519467,\"latitude\":52.03997,\"accuracy\":1299.999,\"version\":\"AAAAAAAAB9g=\",\"createdAt\":\"2018-04-30T08:44:55\",\"updatedAt\":\"2018-04-30T08:44:55\",\"deleted\":false,\"speed\":0,\"userName\":\"tester@test.com\"},{\"$id\":\"626\",\"id\":625,\"timestamp\":\"2018-04-30T08:45:05\",\"noiseValue\":50.273167,\"longitude\":8.682796,\"latitude\":52.10624,\"accuracy\":72.9,\"version\":\"AAAAAAAAB9g=\",\"createdAt\":\"2018-04-30T08:45:05\",\"updatedAt\":\"2018-04-30T08:45:05\",\"deleted\":false,\"speed\":0,\"userName\":\"tester@test.com\"},{\"$id\":\"627\",\"id\":626,\"timestamp\":\"2018-04-30T08:45:16\",\"noiseValue\":55.487713,\"longitude\":8.534162,\"latitude\":52.028576,\"accuracy\":12.901,\"version\":\"AAAAAAAAB9g=\",\"createdAt\":\"2018-04-30T08:45:16\",\"updatedAt\":\"2018-04-30T08:45:16\",\"deleted\":false,\"speed\":0,\"userName\":\"tester@test.com\"},{\"$id\":\"628\",\"id\":627,\"timestamp\":\"2018-04-30T08:45:26\",\"noiseValue\":55.596287,\"longitude\":8.534061,\"latitude\":52.02847,\"accuracy\":10,\"version\":\"AAAAAAAAB9g=\",\"createdAt\":\"2018-04-30T08:45:26\",\"updatedAt\":\"2018-04-30T08:45:26\",\"deleted\":false,\"speed\":0.79103744,\"userName\":\"tester@test.com\"},{\"$id\":\"629\",\"id\":628,\"timestamp\":\"2018-04-30T08:45:36\",\"noiseValue\":73.55476,\"longitude\":8.533835,\"latitude\":52.028507,\"accuracy\":10,\"version\":\"AAAAAAAAB9g=\",\"createdAt\":\"2018-04-30T08:45:36\",\"updatedAt\":\"2018-04-30T08:45:36\",\"deleted\":false,\"speed\":0.9721185,\"userName\":\"tester@test.com\"},{\"$id\":\"630\",\"id\":629,\"timestamp\":\"2018-04-30T08:45:46\",\"noiseValue\":79.48964,\"longitude\":8.533585,\"latitude\":52.02865,\"accuracy\":10,\"version\":\"AAAAAAAAB9g=\",\"createdAt\":\"2018-04-30T08:45:46\",\"updatedAt\":\"2018-04-30T08:45:46\",\"deleted\":false,\"speed\":1.2732898,\"userName\":\"tester@test.com\"},{\"$id\":\"631\",\"id\":630,\"timestamp\":\"2018-04-30T08:45:56\",\"noiseValue\":74.05109,\"longitude\":8.533459,\"latitude\":52.028793,\"accuracy\":10,\"version\":\"AAAAAAAAB9g=\",\"createdAt\":\"2018-04-30T08:45:56\",\"updatedAt\":\"2018-04-30T08:45:56\",\"deleted\":false,\"speed\":1.2992569,\"userName\":\"tester@test.com\"},{\"$id\":\"632\",\"id\":631,\"timestamp\":\"2018-04-30T08:46:06\",\"noiseValue\":75.50115,\"longitude\":8.533406,\"latitude\":52.02894,\"accuracy\":10,\"version\":\"AAAAAAAAB9g=\",\"createdAt\":\"2018-04-30T08:46:06\",\"updatedAt\":\"2018-04-30T08:46:06\",\"deleted\":false,\"speed\":1.3176742,\"userName\":\"tester@test.com\"},{\"$id\":\"633\",\"id\":632,\"timestamp\":\"2018-04-30T08:46:16\",\"noiseValue\":72.976326,\"longitude\":8.533376,\"latitude\":52.029022,\"accuracy\":10,\"version\":\"AAAAAAAAB9g=\",\"createdAt\":\"2018-04-30T08:46:16\",\"updatedAt\":\"2018-04-30T08:46:16\",\"deleted\":false,\"speed\":0.8994334,\"userName\":\"tester@test.com\"},{\"$id\":\"634\",\"id\":633,\"timestamp\":\"2018-04-30T08:46:26\",\"noiseValue\":53.23612,\"longitude\":8.533411,\"latitude\":52.02916,\"accuracy\":10,\"version\":\"AAAAAAAAB9g=\",\"createdAt\":\"2018-04-30T08:46:26\",\"updatedAt\":\"2018-04-30T08:46:26\",\"deleted\":false,\"speed\":1.2070827,\"userName\":\"tester@test.com\"},{\"$id\":\"635\",\"id\":634,\"timestamp\":\"2018-04-30T08:46:36\",\"noiseValue\":52.58027,\"longitude\":8.533278,\"latitude\":52.029236,\"accuracy\":10,\"version\":\"AAAAAAAAB9g=\",\"createdAt\":\"2018-04-30T08:46:36\",\"updatedAt\":\"2018-04-30T08:46:36\",\"deleted\":false,\"speed\":0.8473302,\"userName\":\"tester@test.com\"},{\"$id\":\"636\",\"id\":635,\"timestamp\":\"2018-04-30T08:46:46\",\"noiseValue\":51.338665,\"longitude\":8.533261,\"latitude\":52.029236,\"accuracy\":10,\"version\":\"AAAAAAAAB9g=\",\"createdAt\":\"2018-04-30T08:46:46\",\"updatedAt\":\"2018-04-30T08:46:46\",\"deleted\":false,\"speed\":1.2194068,\"userName\":\"tester@test.com\"},{\"$id\":\"637\",\"id\":636,\"timestamp\":\"2018-04-30T08:46:56\",\"noiseValue\":50.470062,\"longitude\":8.53278,\"latitude\":52.02919,\"accuracy\":10,\"version\":\"AAAAAAAAB9g=\",\"createdAt\":\"2018-04-30T08:46:56\",\"updatedAt\":\"2018-04-30T08:46:56\",\"deleted\":false,\"speed\":0.16137953,\"userName\":\"tester@test.com\"},{\"$id\":\"638\",\"id\":637,\"timestamp\":\"2018-04-30T08:47:06\",\"noiseValue\":75.650894,\"longitude\":8.532687,\"latitude\":52.02917,\"accuracy\":10,\"version\":\"AAAAAAAAB9g=\",\"createdAt\":\"2018-04-30T08:47:06\",\"updatedAt\":\"2018-04-30T08:47:06\",\"deleted\":false,\"speed\":0.11356357,\"userName\":\"tester@test.com\"},{\"$id\":\"639\",\"id\":638,\"timestamp\":\"2018-04-30T08:47:16\",\"noiseValue\":75.88123,\"longitude\":8.532617,\"latitude\":52.029152,\"accuracy\":10,\"version\":\"AAAAAAAAB9g=\",\"createdAt\":\"2018-04-30T08:47:16\",\"updatedAt\":\"2018-04-30T08:47:16\",\"deleted\":false,\"speed\":0.34540874,\"userName\":\"tester@test.com\"},{\"$id\":\"640\",\"id\":639,\"timestamp\":\"2018-04-30T08:47:26\",\"noiseValue\":69.29481,\"longitude\":8.53251,\"latitude\":52.02907,\"accuracy\":10,\"version\":\"AAAAAAAAB9g=\",\"createdAt\":\"2018-04-30T08:47:26\",\"updatedAt\":\"2018-04-30T08:47:26\",\"deleted\":false,\"speed\":0.33216968,\"userName\":\"tester@test.com\"},{\"$id\":\"641\",\"id\":640,\"timestamp\":\"2018-04-30T08:47:36\",\"noiseValue\":68.13644,\"longitude\":8.53246,\"latitude\":52.02906,\"accuracy\":10,\"version\":\"AAAAAAAAB9g=\",\"createdAt\":\"2018-04-30T08:47:36\",\"updatedAt\":\"2018-04-30T08:47:36\",\"deleted\":false,\"speed\":0.3467436,\"userName\":\"tester@test.com\"},{\"$id\":\"642\",\"id\":641,\"timestamp\":\"2018-04-30T08:47:46\",\"noiseValue\":59.974968,\"longitude\":8.532457,\"latitude\":52.029064,\"accuracy\":10,\"version\":\"AAAAAAAAB9g=\",\"createdAt\":\"2018-04-30T08:47:46\",\"updatedAt\":\"2018-04-30T08:47:46\",\"deleted\":false,\"speed\":0.06632591,\"userName\":\"tester@test.com\"},{\"$id\":\"643\",\"id\":642,\"timestamp\":\"2018-04-30T08:47:56\",\"noiseValue\":69.81392,\"longitude\":8.532452,\"latitude\":52.02905,\"accuracy\":10,\"version\":\"AAAAAAAAB9g=\",\"createdAt\":\"2018-04-30T08:47:56\",\"updatedAt\":\"2018-04-30T08:47:56\",\"deleted\":false,\"speed\":0.040433485,\"userName\":\"tester@test.com\"},{\"$id\":\"644\",\"id\":643,\"timestamp\":\"2018-04-30T08:48:06\",\"noiseValue\":75.89888,\"longitude\":8.532447,\"latitude\":52.029037,\"accuracy\":10,\"version\":\"AAAAAAAAB9g=\",\"createdAt\":\"2018-04-30T08:48:06\",\"updatedAt\":\"2018-04-30T08:48:06\",\"deleted\":false,\"speed\":0.040562145,\"userName\":\"tester@test.com\"},{\"$id\":\"645\",\"id\":644,\"timestamp\":\"2018-04-30T08:48:16\",\"noiseValue\":67.20135,\"longitude\":8.532441,\"latitude\":52.029034,\"accuracy\":10,\"version\":\"AAAAAAAAB9g=\",\"createdAt\":\"2018-04-30T08:48:16\",\"updatedAt\":\"2018-04-30T08:48:16\",\"deleted\":false,\"speed\":0.021026332,\"userName\":\"tester@test.com\"},{\"$id\":\"646\",\"id\":645,\"timestamp\":\"2018-04-30T08:48:26\",\"noiseValue\":72.0811,\"longitude\":8.532435,\"latitude\":52.029026,\"accuracy\":10,\"version\":\"AAAAAAAAB9g=\",\"createdAt\":\"2018-04-30T08:48:26\",\"updatedAt\":\"2018-04-30T08:48:26\",\"deleted\":false,\"speed\":0.03208628,\"userName\":\"tester@test.com\"},{\"$id\":\"647\",\"id\":646,\"timestamp\":\"2018-04-30T08:48:36\",\"noiseValue\":60.944096,\"longitude\":8.532431,\"latitude\":52.029022,\"accuracy\":10,\"version\":\"AAAAAAAAB9g=\",\"createdAt\":\"2018-04-30T08:48:36\",\"updatedAt\":\"2018-04-30T08:48:36\",\"deleted\":false,\"speed\":0.015386646,\"userName\":\"tester@test.com\"},{\"$id\":\"648\",\"id\":647,\"timestamp\":\"2018-04-30T08:48:46\",\"noiseValue\":52.925674,\"longitude\":8.532427,\"latitude\":52.029026,\"accuracy\":10,\"version\":\"AAAAAAAAB9g=\",\"createdAt\":\"2018-04-30T08:48:46\",\"updatedAt\":\"2018-04-30T08:48:46\",\"deleted\":false,\"speed\":0.009725144,\"userName\":\"tester@test.com\"},{\"$id\":\"649\",\"id\":648,\"timestamp\":\"2018-04-30T08:48:56\",\"noiseValue\":53.23612,\"longitude\":8.532429,\"latitude\":52.02903,\"accuracy\":10,\"version\":\"AAAAAAAAB9g=\",\"createdAt\":\"2018-04-30T08:48:56\",\"updatedAt\":\"2018-04-30T08:48:56\",\"deleted\":false,\"speed\":0.11735862,\"userName\":\"tester@test.com\"},{\"$id\":\"650\",\"id\":649,\"timestamp\":\"2018-04-30T08:49:06\",\"noiseValue\":55.313694,\"longitude\":8.532437,\"latitude\":52.029034,\"accuracy\":10,\"version\":\"AAAAAAAAB9g=\",\"createdAt\":\"2018-04-30T08:49:06\",\"updatedAt\":\"2018-04-30T08:49:06\",\"deleted\":false,\"speed\":0.051874712,\"userName\":\"tester@test.com\"},{\"$id\":\"651\",\"id\":650,\"timestamp\":\"2018-04-30T08:49:16\",\"noiseValue\":53.692257,\"longitude\":8.532447,\"latitude\":52.029037,\"accuracy\":10,\"version\":\"AAAAAAAAB9g=\",\"createdAt\":\"2018-04-30T08:49:16\",\"updatedAt\":\"2018-04-30T08:49:16\",\"deleted\":false,\"speed\":0.02564357,\"userName\":\"tester@test.com\"},{\"$id\":\"652\",\"id\":651,\"timestamp\":\"2018-04-30T08:49:26\",\"noiseValue\":53.06008,\"longitude\":8.532443,\"latitude\":52.029037,\"accuracy\":10,\"version\":\"AAAAAAAAB9g=\",\"createdAt\":\"2018-04-30T08:49:26\",\"updatedAt\":\"2018-04-30T08:49:26\",\"deleted\":false,\"speed\":0.014578779,\"userName\":\"tester@test.com\"},{\"$id\":\"653\",\"id\":652,\"timestamp\":\"2018-04-30T08:49:36\",\"noiseValue\":58.027363,\"longitude\":8.532438,\"latitude\":52.029037,\"accuracy\":10,\"version\":\"AAAAAAAAB9g=\",\"createdAt\":\"2018-04-30T08:49:36\",\"updatedAt\":\"2018-04-30T08:49:36\",\"deleted\":false,\"speed\":0.048896335,\"userName\":\"tester@test.com\"},{\"$id\":\"654\",\"id\":653,\"timestamp\":\"2018-04-30T08:49:46\",\"noiseValue\":56.999607,\"longitude\":8.532438,\"latitude\":52.029037,\"accuracy\":10,\"version\":\"AAAAAAAAB9g=\",\"createdAt\":\"2018-04-30T08:49:46\",\"updatedAt\":\"2018-04-30T08:49:46\",\"deleted\":false,\"speed\":0.027222909,\"userName\":\"tester@test.com\"},{\"$id\":\"655\",\"id\":654,\"timestamp\":\"2018-04-30T08:49:56\",\"noiseValue\":60.626717,\"longitude\":8.532434,\"latitude\":52.02904,\"accuracy\":10,\"version\":\"AAAAAAAAB9g=\",\"createdAt\":\"2018-04-30T08:49:56\",\"updatedAt\":\"2018-04-30T08:49:56\",\"deleted\":false,\"speed\":0.04271223,\"userName\":\"tester@test.com\"},{\"$id\":\"656\",\"id\":655,\"timestamp\":\"2018-04-30T08:50:06\",\"noiseValue\":57.195118,\"longitude\":8.532433,\"latitude\":52.029045,\"accuracy\":10,\"version\":\"AAAAAAAAB9g=\",\"createdAt\":\"2018-04-30T08:50:06\",\"updatedAt\":\"2018-04-30T08:50:06\",\"deleted\":false,\"speed\":0.027050283,\"userName\":\"tester@test.com\"},{\"$id\":\"657\",\"id\":656,\"timestamp\":\"2018-04-30T08:50:16\",\"noiseValue\":61.50621,\"longitude\":8.532433,\"latitude\":52.02905,\"accuracy\":10,\"version\":\"AAAAAAAAB9g=\",\"createdAt\":\"2018-04-30T08:50:16\",\"updatedAt\":\"2018-04-30T08:50:16\",\"deleted\":false,\"speed\":0.01983684,\"userName\":\"tester@test.com\"},{\"$id\":\"658\",\"id\":657,\"timestamp\":\"2018-04-30T08:50:26\",\"noiseValue\":58.846943,\"longitude\":8.532433,\"latitude\":52.029053,\"accuracy\":10,\"version\":\"AAAAAAAAB9g=\",\"createdAt\":\"2018-04-30T08:50:26\",\"updatedAt\":\"2018-04-30T08:50:26\",\"deleted\":false,\"speed\":0.022968993,\"userName\":\"tester@test.com\"},{\"$id\":\"659\",\"id\":658,\"timestamp\":\"2018-04-30T08:50:36\",\"noiseValue\":63.175926,\"longitude\":8.532434,\"latitude\":52.029053,\"accuracy\":10,\"version\":\"AAAAAAAAB9g=\",\"createdAt\":\"2018-04-30T08:50:36\",\"updatedAt\":\"2018-04-30T08:50:36\",\"deleted\":false,\"speed\":0.015895206,\"userName\":\"tester@test.com\"},{\"$id\":\"660\",\"id\":659,\"timestamp\":\"2018-04-30T15:48:20\",\"noiseValue\":39.962425,\"longitude\":8.907371,\"latitude\":52.29593,\"accuracy\":10,\"version\":\"AAAAAAAAB9g=\",\"createdAt\":\"2018-04-30T15:48:20\",\"updatedAt\":\"2018-04-30T15:48:20\",\"deleted\":false,\"speed\":0.59521824,\"userName\":\"tester@test.com\"},{\"$id\":\"661\",\"id\":660,\"timestamp\":\"2018-04-30T15:48:20\",\"noiseValue\":39.962425,\"longitude\":8.907371,\"latitude\":52.29593,\"accuracy\":10,\"version\":\"AAAAAAAAB9g=\",\"createdAt\":\"2018-04-30T15:48:20\",\"updatedAt\":\"2018-04-30T15:48:20\",\"deleted\":false,\"speed\":0.59521824,\"userName\":\"tester@test.com\"},{\"$id\":\"662\",\"id\":661,\"timestamp\":\"2018-04-30T15:48:30\",\"noiseValue\":44.2984,\"longitude\":8.907547,\"latitude\":52.29619,\"accuracy\":10,\"version\":\"AAAAAAAAB9g=\",\"createdAt\":\"2018-04-30T15:48:30\",\"updatedAt\":\"2018-04-30T15:48:30\",\"deleted\":false,\"speed\":1.3115456,\"userName\":\"tester@test.com\"},{\"$id\":\"663\",\"id\":662,\"timestamp\":\"2018-04-30T15:48:41\",\"noiseValue\":75.89327,\"longitude\":8.907696,\"latitude\":52.296307,\"accuracy\":10,\"version\":\"AAAAAAAAB9g=\",\"createdAt\":\"2018-04-30T15:48:41\",\"updatedAt\":\"2018-04-30T15:48:41\",\"deleted\":false,\"speed\":0.80433244,\"userName\":\"tester@test.com\"},{\"$id\":\"664\",\"id\":663,\"timestamp\":\"2018-04-30T15:49:11\",\"noiseValue\":49.189587,\"longitude\":8.9083185,\"latitude\":52.29597,\"accuracy\":10,\"version\":\"AAAAAAAAB9g=\",\"createdAt\":\"2018-04-30T15:49:11\",\"updatedAt\":\"2018-04-30T15:49:11\",\"deleted\":false,\"speed\":1.3912431,\"userName\":\"tester@test.com\"},{\"$id\":\"665\",\"id\":664,\"timestamp\":\"2018-04-30T15:49:21\",\"noiseValue\":47.509983,\"longitude\":8.908501,\"latitude\":52.295906,\"accuracy\":10,\"version\":\"AAAAAAAAB9g=\",\"createdAt\":\"2018-04-30T15:49:21\",\"updatedAt\":\"2018-04-30T15:49:21\",\"deleted\":false,\"speed\":1.3919339,\"userName\":\"tester@test.com\"},{\"$id\":\"666\",\"id\":665,\"timestamp\":\"2018-04-30T15:49:31\",\"noiseValue\":52.789158,\"longitude\":8.908778,\"latitude\":52.2958,\"accuracy\":10,\"version\":\"AAAAAAAAB9g=\",\"createdAt\":\"2018-04-30T15:49:31\",\"updatedAt\":\"2018-04-30T15:49:31\",\"deleted\":false,\"speed\":1.2649473,\"userName\":\"tester@test.com\"},{\"$id\":\"667\",\"id\":666,\"timestamp\":\"2018-04-30T15:49:41\",\"noiseValue\":48.87145,\"longitude\":8.908904,\"latitude\":52.295704,\"accuracy\":10,\"version\":\"AAAAAAAAB9g=\",\"createdAt\":\"2018-04-30T15:49:41\",\"updatedAt\":\"2018-04-30T15:49:41\",\"deleted\":false,\"speed\":1.6809891,\"userName\":\"tester@test.com\"},{\"$id\":\"668\",\"id\":667,\"timestamp\":\"2018-04-30T15:49:51\",\"noiseValue\":52.257202,\"longitude\":8.909043,\"latitude\":52.295586,\"accuracy\":10,\"version\":\"AAAAAAAAB9g=\",\"createdAt\":\"2018-04-30T15:49:51\",\"updatedAt\":\"2018-04-30T15:49:51\",\"deleted\":false,\"speed\":1.5303522,\"userName\":\"tester@test.com\"},{\"$id\":\"669\",\"id\":668,\"timestamp\":\"2018-04-30T15:50:01\",\"noiseValue\":45.77984,\"longitude\":8.909218,\"latitude\":52.295498,\"accuracy\":10,\"version\":\"AAAAAAAAB9g=\",\"createdAt\":\"2018-04-30T15:50:01\",\"updatedAt\":\"2018-04-30T15:50:01\",\"deleted\":false,\"speed\":1.5695795,\"userName\":\"tester@test.com\"},{\"$id\":\"670\",\"id\":669,\"timestamp\":\"2018-04-30T15:50:11\",\"noiseValue\":57.8531,\"longitude\":8.909397,\"latitude\":52.295433,\"accuracy\":10,\"version\":\"AAAAAAAAB9g=\",\"createdAt\":\"2018-04-30T15:50:11\",\"updatedAt\":\"2018-04-30T15:50:11\",\"deleted\":false,\"speed\":1.5198401,\"userName\":\"tester@test.com\"},{\"$id\":\"671\",\"id\":670,\"timestamp\":\"2018-04-30T15:50:21\",\"noiseValue\":72.62128,\"longitude\":8.909628,\"latitude\":52.29536,\"accuracy\":10,\"version\":\"AAAAAAAAB9g=\",\"createdAt\":\"2018-04-30T15:50:21\",\"updatedAt\":\"2018-04-30T15:50:21\",\"deleted\":false,\"speed\":1.3905458,\"userName\":\"tester@test.com\"},{\"$id\":\"672\",\"id\":671,\"timestamp\":\"2018-04-30T15:50:31\",\"noiseValue\":56.312893,\"longitude\":8.909789,\"latitude\":52.29519,\"accuracy\":10,\"version\":\"AAAAAAAAB9g=\",\"createdAt\":\"2018-04-30T15:50:31\",\"updatedAt\":\"2018-04-30T15:50:31\",\"deleted\":false,\"speed\":1.1386758,\"userName\":\"tester@test.com\"},{\"$id\":\"673\",\"id\":672,\"timestamp\":\"2018-04-30T15:50:41\",\"noiseValue\":73.647705,\"longitude\":8.909963,\"latitude\":52.2951,\"accuracy\":10,\"version\":\"AAAAAAAAB9g=\",\"createdAt\":\"2018-04-30T15:50:41\",\"updatedAt\":\"2018-04-30T15:50:41\",\"deleted\":false,\"speed\":1.1554041,\"userName\":\"tester@test.com\"},{\"$id\":\"674\",\"id\":673,\"timestamp\":\"2018-04-30T15:50:51\",\"noiseValue\":47.551994,\"longitude\":8.910099,\"latitude\":52.295006,\"accuracy\":10,\"version\":\"AAAAAAAAB9g=\",\"createdAt\":\"2018-04-30T15:50:51\",\"updatedAt\":\"2018-04-30T15:50:51\",\"deleted\":false,\"speed\":1.3130677,\"userName\":\"tester@test.com\"},{\"$id\":\"675\",\"id\":674,\"timestamp\":\"2018-04-30T15:51:01\",\"noiseValue\":70.52455,\"longitude\":8.91027,\"latitude\":52.294933,\"accuracy\":10,\"version\":\"AAAAAAAAB9g=\",\"createdAt\":\"2018-04-30T15:51:01\",\"updatedAt\":\"2018-04-30T15:51:01\",\"deleted\":false,\"speed\":1.2457659,\"userName\":\"tester@test.com\"},{\"$id\":\"676\",\"id\":675,\"timestamp\":\"2018-04-30T15:51:11\",\"noiseValue\":60.73074,\"longitude\":8.910483,\"latitude\":52.294834,\"accuracy\":10,\"version\":\"AAAAAAAAB9g=\",\"createdAt\":\"2018-04-30T15:51:11\",\"updatedAt\":\"2018-04-30T15:51:11\",\"deleted\":false,\"speed\":0.8086588,\"userName\":\"tester@test.com\"},{\"$id\":\"677\",\"id\":676,\"timestamp\":\"2018-04-30T15:51:21\",\"noiseValue\":62.890366,\"longitude\":8.910607,\"latitude\":52.294777,\"accuracy\":10,\"version\":\"AAAAAAAAB9g=\",\"createdAt\":\"2018-04-30T15:51:21\",\"updatedAt\":\"2018-04-30T15:51:21\",\"deleted\":false,\"speed\":0.6742648,\"userName\":\"tester@test.com\"},{\"$id\":\"678\",\"id\":677,\"timestamp\":\"2018-04-30T15:51:31\",\"noiseValue\":72.124535,\"longitude\":8.910728,\"latitude\":52.294655,\"accuracy\":10,\"version\":\"AAAAAAAAB9g=\",\"createdAt\":\"2018-04-30T15:51:31\",\"updatedAt\":\"2018-04-30T15:51:31\",\"deleted\":false,\"speed\":0.8353871,\"userName\":\"tester@test.com\"},{\"$id\":\"679\",\"id\":678,\"timestamp\":\"2018-04-30T15:51:41\",\"noiseValue\":74.3637,\"longitude\":8.910908,\"latitude\":52.294556,\"accuracy\":10,\"version\":\"AAAAAAAAB9g=\",\"createdAt\":\"2018-04-30T15:51:41\",\"updatedAt\":\"2018-04-30T15:51:41\",\"deleted\":false,\"speed\":1.2195156,\"userName\":\"tester@test.com\"},{\"$id\":\"680\",\"id\":679,\"timestamp\":\"2018-04-30T15:51:51\",\"noiseValue\":48.256096,\"longitude\":8.911034,\"latitude\":52.29448,\"accuracy\":10,\"version\":\"AAAAAAAAB9g=\",\"createdAt\":\"2018-04-30T15:51:51\",\"updatedAt\":\"2018-04-30T15:51:51\",\"deleted\":false,\"speed\":0.5766562,\"userName\":\"tester@test.com\"},{\"$id\":\"681\",\"id\":680,\"timestamp\":\"2018-04-30T15:52:01\",\"noiseValue\":66.26702,\"longitude\":8.911145,\"latitude\":52.29439,\"accuracy\":10,\"version\":\"AAAAAAAAB9g=\",\"createdAt\":\"2018-04-30T15:52:01\",\"updatedAt\":\"2018-04-30T15:52:01\",\"deleted\":false,\"speed\":1.3547823,\"userName\":\"tester@test.com\"},{\"$id\":\"682\",\"id\":681,\"timestamp\":\"2018-04-30T15:52:11\",\"noiseValue\":64.67708,\"longitude\":8.911276,\"latitude\":52.294193,\"accuracy\":10,\"version\":\"AAAAAAAAB9g=\",\"createdAt\":\"2018-04-30T15:52:11\",\"updatedAt\":\"2018-04-30T15:52:11\",\"deleted\":false,\"speed\":1.4961398,\"userName\":\"tester@test.com\"},{\"$id\":\"683\",\"id\":682,\"timestamp\":\"2018-04-30T15:52:21\",\"noiseValue\":66.11136,\"longitude\":8.911505,\"latitude\":52.29401,\"accuracy\":10,\"version\":\"AAAAAAAAB9g=\",\"createdAt\":\"2018-04-30T15:52:21\",\"updatedAt\":\"2018-04-30T15:52:21\",\"deleted\":false,\"speed\":1.5002241,\"userName\":\"tester@test.com\"},{\"$id\":\"684\",\"id\":683,\"timestamp\":\"2018-04-30T15:52:31\",\"noiseValue\":52.533157,\"longitude\":8.911658,\"latitude\":52.293888,\"accuracy\":10,\"version\":\"AAAAAAAAB9g=\",\"createdAt\":\"2018-04-30T15:52:31\",\"updatedAt\":\"2018-04-30T15:52:31\",\"deleted\":false,\"speed\":1.1967275,\"userName\":\"tester@test.com\"},{\"$id\":\"685\",\"id\":684,\"timestamp\":\"2018-04-30T15:52:41\",\"noiseValue\":55.504505,\"longitude\":8.911762,\"latitude\":52.29379,\"accuracy\":10,\"version\":\"AAAAAAAAB9g=\",\"createdAt\":\"2018-04-30T15:52:41\",\"updatedAt\":\"2018-04-30T15:52:41\",\"deleted\":false,\"speed\":1.2769359,\"userName\":\"tester@test.com\"},{\"$id\":\"686\",\"id\":685,\"timestamp\":\"2018-04-30T15:52:51\",\"noiseValue\":44.0824,\"longitude\":8.911959,\"latitude\":52.293705,\"accuracy\":10,\"version\":\"AAAAAAAAB9g=\",\"createdAt\":\"2018-04-30T15:52:51\",\"updatedAt\":\"2018-04-30T15:52:51\",\"deleted\":false,\"speed\":1.5146025,\"userName\":\"tester@test.com\"},{\"$id\":\"687\",\"id\":686,\"timestamp\":\"2018-04-30T15:53:01\",\"noiseValue\":39.810482,\"longitude\":8.912074,\"latitude\":52.2936,\"accuracy\":10,\"version\":\"AAAAAAAAB9g=\",\"createdAt\":\"2018-04-30T15:53:01\",\"updatedAt\":\"2018-04-30T15:53:01\",\"deleted\":false,\"speed\":1.4593,\"userName\":\"tester@test.com\"},{\"$id\":\"688\",\"id\":687,\"timestamp\":\"2018-04-30T15:53:11\",\"noiseValue\":48.313877,\"longitude\":8.912125,\"latitude\":52.29338,\"accuracy\":10,\"version\":\"AAAAAAAAB9g=\",\"createdAt\":\"2018-04-30T15:53:11\",\"updatedAt\":\"2018-04-30T15:53:11\",\"deleted\":false,\"speed\":0.8402956,\"userName\":\"tester@test.com\"},{\"$id\":\"689\",\"id\":688,\"timestamp\":\"2018-04-30T15:53:21\",\"noiseValue\":55.262093,\"longitude\":8.912128,\"latitude\":52.293335,\"accuracy\":10,\"version\":\"AAAAAAAAB9g=\",\"createdAt\":\"2018-04-30T15:53:21\",\"updatedAt\":\"2018-04-30T15:53:21\",\"deleted\":false,\"speed\":0.4888036,\"userName\":\"tester@test.com\"},{\"$id\":\"690\",\"id\":689,\"timestamp\":\"2018-04-30T15:53:31\",\"noiseValue\":45.11239,\"longitude\":8.9122,\"latitude\":52.2932,\"accuracy\":10,\"version\":\"AAAAAAAAB9g=\",\"createdAt\":\"2018-04-30T15:53:31\",\"updatedAt\":\"2018-04-30T15:53:31\",\"deleted\":false,\"speed\":1.4866493,\"userName\":\"tester@test.com\"},{\"$id\":\"691\",\"id\":690,\"timestamp\":\"2018-04-30T15:53:41\",\"noiseValue\":50.21167,\"longitude\":8.912315,\"latitude\":52.293137,\"accuracy\":10,\"version\":\"AAAAAAAAB9g=\",\"createdAt\":\"2018-04-30T15:53:41\",\"updatedAt\":\"2018-04-30T15:53:41\",\"deleted\":false,\"speed\":1.2145092,\"userName\":\"tester@test.com\"},{\"$id\":\"692\",\"id\":691,\"timestamp\":\"2018-04-30T15:53:51\",\"noiseValue\":53.764297,\"longitude\":8.912496,\"latitude\":52.29314,\"accuracy\":10,\"version\":\"AAAAAAAAB9g=\",\"createdAt\":\"2018-04-30T15:53:51\",\"updatedAt\":\"2018-04-30T15:53:51\",\"deleted\":false,\"speed\":1.0295192,\"userName\":\"tester@test.com\"},{\"$id\":\"693\",\"id\":692,\"timestamp\":\"2018-04-30T15:54:01\",\"noiseValue\":41.850273,\"longitude\":8.912747,\"latitude\":52.293198,\"accuracy\":10,\"version\":\"AAAAAAAAB9g=\",\"createdAt\":\"2018-04-30T15:54:01\",\"updatedAt\":\"2018-04-30T15:54:01\",\"deleted\":false,\"speed\":0.98748183,\"userName\":\"tester@test.com\"},{\"$id\":\"694\",\"id\":693,\"timestamp\":\"2018-04-30T15:54:11\",\"noiseValue\":54.19906,\"longitude\":8.912998,\"latitude\":52.293232,\"accuracy\":10,\"version\":\"AAAAAAAAB9g=\",\"createdAt\":\"2018-04-30T15:54:11\",\"updatedAt\":\"2018-04-30T15:54:11\",\"deleted\":false,\"speed\":1.0199378,\"userName\":\"tester@test.com\"},{\"$id\":\"695\",\"id\":694,\"timestamp\":\"2018-04-30T15:54:21\",\"noiseValue\":42.245155,\"longitude\":8.913205,\"latitude\":52.293247,\"accuracy\":10,\"version\":\"AAAAAAAAB9g=\",\"createdAt\":\"2018-04-30T15:54:21\",\"updatedAt\":\"2018-04-30T15:54:21\",\"deleted\":false,\"speed\":0.95527166,\"userName\":\"tester@test.com\"},{\"$id\":\"696\",\"id\":695,\"timestamp\":\"2018-04-30T15:54:31\",\"noiseValue\":39.41857,\"longitude\":8.913373,\"latitude\":52.2932,\"accuracy\":10,\"version\":\"AAAAAAAAB9g=\",\"createdAt\":\"2018-04-30T15:54:31\",\"updatedAt\":\"2018-04-30T15:54:31\",\"deleted\":false,\"speed\":0.9309675,\"userName\":\"tester@test.com\"},{\"$id\":\"697\",\"id\":696,\"timestamp\":\"2018-04-30T15:54:41\",\"noiseValue\":45.676434,\"longitude\":8.913635,\"latitude\":52.29314,\"accuracy\":10,\"version\":\"AAAAAAAAB9g=\",\"createdAt\":\"2018-04-30T15:54:41\",\"updatedAt\":\"2018-04-30T15:54:41\",\"deleted\":false,\"speed\":1.3813944,\"userName\":\"tester@test.com\"},{\"$id\":\"698\",\"id\":697,\"timestamp\":\"2018-04-30T15:54:51\",\"noiseValue\":46.60068,\"longitude\":8.913951,\"latitude\":52.293083,\"accuracy\":10,\"version\":\"AAAAAAAAB9g=\",\"createdAt\":\"2018-04-30T15:54:51\",\"updatedAt\":\"2018-04-30T15:54:51\",\"deleted\":false,\"speed\":1.0810649,\"userName\":\"tester@test.com\"},{\"$id\":\"699\",\"id\":698,\"timestamp\":\"2018-04-30T15:55:01\",\"noiseValue\":41.308533,\"longitude\":8.914159,\"latitude\":52.293064,\"accuracy\":10,\"version\":\"AAAAAAAAB9g=\",\"createdAt\":\"2018-04-30T15:55:01\",\"updatedAt\":\"2018-04-30T15:55:01\",\"deleted\":false,\"speed\":0.74695677,\"userName\":\"tester@test.com\"},{\"$id\":\"700\",\"id\":699,\"timestamp\":\"2018-04-30T15:55:11\",\"noiseValue\":58.058674,\"longitude\":8.914311,\"latitude\":52.29315,\"accuracy\":10,\"version\":\"AAAAAAAAB9g=\",\"createdAt\":\"2018-04-30T15:55:11\",\"updatedAt\":\"2018-04-30T15:55:11\",\"deleted\":false,\"speed\":1.2249383,\"userName\":\"tester@test.com\"},{\"$id\":\"701\",\"id\":700,\"timestamp\":\"2018-04-30T15:55:21\",\"noiseValue\":44.56845,\"longitude\":8.914484,\"latitude\":52.29321,\"accuracy\":10,\"version\":\"AAAAAAAAB9g=\",\"createdAt\":\"2018-04-30T15:55:21\",\"updatedAt\":\"2018-04-30T15:55:21\",\"deleted\":false,\"speed\":1.1158731,\"userName\":\"tester@test.com\"},{\"$id\":\"702\",\"id\":701,\"timestamp\":\"2018-04-30T15:55:31\",\"noiseValue\":56.30525,\"longitude\":8.914794,\"latitude\":52.2933,\"accuracy\":10,\"version\":\"AAAAAAAAB9g=\",\"createdAt\":\"2018-04-30T15:55:31\",\"updatedAt\":\"2018-04-30T15:55:31\",\"deleted\":false,\"speed\":1.2493252,\"userName\":\"tester@test.com\"},{\"$id\":\"703\",\"id\":702,\"timestamp\":\"2018-04-30T15:55:41\",\"noiseValue\":51.605534,\"longitude\":8.915023,\"latitude\":52.293404,\"accuracy\":10,\"version\":\"AAAAAAAAB9g=\",\"createdAt\":\"2018-04-30T15:55:41\",\"updatedAt\":\"2018-04-30T15:55:41\",\"deleted\":false,\"speed\":1.3500654,\"userName\":\"tester@test.com\"},{\"$id\":\"704\",\"id\":703,\"timestamp\":\"2018-04-30T15:55:51\",\"noiseValue\":45.304504,\"longitude\":8.915223,\"latitude\":52.293476,\"accuracy\":10,\"version\":\"AAAAAAAAB9g=\",\"createdAt\":\"2018-04-30T15:55:51\",\"updatedAt\":\"2018-04-30T15:55:51\",\"deleted\":false,\"speed\":1.3734726,\"userName\":\"tester@test.com\"},{\"$id\":\"705\",\"id\":704,\"timestamp\":\"2018-04-30T15:56:01\",\"noiseValue\":76.38482,\"longitude\":8.915479,\"latitude\":52.293472,\"accuracy\":10,\"version\":\"AAAAAAAAB9g=\",\"createdAt\":\"2018-04-30T15:56:01\",\"updatedAt\":\"2018-04-30T15:56:01\",\"deleted\":false,\"speed\":1.1259358,\"userName\":\"tester@test.com\"},{\"$id\":\"706\",\"id\":705,\"timestamp\":\"2018-04-30T15:56:11\",\"noiseValue\":59.218513,\"longitude\":8.915593,\"latitude\":52.293453,\"accuracy\":10,\"version\":\"AAAAAAAAB9g=\",\"createdAt\":\"2018-04-30T15:56:11\",\"updatedAt\":\"2018-04-30T15:56:11\",\"deleted\":false,\"speed\":0.0015797998,\"userName\":\"tester@test.com\"},{\"$id\":\"707\",\"id\":706,\"timestamp\":\"2018-04-30T15:56:21\",\"noiseValue\":64.32123,\"longitude\":8.915674,\"latitude\":52.29341,\"accuracy\":10,\"version\":\"AAAAAAAAB9g=\",\"createdAt\":\"2018-04-30T15:56:21\",\"updatedAt\":\"2018-04-30T15:56:21\",\"deleted\":false,\"speed\":0.009467507,\"userName\":\"tester@test.com\"},{\"$id\":\"708\",\"id\":707,\"timestamp\":\"2018-04-30T15:56:31\",\"noiseValue\":68.90469,\"longitude\":8.915735,\"latitude\":52.293365,\"accuracy\":10,\"version\":\"AAAAAAAAB9g=\",\"createdAt\":\"2018-04-30T15:56:31\",\"updatedAt\":\"2018-04-30T15:56:31\",\"deleted\":false,\"speed\":0.24318427,\"userName\":\"tester@test.com\"},{\"$id\":\"709\",\"id\":708,\"timestamp\":\"2018-04-30T15:56:41\",\"noiseValue\":48.00119,\"longitude\":8.915806,\"latitude\":52.29337,\"accuracy\":10,\"version\":\"AAAAAAAAB9g=\",\"createdAt\":\"2018-04-30T15:56:41\",\"updatedAt\":\"2018-04-30T15:56:41\",\"deleted\":false,\"speed\":1.4683017,\"userName\":\"tester@test.com\"},{\"$id\":\"710\",\"id\":709,\"timestamp\":\"2018-04-30T15:56:52\",\"noiseValue\":44.597942,\"longitude\":8.916045,\"latitude\":52.293358,\"accuracy\":10,\"version\":\"AAAAAAAAB9g=\",\"createdAt\":\"2018-04-30T15:56:52\",\"updatedAt\":\"2018-04-30T15:56:52\",\"deleted\":false,\"speed\":1.355347,\"userName\":\"tester@test.com\"},{\"$id\":\"711\",\"id\":710,\"timestamp\":\"2018-04-30T15:57:02\",\"noiseValue\":42.283672,\"longitude\":8.916318,\"latitude\":52.293324,\"accuracy\":10,\"version\":\"AAAAAAAAB9g=\",\"createdAt\":\"2018-04-30T15:57:02\",\"updatedAt\":\"2018-04-30T15:57:02\",\"deleted\":false,\"speed\":1.2604659,\"userName\":\"tester@test.com\"},{\"$id\":\"712\",\"id\":711,\"timestamp\":\"2018-04-30T15:57:12\",\"noiseValue\":40.450527,\"longitude\":8.916491,\"latitude\":52.293274,\"accuracy\":10,\"version\":\"AAAAAAAAB9g=\",\"createdAt\":\"2018-04-30T15:57:12\",\"updatedAt\":\"2018-04-30T15:57:12\",\"deleted\":false,\"speed\":1.0167481,\"userName\":\"tester@test.com\"},{\"$id\":\"713\",\"id\":712,\"timestamp\":\"2018-04-30T15:57:22\",\"noiseValue\":67.81006,\"longitude\":8.916727,\"latitude\":52.29319,\"accuracy\":10,\"version\":\"AAAAAAAAB9g=\",\"createdAt\":\"2018-04-30T15:57:22\",\"updatedAt\":\"2018-04-30T15:57:22\",\"deleted\":false,\"speed\":1.0254604,\"userName\":\"tester@test.com\"},{\"$id\":\"714\",\"id\":713,\"timestamp\":\"2018-04-30T15:57:32\",\"noiseValue\":55.08785,\"longitude\":8.91695,\"latitude\":52.293133,\"accuracy\":10,\"version\":\"AAAAAAAAB9g=\",\"createdAt\":\"2018-04-30T15:57:32\",\"updatedAt\":\"2018-04-30T15:57:32\",\"deleted\":false,\"speed\":0.7173117,\"userName\":\"tester@test.com\"},{\"$id\":\"715\",\"id\":714,\"timestamp\":\"2018-04-30T15:57:42\",\"noiseValue\":65.991806,\"longitude\":8.917099,\"latitude\":52.293125,\"accuracy\":10,\"version\":\"AAAAAAAAB9g=\",\"createdAt\":\"2018-04-30T15:57:42\",\"updatedAt\":\"2018-04-30T15:57:42\",\"deleted\":false,\"speed\":1.0296701,\"userName\":\"tester@test.com\"},{\"$id\":\"716\",\"id\":715,\"timestamp\":\"2018-04-30T15:57:52\",\"noiseValue\":57.433475,\"longitude\":8.917362,\"latitude\":52.29308,\"accuracy\":10,\"version\":\"AAAAAAAAB9g=\",\"createdAt\":\"2018-04-30T15:57:52\",\"updatedAt\":\"2018-04-30T15:57:52\",\"deleted\":false,\"speed\":0.7015062,\"userName\":\"tester@test.com\"},{\"$id\":\"717\",\"id\":716,\"timestamp\":\"2018-04-30T15:58:02\",\"noiseValue\":46.62403,\"longitude\":8.917499,\"latitude\":52.293015,\"accuracy\":10,\"version\":\"AAAAAAAAB9g=\",\"createdAt\":\"2018-04-30T15:58:02\",\"updatedAt\":\"2018-04-30T15:58:02\",\"deleted\":false,\"speed\":0.6677246,\"userName\":\"tester@test.com\"},{\"$id\":\"718\",\"id\":717,\"timestamp\":\"2018-04-30T15:58:12\",\"noiseValue\":49.102375,\"longitude\":8.917787,\"latitude\":52.292854,\"accuracy\":10,\"version\":\"AAAAAAAAB9g=\",\"createdAt\":\"2018-04-30T15:58:12\",\"updatedAt\":\"2018-04-30T15:58:12\",\"deleted\":false,\"speed\":0.93915266,\"userName\":\"tester@test.com\"},{\"$id\":\"719\",\"id\":718,\"timestamp\":\"2018-04-30T15:58:22\",\"noiseValue\":43.090538,\"longitude\":8.918087,\"latitude\":52.292763,\"accuracy\":10,\"version\":\"AAAAAAAAB9g=\",\"createdAt\":\"2018-04-30T15:58:22\",\"updatedAt\":\"2018-04-30T15:58:22\",\"deleted\":false,\"speed\":1.1541172,\"userName\":\"tester@test.com\"},{\"$id\":\"720\",\"id\":719,\"timestamp\":\"2018-04-30T15:58:32\",\"noiseValue\":45.676434,\"longitude\":8.918278,\"latitude\":52.29265,\"accuracy\":10,\"version\":\"AAAAAAAAB9g=\",\"createdAt\":\"2018-04-30T15:58:32\",\"updatedAt\":\"2018-04-30T15:58:32\",\"deleted\":false,\"speed\":1.0755825,\"userName\":\"tester@test.com\"},{\"$id\":\"721\",\"id\":720,\"timestamp\":\"2018-04-30T15:58:42\",\"noiseValue\":47.122444,\"longitude\":8.918396,\"latitude\":52.292553,\"accuracy\":10,\"version\":\"AAAAAAAAB9g=\",\"createdAt\":\"2018-04-30T15:58:42\",\"updatedAt\":\"2018-04-30T15:58:42\",\"deleted\":false,\"speed\":0.46311352,\"userName\":\"tester@test.com\"},{\"$id\":\"722\",\"id\":721,\"timestamp\":\"2018-04-30T15:58:52\",\"noiseValue\":45.869316,\"longitude\":8.918651,\"latitude\":52.29251,\"accuracy\":10,\"version\":\"AAAAAAAAB9g=\",\"createdAt\":\"2018-04-30T15:58:52\",\"updatedAt\":\"2018-04-30T15:58:52\",\"deleted\":false,\"speed\":0.8065611,\"userName\":\"tester@test.com\"},{\"$id\":\"723\",\"id\":722,\"timestamp\":\"2018-04-30T15:59:02\",\"noiseValue\":49.08483,\"longitude\":8.9189205,\"latitude\":52.29246,\"accuracy\":10,\"version\":\"AAAAAAAAB9g=\",\"createdAt\":\"2018-04-30T15:59:02\",\"updatedAt\":\"2018-04-30T15:59:02\",\"deleted\":false,\"speed\":0.938681,\"userName\":\"tester@test.com\"},{\"$id\":\"724\",\"id\":723,\"timestamp\":\"2018-04-30T15:59:12\",\"noiseValue\":52.171444,\"longitude\":8.91912,\"latitude\":52.292496,\"accuracy\":10,\"version\":\"AAAAAAAAB9g=\",\"createdAt\":\"2018-04-30T15:59:12\",\"updatedAt\":\"2018-04-30T15:59:12\",\"deleted\":false,\"speed\":0.9744728,\"userName\":\"tester@test.com\"},{\"$id\":\"725\",\"id\":724,\"timestamp\":\"2018-04-30T15:59:22\",\"noiseValue\":70.964165,\"longitude\":8.919315,\"latitude\":52.292393,\"accuracy\":10,\"version\":\"AAAAAAAAB9g=\",\"createdAt\":\"2018-04-30T15:59:22\",\"updatedAt\":\"2018-04-30T15:59:22\",\"deleted\":false,\"speed\":1.201608,\"userName\":\"tester@test.com\"},{\"$id\":\"726\",\"id\":725,\"timestamp\":\"2018-04-30T15:59:32\",\"noiseValue\":60.785713,\"longitude\":8.919652,\"latitude\":52.29232,\"accuracy\":10,\"version\":\"AAAAAAAAB9g=\",\"createdAt\":\"2018-04-30T15:59:32\",\"updatedAt\":\"2018-04-30T15:59:32\",\"deleted\":false,\"speed\":1.3603225,\"userName\":\"tester@test.com\"},{\"$id\":\"727\",\"id\":726,\"timestamp\":\"2018-04-30T15:59:42\",\"noiseValue\":56.605957,\"longitude\":8.919934,\"latitude\":52.29222,\"accuracy\":10,\"version\":\"AAAAAAAAB9g=\",\"createdAt\":\"2018-04-30T15:59:42\",\"updatedAt\":\"2018-04-30T15:59:42\",\"deleted\":false,\"speed\":1.1523194,\"userName\":\"tester@test.com\"},{\"$id\":\"728\",\"id\":727,\"timestamp\":\"2018-04-30T15:59:52\",\"noiseValue\":52.948223,\"longitude\":8.920308,\"latitude\":52.292088,\"accuracy\":10,\"version\":\"AAAAAAAAB9g=\",\"createdAt\":\"2018-04-30T15:59:52\",\"updatedAt\":\"2018-04-30T15:59:52\",\"deleted\":false,\"speed\":1.2716646,\"userName\":\"tester@test.com\"},{\"$id\":\"729\",\"id\":728,\"timestamp\":\"2018-04-30T16:00:02\",\"noiseValue\":55.604584,\"longitude\":8.920589,\"latitude\":52.29201,\"accuracy\":10,\"version\":\"AAAAAAAAB9g=\",\"createdAt\":\"2018-04-30T16:00:02\",\"updatedAt\":\"2018-04-30T16:00:02\",\"deleted\":false,\"speed\":1.4309258,\"userName\":\"tester@test.com\"},{\"$id\":\"730\",\"id\":729,\"timestamp\":\"2018-04-30T16:00:12\",\"noiseValue\":54.910034,\"longitude\":8.920854,\"latitude\":52.291954,\"accuracy\":10,\"version\":\"AAAAAAAAB9g=\",\"createdAt\":\"2018-04-30T16:00:12\",\"updatedAt\":\"2018-04-30T16:00:12\",\"deleted\":false,\"speed\":1.4492302,\"userName\":\"tester@test.com\"},{\"$id\":\"731\",\"id\":730,\"timestamp\":\"2018-04-30T16:00:22\",\"noiseValue\":77.62613,\"longitude\":8.9211235,\"latitude\":52.291912,\"accuracy\":10,\"version\":\"AAAAAAAAB9g=\",\"createdAt\":\"2018-04-30T16:00:22\",\"updatedAt\":\"2018-04-30T16:00:22\",\"deleted\":false,\"speed\":1.4644505,\"userName\":\"tester@test.com\"},{\"$id\":\"732\",\"id\":731,\"timestamp\":\"2018-04-30T16:00:32\",\"noiseValue\":71.323746,\"longitude\":8.921341,\"latitude\":52.29187,\"accuracy\":10,\"version\":\"AAAAAAAAB9g=\",\"createdAt\":\"2018-04-30T16:00:32\",\"updatedAt\":\"2018-04-30T16:00:32\",\"deleted\":false,\"speed\":1.3346686,\"userName\":\"tester@test.com\"},{\"$id\":\"733\",\"id\":732,\"timestamp\":\"2018-04-30T16:00:42\",\"noiseValue\":68.621254,\"longitude\":8.921593,\"latitude\":52.29179,\"accuracy\":10,\"version\":\"AAAAAAAAB9g=\",\"createdAt\":\"2018-04-30T16:00:42\",\"updatedAt\":\"2018-04-30T16:00:42\",\"deleted\":false,\"speed\":0.89938396,\"userName\":\"tester@test.com\"},{\"$id\":\"734\",\"id\":733,\"timestamp\":\"2018-04-30T16:00:52\",\"noiseValue\":55.504505,\"longitude\":8.921796,\"latitude\":52.291744,\"accuracy\":10,\"version\":\"AAAAAAAAB9g=\",\"createdAt\":\"2018-04-30T16:00:52\",\"updatedAt\":\"2018-04-30T16:00:52\",\"deleted\":false,\"speed\":1.2091424,\"userName\":\"tester@test.com\"},{\"$id\":\"735\",\"id\":734,\"timestamp\":\"2018-04-30T16:01:02\",\"noiseValue\":56.06482,\"longitude\":8.921973,\"latitude\":52.291702,\"accuracy\":10,\"version\":\"AAAAAAAAB9g=\",\"createdAt\":\"2018-04-30T16:01:02\",\"updatedAt\":\"2018-04-30T16:01:02\",\"deleted\":false,\"speed\":1.2521402,\"userName\":\"tester@test.com\"},{\"$id\":\"736\",\"id\":735,\"timestamp\":\"2018-04-30T16:01:12\",\"noiseValue\":69.74012,\"longitude\":8.922106,\"latitude\":52.29163,\"accuracy\":10,\"version\":\"AAAAAAAAB9g=\",\"createdAt\":\"2018-04-30T16:01:12\",\"updatedAt\":\"2018-04-30T16:01:12\",\"deleted\":false,\"speed\":1.2827749,\"userName\":\"tester@test.com\"},{\"$id\":\"737\",\"id\":736,\"timestamp\":\"2018-04-30T16:01:22\",\"noiseValue\":62.409584,\"longitude\":8.922257,\"latitude\":52.29152,\"accuracy\":10,\"version\":\"AAAAAAAAB9g=\",\"createdAt\":\"2018-04-30T16:01:22\",\"updatedAt\":\"2018-04-30T16:01:22\",\"deleted\":false,\"speed\":0.51306826,\"userName\":\"tester@test.com\"},{\"$id\":\"738\",\"id\":737,\"timestamp\":\"2018-04-30T16:01:32\",\"noiseValue\":61.527203,\"longitude\":8.922276,\"latitude\":52.291508,\"accuracy\":10,\"version\":\"AAAAAAAAB9g=\",\"createdAt\":\"2018-04-30T16:01:32\",\"updatedAt\":\"2018-04-30T16:01:32\",\"deleted\":false,\"speed\":0.3204501,\"userName\":\"tester@test.com\"},{\"$id\":\"739\",\"id\":738,\"timestamp\":\"2018-04-30T16:01:42\",\"noiseValue\":58.13956,\"longitude\":8.922275,\"latitude\":52.291504,\"accuracy\":10,\"version\":\"AAAAAAAAB9g=\",\"createdAt\":\"2018-04-30T16:01:42\",\"updatedAt\":\"2018-04-30T16:01:42\",\"deleted\":false,\"speed\":0.03410337,\"userName\":\"tester@test.com\"},{\"$id\":\"740\",\"id\":739,\"timestamp\":\"2018-04-30T16:01:52\",\"noiseValue\":68.115845,\"longitude\":8.922327,\"latitude\":52.29147,\"accuracy\":10,\"version\":\"AAAAAAAAB9g=\",\"createdAt\":\"2018-04-30T16:01:52\",\"updatedAt\":\"2018-04-30T16:01:52\",\"deleted\":false,\"speed\":1.416796,\"userName\":\"tester@test.com\"},{\"$id\":\"741\",\"id\":740,\"timestamp\":\"2018-04-30T16:02:02\",\"noiseValue\":58.574398,\"longitude\":8.922457,\"latitude\":52.291363,\"accuracy\":10,\"version\":\"AAAAAAAAB9g=\",\"createdAt\":\"2018-04-30T16:02:02\",\"updatedAt\":\"2018-04-30T16:02:02\",\"deleted\":false,\"speed\":1.4358958,\"userName\":\"tester@test.com\"},{\"$id\":\"742\",\"id\":741,\"timestamp\":\"2018-04-30T16:07:02\",\"noiseValue\":66.1975,\"longitude\":8.927858,\"latitude\":52.28972,\"accuracy\":10,\"version\":\"AAAAAAAAB9g=\",\"createdAt\":\"2018-04-30T16:07:02\",\"updatedAt\":\"2018-04-30T16:07:02\",\"deleted\":false,\"speed\":1.3603766,\"userName\":\"tester@test.com\"},{\"$id\":\"743\",\"id\":742,\"timestamp\":\"2018-04-30T16:12:33\",\"noiseValue\":39.91207,\"longitude\":8.934178,\"latitude\":52.290516,\"accuracy\":10,\"version\":\"AAAAAAAAB9g=\",\"createdAt\":\"2018-04-30T16:12:33\",\"updatedAt\":\"2018-04-30T16:12:33\",\"deleted\":false,\"speed\":1.33676,\"userName\":\"tester@test.com\"},{\"$id\":\"744\",\"id\":743,\"timestamp\":\"2018-04-30T22:10:11\",\"noiseValue\":60.61042,\"longitude\":8.540726,\"latitude\":52.039394,\"accuracy\":10,\"version\":\"AAAAAAAAB9g=\",\"createdAt\":\"2018-04-30T22:10:11\",\"updatedAt\":\"2018-04-30T22:10:11\",\"deleted\":false,\"speed\":0,\"userName\":\"tester@test.com\"},{\"$id\":\"745\",\"id\":744,\"timestamp\":\"2018-04-30T22:10:11\",\"noiseValue\":60.61042,\"longitude\":8.540726,\"latitude\":52.039394,\"accuracy\":10,\"version\":\"AAAAAAAAB9g=\",\"createdAt\":\"2018-04-30T22:10:11\",\"updatedAt\":\"2018-04-30T22:10:11\",\"deleted\":false,\"speed\":0,\"userName\":\"tester@test.com\"},{\"$id\":\"746\",\"id\":745,\"timestamp\":\"2018-05-01T02:49:10\",\"noiseValue\":71.28293,\"longitude\":8.53681,\"latitude\":52.033947,\"accuracy\":10,\"version\":\"AAAAAAAAB9g=\",\"createdAt\":\"2018-05-01T02:49:10\",\"updatedAt\":\"2018-05-01T02:49:10\",\"deleted\":false,\"speed\":1.427015,\"userName\":\"tester@test.com\"},{\"$id\":\"747\",\"id\":746,\"timestamp\":\"2018-05-01T02:49:10\",\"noiseValue\":71.28293,\"longitude\":8.53681,\"latitude\":52.033947,\"accuracy\":10,\"version\":\"AAAAAAAAB9g=\",\"createdAt\":\"2018-05-01T02:49:10\",\"updatedAt\":\"2018-05-01T02:49:10\",\"deleted\":false,\"speed\":1.427015,\"userName\":\"tester@test.com\"},{\"$id\":\"748\",\"id\":747,\"timestamp\":\"2018-05-01T02:49:21\",\"noiseValue\":62.46057,\"longitude\":8.536618,\"latitude\":52.033993,\"accuracy\":10,\"version\":\"AAAAAAAAB9g=\",\"createdAt\":\"2018-05-01T02:49:21\",\"updatedAt\":\"2018-05-01T02:49:21\",\"deleted\":false,\"speed\":1.4117141,\"userName\":\"tester@test.com\"},{\"$id\":\"749\",\"id\":748,\"timestamp\":\"2018-05-01T02:49:31\",\"noiseValue\":63.055443,\"longitude\":8.536365,\"latitude\":52.03404,\"accuracy\":10,\"version\":\"AAAAAAAAB9g=\",\"createdAt\":\"2018-05-01T02:49:31\",\"updatedAt\":\"2018-05-01T02:49:31\",\"deleted\":false,\"speed\":1.4355117,\"userName\":\"tester@test.com\"},{\"$id\":\"750\",\"id\":749,\"timestamp\":\"2018-05-01T02:49:41\",\"noiseValue\":74.49047,\"longitude\":8.53609,\"latitude\":52.034065,\"accuracy\":10,\"version\":\"AAAAAAAAB9g=\",\"createdAt\":\"2018-05-01T02:49:41\",\"updatedAt\":\"2018-05-01T02:49:41\",\"deleted\":false,\"speed\":1.7243408,\"userName\":\"tester@test.com\"},{\"$id\":\"751\",\"id\":750,\"timestamp\":\"2018-05-01T02:49:51\",\"noiseValue\":78.56438,\"longitude\":8.5358095,\"latitude\":52.03408,\"accuracy\":10,\"version\":\"AAAAAAAAB9g=\",\"createdAt\":\"2018-05-01T02:49:51\",\"updatedAt\":\"2018-05-01T02:49:51\",\"deleted\":false,\"speed\":1.6998845,\"userName\":\"tester@test.com\"},{\"$id\":\"752\",\"id\":751,\"timestamp\":\"2018-05-01T02:50:01\",\"noiseValue\":75.7981,\"longitude\":8.535567,\"latitude\":52.034107,\"accuracy\":10,\"version\":\"AAAAAAAAB9g=\",\"createdAt\":\"2018-05-01T02:50:01\",\"updatedAt\":\"2018-05-01T02:50:01\",\"deleted\":false,\"speed\":1.7247298,\"userName\":\"tester@test.com\"},{\"$id\":\"753\",\"id\":752,\"timestamp\":\"2018-05-01T02:50:06\",\"noiseValue\":75.85388,\"longitude\":8.535438,\"latitude\":52.03412,\"accuracy\":10,\"version\":\"AAAAAAAAB9g=\",\"createdAt\":\"2018-05-01T02:50:06\",\"updatedAt\":\"2018-05-01T02:50:06\",\"deleted\":false,\"speed\":1.7285043,\"userName\":\"tester@test.com\"},{\"$id\":\"754\",\"id\":753,\"timestamp\":\"2018-05-01T02:50:06\",\"noiseValue\":78.91518,\"longitude\":8.535438,\"latitude\":52.03412,\"accuracy\":10,\"version\":\"AAAAAAAAB9g=\",\"createdAt\":\"2018-05-01T02:50:06\",\"updatedAt\":\"2018-05-01T02:50:06\",\"deleted\":false,\"speed\":1.7285043,\"userName\":\"tester@test.com\"},{\"$id\":\"755\",\"id\":754,\"timestamp\":\"2018-05-01T02:50:11\",\"noiseValue\":78.68561,\"longitude\":8.535317,\"latitude\":52.034126,\"accuracy\":10,\"version\":\"AAAAAAAAB9g=\",\"createdAt\":\"2018-05-01T02:50:11\",\"updatedAt\":\"2018-05-01T02:50:11\",\"deleted\":false,\"speed\":1.7575576,\"userName\":\"tester@test.com\"},{\"$id\":\"756\",\"id\":755,\"timestamp\":\"2018-05-01T14:08:40\",\"noiseValue\":79.48964,\"longitude\":-122.084,\"latitude\":37.421997,\"accuracy\":20,\"version\":\"AAAAAAAAB9g=\",\"createdAt\":\"2018-05-01T14:08:40\",\"updatedAt\":\"2018-05-01T14:08:40\",\"deleted\":false,\"speed\":0,\"userName\":\"tester@test.com\"},{\"$id\":\"757\",\"id\":756,\"timestamp\":\"2018-05-01T14:08:40\",\"noiseValue\":79.48964,\"longitude\":-122.084,\"latitude\":37.421997,\"accuracy\":20,\"version\":\"AAAAAAAAB9g=\",\"createdAt\":\"2018-05-01T14:08:40\",\"updatedAt\":\"2018-05-01T14:08:40\",\"deleted\":false,\"speed\":0,\"userName\":\"tester@test.com\"},{\"$id\":\"758\",\"id\":757,\"timestamp\":\"2018-05-01T14:08:50\",\"noiseValue\":10.764528,\"longitude\":-122.084,\"latitude\":37.421997,\"accuracy\":20,\"version\":\"AAAAAAAAB9g=\",\"createdAt\":\"2018-05-01T14:08:50\",\"updatedAt\":\"2018-05-01T14:08:50\",\"deleted\":false,\"speed\":0,\"userName\":\"tester@test.com\"},{\"$id\":\"759\",\"id\":758,\"timestamp\":\"2018-05-01T14:09:00\",\"noiseValue\":12.702729,\"longitude\":-122.084,\"latitude\":37.421997,\"accuracy\":20,\"version\":\"AAAAAAAAB9g=\",\"createdAt\":\"2018-05-01T14:09:00\",\"updatedAt\":\"2018-05-01T14:09:00\",\"deleted\":false,\"speed\":0,\"userName\":\"tester@test.com\"},{\"$id\":\"760\",\"id\":759,\"timestamp\":\"2018-05-01T14:09:10\",\"noiseValue\":12.103464,\"longitude\":-122.084,\"latitude\":37.421997,\"accuracy\":20,\"version\":\"AAAAAAAAB9g=\",\"createdAt\":\"2018-05-01T14:09:10\",\"updatedAt\":\"2018-05-01T14:09:10\",\"deleted\":false,\"speed\":0,\"userName\":\"tester@test.com\"},{\"$id\":\"761\",\"id\":760,\"timestamp\":\"2018-05-01T14:09:20\",\"noiseValue\":12.103464,\"longitude\":-122.084,\"latitude\":37.421997,\"accuracy\":20,\"version\":\"AAAAAAAAB9g=\",\"createdAt\":\"2018-05-01T14:09:20\",\"updatedAt\":\"2018-05-01T14:09:20\",\"deleted\":false,\"speed\":0,\"userName\":\"tester@test.com\"},{\"$id\":\"762\",\"id\":761,\"timestamp\":\"2018-05-01T14:09:30\",\"noiseValue\":13.263304,\"longitude\":-122.084,\"latitude\":37.421997,\"accuracy\":20,\"version\":\"AAAAAAAAB9g=\",\"createdAt\":\"2018-05-01T14:09:30\",\"updatedAt\":\"2018-05-01T14:09:30\",\"deleted\":false,\"speed\":0,\"userName\":\"tester@test.com\"},{\"$id\":\"763\",\"id\":762,\"timestamp\":\"2018-05-01T14:09:40\",\"noiseValue\":12.103464,\"longitude\":-122.084,\"latitude\":37.421997,\"accuracy\":20,\"version\":\"AAAAAAAAB9g=\",\"createdAt\":\"2018-05-01T14:09:40\",\"updatedAt\":\"2018-05-01T14:09:40\",\"deleted\":false,\"speed\":0,\"userName\":\"tester@test.com\"},{\"$id\":\"764\",\"id\":763,\"timestamp\":\"2018-05-01T14:09:50\",\"noiseValue\":10.764528,\"longitude\":-122.084,\"latitude\":37.421997,\"accuracy\":20,\"version\":\"AAAAAAAAB9g=\",\"createdAt\":\"2018-05-01T14:09:50\",\"updatedAt\":\"2018-05-01T14:09:50\",\"deleted\":false,\"speed\":0,\"userName\":\"tester@test.com\"},{\"$id\":\"765\",\"id\":764,\"timestamp\":\"2018-05-01T14:10:00\",\"noiseValue\":11.459771,\"longitude\":-122.084,\"latitude\":37.421997,\"accuracy\":20,\"version\":\"AAAAAAAAB9g=\",\"createdAt\":\"2018-05-01T14:10:00\",\"updatedAt\":\"2018-05-01T14:10:00\",\"deleted\":false,\"speed\":0,\"userName\":\"tester@test.com\"},{\"$id\":\"766\",\"id\":765,\"timestamp\":\"2018-05-01T14:10:10\",\"noiseValue\":12.702729,\"longitude\":-122.084,\"latitude\":37.421997,\"accuracy\":20,\"version\":\"AAAAAAAAB9g=\",\"createdAt\":\"2018-05-01T14:10:10\",\"updatedAt\":\"2018-05-01T14:10:10\",\"deleted\":false,\"speed\":0,\"userName\":\"tester@test.com\"},{\"$id\":\"767\",\"id\":766,\"timestamp\":\"2018-05-01T14:10:20\",\"noiseValue\":12.103464,\"longitude\":-122.084,\"latitude\":37.421997,\"accuracy\":20,\"version\":\"AAAAAAAAB9g=\",\"createdAt\":\"2018-05-01T14:10:20\",\"updatedAt\":\"2018-05-01T14:10:20\",\"deleted\":false,\"speed\":0,\"userName\":\"tester@test.com\"},{\"$id\":\"768\",\"id\":767,\"timestamp\":\"2018-05-01T14:10:30\",\"noiseValue\":12.103464,\"longitude\":-122.084,\"latitude\":37.421997,\"accuracy\":20,\"version\":\"AAAAAAAAB9g=\",\"createdAt\":\"2018-05-01T14:10:30\",\"updatedAt\":\"2018-05-01T14:10:30\",\"deleted\":false,\"speed\":0,\"userName\":\"tester@test.com\"},{\"$id\":\"769\",\"id\":768,\"timestamp\":\"2018-05-01T14:10:40\",\"noiseValue\":13.263304,\"longitude\":-122.084,\"latitude\":37.421997,\"accuracy\":20,\"version\":\"AAAAAAAAB9g=\",\"createdAt\":\"2018-05-01T14:10:40\",\"updatedAt\":\"2018-05-01T14:10:40\",\"deleted\":false,\"speed\":0,\"userName\":\"tester@test.com\"},{\"$id\":\"770\",\"id\":769,\"timestamp\":\"2018-05-01T14:10:50\",\"noiseValue\":11.459771,\"longitude\":-122.084,\"latitude\":37.421997,\"accuracy\":20,\"version\":\"AAAAAAAAB9g=\",\"createdAt\":\"2018-05-01T14:10:50\",\"updatedAt\":\"2018-05-01T14:10:50\",\"deleted\":false,\"speed\":0,\"userName\":\"tester@test.com\"}]";

    private List<Sample> samples = new ArrayList<>();
    private List<Polygon> polygons = new ArrayList<Polygon>();

    private Switch switchGrid;
    private SeekBar seekbarAlpha;
    private List<List<List<Double>>> noiseMatrix = new ArrayList<>();

    private enum MapOverlayType {
        OVERLAY_TILES,
        OVERLAY_HEATMAP
    }

    // used to stop the animation of a clicked polygon when another polygon has been clicked
    private Polygon lastClickedPolygon ;
    private void setLastClickedPolygon(Polygon poly) {
        lastClickedPolygon = poly;
    };
    private Polygon getLastClickedPolygon() {
        return lastClickedPolygon;
    }

    private MapOverlayType overlayType = MapOverlayType.OVERLAY_TILES;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
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
                refresh();
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
                setGridVisible(showGrid);
            }
        });
        seekbarAlpha.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                double a = progress/100.d;
                setPolygonAlpha(a);
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
                map.setMapType((int)id+1);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
        //TODO
        JSONArray responseArray;
        try {
            responseArray = new JSONArray(testResponse);
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

    private void setGridVisible(boolean showGrid) {
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

    private void setPolygonAlpha(double alpha) {
        for(Polygon poly : polygons) {
            int curColor = poly.getFillColor();
            if(curColor != 0) {
                curColor = curColor & 0x00ffffff;

                poly.setFillColor((int)(alpha * 255) << 24 | curColor);
            }

        }
    }

    private void toggleMapOverlay() {
        if(overlayType == MapOverlayType.OVERLAY_HEATMAP) {
            overlayType = MapOverlayType.OVERLAY_TILES;
            switchGrid.setVisibility(View.VISIBLE);
            seekbarAlpha.setVisibility(View.VISIBLE);
        } else {
            overlayType = MapOverlayType.OVERLAY_HEATMAP;
            switchGrid.setVisibility(View.INVISIBLE);
            seekbarAlpha.setVisibility(View.INVISIBLE);
        }
        refresh();
    }

    private void refresh() {
        map.clear();
        noiseMatrix.clear();
        polygons.clear();
        LatLng northEastVisible = map.getProjection().getVisibleRegion().latLngBounds.northeast;
        LatLng southWestVisible = map.getProjection().getVisibleRegion().latLngBounds.southwest;
        LatLng northWestVisible = new LatLng(northEastVisible.latitude,southWestVisible.longitude);
        double NUM_OF_RECTS_WIDTH = 20;

        double radius = SphericalUtil.computeDistanceBetween(northWestVisible,northEastVisible) / NUM_OF_RECTS_WIDTH / 2;
        double numOfRectsHeight =SphericalUtil.computeDistanceBetween(northWestVisible, southWestVisible) / radius / 2;
        //map.addMarker(new MarkerOptions().position(start));
        LatLng start = SphericalUtil.computeOffset(northWestVisible, radius * Math.sqrt(2), DIRECTION_SOUTHEAST);

        // init matrix
        for(int i = 0; i < numOfRectsHeight; i++) {
            List<List<Double>> row = new ArrayList<>();
            for(int j = 0; j < NUM_OF_RECTS_WIDTH;j++) {
                List<Double> column = new ArrayList<>();
                row.add(column);
            }
            noiseMatrix.add(row);
        }

        //map.addMarker(new MarkerOptions().position(start));
        double offsetLong =  2 * (start.longitude - northWestVisible.longitude);
        double offsetLat =  2 * (start.latitude - northWestVisible.latitude);

        // cluster the samples samples
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



        double lat1 = 52.0382444;
        double long1 = 8.5257916;
        Polygon poly = null;

        LatLng bielefeld1 = new LatLng(lat1, long1);

        LatLng bielefeld2 = new LatLng(52.0392444, 8.5257916);

        Collection<WeightedLatLng> weightedSamples = new ArrayList<>();

        boolean showGrid = switchGrid.isChecked();
        for(int heightCounter = 0; heightCounter < numOfRectsHeight; heightCounter++) {
            for(int widthCounter = 0; widthCounter < NUM_OF_RECTS_WIDTH; widthCounter++) {

                LatLng center = new LatLng(start.latitude + heightCounter*offsetLat,start.longitude+widthCounter*offsetLong);

                LatLng targetNorthEast = SphericalUtil.computeOffset(center, radius * Math.sqrt(2), DIRECTION_NORTHEAST);
                LatLng targetNorthWest = SphericalUtil.computeOffset(center, radius * Math.sqrt(2), DIRECTION_SOUTHEAST);
                LatLng targetSouthWest = SphericalUtil.computeOffset(center, radius * Math.sqrt(2), DIRECTION_SOUTHWEST);
                LatLng targetSouthEast = SphericalUtil.computeOffset(center, radius * Math.sqrt(2), DIRECTION_NORTHWEST);

                double sum = 0.0;
                List<Double> samplesInArea = noiseMatrix.get(heightCounter).get(widthCounter);
                for (double curValue : samplesInArea) {
                    sum += curValue;
                }

                double meanNoise = sum/samplesInArea.size();
                int fillColor = 0;

                //TODO customizable range
                double normalizedNoise = (meanNoise-30)*75/ 25.0d / 100.0d;
                if(normalizedNoise > 1) {
                    normalizedNoise = 1;
                }
                if(meanNoise > 0.0) {

                    fillColor = getArgbColor(normalizedNoise, seekbarAlpha.getProgress()/100.0d);
                    weightedSamples.add(new WeightedLatLng(center, normalizedNoise));
                }
                if(overlayType == MapOverlayType.OVERLAY_TILES) {
                    PolygonOptions rectOptions = new PolygonOptions()
                            .add(targetSouthWest)
                            .add(targetSouthEast)
                            .add(targetNorthEast)
                            .add(targetNorthWest)
                            .fillColor(fillColor)
                            .strokeWidth(0f);
                    if(showGrid) {
                        rectOptions.strokeWidth(1.0f);
                    }
                    poly = map.addPolygon(rectOptions);
                    if(meanNoise > 0d) {
                        poly.setTag(String.format("%.2f",  meanNoise) + " db(A)");
                        poly.setClickable(true);
                    } else {
                        if(!showGrid) {
                            poly.setVisible(false);
                        }
                    }
                    polygons.add(poly);

                    map.setOnPolygonClickListener(new GoogleMap.OnPolygonClickListener() {
                        public void onPolygonClick(final Polygon polygon) {
                            String displayText = "" + polygon.getTag();
                            Toast.makeText(MapsActivity.this,
                                    displayText,
                                    Toast.LENGTH_LONG)
                                    .show();

                            MapsActivity.this.setLastClickedPolygon(polygon);
                            // add animation (show border)
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

                                    boolean hasAnotherPolygonBeenClicked = MapsActivity.this.getLastClickedPolygon() != polygon;

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
                    });
                }

            }
        }


        if(overlayType == MapOverlayType.OVERLAY_HEATMAP) {
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

    private int getArgbColor(double normalizedValue, double alpha) {
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
        refresh();

    }

    @Override
    public void onMapLoaded() {

    }

    @Override
    public void onCameraIdle() {

    }
}

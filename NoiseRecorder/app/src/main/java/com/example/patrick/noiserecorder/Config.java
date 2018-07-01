package com.example.patrick.noiserecorder;

public class Config {
    public static final String HOST_BASE_URL = "http://noisemaprestapi.azurewebsites.net/";
    public static final String API_BASE_URL = HOST_BASE_URL + "api/";
    public static final double MAX_DB_NORMALIZED = 80.0d;
    public static final double MIN_DB_NORMALIZED = 45.0d;
    public static final int NUMBER_OF_TILES_WIDTH = 20;
}

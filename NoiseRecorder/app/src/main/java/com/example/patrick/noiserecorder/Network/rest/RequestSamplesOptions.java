 package com.example.patrick.noiserecorder.network.rest;

 public class RequestSamplesOptions {
     public RequestSamplesOptions(double longitudeStart, double longitudeEnd, double latitudeStart, double latitudeEnd)
        {
            LongitudeStart = longitudeStart;
            LongitudeEnd = longitudeEnd;
            LatitudeStart = latitudeStart;
            LatitudeEnd = latitudeEnd;
        }
        public double LongitudeStart;
        public double LongitudeEnd;
        public double LatitudeStart;
        public double LatitudeEnd;

        public String toJSONString() {
            return "{\"longitudeStart\": " + LongitudeStart + ","
                    + "\"longitudeEnd\": " + LongitudeEnd + ","
                    + "\"latitudeStart\": " + LatitudeStart + ","
                    + "\"latitudeEnd\": " + LatitudeEnd + "}";
        }
    }
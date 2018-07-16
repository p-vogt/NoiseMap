 package com.iot.noisemap.noiserecorder.network.rest;

 import com.iot.noisemap.noiserecorder.noisemap.HeatMap.TimePoint;

 public class RequestSamplesOptions {
     public RequestSamplesOptions(double longitudeStart, double longitudeEnd, double latitudeStart, double latitudeEnd, TimePoint start, TimePoint end)
        {
            LongitudeStart = longitudeStart;
            LongitudeEnd = longitudeEnd;
            LatitudeStart = latitudeStart;
            LatitudeEnd = latitudeEnd;
            StartTime = start;
            EndTime = end;
        }
        public double LongitudeStart;
        public double LongitudeEnd;
        public double LatitudeStart;
        public double LatitudeEnd;
        public TimePoint StartTime;
        public TimePoint EndTime;
        public String toJSONString() {
            String text =  "{\"longitudeStart\": " + LongitudeStart + ","
                    + "\"longitudeEnd\": " + LongitudeEnd + ","
                    + "\"latitudeStart\": " + LatitudeStart + ","
                    + "\"latitudeEnd\": " + LatitudeEnd + ","
                    + "\"startTime\": \"" + StartTime.toString() + "\","
                    + "\"endTime\": \"" + EndTime.toString() + "\"}";
            return text;
        }
    }
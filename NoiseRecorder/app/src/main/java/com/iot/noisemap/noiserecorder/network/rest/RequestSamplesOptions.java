 package com.iot.noisemap.noiserecorder.network.rest;

 import com.iot.noisemap.noiserecorder.noisemap.NoiseMap.TimePoint;

 /**
  * Options for sample requests.
  */
 public class RequestSamplesOptions {
     /**
      * Creates a new sample request options.
      * @param longitudeStart Start longitude.
      * @param longitudeEnd End longitude.
      * @param latitudeStart Start latitude.
      * @param latitudeEnd End latitude.
      * @param start Start time.
      * @param end End time.
      */
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

     /**
      * Converts the options to a JSON representation.
      * @return The JSON representation.
      */
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
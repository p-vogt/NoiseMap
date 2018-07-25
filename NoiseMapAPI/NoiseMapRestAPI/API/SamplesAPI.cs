using NoiseMapRestAPI.Models;
using System.Collections.Generic;
using System.Globalization;
using System.Linq;

namespace NoiseMapRestAPI.API
{
    public class SamplesAPI
    {
        public static FilteredSamples getSamples(NoiseMapEntities db, RequestSamplesOptions options)
        {
            // filter the region
            if (options == null)
            {
                return new FilteredSamples(db.NOISE_SAMPLE);
            }
            // swap values if start > end
            if (options.LongitudeStart > options.LongitudeEnd)
            {
                var tmp = options.LongitudeStart;
                options.LongitudeStart = options.LongitudeEnd;
                options.LongitudeEnd = tmp;
            }
            if (options.LatitudeStart > options.LatitudeEnd)
            {
                var tmp = options.LatitudeStart;
                options.LatitudeStart = options.LatitudeEnd;
                options.LatitudeEnd = tmp;
            }

            var filteredData = db.NOISE_SAMPLE.Where(x => x.longitude >= options.LongitudeStart && x.longitude <= options.LongitudeEnd
                                    && x.latitude >= options.LatitudeStart && x.latitude <= options.LatitudeEnd);

            filteredData = applyTimeFilter(options, filteredData);
            return new FilteredSamples(filteredData);
        }

        private class TimeWindow
        {
            public int Hour;
            public int Minute;
        }
        private static IQueryable<NOISE_SAMPLE> applyTimeFilter(RequestSamplesOptions options, IQueryable<NOISE_SAMPLE> data)
        {
            var startTimeSplitted = options.StartTime.Split(':');
            var endTimeSplitted = options.EndTime.Split(':');
            int hour;
            int minute;
            var success = int.TryParse(startTimeSplitted[0], out hour);
            if (!success)
            {
                return data;
            }
            success = int.TryParse(startTimeSplitted[1], out minute);
            if (!success)
            {
                return data;
            }

            var start = new TimeWindow
            {
                Hour = hour,
                Minute = minute
            };
            success = int.TryParse(endTimeSplitted[0], out hour);
            if (!success)
            {
                return data;
            }
            success = int.TryParse(endTimeSplitted[1], out minute);
            if (!success)
            {
                return data;
            }

            var end = new TimeWindow
            {
                Hour = hour,
                Minute = minute
            };

            if (start.Hour == end.Hour && start.Minute == end.Minute)
            {
                return data;
            }
            else if (start.Hour != end.Hour && start.Hour < end.Hour)
            {
                return data.Where(x => x.timestamp.Value.Hour == start.Hour && x.timestamp.Value.Minute >= start.Minute
                                    || x.timestamp.Value.Hour > start.Hour && x.timestamp.Value.Hour < end.Hour
                                    || x.timestamp.Value.Hour == end.Hour && x.timestamp.Value.Minute <= end.Minute);

            }
            else if (start.Hour == end.Hour)
            {
                return data.Where(x => x.timestamp.Value.Hour == start.Hour
                                  && x.timestamp.Value.Minute >= start.Minute
                                  && x.timestamp.Value.Minute <= end.Minute);
            }
            else
            {   //start > end 
                return data.Where(x => x.timestamp.Value.Hour == end.Hour && x.timestamp.Value.Minute <= end.Minute
                                || x.timestamp.Value.Hour <= end.Hour
                                || x.timestamp.Value.Hour == start.Hour && x.timestamp.Value.Minute >= start.Minute
                                || x.timestamp.Value.Hour > start.Hour);
            }
        }
    }
    public class FilteredSamples
    {
        public IQueryable samples;
        public FilteredSamples(IQueryable samples)
        {
            this.samples = samples;
        }
    }
    public class RequestSamplesOptions
    {
        public RequestSamplesOptions()
        {

        }
        public RequestSamplesOptions(double longitudeStart, double longitudeEnd, double latitudeStart, double latitudeEnd, string startTime, string endTime)
        {
            LongitudeStart = longitudeStart;
            LongitudeEnd = longitudeEnd;
            LatitudeStart = latitudeStart;
            LatitudeEnd = latitudeEnd;
            StartTime = startTime;
            EndTime = endTime;
        }
        public double LongitudeStart;
        public double LongitudeEnd;
        public double LatitudeStart;
        public double LatitudeEnd;
        public string StartTime;
        public string EndTime;

        public static RequestSamplesOptions FromQuery(IEnumerable<KeyValuePair<string, string>> queryNameValuePairs)
        {
            var query = queryNameValuePairs.ToDictionary(x => x.Key, x => x.Value.Replace(",", "."));
            // filter the region 
            var longitudeStart = 0.0d;
            var longitudeEnd = 0.0d;
            var latitudeStart = 0.0d;
            var latitudeEnd = 0.0d;
            var startTime = "00:00";
            var endTime = "00:00";
            var isValid = false;

            if (query.Keys.Contains(nameof(longitudeStart)) && query.Keys.Contains(nameof(longitudeEnd))
                && query.Keys.Contains(nameof(latitudeStart)) && query.Keys.Contains(nameof(latitudeEnd))
                && query.Keys.Contains(nameof(startTime)) && query.Keys.Contains(nameof(endTime)))
            {
                isValid = double.TryParse(query[nameof(longitudeStart)], NumberStyles.Any, CultureInfo.InvariantCulture, out longitudeStart);
                if (isValid)
                {
                    isValid = double.TryParse(query[nameof(longitudeEnd)], NumberStyles.Any, CultureInfo.InvariantCulture, out longitudeEnd);
                }
                if (isValid)
                {
                    isValid = double.TryParse(query[nameof(latitudeStart)], NumberStyles.Any, CultureInfo.InvariantCulture, out latitudeStart);
                }
                if (isValid)
                {
                    isValid = double.TryParse(query[nameof(latitudeEnd)], NumberStyles.Any, CultureInfo.InvariantCulture, out latitudeEnd);
                }
                if (isValid)
                {
                    startTime = query[nameof(startTime)];
                    endTime = query[nameof(endTime)];
                }
            }

            if (!isValid)
            {
                return null;
            }
            return new RequestSamplesOptions(longitudeStart, longitudeEnd, latitudeStart, latitudeEnd, startTime, endTime);
        }
    }
}
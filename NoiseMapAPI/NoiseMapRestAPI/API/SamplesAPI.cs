using NoiseMapRestAPI.Models;
using System.Collections.Generic;
using System.Linq;

namespace NoiseMapRestAPI.API
{
    public class SamplesAPI
    {
        public static FilteredSamples getSamples(NoiseMapEntities db, RequestSamplesOptions options)
        {
            // filter the region

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

            var filteredData = db.NOISE_SAMPLE.Where(x => x.longitude >= options.LatitudeStart && x.longitude <= options.LatitudeEnd
                                    && x.latitude >= options.LatitudeStart && x.latitude <= options.LatitudeEnd);

            return new FilteredSamples(filteredData);
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

        public static RequestSamplesOptions FromQuery(IEnumerable<KeyValuePair<string, string>> queryNameValuePairs)
        {
            var query = queryNameValuePairs.ToDictionary(x => x.Key, x => x.Value);
            // filter the region 
            var longitudeStart = 0.0d;
            var longitudeEnd = 0.0d;
            var latitudeStart = 0.0d;
            var latitudeEnd = 0.0d;
            var isValid = false;
            if (query.Keys.Contains(nameof(longitudeStart)) && query.Keys.Contains(nameof(longitudeEnd))
                && query.Keys.Contains(nameof(latitudeStart)) && query.Keys.Contains(nameof(latitudeEnd)))
            {
                isValid = double.TryParse(query[nameof(longitudeStart)], out longitudeStart);
                if (isValid)
                {
                    isValid = double.TryParse(query[nameof(longitudeEnd)], out longitudeEnd);
                }
                if (isValid)
                {
                    isValid = double.TryParse(query[nameof(latitudeStart)], out latitudeStart);
                }
                if (isValid)
                {
                    isValid = double.TryParse(query[nameof(latitudeEnd)], out latitudeEnd);
                }
            }

            if (!isValid)
            {
                return null;
            }
            return new RequestSamplesOptions(longitudeStart, longitudeEnd, latitudeStart, latitudeEnd);
        }
    }
}
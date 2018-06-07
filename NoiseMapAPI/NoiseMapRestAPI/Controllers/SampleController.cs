using NoiseMapRestAPI.Models;
using System.Data.Entity;
using System.Linq;
using System.Net;
using System.Net.Http;
using System.Net.Http.Formatting;
using System.Web.Http;

namespace NoiseMapRestAPI.Controllers
{
    public class SampleController : ApiController
    {
        public class FilteredSamples
        {
            public IQueryable samples;
            public FilteredSamples(IQueryable samples)
            {
                this.samples = samples;
            }
        }

        private readonly NoiseMapEntities db = new NoiseMapEntities();

        // GET: api/Sample
        [Authorize]
        public HttpResponseMessage Get()
        {
            if (db.NOISE_SAMPLE == null || db.NOISE_SAMPLE.Count() == 0)
            {
                return Request.CreateResponse(HttpStatusCode.OK, "[]");
            }

            var query = Request.GetQueryNameValuePairs().ToDictionary(x => x.Key, x => x.Value);

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
                return Request.CreateResponse(HttpStatusCode.BadRequest, "requst invalid");
            }
            // swap values if start > end
            if (longitudeStart > longitudeEnd)
            {
                var tmp = longitudeStart;
                longitudeStart = longitudeEnd;
                longitudeEnd = tmp;
            }
            if (latitudeStart > latitudeEnd)
            {
                var tmp = latitudeStart;
                latitudeStart = latitudeEnd;
                latitudeEnd = tmp;
            }

            var filteredData = db.NOISE_SAMPLE.Where(x => x.longitude >= longitudeStart && x.longitude <= longitudeEnd
                                    && x.latitude >= latitudeStart && x.latitude <= latitudeEnd);

            var filteredSamples = new FilteredSamples(filteredData);
            return Request.CreateResponse(HttpStatusCode.OK, filteredSamples);
        }
        ~SampleController()
        {
            db.Dispose();
        }


        // GET: api/Sample/5
        [Authorize]
        public HttpResponseMessage Get(int id)
        {
            var sample = db.NOISE_SAMPLE.FirstOrDefault(x => x.Id == id);
            if (sample == null)
            {
                return Request.CreateResponse(HttpStatusCode.OK, "");
            }
            else
            {
                return Request.CreateResponse(HttpStatusCode.OK, sample);
            }
        }

        // POST: api/Sample
        [Authorize]
        public HttpResponseMessage Post([FromBody] NOISE_SAMPLE newSample)
        {
            newSample.userName = User.Identity.Name;
            newSample.Id = db.NOISE_SAMPLE.Count() == 0 ? 0 : (db.NOISE_SAMPLE.Max(x => x.Id) + 1);
            db.NOISE_SAMPLE.Add(newSample);
            db.SaveChanges();
            var obj = Newtonsoft.Json.JsonConvert.DeserializeObject("{\"status\": \"success\"}");
            return Request.CreateResponse(HttpStatusCode.OK, obj, JsonMediaTypeFormatter.DefaultMediaType);
        }
    }
}

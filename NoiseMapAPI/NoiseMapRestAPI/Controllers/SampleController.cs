using NoiseMapRestAPI.API;
using NoiseMapRestAPI.Models;
using System.Linq;
using System.Net;
using System.Net.Http;
using System.Net.Http.Formatting;
using System.Web.Http;

namespace NoiseMapRestAPI.Controllers
{

    public class SampleController : ApiController
    {

        private readonly NoiseMapEntities db = new NoiseMapEntities();

        // GET: api/Sample
        [Authorize]
        public HttpResponseMessage Get()
        {
            if (db.NOISE_SAMPLE == null || db.NOISE_SAMPLE.Count() == 0)
            {
                return Request.CreateResponse(HttpStatusCode.OK, "[]");
            }

            var keyValuePairs = Request.GetQueryNameValuePairs();
            if(keyValuePairs.Count() < 4)
            {
                return Request.CreateResponse(HttpStatusCode.BadRequest, "requst invalid");
            }
            var options = RequestSamplesOptions.FromQuery(keyValuePairs);
            var filteredSamples = SamplesAPI.getSamples(db, options);
            if (filteredSamples == null)
            {
                Request.CreateResponse(HttpStatusCode.BadRequest, "requst invalid");
            }
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

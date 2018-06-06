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
        public class Samples
        {
            public DbSet<NOISE_SAMPLE> samples;
            public Samples(DbSet<NOISE_SAMPLE> samples)
            {
                this.samples = samples;
            }
        }

        private readonly NoiseMapEntities db = new NoiseMapEntities();

        // GET: api/Sample
        [Authorize]
        public HttpResponseMessage Get()
        {
            var samples = new Samples(db.NOISE_SAMPLE);
            if (samples == null)
            {
                return Request.CreateResponse(HttpStatusCode.OK, "");
            }
            else
            {
                return Request.CreateResponse(HttpStatusCode.OK, samples);
            }
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

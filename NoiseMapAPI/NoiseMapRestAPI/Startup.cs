using Microsoft.Owin;
using Owin;

[assembly: OwinStartup(typeof(NoiseMapRestAPI.Startup))]

namespace NoiseMapRestAPI
{
    public partial class Startup
    {
        public void Configuration(IAppBuilder app)
        {
            ConfigureAuth(app);
        }
    }
}

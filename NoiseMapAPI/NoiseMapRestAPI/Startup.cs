using Microsoft.Owin;
using NoiseMapRestAPI.MQTT;
using Owin;
using System.Threading;

[assembly: OwinStartup(typeof(NoiseMapRestAPI.Startup))]

namespace NoiseMapRestAPI
{
    public partial class Startup
    {
        public void Configuration(IAppBuilder app)
        {
            ConfigureAuth(app);
            var server = new MqttServer();
            var client = new MqttClient();
            new Thread(async () => 
                await server.Start()
            ).Start();
            new Thread(async () => 
                await client.Connect()
            ).Start();
        }
    }
}

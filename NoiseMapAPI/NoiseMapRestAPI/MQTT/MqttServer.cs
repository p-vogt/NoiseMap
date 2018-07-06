using MQTTnet;
using MQTTnet.Server;
using System.Threading.Tasks;

namespace NoiseMapRestAPI
{
    public class MqttServer
    {
        private IMqttServer server;
        private IMqttServerOptions options;
        public MqttServer()
        {
            // Configure MQTT server.
            options = new MqttServerOptionsBuilder()
                .WithConnectionBacklog(100)
                .WithDefaultEndpointPort(1884).Build();

            server = new MqttFactory().CreateMqttServer();

        }
        public async Task Start()
        {
            await server.StartAsync(options);

        }
        public async Task Stop()
        {
            await server.StopAsync();
        }
    }

}
using MQTTnet;
using MQTTnet.Client;
using Newtonsoft.Json;
using NoiseMapRestAPI.Models;
using System;
using System.Linq;
using System.Threading.Tasks;

namespace NoiseMapRestAPI.MQTT
{
    public class MqttClient
    {
        private static string NEW_MEASUREMENT_TOPIC_NAME = "measurement";
        private IMqttClient client;
        private IMqttClientOptions options;
        public MqttClient()
        {
            var factory = new MqttFactory();
            client = factory.CreateMqttClient();
            // Create TCP based options using the builder.
            options = new MqttClientOptionsBuilder()
                .WithClientId("Client1")
                .WithTcpServer("127.0.0.1", 1884)
                .WithCredentials("bud", "%spencer%")
                // .WithTls()
                // .WithCleanSession()
                .Build();

            client.Connected += async (s, e) =>
            {
                Console.WriteLine("### CONNECTED WITH SERVER ###");

                // Subscribe to a topic
                await client.SubscribeAsync(new TopicFilterBuilder().WithTopic(NEW_MEASUREMENT_TOPIC_NAME).Build());
                Console.WriteLine("### SUBSCRIBED ###");

                client.ApplicationMessageReceived += Client_ApplicationMessageReceived;
            };

        }
        private readonly NoiseMapEntities db = new NoiseMapEntities();
        private void Client_ApplicationMessageReceived(object sender, MqttApplicationMessageReceivedEventArgs e)
        {
            if(e.ApplicationMessage.Topic.Equals(NEW_MEASUREMENT_TOPIC_NAME))
            {

                var payload = e.ApplicationMessage.Payload;
                var result = System.Text.Encoding.UTF8.GetString(payload);
                var sample = JsonConvert.DeserializeObject<NOISE_SAMPLE>(result);
                sample.userName = "MQTT_TEST";
                sample.Id = db.NOISE_SAMPLE.Count() == 0 ? 0 : (db.NOISE_SAMPLE.Max(x => x.Id) + 1);
                db.NOISE_SAMPLE.Add(sample);
                db.SaveChanges();
            }
        }
        public async Task Connect()
        {
            await client.ConnectAsync(options);

        }
    }
}
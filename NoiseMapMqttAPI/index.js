const mosca = require('mosca');
const mqtt = require('mqtt');

const settings = {
  port: 1883
};

const server = new mosca.Server(settings);
const mqttClient = mqtt.connect('tcp://127.0.0.1:1883', { clientId: '0' });

mqttClient.on('message', function (topic, message) {
    if(topic.includes("request")) {
      mqttClient.publish(topic.replace("request","response"),"moinsen");
    }
})

mqttClient.on('connect', function () {

})

server.on('clientConnected', function (client) {
  if (client.id != '0') {
    mqttClient.subscribe("clients/+/request");
    console.log('client connected', client.id);
  }
});

// fired when a message is received
server.on('published', function (packet, client) {
  console.log('Published', packet);
});

server.on('ready', setup);

// fired when the mqtt server is ready
function setup() {
  console.log('Mosca server is up and running');
}
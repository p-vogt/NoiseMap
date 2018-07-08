"use strict";
const mosca = require('mosca');
const mqtt = require('mqtt');
const DatabaseConnection = require('./src/DatabaseConnection');
const DB_PW = require("./secret");
var pubsubsettings = {
  type: 'redis',
  redis: require('redis'),
  db: 12,
  port: 6379,
  return_buffers: true, // to handle binary payloads
  host: "localhost"
};

async function getSamples(topic, longitudeStart, longitudeEnd, latitudeStart, latitudeEnd) {

  await db.connect(DB_PW);
  const result = await db.querySamples(longitudeStart, longitudeEnd, latitudeStart, latitudeEnd);
  db.disconnect();
  mqttClient.publish(topic, JSON.stringify({ samples: result }));
}

const settings = {
  port: 1883,
  backend: pubsubsettings
};

const server = new mosca.Server(settings);
const mqttClient = mqtt.connect('tcp://127.0.0.1:1883', { reconnecting: true, clientId: '0', username: "0", password: "dasPW" });
const db = new DatabaseConnection();

mqttClient.on('message', (topic, message) => {
  const jsonMsg = message.toString('ascii');
  let json = {};
  try {
    json = JSON.parse(jsonMsg);
  } catch (ex) {
    console.error(ex);
    return;
  }
  const reSamplesRequest = /^clients\/[^\/]+\/request$/
  const reNewMeasurement = /^clients\/[^\/]+\/newMeasurement$/
  if (topic.match(reNewMeasurement)) {
    if (!client.user) {
      return; // not authorized
    }
    json.userName = client.user
  }
  else if (topic.match(reSamplesRequest)) {
    const { longitudeStart, longitudeEnd, latitudeStart, latitudeEnd } = json;
    getSamples(topic.replace("request", "response"), longitudeStart, longitudeEnd, latitudeStart, latitudeEnd);
  }
})
// Accepts the connection if the username and password are valid
var authenticate = (client, username, password, callback) => {
  var authorized = (password.toString() === 'dasPW');
  if (authorized) client.user = username;
  callback(null, authorized);
}

var authorizePublish = (client, topic, payload, callback) => {
  if (topic.split('/').length > 1) {
    callback(null, client.user == topic.split('/')[1]);
  }
}

function subscribeToClientResponse(clientId) {
  if (clientId !== '0') {
    mqttClient.subscribe(`clients/${clientId}/request`, { qos: 0 });
  }
}
mqttClient.on('connect', (client) => {
  console.log('client connected', client.id);
  for (var client in server.clients) {
    if (server.clients.hasOwnProperty(client)) {
      subscribeToClientResponse(client)
    }
  }
})

server.on('clientConnected', (client) => {
  subscribeToClientResponse(client.id)
});
server.on('clientDisconnected', (client) => {
  if (client.id != '0') {
    mqttClient.unsubscribe(`clients/${client.id}/request`);
    console.log('client disconnected', client.id);
  }
})
// fired when a message is received
server.on('published', (packet, client) => {
  console.log('Published', packet);
});

server.on('ready', setup);

// fired when the mqtt server is ready
function setup() {
  server.authenticate = authenticate;
  server.authorizePublish = authorizePublish;
  console.log('MQTT server is up and running');
}
"use strict";
const mosca = require('mosca');
const mqtt = require('mqtt');
const DatabaseConnection = require('./src/DatabaseConnection');
const secret = require("./secret");
var pubsubsettings = {
  type: 'redis',
  redis: require('redis'),
  db: 12,
  port: 6379,
  return_buffers: true, // to handle binary payloads
  host: "localhost"
};

async function getSamples(topic, longitudeStart, longitudeEnd, latitudeStart, latitudeEnd) {

  await db.connect(secret.DB_PW);
  const result = await db.querySamples(longitudeStart, longitudeEnd, latitudeStart, latitudeEnd);
  db.disconnect();
  mqttClient.publish(topic, JSON.stringify({ samples: result }));
}

const settings = {
  port: 1883,
  backend: pubsubsettings
};

const server = new mosca.Server(settings);
const mqttClient = mqtt.connect('tcp://127.0.0.1:1883', { reconnecting: true, clientId: '0', username: secret.CLIENT_USER, password: secret.CLIENT_PW });
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
var authenticate = async (client, username, password, callback) => {
  await db.connect(secret.DB_PW);
  var authorized = await db.checkLogin(username, password);
  db.disconnect();
  if (authorized) client.user = username;
  callback(null, authorized);
}

var authorizePublish = (client, topic, payload, callback) => {
  if (topic && topic.split('/') && topic.split('/').length > 1) {
    callback(null, client.id == topic.split('/')[1]);
  }
}

mqttClient.on('connect', (client) => {
  mqttClient.subscribe(`clients/+/request`, { qos: 0 });
})

server.on('clientConnected', (client) => {
  console.log('client connected', client.id)
});
server.on('clientDisconnected', (client) => {
  console.log('client disconnected', client.id)
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
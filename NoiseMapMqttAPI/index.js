"use strict";
const mosca = require('mosca');
const mqtt = require('mqtt');
const DatabaseConnection = require('./src/DatabaseConnection');
const DB_PW = require("./secret");

async function getSamples(topic, longitudeStart, longitudeEnd, latitudeStart, latitudeEnd) {
  await db.connect(DB_PW);
  const result = await db.querySamples(longitudeStart, longitudeEnd, latitudeStart, latitudeEnd);
  db.disconnect();
  mqttClient.publish(topic, JSON.stringify({samples: result}));
}

const settings = {
  port: 1883,
};

const server = new mosca.Server(settings);
const mqttClient = mqtt.connect('tcp://127.0.0.1:1883', { clientId: '0' });
const db = new DatabaseConnection();

mqttClient.on('message', (topic, message) => {
  const re = /^clients\/[^\/]+\/request$/
  if (topic.match(re)) {
    const jsonMsg = message.toString('ascii');
    const json = JSON.parse(jsonMsg);
    const { longitudeStart, longitudeEnd, latitudeStart, latitudeEnd } = json;
    getSamples(topic.replace("request", "response"), longitudeStart, longitudeEnd, latitudeStart, latitudeEnd);
  }
})
// Accepts the connection if the username and password are valid
var authenticate = (client, username, password, callback) => {
  var authorized = (username === 'alice' && password.toString() === 'secret');
  if (authorized) client.user = username;
  callback(null, authorized);
}

// In this case the client authorized as alice can publish to /users/alice taking
// the username from the topic and verifing it is the same of the authorized user
var authorizePublish = (client, topic, payload, callback) => {
  callback(null, client.user == topic.split('/')[1]);
}

// In this case the client authorized as alice can subscribe to /users/alice taking
// the username from the topic and verifing it is the same of the authorized user
var authorizeSubscribe = (client, topic, callback) => {
  callback(null, client.user == topic.split('/')[1]);
}
mqttClient.on('connect', () => {

})

server.on('clientConnected', (client) => {
  if (client.id != '0') {
    mqttClient.subscribe(`clients/${client.id}/request`);
    console.log('client connected', client.id);
  }
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
  //server.authenticate = authenticate;
  //server.authorizePublish = authorizePublish;
  //server.authorizeSubscribe = authorizeSubscribe;
  console.log('MQTT server is up and running');
}
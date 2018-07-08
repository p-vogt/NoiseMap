"use strict";
const mosca = require('mosca');
const DatabaseConnection = require('./src/DatabaseConnection');
const MqttServerClient = require('./src/MqttServerClient');
const secret = require("./secret");
var pubsubsettings = {
  type: 'redis',
  redis: require('redis'),
  db: 12,
  port: 6379,
  return_buffers: true, // to handle binary payloads
  host: "localhost"
};

const settings = {
  port: 1883,
  backend: pubsubsettings
};

const db = new DatabaseConnection(secret.DB_PW);

const serverClient = new MqttServerClient('tcp://127.0.0.1:1883', secret.CLIENT_USER, secret.CLIENT_PW, db); // MqttServerClient
const server = new mosca.Server(settings);

// Accepts the connection if the username and password are valid
var authenticate = async (client, username, password, callback) => {
  await db.connect();
  var authorized = await db.checkLogin(username, password);
  db.disconnect();
  if (authorized) {
    client.user = username;
    serverClient.addClientIdMapping(client.id, username)

  }
  callback(null, authorized);
}

var authorizePublish = (client, topic, payload, callback) => {
  if (serverClient && client.id === serverClient.id ||
    (topic && topic.split('/') && topic.split('/').length > 1)) {
    callback(null, serverClient && client.id === serverClient.id || client.id == topic.split('/')[1]);
  }
}



server.on('clientConnected', (client) => {
  console.log('client connected', client.id)
});
server.on('clientDisconnected', (client) => {
  serverClient.removeClientIdMappting(client.id);
  console.log('client disconnected', client.id)
})
// fired when a message is received
server.on('published', (packet, client) => {
  if (packet && packet.topic && packet.topic.length > 0 && packet.topic[0] !== '$') {
    console.log('Published', packet);
  }
});

server.on('ready', setup);

// fired when the mqtt server is ready
function setup() {
  server.authenticate = authenticate;
  server.authorizePublish = authorizePublish;
  serverClient.connect();
  console.log('MQTT server is up and running');
}
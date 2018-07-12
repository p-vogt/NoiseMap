const mqtt = require('mqtt');
const USE_PROTOBUF = true; // change to false to use plain JSON

module.exports = class MqttServerClient {
    // properties:

    // id;
    // mqttClient;
    // uri;
    // db
    // mapClientIdToUsername

    constructor(uri, username, password, db) {
        let milliTime = process.hrtime();
        milliTime = milliTime[0] * 1000 + milliTime[1];
        this.id = 'NoiseMapApiMqttClient' + milliTime;
        this.uri = uri;
        this.username = username;
        this.password = password;
        this.db = db;
        this.mapClientIdToUsername = {};
    }
    connect() {
        this.mqttClient = mqtt.connect(this.uri, { reconnecting: true, clientId: this.id, username: this.username, password: this.password });
        this.mqttClient.on('message', (topic, message) => {
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
                this.insertSample(topic, json);
            }
            else if (topic.match(reSamplesRequest)) {
                const { longitudeStart, longitudeEnd, latitudeStart, latitudeEnd } = json;
                console.log("samples request:", json);
                this.getSamples(topic.replace("request", "response"), longitudeStart, longitudeEnd, latitudeStart, latitudeEnd);
            }
        })

        this.mqttClient.on('connect', (client) => {
            this.mqttClient.subscribe(`clients/+/request`, { qos: 0 });
            this.mqttClient.subscribe(`clients/+/newMeasurement`, { qos: 0 });
        })
    }
    async jsonResultToProtobuf(json) {
        return new Promise((resolve) => {
            const protobuf = require('protobufjs');
            protobuf.load("../proto/src/NoiseMap.proto")
                .then((root) => {
                    // Obtain a message type
                    const Samples = root.lookupType("noisemap.Samples");
                    const Sample = root.lookupType("noisemap.Samples.Sample");

                    let samples = [];
                    json.forEach(element => {
                        const subPayload = {
                            timestamp: element.timestamp.toISOString(),
                            longitude: element.longitude,
                            latitude: element.latitude,
                            noiseValue: element.noiseValue
                        };
                        let errMsg = Sample.verify(subPayload);
                        if (!errMsg) {
                            samples.push(Sample.create(subPayload))
                        }
                    });
                    const payload = { samples };
                    const errMsg = Sample.verify(payload);
                    if (errMsg) {
                        throw Error(errMsg);
                    }

                    const message = Samples.create(payload)
                    const buffer = Samples.encode(message).finish();
                    resolve(buffer)
                })
                .catch(err => {
                    console.error(err);
                    resolve();
                });
        });
    }
    async getSamples(topic, longitudeStart, longitudeEnd, latitudeStart, latitudeEnd) {

        await this.db.connect();
        const result = await this.db.querySamples(longitudeStart, longitudeEnd, latitudeStart, latitudeEnd);
        this.db.disconnect();
        let msg = "";
        if(USE_PROTOBUF) {
            msg = await this.jsonResultToProtobuf(result);
        } else {
            msg = JSON.stringify({ samples: result }); 
        }
        if(msg) {
            this.mqttClient.publish(topic, msg, { qos: 1, retain: "true" });
        }
    }
    async insertSample(topic, json) {
        const clientId = topic.replace("clients/", "").replace("/newMeasurement", "");
        const username = this.mapClientIdToUsername[clientId];
        const insertSampleQuery = `DECLARE @maxId int;
        SELECT @maxId = MAX(ID) FROM NOISE_SAMPLE
        INSERT INTO NOISE_SAMPLE(id, timestamp, noiseValue, latitude, longitude, accuracy, version, createdAt, updatedAt, speed, userName)
        VALUES(@maxID + 1, '${json.timestamp}', ${json.noiseValue}, ${json.latitude}, ${json.longitude}, ${json.accuracy}, '${json.version}', '${json.createdAt}', '${json.updatedAt}', ${json.speed}, '${username}');`

        await this.db.connect();
        const result = await this.db.executeQuery(insertSampleQuery);
        if (!result) {
            console.error("error while inserting sample");
        }
        this.db.disconnect();
    }

    addClientIdMapping(userId, username) {
        this.mapClientIdToUsername[userId] = username;
    }
    removeClientIdMappting(userId) {
        delete this.mapClientIdToUsername[userId];
    }
}


package com.iot.noisemap.noiserecorder.network.mqtt;

import org.eclipse.paho.client.mqttv3.MqttMessage;

public class StoredMqttMessage {

    public StoredMqttMessage(String topic, MqttMessage message) {
        this.topic = topic;
        this.message = message;
    }
    public String topic;
    public MqttMessage message;
}

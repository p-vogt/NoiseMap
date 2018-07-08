package com.example.patrick.noiserecorder.network;

import org.eclipse.paho.client.mqttv3.MqttMessage;

class StoredMqttMessage {

    public StoredMqttMessage(String topic, MqttMessage message) {
        this.topic = topic;
        this.message = message;
    }
    public String topic;
    public MqttMessage message;
}

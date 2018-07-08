package com.example.patrick.noiserecorder.network.mqtt;

import org.eclipse.paho.client.mqttv3.MqttMessage;

public interface INoiseMapMqttConsumer {
    void onMessageArrived(String topic, MqttMessage message);
    void onConnected();
    void onConnectionFailed();
    void onConnectionLost();

}

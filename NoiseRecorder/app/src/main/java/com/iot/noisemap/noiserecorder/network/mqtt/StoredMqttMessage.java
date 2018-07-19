package com.iot.noisemap.noiserecorder.network.mqtt;

import org.eclipse.paho.client.mqttv3.MqttMessage;

/**
 * Buffered MQTT message.
 */
public class StoredMqttMessage {

    /**
     * Creates a new message.
     * @param topic Topic.
     * @param message Message.
     */
    public StoredMqttMessage(String topic, MqttMessage message) {
        this.topic = topic;
        this.message = message;
    }
    public String topic;
    public MqttMessage message;
}

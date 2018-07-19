package com.iot.noisemap.noiserecorder.network.mqtt;

import org.eclipse.paho.client.mqttv3.MqttMessage;

/**
 * Interface for mqtt noise map consumers/clients.
 */
public interface INoiseMapMqttConsumer {
    /**
     * Gets called when a new message arrived.
     * @param topic Message topic.
     * @param message Incoming message.
     */
    void onMessageArrived(String topic, MqttMessage message);

    /**
     * Gets called when the MQTT client connected.
     */
    void onConnected();
    /**
     * Gets called when the MQTT connection failed.
     */
    void onConnectionFailed();
    /**
     * Gets called when the MQTT connection is lost.
     */
    void onConnectionLost();

}

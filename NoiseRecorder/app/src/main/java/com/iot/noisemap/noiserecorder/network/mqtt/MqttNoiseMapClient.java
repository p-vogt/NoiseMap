package com.iot.noisemap.noiserecorder.network.mqtt;

import android.content.Context;
import android.util.Log;
import org.eclipse.paho.android.service.MqttAndroidClient;
import org.eclipse.paho.client.mqttv3.DisconnectedBufferOptions;
import org.eclipse.paho.client.mqttv3.IMqttActionListener;
import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.IMqttToken;
import org.eclipse.paho.client.mqttv3.MqttCallbackExtended;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;

import java.util.ArrayList;
import java.util.List;

/**
 * MQTT client for the MQTT noise map broker.
 */
public class MqttNoiseMapClient {

    private final MqttAndroidClient mqttAndroidClient;
    private boolean isMqttConnected = false;
    private INoiseMapMqttConsumer caller;
    private String clientId;
    private String username;
    private String password;
    MqttConnectOptions mqttConnectOptions;
    private List<StoredMqttMessage> msgBuffer = new ArrayList<>();

    /**
     * Creates a new MQTT client.
     * @param clientId Id of the client.
     * @param username Name of the user.
     * @param password Password of the user.
     * @param caller Calling consumer.
     * @param applicationContext Application context.
     */
    public MqttNoiseMapClient(String clientId, String username, String password, INoiseMapMqttConsumer caller, Context applicationContext) {
        this.caller = caller;
        this.clientId = clientId;
        this.username = username;
        this.password = password;
        this.mqttAndroidClient = new MqttAndroidClient(applicationContext, "tcp://noisemap.westeurope.cloudapp.azure.com:1883", clientId);

        initMqtt();
    }

    /**
     * Initializes the client.
     * -> Set callbacks and settings etc.
     */
    private void initMqtt() {
        mqttAndroidClient.setCallback(new MqttCallbackExtended() {
            @Override
            public void connectComplete(boolean reconnect, String serverURI) {
                isMqttConnected = true;
                try {
                    mqttAndroidClient.subscribe("clients/" + clientId + "/response",1);
                } catch (MqttException e) {
                    Log.d("MQTT: could not sub", e.getMessage());
                }
                publishBufferMessages();
                caller.onConnected();
            }

            @Override
            public void connectionLost(Throwable cause) {
                isMqttConnected = false;
                caller.onConnectionLost();
                Log.d("MQTT", "disconnected");
            }

            @Override
            public void messageArrived(String topic, MqttMessage message) throws Exception {
                caller.onMessageArrived(topic, message);
            }

            @Override
            public void deliveryComplete(IMqttDeliveryToken token) {
                Log.d("MQTT", "deliveryComplete");
            }
        });
        mqttConnectOptions = new MqttConnectOptions();
        mqttConnectOptions.setCleanSession(false);
        mqttConnectOptions.setAutomaticReconnect(true);
        mqttConnectOptions.setUserName(username);
        mqttConnectOptions.setPassword(password.toCharArray());
    }

    /**
     * Closes the connection.
     */
    public void disconnect() {
        if(!isMqttConnected) {
            return;
        }
        try {
            mqttAndroidClient.disconnect();
        } catch (MqttException e) {
        }
    }

    /**
     * Opens the connection.
     */
    public void connect() {
        if(isMqttConnected) {
            return;
        }
        try {
            mqttAndroidClient.connect(mqttConnectOptions, null, new IMqttActionListener() {
                @Override
                public void onSuccess(IMqttToken asyncActionToken) {
                    DisconnectedBufferOptions disconnectedBufferOptions = new DisconnectedBufferOptions();
                    disconnectedBufferOptions.setBufferEnabled(true);
                    disconnectedBufferOptions.setBufferSize(1000);
                    disconnectedBufferOptions.setPersistBuffer(false);
                    disconnectedBufferOptions.setDeleteOldestMessages(false);
                    mqttAndroidClient.setBufferOpts(disconnectedBufferOptions);
                }

                @Override
                public void onFailure(IMqttToken asyncActionToken, Throwable exception) {
                    Log.d("MQTT", "onFailure");
                    caller.onConnectionFailed();
                }
            });


        } catch (MqttException ex){
            Log.d("MQTT: could not connect:", ex.getMessage());
        }
    }

    /**
     * Publishes a new sample to the browker.
     * @param message
     */
    public void publishSample(String message) {
        MqttMessage msg = new MqttMessage();
        msg.setRetained(true);
        msg.setQos(1);
        msg.setPayload(message.getBytes());
        publish("clients/" + clientId + "/newMeasurement",msg);
    }

    /**
     * Publishes all buffered messages.
     */
    public void publishBufferMessages() {
        if(isMqttConnected) {
            int numTries = 0;
            while(msgBuffer.size() > 0) {
                if(numTries > 20) {
                    return; // publish error, should not happen
                }
                StoredMqttMessage curMessage = msgBuffer.get(0);
                try {
                    this.mqttAndroidClient.publish(curMessage.topic, curMessage.message);
                    msgBuffer.remove(0);
                } catch (MqttException e) {
                    numTries++;
                }
            }
        }
    }

    /**
     * Publishes a message to a topic.
     * @param topic Desired topic.
     * @param msg Desired message.
     */
    public void publish(String topic, MqttMessage msg) {
        msgBuffer.add(new StoredMqttMessage(topic,msg));
        if(isMqttConnected) {
            publishBufferMessages();
        } else {
            this.connect();
        }
    }

    /**
     * Sends a new request.
     * @param msg Message.
     * @throws MqttException Publish failed.
     */
    public void request(MqttMessage msg) throws MqttException {

        if(isMqttConnected) {
            this.publish("clients/" + clientId + "/request", msg);
        }else {
            this.connect();
        }
    }
}

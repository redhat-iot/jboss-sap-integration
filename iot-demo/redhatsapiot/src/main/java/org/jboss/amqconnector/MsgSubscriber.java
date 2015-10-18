package org.jboss.amqconnector;

import java.io.IOException;

import javax.script.ScriptException;

import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;

import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.eclipse.paho.client.mqttv3.MqttClient;

/**
 * 
 * @author kprasann
 *
 */
public class MsgSubscriber implements MqttCallback {

	MqttClient clientReceiver;

	MqttConnectOptions connOpt;

	public MsgSubscriber() {
	}

	public static void main(String[] args) throws InterruptedException,
			ScriptException, IOException {
		long startTime = System.currentTimeMillis();
		new MsgSubscriber().doDemo();
		long endTime = System.currentTimeMillis();
	}

	public void doDemo() throws InterruptedException, ScriptException,
			IOException {
		try {

			clientReceiver = new MqttClient("tcp://localhost:1883", "Receiver");
			connOpt = new MqttConnectOptions();
			connOpt.setCleanSession(true);
			connOpt.setKeepAliveInterval(30);
			connOpt.setUserName("admin");
			connOpt.setPassword("admin".toCharArray());
			clientReceiver.connect(connOpt);

			clientReceiver.subscribe("customerenter");
			clientReceiver.setCallback(this);

		} catch (MqttException e) {
			System.out.println("ERROR");
		}
	}

	public void connectionLost(Throwable cause) {
		// TODO Auto-generated method stub

	}

	public void messageArrived(String topic, MqttMessage message) {
		System.out.println("Received: " + message.toString());
	}

	public void deliveryComplete(IMqttDeliveryToken token) {

	}

}
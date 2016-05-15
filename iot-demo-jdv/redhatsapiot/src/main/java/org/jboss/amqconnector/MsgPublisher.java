package org.jboss.amqconnector;

import java.awt.Point;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.json.JSONObject;

/**
 * 
 * @author kprasann
 *
 */
public class MsgPublisher implements MqttCallback {

	MqttClient clientSender;
	MqttClient clientReceiver;
	
	//MqttClient clientClassPublish;
	MqttConnectOptions connOpt;

	public MsgPublisher() {
	}

	public static void main(String[] args) throws InterruptedException,
			 IOException {
		long startTime = System.currentTimeMillis();
		new MsgPublisher().doDemo();
		long endTime = System.currentTimeMillis();
	}

	public void doDemo() throws InterruptedException,
			IOException {
		try {
			int i = 0;
			clientSender = new MqttClient("tcp://localhost:1883", "Sender");
			clientReceiver = new MqttClient("tcp://localhost:1883", "Receiver");
		//	clientClassPublish = new MqttClient("tcp://localhost:1883", "ClassRec");

			connOpt = new MqttConnectOptions();
			connOpt.setCleanSession(true);
			connOpt.setKeepAliveInterval(30);
			connOpt.setUserName("admin");
			connOpt.setPassword("admin".toCharArray());
			// clientR.connect(connOpt);
			clientSender.connect(connOpt);
			MqttMessage message = new MqttMessage();
			clientReceiver.connect(connOpt);

			clientReceiver.subscribe("customerenter");
			clientReceiver.subscribe("customermove");
			clientReceiver.subscribe("customerexit");
			clientReceiver.setCallback(this);

			Process p = Runtime.getRuntime().exec("python custsim.py");
			// StringWriter writer = new StringWriter(); //ouput will be stored
			// here

			BufferedReader stdInput = new BufferedReader(new InputStreamReader(
					p.getInputStream()));

			BufferedReader stdError = new BufferedReader(new InputStreamReader(
					p.getErrorStream()));
			String output = null;
			// read the output from the command
			System.out.println("Here is the standard output of the command:\n");
			while ((output = stdInput.readLine()) != null) {
				System.out.println(output);
				message.setPayload((output).getBytes());

				if (output.contains("New Customer")) {
		//			clientSender.publish("customerenter", message);

				} else if (output.contains("Moving")) {
					if (i % 200 == 0) {
						String jsonString = null;
						jsonString = output.substring(
								output.indexOf(": ") + 2, output.length());
						JSONObject obj = new JSONObject(jsonString);

						int x = obj.getInt("x");
						int y = obj.getInt("y");
						String id = obj.getString("id");
						long timestamp = obj.getLong("ts");
						Point pt = new Point(x, y);
						ClassificationMsgPublisher.doDemo(id, pt, timestamp, "focused customer");
					}
					
				//	clientSender.publish("customermove", message);
					

				} else if (output.contains("Exiting")) {
				//	clientSender.publish("customerexit", message);

				}

			}

			// read any errors from the attempted command
			System.out
					.println("Here is the standard error of the command (if any):\n");
			while ((output = stdError.readLine()) != null) {
				System.out.println(output);
			}
			 clientReceiver.disconnect();
			 clientSender.disconnect();
			 clientReceiver.close();
			 clientSender.close();

		} catch (MqttException e) {
			System.out.println("ERROR");
		}
	}

	public void connectionLost(Throwable cause) {
		// TODO Auto-generated method stub

	}

	public void messageArrived(String topic, MqttMessage message) {
		String msg = message.toString();
		System.out.println("Received: " + msg);
		 // Entry Point to invoke rules
		RuleProcessor.invokeRules(topic, msg);
	}
	

	public void deliveryComplete(IMqttDeliveryToken token) {

	}

}
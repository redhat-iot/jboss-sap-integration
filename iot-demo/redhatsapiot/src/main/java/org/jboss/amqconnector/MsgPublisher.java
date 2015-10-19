package org.jboss.amqconnector;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
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
public class MsgPublisher implements MqttCallback {

	MqttClient clientSender;
	MqttClient clientReceiver;
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
			clientSender = new MqttClient("tcp://localhost:1883", "Sender");
			clientReceiver = new MqttClient("tcp://localhost:1883", "Receiver");

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

			Process p = Runtime.getRuntime().exec("python custsimulator.py");
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
					clientSender.publish("customerenter", message);

				} else if (output.contains("Customer is Moving")) {
					clientSender.publish("customermove", message);

				} else if (output.contains("Customer is Exiting")) {
					clientSender.publish("customerexit", message);

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
		RuleProcessor.invokeRules(RuleProcessor.getKieSession(), msg);
	}

	public void deliveryComplete(IMqttDeliveryToken token) {

	}

}
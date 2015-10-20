package org.jboss.amqconnector;

import java.awt.Point;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;

import org.json.simple.JSONObject;

public class ClassificationMsgPublisher implements MqttCallback{
	
	
	static MqttClient clientSender;
	static MqttConnectOptions connOpt;
	

	public ClassificationMsgPublisher(){
		
		
	};

	public static void main(String[] args) throws InterruptedException,
	 IOException {
    long startTime = System.currentTimeMillis();
    new ClassificationMsgPublisher().doDemo("test_1_2_3", new Point(2,3), 1122323232, "focussed customer");
    long endTime = System.currentTimeMillis();
    }

    public static void doDemo(String custId, Point location, long timestamp, String customerType) throws InterruptedException,
	IOException {
    try {
	 
	clientSender= new MqttClient("tcp://localhost:1883", "ClassReceiver");

	connOpt = new MqttConnectOptions();
	connOpt.setCleanSession(true);
	connOpt.setKeepAliveInterval(30);
	connOpt.setUserName("admin");
	connOpt.setPassword("admin".toCharArray());
	// clientR.connect(connOpt);
	clientSender.connect(connOpt);
	MqttMessage message = new MqttMessage();
	
	JSONObject obj=new JSONObject();
	  obj.put('x',location.getX());
	  obj.put('y',location.getY());
	  obj.put("id",custId);
	  obj.put("ts",timestamp);
	  obj.put("type",customerType);
	  
	  System.out.print(obj.toString());
	  message.setPayload((obj.toString()).getBytes());
	
	 clientSender.publish("salesnotification", message);
	 clientSender.disconnect();
	 
	 
	 clientSender.close();

     } catch (MqttException e) {
	     System.out.println("ERROR");
     }
     }

	public void connectionLost(Throwable arg0) {
		// TODO Auto-generated method stub
		
	}

	public void deliveryComplete(IMqttDeliveryToken arg0) {
		// TODO Auto-generated method stub
		
	}

	public void messageArrived(String arg0, MqttMessage arg1) throws Exception {
		// TODO Auto-generated method stub
		
	}

}

package org.jboss.amqconnector;

import java.util.Properties;

import javax.jms.*;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;

/**
 * Java Client to read messages sent from python script to MQTT Topic.
 * This will parse messages and send it to the JBoss BRMS Engine
 * @author kprasann
 *
 */
public class TopicSubscriber {

    private String topicName;
    private static String[] topicList = new String[]{"customer/enter","customer/move","customer/exit"};

    private boolean messageReceived = false;

    private static Context mContext = null;
    private static TopicConnectionFactory mTopicConnectionFactory = null;
    private TopicConnection  topicConnection = null;

    public static void main(String[] args) {
        TopicSubscriber subscriber = new TopicSubscriber();
        for (String topic : topicList) {
        	subscriber.subscribeWithTopicLookup(topic);
        }
        
    }

    public void subscribeWithTopicLookup(String topicName) {

        Properties properties = new Properties();
        properties.put(Context.INITIAL_CONTEXT_FACTORY, "org.jnp.interfaces.NamingContextFactory");
        properties.put(Context.PROVIDER_URL, "jnp://localhost:1099");
        properties.put("topic." + topicName, topicName);

        try {

            mContext = new InitialContext(properties);
            mTopicConnectionFactory = (TopicConnectionFactory)mContext.lookup("ConnectionFactory");

            topicConnection = mTopicConnectionFactory.createTopicConnection();

            System.out.println("Create Topic Connection for Topic " + topicName);

            while (!messageReceived) {
                try {
                    TopicSession topicSession = topicConnection
                            .createTopicSession(false, Session.AUTO_ACKNOWLEDGE);

                    Topic topic = (Topic) mContext.lookup(topicName);
                    // start the connection
                    topicConnection.start();

                    // create a topic subscriber
                    javax.jms.TopicSubscriber topicSubscriber = topicSession.createSubscriber(topic);

                    TestMessageListener messageListener = new TestMessageListener();
                    topicSubscriber.setMessageListener(messageListener);

                    Thread.sleep(5000);
                    topicSubscriber.close();
                    topicSession.close();
                } catch (JMSException e) {
                    e.printStackTrace();
                } catch (NamingException e) {
                    e.printStackTrace();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        } catch (NamingException e) {
            throw new RuntimeException("Error in initial context lookup", e);
        } catch (JMSException e) {
            throw new RuntimeException("Error in JMS operations", e);
        } finally {
            if (topicConnection != null) {
                try {
                    topicConnection.close();
                } catch (JMSException e) {
                    throw new RuntimeException(
                            "Error in closing topic connection", e);
                }
            }
        }
    }

    public class TestMessageListener implements MessageListener {
        public void onMessage(Message message) {
            try {
            	String msg = ((TextMessage) message).getText();
                System.out.println("Got the Message : "
                        + msg);
                messageReceived = true;
                
                // Invoke rule engine
                
                RuleProcessor.invokeRules(msg);
            } catch (JMSException e) {
                e.printStackTrace();
            }
        }
    }

}

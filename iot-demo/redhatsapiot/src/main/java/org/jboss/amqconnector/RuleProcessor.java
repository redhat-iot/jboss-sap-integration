package org.jboss.amqconnector;

import java.awt.Point;
import java.awt.Rectangle;
import java.util.ArrayList;
import java.util.List;

import org.jboss.brmspojos.CustomerMoveEvent;
import org.jboss.brmspojos.CustomerObj;
import org.jboss.brmspojos.Department;
import org.json.JSONObject;
import org.kie.api.KieBase;
import org.kie.api.KieBaseConfiguration;
import org.kie.api.KieServices;
import org.kie.api.conf.EventProcessingOption;
import org.kie.api.runtime.KieContainer;
import org.kie.api.runtime.KieSession;

/**
 * Java program to invoke drools engine and process messages from the
 * 
 * @author kprasann
 *
 */
public class RuleProcessor {
	private static KieSession kSession;

	public static KieSession getKieSession() {
		KieServices ks = KieServices.Factory.get();

		KieContainer kieContainer = ks.getKieClasspathContainer();

		KieBaseConfiguration config = ks.newKieBaseConfiguration();
		config.setOption(EventProcessingOption.STREAM);
		KieBase kieBase = kieContainer.newKieBase(config);
		// KieSession kieSession = kieContainer.newKieSession();
		kSession = kieBase.newKieSession();

		return kSession;
	}

	public static KieSession insertDepartments() {

		Rectangle deptLocationOne = new Rectangle(1, 2, 2, 4);
		Rectangle deptLocationTwo = new Rectangle(2, 3, 2, 4);
		Rectangle deptLocationThree = new Rectangle(2, 2, 3, 4);
		Rectangle deptLocationFour = new Rectangle(4, 2, 2, 4);

		Department deptOne = new Department("PHARMACY", deptLocationOne,
				Department.SPECIAL);
		Department deptTwo = new Department("FOOD", deptLocationTwo,
				Department.SPECIAL);
		Department deptThree = new Department("CLOTHING", deptLocationThree,
				Department.REGULAR);
		Department deptFour = new Department("TOYS", deptLocationFour,
				Department.REGULAR);

		kSession.insert(deptOne);
		kSession.insert(deptTwo);
		kSession.insert(deptThree);
		kSession.insert(deptFour);

		return kSession;

	}

	public static void invokeRules(KieSession kSession, String message) {

		Point custPt = new Point(2, 2);
		Point custPt1 = new Point(3, 3);
		Rectangle deptLocation = new Rectangle(1, 2, 2, 4);

		Department dept = new Department("PHARMACY", deptLocation,
				Department.SPECIAL);

		// CustomerObj c = new CustomerObj("122ATP1212", 1223323, 0, custPt,
		// dept);
		CustomerObj c = new CustomerObj("122ATP1212", custPt);
		CustomerObj c1 = new CustomerObj("A122ATP1212", custPt1);
		CustomerObj c3 = new CustomerObj("122ATP1212BB", custPt);
		CustomerObj c4 = new CustomerObj("A122ATP1212ff", custPt1);
		CustomerObj c5 = new CustomerObj("122ATP1212", custPt);
		kSession.insert(c);
		kSession.insert(c1);
		kSession.insert(c3);
		kSession.insert(c4);
		kSession.insert(c5);
		kSession.insert(dept);
		kSession.fireAllRules();
	}

	public static void invokeRulesAlt(KieSession kSession, String message) {
		String test = "Customer is Moving: {'y': 4, 'x': 1, 'id': '4c8ec014-73c8-11e5-91d7-a0999b16826b', 'ts': 1444974046}";

		String jsonString = null;
		if (message.contains("Customer is Moving")) {
			jsonString = message.substring(
					message.indexOf("Customer is Moving: "), message.length());

		}

		JSONObject obj = new JSONObject(jsonString);

		int x = obj.getInt("x");
		int y = obj.getInt("y");
		String id = obj.getString("id");
		long timestamp = obj.getLong("ts");
		Point p = new Point(x, y);
		CustomerMoveEvent cm = new CustomerMoveEvent(id, p, timestamp);
		kSession.insert(cm);
	}

	public static void main(String[] args) {
		kSession = getKieSession();
		kSession = insertDepartments();
		
		String testOne = "Customer is Moving: {'y': 4, 'x': 1, 'id': '4c8ec014-73c8-11e5-91d7-a0999b16826b', 'ts': 1444974046}";
        String testTwo = "Customer is Moving: {'y': 3, 'x': 1, 'id': '4c8ec014-73c8-11e5-91d7-a0999b16826b', 'ts': 1444974036}";
       // String testThree = "Customer is Moving: {'y': 4, 'x': 5, 'id': '4a29d19c-73c8-11e5-8ce0-a0999b16826b', 'ts': 1444974051}";
       // String testFour = "Customer is Moving: {'y': 2, 'x': 5, 'id': 'e532f445-73c7-11e5-980c-a0999b16826b', 'ts': 1444974060}";
        
        String jsonString = null;
        List<String> testStrings = new ArrayList<String>();
        testStrings.add(testOne);
        testStrings.add(testTwo);
       // testStrings.add(testThree);
       // testStrings.add(testFour);
        
        for (String message : testStrings) {
        	
        
		if (message.contains("Customer is Moving")) {
			jsonString = message.substring(
					message.indexOf("Customer is Moving: "), message.length());

		}

		JSONObject obj = new JSONObject(jsonString);

		int x = obj.getInt("x");
		int y = obj.getInt("y");
		String id = obj.getString("id");
		long timestamp = obj.getLong("ts");
		Point p = new Point(x, y);
		CustomerMoveEvent cm = new CustomerMoveEvent(id, p, timestamp);
		kSession.insert(cm);
        kSession.fireAllRules();
        }
	}
}

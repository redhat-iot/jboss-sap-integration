package org.jboss.amqconnector;

import java.awt.Point;
import java.awt.Rectangle;
import java.util.ArrayList;
import java.util.List;

import org.jboss.brmspojos.CustomerMoveEvent;
import org.jboss.brmspojos.CustomerObj;
import org.jboss.brmspojos.Department;
import org.jboss.brmspojos.DepartmentType;
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

		Rectangle deptLocationOne = new Rectangle(35, 31, 146, 16); //35 31, 35 57, 181 57, 181 31, 35 31
		Rectangle deptLocationTwo = new Rectangle(31, 99, 59, 61); //31 99, 31 160, 90 160, 90 99, 31 99
		Rectangle deptLocationThree = new Rectangle(110, 99, 61, 62); //110 99, 110 160, 171 161, 171 99, 110 99
		Rectangle deptLocationFour = new Rectangle(197, 132, 71, 27); //197 132, 198 159, 268 159, 269 134, 197 132
		Rectangle deptLocationFive = new Rectangle(206, 27, 24, 75); //206 27, 206 102, 230 102, 228 27, 206 27
		Rectangle deptLocationSix = new Rectangle(242, 27, 23, 76); //242 27, 242 103, 265 103, 264 27, 242 27
		
       
		 
		Department deptOne = new Department("MENS", deptLocationOne,
				Department.REGULAR);
		Department deptTwo = new Department("FORMAL", deptLocationTwo,
				Department.SPECIAL);
		Department deptThree = new Department("BOYS", deptLocationThree,
				Department.REGULAR);
		Department deptFour = new Department("WOMAN", deptLocationFour,
				Department.REGULAR);
		Department deptFive = new Department("GIRLS", deptLocationFive,
				Department.REGULAR);
		Department deptSix = new Department("SPORTWEAR", deptLocationSix,
				Department.SPECIAL);

		kSession.insert(deptOne);
		kSession.insert(deptTwo);
		kSession.insert(deptThree);
		kSession.insert(deptFour);
		kSession.insert(deptFive);
		kSession.insert(deptSix);

		return kSession;

	}

	 public static void invokeRules(String message) {

		kSession = getKieSession();
		kSession = insertDepartments();
		
	
        String jsonString = null;
       
        
		if (message.contains("Moving")) {
			jsonString = message.substring(
					message.indexOf(": ") + 2, message.length());
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
        String testThree = "Customer is Moving: {'y': 3, 'x': 1, 'id': '4c8ec014-73c8-11e5-91d7-a0999b16826b', 'ts': 1444974036}";

        // String testThree = "Customer is Moving: {'y': 4, 'x': 5, 'id': '4a29d19c-73c8-11e5-8ce0-a0999b16826b', 'ts': 1444974051}";
       // String testFour = "Customer is Moving: {'y': 2, 'x': 5, 'id': 'e532f445-73c7-11e5-980c-a0999b16826b', 'ts': 1444974060}";
        
        String jsonString = null;
        List<String> testStrings = new ArrayList<String>();
        testStrings.add(testOne);
        testStrings.add(testTwo);
       // testStrings.add(testThree);
       // testStrings.add(testFour);
        
        for (String message : testStrings) {
        	
        
		if (message.contains("Moving")) {
			jsonString = message.substring(
					message.indexOf(": ") + 2, message.length());

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
	
	public static void processMessages(String message) {
		
		// Customer is Exiting: 00b38fd9-7654-11e5-927e-a0999b16826b

		
		
	}
}

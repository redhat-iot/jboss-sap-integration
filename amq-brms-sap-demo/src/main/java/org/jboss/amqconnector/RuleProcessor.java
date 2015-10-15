package org.jboss.amqconnector;

import java.awt.Point;
import java.awt.Rectangle;

import org.jboss.brmspojos.CustomerObj;
import org.jboss.brmspojos.Department;
import org.kie.api.KieBase;
import org.kie.api.KieBaseConfiguration;
import org.kie.api.KieServices;
import org.kie.api.conf.EventProcessingOption;
import org.kie.api.runtime.KieContainer;
import org.kie.api.runtime.KieSession;

/**
 * Java program to invoke drools engine and process messages from the 
 * @author kprasann
 *
 */
public class RuleProcessor {
	private static KieSession kSession;
	
	  public static KieSession getKieSession() {
		  KieServices ks = KieServices.Factory.get();
			 
		  KieContainer kieContainer = ks.getKieClasspathContainer();

	      KieBaseConfiguration config = ks.newKieBaseConfiguration();
	      config.setOption(EventProcessingOption.STREAM );
	      KieBase kieBase = kieContainer.newKieBase( config );
	      //      KieSession kieSession = kieContainer.newKieSession();
	      kSession = kieBase.newKieSession();
	      return kSession;
	  }
	  public static void invokeRules(String message) {
		 
	      Point custPt = new Point(2,2);
	        Point custPt1 = new Point(3,3);
	        Rectangle deptLocation = new Rectangle(1,2,2,4);
	        
	        //Department dept = new Department(1, "PHARMACY", deptLocation);
	        Department dept = new Department("PHARMACY", deptLocation);

		   // CustomerObj c = new CustomerObj("122ATP1212", 1223323, 0, custPt, dept);
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
	  

}

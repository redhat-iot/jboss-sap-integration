package org.jboss.amqbrmstests;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.awt.Point;
import java.awt.Rectangle;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.Arrays;

import org.jboss.brmspojos.CustomerObj;
import org.jboss.brmspojos.Department;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.kie.api.KieBase;
import org.kie.api.KieBaseConfiguration;
import org.kie.api.KieServices;
import org.kie.api.builder.KieBuilder;
import org.kie.api.builder.KieFileSystem;
import org.kie.api.builder.Message;
import org.kie.api.builder.Results;
import org.kie.api.cdi.KSession;
import org.kie.api.conf.EventProcessingOption;
import org.kie.api.runtime.KieContainer;
import org.kie.api.runtime.KieSession;
import org.kie.api.runtime.StatelessKieSession;
import org.kie.api.runtime.rule.EntryPoint;

/**
 * Test Cases for SAP Red Hat Demo
 * 
 * @author kprasann
 *
 */
public class RetailTrackerTest {

	private static KieSession kSession;

	private static String[] specialDepts = new String[] { "PHARMACY", "FOOD",
			"APPAREL" };

	@BeforeClass
	public static void setup() throws FileNotFoundException {

		KieServices ks = KieServices.Factory.get();
		KieContainer kContainer = ks.getKieClasspathContainer();

		// CEP - get the KIE related configuration container and set the
		// EventProcessing (from default cloud) to Stream
		KieBaseConfiguration config = ks.newKieBaseConfiguration();
		config.setOption(EventProcessingOption.STREAM);
		KieBase kieBase = kContainer.newKieBase(config);
		// KieSession kieSession = kieContainer.newKieSession();
		kSession = kieBase.newKieSession();

	}

	
	/*public void testCustomerInDepartment() {
		System.out
				.println("** Check if customer is in a department with 3 moves and designate as entered **");

		Point custPt = new Point(2, 2);
		Point custPt1 = new Point(3, 3);
		Rectangle deptLocation = new Rectangle(1, 2, 2, 4);

		// Department dept = new Department(1, "PHARMACY", deptLocation);
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
		System.out.println("* Transaction Completed: ");
	}*/

}

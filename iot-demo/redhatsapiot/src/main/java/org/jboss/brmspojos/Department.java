package org.jboss.brmspojos;

import java.awt.Rectangle;

/**
 * 
 * @author kprasann
 *
 */
public class Department {


	public Department(String departmentName,
			Rectangle deptLocation, String deptType) {
		super();
		
		this.departmentName = departmentName;
		this.deptLocation = deptLocation;
		this.setDeptType(deptType);
		
	}

	public Department(
			Rectangle deptLocation) {
		super();
		this.deptLocation = deptLocation;
	}
	
	public Department(
			String departmentName) {
		super();
		this.departmentName = departmentName;
	}
	
	
	private String departmentName;
	private Rectangle deptLocation;
	private String deptType;
	public static final String SPECIAL = "SPECIAL";
	public static final String REGULAR = "REGULAR";

	public Rectangle getDeptLocation() {
		return deptLocation;
	}

	public void setDeptLocation(Rectangle deptLocation) {
		this.deptLocation = deptLocation;
	}
	
	public boolean containsCustomer(CustomerObj c) {
		if (this.deptLocation.contains(c.getLocation())) return true;
		else return false;
	}

	public String getDepartmentName() {
		return departmentName;
	}

	public void setDepartmentName(String departmentName) {
		this.departmentName = departmentName;
	}

	public String getDeptType() {
		return deptType;
	}

	public void setDeptType(String deptType) {
		this.deptType = deptType;
	}
}

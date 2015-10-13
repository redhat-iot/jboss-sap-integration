package org.jboss.brmspojos;

import java.awt.Rectangle;

public class Department {


	public Department(String departmentName,
			Rectangle deptLocation) {
		super();
		
		this.departmentName = departmentName;
		this.deptLocation = deptLocation;
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
}

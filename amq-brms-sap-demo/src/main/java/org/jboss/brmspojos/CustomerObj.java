package org.jboss.brmspojos;

import java.awt.Point;

public class CustomerObj {
	 
	public CustomerObj(String custId, long timestamp, Integer classification,
			Point location, Department department) {
		super();
		this.custId = custId;
		this.timestamp = timestamp;
		this.classification = classification;
		this.location = location;
		this.department = department;
	}
	
	public CustomerObj(String custId, Point location) {
		super();
		this.custId = custId;
		this.location = location;
	}
	
	private String custId;
	private long timestamp;
	private Integer classification;
	private Point location;
	private Department department;
	private Integer deptVisitCount;
	
	
	public long getTimestamp() {
		return timestamp;
	}
	public void setTimestamp(Integer timestamp) {
		this.timestamp = timestamp;
	}
	public String getCustId() {
		return custId;
	}
	public void setCustId(String custId) {
		this.custId = custId;
	}
	public Integer getClassification() {
		return classification;
	}
	public void setClassification(Integer classification) {
		this.classification = classification;
	}
	public Point getLocation() {
		return location;
	}
	public void setLocation(Point location) {
		this.location = location;
	}
	public Department getDepartment() {
		return department;
	}
	public void setDepartment(Department department) {
		this.department = department;
	}

	public Integer getDeptVisitCount() {
		if (deptVisitCount == null)
			return 0;
		else
		return deptVisitCount;
	}

	public void setDeptVisitCount(Integer deptVisitCount) {
		this.deptVisitCount = deptVisitCount;
	}
	

}

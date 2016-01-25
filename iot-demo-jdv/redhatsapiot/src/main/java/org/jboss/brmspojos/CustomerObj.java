package org.jboss.brmspojos;

import java.awt.Point;

/**
 * 
 * @author kprasann
 *
 */
public class CustomerObj {
	 
	public CustomerObj(String custId, long timestamp, Integer classification,
			Point location, String deptName) {
		super();
		this.custId = custId;
		this.location = location;
		this.timestamp = timestamp;
		this.classification = classification;
		this.deptName = deptName;
		this.messageSent = false;
	}
	
	public CustomerObj(String custId, Point location, long timestamp, Integer classification, String deptName) {
		super();
		this.custId = custId;
		this.location = location;
		this.timestamp = timestamp;
		this.classification = classification;
		this.deptName = deptName;
	}
	
	private String custId;
	private long timestamp;
	private Integer classification;
	private Point location;
	private String deptName;
	private Integer deptVisitCount;
	private boolean messageSent;
	
	
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

	public Integer getDeptVisitCount() {
		if (deptVisitCount == null)
			return 0;
		else
		return deptVisitCount;
	}

	public void setDeptVisitCount(Integer deptVisitCount) {
		this.deptVisitCount = deptVisitCount;
	}

	public String getDeptName() {
		return deptName;
	}

	public void setDeptName(String deptName) {
		this.deptName = deptName;
	}

	public boolean isMessageSent() {
		return messageSent;
	}

	public void setMessageSent(boolean messageSent) {
		this.messageSent = messageSent;
	}
	

}

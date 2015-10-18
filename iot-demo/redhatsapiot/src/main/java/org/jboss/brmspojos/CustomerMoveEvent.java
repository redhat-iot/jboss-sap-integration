package org.jboss.brmspojos;

import java.awt.Point;

/**
 * 
 * @author kprasann
 *
 */
public class CustomerMoveEvent {
	
	private String custId;
	private Point location;
	private long timestamp;
	public String getCustId() {
		return custId;
	}
	public void setCustId(String custId) {
		this.custId = custId;
	}
	public Point getLocation() {
		return location;
	}
	public void setLocation(Point location) {
		this.location = location;
	}
	public long getTimestamp() {
		return timestamp;
	}
	public void setTimestamp(long timestamp) {
		this.timestamp = timestamp;
	}
	public CustomerMoveEvent(String custId, Point location, long timestamp) {
		super();
		this.custId = custId;
		this.location = location;
		this.timestamp = timestamp;
	}

}

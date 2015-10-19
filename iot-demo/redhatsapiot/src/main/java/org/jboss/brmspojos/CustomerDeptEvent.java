package org.jboss.brmspojos;

public class CustomerDeptEvent {
	private Integer custId;
	private Department dept;
	private Integer visitCount;
	private boolean messageSent;
	
	public Integer getCustId() {
		return custId;
	}
	public void setCustId(Integer custId) {
		this.custId = custId;
	}
	public Department getDept() {
		return dept;
	}
	public void setDept(Department dept) {
		this.dept = dept;
	}
	public Integer getVisitCount() {
		return visitCount;
	}
	public void setVisitCount(Integer visitCount) {
		this.visitCount = visitCount;
	}
	public boolean isMessageSent() {
		return messageSent;
	}
	public void setMessageSent(boolean messageSent) {
		this.messageSent = messageSent;
	}

}

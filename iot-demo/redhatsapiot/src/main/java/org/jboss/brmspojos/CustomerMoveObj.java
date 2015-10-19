package org.jboss.brmspojos;

public class CustomerMoveObj {
	
	private String custId;
	private Integer totalMoves;
	
	public CustomerMoveObj(String custId, Integer totalMoves) {
		super();
		this.setCustId(custId);
		this.setTotalMoves(totalMoves);
	}

	public String getCustId() {
		return custId;
	}

	public void setCustId(String custId) {
		this.custId = custId;
	}

	public Integer getTotalMoves() {
		return totalMoves;
	}

	public void setTotalMoves(Integer totalMoves) {
		this.totalMoves = totalMoves;
	}
	
	

}

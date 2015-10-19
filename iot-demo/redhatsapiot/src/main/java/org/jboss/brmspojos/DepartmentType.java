package org.jboss.brmspojos;

/**
 * 
 * @author kprasann
 *
 */
public enum DepartmentType {
	
	PHARMACY (0),
	FOOD (1),
	CLOTHING (2),
	TOYS (3),
	STATIONARY (4),
	JEWELRY(5);
	
	private Integer deptId;
	
	private DepartmentType(Integer deptId) {
		this.setDeptId(deptId);
	}

	public Integer getDeptId() {
		return deptId;
	}

	public void setDeptId(Integer deptId) {
		this.deptId = deptId;
	}

}

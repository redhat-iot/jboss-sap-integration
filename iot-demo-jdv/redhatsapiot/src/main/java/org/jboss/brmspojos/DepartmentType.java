package org.jboss.brmspojos;

/**
 * 
 * @author kprasann
 *
 */
public enum DepartmentType {
	
	MENS (0),
	FORMAL (1),
	BOYS (2),
	WOMANS (3),
	GIRLS (4),
	SPORTWEAR(5);
	
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

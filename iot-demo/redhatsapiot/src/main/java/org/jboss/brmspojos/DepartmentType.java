package org.jboss.brmspojos;

/**
 * 
 * @author kprasann
 *
 */
public enum DepartmentType {
	
	PHARMACY ("special"),
	FOOD ("special"),
	CLOTHING ("regular"),
	TOYS ("regular"),
	BOOKS ("regular");
	
	private String deptType;
	
	private DepartmentType(String deptType) {
		this.deptType = deptType;
	}

}

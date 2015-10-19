package org.jboss.amqbrmstests;

import java.awt.Point;
import java.awt.Rectangle;

public class PointChecker {
	
	public static void main(String[] args) {
		
		Point pt1 = new Point(1,4);
		Point pt2 = new Point(1,3);
		
		Rectangle deptLocationOne = new Rectangle(1, 2, 2, 4);
		
		if (deptLocationOne.contains(pt1)) {
			System.out.println("Point 1 exists");
		} 
		
		if (deptLocationOne.contains(pt2)) {
			System.out.println("Point 2 exists");
			
		}
		
		
		
	}

}

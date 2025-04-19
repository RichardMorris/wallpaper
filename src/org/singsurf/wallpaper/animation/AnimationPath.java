/*
Created 9 Sep 2006 - Richard Morris
*/
package org.singsurf.wallpaper.animation;

import java.awt.Rectangle;

import org.singsurf.wallpaper.FundamentalDomain;

public abstract class AnimationPath {

	private String label;

	public void firstItteration(FundamentalDomain fd) { /* do nothing by default */ }

	abstract public void nextItteration(FundamentalDomain fd);
	
	public static AnimationPath getPathByName(String label,int speed,Rectangle rect) {
		AnimationPath path=null;
		if(label==null) return null;
		if(label.equals("up")) {
			path = new ShiftAnimation(0,-speed);
		}
		else if(label.equals("down")) {
			path = new ShiftAnimation(0,speed);
		}
		else if(label.equals("left")) {
			path = new ShiftAnimation(-speed,0);
		}
		else if(label.equals("right")) {
			path = new ShiftAnimation(speed,0);
		}
		else if(label.equals("NE")) {
			path = new ShiftAnimation(-speed,-speed);
		}
		else if(label.equals("NW")) {
			path = new ShiftAnimation(speed,-speed);
		}
		else if(label.equals("SE")) {
			path = new ShiftAnimation(-speed,speed);
		}
		else if(label.equals("SW")) {
			path = new ShiftAnimation(speed,speed);
		}
		else if(label.equals("rotate")) {
			path = new RotateAnimation(rect);
		}
		else if(label.equals("bounce")) {
			path = new BounceAnimation(rect,speed);
		}
		else if(label.equals("smooth")) {
			path = new LissajousAnimation(rect,speed);
		}
		path.label = label;
		return path;
	}

	public String getLabel() {
		return label;
	}
}

/*
Created 4 May 2007 - Richard Morris
*/
package org.singsurf.wallpaper;

import java.awt.Point;

public class Vec extends Point {
	private static final long serialVersionUID = 1L;

	public Vec(int x, int y) {
		super(x, y);
	}
	public Vec(Point p) {
		super(p);
	}
	public Vec add(Point p) {
		return new Vec(this.x+p.x,this.y+p.y);
	}
	public Vec sub(Point p) {
		return new Vec(this.x-p.x,this.y-p.y);
	}
	public Vec negate() {
		return new Vec(-x,-y);
	}
	public Vec div(int div) {
		return new Vec((x+div/2)/div,(y+div/2)/div);
	}
	public Vec mul(int div) {
		return new Vec(x*div,y*div);
	}
	public int lenSq() {
		return x*x+y*y;
	}
	public int dot(Point p) {
		return x*p.x + y*p.y;
	}
	public int cross(Point p) {
		return x*p.y - y*p.x;
	}
	
	public Vec rotateL() {
	    return new Vec(-y,x);
	}
        public Vec rotateR() {
            return new Vec(y,-x);
        }
        
        public void set(Vec v) {
            x = v.x;
            y = v.y;
        }
        
        
        /**
         * Constrains the vector of have multiples of angle.
         * @param baseAngle
         * @return a new vector with same length as this but angle is multiple of baseAngle 
         */
        public Vec constrainedVec(double baseAngle) {
            double len = Math.sqrt(x*x+y*y);
            double ang = Math.atan2(y, x);
            double ang2 = baseAngle * Math.rint(ang/baseAngle);
            float x = (float) (len * Math.cos(ang2));
            float y = (float) (len * Math.sin(ang2));
            return new Vec(Math.round(x),Math.round(y));
        }
        
	static public Vec linComb(int mul1,Vec v1,int mul2,Vec v2) {
		return new Vec(mul1*v1.x + mul2*v2.x, mul1*v1.y+mul2*v2.y);
	}

	static public Vec linComb(int mul1,Vec v1,int mul2,Vec v2,int div) {
		return new Vec((mul1*v1.x + mul2*v2.x+div/2)/div, (mul1*v1.y+mul2*v2.y+div/2)/div);
	}
	
	public void setLC(int mul1,Vec v1,int mul2,Vec v2) {
		x = mul1*v1.x + mul2*v2.x;
		y=  mul1*v1.y + mul2*v2.y;
	}
	
	
	public void setLC(int mul1,Vec v1,int mul2,Vec v2,int div) {
		x = (mul1*v1.x + mul2*v2.x)/div;
		y = (mul1*v1.y + mul2*v2.y)/div;
	}

	public void setLC(int mul1,Vec v1,int mul2,Vec v2,int mul3,Vec v3) {
		x = mul1*v1.x + mul2*v2.x + mul3*v3.x;
		y=  mul1*v1.y + mul2*v2.y + mul3*v3.y;
	}
	
	
	public void setLC(int mul1,Vec v1,int mul2,Vec v2,int mul3,Vec v3,int div) {
		x = (mul1*v1.x + mul2*v2.x + mul3*v3.x)/div;
		y = (mul1*v1.y + mul2*v2.y + mul3*v3.y)/div;
	}

	static public Vec linComb(int mul1,Vec v1,int mul2,Vec v2,int mul3,Vec v3) {
		return new Vec(mul1*v1.x + mul2*v2.x+mul3*v3.x, mul1*v1.y+mul2*v2.y+mul3*v3.y);
	}
	static public Vec linComb(int mul1,Vec v1,int mul2,Vec v2,int mul3,Vec v3,int div) {
		return new Vec((mul1*v1.x + mul2*v2.x+mul3*v3.x)/div,(mul1*v1.y+mul2*v2.y+mul3*v3.y)/div);
	}
	//@Override
    public String toString() { return "("+x+","+y+")"; }
}

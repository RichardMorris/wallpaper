package org.singsurf.wallpaper;

public class DVec {
	public double x,y;
	public DVec() { x=0; y=0; }
	public DVec(double x, double y) { this.x=x; this.y=y; }
	public DVec rotate(double angle) {
		double cos = Math.cos(angle);
		double sin = Math.sin(angle);
		return new DVec(x*cos-y*sin,x*sin+y*cos);
	}
	public DVec reflect(DVec v) {
		var dot = x*v.x+y*v.y;
		var lensq = x*x+y*y;
		return new DVec(2*dot*x/lensq - v.x, 2*dot*y/lensq - v.y);
	}
	public double angle(DVec v) {
		var cos = x * v.x + y * v.y;
		var sin = x * v.y - y * v.x;
		
		return Math.atan2(sin, cos);
	}
	public DVec add(DVec v) {
		return new DVec(x+v.x,y+v.y);
	}
}
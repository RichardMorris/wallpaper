/*
Created 9 Sep 2006 - Richard Morris
*/
package org.singsurf.wallpaper.animation;

import java.awt.Rectangle;

import org.singsurf.wallpaper.FundamentalDomain;
import org.singsurf.wallpaper.Vec;

public class RotateAnimation extends AnimationPath {
	Rectangle rect;

	int count=0;
	int sX[]=new int[6],sY[]=new int[6];
	Vec center;

	private boolean useCenter;
	public RotateAnimation(Rectangle rect, boolean useCenter) {
		this.rect =rect;
		this.useCenter = useCenter;
		center = new Vec(rect.x+rect.width/2,rect.y+rect.height/2);
	}

	@Override
	public void firstItteration(FundamentalDomain fd) {
		count=0;
		Vec O = new Vec(fd.cellVerts[1].x,fd.cellVerts[1].y);
		Vec trans = useCenter ? center.sub(O) : new Vec(0,0);
		for(int i=0;i<6;++i) {
		    sX[i]= fd.cellVerts[i].x + trans.x;
		    sY[i]= fd.cellVerts[i].y + trans.y;
		    fd.cellVerts[i].x = sX[i];
		    fd.cellVerts[i].y = sY[i];
		}
	}

	@Override
	public void nextItteration(FundamentalDomain fd) {
		double angle= (count * Math.PI)/180;
		for(int i=0;i<6;++i)
		{
			int inX = sX[i] - sX[1]; 
			int inY = sY[i] - sY[1]; 
			fd.cellVerts[i].x= sX[1] + (int) Math.rint(Math.cos(angle) * inX - Math.sin(angle)*inY);
			fd.cellVerts[i].y= sY[1] + (int) Math.rint(Math.sin(angle) * inX + Math.cos(angle)*inY);
		}
		++count;
	}

}

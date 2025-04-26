/*
Created 9 Sep 2006 - Richard Morris
*/
package org.singsurf.wallpaper.animation;

import java.awt.Rectangle;

import org.singsurf.wallpaper.FundamentalDomain;

public class RotateAnimation extends AnimationPath {
	Rectangle rect;

	int count=0;
	int sX[]=new int[6],sY[]=new int[6];

	public RotateAnimation(Rectangle rect) {
		this.rect =rect;
	}

	@Override
	public void firstItteration(FundamentalDomain fd) {
		count=0;
		for(int i=0;i<6;++i) {
		    sX[i]= fd.cellVerts[i].x;
		    sY[i]= fd.cellVerts[i].y;
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

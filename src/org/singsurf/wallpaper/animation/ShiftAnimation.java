/*
Created 9 Sep 2006 - Richard Morris
*/
package org.singsurf.wallpaper.animation;

import org.singsurf.wallpaper.FundamentalDomain;

public class ShiftAnimation extends AnimationPath {

	int dx,dy;

	public ShiftAnimation(int dx,int dy) {
		this.dx = dx;
		this.dy = dy;
	}
	int count=0;

	public void nextItteration(FundamentalDomain fd) {
		for(int i=0;i<6;++i)
		{
			fd.cellVerts[i].x += dx;
			fd.cellVerts[i].y += dy;
		}
		++count;
	}

}

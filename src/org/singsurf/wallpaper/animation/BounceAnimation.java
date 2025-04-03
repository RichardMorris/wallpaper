/*
Created 9 Sep 2006 - Richard Morris
*/
package org.singsurf.wallpaper.animation;

import java.awt.Rectangle;

import org.singsurf.wallpaper.FundamentalDomain;

public class BounceAnimation extends AnimationPath {

	int dx,dy,speed;
	private final Rectangle rect;

	public BounceAnimation(Rectangle rect,int speed) {
		this.rect = rect;
		this.dx = speed;
		this.dy = speed;
		this.speed = speed;
	}

	public void nextItteration(FundamentalDomain fd) {
		Rectangle fdBB = fd.getFDBoundingBox();
		if(fdBB.x<=rect.x) dx=speed;
		if(fdBB.y<=rect.y) dy=speed;
		if(fdBB.x+fdBB.width >= rect.x+rect.width-1) dx = -speed;
		if(fdBB.y+fdBB.height >= rect.y+rect.height-1) dy = -speed;
		for(int i=0;i<6;++i)
		{
			fd.cellVerts[i].x += dx;
			fd.cellVerts[i].y += dy;
		}
	}

}

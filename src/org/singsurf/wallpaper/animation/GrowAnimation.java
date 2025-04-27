/*
Created 9 Sep 2006 - Richard Morris
*/
package org.singsurf.wallpaper.animation;

import java.awt.Rectangle;

import org.singsurf.wallpaper.FundamentalDomain;
import org.singsurf.wallpaper.Vec;

/**
 * Animation that grows and shrinks the fundamental domain.
 * Green point is fixed
 * Other point 
 * 
 * @author Richard Morris
 */
public class GrowAnimation extends AnimationPath {
	Rectangle rect;

	int dir=1;
	Vec center;
	Vec U,V;
	double len;
	private boolean useCenter;
	private Vec O;
	private double lenU;
	public GrowAnimation(Rectangle rect, boolean useCenter) {
		this.rect =rect;
		this.useCenter = useCenter;
		center = new Vec(rect.x+rect.width/2,rect.y+rect.height/2);
	}

	@Override
	public void firstItteration(FundamentalDomain fd) {
		O = new Vec(fd.cellVerts[1].x,fd.cellVerts[1].y);
		Vec trans = useCenter ? center.sub(O) : new Vec(0,0);
		for(int i=0;i<6;++i) {
		    fd.cellVerts[i].x += trans.x;
		    fd.cellVerts[i].y += trans.y;
		}
		O = new Vec(fd.cellVerts[1].x,fd.cellVerts[1].y);
		Vec A = new Vec(fd.cellVerts[0].x,fd.cellVerts[0].y);
		Vec B = new Vec(fd.cellVerts[2].x,fd.cellVerts[2].y);
		U = A.sub(O);
		V = B.sub(O);
		lenU = Math.sqrt(U.lenSq());
		len = lenU;
		dir = 1;
	}

	@Override
	public void nextItteration(FundamentalDomain fd) {

		//len += dir;
		if(dir == 1) {
			len = Math.max(len*1.01 , len + 1.0);
		}
		else if(dir == -1) {
			len = Math.min(len/1.01 , len - 1.0);
		}
		double mul = len/lenU;
		var P = U.mul(mul);
		var Q = V.mul(mul);
		var A = O.add(P);
		var B = O.add(Q);
		fd.cellVerts[0].set(A);
		fd.cellVerts[1].set(O);
		fd.cellVerts[2].set(B);
		
		
		if(fd.getLatticeType() == FundamentalDomain.PARALLOGRAM
				|| fd.getLatticeType() == FundamentalDomain.HEXAGON) {

			Rectangle fdBB = fd.getFDBoundingBox();

			if(!rect.contains(fdBB) ) {
				dir = -1;
			}
		}
		else  {
			if(!rect.contains(O)|| !rect.contains(A)) {
				dir = -1;
			}
		}
		if(len < 40)
			dir = 1;
	}

}

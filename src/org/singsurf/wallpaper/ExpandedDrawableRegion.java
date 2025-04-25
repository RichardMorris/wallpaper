/*
Created 6 May 2007 - Richard Morris
*/
package org.singsurf.wallpaper;

import java.awt.Rectangle;

/**
 * A drawable region which fills the viewport.
 * @author Richard Morris
 *
 */
public class ExpandedDrawableRegion extends DrawableRegion {

	/**
	 * 
	 */
	public ExpandedDrawableRegion() {
		super(null);
	}

	//@Override
	public void calcDispRegion() {
//		int minX = this.viewpointL;
//		int minY = this.viewpointT;
		int maxX = this.viewpointR;
		int maxY = this.viewpointB;
		this.makeDest(maxX,maxY);
		this.dispRect = new Rectangle(0,0,maxX,maxY);
		this.makeOutImage();
		this.img_ok = true;
	}


	
	
}

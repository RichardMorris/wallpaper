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

	@Override
	public void calcDispRegion() {
		int maxX = (int) viewpointRect.getMaxX();
		int maxY = (int) viewpointRect.getMaxY();
		makeDest(maxX,maxY);
		dispRect = new Rectangle(0,0,maxX,maxY);
		makeOutImage();
		img_ok = true;
	}


	
	
}

/*
Created 8 Apr 2007 - Richard Morris
*/
package org.singsurf.wallpaper;

import java.awt.Rectangle;

/**
 * A drawable region where the destination pixels are a small portion of the parent pixels.
 * @author Richard Morris
 *
 */
public class DrawableRegionTile extends DrawableRegion {
	int outWidth,outHeight;
	public DrawableRegionTile(DrawableRegion dr,int w,int h) {
		super(dr.wall);
		dispRect = new Rectangle(0,0,w,h);
		makeDest(w, h);
		inpixels = dr.inpixels;
		srcRect = dr.srcRect;
		makeOutImage();
		img_ok = true;
	}

	public void reset() {/*do nothing */ }

}

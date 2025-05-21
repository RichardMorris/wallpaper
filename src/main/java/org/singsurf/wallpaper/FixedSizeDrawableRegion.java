/*
Created 19 May 2007 - Richard Morris
*/
package org.singsurf.wallpaper;

import java.awt.Dimension;
import java.awt.Image;
import java.awt.Rectangle;
import java.awt.image.PixelGrabber;

/**
 * A drawable region with a fixed size.
 * Images are clipped/expanded to fit.
 * @author Richard Morris
 *
 */
public class FixedSizeDrawableRegion extends DrawableRegion {

	public FixedSizeDrawableRegion(Wallpaper wall,Dimension dimension) {
		super(wall);
		dispRect = new Rectangle(0,0,dimension.width,dimension.height);
		makeDest(dimension.width, dimension.height);
		makeSrc(dimension.width, dimension.height);
	}

	@Override
	public boolean loadImage(Image imgin) {

	    img_ok=false;
	    if(imgin==null) return false;
	    int w=0,h=0;
	    int pix[]=null;


	    try {
	        PixelGrabber pg = new PixelGrabber(imgin, 0, 0, -1, -1,true);
	        pg.grabPixels();
	        w = pg.getWidth();
	        h = pg.getHeight();
	        pix = (int []) pg.getPixels();
	        System.out.println("loadImage,pg "+w+" "+h);
	    } 
	    catch (InterruptedException e) 
	    {
	        System.out.println("interrupted waiting for pixels!");
	        return false;
	    }
	    System.out.println("FSDR "+w+" "+h+" "+dispRect);
	    // If bigger than screen rescale to fit
	    if( dispRect.width == 0 || dispRect.height == 0)
	    {
	        
	    }
	    else
	    if(w > dispRect.width || h > dispRect.height) {
	        double xfactor = (double) dispRect.width / w;
	        double yfactor = (double) dispRect.height / h;
	        double factor = (xfactor > yfactor ? yfactor : xfactor);
	        int nw = (int) (factor * w);
	        int nh = (int) (factor * h);
	        Image img = imgin.getScaledInstance(nw,nh,Image.SCALE_AREA_AVERAGING);
	        return loadImage(img);
	    }


	    int offX = (srcRect.width - w ) / 2;
	    int offY = (srcRect.height - h ) / 2;

	    for(int i=0;i<inpixels.length;++i)
	        inpixels[i] = backgroundRGB;
	    for(int j=0;j<h;++j) {
	        System.arraycopy(pix,j*w,inpixels, offX + (offY+j)*srcRect.width,w);
	    }
	    copySrcDest();
	    makeOutImage();
	    if(DEBUG) System.out.println("loadImage successful: Width "+w+" height "+h);
	    img_ok = true;

	    return img_ok;

	}

	public void reset() {
		if(!img_ok) return;
		//System.out.println("reload");
		System.arraycopy(inpixels,0,pixels,0,inpixels.length);
		source.newPixels();
	}
}

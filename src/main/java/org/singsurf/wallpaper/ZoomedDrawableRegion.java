/*
Created 9 Apr 2007 - Richard Morris
 */
package org.singsurf.wallpaper;

import java.awt.Image;
import java.awt.Rectangle;
import java.awt.Toolkit;
import java.awt.image.AreaAveragingScaleFilter;
import java.awt.image.FilteredImageSource;
import java.awt.image.ImageFilter;
import java.awt.image.ImageObserver;
import java.awt.image.ImageProducer;
import java.awt.image.MemoryImageSource;
import java.awt.image.PixelGrabber;
import java.awt.image.ReplicateScaleFilter;

import org.singsurf.wallpaper.tessrules.BasicRule;
import org.singsurf.wallpaper.tessrules.TessRule;

public class ZoomedDrawableRegion extends DrawableRegion {
    public int zoomDenom;
    public int zoomNumer;
    /** Pixels of the base unzoomed image */
    int[] basePixels;
    boolean split =false;
    public ZoomedDrawableRegion(Wallpaper wall) {
        super(wall);
        zoomNumer = 1;
        zoomDenom = 1;
    }

    /**
     * Loads the image into the basePixels array,
     * and sets the baseRect to the size of the image.
     * Calculates the zoomed image and the display region.
     */
        @Override
        public boolean loadImage(Image imgin) {
        	var success = loadImageCore(imgin);
            if(!success) {
				return false;
			}
            
            calcZoomedImages();
            calcDispRegion();
            makeOutImage();
            img_ok = true;
            return img_ok;
        }
        
    	/**
    	 * Loads the image into the basePixels array,
    	 * and sets the baseRect to the size of the image.
    	 * @param imgin
    	 * @return
    	 */
    	public boolean loadImageCore(Image imgin) {
    	img_ok=false;
    	int w=0,h=0;
    	try {
    	    PixelGrabber pg = new PixelGrabber(imgin, 0, 0, -1, -1,true);
    	    //PixelGrabber pg = new PixelGrabber(img, 0, 0, w, h,inpixels,0,w);
    	    if(DEBUG) System.out.println("ZDR: loadImage");
    	    try
    	    {
    	        pg.grabPixels();
    	    } 
    	    catch (InterruptedException e) 
    	    {
    	        System.out.println("interrupted waiting for pixels!");
    	        return false;
    	    }
    	    if(DEBUG) System.out.println("OK");    
    	    if ((pg.getStatus() & ImageObserver.ABORT) != 0) {
    	        System.out.println("Error loading image");
    	        return false;
    	    }
    	    if(DEBUG) System.out.println("grabbed");
    	    basePixels = (int []) pg.getPixels();
    	    try
    	    {
    	        w=pg.getWidth();
    	        h=pg.getHeight();	
    	        if(DEBUG) System.out.println("w "+w+" "+h+" "+basePixels.length);
    	    }
    	    catch(Exception e)
    	    {
    	        System.out.println("Error copying the image array");
    	        System.out.println(e.getMessage());
    	        return false;
    	    }
    	    //width=pg.getWidth();
    	    //height=pg.getHeight();	
    	    if(DEBUG) System.out.println("creating source");
    	    baseRect = new Rectangle(0,0,w,h);
    	}
    	catch(OutOfMemoryError e) {
    	    reportMemoryError(e,w*h*(1+2*(zoomNumer/zoomDenom)));
    	}
    	img_ok = true;
    	return img_ok;
    	
    	}


    boolean grabSrcPixels(ImageProducer imgin) {
        img_ok=false;

        PixelGrabber pg = new PixelGrabber(imgin, 0, 0,
                srcRect.width,srcRect.height,
                inpixels,0,srcRect.width);
        if(DEBUG) System.out.println("OK");
        try
        {
            pg.grabPixels();
        } 
        catch (InterruptedException e) 
        {
            System.out.println("interrupted waiting for pixels!");
            return false;
        }
        if(DEBUG) System.out.println("OK");    
        if ((pg.getStatus() & ImageObserver.ABORT) != 0) {
            System.out.println("Error loading image");
            return false;
        }
        if(DEBUG) System.out.println("grabbed");
        img_ok = true;
        return true;
    }

    public void calcZoomedImages() {
        if(zoomNumer == 1 && zoomDenom == 1) {
            makeSrc(baseRect.width,baseRect.height);
            System.arraycopy(basePixels,0,inpixels,0,baseRect.width*baseRect.height);
        }
        else if(zoomNumer ==1) {
            MemoryImageSource mis = new MemoryImageSource(baseRect.width,baseRect.height,basePixels, 0, baseRect.width);
            ImageFilter scale = new AreaAveragingScaleFilter(baseRect.width/zoomDenom,baseRect.height/zoomDenom);
            ImageProducer prod = new FilteredImageSource(mis,scale);
            makeSrc(baseRect.width/zoomDenom,baseRect.height/zoomDenom);
            grabSrcPixels(prod);
        }
        else if(zoomDenom ==1) {
            MemoryImageSource mis = new MemoryImageSource(
                    baseRect.width,baseRect.height,basePixels, 0, baseRect.width);
            ImageFilter scale = new ReplicateScaleFilter(
                    baseRect.width*zoomNumer,baseRect.height*zoomNumer);
            ImageProducer prod = new FilteredImageSource(mis,scale);
            makeSrc(baseRect.width*zoomNumer,baseRect.height*zoomNumer);
            grabSrcPixels(prod);
        }
        else {
			MemoryImageSource mis = new MemoryImageSource(
					baseRect.width,baseRect.height,basePixels, 0, baseRect.width);
			ImageFilter scale = new AreaAveragingScaleFilter(
					baseRect.width*zoomNumer/zoomDenom,baseRect.height*zoomNumer/zoomDenom);
			ImageProducer prod = new FilteredImageSource(mis,scale);
			makeSrc(baseRect.width*zoomNumer/zoomDenom,baseRect.height*zoomNumer/zoomDenom);
			grabSrcPixels(prod);
		}
        makeDest(baseRect.width*zoomNumer/zoomDenom,baseRect.height*zoomNumer/zoomDenom);
        copySrcDest();
        makeOutImage();

    }


    public void zoom(int numerator,int denom) {
        // nothing to do
        try {
            zoomDenom = denom;
            zoomNumer = numerator;
            calcZoomedImages();
        }
        catch(OutOfMemoryError e) {
            reportMemoryError(e,baseRect.width*baseRect.height*(1+2*(zoomNumer/zoomDenom)));
        }

    }

    @Override
    public void flip(String code) {
        int oh = baseRect.height;
        int ow = baseRect.width;
        int w = oh;
        int h = ow;
        try {
            if(code.equals(Wallpaper.FLIP_X)) {
                for(int j=0;j<oh;++j)
                    for(int i=0;i<ow/2;++i) {
                        int indexA = i + j * ow;
                        int indexB = (ow-1 - i) + j * ow;
                        int pixA = basePixels[indexA];
                        int pixB = basePixels[indexB];
                        basePixels[indexA] = pixB;
                        basePixels[indexB] = pixA;
                    }
            }
            else if(code.equals(Wallpaper.FLIP_Y)) {
                for(int j=0;j<oh/2;++j)
                    for(int i=0;i<ow;++i) {
                        int indexA = i + j * ow;
                        int indexB = i + (oh-1-j) * ow;
                        int pixA = basePixels[indexA];
                        int pixB = basePixels[indexB];
                        basePixels[indexA] = pixB;
                        basePixels[indexB] = pixA;
                    }
            }
            else if(code.equals(Wallpaper.FLIP_90)) {
                int tmpPix[] = new int[oh*ow];
                for(int j=0;j<h;++j)
                    for(int i=0;i<w;++i) {
                        tmpPix[i+j*w] = basePixels[j+(oh-1-i)*ow];

                    }
                basePixels = tmpPix;
                baseRect = new Rectangle(0,0,w,h);
            }
            else if(code.equals(Wallpaper.FLIP_180)) {
                for(int j=0;j<oh/2;++j)
                    for(int i=0;i<ow;++i) {
                        int indexA = i + j * ow;
                        int indexB = (ow-1-i) + (oh-1-j) * ow;
                        int pixA = basePixels[indexA];
                        int pixB = basePixels[indexB];
                        basePixels[indexA] = pixB;
                        basePixels[indexB] = pixA;
                    }
                if((oh%2)==1) {
                    int j = oh/2;
                    for(int i=0;i<ow/2;++i) {
                        int indexA = i + j * ow;
                        int indexB = (ow-1-i) + (oh-1-j) * ow;
                        int pixA = basePixels[indexA];
                        int pixB = basePixels[indexB];
                        basePixels[indexA] = pixB;
                        basePixels[indexB] = pixA;
                    }
                }	
            }
            else if(code.equals(Wallpaper.FLIP_270)) {
                int tmpPix[] = new int[oh*ow];
                for(int j=0;j<h;++j)
                    for(int i=0;i<w;++i)
                        tmpPix[i+j*w] = basePixels[(ow-1-j)+i*ow];
                basePixels = tmpPix;
                baseRect = new Rectangle(0,0,w,h);

            }
            else { // flip x-y
                for(int j=0;j<h;++j)
                    for(int i=0;i<w;++i)
                        pixels[i+j*w] = inpixels[j+i*ow];
                destRect = new Rectangle(0,0,w,h);
                srcRect = new Rectangle(0,0,w,h);
            }
            calcZoomedImages();
        }
        catch(OutOfMemoryError e) {
            reportMemoryError(e,baseRect.width*baseRect.height*(1+2*(zoomNumer/zoomDenom)));
        }

    }

    @Override
    public void rescale(int w, int h) {
        try {
            MemoryImageSource mis = new MemoryImageSource(
                    baseRect.width,baseRect.height,basePixels, 0, baseRect.width);
            ImageFilter scale = new AreaAveragingScaleFilter(w,h);
            ImageProducer prod = new FilteredImageSource(mis,scale);
            Image img = Toolkit.getDefaultToolkit().createImage(prod);
            loadImage(img);
        }
        catch(OutOfMemoryError e) {
            reportMemoryError(e,baseRect.width*baseRect.height*(1+2*(zoomNumer/zoomDenom)));
        }

    }

    @Override
    public void resize(int w, int h, int xoff, int yoff) {
        try {
            int tmpPix[] = new int[w*h];
            makeDest(w,h);
            if(TessRule.tileBackground) {
                for(int i=0;i<w;++i)
                    for(int j=0;j<h;++j) {
                        int sx = (i-xoff) % baseRect.width; if(sx<0) sx+= baseRect.width;
                        int sy = (j-yoff) % baseRect.height; if(sy<0) sy+= baseRect.height;
                        tmpPix[i+j*w] = basePixels[sx+sy*baseRect.width];
                    }
            }
            else {
                for(int i=0;i<w;++i)
                    for(int j=0;j<h;++j)
                        tmpPix[i+j*w]=backgroundRGB;
                for(int i=0;i<baseRect.width;++i)
                    for(int j=0;j<baseRect.height;++j) {
                        int x = i+xoff;
                        int y = j+yoff;
                        if(x>=0 && x<w && y>=0 && y<h)
                            tmpPix[x+y*w] = basePixels[i+j*baseRect.width];
                    }
            }
            basePixels = tmpPix;
            baseRect = new Rectangle(0,0,w,h);
            calcZoomedImages();
        }
        catch(OutOfMemoryError e) {
            reportMemoryError(e,baseRect.width*baseRect.height*(1+2*(zoomNumer/zoomDenom)));
        }

    }

    @Override
	public
	void calcDispRegion() {
		int minX = viewpointRect.x;
		int minY = viewpointRect.y;
		int maxX = (destRect.width > viewpointRect.getMaxX() ? (int) viewpointRect.getMaxX() : destRect.width);
		int maxY = (destRect.height > viewpointRect.getMaxY() ? (int) viewpointRect.getMaxY() : destRect.height);
		int width = maxX-minX;
		int height = maxY-minY;
		if(split) {
			dispRect = new Rectangle(minX+width/2,minY,width/2,height);
		} else {
			dispRect = new Rectangle(minX,minY,width,height);
		}
		if(DEBUG) System.out.println(""+minX+" "+minY+" "+maxX+" "+maxY);
    	if(DEBUG) System.out.println("CDR w "+destRect.width+" h "+destRect.height+" ");
		if(DEBUG) System.out.println("vp "+viewpointRect);
	}

	public void setSplit(boolean b) {
		split = b;
	}

	public void reset() {
		if(!img_ok) return;
		//System.out.println("reload");
		if(baseRect == srcRect) {
			System.arraycopy(inpixels,0,pixels,0,inpixels.length);
			source.newPixels();
		} else {
			wall.controller.applyTessellation(BasicRule.id);
		}
	}


}

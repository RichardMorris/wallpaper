/*
Created 2 Apr 2007 - Richard Morris
*/
package org.singsurf.wallpaper;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Toolkit;
import java.awt.image.AreaAveragingScaleFilter;
import java.awt.image.FilteredImageSource;
import java.awt.image.ImageFilter;
import java.awt.image.ImageObserver;
import java.awt.image.ImageProducer;
import java.awt.image.MemoryImageSource;
import java.awt.image.PixelGrabber;

import org.singsurf.wallpaper.tessrules.TessRule;

public class DrawableRegion {
	static final boolean DEBUG = true;
	
	/** Un-zoomed rectangle */ 
	public Rectangle baseRect;
	/** Zoomed rectangle for source pixels */
	public Rectangle srcRect;
	/** Zoomed rectangle for destination pixels */
	public Rectangle destRect=new Rectangle(0,0,100,100);
	/** Display area */
	public Rectangle dispRect;
	/** Offset for actual coordinates from image coordinates. */
	public Point offset = new Point(0,0);
	/** area actually drawn */
	//int minX,maxX,minY,maxY;
	/** size of image */
	//int width,height;
	/** offset used creating an expanded image */
	//int offsetX=0,offsetY=0;
	
	int backgroundRGB = Color.black.getRGB();
	
	/** displayable area */
	int viewpointL,viewpointR,viewpointT,viewpointB;

	/** pixels of underlying image */
	public int[] inpixels;
	/** pixels of work image */
	public int[] pixels;
	protected MemoryImageSource source;

	/** Output image */
	Image outImage;

	/** Status */
	public boolean img_ok=false;

	/** Wallpaper instance */
	Wallpaper wall=null;

	public DrawableRegion(Wallpaper wall) {
		this.wall=wall;
	}


	public void calcDispRegion() {
		int minX = viewpointL;
		int minY = viewpointT;
		int maxX = (destRect.width > viewpointR ? viewpointR : destRect.width);
		int maxY = (destRect.height > viewpointB ? viewpointB : destRect.height);
		dispRect = new Rectangle(minX,minY,maxX-minX,maxY-minY);
		if(DEBUG) System.out.println("CDR w "+destRect.width+" h "+destRect.height+" ");
		if(DEBUG) System.out.println("vp "+viewpointL+" "+viewpointT+" "+viewpointR+" "+viewpointB);
		if(DEBUG) System.out.println(""+minX+" "+minY+" "+maxX+" "+maxY);
	}

	public void fillSource() {
		if(!img_ok) return;
		//System.out.println("fillSource");
		source.newPixels(dispRect.x,dispRect.y,dispRect.width,dispRect.height);
	}
	
	public void reset() {
		if(!img_ok) return;
		//System.out.println("reloadDelayed");
		System.arraycopy(inpixels,0,pixels,0,inpixels.length);
	}

	public void resetDelayed() {
		if(!img_ok) return;
		//System.out.println("reloadDelayed");
		try {
			System.arraycopy(inpixels,0,pixels,0,inpixels.length);
		} catch (ArrayIndexOutOfBoundsException e) {
			System.out.println("ArrayIndexOutOfBoundsException "+e.getMessage());
			System.out.println("inpixels "+inpixels.length+" pixels "+pixels.length);
			System.out.println("srcRect "+srcRect.width+" "+srcRect.height);
			System.out.println("destRect "+destRect.width+" "+destRect.height);
			System.out.println("offset "+offset.x+" "+offset.y);
			System.out.println("viewpoint "+viewpointL+" "+viewpointT+" "+viewpointR+" "+viewpointB);
			System.out.println("dispRect "+dispRect.x+" "+dispRect.y+" "+dispRect.width+" "+dispRect.height);
		}
	}

	
	
	public boolean loadImage(Image imgin) {
		img_ok=false;
		int w=0,h=0;

		try {
			PixelGrabber pg = new PixelGrabber(imgin, 0, 0, -1, -1,true);
			//PixelGrabber pg = new PixelGrabber(img, 0, 0, w, h,inpixels,0,w);
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
			inpixels = (int []) pg.getPixels();
			pixels = new int[inpixels.length];
			try
			{
				w=pg.getWidth();
				h=pg.getHeight();	
				if(DEBUG) System.out.println("w "+w+" "+h+" "+inpixels.length);
				copySrcDest();
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

			srcRect = new Rectangle(0,0,w,h);
			destRect = new Rectangle(0,0,w,h);
			calcDispRegion();
			makeOutImage();
			if(DEBUG) System.out.println("loadImage successful: Width "+w+" height "+h);
			img_ok = true;
		}
		catch(OutOfMemoryError e) {
			reportMemoryError(e,w*h*2);
		}
		return img_ok;
	}

	public boolean isGoodImage() {
		return img_ok;
	}
	public Image getActiveImage() {
		if(source!=null)
			return outImage;
		return null;
	}
	public void setViewport(Rectangle r) {
	    if(DEBUG)	    System.out.println("DR.setViewport "+r);
		viewpointL = r.x-offset.x;
		viewpointR = r.x+r.width-offset.x;
		viewpointT = r.y-offset.y;
		viewpointB = r.y + r.height-offset.y;
		calcDispRegion();
	}

	protected void makeSrc(int w,int h) {
		srcRect = new Rectangle(0,0,w,h);
		inpixels = new int[w*h];
	}

	public void makeDest(int w,int h) {
		destRect = new Rectangle(0,0,w,h);
		pixels = new int[w*h];
	}
	
	protected void copySrcDest() {
		System.arraycopy(inpixels,0,pixels,0,inpixels.length);
	}

	protected void copyDestSrc() {
		System.arraycopy(pixels,0,inpixels,0,pixels.length);
	}

	public void makeOutImage() {
	    if(DEBUG) System.out.println("makeOutImage");
		source = new MemoryImageSource(destRect.width, destRect.height, pixels, 0, destRect.width);
		source.setAnimated(true);
		outImage = Toolkit.getDefaultToolkit().createImage(source);
	}

	public void resize(int w, int h, int xoff, int yoff) {
		try {
//			System.gc();
			makeDest(w,h);
			if(TessRule.tileBackground) {
				for(int i=0;i<w;++i)
					for(int j=0;j<h;++j) {
						int sx = (i-xoff) % srcRect.width; if(sx<0) sx+= srcRect.width;
						int sy = (j-yoff) % srcRect.height; if(sy<0) sy+= srcRect.height;
						pixels[i+j*w] = inpixels[sx+sy*srcRect.width];
					}
			}
			else {
				for(int i=0;i<w;++i)
					for(int j=0;j<h;++j)
						pixels[i+j*w]=backgroundRGB;
				for(int i=0;i<srcRect.width;++i)
					for(int j=0;j<srcRect.height;++j) {
						int x = i+xoff;
						int y = j+yoff;
						if(x>=0 && x<w && y>=0 && y<h)
							pixels[x+y*w] = inpixels[i+j*destRect.width];
					}
			}
			makeSrc(w,h);
			copyDestSrc();

			calcDispRegion();
			makeOutImage();
			img_ok = true;
		}
		catch(OutOfMemoryError e) {
			reportMemoryError(e,w*h*2);
		}
	}


	public void rescale(int w, int h) {
		try {
		MemoryImageSource mis = new MemoryImageSource(srcRect.width,srcRect.height,inpixels, 0, srcRect.width);
		ImageFilter scale = new AreaAveragingScaleFilter(w,h);
		ImageProducer prod = new FilteredImageSource(mis,scale);
		Image img = Toolkit.getDefaultToolkit().createImage(prod);
		loadImage(img);
		}
		catch(OutOfMemoryError e) {
			reportMemoryError(e,w*h*2);
		}
	}

	
	public void flip(String code) {
		// rotated width and height
		int w = destRect.height,h=destRect.width;
		// original width and height
		int ow = destRect.width,oh=destRect.height;
		try {
		//		System.out.println("flip " + code);
		if(code.equals(Wallpaper.FLIP_X)) {
			for(int j=0;j<oh;++j)
				for(int i=0;i<ow;++i) {
					pixels[i+j*ow] = inpixels[(ow-1-i)+j*ow];
				}
		}
		else if(code.equals(Wallpaper.FLIP_Y)) {
			for(int j=0;j<oh;++j)
				for(int i=0;i<ow;++i) {
					pixels[i+j*ow] = inpixels[i+(oh-1-j)*ow];
				}
		}
		else if(code.equals(Wallpaper.FLIP_90)) {
			for(int j=0;j<h;++j)
				for(int i=0;i<w;++i)
					pixels[i+j*w] = inpixels[j+(oh-1-i)*ow];
			destRect = new Rectangle(0,0,w,h);
			srcRect = new Rectangle(0,0,w,h);
		}
		else if(code.equals(Wallpaper.FLIP_180)) {
			for(int j=0;j<oh;++j)
				for(int i=0;i<ow;++i) {
					pixels[i+j*ow] = inpixels[(ow-1-i)+(oh-1-j)*ow];
				}
		}
		else if(code.equals(Wallpaper.FLIP_270)) {
			for(int j=0;j<h;++j)
				for(int i=0;i<w;++i)
					pixels[i+j*w] = inpixels[(ow-1-j)+i*ow];
			destRect = new Rectangle(0,0,w,h);
			srcRect = new Rectangle(0,0,w,h);
		}
		else { // flip x-y
			for(int j=0;j<h;++j)
				for(int i=0;i<w;++i)
					pixels[i+j*w] = inpixels[j+i*ow];
			destRect = new Rectangle(0,0,w,h);
			srcRect = new Rectangle(0,0,w,h);
		}

		copyDestSrc();
		calcDispRegion();
		makeOutImage();
		img_ok = true;
		}
		catch(OutOfMemoryError e) {
			reportMemoryError(e,w*h*2);
		}

	}
	
	public void paint(Graphics g,Wallpaper wall) {
//	    if(DEBUG) System.out.println("DR:paint");
		try {
			g.drawImage(getActiveImage(),offset.x,offset.y,wall);
		}
		catch(OutOfMemoryError e) {
			reportMemoryError(e,destRect.width*destRect.height);
		}
		
		return;
	}
	
	protected void reportMemoryError(OutOfMemoryError e,int reqSize) {
		if(wall!=null) wall.setText("Out of memory");
		System.err.print("Out of memory: '"+e.getMessage());
		System.err.println("' while resizing image");
		//System.err.println("MaxMemory "+Runtime.getRuntime().maxMemory());
		System.err.println("FreeMemory "+Runtime.getRuntime().freeMemory());
		System.err.println("This image will require "+(reqSize*4)+" bytes");
		//System.err.println("TotalMemory "+Runtime.getRuntime().totalMemory());
		System.err.println("Rerun application with the -Xmx512m VM flag to assign more memory");
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
	    inpixels = (int []) pg.getPixels();
	    try
	    {
	        w=pg.getWidth();
	        h=pg.getHeight();	
	        if(DEBUG) System.out.println("w "+w+" "+h+" "+inpixels.length);
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
	    reportMemoryError(e,w*h);
	}
	return img_ok;
	
	}

}

/*
Created 18 May 2007 - Richard Morris
*/
package org.singsurf.wallpaper;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.image.BufferedImage;

public class DefaultImage {
	private static final int NUM_CIRCLES = 40;
    /** Size of default image */
    static final int DEFAULT_SIZE=400;
    static final int MIN_SIZE=10;
    
	   static Image createDefaultImage() {
	       BufferedImage img = new BufferedImage(DEFAULT_SIZE,DEFAULT_SIZE,BufferedImage.TYPE_INT_RGB);
	    	Graphics g = img.getGraphics();
	    	g.setColor(Color.black);
	    	g.fillRect(0,0,DEFAULT_SIZE,DEFAULT_SIZE);
	    	for(int i=0;i<NUM_CIRCLES;++i) {
	    		float r = (float) Math.random();
	    		float gr = (float) Math.random();
	    		float b = (float) Math.random();
	    		g.setColor(new Color(r,gr,b));
	    		int x = (int) (Math.random() * DEFAULT_SIZE);
	    		int y = (int) (Math.random() * DEFAULT_SIZE);
	    		int w = (int) (Math.random() * DEFAULT_SIZE/4 + MIN_SIZE);
//	    		int min =x; 
//	   		if(y<min) min=y;
//	    		if(DEFAULT_SIZE-x<min) min = DEFAULT_SIZE-x;
//	    		if(DEFAULT_SIZE-y<min) min = DEFAULT_SIZE-y;

	    		g.fillOval(x-DEFAULT_SIZE, y-DEFAULT_SIZE, w,w);
	    		g.fillOval(x-DEFAULT_SIZE, y			 , w,w);
	    		g.fillOval(x-DEFAULT_SIZE, y+DEFAULT_SIZE, w,w);
	    		
	    		g.fillOval(x, y-DEFAULT_SIZE, w,w);
	    		g.fillOval(x, y             , w,w);
	    		g.fillOval(x, y+DEFAULT_SIZE, w,w);

	    		g.fillOval(x+DEFAULT_SIZE, y-DEFAULT_SIZE, w,w);
	    		g.fillOval(x+DEFAULT_SIZE, y			 , w,w);
	    		g.fillOval(x+DEFAULT_SIZE, y+DEFAULT_SIZE, w,w);
	    	}
	    	g.dispose();
	    	return img;
	    }

}

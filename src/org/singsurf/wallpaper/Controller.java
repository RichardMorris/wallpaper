/*
Created 2 Apr 2007 - Richard Morris
 */
package org.singsurf.wallpaper;

import java.awt.Rectangle;

import org.singsurf.wallpaper.tessrules.TessRule;

public class Controller {

    Wallpaper wallpaper=null;
    TessRule tr=null;
    DrawableRegion dr=null;
    FundamentalDomain fd=null;
    boolean showingOriginal = true;
    boolean constrainVertices = false;
    /**
     * @param wallpaper
     * @param dr
     */
    public Controller(Wallpaper wallpaper, DrawableRegion dr,FundamentalDomain fd) {
        this.wallpaper = wallpaper;
        this.dr = dr;
        this.fd = fd;
    }

    public void setDr(DrawableRegion dr) {
        this.dr = dr;
    }

    public void setFd(FundamentalDomain fd) {
        this.fd = fd;
    }

    /**
     * Performs the given tessellation. First works out
     * coordinate system, i.e. the origin and vectors (1,0) and (0,1).
     * Then calls the ate function with self as callback parameter.
     */
    public void applyTessellation() {
        if(this.constrainVertices)
            tr.constrainVertices(fd.cellVerts, this.wallpaper.curvertex);
        tr.calcFrame(fd,this.wallpaper.curvertex, this.constrainVertices);
        tr.fixVerticies(fd);
        tr.calcFund(fd);
        if(showCoords)
            setText(fd.toString(dr));
        tr.replicate(dr,fd);
        if(this.showingOriginal) {
            wallpaper.origTileButton.setText("Original Image");
        }
        showingOriginal = false;
        repaint();
    }

    /** Apply the tessellation to the full image 
     * @param dr */
    public void applyFull(DrawableRegion dr) {
        Rectangle  oldRect = dr.dispRect;
        dr.dispRect = dr.destRect;
        DrawableRegion oldDr = this.dr;
        this.dr = dr;
        applyTessellation();
        this.dr = oldDr;
        dr.dispRect = oldRect;
    }
    /**
     * Calculate geometry but not image.
     */
    public void calcGeom()
    {
        if(this.constrainVertices)
            tr.constrainVertices(fd.cellVerts, this.wallpaper.curvertex);
        // first need to ensure we've got a rectangle
        tr.calcFrame(fd,this.wallpaper.curvertex, this.constrainVertices);
        tr.fixVerticies(fd);
        tr.calcFund(fd);
        if(showCoords)
            setText(fd.toString(dr));
    }

    /** 
     * Show original image.
     */
    public void showOriginal() {
        dr.reset();
        if(!this.showingOriginal) {
            wallpaper.origTileButton.setText("Tile image");
        }
        showingOriginal = true;
        repaint();
    }

    /** Redraw the current image, applying tessellation if necessary. */
    public void redraw() {
        if(!showingOriginal)
            applyTessellation();
        else
            showOriginal();

    }

    public void repaint() {
        this.wallpaper.myCanvas.repaint();
    }

    public void setTesselation(TessRule tr) {
        this.tr = tr;
        tr.firstCall = true;
        dr.resetDelayed();
    }

    public void setText(String message) {
        this.wallpaper.setText(message);

    }

    boolean showCoords=false;
    public void setShowCoords(boolean b) {
        showCoords=b;

    }

	public void split(boolean b) {
		if(dr instanceof ZoomedDrawableRegion) {
			((ZoomedDrawableRegion)dr).setSplit(b);
			dr.calcDispRegion();
			showOriginal();
			applyTessellation();
//			calcGeom();
			//dr.makeOutImage();
//			redraw();
		}
	}
}

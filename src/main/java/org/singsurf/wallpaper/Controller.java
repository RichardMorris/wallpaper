/*
Created 2 Apr 2007 - Richard Morris
 */
package org.singsurf.wallpaper;

import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Rectangle;
import java.awt.Toolkit;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.image.BufferedImage;
import java.io.IOException;

import org.singsurf.wallpaper.WallpaperFramed.ImageSelection;
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

    public void setDr(DrawableRegion dr1) {
        dr = dr1;
    }

    public void setFd(FundamentalDomain fd1) {
        fd = fd1;
    }

    /**
     * Performs the given tessellation. First works out
     * coordinate system, i.e. the origin and vectors (1,0) and (0,1).
     * Then calls the ate function with self as callback parameter.
     */
    public void applyTessellation() {
        if(constrainVertices)
            tr.constrainVertices(fd.cellVerts, wallpaper.curvertex);
        tr.calcFrame(fd,wallpaper.curvertex, constrainVertices);
        tr.fixVerticies(fd);
        tr.calcFund(fd);
        if(showCoords)
            setText(fd.toString(dr));
        applyTessellation(tr);
        if(showingOriginal) {
            wallpaper.origTileButton.setText("Original Image");
        }
        showingOriginal = false;
        repaint();
    }

	public void applyTessellation(TessRule tr2) {
		tr2.replicate(dr,fd);
	}

    /** Apply the tessellation to the full image 
     * @param dr */
    public void applyFull() {
        Rectangle  oldRect = dr.dispRect;
        dr.dispRect = dr.destRect;
        applyTessellation();
        dr.dispRect = oldRect;
    }
    /**
     * Calculate geometry but not image.
     */
    public void calcGeom()
    {
        if(constrainVertices)
            tr.constrainVertices(fd.cellVerts, wallpaper.curvertex);
        // first need to ensure we've got a rectangle
        tr.calcFrame(fd,wallpaper.curvertex, constrainVertices);
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
        if(!showingOriginal) {
            wallpaper.origTileButton.setText("Tile image");
        }
        showingOriginal = true;
        repaint();
    }

    /** Redraw the current image, applying tessellation if necessary. */
    public void redraw() {
        if(showingOriginal)
            showOriginal();
        else
            applyTessellation();

    }

    public void repaint() {
        wallpaper.myCanvas.repaint();
    }

    public void setTesselation(TessRule tr1) {
        tr = tr1;
        tr.firstCall = true;
        dr.resetDelayed();
    }

    public void setText(String message) {
        wallpaper.setText(message);

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
		}
	}

	public FundamentalDomain getFD() {
		return fd;
	}

	void flip(String com) {
	    fd.flip(com,dr.destRect.width,dr.destRect.height,tr);
	    dr.flip(com);
	    wallpaper.imageChanged();
	}

	protected void copy() {
		if(!showingOriginal) {
			applyFull();
		}
	    copyImageToClipboard(dr.getActiveImage());
	}

    protected void paste() {
        Image img = getClipboardImage();
        if (img != null && dr.loadImage(img)) {
            wallpaper.imageChanged();
        }
    }

	protected void copyFull() {
	    copyFullImageToClipboard(dr.getActiveImage());
	}

	private void copyFullImageToClipboard(Image image) {
	    // Work around a Sun bug that causes a hang in "sun.awt.image.ImageRepresentation.reconstruct".
	    new javax.swing.ImageIcon(image); // Force load.
	    BufferedImage newImage = new BufferedImage(image.getWidth(null), image.getHeight(null), BufferedImage.TYPE_INT_ARGB);
	
	    Graphics2D g = newImage.createGraphics();
	    g.setClip(0, 0, image.getWidth(null), image.getHeight(null));
	    wallpaper.paintCanvas(g);
	    //	          g.drawImage(image, 0, 0, null);
	    //	          fd.paintSymetries(g, controller.tr);
	
	    ImageSelection imageSelection = new ImageSelection(newImage);
	    Toolkit toolkit = Toolkit.getDefaultToolkit();
	    toolkit.getSystemClipboard().setContents(imageSelection, null);
	}

	private void copyImageToClipboard(Image image) {
	    // Work around a Sun bug that causes a hang in "sun.awt.image.ImageRepresentation.reconstruct".
	    new javax.swing.ImageIcon(image); // Force load.
	    BufferedImage newImage = new BufferedImage(image.getWidth(null), image.getHeight(null), BufferedImage.TYPE_INT_ARGB);
	
	    Graphics2D g = newImage.createGraphics();
	    g.setClip(0, 0, image.getWidth(null), image.getHeight(null));
	    g.drawImage(image, 0, 0, null);
	    System.out.println("copyImageToClipboard "+image.getWidth(null)+" "+image.getHeight(null));
	    ImageSelection imageSelection = new ImageSelection(newImage);
	    Toolkit toolkit = Toolkit.getDefaultToolkit();
	    toolkit.getSystemClipboard().setContents(imageSelection, null);
	}

	private Image getClipboardImage() {
	    Transferable t = Toolkit.getDefaultToolkit().getSystemClipboard().getContents(null);
	
	    try {
	        if (t != null && t.isDataFlavorSupported(DataFlavor.imageFlavor)) {
	            Image text = (Image)t.getTransferData(DataFlavor.imageFlavor);
	            return text;
	        }
	    } catch (UnsupportedFlavorException e) {/*ignore*/
	    } catch (IOException e) {/*ignore*/
	    }
	    return null;
	}
}

/*
Created 2 Apr 2007 - Richard Morris
 */
package org.singsurf.wallpaper;

import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Polygon;
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
    enum PaintStyle { TESS, ORIG, DOMAIN, TILE, SPLIT } 
    PaintStyle style = PaintStyle.ORIG;

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
    private void showOriginal() {
        dr.reset();
        repaint();
    }

    /** Redraw the current image, applying tessellation if necessary. */
    public void redraw() {
//    	System.out.println("redraw "+clickCount+" "+style);
    	switch(style) {
		case TESS:
            applyTessellation();
			break;
		case ORIG:
            showOriginal();
			break;
		case DOMAIN:
        	showIsolatedDomain();
			break;
		case TILE:
        	showIsolatedTile();
			break;
		case SPLIT:
			showOriginal();
			applyTessellation();
			break;
		default:
			break;    	
    	}
    }


	public void repaint() {
        wallpaper.myCanvas.repaint();
    }

    public void setTesselation(TessRule tr1) {
        tr = tr1;
        tr.firstCall = true;
        setText(tr.message);
        dr.resetDelayed();
        wallpaper.tickCheckbox(tr.name);
    }

    public void setText(String message) {
        wallpaper.setText(message);

    }

    boolean showCoords=false;
	//    protected URL imageURL=null;
	
	private int clickCount = 0;
	
    public void setShowCoords(boolean b) {
        showCoords=b;

    }

	public void split(boolean b) {
		if(dr instanceof ZoomedDrawableRegion) {
			((ZoomedDrawableRegion)dr).setSplit(b);
			dr.calcDispRegion();
		}
		style = b ? PaintStyle.SPLIT : PaintStyle.TESS;
		redraw();
        wallpaper.setViewCheckboxes();
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
//		if(!showingOriginal) {
//			applyFull();
//		}
		redraw();
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
	    wallpaper.controller.paintCanvas(wallpaper, g);
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

	public void flipOriginal() {
		if(style == PaintStyle.ORIG) {
			style = PaintStyle.TESS;
			wallpaper.origTileButton.setText(Messages.getString("Button.OrigImage"));
        }
        else {
			wallpaper.origTileButton.setText(Messages.getString("Button.TileImage"));
			style = PaintStyle.ORIG;
        }
		redraw();
	}

    private void showIsolatedTile() {
        tr.calcFrame(fd,wallpaper.curvertex, constrainVertices);
        tr.fixVerticies(fd);
        tr.calcFund(fd);
        Polygon poly = fd.make_tile_polygon();
        tr.replicate_isolated_domain(dr, poly);
        repaint();
	}

	private void showIsolatedDomain() {
        tr.calcFrame(fd,wallpaper.curvertex, constrainVertices);
        tr.fixVerticies(fd);
        tr.calcFund(fd);
        Polygon poly = fd.make_FD_polygon();
        tr.replicate_isolated_domain(dr, poly);
        repaint();				
	}

	public void applyIsolate(boolean flag) {
		if(flag) {
			style = PaintStyle.DOMAIN;
	        wallpaper.setText(Messages.getString("Msg.isolate_domain"));
		}
		else
			style = PaintStyle.TESS;
        redraw();
        wallpaper.setViewCheckboxes();
	}

	public void applyIsolateTile(boolean flag) {
		if(flag) {
			style = PaintStyle.TILE;
			wallpaper.setText(Messages.getString("Msg.isolate_tile"));
		}
		else
			style = PaintStyle.TESS;
        redraw();
        wallpaper.setViewCheckboxes();
	}

	public void paintCanvas(Wallpaper wallpaper, Graphics g) {
	    if(Wallpaper.DEBUG) 
	    	System.out.println("paintCanvas" + wallpaper.dr.dispRect); //$NON-NLS-1$
	    
	    //System.out.printf("cp %d %d %d %d %d %d\n",fd.verticies[0].x,fd.verticies[0].y,fd.verticies[1].x,fd.verticies[1].y,fd.verticies[2].x,fd.verticies[2].y);
	    //System.out.printf("%d %d%n", offset.x,offset.y);
	    g.translate(wallpaper.offset.x,wallpaper.offset.y);
	    Rectangle bounds = g.getClipBounds();
	    if(bounds != null && (bounds.x + bounds.width > wallpaper.dr.dispRect.x+wallpaper.dr.dispRect.width)) {
	        g.clearRect(wallpaper.dr.dispRect.x+wallpaper.dr.dispRect.width, bounds.y,
	                bounds.x + bounds.width - (wallpaper.dr.dispRect.x+wallpaper.dr.dispRect.width), bounds.height);
	    }
	    if(bounds != null && (bounds.y + bounds.height > wallpaper.dr.dispRect.y+wallpaper.dr.dispRect.height)) {
	        g.clearRect(bounds.x,wallpaper.dr.dispRect.y+wallpaper.dr.dispRect.height,
	                bounds.width,bounds.y + bounds.height - (wallpaper.dr.dispRect.y+wallpaper.dr.dispRect.height));
	    }
	    wallpaper.dr.paint(g,wallpaper);
	    g.setPaintMode();
	
	    wallpaper.fd.paintSymetries(g, tr);
	    wallpaper.fd.paint(g);
	
	    if(clickCount==0)
	        paintIntro(g);
	    if(clickCount==1)
	    	style = PaintStyle.TESS;
	    
	    if(constrainVertices)
	        wallpaper.fd.paintRegularTile(g);
	
	    g.translate(-wallpaper.offset.x,-wallpaper.offset.y);
	    wallpaper.paintDone = true;
	}

	void paintIntro(Graphics g) {
	    Vec base = tr.frameO;
	    String s1 = Messages.getString("IntroBox1a"); //$NON-NLS-1$
	    String s2 = Messages.getString("IntroBox1b"); //$NON-NLS-1$
	    Font f = new Font("SansSerif",Font.BOLD,16); //$NON-NLS-1$
	    g.setFont(f);
	    FontMetrics fm = g.getFontMetrics();
	    int len1 = fm.stringWidth(s1);
	    int height = fm.getHeight();
	    int accent = fm.getMaxAscent();
	    g.setColor(Color.white);
	    g.fillRoundRect(210,base.y+20,len1+20,height*2+20, 20, 20);
	    g.setColor(Color.black);
	
	    g.drawString(s1,220,base.y+30+accent);
	    g.drawString(s2,220,base.y+30+accent+height);
	}

	/**
	 * Called after the first action, mouse click, key press etc. 
	 */
	public void firstAction() {
		if(clickCount==0)
			style = PaintStyle.TESS;
		++clickCount;
	}

	public void resetDomain() {
        fd.resetDomain(dr.dispRect);
        tr.firstCall = true;
	}

}

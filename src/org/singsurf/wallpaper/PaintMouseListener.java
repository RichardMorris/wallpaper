/*
Created 27 Feb 2009 - Richard Morris
 */
package org.singsurf.wallpaper;

import java.awt.CheckboxMenuItem;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Menu;
import java.awt.MenuItem;
import java.awt.PopupMenu;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import javax.swing.JCheckBoxMenuItem;


import org.singsurf.wallpaper.tessrules.TessRule;

public class PaintMouseListener implements MouseMotionListener, MouseListener, ActionListener, ItemListener {

    int brushWidth=9;
    double ratio = 1.0;
    PopupMenu popup;
    int[][] offsets = new int[][] { {1,0},{0,1},{-1,0},{0,-1},{1,1},{1,-1},{-1,1},{-1,-1}};
    
    Map<Vec, Vec> mapping = new HashMap<Vec, Vec>();
    Vec cen;
    private ZoomedDrawableRegion dr;
    private int det;
    private final Menu widthMenu;
 
    /**
     * @param controller
     */
    public PaintMouseListener(Controller controller) {
        this.controller = controller;

        popup = new PopupMenu("Popup");
        popup.add(new MenuItem("Tile/Original"));
        popup.add(new MenuItem("Clear"));
        widthMenu = new Menu("Width");
        for(int i=1;i<=9;++i) {
            CheckboxMenuItem mi = new CheckboxMenuItem(""+i);
            mi.setActionCommand("WID"+i);
            mi.addItemListener(this);
            widthMenu.add(mi);
        }
        popup.add(widthMenu);
        
        transMenu = new Menu("Transparancy");
        for(int i=1;i<=10;++i) {
            double f = i/(10.0);
            CheckboxMenuItem mi = new CheckboxMenuItem(""+f);
            mi.setActionCommand("TRANS"+f);
            mi.addItemListener(this);
            transMenu.add(mi);
        }
        popup.add(transMenu);
        
        transMenu.addActionListener(this);
        widthMenu.addActionListener(this);
        popup.add(new MenuItem("Finish paint"));
        
        popup.addActionListener(this);
        controller.wallpaper.myCanvas.add(popup);
        this.setCheckboxStates("TRANS1.0");
        this.setCheckboxStates("WID9");
    }

    Controller controller;
    private final Menu transMenu;

    public void mouseDragged(MouseEvent e) {
        //System.out.println(e);

        int outX = e.getX()-controller.wallpaper.offset.x;
        int outY = e.getY()-controller.wallpaper.offset.y;
        copyBaseToSrc(outX,outY);
    }

    
    
    public void copyBaseToSrc(int outX,int outY) {
        TessRule tr = controller.tr;
        dr = (ZoomedDrawableRegion) controller.dr;
        mapping.clear();

        final int x0=tr.frameO.x;
        final int y0=tr.frameO.y;
        int u1=tr.frameU.x;
        int u2=tr.frameU.y;
        int v1=tr.frameV.x;
        int v2=tr.frameV.y;

        det = u1 * v2 - v1 * u2;
        if(det < 0 )
        {
            //                          System.out.println("Negative det");
            det = - det;
            int w1 = v1; v1 = u1; u1 = w1; 
            int w2 = v2; v2 = u2; u2 = w2;
        }


        int in[] = new int[2];
        int res[] = new int[2];

        //System.out.println("");
        int min = -(brushWidth /2);
        int max = (brushWidth /2);

        for(int i=min;i<=max;++i) {
            for(int j=min;j<=max;++j) {
                if(i*i+j*j>brushWidth*brushWidth/4) continue;

                int x = outX+i+dr.offset.x - x0;
                int y = outY+j+dr.offset.y - y0; // offset of figure
                in[0] = v2 * x - v1 * y;
                in[1] = -u2 * x + u1 * y;
                tr.fun(in,res,det);

                int inX = outX+i;
                int inY = outY+j;
                int srcX = x0 + (res[0] * u1 + res[1] * v1 ) / det; 
                int srcY = y0 + (res[0] * u2 + res[1] * v2 ) / det; 

                //System.out.println(" ("+inX+","+inY+") ("+srcX+","+srcY+")");

                addMap(inX,inY,srcX,srcY,i==0&&j==0);
                plotPoint(inX, inY, srcX, srcY);
            }
        }
        fillInMissingPoints();

        controller.redraw();

    }

    private void addMap(int i, int j, int srcX, int srcY, boolean isCen) {
        mapping.put(new Vec(srcX,srcY), new Vec(i,j));
        if(isCen)
            cen = new Vec(srcX,srcY);
    }

    private void fillInMissingPoints() {
        int minX=Integer.MAX_VALUE;
        int maxX=Integer.MIN_VALUE;
        int minY=Integer.MAX_VALUE;
        int maxY=Integer.MIN_VALUE;
        
        Iterator<Vec> it = mapping.keySet().iterator();
        while(it.hasNext()) {
            Vec key = (Vec) it.next();
            if(key.x<minX) minX=key.x;
            if(key.x>maxX) maxX=key.x;
            if(key.y<minY) minY=key.y;
            if(key.y>maxY) maxY=key.y;
        }
        
        for(int i=minX;i<=maxX;++i) {
            for(int j=minY;j<=maxY;++j) {
                Vec p1 = new Vec(i,j);
                Vec p2 = p1.sub(cen);
                if(p2.lenSq()>brushWidth*brushWidth/4) continue;
                
                if(!mapping.containsKey(p1)) {
                    //System.out.println("Missing "+p1);
                    for(int k=0;k<offsets.length;++k) {
                        Vec v = new Vec(p1.x+offsets[k][0],p1.y+offsets[k][1]);
                        if(mapping.containsKey(v)) {
                            Vec dest = (Vec) mapping.get(v);
                            plotPoint(dest.x, dest.y, p1.x, p1.y);
                            //System.out.println("replacement "+dest);
                            break;
                        }
                }
                }
            }
        }

    }


    private void plotPoint(int inX, int inY, int srcX, int srcY) {
        try
        {
            int px;

            //int outInd = outX+outY*dr.destRect.width;
            //px = dr.pixels[outInd];
            int inInd = inX+inY*dr.srcRect.width;
            px = dr.basePixels[inInd];

            int outInd = srcX+(srcY)*dr.srcRect.width;
            int px2 = dr.inpixels[outInd];

            Color c1 = new Color(px);
            Color c2 = new Color(px2);
            int red = (int) (ratio * c1.getRed() + (1-ratio) * c2.getRed() + 0.5);
            int green = (int) (ratio * c1.getGreen() + (1-ratio) * c2.getGreen() + 0.5);
            int blue = (int) (ratio * c1.getBlue() + (1-ratio) * c2.getBlue() + 0.5);
            Color c3 = new Color(red,green,blue);
            dr.inpixels[outInd] = c3.getRGB();

            /*            
         if(TessRule.tileBackground) {
        srcX %= dr.srcRect.width; if(srcX <0) srcX += dr.srcRect.width;
        srcY %= dr.srcRect.height; if(srcY <0) srcY += dr.srcRect.height;
        //int inInd = srcX+srcY*dr.srcRect.width;
        //dr.inpixels[inInd] = px;
         }
         else {
        if(srcX<0 || srcX>=dr.srcRect.width || srcY<0 || srcY>=dr.srcRect.height) {
            //px = backgroundRGB;
        }
        else 
            dr.inpixels[srcX+srcY*dr.srcRect.width]=px;
         }
             */        }
        catch(Exception e1)
        {
            //if(!error_flag)
            System.out.println("Error ("+inX+","+inY+") det "+det
                    //+ " x "+x
                    //+ " y "+y
                    //+ " in ("+ in[0] + ","+in[1]+")"
                   // + " res ("+ res[0] + ","+res[1]+")"
                    + " sX "+srcX
                    + " sY "+srcY
            );
            //error_flag = true;
            // dr.pixels[i+j*dr.destRect.width] = backgroundRGB;
        }
    }




    public void mouseMoved(MouseEvent e) {
     }

    public void mouseClicked(MouseEvent e) {
     }

    public void mouseEntered(MouseEvent e) {
     }

    public void mouseExited(MouseEvent e) {
     }

    public void mousePressed(MouseEvent e) {
        //System.out.println(e);
        if(e.isPopupTrigger()) {
            popup(e);
            return;
        }
        if(e.getButton()!=MouseEvent.BUTTON1)
            return;
        int outX = e.getX()-controller.wallpaper.offset.x;
        int outY = e.getY()-controller.wallpaper.offset.y;
        copyBaseToSrc(outX,outY);
    }

    public void mouseReleased(MouseEvent e) {
        if(e.isPopupTrigger()) {
            popup(e);
            return;
        }

    }

    void popup(MouseEvent e) {
        popup.show(e.getComponent(), e.getX(), e.getY());
    }

    public void actionPerformed(ActionEvent e) {
        System.out.println(e);
        String cmd = e.getActionCommand();
        
        if(cmd.equals("Tile/Original")) {
            if(controller.showingOriginal /* || !this.interactiveMode */ ) {
                controller.applyTessellation();
            }
            else {
                controller.showOriginal();
            }
        }
        if(cmd.equals("Clear")) {
            ((ZoomedDrawableRegion)controller.dr).calcZoomedImages();
            controller.redraw();
        }
        if(cmd.equals("Finish paint"))
        {
            controller.wallpaper.myCanvas.removeMouseMotionListener(this);
            controller.wallpaper.myCanvas.removeMouseListener(this);
            controller.wallpaper.myCanvas.addMouseListener(controller.wallpaper);
            controller.wallpaper.myCanvas.addMouseMotionListener(controller.wallpaper);
            controller.wallpaper.myCanvas.setCursor(Cursor.getDefaultCursor());
            ((JCheckBoxMenuItem) ((WallpaperFramed )controller.wallpaper).drawMenu.getItem(0)).setState(false);

        }
    }


    public void setCheckboxStates(String cmd) {
        
        if(cmd.startsWith("WID")) {
            for(int i=0;i<widthMenu.getItemCount();++i)
            {
                if(widthMenu.getItem(i).getActionCommand().equals(cmd))
                    ((CheckboxMenuItem)widthMenu.getItem(i)).setState(true);
                else
                    ((CheckboxMenuItem)widthMenu.getItem(i)).setState(false);
            }

        }
        else if(cmd.startsWith("TRANS")) {
            for(int i=0;i<transMenu.getItemCount();++i)
            {
                if(transMenu.getItem(i).getActionCommand().equals(cmd)) 
                    ((CheckboxMenuItem)transMenu.getItem(i)).setState(true);
                else
                    ((CheckboxMenuItem)transMenu.getItem(i)).setState(false);
                
            }
        }
        
    }
    public void itemStateChanged(ItemEvent e) {
        System.out.println(e);
        String cmd = ((MenuItem) e.getItemSelectable()).getActionCommand();
        
        if(cmd.startsWith("WID")) {
            this.brushWidth = Integer.parseInt(cmd.substring(3));
        }
        else if(cmd.startsWith("TRANS")) {
            this.ratio = Double.parseDouble(cmd.substring(5));
        }
        setCheckboxStates(cmd);
        
    }

}

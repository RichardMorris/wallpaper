/*
Created 31 May 2010 - Richard Morris
*/
package org.singsurf.wallpaper;

import java.awt.Image;
import java.awt.Rectangle;
import java.net.MalformedURLException;
import java.net.URL;

import javax.swing.JApplet;

public class WallpaperApplet extends JApplet {

    private static final long serialVersionUID = 1L;
    Wallpaper wall;
    
 
    @Override
    public void init() {
        //Execute a job on the event-dispatching thread:
        //creating this applet's GUI.
        try {
            javax.swing.SwingUtilities.invokeAndWait(new Runnable() {
                public void run() {
                    createGUI();
                }
            });
        } catch (Exception e) {
            System.err.println("createGUI didn't successfully complete");
            System.err.println(e.toString());
            e.printStackTrace(System.err);
        }
    }

    void createGUI() {
    	System.out.println(this.getDocumentBase());
        String s = this.getParameter("image");
        if(s==null) s = "HemerocallisFulva.jpg";
        Rectangle rect = this.getBounds();
        Image img = appletGetImage(s);

        wall = new Wallpaper();
        wall.initialize(img,rect.width,rect.height);
        getContentPane().add(wall);
       
    }
    
    /** gets an image in an applet context
     * 
     * @param imgloc either a full URL or relative
     * @return the image
     */
    public Image appletGetImage(String imgloc)
    {
        URL imgurl=null;
        Image imgin=null;

        // first try if its a a full URL
        try
        {
            imgurl = new URL(imgloc);
        }
        catch(MalformedURLException e)
        {
            // then see if its relative 
            try
            {
                imgurl = new URL(this.getDocumentBase(),imgloc);
            }
            catch(MalformedURLException e1)
            {
                System.out.println("Bad URL"+imgloc);
                return null;
            }
        }
        if(imgurl!=null) System.out.println("URL "+imgurl.toString());
        try
        {
            if(imgurl!=null)
                imgin = this.getImage(imgurl);
        }
        catch(Exception e)
        {
            System.out.println("Error with getImage");
            System.out.println(e.getMessage());
            e.printStackTrace();
            return null;
        }

        // grabs the default rect
        //System.out.println("Image is "+imgin);
        if(imgin==null)
        {
            System.out.println("NULL image");
            return null;
        }
        return imgin;
    }

}

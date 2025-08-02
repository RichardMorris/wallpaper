package org.singsurf.wallpaper;
import java.awt.Color;

import javax.swing.JFrame;
import javax.swing.SwingUtilities;

/*
 Created 10 Apr 2007 - Richard Morris
 */

public class WallpaperApplication {
    // This interface is used for objects defining now to do tessellation
    JFrame mainFrame = null;
    String image;

	private WallpaperFramed app;
	

    public WallpaperApplication(String image,int w,int h) {
		this.image = image;
        mainFrame = new JFrame(Messages.getString("Window.title")); //$NON-NLS-1$        
        String iconFileName = GraphicalTesselationPanel.iconPrefix + Messages.getString("Window.icon");
        var icon = GraphicalTesselationPanel.createImageIcon(iconFileName, "");
        mainFrame.setIconImage(icon.getImage());
        mainFrame.setBounds(0, 0, w, h);
        app = new WallpaperFramed(image, w, h);
        app.mainFrame = mainFrame;
        app.setBackground(Color.green);
        mainFrame.setBackground(Color.red);
        var menu = app.buildMenu();
		mainFrame.setJMenuBar(menu);
        mainFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        mainFrame.add(app);
        mainFrame.pack();
        mainFrame.setFocusable(true);
        mainFrame.setVisible(true);
    }

	/**
     * @param progargs
     */
    public static void main(String progargs[]) {
        String args[] = null;
    	final String image;
        System.out.println(Wallpaper.programInfo());
        System.out.println(Wallpaper.helpInfo());
        args = progargs;
        if (args.length == 0) {
            image = ""; //$NON-NLS-1$
        } else
            image = args[0];
        final int w,h;
        if (args.length >= 3) {
            w = Integer.parseInt(args[1]);
            h = Integer.parseInt(args[2]);
        } else {
			w = 800;
			h = 600;
		}

        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                new WallpaperApplication(image,w,h);
            }
        });

    }

}

/*
Created 28 Apr 2007 - Richard Morris
 */
package org.singsurf.wallpaper;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GraphicsDevice;
import java.awt.GraphicsEnvironment;
import java.awt.Image;
import java.awt.Rectangle;
import java.awt.Toolkit;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.AdjustmentEvent;
import java.awt.event.AdjustmentListener;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.KeyEvent;
import java.awt.image.BufferedImage;
import java.awt.print.PageFormat;
import java.awt.print.Printable;
import java.awt.print.PrinterException;
import java.awt.print.PrinterJob;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
//import java.io.FilenameFilter;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;

import javax.imageio.ImageIO;
import javax.swing.ButtonGroup;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JComponent;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JRadioButtonMenuItem;
import javax.swing.JScrollPane;
import javax.swing.ScrollPaneConstants;
import javax.swing.filechooser.FileFilter;

import org.singsurf.wallpaper.dialogs.ErrorDialog;
import org.singsurf.wallpaper.dialogs.ExpandDialog;
import org.singsurf.wallpaper.dialogs.ExpandedSizeDialog;
import org.singsurf.wallpaper.dialogs.JColourPicker;
import org.singsurf.wallpaper.dialogs.RescaleDialog;
import org.singsurf.wallpaper.dialogs.ResizeDialog;
import org.singsurf.wallpaper.tessrules.TessRule;


public class WallpaperFramed extends Wallpaper implements ActionListener, ComponentListener, AdjustmentListener {

	public WallpaperFramed(String imgfilename, int w, int h) {
		super(frameGetImage(imgfilename), w, h);
		myCanvas.requestFocus();
	}

	private static final long serialVersionUID = 1L;
    /** Scrollable pane */
    protected JScrollPane jsp;


    @Override
    protected DrawableRegion buildDrawableRegion() {
        return new ZoomedDrawableRegion(this);
    }

    @Override
    public void keyPressed(KeyEvent e) {
//        int code = e.getKeyCode();
//        System.out.println("WF: Key Pressed: " + code);
		if (e.getKeyCode() == KeyEvent.VK_F11) {
			toggleFullScreen(mainFrame);
		}
		if (e.getKeyCode() == KeyEvent.VK_ESCAPE && isFullScreen) {
			toggleFullScreen(mainFrame);
		}
		if (e.getKeyCode() == KeyEvent.VK_SPACE) {
			animController.stopStartAnim();
		}


        super.keyPressed(e);
    }

    public void actionPerformed(ActionEvent e) {
        String com = e.getActionCommand();
        if(com.equals("load image"))
            load();
        else if(com.equals("save image"))
            save();
        else if(com.equals("save tile"))
            saveTile();
        else if(com.equals("load pattern"))
            loadPat();
        else if(com.equals("save pattern"))
            savePat();
        else if(com.equals("print"))
            printImage();
        else if(com.equals("exit"))
            System.exit(0);
        else if(com.equals("save big"))
            saveBig();
        else if(com.equals("crop")) {
            final ResizeDialog rd = new ResizeDialog(mainFrame,this);

            rd.open(dr.baseRect.width,dr.baseRect.height);
            if (rd.ok)
                resizeImage(rd.width, rd.height, -rd.xoff, -rd.yoff);
            else
                controller.redraw();
        }
        else if(com.equals("expand")) {
            ExpandDialog ed = new ExpandDialog(mainFrame,this);

            ed.open(dr.baseRect.width,dr.baseRect.height);
            if (ed.ok)
                resizeImage(ed.imageWidth, ed.imageHeight, ed.xoff, ed.yoff);
            else
                controller.redraw();
        }
        else if(com.equals("resize")) {
            final RescaleDialog rs = new RescaleDialog(mainFrame,this);

            rs.open(dr.baseRect.width, dr.baseRect.height);
            if (rs.isOk())
                rescaleImage(rs.getNewWidth(), rs.getNewHeight());
            else
                controller.redraw();
        }
        else if(com.equals("copy")) {
            copy();
        }
        else if(com.equals("copyfull")) {
            copyFull();
        }
        else if(com.equals("paste")) {
            paste();
        }
        else if(com.startsWith("bg/")) {
            Color col=backgroundColour;
            String label = com.substring(3);
            TessRule.tileBackground = false;
            if(label.equals("tile")) {
                TessRule.tileBackground = true;
            }
            else if(label.equals("black")) {
                col = Color.black;
            }
            else if(label.equals("white")) {
                col = Color.white;
            }
            else if(label.equals("other ...")) {
                colD.open(backgroundColour);
                if(colD.isOk())
                    col = colD.getCol();
            }
            else if(label.equals("pick")) {
                mouseMode = MOUSE_PIPET;
            }
            setBGColor(col);
            controller.redraw();
        }
        else if(com.startsWith("anim/")) {
            String label = com.substring(5);
            //"up","down","left","right","rotate","Stop"
            if(label.equals("Stop")) {
                animController.stopAnim();
            }
            else startAnim(label);

        }
        else if(com.equals("about")) {
            final ErrorDialog rs = new ErrorDialog(mainFrame,"About");

            rs.open(programInfo());
         }
    }

    Printable printable = new Printable() {

    	public int print(Graphics g, PageFormat pageFormat, int pageIndex)
    	throws PrinterException {
    		if (pageIndex > 0) {
    			return(NO_SUCH_PAGE);
    		} else {
    			Graphics2D g2d = (Graphics2D)g;
    			g2d.translate(pageFormat.getImageableX(), pageFormat.getImageableY());
    			g2d.scale(2.0, 0.5);
    			dr.paint(g2d, WallpaperFramed.this);
    			// Turn off double bsuffering
    			//componentToBePrinted.paint(g2d);
    			// Turn double buffering back on
    			return(PAGE_EXISTS);
    		}
  	}};

    private void printImage() {
    	PrinterJob printJob = PrinterJob.getPrinterJob();
        printJob.setPrintable(printable);
        if (printJob.printDialog())
          try { 
            printJob.print();
          } catch(PrinterException pe) {
            System.out.println("Error printing: " + pe);
          }
	}



	@Override
	public void hideControls() {
		super.hideControls();
		viewMenu.setVisible(false);
		jsp.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
		jsp.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_NEVER);
	}

	@Override
	public void showControls() {
		super.showControls();
		viewMenu.setVisible(true);
		jsp.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED);
		jsp.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED);
	}	
	
	protected FileFilter saveFF = new FileFilter() {
        public boolean accept(File dir, String name) {
            String lcname = name.toLowerCase();
            if (lcname.endsWith(".jpg"))
                return true;
            if (lcname.endsWith(".png"))
                return true;
            if (lcname.endsWith(".bmp"))
                return true;
            if (lcname.endsWith(".jpeg"))
                return true;
            if (lcname.endsWith(".tga"))
                return true;
            if (lcname.endsWith(".psd"))
                return true;

            return false;
        }

        @Override
        public boolean accept(File file) {
            if(file.isDirectory()) return true;
            return(accept(file.getParentFile(),file.getName()));
        }

        @Override
        public String getDescription() {
            return "jpg, png, bmp, tga, psd images";
        }
    };

    protected FileFilter ppmFF = new FileFilter() {
        public boolean accept(File dir, String name) {
            String lcname = name.toLowerCase();
            if (lcname.endsWith(".ppm") || lcname.endsWith(".bmp"))
                return true;
            return false;
        }
        @Override
        public boolean accept(File file) {
            if(file.isDirectory()) return true;
            return(accept(file.getParentFile(),file.getName()));
        }
        @Override
        public String getDescription() {
            return "ppm or bmp images";
        }

    };

    protected FileFilter patFF = new FileFilter() {
        @Override
        public boolean accept(File file) {
            if(file.isDirectory()) return true;
            return(file.getName().toLowerCase().endsWith(".pat"));
        }
        @Override
        public String getDescription() {
            return "Wallpaper patter files .pat";
        }

    };

    protected FileFilter loadFF = new FileFilter() {
        @Override
        public boolean accept(File file) {
            if(file.isDirectory()) return true;
            return(saveFF.accept(file));
        }
        @Override
        public String getDescription() {
            return "gif, " +saveFF.getDescription();
        }

    };

    protected ItemListener zoomItemListener = new ItemListener(){
        public void itemStateChanged(ItemEvent arg0) {
            int oldZoomD = ((ZoomedDrawableRegion) dr).zoomDenom;
            int oldZoomN = ((ZoomedDrawableRegion) dr).zoomNumer;
            JRadioButtonMenuItem cbmi = (JRadioButtonMenuItem) arg0.getItemSelectable();
            String label = cbmi.getText();
            int index = label.indexOf('/');
            if(index>0) {
            	var numer = label.substring(0,index);
            	var denom = label.substring(index+1);
                int newZoomD = Integer.parseInt(denom); 
                int newZoomN = Integer.parseInt(numer); 
                fd.zoom(((float)oldZoomD*newZoomN)/
                		       (oldZoomN*newZoomD));
                ((ZoomedDrawableRegion) dr).zoom(newZoomN,newZoomD);	
            }
            else {
                int newZoomD = 1; 
                int newZoomN = Integer.parseInt(label); 
                fd.zoom(((float)oldZoomD*newZoomN)/
                		(oldZoomN*newZoomD));
                ((ZoomedDrawableRegion) dr).zoom(newZoomN,newZoomD);	
            }
            imageChanged();
//TODO             parent = (JMenu) cbmi.getParent();
//           for(int i=0;i<parent.getItemCount();++i) {
//                JCheckBoxMenuItem mi = (JCheckBoxMenuItem) parent.getItem(i);
//                if(mi != cbmi) {
//                    mi.setState(false);
//                }
//            }

        }};

        protected ActionListener flipActionListener = new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                flip(e.getActionCommand());
            }
        };

        
        protected ItemListener viewActionListener = new ItemListener() {
        	boolean recursive=false;
        	
            public void itemStateChanged(ItemEvent e) {
            	if(recursive) return;
            	recursive=true;
                JCheckBoxMenuItem cbmi = (JCheckBoxMenuItem) e.getItemSelectable();
                String label = cbmi.getActionCommand();
                if(label.equals("l"))
                    fd.drawCells = cbmi.isSelected();
                if(label.equals("m"))
                    fd.drawReflectionLines = cbmi.isSelected();
                if(label.equals("r"))
                    fd.drawRotationPoints = cbmi.isSelected();
                if(label.equals("g"))
                    fd.drawGlideLines = cbmi.isSelected();
                if(label.equals("d"))
                    fd.drawDomain = cbmi.isSelected();
                if(label.equals("s"))
                    fd.drawSelectionPoints = cbmi.isSelected();
                if(label.equals("t"))
                    fd.drawTiles = cbmi.isSelected();
                if(label.equals("a")) {
                    fd.drawGlideLines = cbmi.isSelected();
                    fd.drawReflectionLines = cbmi.isSelected();
                    fd.drawRotationPoints = cbmi.isSelected();
                }
                if(label.equals("h")) {
                    fd.drawGlideLines = !cbmi.isSelected();
                    fd.drawReflectionLines = !cbmi.isSelected();
                    fd.drawRotationPoints = !cbmi.isSelected();
                }

                imageChanged();
            	recursive=false;
            	setViewCheckboxes();
            }
        };


		protected JColourPicker colD;

        /**
         * Build the menus for application usage.
         * @param wallpaperApplication 
         * @return the MenuBar
         */
        public JMenuBar buildMenu() {
            colD = new JColourPicker(mainFrame,this);
            
            mainFrame.addKeyListener(this);

            JMenuBar mb = new JMenuBar();

            mb.add(buildFileMenu());
            JMenu editMenu = buildEditMenu();
            mb.add(editMenu);

            JMenu imageMenu = buildImageMenu();
            mb.add(imageMenu);

            buildViewMenu();
            mb.add(viewMenu);

            JMenu optionsMenu = buildOptionsMenu();
            mb.add(optionsMenu);

            JMenu animateMenu = buildAnimationMenu();
            mb.add(animateMenu);

            JMenu winMenu = buildWindowMenu();
            mb.add(winMenu);

            //JMenu helpMenu = buildHelpMenu();
            //mb.setHelpMenu(helpMenu);
            return (mb);
        }

        
        private JMenu buildWindowMenu() {
            JMenu menu = new JMenu("Window");
            JMenuItem mi = new JMenuItem("Full Screen");
            mi.addActionListener((e) -> toggleFullScreen(mainFrame));
            menu.add(mi);
			return menu;
		}

		private JMenu buildOptionsMenu() {
            JMenu optionsMenu = new JMenu("Options");
            JMenu backgroundMenu = new JMenu("BG colour");
            String colours[] = {"tile","black","white","other ...","pick"};
            for(int i=0;i<colours.length;++i) {
                JMenuItem mi = new JMenuItem(colours[i]);
                mi.setActionCommand("bg/"+colours[i]);
                mi.addActionListener(this);
                backgroundMenu.add(mi);
            }
            optionsMenu.add(backgroundMenu);
            
            JCheckBoxMenuItem cvmi = new JCheckBoxMenuItem("Constrain vertices");
            cvmi.addItemListener(new ItemListener() {

                public void itemStateChanged(ItemEvent arg0) {
                    int state = arg0.getStateChange();
                    switch(state) {
                    case ItemEvent.SELECTED:
                        controller.constrainVertices = true;
                        controller.setText("This constrains the vertices of pattern so that it is axis alligned\n" +
                        		"and can be used as a tiled desktop/webpage background image.\n" +
                        		"Use File->Save Tile to save the tilable image.\n");
                        controller.calcGeom();
                        controller.redraw();
                        break;
                    case ItemEvent.DESELECTED:
                        controller.constrainVertices = false;
                        break;
                    }

                }});
            optionsMenu.add(cvmi);

            JCheckBoxMenuItem scmi = new JCheckBoxMenuItem("Show coordinates");
            scmi.setActionCommand("coords");
            scmi.addItemListener(new ItemListener() {

                public void itemStateChanged(ItemEvent arg0) {
                    int state = arg0.getStateChange();
                    switch(state) {
                    case ItemEvent.SELECTED:
                        controller.setShowCoords(true);
                        break;
                    case ItemEvent.DESELECTED:
                        controller.setShowCoords(false);
                        break;
                    }

                }});
            optionsMenu.add(scmi);
            return optionsMenu;
        }

        private void buildViewMenu() {
            viewMenu = new JMenu("View");

            String views[] = {"Cells","Tiles","Selection domain","Selection points","-",
                    "All symetries","-",
                    "Reflection lines","Rotation points","Glide-reflection lines"};
            String viewKeys[] = {"l","t","d","s","-",
                    "a","-",
                    "m","r","g"};
            boolean viewStates[] = {false,false,true,true,false,
                    false,false,
                    false,false,false};
            for(int i=0;i<views.length;++i) {
                if(views[i].equals("-")) {
                    viewMenu.addSeparator();
                    continue;
                }
                JCheckBoxMenuItem vmi = new JCheckBoxMenuItem(views[i],viewStates[i]);
                vmi.setActionCommand(viewKeys[i]);
                viewMenu.add(vmi);
                vmi.addItemListener(viewActionListener);
            }
        }


        private JMenu buildImageMenu() {
            JMenu imageMenu = new JMenu("Image");

            JMenu zoomMenu = new JMenu("Zoom");
            ButtonGroup group = new ButtonGroup();
            String factors[] = {"4","3","2","3/2","1","2/3","1/2","1/3","1/4","1/8"};
            for(var label: factors) {
                JRadioButtonMenuItem mi = new JRadioButtonMenuItem(label);
                mi.addItemListener(zoomItemListener);
                if(label=="1") mi.setSelected(true);
                zoomMenu.add(mi);
                group.add(mi);
            }
            imageMenu.add(zoomMenu);

            JMenuItem resizeMI = new JMenuItem("Crop");
            resizeMI.addActionListener(this);
            resizeMI.setActionCommand("crop");
            imageMenu.add(resizeMI);

            JMenuItem expandMI1 = new JMenuItem("Expand");
            expandMI1.addActionListener(this);
            expandMI1.setActionCommand("expand");
            imageMenu.add(expandMI1);

            JMenuItem rescaleMI = new JMenuItem("Resize");
            rescaleMI.addActionListener(this);
            rescaleMI.setActionCommand("resize");
            imageMenu.add(rescaleMI);

            JCheckBoxMenuItem splitMI = new JCheckBoxMenuItem("Split");
            splitMI.addItemListener(new ItemListener() {
				public void itemStateChanged(ItemEvent e) {
					int state = e.getStateChange();
					controller.split(state==ItemEvent.SELECTED);
				}});
            splitMI.addActionListener(this);
            splitMI.setActionCommand("split");
            imageMenu.add(splitMI);

            JMenu flipMI = new JMenu("Flip/Rotate");
            JMenuItem flipX = new JMenuItem(Wallpaper.FLIP_X);
            flipX.addActionListener(flipActionListener);
            flipX.setActionCommand(Wallpaper.FLIP_X);
            flipMI.add(flipX);
            JMenuItem flipY = new JMenuItem(Wallpaper.FLIP_Y);
            flipY.addActionListener(flipActionListener);
            flipY.setActionCommand(Wallpaper.FLIP_Y);
            flipMI.add(flipY);
            //		MenuItem flipXY = new MenuItem("Flip XY");
            //		flipXY.addActionListener(new ActionListener(){
            //			public void actionPerformed(ActionEvent arg0) {
            //				flip(0);
            //			}});
            //		flipMI.add(flipXY);
            JMenuItem rot90 = new JMenuItem(Wallpaper.FLIP_90);
            rot90.addActionListener(flipActionListener);
            rot90.setActionCommand(Wallpaper.FLIP_90);
            flipMI.add(rot90);
            JMenuItem rot180 = new JMenuItem(Wallpaper.FLIP_180);
            rot180.addActionListener(flipActionListener);
            rot180.setActionCommand(Wallpaper.FLIP_180);
            flipMI.add(rot180);
            JMenuItem rot270 = new JMenuItem(Wallpaper.FLIP_270);
            rot270.addActionListener(flipActionListener);
            rot270.setActionCommand(Wallpaper.FLIP_270);
            flipMI.add(rot270);

            imageMenu.add(flipMI);
            return imageMenu;
        }

        private JMenu buildEditMenu() {
            JMenu editMenu = new JMenu("Edit");
            JMenuItem copyMI = new JMenuItem("Copy");
            copyMI.setMnemonic(KeyEvent.VK_C);
            copyMI.addActionListener(this);
            copyMI.setActionCommand("copy");

            JMenuItem copyFullMI = new JMenuItem("Copy visable");
            copyFullMI.addActionListener(this);
            copyFullMI.setActionCommand("copyfull");

            JMenuItem pasteMI = new JMenuItem("Paste");
            pasteMI.setMnemonic(KeyEvent.VK_V);
            pasteMI.addActionListener(this);
            pasteMI.setActionCommand("paste");

            editMenu.add(copyMI);
            editMenu.add(copyFullMI);
            editMenu.add(pasteMI);
            return editMenu;
        }

        private JMenu buildFileMenu() {
            JMenu fileMenu = new JMenu("File");

            JMenuItem loadMI = new JMenuItem("Load");
            loadMI.setMnemonic(KeyEvent.VK_L);
            loadMI.addActionListener(this);
            loadMI.setActionCommand("load image");

            JMenuItem loadGMI = new JMenuItem("Load pattern");
            loadGMI.setMnemonic(KeyEvent.VK_P);
            loadGMI.addActionListener(this);
            loadGMI.setActionCommand("load pattern");

            JMenuItem saveMI = new JMenuItem("Save");
            saveMI.setMnemonic(KeyEvent.VK_S);
            saveMI.addActionListener(this);
            saveMI.setActionCommand("save image");

            JMenuItem savetileMI = new JMenuItem("Save Tile");
            savetileMI.setMnemonic(KeyEvent.VK_T);
            savetileMI.addActionListener(this);
            savetileMI.setActionCommand("save tile");

            JMenuItem saveBigMI = new JMenuItem("Save Expanded");
            saveBigMI.setMnemonic(KeyEvent.VK_E);
            saveBigMI.addActionListener(this);
            saveBigMI.setActionCommand("save big");

            JMenuItem saveGMI = new JMenuItem("Save Pattern");
            saveGMI.setMnemonic(KeyEvent.VK_L);
            saveGMI.addActionListener(this);
            saveGMI.setActionCommand("save pattern");

            JMenuItem printMI = new JMenuItem("Print ...");
            printMI.setMnemonic(KeyEvent.VK_P);
            printMI.addActionListener(this);
            printMI.setActionCommand("print");

            JMenuItem exitMI = new JMenuItem("Exit");
            exitMI.setMnemonic(KeyEvent.VK_X);
            exitMI.addActionListener(this);
            exitMI.setActionCommand("exit");

            fileMenu.add(loadMI);
            fileMenu.add(loadGMI);
            fileMenu.add(saveMI);
            fileMenu.add(savetileMI);
            fileMenu.add(saveBigMI);
            fileMenu.add(saveGMI);
            fileMenu.add(printMI);
            //fileMenu.add(webMI);
            fileMenu.addSeparator();
            fileMenu.add(exitMI);
            return fileMenu;
        }

        @Override
        protected JComponent buildCanvasComponent(JComponent c) {
            jsp = new JScrollPane(c,
                    ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED,
                    ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED);
            jsp.getVerticalScrollBar().addAdjustmentListener(this);
            jsp.getHorizontalScrollBar().addAdjustmentListener(this);
            return jsp;
        }
        
        public void addCanvas(JComponent c) {
		}
        
        protected JMenu buildAnimationMenu() {
    	    stopBut.setVisible(true);
    	    stopBut.setEnabled(true);
    	
    	    JMenu animateMenu = new JMenu("Animation");
    	    String animations[] = {"up","down","left","right","NE","NW","SE","SW","rotate","bounce","smooth"};
    	    for(int i=0;i<animations.length;++i) {
    	        JMenuItem mi = new JMenuItem(animations[i]);
    	        mi.setActionCommand("anim/"+animations[i]);
    	        mi.addActionListener(this);
    	        animateMenu.add(mi);
    	    }
    	    animateMenu.addSeparator();
    	    JMenuItem mi = new JMenuItem("Stop");
    	    mi.setActionCommand("anim/Stop");
    	    mi.addActionListener(this);
    	    animateMenu.add(mi);
    	
    	    return animateMenu;
    	}

        //@Override
       public Image getImage(String loc) {
            return frameGetImage(loc);
        }

        private void load() {
            System.out.println("load ...");
            fc.setFileFilter(loadFF);
            int res = fc.showOpenDialog(mainFrame);
            if(res != JFileChooser.APPROVE_OPTION) return;
            File f = fc.getSelectedFile();
            
            if (f != null) {
                System.out.println(f);
                WallpaperFramed.this.imageFilename = f.getAbsolutePath();
                //WallpaperFramed.this.imageURL = null;
                Image img;
                try {
                    img = ImageIO.read(f);
                } catch (IOException e) {
                    ErrorDialog errorD = new ErrorDialog(mainFrame);
                    errorD.open("Error loading image "+imageFilename+".","");
                    errorD.dispose();
                    return;
                }
                if (img != null && dr.loadImage(img)) {
                    imageChanged();
                }
                else {
                    ErrorDialog errorD = new ErrorDialog(mainFrame);
                    errorD.open("Error loading image "+imageFilename+".","");
                    errorD.dispose();
                    return;
                }
                setTitle(f.getName());
                System.out.println("load done");
            }

        }

        private String getType(String name) {
            String lcname = name.toLowerCase();
            if (lcname.endsWith(".jpg"))
                return "jpg";
            if (lcname.endsWith(".png"))
                return "png";
            if (lcname.endsWith(".bmp"))
                return "bmp";
            if (lcname.endsWith(".jpeg"))
                return "jpg";
            return null;
        }
        
        JFileChooser fc = new JFileChooser(System.getProperty("user.dir"));
        {
    	    //Add the preview pane.
            fc.setAccessory(new ImagePreview(fc));

        }
        private void save() {
            fc.setFileFilter(saveFF);
            int res = fc.showSaveDialog(mainFrame);
            if(res != JFileChooser.APPROVE_OPTION) return;
            File f = fc.getSelectedFile();
            if (f != null)
                try {
                    int denom = ((ZoomedDrawableRegion) dr).zoomDenom;
					int numer = ((ZoomedDrawableRegion) dr).zoomNumer;
					fd.zoom(((float) denom)/numer);
		             ((ZoomedDrawableRegion) dr).zoom(1,1);	

                     if(controller.showingOriginal) {
                         controller.showOriginal();
                     }
                     else {
                         controller.applyTessellation();
                         controller.calcGeom();
                         controller.applyFull(dr);
                     }

                    String type = getType(f.getName());
                    try {
                        Image img = dr.getActiveImage();
                        BufferedImage bImg = new BufferedImage(
                                img.getWidth(null),img.getHeight(null),
                                BufferedImage.TYPE_INT_RGB);
                        Graphics g = bImg.getGraphics();
                        g.setClip(0, 0, img.getWidth(null),img.getHeight(null));
                        paintCanvas(g);
                        ImageIO.write(bImg,type,f);

                    } catch (IOException e) {
                        ErrorDialog errorD = new ErrorDialog(mainFrame);
                        errorD.open("Error loading image "+f.getAbsolutePath()+".","");
                        errorD.dispose();
                        return;
                    }

                    if (DEBUG)
                        System.out.println("Save " + f.getAbsolutePath());

                    fd.zoom(((float)numer)/denom);
                    ((ZoomedDrawableRegion) dr).zoom(numer,denom);	
                    controller.calcGeom();
                    controller.redraw();
                } catch (Exception e) {
                    ErrorDialog errorD = new ErrorDialog(mainFrame);
                    errorD.open("Error saving image",e.getMessage());
                    errorD.dispose();
                    System.out.println(e.getMessage());
                }

        }

        private void saveTile() {
            Rectangle rect = fd.tileableRegion(dr.dispRect); 
            if(rect==null) {
                ErrorDialog errorD = new ErrorDialog(mainFrame);
                errorD.open("A rectangular tile cannot be created for this geometry.","Select options->show cordinates show when rectangular tiles can be created.");
                errorD.dispose();
                return;
            }
            fc.setFileFilter(saveFF);
            int res = fc.showSaveDialog(mainFrame);
            if(res != JFileChooser.APPROVE_OPTION) return;
            File f = fc.getSelectedFile();

//            JFileChooser fid = new JFileChooser(mainFrame, "Save image",
//                    JFileChooser.SAVE);
//            fid.setFilenameFilter(saveFF);
//            fid.setVisible(true);
//            String dir = fid.getDirectory();
//            String filename = fid.getFile();
//            fid.dispose();
            if (f != null)
                try {
//                    if(!saveFF.accept(null, filename)) {
//                        ErrorDialog errorD = new ErrorDialog(mainFrame);
//                        errorD.open("File type not supported "+filename+".","Only bmp, jpg, png psd tga formats supported.");
//                        errorD.dispose();
//                        return;
//                    }

                    fd.zoom(((ZoomedDrawableRegion) dr).zoomDenom);
                    controller.calcGeom();
                    controller.applyFull(dr);
                    //				Jimi.putImage(dr.getActiveImage(), dir + filename);
                    String type = getType(f.getName());
//                    File f = new File(dir,filename);
                    try {
                        Image img = dr.getActiveImage();
                        BufferedImage bImg = new BufferedImage(
                                rect.width,rect.height,
                                BufferedImage.TYPE_INT_RGB);
                        Graphics g = bImg.getGraphics();
                        g.drawImage(img, 0, 0, null);
                        ImageIO.write(bImg,type,f);

                    } catch (IOException e) {
                        ErrorDialog errorD = new ErrorDialog(mainFrame);
                        errorD.open("Error loading image "+f.getAbsolutePath()+".","");
                        errorD.dispose();
                        return;
                    }

                    if (DEBUG)
                        System.out.println("Save " + f.getAbsolutePath());
                    fd.zoom(1/((float) ((ZoomedDrawableRegion) dr).zoomDenom));
                    controller.calcGeom();
                } catch (Exception e) {
                    ErrorDialog errorD = new ErrorDialog(mainFrame);
                    errorD.open("Error saving image",e.getMessage());
                    errorD.dispose();
                    System.out.println(e.getMessage());
                }

        }

		/**
		 * Save an expanded version of the image.
		 *
		 */
		private void saveBig() {
			final ExpandedSizeDialog esd = new ExpandedSizeDialog(mainFrame);

			esd.open(dr.baseRect.width, dr.baseRect.height);
			if (!esd.isOk())
				return;

			fc.setFileFilter(ppmFF);
			int res = fc.showSaveDialog(mainFrame);
			if (res != JFileChooser.APPROVE_OPTION)
				return;
			File f = fc.getSelectedFile();

			if (f == null)
				return;
			FileOutputStream fos;
			try {
				fos = new FileOutputStream(f);
			} catch (FileNotFoundException e) {
				return;
			}
			String filename = f.getName();
			BufferedOutputStream bos = new BufferedOutputStream(fos);

			int denom = ((ZoomedDrawableRegion) dr).zoomDenom;
			int numer = ((ZoomedDrawableRegion) dr).zoomNumer;
			fd.zoom(((float) denom) / numer);
			((ZoomedDrawableRegion) dr).zoom(1, 1);
			controller.calcGeom();

			int w = esd.getImgWidth();
			int h = esd.getImgHeight();
			try {
				boolean isPPM = filename.toLowerCase().endsWith(".ppm");
				boolean isBMP = filename.toLowerCase().endsWith(".bmp");
				if (isPPM) {
					// File f = new File(dir, filename);
					String header = "P6\n#Created by org.singsurf.wallpaper\n" + w + " " + h + "\n255\n";
					bos.write(header.getBytes());
				} else if (isBMP) {
					// File f = new File(dir, filename);
					int rowsize = 4 * ((24 * w + 31) / 32);
					int filesize = 54 + rowsize * h;
					byte b1 = (byte) filesize;
					byte b2 = (byte) (filesize >>> 8);
					byte b3 = (byte) (filesize >>> 16);
					byte b4 = (byte) (filesize >>> 24);
					bos.write(new byte[] { 0x42, 0x4d, // header
							b1, b2, b3, b4, // size of file
							0, 0, 0, 0, // app id
							54, 0, 0, 0, // offset of image data
							40, 0, 0, 0, // remaining header size
							(byte) w, (byte) (w >>> 8), (byte) (w >>> 16), (byte) (w >>> 24), // width
							(byte) h, (byte) (h >>> 8), (byte) (h >>> 16), (byte) (h >>> 24), // height
							1, 0, // Number of color planes being used.
							24, 0, // The number of bits/pixel
							0, 0, 0, 0, // BI_RGB, No compression used
							16, 0, 0, 0, // The size of the raw BMP data (after this header)
							0x13, 0x0B, 0, 0, // The horizontal resolution of the image
							0x13, 0x0B, 0, 0, // The vertical resolution of the image
							0, 0, 0, 0, // Number of colors in the palette
							0, 0, 0, 0, // Means all colors are important

					});
				}
				int fill = (4 - (3 * w) % 4) % 4;

				DrawableRegion dr2 = new DrawableRegionTile(dr, w, 1);
				for (int j = 0; j < h; ++j) {
					controller.tr.replicate(dr2, fd);

					for (int i = 0; i < dr2.destRect.width; ++i) {
						Color c = new Color(dr2.pixels[i]);
						if (isPPM) {
							bos.write((byte) c.getRed());
							bos.write((byte) c.getGreen());
							bos.write((byte) c.getBlue());
						} else if (isBMP) {
							bos.write((byte) c.getBlue());
							bos.write((byte) c.getGreen());
							bos.write((byte) c.getRed());
						}
					}
					dr2.offset.y++;
					if (isBMP) {
						for (int i = 0; i < fill; ++i)
							bos.write(0);
					}
				}
				bos.flush();
				bos.close();

			} catch (Exception e) {
				ErrorDialog errorD = new ErrorDialog(mainFrame);
				errorD.open("Error saving image", e.getMessage());
				errorD.dispose();
				System.out.println(e.getClass().getName() + ": " + e.getMessage());
			}
			fd.zoom(((float) numer) / denom);
            controller.calcGeom();

		}

        private void savePat() {
            fc.setFileFilter(patFF);
            int res = fc.showSaveDialog(mainFrame);
            if(res != JFileChooser.APPROVE_OPTION) return;
            File f = fc.getSelectedFile();

//            JFileChooser fid = new JFileChooser(mainFrame, "Save pattern",
//                    JFileChooser.SAVE);
//            fid.setVisible(true);
//            String dir = fid.getDirectory();
//            String filename = fid.getFile();
//            fid.dispose();
            if (f != null) {
                try {
                    //File f = new File(dir, filename);
                    FileWriter fw = new FileWriter(f);
                    PrintWriter pw = new PrintWriter(fw);
                    Yaml yaml = new Yaml(this);
                    yaml.write(pw);
                    pw.close();
                } catch (Exception e) {
                    ErrorDialog errorD = new ErrorDialog(mainFrame);
                    errorD.open("Error loading pattern",e.getMessage());
                    errorD.dispose();
                    System.out.println(e.getMessage());
                }
            }
        }

        private void loadPat() {
            fc.setFileFilter(patFF);
            int res = fc.showOpenDialog(mainFrame);
            if(res != JFileChooser.APPROVE_OPTION) return;
            File f = fc.getSelectedFile();

//            JFileChooser fid = new JFileChooser(mainFrame, "Load pattern",
//                    JFileChooser.LOAD);
//            fid.setVisible(true);
//            String dir = fid.getDirectory();
//            String filename = fid.getFile();
//            fid.dispose();
            if (f != null)
                try {
                    //File f = new File(dir, filename);
                    FileReader fr = new FileReader(f);
                    BufferedReader br = new BufferedReader(fr);
                    Yaml yaml = new Yaml(this);
                    yaml.read(br);
                    br.close();
                    if(yaml.group!=null) {
                        TessRule tr1 = TessRule.getTessRuleByName(yaml.group);
                        this.tickCheckbox(yaml.group);
                        ((ZoomedDrawableRegion) dr).zoom(yaml.zNumer,yaml.zDenom);

                        for(int i=0;i<3;++i)
                            fd.setVertex(i, yaml.vertX[i],yaml.vertY[i]);

                        this.curvertex = -1;
                        //System.out.printf("fd %d %d %d %d %d %d\n",fd.verticies[0].x,fd.verticies[0].y,fd.verticies[1].x,fd.verticies[1].y,fd.verticies[2].x,fd.verticies[2].y);

                        this.controller.setTesselation(tr1);
                        //System.out.printf("fd %d %d %d %d %d %d\n",fd.verticies[0].x,fd.verticies[0].y,fd.verticies[1].x,fd.verticies[1].y,fd.verticies[2].x,fd.verticies[2].y);
                        controller.applyTessellation();
                        //System.out.printf("fd %d %d %d %d %d %d\n",fd.verticies[0].x,fd.verticies[0].y,fd.verticies[1].x,fd.verticies[1].y,fd.verticies[2].x,fd.verticies[2].y);
                        imageChanged();
                    }
                } catch (Exception e) {
                    ErrorDialog errorD = new ErrorDialog(mainFrame);
                    errorD.open("Error loading pattern",e.getMessage());
                    errorD.dispose();
                    System.out.println(e.getMessage());
                }

        }


        /**
         * Gets an image in application/frame context 
         * @param imgloc either a URL or filename
         * @return loaded image or null on error
         */
        public static Image frameGetImage(String imgloc) {
            URI imgurl=null;
            String filename=null;
            Image imgin;

            // first try if its a a full URL
            try
            {
                imgurl = new URI(imgloc);
                var imageURL = imgurl.toURL();
                imgin = Toolkit.getDefaultToolkit().getImage(imageURL);
            }
            catch(MalformedURLException | URISyntaxException | IllegalArgumentException e)
            {
                // then see if its a regular file
                filename = System.getProperty("user.dir")+System.getProperty("file.separator")+imgloc;
                imgin = Toolkit.getDefaultToolkit().getImage(filename);
            }
            if(DEBUG) {
                if(imgurl!=null) System.out.println("URL "+imgurl.toString());
                else System.out.println(filename);
            }
//            imageFilename = filename;

            if(imgin==null)
            {
                System.out.println("NULL image");
                return null;
            }
            return imgin;
        }

        protected void paste() {
            Image img = getClipboardImage();
            if (img != null && dr.loadImage(img)) {
                imageChanged();
            }
        }

        // This class is used to hold an image while on the clipboard.
        public static class ImageSelection implements Transferable {
            private final Image image;

            public ImageSelection(Image image) {
                this.image = image;
            }

            // Returns supported flavors
            public DataFlavor[] getTransferDataFlavors() {
                return new DataFlavor[]{DataFlavor.imageFlavor};
            }

            // Returns true if flavor is supported
            public boolean isDataFlavorSupported(DataFlavor flavor) {
                return DataFlavor.imageFlavor.equals(flavor);
            }

            // Returns image
            public Object getTransferData(DataFlavor flavor) throws UnsupportedFlavorException, IOException {
                if (!DataFlavor.imageFlavor.equals(flavor)) {
                    throw new UnsupportedFlavorException(flavor);
                }
                return image;
            }
        }

        protected void copy() {
            controller.applyFull(dr);
            copyImageToClipboard(dr.getActiveImage());
        }

        protected void copyFull() {
            copyFullImageToClipboard(dr.getActiveImage());
        }

        private void copyImageToClipboard(Image image) {
            // Work around a Sun bug that causes a hang in "sun.awt.image.ImageRepresentation.reconstruct".
            new javax.swing.ImageIcon(image); // Force load.
            BufferedImage newImage = new BufferedImage(image.getWidth(null), image.getHeight(null), BufferedImage.TYPE_INT_ARGB);

            Graphics2D g = newImage.createGraphics();
            g.setClip(0, 0, image.getWidth(null), image.getHeight(null));
            g.drawImage(image, 0, 0, null);

            ImageSelection imageSelection = new ImageSelection(newImage);
            Toolkit toolkit = Toolkit.getDefaultToolkit();
            toolkit.getSystemClipboard().setContents(imageSelection, null);
        }

        private void copyFullImageToClipboard(Image image) {
            // Work around a Sun bug that causes a hang in "sun.awt.image.ImageRepresentation.reconstruct".
            new javax.swing.ImageIcon(image); // Force load.
            BufferedImage newImage = new BufferedImage(image.getWidth(null), image.getHeight(null), BufferedImage.TYPE_INT_ARGB);

            Graphics2D g = newImage.createGraphics();
            g.setClip(0, 0, image.getWidth(null), image.getHeight(null));
            this.paintCanvas(g);
            //	          g.drawImage(image, 0, 0, null);
            //	          fd.paintSymetries(g, this.controller.tr);

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

        protected void resizeImage(int w, int h, int xoff, int yoff) {
            dr.resize(w, h, xoff, yoff);
            fd.shift(xoff,yoff);
            imageChanged();

        }

        protected void rescaleImage(int w, int h) {
            fd.rescale(w/((float) dr.baseRect.width), h/((float) dr.baseRect.height));
            dr.rescale(w, h);
            imageChanged();
        }

        void flip(String com) {
            fd.flip(com,dr.destRect.width,dr.destRect.height,controller.tr);
            dr.flip(com);
            imageChanged();
        }

        JMenu viewMenu;

        JMenu drawMenu;
        public void componentHidden(ComponentEvent arg0) {/*ignore*/}

        public void componentMoved(ComponentEvent arg0) {/*ignore*/}

        public void componentResized(ComponentEvent arg0) {
            //if(DEBUG) 
            System.out.println("Comp resize");
            System.out.println(jsp.getViewportBorderBounds());
            //		System.out.println(sp.getScrollPosition());
            //                System.out.println(this.myCanvas.getSize());
            //                System.out.println(this.myCanvas.getPreferredSize());
            //                System.out.println(this.myCanvas.getSize());
            //Point p = (Point) jsp.getScrollPosition().clone();
            //p.move(this.offset.x,this.offset.y);
            //dr.setViewport(new Rectangle(p,sp.getViewportSize()));
            //                System.out.println(this.myCanvas.getSize());
            dr.setViewport(jsp.getViewportBorderBounds());
            if(first) {
                fd.resetDomain(dr.dispRect);
                controller.tr.firstCall = true;
                controller.calcGeom();
                first=false;
            }
            myCanvas.setSize(dr.destRect.width, dr.destRect.height);
            //sp.doLayout();

            //sp.doLayout();
            //                System.out.println(this.myCanvas.getSize());
            controller.redraw();
            //                System.out.println(this.myCanvas.getSize());
        }

        public void componentShown(ComponentEvent arg0) {/*ignore*/}

        public void adjustmentValueChanged(AdjustmentEvent arg0) {
            Rectangle rect =  jsp.getViewport().getViewRect();
            if (DEBUG)
                System.out.println("Adjustment value changed"+rect);
            //TODO cope with centered images
           dr.setViewport(rect);
           controller.redraw();
        }

        public String titleFilename="";
		private boolean isFullScreen;
        public void imageChanged() {
            //		imgout = dr.getActiveImage();
            myCanvas.setSize(dr.destRect.width, dr.destRect.height);
            myCanvas.setPreferredSize(dr.destRect.getSize());
            //TODO sp.doLayout();
            //controller.showOriginal();
            controller.redraw();
            setTitle();
        }

        public void setTitle(String newTitle) {
            this.titleFilename = newTitle;
            setTitle();
        }
        public void setTitle() {
            this.mainFrame.setTitle("Wallpaper patterns: "+titleFilename+" "+dr.baseRect.width+" X "+dr.baseRect.height);
        }

	    public void toggleFullScreen(JFrame frame) {
			if (gd.isFullScreenSupported()) {
				if (isFullScreen) {
					showNormalScreen(frame);

				} else {
					showFullScreen(frame);
				}
			}
		}

		GraphicsDevice gd = GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice();
		Rectangle oldBounds = null;
		
		public void showFullScreen(JFrame frame) {
			System.out.println("Entering Full-Screen");
			frame.dispose();
			hideControls();
			var menu = frame.getJMenuBar();
			menu.setVisible(false);
			frame.setExtendedState(JFrame.MAXIMIZED_BOTH);
			frame.setUndecorated(true);
			Rectangle bounds = frame.getGraphicsConfiguration().getBounds();
			System.out.println("Full-Screen"+bounds);
			oldBounds = dr.dispRect.getBounds();
			dr.resize(bounds.width, bounds.height, bounds.x, bounds.y);
			clearViewCheckboxes();
			imageChanged();
			gd.setFullScreenWindow(frame);
			myCanvas.requestFocus();
			isFullScreen = true;
		}

		public void showNormalScreen(JFrame frame) {
			System.out.println("Exiting Full-Screen");
			frame.dispose();
			frame.setUndecorated(false);
			frame.setExtendedState(JFrame.NORMAL);
			dr.resize(oldBounds.width, oldBounds.height, oldBounds.x, oldBounds.y);

			showControls();
			var menu = frame.getJMenuBar();
			menu.setVisible(true);
			frame.setVisible(true);
			gd.setFullScreenWindow(null);
			setDefaultViewCheckboxes();
			isFullScreen = false;
			myCanvas.requestFocus();
		}

		private void clearViewCheckboxes() {
			fd.drawCells = false;
			fd.drawTiles = false;
			fd.drawDomain = false;
			fd.drawSelectionPoints = false;
			fd.drawGlideLines = false;
			fd.drawReflectionLines = false;
			fd.drawRotationPoints = false;
			setViewCheckboxes();
		}

		private void setDefaultViewCheckboxes() {
			fd.drawCells = false;
			fd.drawTiles = false;
			fd.drawDomain = true;
			fd.drawSelectionPoints = true;
			fd.drawGlideLines = false;
			fd.drawReflectionLines = false;
			fd.drawRotationPoints = false;
			setViewCheckboxes();
		}

        @Override
		protected void setViewCheckboxes() {
			super.setViewCheckboxes();
			int num = viewMenu.getItemCount();
			for(int i=0;i<num;++i) {
				JCheckBoxMenuItem mi = (JCheckBoxMenuItem) viewMenu.getItem(i);
				if(mi==null) continue;
//				System.out.println("mi "+i+" "+mi.getText()+" "+mi.isSelected());
				if(mi.getText().equals("Cells")) {
					mi.setSelected(fd.drawCells);
				}
				else if(mi.getText().equals("Tiles")) {
					mi.setSelected(fd.drawTiles);
				}
				else if(mi.getText().equals("Selection domain")) {
					mi.setSelected(fd.drawDomain);
				}
				else if(mi.getText().equals("Selection points")) {
					mi.setSelected(fd.drawSelectionPoints);
				}
				else if(mi.getText().equals("Reflection lines")) {
					mi.setSelected(fd.drawReflectionLines);
				}
				else if(mi.getText().equals("Rotation points")) {
					mi.setSelected(fd.drawRotationPoints);
				}
				else if(mi.getText().equals("Glide-reflection lines")) {
					mi.setSelected(fd.drawGlideLines);
				}
				else if(mi.getText().equals("All symetries")) {
					mi.setSelected(fd.drawGlideLines && fd.drawReflectionLines && fd.drawRotationPoints);
				}
				else if(mi.getText().equals("Hide all symetries")) {
					mi.setSelected(!fd.drawGlideLines && !fd.drawReflectionLines && !fd.drawRotationPoints);
				}
			}
		}

}

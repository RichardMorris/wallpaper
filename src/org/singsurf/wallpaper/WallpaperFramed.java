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
import java.awt.print.PageFormat;
import java.awt.print.Printable;
import java.awt.print.PrinterException;
//import java.io.FilenameFilter;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;

import javax.swing.ButtonGroup;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JRadioButtonMenuItem;
import javax.swing.JScrollPane;
import javax.swing.ScrollPaneConstants;
import javax.swing.border.EmptyBorder;

import org.singsurf.wallpaper.animation.AnimationPath;
import org.singsurf.wallpaper.dialogs.ErrorDialog;
import org.singsurf.wallpaper.dialogs.ExpandDialog;
import org.singsurf.wallpaper.dialogs.JColourPicker;
import org.singsurf.wallpaper.dialogs.RescaleDialog;
import org.singsurf.wallpaper.dialogs.ResizeDialog;
import org.singsurf.wallpaper.tessrules.TessRule;


public class WallpaperFramed extends Wallpaper implements ActionListener, ComponentListener, AdjustmentListener {

	public FileController fileController;
	private static final boolean DEBUG = false;

	public static final String programName = "Wallpaper";
	public static final String programVersion = "1.7";
	public static final String programInfo = programName + " version " + programVersion;


    
	public WallpaperFramed(String imgfilename, int w, int h) {
		super(frameGetImage(imgfilename), w, h);
		fileController = new FileController(this);

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
        if(com.equals("exit"))
            System.exit(0);
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
                myCanvas.setCursor(pipet);  
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
                controller.flip(e.getActionCommand());
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

            JCheckBoxMenuItem splitMI = new JCheckBoxMenuItem("Split");
            splitMI.addItemListener(new ItemListener() {
				public void itemStateChanged(ItemEvent e) {
					int state = e.getStateChange();
					controller.split(state==ItemEvent.SELECTED);
				}});
            splitMI.addActionListener(this);
            splitMI.setActionCommand("split");
            imageMenu.add(splitMI);

            return imageMenu;
        }

        private JMenu buildEditMenu() {
            JMenu editMenu = new JMenu("Edit");
            JMenuItem copyMI = new JMenuItem("Copy");
            copyMI.setMnemonic(KeyEvent.VK_C);
            copyMI.addActionListener((e) -> {
				controller.copy();
			});
            copyMI.setActionCommand("copy");
            editMenu.add(copyMI);

            JMenuItem copyFullMI = new JMenuItem("Copy full");
            copyFullMI.addActionListener((e) -> {
            	controller.copyFull();
            });
            copyFullMI.setActionCommand("copyfull");
            editMenu.add(copyFullMI);

            JMenuItem pasteMI = new JMenuItem("Paste");
            pasteMI.setMnemonic(KeyEvent.VK_V);
            pasteMI.addActionListener((e) -> {
				controller.paste();
			});
            pasteMI.setActionCommand("paste");
            editMenu.add(pasteMI);
            
            return editMenu;
        }

        private JMenu buildFileMenu() {
            JMenu fileMenu = new JMenu("File");

            JMenuItem loadMI = new JMenuItem("Load Image");
            loadMI.setMnemonic(KeyEvent.VK_L);
            loadMI.addActionListener((e) -> {
            	fileController.load();
            });
            loadMI.setActionCommand("load image");
            fileMenu.add(loadMI);

            JMenuItem loadGMI = new JMenuItem("Load Pattern");
            loadGMI.setMnemonic(KeyEvent.VK_P);
            loadGMI.addActionListener((e) -> {
				fileController.loadPat();
			});
            loadGMI.setActionCommand("load pattern");
            fileMenu.add(loadGMI);
            
            JMenuItem loadSeqMI = new JMenuItem("Load Sequence");
            //loadSeqMI.setMnemonic(KeyEvent.VK_P);
            loadSeqMI.addActionListener((e) -> {
            	fileController.loadAnimSequence();
            });
            loadSeqMI.setActionCommand("load sequence");
            fileMenu.add(loadSeqMI);
            
            JMenuItem saveMI = new JMenuItem("Save Image");
            saveMI.setMnemonic(KeyEvent.VK_S);
            saveMI.addActionListener((e) -> {
				fileController.save();
			});
            saveMI.setActionCommand("save image");
            fileMenu.add(saveMI);
            
            JMenuItem savetileMI = new JMenuItem("Save Tile");
            savetileMI.setMnemonic(KeyEvent.VK_T);
            savetileMI.addActionListener((e) -> {	
            	fileController.saveTile();
            });
            savetileMI.setActionCommand("save tile");
            fileMenu.add(savetileMI);
            
            JMenuItem saveBigMI = new JMenuItem("Save Expanded");
            saveBigMI.setMnemonic(KeyEvent.VK_E);
            saveBigMI.addActionListener((e) -> {
				fileController.saveBig();	
			});
            saveBigMI.setActionCommand("save big");
            fileMenu.add(saveBigMI);
            
            JMenuItem saveGMI = new JMenuItem("Save Pattern");
            saveGMI.setMnemonic(KeyEvent.VK_L);
            saveGMI.addActionListener((e) -> {
            	fileController.savePat();	
            });	
            saveGMI.setActionCommand("save pattern");
            fileMenu.add(saveGMI);
            
            JMenuItem appendGMI = new JMenuItem("Append Sequence");
//            appendGMI.setMnemonic(KeyEvent.VK_L);
            appendGMI.addActionListener((e) -> {
				fileController.appendAnim(this);
			});
            fileMenu.add(appendGMI);
            
            JMenuItem printMI = new JMenuItem("Print ...");
            printMI.setMnemonic(KeyEvent.VK_P);
            printMI.addActionListener((e) -> {
				fileController.printImage();
			});
            printMI.setActionCommand("print");
            fileMenu.add(printMI);
            
            fileMenu.addSeparator();
            
            JMenuItem exitMI = new JMenuItem("Exit");
            exitMI.setMnemonic(KeyEvent.VK_X);
            exitMI.addActionListener(this);
            exitMI.setActionCommand("exit");
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
            jsp.setBorder(new EmptyBorder(0, 0, 0, 0));
            return jsp;
        }
                
        protected JMenu buildAnimationMenu() {
    	    stopBut.setVisible(true);
    	    stopBut.setEnabled(true);
    	
    	    JMenu animateMenu = new JMenu("Animation");
//    	    String animations[] = {"up","down","left","right","NE","NW","SE","SW","rotate","bounce","smooth"};
    	    String animations[] = AnimationPath.getPathNames();	
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
    	    
    	    JMenuItem mi2 = new JMenuItem("Next frame");
    	    mi2.addActionListener((e) -> {
		    	animController.nextYaml();
		    });
    	    animateMenu.add(mi2);
    	    
    	    return animateMenu;
    	}

    	public boolean isFullScreen() {
    		return isFullScreen;
    	}

        //@Override
       public Image getImage(String loc) {
            return frameGetImage(loc);
        }

      

		
		@Override
	public void nextFrame() {
			animController.nextYaml();
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

        JMenu viewMenu;

        public void componentHidden(ComponentEvent arg0) {/*ignore*/}

        public void componentMoved(ComponentEvent arg0) {/*ignore*/}

        public void componentResized(ComponentEvent arg0) {
            //if(DEBUG) 
            System.out.println("Comp resize");
            System.out.println(jsp.getViewportBorderBounds());

            dr.setViewport(jsp.getViewportBorderBounds());
            if(first) {
                fd.resetDomain(dr.dispRect);
                controller.tr.firstCall = true;
                controller.calcGeom();
                first=false;
            }
            myCanvas.setSize(dr.destRect.width, dr.destRect.height);
            controller.redraw();
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
		boolean isFullScreen = false;
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
			oldBounds = dr.baseRect.getBounds();
			dr.makeDest(bounds.width, bounds.height);
			dr.makeOutImage();
//			dr.resize(bounds.width, bounds.height, bounds.x, bounds.y);
			clearViewCheckboxes();
			isFullScreen = true;
			imageChanged();
			gd.setFullScreenWindow(frame);
			myCanvas.requestFocus();
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

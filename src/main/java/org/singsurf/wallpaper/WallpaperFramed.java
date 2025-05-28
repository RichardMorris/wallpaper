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
import java.net.URL;
import java.text.MessageFormat;

import javax.imageio.ImageIO;
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

import org.singsurf.wallpaper.animation.AnimationController;
import org.singsurf.wallpaper.animation.AnimationPath;
import org.singsurf.wallpaper.dialogs.CropDialog;
import org.singsurf.wallpaper.dialogs.ErrorDialog;
import org.singsurf.wallpaper.dialogs.ExpandDialog;
import org.singsurf.wallpaper.dialogs.JColourPicker;
import org.singsurf.wallpaper.dialogs.RescaleDialog;
import org.singsurf.wallpaper.tessrules.TessRule;


public class WallpaperFramed extends Wallpaper implements ActionListener, ComponentListener, AdjustmentListener {
	private static final long serialVersionUID = 1L;
	private static final boolean DEBUG = false;

	public static final String programName = Messages.getString("Program.name"); //$NON-NLS-1$
	public static final String programVersion = Messages.getString("Program.version"); //$NON-NLS-1$
	public static final String programInfo = programName + " version " + programVersion; //$NON-NLS-1$


	public FileController fileController;
	/** Scrollable pane */
    public JScrollPane jsp;
    
	public WallpaperFramed(String imgfilename, int w, int h) {
		super(frameGetImage(imgfilename), w, h);
		fileController = new FileController(this);
		animController = new AnimationController(this,controller);
	}

    @Override
    protected DrawableRegion buildDrawableRegion() {
        return new ZoomedDrawableRegion(this);
    }

    @Override
    public void keyPressed(KeyEvent e) {
		if (e.getKeyCode() == KeyEvent.VK_F11) {
			toggleFullScreen(mainFrame);
		}
		if (e.getKeyCode() == KeyEvent.VK_ESCAPE && isFullScreen) {
			toggleFullScreen(mainFrame);
		}
		if (e.getKeyCode() == KeyEvent.VK_SPACE) {
			animController.stopStartAnim();
		}
		if (e.getKeyCode() == KeyEvent.VK_N) {
			animController.nextYaml();
		}
		if (e.getKeyCode() == KeyEvent.VK_P) {
			animController.prevYaml();
		}


        super.keyPressed(e);
    }

    public void actionPerformed(ActionEvent e) {
        String com = e.getActionCommand();
        if(com.equals(Messages.getString("Command.exit"))) //$NON-NLS-1$
            System.exit(0);
        else if(com.equals(Messages.getString("Command.image.crop"))) { //$NON-NLS-1$
            final CropDialog rd = new CropDialog(mainFrame,this);

            rd.open(dr.baseRect.width,dr.baseRect.height);
            if (rd.ok)
                resizeImage(-rd.xoff, -rd.yoff, rd.width, rd.height);
            else
                controller.redraw();
        }
        else if(com.equals(Messages.getString("Command.image.expand"))) { //$NON-NLS-1$
            ExpandDialog ed = new ExpandDialog(mainFrame,this);

            ed.open(dr.baseRect.width,dr.baseRect.height);
            if (ed.ok)
                resizeImage(ed.xoff, ed.yoff, ed.imageWidth, ed.imageHeight);
            else
                controller.redraw();
        }
        else if(com.equals(Messages.getString("Command.image.resize"))) { //$NON-NLS-1$
            final RescaleDialog rs = new RescaleDialog(mainFrame,this);

            rs.open(dr.baseRect.width, dr.baseRect.height);
            if (rs.isOk())
                rescaleImage(rs.getNewWidth(), rs.getNewHeight());
            else
                controller.redraw();
        }
        else if(com.startsWith(Messages.getString("Command.bg.prefix"))) { //$NON-NLS-1$
            Color col=backgroundColour;
            String label = com.substring(3);
            TessRule.tileBackground = false;
            if(label.equals(Messages.getString("Command.bg.tile"))) { //$NON-NLS-1$
                TessRule.tileBackground = true;
            }
            else if(label.equals(Messages.getString("Command.bg.black"))) { //$NON-NLS-1$
                col = Color.black;
            }
            else if(label.equals(Messages.getString("Command.bg.white"))) { //$NON-NLS-1$
                col = Color.white;
            }
            else if(label.equals(Messages.getString("Command.bg.other"))) { //$NON-NLS-1$
                colD.open(backgroundColour);
                if(colD.isOk())
                    col = colD.getCol();
            }
            else if(label.equals(Messages.getString("Command.bg.pick"))) { //$NON-NLS-1$
                mouseMode = MOUSE_PIPET;
                myCanvas.setCursor(pipet);  
            }
            setBGColor(col);
            controller.redraw();
        }
        else if(com.startsWith(Messages.getString("Command.anim.prefix"))) { //$NON-NLS-1$
            String label = com.substring(5);
            //"up","down","left","right","rotate","Stop"
            if(label.equals(Messages.getString("Command.anim.start"))) { //$NON-NLS-1$
                animController.stopAnim();
            }
            else startAnim(label);

        }
        else if(com.equals(Messages.getString("Command.about"))) { //$NON-NLS-1$
            final ErrorDialog rs = new ErrorDialog(mainFrame,Messages.getString("Dialog.about")); //$NON-NLS-1$

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
        }};
        
        protected ItemListener viewActionListener = new ItemListener() {
        	boolean recursive=false;
        	
            public void itemStateChanged(ItemEvent e) {
                JCheckBoxMenuItem cbmi = (JCheckBoxMenuItem) e.getItemSelectable();
                String label = cbmi.getActionCommand();
            	if(recursive) return;
            	recursive=true;
                if(label.equals(Messages.getString("Command.view.cells"))) //$NON-NLS-1$
                    fd.drawCells = cbmi.isSelected();
                if(label.equals(Messages.getString("Command.view.mirrors"))) //$NON-NLS-1$
                    fd.drawReflectionLines = cbmi.isSelected();
                if(label.equals(Messages.getString("Command.view.rotations"))) //$NON-NLS-1$
                    fd.drawRotationPoints = cbmi.isSelected();
                if(label.equals(Messages.getString("Command.view.glides"))) //$NON-NLS-1$
                    fd.drawGlideLines = cbmi.isSelected();
                if(label.equals(Messages.getString("Command.view.domain"))) //$NON-NLS-1$
                    fd.drawDomain = cbmi.isSelected();
                if(label.equals(Messages.getString("Command.view.sel_pts"))) //$NON-NLS-1$
                    fd.drawSelectionPoints = cbmi.isSelected();
                if(label.equals(Messages.getString("Command.view.tiles"))) //$NON-NLS-1$
                    fd.drawTiles = cbmi.isSelected();
                if(label.equals(Messages.getString("Command.view.all"))) { //$NON-NLS-1$
                    fd.drawGlideLines = cbmi.isSelected();
                    fd.drawReflectionLines = cbmi.isSelected();
                    fd.drawRotationPoints = cbmi.isSelected();
                }
                if(label.equals(Messages.getString("Command.view.hide"))) { //$NON-NLS-1$
                    fd.drawGlideLines = !cbmi.isSelected();
                    fd.drawReflectionLines = !cbmi.isSelected();
                    fd.drawRotationPoints = !cbmi.isSelected();
                }

//                imageChanged();
        	    controller.redraw();
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
        JMenuBar buildMenu() {
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
            JMenu menu = new JMenu(Messages.getString("Menu.window")); //$NON-NLS-1$
            JMenuItem mi = new JMenuItem(Messages.getString("Menu.window.fullscreen")); //$NON-NLS-1$
            mi.addActionListener((e) -> toggleFullScreen(mainFrame));
            menu.add(mi);
			return menu;
		}

		private JMenu buildOptionsMenu() {
            JMenu optionsMenu = new JMenu(Messages.getString("Menu.options")); //$NON-NLS-1$
            JMenu backgroundMenu = new JMenu(Messages.getString("Menu.options.bgcolour")); //$NON-NLS-1$
            String colours[] = {Messages.getString("Command.bg.tile"),Messages.getString("Command.bg.black"),Messages.getString("Command.bg.white"),Messages.getString("Command.bg.other"),Messages.getString("Command.bg.pick")}; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$
            for(int i=0;i<colours.length;++i) {
                JMenuItem mi = new JMenuItem(colours[i]);
                mi.setActionCommand(Messages.getString("Command.bg.prefix")+colours[i]); //$NON-NLS-1$
                mi.addActionListener(this);
                backgroundMenu.add(mi);
            }
            optionsMenu.add(backgroundMenu);
            
            JCheckBoxMenuItem cvmi = new JCheckBoxMenuItem(Messages.getString("Menu.options.constrainverticies")); //$NON-NLS-1$
            cvmi.addItemListener(new ItemListener() {

                public void itemStateChanged(ItemEvent arg0) {
                    int state = arg0.getStateChange();
                    switch(state) {
                    case ItemEvent.SELECTED:
                        controller.constrainVertices = true;
                        controller.setText(Messages.getString("Info.constrain1") + //$NON-NLS-1$
                        		Messages.getString("Info.constrain2") + //$NON-NLS-1$
                        		Messages.getString("Info.constrain3")); //$NON-NLS-1$
                        controller.calcGeom();
                        controller.redraw();
                        break;
                    case ItemEvent.DESELECTED:
                        controller.constrainVertices = false;
                        break;
                    }

                }});
            optionsMenu.add(cvmi);

            JCheckBoxMenuItem scmi = new JCheckBoxMenuItem(Messages.getString("Menu.options.showcoords")); //$NON-NLS-1$
            scmi.setActionCommand(Messages.getString("Command.showcoords")); //$NON-NLS-1$
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
            viewMenu = new JMenu(Messages.getString("Menu.view")); //$NON-NLS-1$

            String views[] = {Messages.getString("Menu.view.cells"),Messages.getString("Menu.view.tiles"),Messages.getString("Menu.view.domain"),Messages.getString("Menu.view.selpts"),"-", //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$
                    Messages.getString("Menu.view.all"),"-", //$NON-NLS-1$ //$NON-NLS-2$
                    Messages.getString("Menu.view.mirrors"),Messages.getString("Menu.view.rotations"),Messages.getString("Menu.view.glides")}; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
            String viewKeys[] = {Messages.getString("Command.view.cells"),Messages.getString("Command.view.tiles"),Messages.getString("Command.view.domain"),Messages.getString("Command.view.sel_pts"),"-", //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$
                    Messages.getString("Command.view.all"),"-", //$NON-NLS-1$ //$NON-NLS-2$
                    Messages.getString("Command.view.mirrors"),Messages.getString("Command.view.rotations"),Messages.getString("Command.view.glides")}; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
            boolean viewStates[] = {false,false,true,true,false,
                    false,false,
                    false,false,false};
            for(int i=0;i<views.length;++i) {
                if(views[i].equals("-")) { //$NON-NLS-1$
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
            JMenu imageMenu = new JMenu(Messages.getString("Menu.image")); //$NON-NLS-1$

            JMenu zoomMenu = new JMenu(Messages.getString("Menu.image.zoom")); //$NON-NLS-1$
            ButtonGroup group = new ButtonGroup();
            String factorString = Messages.getString("Menu.image.zoom.factors");  //$NON-NLS-1$
            String factors[] = factorString.split(","); //$NON-NLS-1$ 
            for(var label: factors) {
                JRadioButtonMenuItem mi = new JRadioButtonMenuItem(label);
                mi.addItemListener(zoomItemListener);
                if(label=="1") mi.setSelected(true); //$NON-NLS-1$
                zoomMenu.add(mi);
                group.add(mi);
            }
            imageMenu.add(zoomMenu);

            JMenuItem resizeMI = new JMenuItem(Messages.getString("Menu.image.crop")); //$NON-NLS-1$
            resizeMI.addActionListener(this);
            resizeMI.setActionCommand(Messages.getString("Command.image.crop")); //$NON-NLS-1$
            imageMenu.add(resizeMI);

            JMenuItem expandMI1 = new JMenuItem(Messages.getString("Menu.image.expand")); //$NON-NLS-1$
            expandMI1.addActionListener(this);
            expandMI1.setActionCommand(Messages.getString("Command.image.expand")); //$NON-NLS-1$
            imageMenu.add(expandMI1);

            JMenuItem rescaleMI = new JMenuItem(Messages.getString("Menu.image.rescale")); //$NON-NLS-1$
            rescaleMI.addActionListener(this);
            rescaleMI.setActionCommand(Messages.getString("Command.image.resize")); //$NON-NLS-1$
            imageMenu.add(rescaleMI);


            JMenu flipMI = new JMenu(Messages.getString("Menu.image.flip_rotate")); //$NON-NLS-1$
            JMenuItem flipX = new JMenuItem(Wallpaper.FLIP_X);
            flipX.addActionListener(e ->  controller.flip(Wallpaper.FLIP_X));
            flipMI.add(flipX);
            JMenuItem flipY = new JMenuItem(Wallpaper.FLIP_Y);
            flipY.addActionListener(e ->  controller.flip(Wallpaper.FLIP_Y));
            flipMI.add(flipY);
            JMenuItem rot90 = new JMenuItem(Wallpaper.FLIP_90);
            rot90.addActionListener(e ->  controller.flip(Wallpaper.FLIP_90));
            flipMI.add(rot90);
            JMenuItem rot180 = new JMenuItem(Wallpaper.FLIP_180);
            rot180.addActionListener(e ->  controller.flip(Wallpaper.FLIP_180));
            flipMI.add(rot180);
            JMenuItem rot270 = new JMenuItem(Wallpaper.FLIP_270);
            rot270.addActionListener(e ->  controller.flip(Wallpaper.FLIP_270));
            flipMI.add(rot270);
            imageMenu.add(flipMI);

            JCheckBoxMenuItem splitMI = new JCheckBoxMenuItem(Messages.getString("Menu.image.split")); //$NON-NLS-1$
            splitMI.addItemListener(e ->
					controller.split(e.getStateChange()==ItemEvent.SELECTED));
            imageMenu.add(splitMI);

            return imageMenu;
        }

        private JMenu buildEditMenu() {
            JMenu editMenu = new JMenu(Messages.getString("Menu.edit")); //$NON-NLS-1$
            JMenuItem copyMI = new JMenuItem(Messages.getString("Menu.edit.copy")); //$NON-NLS-1$
            copyMI.setMnemonic(KeyEvent.VK_C);
            copyMI.addActionListener((e) -> {
				controller.copy();
			});
            copyMI.setActionCommand(Messages.getString("Command.edit.copy")); //$NON-NLS-1$
            editMenu.add(copyMI);

            JMenuItem copyFullMI = new JMenuItem(Messages.getString("Menu.edit.copyfull")); //$NON-NLS-1$
            copyFullMI.addActionListener((e) -> {
            	controller.copyFull();
            });
            copyFullMI.setActionCommand(Messages.getString("Command.edit.copyfull")); //$NON-NLS-1$
            editMenu.add(copyFullMI);

            JMenuItem pasteMI = new JMenuItem(Messages.getString("Menu.edit.paste")); //$NON-NLS-1$
            pasteMI.setMnemonic(KeyEvent.VK_V);
            pasteMI.addActionListener((e) -> {
				controller.paste();
			});
            pasteMI.setActionCommand(Messages.getString("Command.edit.paste")); //$NON-NLS-1$
            editMenu.add(pasteMI);
            
            return editMenu;
        }

        private JMenu buildFileMenu() {
            JMenu fileMenu = new JMenu(Messages.getString("Menu.file")); //$NON-NLS-1$

            JMenuItem loadMI = new JMenuItem(Messages.getString("Menu.file.load_img")); //$NON-NLS-1$
            loadMI.setMnemonic(KeyEvent.VK_L);
            loadMI.addActionListener((e) -> {
            	fileController.load();
            });
            loadMI.setActionCommand(Messages.getString("Command.file.load_img")); //$NON-NLS-1$
            fileMenu.add(loadMI);

            JMenuItem loadGMI = new JMenuItem(Messages.getString("Menu.file.load_pat")); //$NON-NLS-1$
            loadGMI.setMnemonic(KeyEvent.VK_P);
            loadGMI.addActionListener((e) -> {
				fileController.loadPat();
			});
            loadGMI.setActionCommand(Messages.getString("Command.file.load_pat")); //$NON-NLS-1$
            fileMenu.add(loadGMI);
            
            JMenuItem loadSeqMI = new JMenuItem(Messages.getString("Menu.file.load_seq")); //$NON-NLS-1$
            //loadSeqMI.setMnemonic(KeyEvent.VK_P);
            loadSeqMI.addActionListener((e) -> {
            	fileController.loadAnimSequence();
            });
            loadSeqMI.setActionCommand(Messages.getString("Command.file.load_seq")); //$NON-NLS-1$
            fileMenu.add(loadSeqMI);
            
            JMenuItem saveMI = new JMenuItem(Messages.getString("Menu.file.save_img")); //$NON-NLS-1$
            saveMI.setMnemonic(KeyEvent.VK_S);
            saveMI.addActionListener((e) -> {
				fileController.save();
			});
            saveMI.setActionCommand(Messages.getString("Command.file.save_img")); //$NON-NLS-1$
            fileMenu.add(saveMI);
            
            JMenuItem savetileMI = new JMenuItem(Messages.getString("Menu.file.save_tile")); //$NON-NLS-1$
            savetileMI.setMnemonic(KeyEvent.VK_T);
            savetileMI.addActionListener((e) -> {	
            	fileController.saveTile();
            });
            savetileMI.setActionCommand(Messages.getString("Command.file.save_tile")); //$NON-NLS-1$
            fileMenu.add(savetileMI);
            
            JMenuItem saveBigMI = new JMenuItem(Messages.getString("Menu.file.save_expanded")); //$NON-NLS-1$
            saveBigMI.setMnemonic(KeyEvent.VK_E);
            saveBigMI.addActionListener((e) -> {
				fileController.saveBig();	
			});
            saveBigMI.setActionCommand(Messages.getString("Command.file.save_expanded")); //$NON-NLS-1$
            fileMenu.add(saveBigMI);
            
            JMenuItem saveGMI = new JMenuItem(Messages.getString("Menu.file.save_pat")); //$NON-NLS-1$
            saveGMI.setMnemonic(KeyEvent.VK_L);
            saveGMI.addActionListener((e) -> {
            	fileController.savePat();	
            });	
            saveGMI.setActionCommand(Messages.getString("Command.file.save_pat")); //$NON-NLS-1$
            fileMenu.add(saveGMI);
            
            JMenuItem appendGMI = new JMenuItem(Messages.getString("Menu.file.append_seq")); //$NON-NLS-1$
//            appendGMI.setMnemonic(KeyEvent.VK_L);
            appendGMI.addActionListener((e) -> {
				fileController.appendAnim(this);
			});
            fileMenu.add(appendGMI);
            
            JMenuItem printMI = new JMenuItem(Messages.getString("Menu.file.print")); //$NON-NLS-1$
            printMI.setMnemonic(KeyEvent.VK_P);
            printMI.addActionListener((e) -> {
				fileController.printImage();
			});
            printMI.setActionCommand(Messages.getString("Command.file.print")); //$NON-NLS-1$
            fileMenu.add(printMI);
            
            fileMenu.addSeparator();
            
            JMenuItem exitMI = new JMenuItem(Messages.getString("Menu.file.exit")); //$NON-NLS-1$
            exitMI.setMnemonic(KeyEvent.VK_X);
            exitMI.addActionListener(this);
            exitMI.setActionCommand(Messages.getString("Command.file.exit")); //$NON-NLS-1$
            fileMenu.add(exitMI);
            return fileMenu;
        }

        @Override
        protected JComponent buildCanvasContainer(JComponent c) {
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
    	
    	    JMenu animateMenu = new JMenu(Messages.getString("Menu.anim")); //$NON-NLS-1$
    	    String animations[] = AnimationPath.getPathNames();	
    	    for(int i=0;i<animations.length;++i) {
	        	String label = animations[i];
    	        JMenuItem mi = new JMenuItem(label);
    	        mi.addActionListener((e) -> {
					setAnimationChoice(label);
		        });
    	        animateMenu.add(mi);
    	    }
    	    animateMenu.addSeparator();
    	    JMenuItem mi = new JMenuItem(Messages.getString("Menu.anim.stop")); //$NON-NLS-1$
    	    mi.setActionCommand(Messages.getString("Command.anin.stop")); //$NON-NLS-1$
    	    mi.addActionListener(this);
    	    animateMenu.add(mi);
    	    
    	    JMenuItem mi2 = new JMenuItem(Messages.getString("Menu.anim.next_frame")); //$NON-NLS-1$
    	    mi2.addActionListener((e) -> {
		    	animController.nextYaml();
		    });
    	    animateMenu.add(mi2);

    	    JMenuItem mi3 = new JMenuItem(Messages.getString("Menu.anim.prev_frame")); //$NON-NLS-1$
    	    mi3.addActionListener((e) -> {
		    	animController.prevYaml();
		    });
    	    animateMenu.add(mi3);

    	    return animateMenu;
    	}

    	public boolean isFullScreen() {
    		return isFullScreen;
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
            if(imgloc=="") { //$NON-NLS-1$
	            URL imgURL = WallpaperFramed.class.getResource(Messages.getString("Resource.default_img")); //$NON-NLS-1$
	            if (imgURL != null) {
	            	try {
						return ImageIO.read(imgURL);
					} catch (IOException e) {
						System.out.println(Messages.getString("Msg.error.load_default")+imgloc+"," +imgURL); //$NON-NLS-1$ //$NON-NLS-2$
					}
	            }
			}

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
                filename = System.getProperty("user.dir")+System.getProperty("file.separator")+imgloc; //$NON-NLS-1$ //$NON-NLS-2$
                imgin = Toolkit.getDefaultToolkit().getImage(filename);
            }
            if(DEBUG) {
                if(imgurl!=null) System.out.println(Messages.getString("Msg.url")+imgurl.toString()); //$NON-NLS-1$
                else System.out.println(filename);
            }
//            imageFilename = filename;

            if(imgin==null)
            {
                System.out.println(Messages.getString("Msg.error.load_img")); //$NON-NLS-1$
                return null;
            }
            return imgin;
        }


        // This class is used to hold an image while on the clipboard.
        public static class ImageSelection implements Transferable {
            private final Image image;

            public ImageSelection(Image img) {
               image = img;
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

        protected void resizeImage(int xoff, int yoff, int w, int h) {
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
            if(DEBUG) 
            	System.out.println("Comp resize"+jsp.getViewportBorderBounds()); //$NON-NLS-1$

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
                System.out.println("Adjustment value changed"+rect); //$NON-NLS-1$
           dr.setViewport(rect);
           controller.redraw();
        }

        public String titleFilename=""; //$NON-NLS-1$
		boolean isFullScreen = false;
        public void setTitle(String newTitle) {
            titleFilename = newTitle;
            setTitle();
        }
        public void setTitle() {
            mainFrame.setTitle(
            		MessageFormat.format(
                    		Messages.getString("Window.title_from_image"), //$NON-NLS-1$
                    		titleFilename,
                    		dr.baseRect.width,
                    		dr.baseRect.height));
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
			frame.dispose();
			hideControls();
			var menu = frame.getJMenuBar();
			menu.setVisible(false);
			frame.setExtendedState(JFrame.MAXIMIZED_BOTH);
			frame.setUndecorated(true);
			makeFullScreen(frame);
			clearViewCheckboxes();
			isFullScreen = true;
			imageChanged();
			gd.setFullScreenWindow(frame);
			myCanvas.requestFocus();
		}

		private void makeFullScreen(JFrame frame) {
			Rectangle bounds = frame.getGraphicsConfiguration().getBounds();
			oldBounds = dr.destRect.getBounds();
			dr.makeDest(bounds.width, bounds.height);
			dr.makeOutImage();
		}

		public void showNormalScreen(JFrame frame) {
			frame.dispose();
			frame.setUndecorated(false);
			frame.setExtendedState(JFrame.NORMAL);
			makeNormalScreen();

			showControls();
			var menu = frame.getJMenuBar();
			menu.setVisible(true);
			frame.setVisible(true);
			gd.setFullScreenWindow(null);
			setDefaultViewCheckboxes();
			isFullScreen = false;
			myCanvas.requestFocus();
		}

		private void makeNormalScreen() {
//			dr.resize(oldBounds.width, oldBounds.height, oldBounds.x, oldBounds.y);
			dr.makeDest(oldBounds.width, oldBounds.height);
			dr.makeOutImage();
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
				if(mi.getText().equals(Messages.getString("Menu.view.cells"))) { //$NON-NLS-1$
					mi.setSelected(fd.drawCells);
				}
				else if(mi.getText().equals(Messages.getString("Menu.view.tiles"))) { //$NON-NLS-1$
					mi.setSelected(fd.drawTiles);
				}
				else if(mi.getText().equals(Messages.getString("Menu.view.domain"))) { //$NON-NLS-1$
					mi.setSelected(fd.drawDomain);
				}
				else if(mi.getText().equals(Messages.getString("Menu.view.sel_pts"))) { //$NON-NLS-1$
					mi.setSelected(fd.drawSelectionPoints);
				}
				else if(mi.getText().equals(Messages.getString("Menu.view.mirrors"))) { //$NON-NLS-1$
					mi.setSelected(fd.drawReflectionLines);
				}
				else if(mi.getText().equals(Messages.getString("Menu.view.rotations"))) { //$NON-NLS-1$
					mi.setSelected(fd.drawRotationPoints);
				}
				else if(mi.getText().equals(Messages.getString("Menu.view.glides"))) { //$NON-NLS-1$
					mi.setSelected(fd.drawGlideLines);
				}
				else if(mi.getText().equals(Messages.getString("Menu.view.all"))) { //$NON-NLS-1$
					mi.setSelected(fd.drawGlideLines && fd.drawReflectionLines && fd.drawRotationPoints);
				}
				else if(mi.getText().equals(Messages.getString("Menu.view.hide"))) { //$NON-NLS-1$
					mi.setSelected(!fd.drawGlideLines && !fd.drawReflectionLines && !fd.drawRotationPoints);
				}
			}
		}

		public void setAnimationChoice(String label) {
			animateChoice.setSelectedItem(label);
		}

}

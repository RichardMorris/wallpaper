package org.singsurf.wallpaper;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import java.awt.print.PrinterException;
import java.awt.print.PrinterJob;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;

import javax.imageio.ImageIO;
import javax.swing.JFileChooser;
import javax.swing.filechooser.FileFilter;

import org.singsurf.wallpaper.animation.AnimationPath;
import org.singsurf.wallpaper.dialogs.AnimDialog;
import org.singsurf.wallpaper.dialogs.ErrorDialog;
import org.singsurf.wallpaper.dialogs.ExpandedSizeDialog;
import org.singsurf.wallpaper.tessrules.TessRule;

public class FileController {

	private static final boolean DEBUG = false;

	WallpaperFramed wall;
	
    JFileChooser fc = new JFileChooser(System.getProperty("user.dir"));
    {
	    //Add the preview pane.
        fc.setAccessory(new ImagePreview(fc));
    }

	List<WallpaperML> yamlList;

	int yamlListPoss;
	public FileController(WallpaperFramed wall) {
		this.wall = wall;
	}

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

	protected FileFilter seqFF = new FileFilter() {
	    @Override
	    public boolean accept(File file) {
	        if(file.isDirectory()) return true;
	        return(file.getName().toLowerCase().endsWith(".seq"));
	    }
	    @Override
	    public String getDescription() {
	        return "Wallpaper animation sequence files .seq";
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

	void load() {
	    System.out.println("load ...");
	    fc.setFileFilter(loadFF);
	    int res = fc.showOpenDialog(wall.mainFrame);
	    if(res != JFileChooser.APPROVE_OPTION) return;
	    File f = fc.getSelectedFile();
	    
	    if (f != null) {
	        wall.imageFilename = f.getPath();
	        System.out.println("name "+f.getName());
	        System.out.println("path "+f.getPath());
	        System.out.println("absolute "+f.getAbsoluteFile());
	        //WallpaperFramed.this.imageURL = null;
	        Image img;
	        try {
	        	System.out.println("canocal "+f.getCanonicalFile());
	            img = ImageIO.read(f);
	        } catch (IOException e) {
	            ErrorDialog errorD = new ErrorDialog(wall.mainFrame);
	            errorD.open("Error loading image "+wall.imageFilename+".","");
	            errorD.dispose();
	            return;
	        }
	        if (img != null && wall.dr.loadImage(img)) {
	        	wall.imageChanged();
	        }
	        else {
	            ErrorDialog errorD = new ErrorDialog(wall.mainFrame);
	            errorD.open("Error loading image "+wall.imageFilename+".","");
	            errorD.dispose();
	            return;
	        }
	        wall.setTitle(f.getName());
	        System.out.println("load done");
	    }
	
	}

	void loadPat() {
        fc.setFileFilter(patFF);
        loadPatSeq();
	}

	void loadPatSeq() {
	            int res = fc.showOpenDialog(wall.mainFrame);
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
	                    yamlList = WallpaperML.read(br);
	                    yamlListPoss = 0;
	                    br.close();
	                    nextYaml(wall);
	                } catch (Exception e) {
	                    ErrorDialog errorD = new ErrorDialog(wall.mainFrame);
	                    errorD.open("Error loading pattern",e.getMessage());
	                    errorD.dispose();
	                    System.out.println(e.getMessage());
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

	void save() {
	    fc.setFileFilter(saveFF);
	    int res = fc.showSaveDialog(wall.mainFrame);
	    if(res != JFileChooser.APPROVE_OPTION) return;
	    File f = fc.getSelectedFile();
	    if (f != null)
	        try {
	            int denom = ((ZoomedDrawableRegion) wall.dr).zoomDenom;
				int numer = ((ZoomedDrawableRegion) wall.dr).zoomNumer;
				wall.fd.zoom(((float) denom)/numer);
	             ((ZoomedDrawableRegion) wall.dr).zoom(1,1);	
	
	             if(wall.controller.showingOriginal) {
	            	 wall.controller.showOriginal();
	             }
	             else {
	            	 wall.controller.applyTessellation();
	            	 wall.controller.calcGeom();
	            	 wall.controller.applyFull(wall.dr);
	             }
	
	            String type = getType(f.getName());
	            try {
	                Image img = wall.dr.getActiveImage();
	                BufferedImage bImg = new BufferedImage(
	                        img.getWidth(null),img.getHeight(null),
	                        BufferedImage.TYPE_INT_RGB);
	                Graphics g = bImg.getGraphics();
	                g.setClip(0, 0, img.getWidth(null),img.getHeight(null));
	                wall.paintCanvas(g);
	                ImageIO.write(bImg,type,f);
	
	            } catch (IOException e) {
	                ErrorDialog errorD = new ErrorDialog(wall.mainFrame);
	                errorD.open("Error loading image "+f.getAbsolutePath()+".","");
	                errorD.dispose();
	                return;
	            }
	
	            if (DEBUG)
	                System.out.println("Save " + f.getAbsolutePath());
	
	            wall.fd.zoom(((float)numer)/denom);
	            ((ZoomedDrawableRegion) wall.dr).zoom(numer,denom);	
	            wall.controller.calcGeom();
	            wall.controller.redraw();
	        } catch (Exception e) {
	            ErrorDialog errorD = new ErrorDialog(wall.mainFrame);
	            errorD.open("Error saving image",e.getMessage());
	            errorD.dispose();
	            System.out.println(e.getMessage());
	        }
	
	}
	/**
	 * Save an expanded version of the image.
	 *
	 */
	void saveBig() {
		final ExpandedSizeDialog esd = new ExpandedSizeDialog(wall.mainFrame);
	
		esd.open(wall.dr.baseRect.width, wall.dr.baseRect.height);
		if (!esd.isOk())
			return;
	
		fc.setFileFilter(ppmFF);
		int res = fc.showSaveDialog(wall.mainFrame);
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
	
		int denom = ((ZoomedDrawableRegion) wall.dr).zoomDenom;
		int numer = ((ZoomedDrawableRegion) wall.dr).zoomNumer;
		wall.fd.zoom(((float) denom) / numer);
		((ZoomedDrawableRegion) wall.dr).zoom(1, 1);
		wall.controller.calcGeom();
	
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
	
			DrawableRegion dr2 = new DrawableRegionTile(wall.dr, w, 1);
			for (int j = 0; j < h; ++j) {
				wall.controller.tr.replicate(dr2, wall.fd);
	
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
			ErrorDialog errorD = new ErrorDialog(wall.mainFrame);
			errorD.open("Error saving image", e.getMessage());
			errorD.dispose();
			System.out.println(e.getClass().getName() + ": " + e.getMessage());
		}
		wall.fd.zoom(((float) numer) / denom);
		wall.controller.calcGeom();
	
	}
	void savePat() {
	            fc.setFileFilter(patFF);
	            int res = fc.showSaveDialog(wall.mainFrame);
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
	                    WallpaperML yaml = new WallpaperML(wall);
	                    yaml.write(pw);
	                    pw.close();
	                } catch (Exception e) {
	                    ErrorDialog errorD = new ErrorDialog(wall.mainFrame);
	                    errorD.open("Error loading pattern",e.getMessage());
	                    errorD.dispose();
	                    System.out.println(e.getMessage());
	                }
	            }
	        }
	void saveTile() {
	            Rectangle rect = wall.fd.tileableRegion(wall.dr.dispRect); 
	            if(rect==null) {
	                ErrorDialog errorD = new ErrorDialog(wall.mainFrame);
	                errorD.open("A rectangular tile cannot be created for this geometry.","Select options->show cordinates show when rectangular tiles can be created.");
	                errorD.dispose();
	                return;
	            }
	            fc.setFileFilter(saveFF);
	            int res = fc.showSaveDialog(wall.mainFrame);
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
	
	                	wall.fd.zoom(((ZoomedDrawableRegion) wall.dr).zoomDenom);
	                	wall.controller.calcGeom();
	                	wall.controller.applyFull(wall.dr);
	                    //				Jimi.putImage(dr.getActiveImage(), dir + filename);
	                    String type = getType(f.getName());
	//                    File f = new File(dir,filename);
	                    try {
	                        Image img = wall.dr.getActiveImage();
	                        BufferedImage bImg = new BufferedImage(
	                                rect.width,rect.height,
	                                BufferedImage.TYPE_INT_RGB);
	                        Graphics g = bImg.getGraphics();
	                        g.drawImage(img, 0, 0, null);
	                        ImageIO.write(bImg,type,f);
	
	                    } catch (IOException e) {
	                        ErrorDialog errorD = new ErrorDialog(wall.mainFrame);
	                        errorD.open("Error loading image "+f.getAbsolutePath()+".","");
	                        errorD.dispose();
	                        return;
	                    }
	
	                    if (DEBUG)
	                        System.out.println("Save " + f.getAbsolutePath());
	                    wall.fd.zoom(1/((float) ((ZoomedDrawableRegion) wall.dr).zoomDenom));
	                    wall.controller.calcGeom();
	                } catch (Exception e) {
	                    ErrorDialog errorD = new ErrorDialog(wall.mainFrame);
	                    errorD.open("Error saving image",e.getMessage());
	                    errorD.dispose();
	                    System.out.println(e.getMessage());
	                }
	
	        }
	void appendAnim(WallpaperFramed wallpaperFramed) {
		final AnimDialog esd = new AnimDialog(wallpaperFramed.mainFrame, wallpaperFramed);
	
		esd.open();
		if (!esd.isOk())
			return;
	
	    fc.setFileFilter(seqFF);
	    int res = fc.showSaveDialog(wallpaperFramed.mainFrame);
	    if(res != JFileChooser.APPROVE_OPTION) return;
	    File f = fc.getSelectedFile();
	
	//        JFileChooser fid = new JFileChooser(mainFrame, "Save pattern",
	//                JFileChooser.SAVE);
	//        fid.setVisible(true);
	//        String dir = fid.getDirectory();
	//        String filename = fid.getFile();
	//        fid.dispose();
	    if (f != null) {
	        try {
	            //File f = new File(dir, filename);
	            FileWriter fw = new FileWriter(f,true);
	            PrintWriter pw = new PrintWriter(fw);
	            WallpaperML yaml = new WallpaperML(wallpaperFramed,wallpaperFramed.animController.path.getLabel(),esd.time);
				yaml.write(pw);
	            pw.close();
	        } catch (Exception e) {
	            ErrorDialog errorD = new ErrorDialog(wallpaperFramed.mainFrame);
	            errorD.open("Error loading pattern",e.getMessage());
	            errorD.dispose();
	            System.out.println(e.getMessage());
	        }
	    }
		
	}
	void nextYaml(WallpaperFramed wallpaperFramed) {
		if(yamlList==null) return;
		if(yamlListPoss>=yamlList.size()) {
			yamlListPoss = 0;
		}
		WallpaperML yaml = yamlList.get(yamlListPoss);
		++yamlListPoss;
		wallpaperFramed.fileController.processYaml(wallpaperFramed, yaml);
	}
	public void processYaml(WallpaperFramed wallpaperFramed, WallpaperML yaml) {
	//			if(yaml.restart)	 {
	//				if(yamlList!=null) {
	//					yamlListPoss = 0;
	//					nextYaml();
	//				}
	//				return;
	//			}
		if(yaml.group!=null) {
		    TessRule tr1 = TessRule.getTessRuleByName(yaml.group);
		    wallpaperFramed.tickCheckbox(yaml.group);
		    if(yaml.zNumer != -1)
		    {
		    	((ZoomedDrawableRegion) wallpaperFramed.dr).zoom(yaml.zNumer,yaml.zDenom);
		    }
		    for(int i=0;i<3;++i)
		        wallpaperFramed.fd.setVertex(i, yaml.vertX[i],yaml.vertY[i]);
	
		    wallpaperFramed.curvertex = -1;
		    wallpaperFramed.controller.setTesselation(tr1);
		    wallpaperFramed.controller.applyTessellation();
		    wallpaperFramed.imageChanged();
		}
		if(yaml.filename!=null) {
	        BufferedImage img;
			try {
				img = ImageIO.read(new File(yaml.filename));
			} catch (IOException e) {
				System.out.println("Error loading image "+yaml.filename+".");
				return;
			}
	
	//				var img = frameGetImage(yaml.filename);
			if (img != null && wallpaperFramed.dr.loadImage(img)) {
				Rectangle bounds = wallpaperFramed.mainFrame.getGraphicsConfiguration().getBounds();
				System.out.println("Full-Screen"+bounds);
				wallpaperFramed.dr.resize(bounds.width, bounds.height, bounds.x, bounds.y);
				wallpaperFramed.imageChanged();
				if(wallpaperFramed.isFullScreen) {
	//					oldBounds = dr.baseRect.getBounds();
				
				}
			}
			else {
				System.out.println("Error loading image "+yaml.filename+".");
				return;
			}
		}
		if(yaml.anim!=null) {
			var path = AnimationPath.getPathByName(yaml.anim, yaml.animSpeed, wallpaperFramed.dr.dispRect);
			wallpaperFramed.animController.setAnimationPath(path);
			wallpaperFramed.animController.startAnim();
		}
		if(yaml.repeat!=-1) {
			wallpaperFramed.setRepeat(yaml.repeat);
		}
	}
	public void loadAnimSequence() {
        fc.setFileFilter(seqFF);
        loadPatSeq();
	}

	void printImage() {
		PrinterJob printJob = PrinterJob.getPrinterJob();
	    printJob.setPrintable(wall.printable);
	    if (printJob.printDialog())
	      try { 
	        printJob.print();
	      } catch(PrinterException pe) {
	        System.out.println("Error printing: " + pe);
	      }
	}

}

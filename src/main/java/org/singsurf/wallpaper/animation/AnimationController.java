package org.singsurf.wallpaper.animation;

import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.text.MessageFormat;
import java.util.List;

import javax.imageio.ImageIO;
import javax.swing.Timer;

import org.singsurf.wallpaper.Controller;
import org.singsurf.wallpaper.Messages;
import org.singsurf.wallpaper.WallpaperFramed;
import org.singsurf.wallpaper.WallpaperML;
import org.singsurf.wallpaper.ZoomedDrawableRegion;
import org.singsurf.wallpaper.tessrules.TessRule;


public class AnimationController implements ActionListener {
	private static final boolean DEBUG = false;
	Controller controller;
	WallpaperFramed wall;
	Timer timer;
	public Timer timer2;

	boolean animRunning = false;

    public AnimationPath path = null;
    long count=0;
    long sum = 0;
	private List<WallpaperML> yamlList;
	private int yamlListPoss;

	
	public AnimationController(WallpaperFramed w,Controller c) {
		controller = c;
		wall = w;
		path = AnimationPath.getDefaultPath(w);
		timer = new Timer(50, this);
	}
	
    
	public void setAnimationPath(AnimationPath path) {
			this.path = path;
	}
	

	public void startAnim() {
	    if(DEBUG) 
	    	System.out.println("Start anim"); //$NON-NLS-1$
	    
	    path.firstItteration(controller.getFD());
	
	
	    wall.myCanvas.requestFocus();
	    TessRule.tileBackground=true;
	
	
	    //timer.scheduleAtFixedRate(animateTask, 0, 50);
	    animRunning = true;
	    wall.stopBut.setEnabled(true);
	    wall.stopBut.setText(Messages.getString("Anim.stop")); //$NON-NLS-1$
	    timer.start();
	}

	public void stopAnim() {
	    if(DEBUG) System.out.println("Stop anim"); //$NON-NLS-1$
	    timer.stop();
	    if(timer2 != null)
	    	timer2.stop();
	    animRunning = false;
	    wall.myCanvas.requestFocus();

	    wall.stopBut.setText(Messages.getString("Anim.start")); //$NON-NLS-1$
	}

	public void stopStartAnim() {
		if(DEBUG) System.out.println("startStopAnim"); //$NON-NLS-1$
	    if(animRunning)
	    	stopAnim();
	    else 
	    	startAnim();
	}

	long lastTime = 0;
	@Override
	public void actionPerformed(ActionEvent e) {
        long t1 = System.nanoTime();
        path.nextItteration(controller.getFD());
        controller.applyTessellation();
        long t2 = System.nanoTime();
        long elapsed = t2-t1;
        sum += elapsed;
        if(count%10==0) {
        	long currentTime = System.currentTimeMillis();
            if(DEBUG) System.out.println("Time to calculate 10 frames "+(sum/1000000)+"ms wall clock change "+(currentTime-lastTime)/10); //$NON-NLS-1$ //$NON-NLS-2$
            if(DEBUG) System.out.flush();
            sum=0;
            lastTime = currentTime;
        }
        ++count;
	}


	public void setRepeat(int repeat) {
		if(timer2 != null) {
			timer2.stop();
		}
		timer2 = new Timer(repeat*1000, (e) -> {
			if (animRunning) {
				stopAnim();
			}
			else {
				startAnim();
			}
			nextYaml();
		});
		timer2.setRepeats(false);
		timer2.start();
		
	}


	public void setYamlList(List<WallpaperML> yamlList1) {
		yamlList = yamlList1;
		yamlListPoss = -1;
		nextYaml();
	}




	void processYaml(WallpaperML yaml) {

		if(yaml.filename!=null) {
			loadAnimImage(yaml);
		}
		
		if(yaml.group!=null) {
		    TessRule tr1 = TessRule.getTessRuleByName(yaml.group);
		    if(tr1==null) {
		        System.out.println(MessageFormat.format(Messages.getString("Anim.error_tess_rule"),yaml.group)); //$NON-NLS-1$
		        return;
		    }
		    System.out.println(MessageFormat.format(Messages.getString("Anim.groupname"),tr1.name)); //$NON-NLS-1$
		    wall.tickCheckbox(tr1.name);
		    for(int i=0;i<3;++i)
		        wall.fd.setVertex(i, yaml.vertX[i],yaml.vertY[i]);
	
		    wall.curvertex = -1;
		    wall.controller.setTesselation(tr1);
		}

		if(yaml.anim!=null) {
			var path = AnimationPath.getPathByName(yaml.anim, yaml.animSpeed, wall.dr.dispRect);
		    System.out.println(MessageFormat.format(Messages.getString("Anim.path"),path.label)); //$NON-NLS-1$
			wall.setAnimationChoice(path.label);
		}
		if(yaml.repeat!=-1) {
			setRepeat(yaml.repeat);
		}
		if(yaml.description!=null) {
			wall.setText(yaml.description);
		}
		if(yaml.anim!=null)
			startAnim();
		else
	        controller.applyTessellation();

	}

	/**
	 * From a pattern like "dir/*.png" get a random image file.
	 * @param pattern
	 * @return
	 * @throws IOException 
	 */
	File get_random_image(String pattern) throws IOException {
		var ind = pattern.lastIndexOf('/');
		if(ind<0) return null;
		var dir = pattern.substring(0, ind);
		var filePattern = pattern.substring(ind+1);
		var dirFile = Path.of(dir);
		DirectoryStream<Path> stream = Files.newDirectoryStream(dirFile, filePattern);
		List<Path> files = new java.util.ArrayList<>();
		for(Path path : stream) {
			if(Files.isRegularFile(path)) {
				files.add(path);
			}
		}
		stream.close();
		int size = files.size();
		int rnd = (int) (Math.random() * size);
		return files.get(rnd).toFile();
	}

	public void loadAnimImage(WallpaperML yaml) {
		BufferedImage img;
		String fname;
		try {
				if(yaml.filename.contains("*")) {
					File f = get_random_image(yaml.filename);
					if(f==null) {
						System.out.println(MessageFormat.format(Messages.getString("Anim.error_loading_image"),yaml.filename)); //$NON-NLS-1$
						return;
					}
					System.out.println(MessageFormat.format(Messages.getString("Anim.load_image"),f.getPath())); //$NON-NLS-1$
					fname = f.getName();
					img = ImageIO.read(f);
				}
				else {
					System.out.println(MessageFormat.format(Messages.getString("Anim.load_image"),yaml.filename)); //$NON-NLS-1$
					fname = yaml.filename;
					img = ImageIO.read(new File(yaml.filename));
				}
			} catch (IOException e) {
			System.out.println(MessageFormat.format(Messages.getString("Anim.error_loading_image"),yaml.filename)); //$NON-NLS-1$
			return;
		}
		if(img==null) {
			System.out.println(MessageFormat.format(Messages.getString("Anim.error_loading_image"),yaml.filename)); //$NON-NLS-1$
			return;
		}
		
		boolean flag = ((ZoomedDrawableRegion) wall.dr).loadImageCore(img);
		if (flag) {
			((ZoomedDrawableRegion) wall.dr).zoom(yaml.zNumer,yaml.zDenom);
			if(wall.isFullScreen()) {
				Rectangle bounds = wall.mainFrame.getGraphicsConfiguration().getBounds();
				wall.dr.makeDest(bounds.width, bounds.height);

			}
			wall.dr.makeOutImage();
			if(!wall.isFullScreen()) wall.dr.calcDispRegion();
			wall.setTitle(fname);
			wall.imageFilename = yaml.filename;
		}
		else {
			System.out.println(MessageFormat.format(Messages.getString("Anim.error_loading_image"),yaml.filename)); //$NON-NLS-1$
			return;
		}
	}

	public void nextYaml() {
		if(yamlList==null) return;
		++yamlListPoss;
		if(yamlListPoss>=yamlList.size()) {
			yamlListPoss = 0;
		}
		WallpaperML yaml = yamlList.get(yamlListPoss);
		processYaml(yaml);
	}

	public void prevYaml() {
		if(yamlList==null) return;
		--yamlListPoss;
		if(yamlListPoss<0) {
			yamlListPoss = yamlList.size()- 1;
		}
		WallpaperML yaml = yamlList.get(yamlListPoss);
		processYaml(yaml);
	}


}

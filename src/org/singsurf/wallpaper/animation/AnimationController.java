package org.singsurf.wallpaper.animation;

import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.List;

import javax.imageio.ImageIO;
import javax.swing.Timer;

import org.singsurf.wallpaper.Controller;
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

	
	public AnimationController(WallpaperFramed w,Controller controller) {
		this.controller = controller;
		this.wall = w;
		path = AnimationPath.getDefaultPath(w);
		timer = new Timer(50, this);
	}
	
    
	public void setAnimationPath(AnimationPath path) {
			this.path = path;
	}
	

	public void startAnim() {
	    if(DEBUG) 
	    	System.out.println("Start anim");
	    
	    path.firstItteration(controller.getFD());
	
	
	    wall.myCanvas.requestFocus();
	    wall.setText("Hit space bar to stop");
	    TessRule.tileBackground=true;
	
	
	    //timer.scheduleAtFixedRate(animateTask, 0, 50);
	    animRunning = true;
	    wall.stopBut.setEnabled(true);
	    wall.stopBut.setText("Stop");
	    timer.start();
	}

	public void stopAnim() {
	    if(DEBUG) System.out.println("Stop anim");
	    timer.stop();
	    if(timer2 != null)
	    	timer2.stop();
	    animRunning = false;
	    wall.myCanvas.requestFocus();

	    wall.stopBut.setText("Start");
	}

	public void stopStartAnim() {
		if(DEBUG) System.out.println("startStopAnim");
	    if(animRunning)
	    	stopAnim();
	    else 
	    	startAnim();
	}


	@Override
	public void actionPerformed(ActionEvent e) {
        long t1 = System.currentTimeMillis();
        //long diff = (t1-lastTime)/50;
        //for(long i=count;i<=diff;++i)
        path.nextItteration(controller.getFD());
        controller.applyTessellation();
        long t2 = System.currentTimeMillis();
        long elapsed = t2-t1;
        sum += elapsed;
        if(count%10==0) {
            if(DEBUG) System.out.println("Elapse " + sum);
            if(DEBUG) System.out.flush();
            sum=0;
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
			wall.nextFrame();
		});
		timer2.setRepeats(false);
		timer2.start();
		
	}


	public void setYamlList(List<WallpaperML> yamlList) {
		this.yamlList = yamlList;
		this.yamlListPoss = -1;
		nextYaml();
	}




	void processYaml(WallpaperML yaml) {

		if(yaml.filename!=null) {
			loadAnimImage(yaml);
		}
		
		if(yaml.group!=null) {
		    TessRule tr1 = TessRule.getTessRuleByName(yaml.group);
		    wall.tickCheckbox(yaml.group);
		    for(int i=0;i<3;++i)
		        wall.fd.setVertex(i, yaml.vertX[i],yaml.vertY[i]);
	
		    wall.curvertex = -1;
		    wall.controller.setTesselation(tr1);
		}

		if(yaml.anim!=null) {
			var path = AnimationPath.getPathByName(yaml.anim, yaml.animSpeed, wall.dr.dispRect);
			wall.setAnimationChoice(path.label);
		}
		if(yaml.repeat!=-1) {
			setRepeat(yaml.repeat);
		}
		startAnim();
	}


	public void loadAnimImage(WallpaperML yaml) {
		System.out.println("Anim LoadImage "+yaml.filename);
		BufferedImage img;
		try {
			img = ImageIO.read(new File(yaml.filename));
		} catch (IOException e) {
			System.out.println("Error loading image "+yaml.filename+".");
			return;
		}
		if(img==null) {
			System.out.println("Error loading image "+yaml.filename+".");
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
			wall.setTitle(yaml.filename);
		}
		else {
			System.out.println("Error loading image "+yaml.filename+".");
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

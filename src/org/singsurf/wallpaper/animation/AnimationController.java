package org.singsurf.wallpaper.animation;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.Timer;

import org.singsurf.wallpaper.Controller;
import org.singsurf.wallpaper.Wallpaper;
import org.singsurf.wallpaper.tessrules.TessRule;


public class AnimationController implements ActionListener {
	private static final boolean DEBUG = false;
	Controller controller;
	Wallpaper wall;
	Timer timer;
	boolean animRunning = false;

    public AnimationPath path = null;
    long count=0;
    long sum = 0;

	
	public AnimationController(Wallpaper w,Controller controller) {
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
	    wall.startAll();
	}

	public void stopAnim() {
	    if(DEBUG) System.out.println("Stop anim");
	    timer.stop();
	    wall.stopAll();
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


}

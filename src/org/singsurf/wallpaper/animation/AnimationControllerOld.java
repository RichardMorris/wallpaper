package org.singsurf.wallpaper.animation;

import java.util.Timer;
import java.util.TimerTask;

import org.singsurf.wallpaper.Controller;
import org.singsurf.wallpaper.Wallpaper;
import org.singsurf.wallpaper.tessrules.TessRule;


public class AnimationControllerOld {
	private static final boolean DEBUG = false;
	Controller controller;
	Wallpaper wall;
	private TimerTask animateTask = null;

	boolean animRunning = false;

	public Thread animate = null;

	
	public AnimationControllerOld(Wallpaper w,Controller controller) {
		this.controller = controller;
		this.wall = w;
		path = new BounceAnimation(wall.dr.dispRect,1);
				
	}
	
    AnimationPath path = null;
    boolean first = true;
    boolean stop = false;
	private Timer timer = new Timer();
    
	public void setAnimationPath(AnimationPath path) {
			this.path = path;
			first = true;
	}
	

	public void startAnim() {
	    //if(DEBUG) 
	    	System.out.println("Start anim");
	    
	    path.firstItteration(controller.getFD());
	
	    if (animateTask != null)
	        animateTask.cancel();
	
	    wall.myCanvas.requestFocus();
	    wall.setText("Hit space bar to stop");
	    TessRule.tileBackground=true;
	    animateTask = new TimerTask() {
	        long count=0;
	        long sum = 0;
	        //long lastTime = System.currentTimeMillis();
	        //@Override
	        @Override
	        public void run() {
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
	            //lastTime = System.currentTimeMillis();
	        }
	
	    };
	
	    //timer.scheduleAtFixedRate(animateTask, 0, 50);
	    timer.schedule(animateTask, 0, 50);
	    animRunning = true;
	    wall.stopBut.setEnabled(true);
	    wall.stopBut.setText("Stop");
	}

	public void stopAnim() {
	    if(DEBUG) System.out.println("Stop anim");
	    if (animateTask != null)
	        animateTask.cancel();
	    animRunning = false;
	    wall.stopBut.setText("Start");
	}

	public void stopStartAnim() {
		System.out.println("startStopAnim");
	    if(animRunning)
	    	stopAnim();
	    else 
	    	startAnim();
	}

}

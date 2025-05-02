/*
Created 6 May 2007 - Richard Morris
*/
package org.singsurf.wallpaper;

import java.awt.Component;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.Rectangle;
import java.awt.Toolkit;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FilenameFilter;
import java.io.PrintStream;
import java.net.URL;

//import org.jdesktop.jdic.screensaver.ScreensaverSettings;
//import org.jdesktop.jdic.screensaver.SimpleScreensaver;
import org.singsurf.wallpaper.animation.AnimationPath;
import org.singsurf.wallpaper.animation.ShiftAnimation;
import org.singsurf.wallpaper.tessrules.TessRule;

public class WallSaver /* extends SimpleScreensaver */ {
    public class ScreensaverSettings {

		public String getProperty(String string) {
			// TODO Auto-generated method stub
			return null;
		}

	}

    public class ScreensaverContext {

		public ScreensaverSettings getSettings() {
			// TODO Auto-generated method stub
			return null;
		}

		public Component getComponent() {
			// TODO Auto-generated method stub
			return null;
		}



	}

    /** Dup output to file */
    static final boolean DUMP=false;
    /** Test mode */
    static final boolean TEST_MODE=false;
    /** Multiplier for size of fundamental domain */
    static final int sizeMul = 30;
    /** Number of frames for transition */
    static final int MAX_TRANS=15;
 
    String[] stdImageNames= new String[]{
	    "chub.jpg",
	    "fliss pics 009.jpg",
	    "fliss pics 030.jpg",
	    "fliss pics 119s.jpg",
	    "fliss pics 147.jpg",
	    "fliss pics004.jpg",
	    "fliss pics013a.jpg",
	    "hemero2.jpg",
	    "oca2.jpg",
	    "shells1.jpg",
	    "tile.jpg"
    };
    Image[] stdImages;
    
    String imageFileName=null;
    Image srcImg=null;
    Image tessImg=null;
    DrawableRegion dr;
    FundamentalDomain fd;
    TessRule tr;
    AnimationPath path;
    int speed;
    int change;
    long time;
    boolean randomMotion=false;
    boolean isDirectory=false;
    String group=null;
    /** Number of time init called */
    int count=0;
    public void init() {

	TessRule.tileBackground = true;
	ScreensaverSettings settings = getContext().getSettings();
	group = settings.getProperty("group");
	if(DUMP) {
	    try {
		FileOutputStream fos = new FileOutputStream("C:\\tmp\\zap"+System.currentTimeMillis());
		PrintStream ps = new PrintStream(fos);
//		System.getProperties().list(ps);
		ps.println("filename "+settings.getProperty("baseImage"));
		ps.println("group "+settings.getProperty("group"));
		ps.println("speed "+settings.getProperty("speed"));
		ps.println("size "+settings.getProperty("size"));
		ps.println("anim "+settings.getProperty("anim"));
		ps.println("change "+settings.getProperty("change"));
		ps.close();
	    } catch (FileNotFoundException e) {
		e.printStackTrace();
	    }
	}
	
	if(count==0)
	{
	    dr = new ExpandedDrawableRegion();
	    imageFileName = settings.getProperty( "baseImage" );
	    if(imageFileName==null) {
		if(TEST_MODE)
		    imageFileName = "C:\\eclipse\\workspace\\wallpaper\\distImages\\chub.jpg";
		else {
		//    imageFileName = System.getProperty("user.dir");
		    this.loadImagesFromResource();
		    this.loadStandardImage();
		}
	    }
	    else
		loadImage(imageFileName);
	    tr = TessRule.getTessRuleByName(group);
	}
	++count;
	Component c = getContext().getComponent();
	dr.setViewport(c.getBounds());
	tessImg = dr.getActiveImage();

	int size = this.getIntProp("size",1)*sizeMul;
	fd = new FundamentalDomain();
	double area = tr.approxArea();
	double xmul = Math.sqrt(1/(area * tr.approxAspect()));
	double ymul = Math.sqrt(tr.approxAspect() / area);
	//System.out.println("size "+size+" "+xmul+" "+ymul);
	fd.resetDomain(dr.srcRect,(int)(size*xmul),(int)(size*ymul));
	tr.calcFrame(fd,1, false);    
	tr.fixVerticies(fd);
	tr.calcFund(fd);

	speed = getIntProp("speed",1);
	path = AnimationPath.getPathByName(settings.getProperty("anim"),speed,dr.srcRect);
	if(path==null) {
	    path = new ShiftAnimation(-speed,speed);
	    randomMotion=true;
	}
	path.firstItteration(fd);

	if(TEST_MODE)
	    change = getIntProp("change",10);
	else
	    change = getIntProp("change",0);
	time = System.currentTimeMillis();
    }
    
    private ScreensaverContext getContext() {
		// TODO Auto-generated method stub
		return null;
	}

	int getIntProp(String key,int def) {
    	String value = getContext().getSettings().getProperty(key);
    	if(value==null) return def;
    	try {
    		return Integer.parseInt(value);
    	} catch(Exception e) {/*ignore*/}
    	return def;
    }
   
    FilenameFilter ff = new FilenameFilter() {

		public boolean accept(File dir, String name) {
			if(name.endsWith(".gif")) return true;
			if(name.endsWith(".GIF")) return true;
			if(name.endsWith(".jpg")) return true;
			if(name.endsWith(".JPG")) return true;
			if(name.endsWith(".jpeg")) return true;
			if(name.endsWith(".png")) return true;
			if(name.endsWith(".PNG")) return true;
			return false;
		}
    	
    };
    boolean getImage(String filename) {
    	if(filename==null) return false;
    	File f = new File(filename);
    	if(!f.exists() || !f.canRead()) return false;
    	if(f.isDirectory()) {
    		isDirectory=true;
    		String[] elements = f.list(ff);
    		boolean flag=false;
    		int rand = (int) ( Math.random() * elements.length);
    		for(int i=0;i<elements.length;++i) {
   				File f2 = new File(f,elements[rand]);
   				String path = f2.toString();
				flag = getImage(path);
				if(flag) return true;
    			++rand; 
    			rand %= elements.length;
    		}
    		return flag;
    	}
    	else {
            srcImg = Toolkit.getDefaultToolkit().getImage(filename);
            boolean flag = dr.loadImage(srcImg);
    		return flag;
    	}
    }

    void loadImagesFromResource() {
	stdImages = new Image[stdImageNames.length];
	for(int i=0;i<stdImageNames.length;++i)
	{
	    URL url = this.getClass().getResource("/"+stdImageNames[i]);
	    stdImages[i] = Toolkit.getDefaultToolkit().getImage(url);
	}
    }

    void loadStandardImage() {
	int rand = (int) (Math.random() * stdImages.length);
	dr.loadImage(stdImages[rand]);
    }

    void loadImage(String filename) {
    	if(!getImage(filename)) {
    		Image img = DefaultImage.createDefaultImage();
    		dr.loadImage(img);
    	}
    }
    
    abstract class Transition {
    	abstract Rectangle getRect(Rectangle rect,int trans);
    }
    Transition leftTrans = new Transition() {
		//@Override
		Rectangle getRect(Rectangle rect, int t) {
			return new Rectangle(rect.x,rect.y,(rect.width*t)/MAX_TRANS,rect.height);
		}
    };
    Transition rightTrans = new Transition() {
		//@Override
		Rectangle getRect(Rectangle rect, int t) {
			int w = (rect.width*t)/MAX_TRANS;
			return new Rectangle(rect.width-w,rect.y,w,rect.height);
		}
    };
    Transition downTrans = new Transition() {
		//@Override
		Rectangle getRect(Rectangle rect, int t) {
			return new Rectangle(rect.x,rect.y,rect.width,(rect.height*t)/MAX_TRANS);
		}
    };
    Transition upTrans = new Transition() {
		//@Override
		Rectangle getRect(Rectangle rect, int t) {
			int h = (rect.height*t)/MAX_TRANS;
			return new Rectangle(rect.x,rect.height-h,rect.width,h);
		}
    };
    Transition centerTrans = new Transition() {
		//@Override
		Rectangle getRect(Rectangle rect, int t) {
	    	int cenX = rect.x + rect.width/2;
	    	int cenY = rect.y + rect.height/2;
	    	int w = (rect.width * t )/ (2*MAX_TRANS );
	    	int h = (rect.height* t )/ (2*MAX_TRANS );
	    	return new Rectangle(cenX-w,cenY-h,w*2,h*2);
		}
    };

    int trans=MAX_TRANS;
    Transition transition=upTrans;
    //@Override
    public void paint(Graphics g) {
	Component c = getContext().getComponent();
	//c.setVisible(true);
	if(path==null || fd ==null || g == null) return;

	if(trans<MAX_TRANS) {
	    Rectangle rect = transition.getRect(c.getBounds(),trans);
	    g.clipRect(rect.x,rect.y,rect.width,rect.height);
	    ++trans;
	}
	//g.setColor(Color.orange);
	//g.fillRect(width/3, height/3, width/3, height/3);
	//g.setColor(Color.green);
	//Font font = new Font("SansSerif",Font.PLAIN,50);
	//g.setFont(font);
	//g.drawString(imageFileName+""+width + " "+ height, width/3, height/3);

	path.nextItteration(fd);
	if(tr==null) return;
	tr.calcFrame(fd,1, false);    
	tr.fixVerticies(fd);
	tr.calcFund(fd);

	tr.replicate(dr, fd);
	//System.out.println("tess HW "+tessImg.getWidth(null)+" "+tessImg.getWidth(null));
	g.drawImage(tessImg,0,0,null);

	long curTime = System.currentTimeMillis();
	if(change!=0) {
	    if((curTime - time) > change * 1000) {
		time = curTime;
		    tr = TessRule.getTessRuleByName(group);
		if(this.isDirectory) {
		    loadImage(this.imageFileName);
		    dr.setViewport(c.getBounds());
		    tessImg = dr.getActiveImage();
		}
		else if(imageFileName==null) {
		    this.loadStandardImage();
		    dr.setViewport(c.getBounds());
		    tessImg = dr.getActiveImage();
		}
		int size = this.getIntProp("size",1)*sizeMul;
		double area = tr.approxArea();
		double xmul = Math.sqrt(1/(area * tr.approxAspect()));
		double ymul = Math.sqrt(tr.approxAspect() / area);
		fd.resizeDomain((int)(size*xmul),(int)(size*ymul));
		if(randomMotion) {
		    switch((int)(Math.random()*8)) {
		    case 0:	path = new ShiftAnimation(-speed,-speed); break;
		    case 1:	path = new ShiftAnimation(-speed,speed); break;
		    case 2:	path = new ShiftAnimation(speed,-speed); break;
		    case 3:	path = new ShiftAnimation(speed,speed); break;
		    case 4:	path = new ShiftAnimation(-speed,0); break;
		    case 5:	path = new ShiftAnimation(speed,0); break;
		    case 6:	path = new ShiftAnimation(0,-speed); break;
		    case 7:	path = new ShiftAnimation(0,speed); break;

		    }
		}
		path.firstItteration(fd);
		trans=0;
		switch(((int) (Math.random()*5))) {
		case 0: transition = leftTrans; break;
		case 1: transition = rightTrans; break;
		case 2: transition = downTrans; break;
		case 3: transition = upTrans; break;
		case 4: transition = centerTrans; break;
		}
	    }
	}
    }

}

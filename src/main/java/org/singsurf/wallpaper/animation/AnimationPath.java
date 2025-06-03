/*
Created 9 Sep 2006 - Richard Morris
*/
package org.singsurf.wallpaper.animation;

import java.awt.Rectangle;
import java.text.MessageFormat;

import org.singsurf.wallpaper.FundamentalDomain;
import org.singsurf.wallpaper.Messages;
import org.singsurf.wallpaper.Wallpaper;

public abstract class AnimationPath {

	String label;

	public void firstItteration(FundamentalDomain fd) { /* do nothing by default */ }

	abstract public void nextItteration(FundamentalDomain fd);
	
	public static AnimationPath getDefaultPath(Wallpaper wall) {
		var path = new BounceAnimation(wall.dr.dispRect,1,1);
		path.label = Messages.getString("Anim.bounce"); //$NON-NLS-1$
		return path;
	}
	
	public static String[] getPathNames() {
		var str =Messages.getString("Anim.list"); //$NON-NLS-1$
		return str.split(","); //$NON-NLS-1$
	}
	public static AnimationPath getPathByName(String label,int speed,Rectangle rect) {
		AnimationPath path=null;
		if(label==null) return null;
		if(label.equals(Messages.getString("Anim.up"))) { //$NON-NLS-1$
			path = new ShiftAnimation(0,-speed);
		}
		else if(label.equals(Messages.getString("Anim.down"))) { //$NON-NLS-1$
			path = new ShiftAnimation(0,speed);
		}
		else if(label.equals(Messages.getString("Anim.left"))) { //$NON-NLS-1$
			path = new ShiftAnimation(-speed,0);
		}
		else if(label.equals(Messages.getString("Anim.right"))) { //$NON-NLS-1$
			path = new ShiftAnimation(speed,0);
		}
		else if(label.equals(Messages.getString("Anim.ne"))) { //$NON-NLS-1$
			path = new ShiftAnimation(-speed,-speed);
		}
		else if(label.equals(Messages.getString("Anim.nw"))) { //$NON-NLS-1$
			path = new ShiftAnimation(speed,-speed);
		}
		else if(label.equals(Messages.getString("Anim.se"))) { //$NON-NLS-1$
			path = new ShiftAnimation(-speed,speed);
		}
		else if(label.equals(Messages.getString("Anim.sw"))) { //$NON-NLS-1$
			path = new ShiftAnimation(speed,speed);
		}
		else if(label.equals(Messages.getString("Anim.rotate"))) { //$NON-NLS-1$
			path = new RotateAnimation(rect,false);
		}
		else if(label.equals(Messages.getString("Anim.rotate_centre"))) { //$NON-NLS-1$
			path = new RotateAnimation(rect,true);
		}
		else if(label.equals(Messages.getString("Anim.bounce"))) { //$NON-NLS-1$
			path = new BounceAnimation(rect,speed,speed);
		}
		else if(label.equals(Messages.getString("Anim.left_right"))) { //$NON-NLS-1$
			path = new BounceAnimation(rect,speed,0);
		}
		else if(label.equals(Messages.getString("Anim.up_down"))) { //$NON-NLS-1$
			path = new BounceAnimation(rect,0,speed);
		}
		else if(label.equals(Messages.getString("Anim.none"))) { //$NON-NLS-1$
			path = new BounceAnimation(rect,0,0);
		}
		else if(label.equals(Messages.getString("Anim.smooth"))) { //$NON-NLS-1$
			path = new LissajousAnimation(rect,speed);
		}
		else if(label.equals(Messages.getString("Anim.grow"))) { //$NON-NLS-1$
			path = new GrowAnimation(rect,false);
		}
		else if(label.equals(Messages.getString("Anim.grow_centre"))) { //$NON-NLS-1$
			path = new GrowAnimation(rect,true);
		}
		else {
	        String[] collections = Messages.getString("Anim.collections").split(",");
			for(var key : collections) {
	    		if(label.equalsIgnoreCase(key)) { //$NON-NLS-1$
	    			String[] names = Messages.getString("Anim."+key+".list").split(","); //$NON-NLS-1$ //$NON-NLS-2$
	    			int rnd = (int) (Math.random() * names.length);
	    			var path2 = getPathByName(names[rnd],speed,rect);
	    			if(path2==null) {
						System.out.println(MessageFormat.format(Messages.getString("Rule.error_tess_rule"),names[rnd])); //$NON-NLS-1$
					}
					else {
						return path2;
					}
	    		}
			}
//			if(label.equals(Messages.getString("Anim.nice"))) { //$NON-NLS-1$
//			String[] name = Messages.getString("Anim.nice.list").split(","); //$NON-NLS-1$ //$NON-NLS-2$
//			int rnd = (int) (Math.random() * name.length);
//			return getPathByName(name[rnd],speed,rect);

		}
			
		path.label = label;
		return path;
	}

	public String getLabel() {
		return label;
	}
}

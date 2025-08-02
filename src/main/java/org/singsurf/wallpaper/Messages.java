package org.singsurf.wallpaper;

import java.awt.Color;
import java.lang.reflect.Field;
import java.util.MissingResourceException;
import java.util.ResourceBundle;

public class Messages {
	private static final String BUNDLE_NAME = Messages.class.getPackageName() + ".messages"; //$NON-NLS-1$

	private static final ResourceBundle RESOURCE_BUNDLE = ResourceBundle.getBundle(BUNDLE_NAME);

	private Messages() {
	}

	public static String getString(String key) {
		try {
			return RESOURCE_BUNDLE.getString(key);
		} catch (MissingResourceException e) {
			return '!' + key + '!';
		}
	}

	public static int getInt(String key) {
		try {
			var str =  RESOURCE_BUNDLE.getString(key);
			return Integer.parseInt(str);
		} catch (MissingResourceException e) {
			return -1;
		}
	}

	public static Color getColor(String key) {
		var cname = getString(key);
		Color color;
		try {
		    Field field = Class.forName("java.awt.Color").getField(cname.toUpperCase());
		    color = (Color)field.get(null);
		} catch (Exception e) {
		    color = Color.decode(cname); 
		}
		return color;
	}
}

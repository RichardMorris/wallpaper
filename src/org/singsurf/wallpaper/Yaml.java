/*
Created 11 Apr 2007 - Richard Morris
*/
package org.singsurf.wallpaper;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Yaml {
	private final Wallpaper wallpaper;

	String group;
	int vertX[]=new int[3],vertY[] = new int[3];

	public int zNumer=1;
	public int zDenom=1;

	public Yaml(Wallpaper w)
	{
		this.wallpaper = w;
	}
	
	public void write(PrintWriter pr) {
		FundamentalDomain fd = wallpaper.controller.fd;
		pr.println("group: "+wallpaper.controller.tr.name);
		pr.println("vertices:");
		pr.println("  - [" + fd.cellVerts[0].x+","+fd.cellVerts[0].y+"]");
		pr.println("  - [" + fd.cellVerts[1].x+","+fd.cellVerts[1].y+"]");
		pr.println("  - [" + fd.cellVerts[2].x+","+fd.cellVerts[2].y+"]");
/*		if(wallpaper.imageFilename!=null) {
			pr.println("filename: "+wallpaper.imageFilename);
		}
		else if(wallpaper.imageURL!=null)
			pr.println("url: "+wallpaper.imageURL.toString());
*/			
		pr.println("zoom: ["+((ZoomedDrawableRegion) wallpaper.dr).zoomNumer + "," +((ZoomedDrawableRegion) wallpaper.dr).zoomDenom+"]");
	}

	public void read(BufferedReader br) throws IOException {
		Pattern pat = Pattern.compile(".*\\[\\s*(\\d+)\\s*,\\s*(\\d+)\\s*\\]");
		String line=null;
		while((line = br.readLine())!=null) {
			line = line.trim();
			if(line.startsWith("group:")) {
				group = line.substring(7).trim();
			}
			else if(line.startsWith("vertices:")) {
				for(int i=0;i<3;++i) {
					String line2 = br.readLine();
					Matcher m = pat.matcher(line2);
					if(m.matches()) {
						vertX[i] = Integer.parseInt(m.group(1));
						vertY[i] = Integer.parseInt(m.group(2));
					}
				}
					
			}
			if(line.startsWith("zoom:")) {
				Matcher m = pat.matcher(line);
				if(m.matches()) {
					zNumer = Integer.parseInt(m.group(1));
					zDenom = Integer.parseInt(m.group(2));
				}
			}
		}
	}
}

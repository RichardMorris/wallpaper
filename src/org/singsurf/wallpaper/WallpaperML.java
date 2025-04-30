/*
Created 11 Apr 2007 - Richard Morris
*/
package org.singsurf.wallpaper;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class WallpaperML {
	private final Wallpaper wallpaper;

	public String group;
	public int vertX[]=new int[3],vertY[] = new int[3];

	public int zNumer=1;
	public int zDenom=1;

	public String filename;

	public String anim;
	public int animSpeed=1;

	public int repeat=-1;

//	public boolean restart=false;

	static final Pattern vertexRE = Pattern.compile(".*\\[\\s*-?(\\d+)\\s*,\\s*-?(\\d+)\\s*\\]");
;

	public WallpaperML()
	{
		this.wallpaper = null;
	}

	public WallpaperML(Wallpaper w)
	{
		this.wallpaper = w;
	}
	
	public WallpaperML(Wallpaper w, String path, int time) {
		this.wallpaper = w;
		this.anim = path;
		this.repeat = time;

	}

	public void write(PrintWriter pr) {
		if(anim!= null) {
			pr.println("frame:");
		}
		
		FundamentalDomain fd = wallpaper.controller.fd;
		pr.println("group: "+wallpaper.controller.tr.name);
		pr.println("vertices:");
		pr.println("  - [" + fd.cellVerts[0].x+","+fd.cellVerts[0].y+"]");
		pr.println("  - [" + fd.cellVerts[1].x+","+fd.cellVerts[1].y+"]");
		pr.println("  - [" + fd.cellVerts[2].x+","+fd.cellVerts[2].y+"]");
		if(wallpaper.imageFilename!=null) {
			Path cdw = Path.of(System.getProperty("user.dir"));
			Path absPath = Path.of(wallpaper.imageFilename).toAbsolutePath();
			Path relativePath =
				absPath.startsWith(cdw) 
				? cdw.relativize(absPath)
				: absPath;
			
			String str = relativePath.toString();
			String converted = str.replaceAll("\\\\","/");
			//System.out.println("filename: "+str + " converted "+converted);
			pr.println("filename: "+converted);
			pr.println("zoom: ["+((ZoomedDrawableRegion) wallpaper.dr).zoomNumer + "," +((ZoomedDrawableRegion) wallpaper.dr).zoomDenom+"]");
		}
		if(anim!= null) {
			pr.println("anim: "+anim);
			pr.println("repeat: "+repeat);
		}
	}

	public static List<WallpaperML> read(BufferedReader br) throws IOException {
		String line=null;
		List<WallpaperML> list = new ArrayList<>();
		WallpaperML obj = new WallpaperML();
		list.add(obj);
		
		while((line = br.readLine())!=null) {
			line = line.trim();
			if(line.startsWith("group:")) {
				obj.group = line.substring(7).trim();
			}
			else if(line.startsWith("vertices:")) {
				for(int i=0;i<3;++i) {
					String line2 = br.readLine();
					Matcher m = vertexRE.matcher(line2);
					if(m.matches()) {
						obj.vertX[i] = Integer.parseInt(m.group(1));
						obj.vertY[i] = Integer.parseInt(m.group(2));
					}
				}
					
			}
			else if(line.startsWith("zoom:")) {
				Matcher m = vertexRE.matcher(line);
				if(m.matches()) {
					obj.zNumer = Integer.parseInt(m.group(1));
					obj.zDenom = Integer.parseInt(m.group(2));
				}
			}
			else if(line.startsWith("filename:")) {
				String fn = line.substring(9).trim();
				obj.filename = fn;
			}
			else if(line.startsWith("anim:")) {
				var parts = line.split(" ");
				obj.anim = parts[1].trim();
				obj.animSpeed = parts.length > 2 ? Integer.parseInt(parts[2].trim()) : 1;
			}
			else if(line.startsWith("repeat:")) {
				var parts = line.split(" ");
				obj.repeat = Integer.parseInt(parts[1].trim());
			}
//			else if(line.startsWith("restart:")) {
//				obj.restart = true;
//			}
			else if(line.startsWith("frame:")) {
				if(obj.group != null) {
					obj = new WallpaperML();
					list.add(obj);
				}
			}
			else if(line.length() > 0) {
				System.out.println("Unknown line in wallpaper file: "+line);
			}
		}
		return list;
	}

	@Override
	public String toString() {
		return "WallpaperML [group=" + group + ", vertX=" + Arrays.toString(vertX) + ", vertY=" + Arrays.toString(vertY)
				+ ", zNumer=" + zNumer + ", zDenom=" + zDenom + ", filename=" + filename + ", anim=" + anim
				+ ", animSpeed=" + animSpeed + ", repeat=" + repeat +"]"; //"+ ", restart=" + restart + "]";
	}
}

package org.singsurf.wallpaper.tessrules;

import java.awt.Color;
import java.awt.Component;
import java.awt.Rectangle;

import org.singsurf.wallpaper.DrawableRegion;
import org.singsurf.wallpaper.FundamentalDomain;
import org.singsurf.wallpaper.Vec;

/**
 * A class to perform a given tessellation.
 */
public abstract class TessRule
{
    static final boolean TIME=false;
    static final boolean DEBUG=false;
    /** Whether advanced tile copying is being used */
	private static final boolean COPY_TILES = true;
    /** The Checkbox for this rule. */
    public Component comp;
    /** Name of the rule */
    public String name;
    /** Message describing the rule.*/
    public String message;
    /** true the first time this rule used, false otherwise. */
    public boolean firstCall=true;
    /** coordinate frame for rule. */
    public //public int frame[] = new int[6];

    /**
     * The origin
     */
    Vec frameO = new Vec(0,0);
    /**
     * First basis vector
     */
    public Vec frameU = new Vec(0,0);
    /**
     * Second basis vector
     */
    public Vec frameV = new Vec(0,0);

    public static int backgroundRGB = Color.black.getRGB();
    public static boolean tileBackground = true;
    public TessRule(String name,String message)
    {
        this.name = name;
        this.message = message; 
    }

    /** calculates the frame. 
     * @param constrained TODO*/
    public abstract void calcFrame(FundamentalDomain fd,int selectedVertex, boolean constrained);


    /** adjust coordinates to give correct shape. 
     * Also works out outer translation region.
     **/
    public abstract void fixVerticies(FundamentalDomain fd);

    /** calculate the fundamental domain */
    public abstract void calcFund(FundamentalDomain fd);

    /**
     * Constrain the vertices so that it could be used as a desktop tile.
     * @param verts array of three selection points modified on return
     * @param selectedVertex this vertex has been changed by the user
     */
    public void constrainVertices(Vec[] verts,int selectedVertex) {
        // do nothing by default
    }
    
    /** callback function to find the source pixel. Works in u-v coordinate space.
     * @param in - in coordinates
     * @param out - coord of point in fundamental domain.
     * @param det - determinant of coord system u^v.
     */
    public abstract void fun(int[] in,int[] out,int det);

    /** 
     * Replicate the fundamental domain for a given tessellation rule.
     * For each pixel (x,y) in the image, find the 
     * source pixel in the fundamental domain from the callback method 
     * and copy the pixel values.
     * First transform
     * to u-v coordinate system specified by parameters.
     * Then use callback to find the u-v coordinates of the
     * source pixel in the fundamental domain.
     * Translate these coordinates back to x-y coordinates and copy the
     * pixel value from the source to (x,y).
     * 
     * <p>
     * The parameters specify origin and axis of new coordinate system.
     * We wish to solve <pre>x = a u + b v</pre>
     * after translating to origin. Now
     * <pre>x^u = b v^u, x^v = a u^v</pre>
     * so we pass the values (x^v,x^u, and u^v) to callback method.
     * This function will return the u-v coordinates of the point in the
     * fundamental domain which yields this pixel.
     * If u and v are negatively oriented then swap them round.
     * To allow integer arithmetic we do not divide through by the determinant u^v.
     */


    public void replicate(DrawableRegion dr,FundamentalDomain fd)
    {
        //			System.out.println("replicate");
        if(!dr.img_ok) return;
        int x0=frameO.x;
        int y0=frameO.y;
        int u1=frameU.x;
        int u2=frameU.y;
        int v1=frameV.x;
        int v2=frameV.y;
        int i,j,srcX,srcY,x,y;
        int[] in = new int[2];
        int[] res = new int[2];

        boolean error_flag = false;

        int det = u1 * v2 - v1 * u2;
        if( det == 0 ) return;
        if(det < 0 )
        {
            //				System.out.println("Negative det");
            det = - det;
            int w1 = v1; v1 = u1; u1 = w1; 
            int w2 = v2; v2 = u2; u2 = w2;
        }
        long t1=0;
        if(TIME)  t1=System.currentTimeMillis();
        
        //long n1 = System.nanoTime();
        //Arrays.fill(dr.pixels, Color.BLACK.getRGB());

//        int latticeWidth = fd.getLatticeWidth();
//        int latticeHeight = fd.getLatticeHeight();
        final int startX = dr.dispRect.x;
        final int startY = dr.dispRect.y;
        Vec[] points;
//        if(latticeWidth < 5 || latticeHeight < 5 
//                || latticeWidth > dr.destRect.width
//                || latticeHeight > dr.destRect.height ) {
//            latticeWidth = dr.dispRect.width;
//            latticeHeight = dr.dispRect.height;
//            points = new Vec[0];
//        }
//        else
            points = fd.getLatticePoints(new Rectangle(0,0,dr.dispRect.width,dr.dispRect.height));

//        if(DEBUG) System.out.println("lattice "+latticeWidth+" "+latticeHeight);
        Rectangle baseRect = fd.getMinimalRectangle(points);
        if(!COPY_TILES || baseRect==null)
            baseRect = new Rectangle(0,0,dr.dispRect.width,dr.dispRect.height);
        
        for(i=startX;i<startX+baseRect.width;++i)
            for(j=startY;j<startY+baseRect.height;++j)
            {
                x = i+dr.offset.x - x0;
                y = j+dr.offset.y - y0; // offset of figure
                in[0] = v2 * x - v1 * y;
                in[1] = -u2 * x + u1 * y;
                fun(in,res,det);

                srcX = x0 + (res[0] * u1 + res[1] * v1 ) / det;	
                srcY = y0 + (res[0] * u2 + res[1] * v2 ) / det;	

                try
                {
                    int px;
                    if(TessRule.tileBackground) {
                        srcX %= dr.srcRect.width; if(srcX <0) srcX += dr.srcRect.width;
                        srcY %= dr.srcRect.height; if(srcY <0) srcY += dr.srcRect.height;
                        int inInd = srcX+srcY*dr.srcRect.width;
                        px = dr.inpixels[inInd];
                    }
                    else {
                        if(srcX<0 || srcX>=dr.srcRect.width || srcY<0 || srcY>=dr.srcRect.height) {
                            px = backgroundRGB;
                        }
                        else 
                            px = dr.inpixels[srcX+srcY*dr.srcRect.width];
                    }
                    //if(i==0&&j==0) px = backgroundRGB;
                    int outX = i;
                    int outY = j;
                    int outInd = outX+outY*dr.destRect.width;
                    dr.pixels[outInd] = px;
                    //						pixels[i+j*width] = ((res[0]*256)/det)+((res[1]*256)/det)*256;
                }
                catch(Exception e)
                {
                    if(!error_flag)
                        System.out.println("Error ("+i+","+j+") det "+det
                                + " x "+x
                                + " y "+y
                                + " in ("+ in[0] + ","+in[1]+")"
                                + " res ("+ res[0] + ","+res[1]+")"
                                + " sX "+srcX
                                + " sY "+srcY
                        );
                    error_flag = true;
                    dr.pixels[i+j*dr.destRect.width] = backgroundRGB;
                }
            }
        if(TIME) System.out.println("Micro "+((System.currentTimeMillis()-t1)/1000));

        if(COPY_TILES)
        	      copyTiles(dr, fd, baseRect);


        /*		else {
			for(i=dr.minX;i<dr.maxX;++i)
				for(j=dr.minY;j<dr.maxY;++j)
				{
					x = i+dr.offsetX - x0;
					y = j+dr.offsetY - y0; // offset of figure
					in[0] = v2 * x - v1 * y;
					in[1] = -u2 * x + u1 * y;
					fun(in,res,det);

					srcX = x0 + (res[0] * u1 + res[1] * v1 ) / det;	
					srcY = y0 + (res[0] * u2 + res[1] * v2 ) / det;	

					try
					{
						if(srcX<0 || srcX>=dr.width || srcY<0 || srcY>=dr.height) {
							dr.pixels[i+j*dr.getOutWidth()] = backgroundRGB;
						}
						else {
							int outInd = i+j*dr.getOutWidth();
							int inInd = srcX+srcY*dr.width;
							int px = dr.inpixels[inInd];
							dr.pixels[outInd] = px;
						}
//						pixels[i+j*width] = ((res[0]*256)/det)+((res[1]*256)/det)*256;
					}
					catch(Exception e)
					{
						if(!error_flag)
							System.out.println("Error ("+i+","+j+") det "+det
									+ " x "+x
									+ " y "+y
									+ " in ("+ in[0] + ","+in[1]+")"
									+ " res ("+ res[0] + ","+res[1]+")"
									+ " sX "+srcX
									+ " sY "+srcY
							);
						error_flag = true;
						dr.pixels[i+j*dr.getOutWidth()] = 0;
					}
				}
		}
         */
       // if(TIME) System.out.println("Micro "+((System.nanoTime()-n1)/1000));
        /*
		System.out.println(
			 "vX0 " + verticies[0].x
			+" vY0 " + verticies[0].y
			+" u1 " + u1
			+" u2 " + u2
			+" v1 " + v1
			+" v2 " + v2
			);
         */
//        long t2 = System.currentTimeMillis();
//        System.out.println(t2-t1);

        dr.fillSource();
    }

    private void copyTiles(DrawableRegion dr, FundamentalDomain fd, Rectangle baseRect) {
        Vec[] points = fd.getLatticePoints(new Rectangle(
                -baseRect.width,
                -baseRect.height,
                dr.dispRect.width+2*baseRect.width,
                dr.dispRect.height+2*baseRect.height
                ));
        Rectangle clipRect = new Rectangle(0,0,dr.dispRect.width,dr.dispRect.height);
        for(int i=0;i<points.length;++i) { Vec p = points[i];
            int outX = p.x;
            int outY = p.y;
            if(outX==0 && outY==0) continue;
            Rectangle latticeRect = new Rectangle(outX,outY,baseRect.width,baseRect.height);
            Rectangle resRect = clipRect.intersection(latticeRect);
            if(resRect.width<=0 || resRect.height <= 0) continue;

            int sx = dr.dispRect.x + resRect.x - outX;
            int sy = dr.dispRect.y + resRect.y - outY;
            int dx = dr.dispRect.x + resRect.x; 
            int dy = dr.dispRect.y + resRect.y;
            for(int k=0;k<resRect.height;++k) {
//                System.out.println("src "+(sx + (sy+k) * dr.destRect.width) + 
//                        " dest "+(dx+(dy+k) * dr.destRect.width) + 
//                        " len "+resRect.width);   
                System.arraycopy(
                        dr.pixels, sx + (sy+k) * dr.destRect.width, 
                        dr.pixels, dx+(dy+k) * dr.destRect.width, 
                        resRect.width);
            }
        }
    }

    public double approxArea() { return 1; }
    public double approxAspect() { return 1;	}

    public static String[] WallpaperNames = {
        "P1","P2","PM","PG","CM","CMM","PMG","PGG","PMM",
        "P4","P4M","P4G","P3M1","P31M","P6","P6M"
    };
    
    public static final String[] basicNames = new String[]{
        "Translation","Rotation","Reflection","Glide-Reflection","Scale","Linear"};

    public static TessRule getTessRuleByName(String name) {
        if(name==null) name="";
        if(name.equalsIgnoreCase("P1")) return PgramRule.rhombusTT;
        //if(name.equalsIgnoreCase("P2")) return PgramRule.rhombusR1;
        if(name.equalsIgnoreCase("P2")) return IrregularHexRule.p2hex;
        if(name.equalsIgnoreCase("CM")) return DiamondRule.rhombCM;
        if(name.equalsIgnoreCase("CMM")) return DiamondRule.rhombCMM;
        if(name.equalsIgnoreCase("PM")) return RectRule.rectPM;
        if(name.equalsIgnoreCase("PG")) return RectRule.rectPG;
        if(name.equalsIgnoreCase("PMG")) return RectRule.rectPMG;
        if(name.equalsIgnoreCase("PMM")) return RectRule.rectPMM;
        if(name.equalsIgnoreCase("PGG")) return RectRule.rectPGG;
        if(name.equalsIgnoreCase("P4")) return SquRule.squP4;
        if(name.equalsIgnoreCase("P4M")) return SquRule.squP4m;
        if(name.equalsIgnoreCase("P4G")) return SquRule.squP4g;
        if(name.equalsIgnoreCase("P3")) return HexiRule.triP3;
        if(name.equalsIgnoreCase("P3M1")) return HexiRule.triP3m1;
        if(name.equalsIgnoreCase("P31M")) return HexiRule.triP31m;
        if(name.equalsIgnoreCase("P6")) return HexiRule.triP6;
        if(name.equalsIgnoreCase("P6M")) return HexiRule.triP6m;
        if(name.equalsIgnoreCase("F1")) return FrezeRule.F1;
        if(name.equalsIgnoreCase("F2")) return FrezeRule.F2;
        if(name.equalsIgnoreCase("F3")) return FrezeRule.F3;
        if(name.equalsIgnoreCase("F4")) return FrezeRule.F4;
        if(name.equalsIgnoreCase("F5")) return FrezeRule.F5;
        if(name.equalsIgnoreCase("F6")) return FrezeRule.F6;
        if(name.equalsIgnoreCase("F7")) return FrezeRule.F7;
        if(name.equalsIgnoreCase("Translation")) return BasicRule.trans;
        if(name.equalsIgnoreCase("Rotation")) return BasicRule.rot;
        if(name.equalsIgnoreCase("Reflection")) return BasicRule.reflect;
        if(name.equalsIgnoreCase("Glide-Reflection")) return BasicRule.glide;
        if(name.equalsIgnoreCase("Scale")) return BasicRule.scale;
        if(name.equalsIgnoreCase("Scale XY")) return BasicRule.scaleXY;
        if(name.equalsIgnoreCase("Linear")) return BasicRule.shear;
        try {

            if(name.startsWith("C") || name.startsWith("c")) {
                int num = Integer.parseInt(name.substring(1));
                return PointRule.cycleRules[num];
            }
            else if(name.startsWith("D") || name.startsWith("d")) {
                int num = Integer.parseInt(name.substring(1));
                return PointRule.dyhRules[num];
            }
        } catch(Exception e) {System.out.println(e.getMessage());}
        int rand;
        if(name.equalsIgnoreCase("attractive")) {
            rand = (int) (Math.random() * 13 + 4);
        }
        else if(name.equalsIgnoreCase("symmetrical")) {
            rand = (int) (Math.random() * 6 + 9);
        }
        else
            rand = (int) (Math.random() * WallpaperNames.length);
        return(getTessRuleByName(WallpaperNames[rand]));


    }

    FundamentalDomain paintFd=null;
    public void paintSymetries(Vec U, Vec V, Vec O,FundamentalDomain fd) {
        this.paintFd = fd;
        paintSymetries(U,V,O);
    }

    public void paintDomainEdges(Vec U, Vec V, Vec O, FundamentalDomain fd) {
        this.paintFd = fd;
        paintDomainEdges(U,V,O,fd.det);
    }

    protected void paintSymetries(Vec U, Vec V, Vec O) { /* null default sub classes over ride */ }

    public void paintDomainEdges(Vec U, Vec V, Vec O, int det) { /* null default sub classes over ride */ }

    final protected void drawReflectionLine(Vec P1,Vec P2) {
        paintFd.drawReflectionLine(P1, P2);
    }
    final protected void drawGlideLine(Vec P1,Vec P2) {
        paintFd.drawGlideLine(P1, P2);
    }

    final protected void drawRotationPoint(Vec P,int angle) {
        paintFd.drawRotationPoint(P, angle);
    }

    final protected void drawSimpleEdge(Vec P,Vec Q) {
        paintFd.drawSimpleEdge(P, Q);
    }

    public void fixFlip(String code,FundamentalDomain fd) {
        return;
    }

}

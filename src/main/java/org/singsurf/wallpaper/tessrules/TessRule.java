package org.singsurf.wallpaper.tessrules;

import java.awt.Color;
import java.awt.Component;
import java.awt.Rectangle;

import org.singsurf.wallpaper.DrawableRegion;
import org.singsurf.wallpaper.FundamentalDomain;
import org.singsurf.wallpaper.Messages;
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
                        srcX %= dr.srcRect.width; 
                        if(srcX <0) srcX += dr.srcRect.width;
                        srcY %= dr.srcRect.height; 
                        if(srcY <0) srcY += dr.srcRect.height;
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
                        System.out.println("Error ("+i+","+j+") det "+det //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
                                + " x "+x //$NON-NLS-1$
                                + " y "+y //$NON-NLS-1$
                                + " in ("+ in[0] + ","+in[1]+")" //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
                                + " res ("+ res[0] + ","+res[1]+")" //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
                                + " sX "+srcX //$NON-NLS-1$
                                + " sY "+srcY //$NON-NLS-1$
                        );
                    error_flag = true;
                    dr.pixels[i+j*dr.destRect.width] = backgroundRGB;
                }
            }
        if(TIME) System.out.println("Micro "+((System.currentTimeMillis()-t1)/1000)); //$NON-NLS-1$

        if(COPY_TILES)
        	      copyTiles(dr, fd, baseRect);

        if(TIME) System.out.println("Micro "+((System.nanoTime()-t1)/1000)); //$NON-NLS-1$

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
        Messages.getString("Rule.P1"),Messages.getString("Rule.P2"),Messages.getString("Rule.PM"),Messages.getString("Rule.PG"),Messages.getString("Rule.CM"),Messages.getString("Rule.CMM"),Messages.getString("Rule.PMG"),Messages.getString("Rule.PGG"),Messages.getString("Rule.PMM"), //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$ //$NON-NLS-6$ //$NON-NLS-7$ //$NON-NLS-8$ //$NON-NLS-9$
        Messages.getString("Rule.P4"),Messages.getString("Rule.P4M"),Messages.getString("Rule.P4G"),Messages.getString("Rule.P3M1"),Messages.getString("Rule.P31M"),Messages.getString("Rule.P6"),Messages.getString("Rule.P6M") //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$ //$NON-NLS-6$ //$NON-NLS-7$
    };
    
    public static final String[] basicNames = new String[]{
        Messages.getString("Rule.trans"),Messages.getString("Rule.rotation"),Messages.getString("Rule.reflection"),Messages.getString("Rule.glide"),Messages.getString("Rule.uniformscale"),Messages.getString("Rule.linear")}; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$ //$NON-NLS-6$

    public static TessRule getTessRuleByName(String name) {
        if(name==null) name=""; //$NON-NLS-1$
        if(name.equalsIgnoreCase(Messages.getString("Rule.P1"))) return PgramRule.rhombusTT; //$NON-NLS-1$
        if(name.equalsIgnoreCase(Messages.getString("Rule.P1a"))) return IrregularHexRule.p1hex; //$NON-NLS-1$
        if(name.equalsIgnoreCase(Messages.getString("Rule.P1h"))) return IrregularHexRule.p1hex; //$NON-NLS-1$
        if(name.equalsIgnoreCase(Messages.getString("Rule.P2"))) return PgramRule.rhombusR1; //$NON-NLS-1$
        if(name.equalsIgnoreCase(Messages.getString("Rule.P2a"))) return IrregularHexRule.p2hex; //$NON-NLS-1$
        if(name.equalsIgnoreCase(Messages.getString("Rule.P2h"))) return IrregularHexRule.p2hex; //$NON-NLS-1$
        if(name.equalsIgnoreCase(Messages.getString("Rule.CM"))) return DiamondRule.rhombCM; //$NON-NLS-1$
        if(name.equalsIgnoreCase(Messages.getString("Rule.CMM"))) return DiamondRule.rhombCMM; //$NON-NLS-1$
        if(name.equalsIgnoreCase(Messages.getString("Rule.PM"))) return RectRule.rectPM; //$NON-NLS-1$
        if(name.equalsIgnoreCase(Messages.getString("Rule.PG"))) return RectRule.rectPG; //$NON-NLS-1$
        if(name.equalsIgnoreCase(Messages.getString("Rule.PMG"))) return RectRule.rectPMG; //$NON-NLS-1$
        if(name.equalsIgnoreCase(Messages.getString("Rule.PMM"))) return RectRule.rectPMM; //$NON-NLS-1$
        if(name.equalsIgnoreCase(Messages.getString("Rule.PGG"))) return RectRule.rectPGG; //$NON-NLS-1$
        if(name.equalsIgnoreCase(Messages.getString("Rule.P4"))) return SquRule.squP4; //$NON-NLS-1$
        if(name.equalsIgnoreCase(Messages.getString("Rule.P4M"))) return SquRule.squP4m; //$NON-NLS-1$
        if(name.equalsIgnoreCase(Messages.getString("Rule.P4G"))) return SquRule.squP4g; //$NON-NLS-1$
        if(name.equalsIgnoreCase(Messages.getString("Rule.P3"))) return HexiRule.triP3; //$NON-NLS-1$
        if(name.equalsIgnoreCase(Messages.getString("Rule.P3M1"))) return HexiRule.triP3m1; //$NON-NLS-1$
        if(name.equalsIgnoreCase(Messages.getString("Rule.P31M"))) return HexiRule.triP31m; //$NON-NLS-1$
        if(name.equalsIgnoreCase(Messages.getString("Rule.P31Mk"))) return HexiRule.triP31mk; //$NON-NLS-1$
        if(name.equalsIgnoreCase(Messages.getString("Rule.P6"))) return HexiRule.triP6; //$NON-NLS-1$
        if(name.equalsIgnoreCase(Messages.getString("Rule.P6M"))) return HexiRule.triP6m; //$NON-NLS-1$
        if(name.equalsIgnoreCase(Messages.getString("Rule.F1"))) return FrezeRule.F1; //$NON-NLS-1$
        if(name.equalsIgnoreCase(Messages.getString("Rule.F2"))) return FrezeRule.F2; //$NON-NLS-1$
        if(name.equalsIgnoreCase(Messages.getString("Rule.F3"))) return FrezeRule.F3; //$NON-NLS-1$
        if(name.equalsIgnoreCase(Messages.getString("Rule.F4"))) return FrezeRule.F4; //$NON-NLS-1$
        if(name.equalsIgnoreCase(Messages.getString("Rule.F5"))) return FrezeRule.F5; //$NON-NLS-1$
        if(name.equalsIgnoreCase(Messages.getString("Rule.F6"))) return FrezeRule.F6; //$NON-NLS-1$
        if(name.equalsIgnoreCase(Messages.getString("Rule.F7"))) return FrezeRule.F7; //$NON-NLS-1$
        if(name.equalsIgnoreCase(Messages.getString("Rule.trans"))) return BasicRule.trans; //$NON-NLS-1$
        if(name.equalsIgnoreCase(Messages.getString("Rule.rotation"))) return BasicRule.rot; //$NON-NLS-1$
        if(name.equalsIgnoreCase(Messages.getString("Rule.reflection"))) return BasicRule.reflect; //$NON-NLS-1$
        if(name.equalsIgnoreCase(Messages.getString("Rule.glide"))) return BasicRule.glide; //$NON-NLS-1$
        if(name.equalsIgnoreCase(Messages.getString("Rule.uniformscale"))) return BasicRule.scale; //$NON-NLS-1$
        if(name.equalsIgnoreCase(Messages.getString("Rule.scalexy"))) return BasicRule.scaleXY; //$NON-NLS-1$
        if(name.equalsIgnoreCase(Messages.getString("Rule.linear"))) return BasicRule.shear; //$NON-NLS-1$
        try {

            if(name.startsWith(Messages.getString("Rule.C.prefix")) || name.startsWith(Messages.getString("Rule.c.prefix"))) { //$NON-NLS-1$ //$NON-NLS-2$
                int num = Integer.parseInt(name.substring(1));
                return PointRule.cycleRules[num];
            }
            else if(name.startsWith(Messages.getString("Rule.D.prefix")) || name.startsWith(Messages.getString("Rule.d.prefix"))) { //$NON-NLS-1$ //$NON-NLS-2$
                int num = Integer.parseInt(name.substring(1));
                return PointRule.dyhRules[num];
            }
        } catch(Exception e) {System.out.println(e.getMessage());}
        int rand;
        if(name.equalsIgnoreCase(Messages.getString("Rule.attractive"))) { //$NON-NLS-1$
            rand = (int) (Math.random() * 13 + 4);
        }
        else if(name.equalsIgnoreCase(Messages.getString("Rule.symmetrical"))) { //$NON-NLS-1$
            rand = (int) (Math.random() * 6 + 9);
        }
        else
            rand = (int) (Math.random() * WallpaperNames.length);
        return(getTessRuleByName(WallpaperNames[rand]));


    }

    FundamentalDomain paintFd=null;
    public void paintSymetries(Vec U, Vec V, Vec O,FundamentalDomain fd) {
        paintFd = fd;
        paintSymetries(U,V,O);
    }

    public void paintDomainEdges(Vec U, Vec V, Vec O, FundamentalDomain fd) {
        paintFd = fd;
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

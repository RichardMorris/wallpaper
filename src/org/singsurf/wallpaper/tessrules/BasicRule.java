/**
 * 
 */
package org.singsurf.wallpaper.tessrules;

import java.awt.Rectangle;

import org.singsurf.wallpaper.DrawableRegion;
import org.singsurf.wallpaper.FundamentalDomain;
import org.singsurf.wallpaper.Vec;

public abstract class BasicRule extends TessRule
{
    public void fun(int[] in, int[] out, int det) {
        // TODO Auto-generated method stub

    }

    public BasicRule(String name, String message) {
        super(name, message);
    }

    int det=1;
    double cos;
    double sin;
    int lensq;
    double len;
    int unitLen;

    //@Override
    public void calcFrame(FundamentalDomain fd,int selVert, boolean constrained)
    {
        int u1,u2,v1,v2; //,w1,w2;

        v1 =	fd.cellVerts[0].x - fd.cellVerts[1].x;
        v2 =	fd.cellVerts[0].y - fd.cellVerts[1].y;
        u1 = -v2;
        u2 = v1;
        det = u1 * v2 - v1 * u2;
        frameO.x = fd.cellVerts[1].x;
        frameO.y = fd.cellVerts[1].y;
        frameU.x = v1;
        frameU.y = v2;
        frameV.x = u1;
        frameV.y = u2;
    }

    //@Override
    public void fixVerticies(FundamentalDomain fd)
    {
        fd.cellVerts[0].x = frameO.x+frameU.x;
        fd.cellVerts[0].y = frameO.y+frameU.y;
        fd.cellVerts[1].x = frameO.x;
        fd.cellVerts[1].y = frameO.y;
        fd.numSelPoints = 2;
        fd.numOuterPoints = 2;
        fd.setLatticeType(FundamentalDomain.BASIC);

    }

    /** Calculates the fundamental domain */
    //@Override
    public void calcFund(FundamentalDomain fd) {
        fd.fund[0].x = fd.cellVerts[1].x; 
        fd.fund[0].y = fd.cellVerts[1].y;
        fd.fund[1].x = fd.cellVerts[0].x;
        fd.fund[1].y = fd.cellVerts[0].y;
        fd.numFund = 2;
    }

    //@Override
    public void replicate(DrawableRegion dr,FundamentalDomain fd)
    {
        //                      System.out.println("replicate");
        if(!dr.img_ok) return;
        int x0=frameO.x;
        int y0=frameO.y;
        int i,j,srcX,srcY,x,y;
        int[] in = new int[2];
        int[] res = new int[2];
        lensq = frameU.lenSq();
        len = Math.sqrt(frameU.lenSq());
        unitLen = (dr.dispRect.width>dr.dispRect.height?dr.dispRect.height:dr.dispRect.width)/4;

        if(lensq==0) {
            if(this == BasicRule.glide || this == BasicRule.reflect || this == BasicRule.rot) {
                return;
            }
        }
        if(det==0) {
            if(this == BasicRule.shear)
                return;
        }
        cos = frameU.x / len;
        sin = frameU.y / len;
        boolean error_flag = false;

        //long t1=System.currentTimeMillis();
//        long n1 = System.nanoTime();
        //        Arrays.fill(dr.pixels, Color.BLACK.getRGB());

        //        int latticeWidth = fd.getLatticeWidth();
        //        int latticeHeight = fd.getLatticeHeight();
        final int startX = dr.dispRect.x;
        final int startY = dr.dispRect.y;
        final int dispW = dr.dispRect.width;
        final int dispH = dr.dispRect.height;
        final int dispR = startX + dispW;
        final int dispB = startY + dispH;

        for(i=startX;i<dispR;++i)
            for(j=startY;j<dispB;++j)
            {
                x = i+dr.offset.x - x0;
                y = j+dr.offset.y - y0; // offset of figure
                //                in[0] = x;
                //                in[1] = y;
                fun(x,y,res);

                srcX = x0 + res[0]; 
                srcY = y0 + res[1]; 

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
                    //                                          pixels[i+j*width] = ((res[0]*256)/det)+((res[1]*256)/det)*256;
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
                    dr.pixels[i+j*dr.destRect.width] = 0;
                }
            }
        //long t2 = System.currentTimeMillis();
//        long n2 = System.nanoTime();

        //System.out.println(n2-n1);

        dr.fillSource();
    }
    abstract void fun(int x,int y,int[] out);


    public static final TessRule trans = new BasicRule("Translation",
    "A Translation of the image.\n" +
    "Translates green point to red point") {
        //@Override
        public final void fun(int x,int y,int[] out)
        {
            out[0] = x - frameU.x;
            out[1] = y - frameU.y;
        }
    };

    public static final TessRule rot = new BasicRule("Rotation",
    "A rotation of the image.\nRotates around the green point") {
        protected void paintSymetries(Vec U, Vec V, Vec O) {
            this.drawRotationPoint(frameO, 10);
        }

        //@Override
        public final void fun(int x,int y,int[] out)
        {
            double x1 = cos * x + sin * y;
            double y1 = -sin * x + cos * y; 
            out[0] = (int) (x1+0.5);     
            out[1] = (int) (y1+0.5);
        }
        
        
    };

    public static final TessRule reflect = new BasicRule("Reflection",
    "A reflection of the image.\n"
    + "Reflects along the line through the green point and red point") {
        //@Override
        public final void fun(int x,int y,int[] out)
        {
            int xdotu = x * frameU.x + y * frameU.y;
            int xdotv = x * frameV.x + y * frameV.y;
            out[0] = (frameU.x * xdotu - frameV.x * xdotv) / lensq ;     
            out[1] = (frameU.y * xdotu - frameV.y * xdotv) / lensq ;     
        }
        
        protected void paintSymetries(Vec U, Vec V, Vec O) {
            Vec[] points = laticePoints();
            for(int i=1;i<points.length;++i) {
                this.drawReflectionLine(points[i-1], points[i]);
            }
        }

    };

    public Vec[] laticePoints() {
        Rectangle rect = this.paintFd.graphics.getClipBounds();
        Vec[] corners = new Vec[]{new Vec(rect.x,rect.y),
                new Vec(rect.x+rect.width,rect.y),
                new Vec(rect.x+rect.width,rect.y+rect.height),
                new Vec(rect.x,rect.y+rect.height)};
        int minDot = Integer.MAX_VALUE;
        int maxDot = Integer.MIN_VALUE;
        for(int i=0;i<4;++i) {
            Vec p = corners[i].sub(frameO);
            int dot = p.dot(frameU);
            if(dot<minDot) minDot=dot;
            if(dot>maxDot) maxDot=dot;
        }
        int len = frameU.lenSq();
        int minU = minDot < 0 ? minDot / len-1 : minDot / len-1 ; 
        int maxU = maxDot < 0 ? maxDot / len : maxDot / len+1 ;

        Vec[] points = new Vec[maxU-minU+1];
        for(int i=minU;i<=maxU;++i) {
            points[i-minU] = frameU.mul(i).add(frameO);
        }
        return points;
    }

    public static final TessRule glide = new BasicRule("Glide-Reflection",
    "First the image is translated along a line and then reflected along the line.\n"
    + "The lenght of the line specifies the length of the translation.") {
        protected void paintSymetries(Vec U, Vec V, Vec O) {
            Vec[] points = laticePoints();
            for(int i=1;i<points.length;++i) {
                this.drawGlideLine(points[i-1], points[i]);
            }
        }

        //@Override
        public final void fun(int x,int y,int[] out)
        {
            int xdotu = x * frameU.x + y * frameU.y;
            int xdotv = x * frameV.x + y * frameV.y;
            out[0] = -frameU.x + (frameU.x * xdotu - frameV.x * xdotv) / lensq ;     
            out[1] = -frameU.y + (frameU.y * xdotu - frameV.y * xdotv) / lensq ;     
        }
        
        
    };

    public static final TessRule scale = new BasicRule("Scale",
    "A uniform scalling") {
        //@Override
        public final void fun(int x,int y,int[] out)
        {
            double x1 = (len * x)/unitLen;
            double y1 = (len * y)/unitLen; 
            out[0] = (int) (x1+0.5);     
            out[1] = (int) (y1+0.5);
        }
    };

    public static final TessRule scaleXY = new BasicRule("Scale XY",
    "Scalling in XY direction") {
        //@Override
        public final void fun(int x,int y,int[] out)
        {
            double x1 = (frameU.x * x)/unitLen;
            double y1 = (frameU.y * y)/unitLen; 
            out[0] = (int) (x1+0.5);     
            out[1] = (int) (y1+0.5);
        }
    };

    public static final TessRule shear = new BasicRule("Linear",
    "General linear map\n"
    + "Specified by two vectors") {
        public void calcFrame(FundamentalDomain fd, int selVert, boolean constrained) {
            frameO.set(fd.cellVerts[1]);
            frameU.set(fd.cellVerts[0].sub(fd.cellVerts[1]));
            frameV.set(fd.cellVerts[2].sub(fd.cellVerts[1]));
            det = frameU.x * frameV.y - frameU.y * frameV.x;

        }

        public void fixVerticies(FundamentalDomain fd) {
            fd.cellVerts[0].set(frameO.add(frameU));
            fd.cellVerts[1].set(frameO);
            fd.cellVerts[2].set(frameO.add(frameV));
            fd.cellVerts[3].set(frameO);
            fd.numSelPoints = 3;
            fd.numOuterPoints = 2;
            fd.setLatticeType(FundamentalDomain.BASIC);
        }


        public void calcFund(FundamentalDomain fd) {
            fd.fund[0].set(frameO.add(frameU));
            fd.fund[1].set(frameO);
            fd.fund[2].set(frameO.add(frameV));
            fd.fund[3].set(frameO);
            fd.numFund = 4;
        }

        //@Override
        public final void fun(int x,int y,int[] out)
        {
            out[0] = (frameV.y * x - frameV.x * y)*unitLen/det ;     
            out[1] = (frameU.y * x - frameU.x * y)*unitLen/det ;
        }
    };
    
}
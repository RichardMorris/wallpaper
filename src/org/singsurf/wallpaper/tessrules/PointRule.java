/**
 * Represents the two classes of point groups, the cyclic groups and the dihedral groups.
 */
package org.singsurf.wallpaper.tessrules;

import org.singsurf.wallpaper.DrawableRegion;
import org.singsurf.wallpaper.FundamentalDomain;
import org.singsurf.wallpaper.Vec;

public abstract class PointRule extends TessRule
{
    static final boolean DEBUG=false;
    int n;
    double cos,sin;
    boolean dihedral;
    double spokesX[],spokesY[];
    
    static final int NUM_RULES = 10;
    public static final CyclicRule[] cycleRules = new CyclicRule[NUM_RULES+1];
    public static final DihedralRule[] dyhRules = new DihedralRule[NUM_RULES+1];

    static {
        for(int i=1;i<=NUM_RULES;++i) {
            if(i>=2)
                cycleRules[i] = new CyclicRule(i);
            dyhRules[i] = new DihedralRule(i);
        }
    }
    public PointRule(int n,String name, String message) {
        super(name, message);
        this.n = n;
        this.cos = Math.cos(2*Math.PI/n);
        this.sin = Math.sin(2*Math.PI/n);
        spokesX = new double[n];
        spokesY = new double[n];
    }

    int det=1;

    //@Override
    public void calcFrame(FundamentalDomain fd,int selVert, boolean constrained)
    {
        int u1,u2,v1,v2; //,w1,w2;

        v1 =	fd.cellVerts[0].x - fd.cellVerts[1].x;
        v2 =	fd.cellVerts[0].y - fd.cellVerts[1].y;
        u1 = (int) (cos * v1 - sin * v2);
        u2 = (int) (+sin * v1 + cos * v2);
        det = u1 * v2 - v1 * u2;
        frameO.x = fd.cellVerts[1].x;
        frameO.y = fd.cellVerts[1].y;
        frameU.x = v1;
        frameU.y = v2;
        frameV.x = u1;
        frameV.y = u2;
        for(int i=0;i<n;++i) {
            spokesX[i] = (Math.cos((2*Math.PI*i)/n) * v1 - Math.sin((2*Math.PI*i)/n) * v2) /
            Math.sqrt(v1*v1+v2*v2); 
            spokesY[i] = (Math.sin((2*Math.PI*i)/n) * v1 + Math.cos((2*Math.PI*i)/n) * v2) / 
            Math.sqrt(v1*v1+v2*v2); 
        }
    }

    //@Override
    public void fixVerticies(FundamentalDomain fd)
    {
        fd.cellVerts[0].x = frameO.x+frameU.x;
        fd.cellVerts[0].y = frameO.y+frameU.y;
        fd.cellVerts[2].x = frameO.x+frameV.x;
        fd.cellVerts[2].y = frameO.y+frameV.y;
        fd.cellVerts[3].x = frameO.x+100*frameV.x;
        fd.cellVerts[3].y = frameO.y+100*frameV.y;
        fd.cellVerts[4].x = frameO.x+100*frameU.x;
        fd.cellVerts[4].y = frameO.y+100*frameU.y;
        fd.numSelPoints = 2;
        fd.numOuterPoints = 1;
        fd.setLatticeType(FundamentalDomain.POINT);
    }

    //@Override
    public final void fun(int[] in, int[] out, int det) {
        // TODO Auto-generated method stub

    }

    //@Override
    public void replicate(DrawableRegion dr,FundamentalDomain fd) {
        int x0=frameO.x;
        int y0=frameO.y;
        //double len = Math.sqrt(spokesX[0] * spokesX[0] + spokesY[0] * spokesY[0]);
        boolean error_flag = false;

        for(int i=dr.dispRect.x;i<dr.dispRect.width+dr.dispRect.x;++i)
            for(int j=dr.dispRect.y;j<dr.dispRect.height+dr.dispRect.y;++j)
            {
                int x = i+dr.offset.x - x0;
                int y = j+dr.offset.y - y0; // offset of figure

                // Find the max value of cos among the spokes
                double cosSel = -Double.MAX_VALUE;
                double sinSel=0.0;
                int which = 0;
                boolean mirror=false;
                for(int k=0;k<n;++k) {
                    double cos =  x * spokesX[k] + y * spokesY[k];
                    double sin = -x * spokesY[k] + y * spokesX[k];
                    if(sin>0 || (sin==0 && cos >= 0 ) ){ 
                        if(cos>cosSel) {
                            cosSel=cos;
                            sinSel=sin;
                            which = k;
                        }
                    }
                }
                if(this.dihedral) {
                    int next = (which+1)%n;
                    double cos2 =  x * spokesX[next] + y * spokesY[next];
                    double sin2 = -x * spokesY[next] + y * spokesX[next];
                    if(cos2>cosSel) {
                        cosSel = cos2;
                        sinSel = -sin2;
                        mirror=true;
                    }
                }
                which = 1;
                int srcX,srcY;

                if(which==0 && !mirror) {
                    srcX = x0 + x;
                    srcY = y0 + y;
                }
                else {
                    double x1 = cosSel * spokesX[0] - sinSel * spokesY[0]; 
                    double y1 = cosSel * spokesY[0] + sinSel * spokesX[0]; 
                    srcX = x0 + (int) (x1+0.5);	
                    srcY = y0 + (int) (y1+0.5);
                }
                try
                {
                    if(srcX<0 || srcX>=dr.srcRect.width || srcY<0 || srcY>=dr.srcRect.height) {
                        if(TessRule.tileBackground) {
                            srcX %= dr.srcRect.width; 
                            if(srcX <0) srcX += dr.srcRect.width;
                            srcY %= dr.srcRect.height; 
                            if(srcY <0) srcY += dr.srcRect.height;
                            int outInd = i+j*dr.destRect.width;
                            int inInd = srcX+srcY*dr.srcRect.width;
                            int px = dr.inpixels[inInd];
                            dr.pixels[outInd] = px;
                        }
                        else
                            dr.pixels[i+j*dr.dispRect.width] = backgroundRGB;
                    }
                    else {
                        int outInd = i+j*dr.destRect.width;
                        int inInd = srcX+srcY*dr.srcRect.width;
                        int px = dr.inpixels[inInd];
                        dr.pixels[outInd] = px;
                    }
                    //					pixels[i+j*width] = ((res[0]*256)/det)+((res[1]*256)/det)*256;
                }
                catch(Exception e)
                {
                    if(!error_flag)
                        System.out.println("Error ("+i+","+j+") det "+det
                                + " x "+x
                                + " y "+y
                                + " sX "+srcX
                                + " sY "+srcY
                        );
                    error_flag = true;
                    dr.pixels[i+j*dr.destRect.width] = 0;
                }
            }

        dr.fillSource();
    }

    public static class CyclicRule extends PointRule {
        CyclicRule(int n) {
            super(n,"C"+n,
                    "Cyclic groups describe rotation by 2 pi/n around a single point.\n" +
            "It is equivalent to the group of integers mod n under addition.");
            this.dihedral = false;
        }

        /** Calculates the fundamental domain */
        //@Override
        public void calcFund(FundamentalDomain fd)
        {
            if(n==2) {
                fd.fund[0].x = fd.cellVerts[1].x+100*frameU.x;
                fd.fund[0].y = fd.cellVerts[1].y+100*frameU.y;
                fd.fund[1].x = fd.cellVerts[1].x;
                fd.fund[1].y = fd.cellVerts[1].y;
                fd.fund[2].x = fd.cellVerts[1].x-100*frameU.x;
                fd.fund[2].y = fd.cellVerts[1].y-100*frameU.y;
                fd.numFund = 3;
            }
            else {
                fd.fund[0].x = fd.cellVerts[1].x+100*frameV.x; 
                fd.fund[0].y = fd.cellVerts[1].y+100*frameV.y;
                fd.fund[1].x = fd.cellVerts[1].x;
                fd.fund[1].y = fd.cellVerts[1].y;
                fd.fund[2].x = fd.cellVerts[1].x+100*frameU.x;
                fd.fund[2].y = fd.cellVerts[1].y+100*frameU.y;
                fd.numFund = 3;
            }
        }

        //@Override
        public void paintDomainEdges(Vec U, Vec V, Vec O, int det) {
            for(int i=0;i<spokesX.length;++i)
                this.drawSimpleEdge(frameO, new Vec((int) (spokesX[i]*1000),(int) (spokesY[i]*1000)).add(frameO));
       }

        //@Override
        protected void paintSymetries(Vec U, Vec V, Vec O) {
            this.drawRotationPoint(frameO, n);
        }
        
        
    } // end CyclicRule

    public static class DihedralRule extends PointRule {
        DihedralRule(int n) {
            super(n,"D"+n,
                    "The dihedral group of order n has a rotations of 2pi/n and n axis of reflection.\n" +
            "It is the symmetry group of and n sided regular polygon.");
            this.dihedral = true;
            if(n==1)
                this.message = "The first dihedral group D2 is just a reflection in a line";
        }

        /** Calculates the fundamental domain */
        //@Override
        public void calcFund(FundamentalDomain fd)
        {
            if(n==1) {
                fd.fund[0].x = fd.cellVerts[1].x+100*frameU.x;
                fd.fund[0].y = fd.cellVerts[1].y+100*frameU.y;
                fd.fund[1].x = fd.cellVerts[1].x;
                fd.fund[1].y = fd.cellVerts[1].y;
                fd.fund[2].x = fd.cellVerts[1].x-100*frameU.x;
                fd.fund[2].y = fd.cellVerts[1].y-100*frameU.y;
                fd.fund[3].x = fd.cellVerts[1].x;
                fd.fund[3].y = fd.cellVerts[1].y;
                fd.numFund = 4;
            }
            else if(n==2) {
                fd.fund[0].x = fd.cellVerts[1].x+100*frameU.x;
                fd.fund[0].y = fd.cellVerts[1].y+100*frameU.y;
                fd.fund[1].x = fd.cellVerts[1].x;
                fd.fund[1].y = fd.cellVerts[1].y;
                fd.fund[2].x = fd.cellVerts[1].x-100*frameU.y;
                fd.fund[2].y = fd.cellVerts[1].y+100*frameU.x;
                fd.fund[3].x = fd.cellVerts[1].x;
                fd.fund[3].y = fd.cellVerts[1].y;
                fd.numFund = 4;
            }
            else {
                fd.fund[0].x = fd.cellVerts[1].x+100*frameU.x; 
                fd.fund[0].y = fd.cellVerts[1].y+100*frameU.y;
                fd.fund[1].x = fd.cellVerts[1].x;
                fd.fund[1].y = fd.cellVerts[1].y;
                fd.fund[2].x = fd.cellVerts[1].x+50*frameU.x+50*frameV.x;
                fd.fund[2].y = fd.cellVerts[1].y+50*frameU.y+50*frameV.y;
                fd.numFund = 3;
            }
        }
        
        //@Override
        public void paintDomainEdges(Vec U, Vec V, Vec O, int det) {
            for(int i=0;i<spokesX.length;++i)
                this.drawSimpleEdge(frameO, new Vec((int) (spokesX[i]*1000),(int) (spokesY[i]*1000)).add(frameO));


            if(n==1) {
                this.drawSimpleEdge(frameO, 
                        new Vec((int) -(spokesX[0]*1000),
                                (int) -(spokesY[0]*1000)).add(frameO));
            }
            else if(n ==2) {
                this.drawSimpleEdge(frameO, 
                        new Vec((int) -(spokesY[0]*1000),
                                (int) (spokesX[0]*1000)).add(frameO));
                this.drawSimpleEdge(frameO, 
                        new Vec((int) (spokesY[0]*1000),
                                (int) -(spokesX[0]*1000)).add(frameO));
            }
            else
            for(int i=0;i<spokesX.length;++i)
                this.drawSimpleEdge(frameO, 
                        new Vec((int) ((spokesX[i]+spokesX[(i+1)%spokesX.length])*1000),
                                (int) ((spokesY[i]+spokesY[(i+1)%spokesX.length])*1000)).add(frameO));
        }

        //@Override
        protected void paintSymetries(Vec U, Vec V, Vec O) {
            this.drawRotationPoint(frameO, n);
            for(int i=0;i<spokesX.length;++i)
                this.drawReflectionLine(frameO, new Vec((int) (spokesX[i]*1000),(int) (spokesY[i]*1000)).add(frameO));


            if(n==1)
                this.drawReflectionLine(frameO, 
                        new Vec((int) -(spokesX[0]*1000),
                                (int) -(spokesY[0]*1000)).add(frameO));
            else if(n==2) {
                this.drawReflectionLine(frameO, 
                        new Vec((int) -(spokesY[0]*1000),
                                (int) (spokesX[0]*1000)).add(frameO));
                this.drawReflectionLine(frameO, 
                        new Vec((int) (spokesY[0]*1000),
                                (int) -(spokesX[0]*1000)).add(frameO));

            }
            else
                for(int i=0;i<spokesX.length;++i)
                    this.drawReflectionLine(frameO, 
                        new Vec((int) ((spokesX[i]+spokesX[(i+1)%spokesX.length])*1000),
                                (int) ((spokesY[i]+spokesY[(i+1)%spokesX.length])*1000)).add(frameO));
            
         }

        
    }
}
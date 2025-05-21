/**
 * 
 */
package org.singsurf.wallpaper.tessrules;

import java.awt.Rectangle;

import org.singsurf.wallpaper.FundamentalDomain;
import org.singsurf.wallpaper.Vec;

public abstract class FrezeRule extends TessRule
{
    public FrezeRule(String name, String message) {
        super(name, message);
    }

    int det=1;

    @Override
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

    @Override
    public void fixVerticies(FundamentalDomain fd)
    {
        fd.cellVerts[0].x = frameO.x+frameU.x;
        fd.cellVerts[0].y = frameO.y+frameU.y;
        fd.cellVerts[2].x = frameO.x+100*frameV.x;
        fd.cellVerts[2].y = frameO.y+100*frameV.y;
        fd.cellVerts[3].x = frameO.x+frameU.x+100*frameV.x;
        fd.cellVerts[3].y = frameO.y+frameU.y+100*frameV.y;
        fd.numSelPoints = 2;
        fd.numOuterPoints = 4;
        fd.setLatticeType(FundamentalDomain.FRIEZE);

    }

    public Vec[] laticePoints() {
        Rectangle rect = paintFd.graphics.getClipBounds();
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
    public static TessRule F1 = new FrezeRule("F1",
            "The simplest frieze group with a single translation in one direction.") {
        /** Calculates the fundamental domain */
        @Override
        public void calcFund(FundamentalDomain fd)
        {
            fd.fund[0].x = fd.cellVerts[1].x-100*frameV.x; 
            fd.fund[0].y = fd.cellVerts[1].y-100*frameV.y;
            fd.fund[1].x = fd.cellVerts[1].x+100*frameV.x;
            fd.fund[1].y = fd.cellVerts[1].y+100*frameV.y;
            fd.fund[2].x = fd.cellVerts[1].x+frameU.x/2+100*frameV.x;
            fd.fund[2].y = fd.cellVerts[1].y+frameU.y/2+100*frameV.y;
            fd.fund[3].x = fd.cellVerts[1].x+frameU.x/2-100*frameV.x;
            fd.fund[3].y = fd.cellVerts[1].y+frameU.y/2-100*frameV.y;
            fd.numFund = 4;
        }

        @Override
        public void fun(int[] in,int[] out,int det)
        {
            int alpha = in[0] % (det/2); if(alpha < 0) alpha = alpha + (det/2);
            int beta = in[1];
            out[0] = alpha;
            out[1] = beta;
        }

        @Override
        public void paintDomainEdges(Vec U, Vec V, Vec O, int det) {
            Vec[] points = laticePoints();
            for(int i=0;i<points.length;++i)
            {
                drawSimpleEdge(points[i],points[i].add(frameV.mul(100)));
                drawSimpleEdge(points[i],points[i].add(frameV.mul(-100)));
            }
        }

        @Override
        protected void paintSymetries(Vec U, Vec V, Vec O) { /* no symmetries */  }


    };

    public static TessRule F4 = new FrezeRule("F4",
    "This group has two reflections in parallel lines and a translation in a perpendicular direction.") {
        /** Calculates the fundamental domain */
        @Override
        public void calcFund(FundamentalDomain fd)
        {
            fd.fund[0].x = fd.cellVerts[1].x-100*frameV.x; 
            fd.fund[0].y = fd.cellVerts[1].y-100*frameV.y;
            fd.fund[1].x = fd.cellVerts[1].x+100*frameV.x;
            fd.fund[1].y = fd.cellVerts[1].y+100*frameV.y;
            fd.fund[2].x = fd.cellVerts[1].x+frameU.x/2+100*frameV.x;
            fd.fund[2].y = fd.cellVerts[1].y+frameU.y/2+100*frameV.y;
            fd.fund[3].x = fd.cellVerts[1].x+frameU.x/2-100*frameV.x;
            fd.fund[3].y = fd.cellVerts[1].y+frameU.y/2-100*frameV.y;
            fd.numFund = 4;
        }

        @Override
        public void fun(int[] in,int[] out,int det)
        {
            int alpha = in[0] % det; if(alpha < 0) alpha = alpha + det;
            int beta = in[1];
            if(2*alpha>det) alpha = det - alpha;
            out[0] = alpha;
            out[1] = beta;
        }	
        
        @Override
        public void paintDomainEdges(Vec U, Vec V, Vec O, int det) {
            Vec[] points = laticePoints();
            for(int i=0;i<points.length;++i)
            {
                drawSimpleEdge(points[i],points[i].add(frameV.mul(100)));
                drawSimpleEdge(points[i],points[i].add(frameV.mul(-100)));
            }
            for(int i=0;i<points.length-1;++i)
            {
                Vec v = Vec.linComb(1, points[i], 1,points[i+1], 2);
                drawSimpleEdge(v,v.add(frameV.mul(100)));
                drawSimpleEdge(v,v.add(frameV.mul(-100)));
            }
        }

        @Override
        protected void paintSymetries(Vec U, Vec V, Vec O) {
            Vec[] points = laticePoints();
            for(int i=0;i<points.length;++i)
            {
                drawReflectionLine(points[i],points[i].add(frameV.mul(100)));
                drawReflectionLine(points[i],points[i].add(frameV.mul(-100)));
            }
            for(int i=0;i<points.length-1;++i)
            {
                Vec v = Vec.linComb(1, points[i], 1,points[i+1], 2);
                drawReflectionLine(v,v.add(frameV.mul(100)));
                drawReflectionLine(v,v.add(frameV.mul(-100)));
            }
        }


    };

    public static TessRule F3 = new FrezeRule("F3",
    "This has one line of reflection and a translation along that line.") {
        /** Calculates the fundamental domain */
        @Override
        public void calcFund(FundamentalDomain fd)
        {
            fd.fund[0].x = fd.cellVerts[1].x; 
            fd.fund[0].y = fd.cellVerts[1].y;
            fd.fund[1].x = fd.cellVerts[1].x+100*frameV.x;
            fd.fund[1].y = fd.cellVerts[1].y+100*frameV.y;
            fd.fund[2].x = fd.cellVerts[1].x+frameU.x/2+100*frameV.x;
            fd.fund[2].y = fd.cellVerts[1].y+frameU.y/2+100*frameV.y;
            fd.fund[3].x = fd.cellVerts[1].x+frameU.x/2;
            fd.fund[3].y = fd.cellVerts[1].y+frameU.y/2;
            fd.numFund = 4;
        }

        @Override
        public void fun(int[] in,int[] out,int det)
        {
            int alpha = in[0] % (det/2); if(alpha < 0) alpha = alpha + (det/2);
            int beta = in[1];
            if(beta<0) beta = -beta;
            out[0] = alpha;
            out[1] = beta;
        }	
        
        @Override
        public void paintDomainEdges(Vec U, Vec V, Vec O, int det) {
            Vec[] points = laticePoints();
            for(int i=0;i<points.length;++i)
            {
                drawSimpleEdge(points[i],points[i].add(frameV.mul(100)));
                drawSimpleEdge(points[i],points[i].add(frameV.mul(-100)));
            }
            for(int i=0;i<points.length-1;++i)
            {
                drawSimpleEdge(points[i],points[i+1]);
            }
        }

        @Override
        protected void paintSymetries(Vec U, Vec V, Vec O) {
            Vec[] points = laticePoints();
            for(int i=0;i<points.length-1;++i)
            {
                drawReflectionLine(points[i],points[i+1]);
            }
       }

    };

    public static TessRule F7 = new FrezeRule("F7",
            "Two reflections in parallel lines and one reflection in a perpendicular line.\n" +
    "It also has a translation and two 180 degree rotations where the lines of reflections meet.\n")
    {
        /** Calculates the fundamental domain */
        @Override
        public void calcFund(FundamentalDomain fd)
        {
            fd.fund[0].x = fd.cellVerts[1].x; 
            fd.fund[0].y = fd.cellVerts[1].y;
            fd.fund[1].x = fd.cellVerts[1].x+100*frameV.x;
            fd.fund[1].y = fd.cellVerts[1].y+100*frameV.y;
            fd.fund[2].x = fd.cellVerts[1].x+frameU.x/2+100*frameV.x;
            fd.fund[2].y = fd.cellVerts[1].y+frameU.y/2+100*frameV.y;
            fd.fund[3].x = fd.cellVerts[1].x+frameU.x/2;
            fd.fund[3].y = fd.cellVerts[1].y+frameU.y/2;
            fd.numFund = 4;
        }

        @Override
        public void fun(int[] in,int[] out,int det)
        {
            int alpha = in[0] % det; if(alpha < 0) alpha = alpha + det;
            int beta = in[1];
            if(beta<0) beta = -beta;
            if(2*alpha>det) alpha = det - alpha;
            out[0] = alpha;
            out[1] = beta;
        }	
        
        @Override
        public void paintDomainEdges(Vec U, Vec V, Vec O, int det) {
            Vec[] points = laticePoints();
            for(int i=0;i<points.length;++i)
            {
                drawSimpleEdge(points[i],points[i].add(frameV.mul(100)));
                drawSimpleEdge(points[i],points[i].add(frameV.mul(-100)));
            }
            for(int i=0;i<points.length-1;++i)
            {
                Vec v = Vec.linComb(1, points[i], 1,points[i+1], 2);
                drawSimpleEdge(v,v.add(frameV.mul(100)));
                drawSimpleEdge(v,v.add(frameV.mul(-100)));
                drawSimpleEdge(points[i],points[i+1]);
            }
        }

        @Override
        protected void paintSymetries(Vec U, Vec V, Vec O) {
            Vec[] points = laticePoints();
            for(int i=0;i<points.length;++i)
            {
                drawReflectionLine(points[i],points[i].add(frameV.mul(100)));
                drawReflectionLine(points[i],points[i].add(frameV.mul(-100)));

                drawRotationPoint(points[i],2);
            }
            for(int i=0;i<points.length-1;++i)
            {
                Vec v = Vec.linComb(1, points[i], 1,points[i+1], 2);
                drawRotationPoint(v,2);
                drawReflectionLine(v,v.add(frameV.mul(100)));
                drawReflectionLine(v,v.add(frameV.mul(-100)));
                drawReflectionLine(points[i],points[i+1]);
           }
        }

    };

    public static TessRule F5 = new FrezeRule("F5",
    "Two 180 degree rotations and a translation.") {
        /** Calculates the fundamental domain */
        @Override
        public void calcFund(FundamentalDomain fd)
        {
            fd.fund[0].x = fd.cellVerts[1].x-100*frameV.x; 
            fd.fund[0].y = fd.cellVerts[1].y-100*frameV.y;
            fd.fund[1].x = fd.cellVerts[1].x+100*frameV.x;
            fd.fund[1].y = fd.cellVerts[1].y+100*frameV.y;
            fd.fund[2].x = fd.cellVerts[1].x+frameU.x/2+100*frameV.x;
            fd.fund[2].y = fd.cellVerts[1].y+frameU.y/2+100*frameV.y;
            fd.fund[3].x = fd.cellVerts[1].x+frameU.x/2-100*frameV.x;
            fd.fund[3].y = fd.cellVerts[1].y+frameU.y/2-100*frameV.y;
            fd.numFund = 4;
        }

        @Override
        public void fun(int[] in,int[] out,int det)
        {
            int alpha = in[0] % det; if(alpha < 0) alpha = alpha + det;
            int beta = in[1];

            if(2*alpha>det) {
                alpha = det - alpha;
                beta = - beta;
            }
            out[0] = alpha;
            out[1] = beta;
        }	
 
        @Override
        public void paintDomainEdges(Vec U, Vec V, Vec O, int det) {
            Vec[] points = laticePoints();
            for(int i=0;i<points.length;++i)
            {
                drawSimpleEdge(points[i],points[i].add(frameV.mul(100)));
                drawSimpleEdge(points[i],points[i].add(frameV.mul(-100)));
            }
            for(int i=0;i<points.length-1;++i)
            {
                Vec v = Vec.linComb(1, points[i], 1,points[i+1], 2);
                drawSimpleEdge(v,v.add(frameV.mul(100)));
                drawSimpleEdge(v,v.add(frameV.mul(-100)));
            }
        }

        @Override
        protected void paintSymetries(Vec U, Vec V, Vec O) {
            Vec[] points = laticePoints();
            for(int i=0;i<points.length;++i)
            {
                drawRotationPoint(points[i],2);
            }
            for(int i=0;i<points.length-1;++i)
            {
                Vec v = Vec.linComb(1, points[i], 1,points[i+1], 2);
                drawRotationPoint(v,2);
            }
        }

    };

    public static TessRule F2 = new FrezeRule("F2",
    "A glide reflection: translate in one direction then reflect.") {
        /** Calculates the fundamental domain */
        @Override
        public void calcFund(FundamentalDomain fd)
        {
            fd.fund[0].x = fd.cellVerts[1].x-100*frameV.x; 
            fd.fund[0].y = fd.cellVerts[1].y-100*frameV.y;
            fd.fund[1].x = fd.cellVerts[1].x+100*frameV.x;
            fd.fund[1].y = fd.cellVerts[1].y+100*frameV.y;
            fd.fund[2].x = fd.cellVerts[1].x+frameU.x/2+100*frameV.x;
            fd.fund[2].y = fd.cellVerts[1].y+frameU.y/2+100*frameV.y;
            fd.fund[3].x = fd.cellVerts[1].x+frameU.x/2-100*frameV.x;
            fd.fund[3].y = fd.cellVerts[1].y+frameU.y/2-100*frameV.y;
            fd.numFund = 4;
        }

        @Override
        public void fun(int[] in,int[] out,int det)
        {
            int alpha = in[0] % det; if(alpha < 0) alpha = alpha + det;
            int beta = in[1];

            if(2*alpha>det) {
                alpha = alpha - det/2;
                beta = - beta;
            }
            out[0] = alpha;
            out[1] = beta;
        }	
        
        @Override
        public void paintDomainEdges(Vec U, Vec V, Vec O, int det) {
            Vec[] points = laticePoints();
            for(int i=0;i<points.length;++i)
            {
                drawSimpleEdge(points[i],points[i].add(frameV.mul(100)));
                drawSimpleEdge(points[i],points[i].add(frameV.mul(-100)));
            }
            for(int i=0;i<points.length-1;++i)
            {
                Vec v = Vec.linComb(1, points[i], 1,points[i+1], 2);
                drawSimpleEdge(v,v.add(frameV.mul(100)));
                drawSimpleEdge(v,v.add(frameV.mul(-100)));
            }
        }

        @Override
        protected void paintSymetries(Vec U, Vec V, Vec O) {
            Vec[] points = laticePoints();
            for(int i=0;i<points.length-1;++i)
            {
                drawGlideLine(points[i],points[i+1]);
            }
       }

    };

    public static TessRule F6 = new FrezeRule("F6",
    "Two rotations, two reflections, a glide reflection and a translation.") {
        /** Calculates the fundamental domain */
        @Override
        public void calcFund(FundamentalDomain fd)
        {
            fd.fund[0].x = fd.cellVerts[1].x-100*frameV.x; 
            fd.fund[0].y = fd.cellVerts[1].y-100*frameV.y;
            fd.fund[1].x = fd.cellVerts[1].x+100*frameV.x;
            fd.fund[1].y = fd.cellVerts[1].y+100*frameV.y;
            fd.fund[2].x = fd.cellVerts[1].x+frameU.x/2+100*frameV.x;
            fd.fund[2].y = fd.cellVerts[1].y+frameU.y/2+100*frameV.y;
            fd.fund[3].x = fd.cellVerts[1].x+frameU.x/2-100*frameV.x;
            fd.fund[3].y = fd.cellVerts[1].y+frameU.y/2-100*frameV.y;
            fd.numFund = 4;
        }

        @Override
        public void fun(int[] in,int[] out,int det)
        {
            int alpha = in[0] % (2*det); if(alpha < 0) alpha = alpha + 2*det;
            int beta = in[1];

            int index = (4*alpha) / (2*det);

            switch(index) {
            case 0:
                break;
            case 1:
                alpha = det - alpha;
                beta = -beta;
                break;
            case 2:
                alpha = alpha - det;
                beta = -beta;
                break;
            case 3:
                alpha = 2*det - alpha;
                break;
            }
            out[0] = alpha;
            out[1] = beta;
        }	
        
        @Override
        public void paintDomainEdges(Vec U, Vec V, Vec O, int det) {
            Vec[] points = laticePoints();
            for(int i=0;i<points.length;++i)
            {
                drawSimpleEdge(points[i],points[i].add(frameV.mul(100)));
                drawSimpleEdge(points[i],points[i].add(frameV.mul(-100)));
            }
            for(int i=0;i<points.length-1;++i)
            {
                Vec v = Vec.linComb(1, points[i], 1,points[i+1], 2);
                drawSimpleEdge(v,v.add(frameV.mul(100)));
                drawSimpleEdge(v,v.add(frameV.mul(-100)));
            }
        }

        @Override
        protected void paintSymetries(Vec U, Vec V, Vec O) {
            Vec[] points = laticePoints();
            for(int i=0;i<points.length;++i)
            {
                drawReflectionLine(points[i],points[i].add(frameV.mul(100)));
                drawReflectionLine(points[i],points[i].add(frameV.mul(-100)));
            }
            for(int i=0;i<points.length-1;++i)
            {
                Vec v = Vec.linComb(1, points[i], 1,points[i+1], 2);
                drawRotationPoint(v,2);
                drawGlideLine(points[i], points[i+1]);
            }
        }

    };

}
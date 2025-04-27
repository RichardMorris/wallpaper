/**
 * 
 */
package org.singsurf.wallpaper.tessrules;

import org.singsurf.wallpaper.DVec;
import org.singsurf.wallpaper.FundamentalDomain;
import org.singsurf.wallpaper.Vec;

public abstract class DiamondRule extends TessRule
{
    static final boolean DEBUG = false;
    int det=1;
    int lastSelectedVertex = -1;
    public DiamondRule(String name,String message) { super(name,message); }
    DVec ud, vd, P;
	private double angd;
//    double cosUV,sinUV,lenW,perp;
    //@Override
    public void calcFrame(FundamentalDomain fd,int selectedVertex, boolean constrained)
    {
        //            System.out.println("calcFrame "+Arrays.toString(fd.cellVerts));
        if(constrained) {
            frameO.set(fd.cellVerts[1]);
            frameU.set(fd.cellVerts[0].sub(fd.cellVerts[1]));
            frameV.set(fd.cellVerts[2].sub(fd.cellVerts[1]));
            firstCall = false;
            det = -frameU.cross(frameV); //u1 * v2 - v1 * u2;

            return;
        }
        int u1,u2,v1,v2,w1,w2;

        u1 =	fd.cellVerts[2].x - fd.cellVerts[1].x;
        u2 =	fd.cellVerts[2].y - fd.cellVerts[1].y;
        v1 =	fd.cellVerts[0].x - fd.cellVerts[1].x;
        v2 =	fd.cellVerts[0].y - fd.cellVerts[1].y;
        
        if( selectedVertex != lastSelectedVertex) {
			firstCall = true;
			lastSelectedVertex = selectedVertex;
		}
        if(firstCall) {
			ud = new DVec(u1,u2);
			vd = new DVec(v1,v2);
			angd = ud.angle(vd);
			P = ud.add(vd);
		}

        if(DEBUG) {
            System.out.println("sel "+selectedVertex+" fc "+firstCall);
            System.out.println("u "+u1+","+u2+" v "+v1+","+v2);
        }

        if(selectedVertex==0) {
            var vec = new DVec(v1,v2);
            var rot = vec.rotate(-angd);
            u1 = (int) Math.rint(rot.x);
            u2 = (int) Math.rint(rot.y);
        }
        else if(selectedVertex==1) {
            w1 =  fd.cellVerts[2].x - fd.cellVerts[0].x;
            w2 =  fd.cellVerts[2].y - fd.cellVerts[0].y;
            double lenV = Math.sqrt((double) v1*v1+v2*v2);
            double lenW = Math.sqrt(w1*w1+w2*w2);

            double sinHalf = 0.5 * lenW / lenV;
            double theta = -2 * Math.asin(sinHalf); 
            if(det<0) theta = -theta; 
            u1 = (int) Math.rint(Math.cos(theta) * v1 - Math.sin(theta) * v2);
            u2 = (int) Math.rint(Math.sin(theta) * v1 + Math.cos(theta) * v2);
        }
        else if(selectedVertex==2) {
        	ud = new DVec(u1,u2);
        	var ref = P.reflect(ud);
        	v1 = (int) Math.rint(ref.x);
        	v2 = (int) Math.rint(ref.y);
        }
        if(DEBUG) {
            System.out.println("u "+u1+","+u2+" v "+v1+","+v2);
        }
        det = u1 * v2 - v1 * u2;
        frameO.x = fd.cellVerts[1].x;
        frameO.y = fd.cellVerts[1].y;
        frameU.x = v1;
        frameU.y = v2;
        frameV.x = u1;
        frameV.y = u2;
        firstCall = false;
    }

    public void constrainVertices(Vec[] verts, int selectedVertex) {
        Vec u;
        switch(selectedVertex) {
        case 0:
            u = verts[2].sub(verts[0]);
            u = u.constrainedVec(Math.PI/2);
            if(u.x==0) {
                int y = verts[0].y - verts[1].y;
                verts[0].y = verts[1].y + y;
                verts[2].y = verts[1].y - y;
                verts[2].x = verts[0].x;
            }
            else {
                int x = verts[0].x - verts[1].x;
                verts[0].x = verts[1].x + x;
                verts[2].x = verts[1].x - x;
                verts[2].y = verts[0].y;
            }

            break;
        case 1:
            // first fix blue point
            u = verts[2].sub(verts[0]);
            u = u.constrainedVec(Math.PI/2);
            verts[2].set(verts[0].add(u));

            // now line from mid point of RB to G
            if(u.x==0) {
                Vec v = verts[1].sub(verts[0]);
                verts[2].x=verts[0].x;
                verts[2].y=verts[0].y+v.y*2;
            }
            else {
                Vec v = verts[1].sub(verts[0]);
                verts[2].x=verts[0].x+v.x*2;
                verts[2].y=verts[0].y;
            }

            break;
        case 2:
            // fix blue
            u = verts[2].sub(verts[0]);
            u = u.constrainedVec(Math.PI/2);
            if(u.x==0) {
                u.y = (u.y/2)*2;
            }
            else
                u.x = (u.x/2)*2;
            verts[2].set(verts[0].add(u));

            // fix green same dist from BR line
            if(u.x == 0)
                verts[1].y = (verts[0].y+verts[2].y)/2;
            else
                verts[1].x = (verts[0].x+verts[2].x)/2;

            break;
        default:
            System.out.println("Illegal seletion point");
        return;
        }
    }

    @Override
    public void fixVerticies(FundamentalDomain fd)
    {
        fd.cellVerts[0].x = frameO.x+frameU.x;
        fd.cellVerts[0].y = frameO.y+frameU.y;
        fd.cellVerts[1].x = frameO.x;
        fd.cellVerts[1].y = frameO.y;
        fd.cellVerts[2].x = frameO.x+frameV.x;
        fd.cellVerts[2].y = frameO.y+frameV.y;
        fd.cellVerts[3].x = frameO.x+frameU.x+frameV.x;
        fd.cellVerts[3].y = frameO.y+frameU.y+frameV.y;
        fd.numSelPoints = 3;
        fd.numOuterPoints = 4;
        fd.setLatticeType(FundamentalDomain.PARALLOGRAM);
    }

    public static TessRule rhombCM = new DiamondRule("CM",
            "A reflection through opposite corners of diamond.\n"+
            "The lengths of each side of the diamond are all equal\n"+
            "Only one side of the fundamental domain needs to be a straight line"
    )
    {
        //@Override
        public void calcFund(FundamentalDomain fd)
        {
            fd.fund[0].x = fd.cellVerts[1].x; 
            fd.fund[0].y = fd.cellVerts[1].y;
            fd.fund[1].x = fd.cellVerts[1].x+frameU.x;
            fd.fund[1].y = fd.cellVerts[1].y+frameU.y;
            fd.fund[2].x = fd.cellVerts[1].x+frameV.x;
            fd.fund[2].y = fd.cellVerts[1].y+frameV.y;
            fd.numFund = 3;
        }

        //@Override
        public void fun(int[] in,int[] out,int det)
        {
            int alpha = in[0] % det; if(alpha < 0) alpha = alpha + det;
            int beta = in[1] % det; if(beta < 0) beta = beta + det;
            if((alpha+beta) > det)
            {
                int gamma = beta;
                beta = det - alpha;
                alpha = det - gamma;
            }
            out[0] = alpha;
            out[1] = beta;
        }

        ////@Override
        //@Override
        public void paintSymetries(Vec U,Vec V,Vec O) {
            Vec P1 = U.add(O); 
            Vec P2 = V.add(O);
            drawReflectionLine(P1,P2);
            Vec P5 = Vec.linComb(2,U,-1,V,2).add(O);
            Vec P6 = V.div(2).add(O);
            drawGlideLine(P5,P6);
        }

        //@Override
        public void paintDomainEdges(Vec U, Vec V, Vec O, int det) {
            this.drawSimpleEdge(O, O.add(U));
            this.drawSimpleEdge(O, O.add(V));
            //this.drawSimpleEdge(O, O.sum(U).sum(V));
            this.drawSimpleEdge(O.add(V), O.add(U));
        }

        ////@Override
        //@Override
        public double approxArea() { return 0.5; }
    };


    public static TessRule rhombCMM = new DiamondRule("CMM",
            "Two reflections through opposite corners of diamond.\n"+
            "The reflections must be at right angles and form "+
            "two of the sides of the fundamental domain.\n"+
            "The other side does not needs to be a straight line."
    )
    {
        //@Override
        public void calcFund(FundamentalDomain fd)
        {
            fd.fund[0].x = fd.cellVerts[1].x; 
            fd.fund[0].y = fd.cellVerts[1].y;
            if(det<0)
            {
                fd.fund[1].x = fd.cellVerts[1].x+frameV.x;
                fd.fund[1].y = fd.cellVerts[1].y+frameV.y;
            }
            else
            {
                fd.fund[1].x = fd.cellVerts[1].x+frameU.x;
                fd.fund[1].y = fd.cellVerts[1].y+frameU.y;
            }
            fd.fund[2].x = fd.cellVerts[1].x+(frameV.x+frameU.x)/2;
            fd.fund[2].y = fd.cellVerts[1].y+(frameV.y+frameU.y)/2;
            fd.numFund=3;
        }

        //@Override
        public void fun(int[] in,int[] out,int det)
        {
            int alpha = in[0] % det; if(alpha < 0) alpha = alpha + det;
            int beta = in[1] % det; if(beta < 0) beta = beta + det;
            if((alpha+beta) > det)
            {
                int gamma = beta;
                beta = det - alpha;
                alpha = det - gamma;
            }
            if( beta < alpha ) 
            { 
                int gamma = alpha;
                alpha = beta;
                beta = gamma;
            }
            out[0] = alpha;
            out[1] = beta;
        }

        //@Override
        public void paintSymetries(Vec U,Vec V,Vec O) {
            Vec P1 = U.add(O); 
            Vec P2 = V.add(O);
            drawReflectionLine(P1,P2);
            Vec P3 = O;
            Vec P4 = Vec.linComb(1,U,1,V,1,O);
            drawReflectionLine(P3,P4);
            drawRotationPoint(O,2);
            drawRotationPoint(Vec.linComb(1,U,1,V,2).add(O),2);
            drawRotationPoint(U.div(2).add(O),2);
            drawRotationPoint(V.div(2).add(O),2);

            Vec P5 = Vec.linComb(2,U,-1,V,2).add(O);
            Vec P6 = V.div(2).add(O);

            drawGlideLine(P5,P6);

            Vec P7   = Vec.linComb(2, U, 3, V,2,O,2);
            drawGlideLine(P6,P7);
        }

        //@Override
        public void paintDomainEdges(Vec U, Vec V, Vec O, int det) {
            this.drawSimpleEdge(O, O.add(U));
            this.drawSimpleEdge(O, O.add(V));
            this.drawSimpleEdge(O, O.add(U).add(V));
            this.drawSimpleEdge(O.add(V), O.add(U));
        }

        ////@Override
        //@Override
        public double approxArea() { return 0.25; }

    };

}
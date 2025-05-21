/**
 * 
 */
package org.singsurf.wallpaper.tessrules;

import org.singsurf.wallpaper.FundamentalDomain;
import org.singsurf.wallpaper.Vec;
import org.singsurf.wallpaper.Wallpaper;

public abstract class SquRule extends TessRule
{
    public SquRule(String s,String message) {super(s,message);}

    
    /** The frame uses the base line and a line at right angles. */
    
    public void calcFrame(FundamentalDomain fd,int selectedVertex, boolean constrained)
    {
        int u1,u2,v1,v2; //,w1,w2;

        u1 = fd.cellVerts[0].x - fd.cellVerts[1].x;
        u2 = fd.cellVerts[0].y - fd.cellVerts[1].y;
        v1 = u2;
        v2 = -u1;
        frameO.x= fd.cellVerts[1].x;
        frameO.y= fd.cellVerts[1].y;
        frameU.x= v1;
        frameU.y= v2;
        frameV.x=u1;
        frameV.y=u2;
    }

    public void constrainVertices(Vec[] verts, int selectedVertex) {
        Vec u;
        switch(selectedVertex) {
        case 0:
            u = verts[0].sub(verts[1]);
            u = u.constrainedVec(Math.PI/4);
            verts[0].set(verts[1].add(u));
            break;
        case 1:
            u = verts[1].sub(verts[0]);
            u = u.constrainedVec(Math.PI/4);
            verts[1].set(verts[0].add(u));
            break;
        default:
            System.out.println("Only green or red points can be selected");
            return;
        }
    }

    
    public void fixVerticies(FundamentalDomain fd)
    {
        fd.cellVerts[2].x = frameO.x+frameU.x;
        fd.cellVerts[2].y = frameO.y+frameU.y;
        fd.cellVerts[3].x = frameO.x+frameU.x+frameV.x;
        fd.cellVerts[3].y = frameO.y+frameU.y+frameV.y;
        fd.numOuterPoints = 4;
        fd.numSelPoints = 2;
        fd.setLatticeType(FundamentalDomain.PARALLOGRAM);
    }


    
    public void paintDomainEdges(Vec U, Vec V, Vec O,int det) {
        Vec A = O.add(U);
        Vec B = Vec.linComb(2, O, 1,U,2);
        Vec C = Vec.linComb(2, O, 1, U,2,V,2);
        Vec D = O.add(V);
        Vec F = Vec.linComb(2, O, 1,V,2);
        Vec G = Vec.linComb(2, O, 2, U,1,V,2);

        drawSimpleEdge(O,A);
        drawSimpleEdge(B,C);
        drawSimpleEdge(O,D);
        drawSimpleEdge(F,G);
    }

    
    public void fixFlip(String code, FundamentalDomain fd) {
        if((code == Wallpaper.FLIP_X || code == Wallpaper.FLIP_Y)) {
            calcFrame(fd,0, true);
            int x = fd.cellVerts[0].x;	fd.cellVerts[0].x = fd.cellVerts[1].x; fd.cellVerts[1].x = x;
            int y = fd.cellVerts[0].y;	fd.cellVerts[0].y = fd.cellVerts[1].y; fd.cellVerts[1].y = y;
            calcFrame(fd,0, true);
        }
    }

    /** Calculates 90 degree rotation around (.5,.5). **/
    public void calcRot4(int alpha,int beta,int det,int res[])
    {
        // first calc rotations
        if(2 * alpha > det)
        {
            if(2 * beta > det)
            {
                res[0] = det - alpha;
                res[1] = det - beta;
            }
            else
            {
                res[0] = beta;
                res[1] = det - alpha;
            }
        }
        else
        {
            if(2 * beta > det)
            {
                res[0] = det - beta;
                res[1] = alpha;
            }
            else
            {
                res[0] = alpha;
                res[1] = beta;
            }
        }
    }

    public static TessRule squP4 = new SquRule("P4",
            "A 90\u00ba rotation.\n"+
            "A square has much more symmetry than a rectangle or diamond.\n"+
            "As well as the two  90\u00ba rotation there is an 180\u00ba rotation.\n"+
            "and two lines of reflection."
    )
    {
        
        public void calcFund(FundamentalDomain fd)
        {
            int u1,u2,v1,v2; //,w1,w2;

            u1 =	frameV.x;
            u2 =	frameV.y;
            v1 = 	frameU.x;
            v2 = 	frameU.y;
            fd.fund[0].x = frameO.x; 		fd.fund[0].y = frameO.y;
            fd.fund[1].x = frameO.x + v1/2; 	fd.fund[1].y = frameO.y+v2/2;
            fd.fund[2].x = frameO.x + v1/2+u1/2; fd.fund[2].y = frameO.y+v2/2+u2/2;
            fd.fund[3].x = frameO.x + u1/2;	 	fd.fund[3].y = frameO.y+u2/2;
            fd.numFund = 4;
        }

        
        public void fun(int[] in,int[] out,int det)
        {
            int alpha = in[0] % det; if(alpha < 0) alpha = alpha + det;
            int beta = in[1] % det; if(beta < 0) beta = beta + det;
            calcRot4(alpha,beta,det,out);
        }

        
        protected void paintSymetries(Vec U, Vec V, Vec O) {
            drawRotationPoint(O,4);
            drawRotationPoint(Vec.linComb(1,U,2,O,2),2);
            drawRotationPoint(Vec.linComb(1,V,2,O,2),2);
            drawRotationPoint(Vec.linComb(1,U,1,V,2,O,2),4);
        }

        
        public double approxArea() { return 0.25; }

    };

    public static TessRule squP4g = new SquRule("P4g",
            "A 90\u00ba rotation and a glide-reflection.\n"+
            "There is only one lines of reflection which does not pass through\n"+
            "the 90\u00ba rotation points.\n"+
            "One of my favorite patterns with the rotation appearing\n"+
            "to go in opposite directions.\n"+
            "The fundamental domain is a right angled isosceles triangle."
    )
    {
        
        public void calcFund(FundamentalDomain fd)
        {
            int u1,u2,v1,v2;

            u1 =	frameV.x;
            u2 =	frameV.y;
            v1 = 	frameU.x;
            v2 = 	frameU.y;
            fd.fund[0].x = fd.cellVerts[1].x; fd.fund[0].y = fd.cellVerts[1].y;
            fd.fund[1].x = fd.cellVerts[1].x + u1/2; fd.fund[1].y = fd.cellVerts[1].y + u2/2;
            fd.fund[2].x = fd.cellVerts[1].x + v1/2; fd.fund[2].y = fd.cellVerts[1].y + v2/2;
            fd.numFund=3;
        }

        
        public void fun(int[] in,int[] out,int det)
        {
            int alpha = in[0] % det; if(alpha < 0) alpha = alpha + det;
            int beta = in[1] % det; if(beta < 0) beta = beta + det;
            int res[] = new int[2];
            calcRot4(alpha,beta,det,res);
            alpha = res[0]; beta = res[1];
            if(2 * (alpha + beta ) > det)
            {
                alpha = det/2 - res[1];
                beta = det/2 - res[0]; 
            }
            out[0] = alpha;
            out[1] = beta;
        }

        
        protected void paintSymetries(Vec U, Vec V, Vec O) {

            drawReflectionLine(Vec.linComb(1,U,2,O,2),Vec.linComb(1,V,2,O,2));
            drawReflectionLine(Vec.linComb(1,V,2,O,2),Vec.linComb(1,U,2,V,2,O,2));
            drawReflectionLine(Vec.linComb(1,U,2,V,2,O,2),Vec.linComb(2,U,1,V,2,O,2));
            drawReflectionLine(Vec.linComb(2,U,1,V,2,O,2),Vec.linComb(1,U,2,O,2));

            drawGlideLine(Vec.linComb(1,U,4,O,4),Vec.linComb(1,U,4,V,4,O,4));
            drawGlideLine(Vec.linComb(3,U,4,O,4),Vec.linComb(3,U,4,V,4,O,4));
            drawGlideLine(Vec.linComb(1,V,4,O,4),Vec.linComb(1,V,4,U,4,O,4));
            drawGlideLine(Vec.linComb(3,V,4,O,4),Vec.linComb(3,V,4,U,4,O,4));

            drawGlideLine(O,O.add(U).add(V));
            drawGlideLine(O.add(U),O.add(V));

            drawRotationPoint(O,4);
            drawRotationPoint(Vec.linComb(1,U,2,O,2),2);
            drawRotationPoint(Vec.linComb(1,V,2,O,2),2);
            drawRotationPoint(Vec.linComb(1,U,1,V,2,O,2),4);
        }

        
        public void paintDomainEdges(Vec U, Vec V, Vec O, int det) {
            super.paintDomainEdges(U, V, O, det);
            drawSimpleEdge(Vec.linComb(2, O, 1, V, 2), Vec.linComb(2, O, 2, U, -1, V, 2));
            drawSimpleEdge(Vec.linComb(2, O, -1, V, 2), Vec.linComb(2, O, 2, U, 1, V, 2));
        }


        
        public double approxArea() { return 0.125; }

    };

    public static TessRule squP4m = new SquRule("P4m",
            "A 90\u00ba rotation and a reflection passing through the\n"+
            "center of rotation.\n"+
            "Shows the full symmetry of the square with\n"+
            "three reflections and a 180\u00ba rotation."
    )
    {
        
        public void calcFund(FundamentalDomain fd)
        {
            int u1,u2,v1,v2; //,w1,w2;

            u1 =	frameV.x;
            u2 =	frameV.y;
            v1 = 	frameU.x;
            v2 = 	frameU.y;
            fd.fund[0].x = fd.cellVerts[1].x; fd.fund[0].y = fd.cellVerts[1].y;
            fd.fund[1].x = fd.cellVerts[1].x + u1/2; fd.fund[1].y = fd.cellVerts[1].y + u2/2;
            fd.fund[2].x = fd.cellVerts[1].x + u1/2+v1/2; fd.fund[2].y = fd.cellVerts[1].y + u2/2+v2/2;
            fd.numFund=3;
        }
        
        public void fun(int[] in,int[] out,int det)
        {
            int alpha = in[0] % det; if(alpha < 0) alpha = alpha + det;
            int beta = in[1] % det; if(beta < 0) beta = beta + det;
            int res[] = new int[2];
            calcRot4(alpha,beta,det,res);
            alpha = res[0]; beta = res[1];
            if(beta < alpha)
            {
                alpha = res[1];
                beta = res[0]; 
            }
            out[0] = alpha;
            out[1] = beta;
        }

        
        protected void paintSymetries(Vec U, Vec V, Vec O) {

            drawReflectionLine(O,U.add(O));
            drawReflectionLine(O,V.add(O));
            drawReflectionLine(Vec.linComb(1,U,2,O,2),Vec.linComb(1,U,2,V,2,O,2));
            drawReflectionLine(Vec.linComb(1,V,2,O,2),Vec.linComb(2,U,1,V,2,O,2));
            drawReflectionLine(O,Vec.linComb(1,U,1,V,1,O));
            drawReflectionLine(Vec.linComb(1,U,1,O),Vec.linComb(1,V,1,O));

            drawRotationPoint(O,4);
            drawRotationPoint(Vec.linComb(1,U,2,O,2),2);
            drawRotationPoint(Vec.linComb(1,V,2,O,2),2);
            drawRotationPoint(Vec.linComb(1,U,1,V,2,O,2),4);

            Vec P5 = Vec.linComb(2,U,-1,V,2).add(O);
            Vec P6 = V.div(2).add(O);

            drawGlideLine(P5,P6);

            Vec P7   = Vec.linComb(2, U, 3, V,2,O,2);
            drawGlideLine(P6,P7);


        }
        
        public void paintDomainEdges(Vec U, Vec V, Vec O, int det) {
            super.paintDomainEdges(U, V, O, det);
            drawSimpleEdge(O,  O.add(U).add(V));
            drawSimpleEdge(O.add(U),O.add(V));
        }
        
        public double approxArea() { return 0.125; }

    };

}
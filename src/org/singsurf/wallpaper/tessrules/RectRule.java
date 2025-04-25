/**
 * 
 */
package org.singsurf.wallpaper.tessrules;

import org.singsurf.wallpaper.FundamentalDomain;
import org.singsurf.wallpaper.Vec;

public abstract class RectRule extends TessRule
{
    static final boolean DEBUG = false;
    int det=1;
    double lenU = -1,lenV = -1.0;
    public RectRule(String name,String message) { super(name,message);	}
    //@Override
    public void calcFrame(FundamentalDomain fd,int selVert, boolean constrained)
    {
        if(constrained) {
            frameO.set(fd.cellVerts[1]);
            frameU.set(fd.cellVerts[2].sub(fd.cellVerts[1]));
            frameV.set(fd.cellVerts[0].sub(fd.cellVerts[1]));
            firstCall = false;
            det = -frameU.cross(frameV); //u1 * v2 - v1 * u2;

            return;
        }

        int u1,u2,v1,v2,w1,w2;
        //		System.out.println("calcFrame start "+numFund);
        //		for(int i=0;i<6;++i)
        //			System.out.println("i "+i+"["+verticies[i].x+","+verticies[i].y);

        u1 =	fd.cellVerts[0].x - fd.cellVerts[1].x;
        u2 =	fd.cellVerts[0].y - fd.cellVerts[1].y;
        v1 =	fd.cellVerts[2].x - fd.cellVerts[1].x;
        v2 =	fd.cellVerts[2].y - fd.cellVerts[1].y;
        frameO.x= fd.cellVerts[1].x;
        frameO.y= fd.cellVerts[1].y;
        det = u1 * v2 - u2 * v1;
        if(DEBUG) System.out.println("det "+det+" sel "+selVert+" first "+firstCall);
        if(selVert==2 || lenV < 0.0 || firstCall) 
            lenV = Math.sqrt((v1 * v1 + v2 * v2));

        if(selVert!=2 || lenU < 0.0 || firstCall)
            lenU = Math.sqrt((u1 * u1 + u2 * u2));

        if(selVert==2 && !firstCall) {
            w1 = -v2;
            w2 = v1;
            u1 = (int) Math.floor((w1)*lenU/lenV);
            u2 = (int) Math.floor((w2)*lenU/lenV);
            if(det>0) { u1 = - u1; u2 = -u2; }
        }
        else
        {
            w1 = u2;
            w2 = -u1;
            v1 = (int) Math.floor((w1)*lenV/lenU);
            v2 = (int) Math.floor((w2)*lenV/lenU);
            if(det>0) { v1 = - v1; v2 = -v2; }
        }
        det = u1 * v2 - u2 * v1;
        //		System.out.println("u "+u1+" "+u2+" v "+v1+" "+v2+" w "+w1+" "+w2+" det "+det+" len "+(w1 * w1 + w2 * w2)+" lenV "+(lenV*lenV)+" firstcall "+firstCall);
        frameU.x= v1;
        frameU.y= v2;
        frameV.x= u1;
        frameV.y= u2;

        //		System.out.println("calcFrame end "+numFund);
        //		for(int i=0;i<6;++i)
        //			System.out.println("i "+i+"["+verticies[i].x+","+verticies[i].y);
        //		for(int i=0;i<6;++i)
        //			System.out.println("i "+i+"["+frame[i]);
        firstCall = false;
    }

    public void constrainVertices(Vec[] verts, int selectedVertex) {
        Vec u;
        switch(selectedVertex) {
        case 0:
            u = verts[0].sub(verts[1]);
            u = u.constrainedVec(Math.PI/2);
            verts[0].set(verts[1].add(u));
            
            Vec w = verts[2].sub(verts[1]);
            w = w.constrainedVec(Math.PI/2);
            if(u.x==0) {
                if(w.x==0) {
                    w.x = w.y > 0 ? w.y : -w.y; // always +ve
                    w.y = 0;
                    w.x = u.y < 0 ? -w.x : w.x;
                    verts[2].set(verts[1].add(w));
                }
            } else if(u.y==0) {
                if(w.y==0) {
                    w.y = w.x>0 ? w.x : -w.x; // always +ve
                    w.x = 0;
                    w.y = u.x > 0 ? -w.y : w.y;
                    verts[2].set(verts[1].add(w));
                }
            }

            break;
        case 1:
            u = verts[0].sub(verts[1]);
            u = u.constrainedVec(Math.PI/2);
            if(u.x==0) {
                verts[0].x = verts[1].x;
                verts[2].y = verts[1].y;
            }
            else {
                verts[2].x = verts[1].x;
                verts[0].y = verts[1].y;
            }
            break;
        case 2:
            u = verts[2].sub(verts[1]);
            u = u.constrainedVec(Math.PI/2);
            verts[2].set(verts[1].add(u));
            
            Vec v = verts[0].sub(verts[1]);
            v = v.constrainedVec(Math.PI/2);
            if(u.x==0) {
                if(v.x==0) {
                    v.x = v.y > 0 ? v.y : -v.y; // always +ve
                    v.y = 0;
                    v.x = u.y > 0 ? -v.x : v.x;
                    verts[0].set(verts[1].add(v));
                }
            } else if(u.y==0) {
                if(v.y==0) {
                    v.y = v.x>0 ? v.x : -v.x; // always +ve
                    v.x = 0;
                    v.y = u.x < 0 ? -v.y : v.y;
                    verts[0].set(verts[1].add(v));
                }
            }
 
            break;
        default:
            System.out.println("Illegal seletion point");
            return;
        }
    }

    //@Override
    public void fixVerticies(FundamentalDomain fd)
    {
        fd.cellVerts[0].x = frameO.x+frameV.x;
        fd.cellVerts[0].y = frameO.y+frameV.y;
        fd.cellVerts[1].x = frameO.x;
        fd.cellVerts[1].y = frameO.y;
        fd.cellVerts[2].x = frameO.x+frameU.x;
        fd.cellVerts[2].y = frameO.y+frameU.y;
        fd.cellVerts[3].x = frameO.x+frameU.x+frameV.x;
        fd.cellVerts[3].y = frameO.y+frameU.y+frameV.y;
        fd.numSelPoints = 3;
        fd.numOuterPoints = 4;
        fd.setLatticeType(FundamentalDomain.PARALLOGRAM);
    }

    //@Override
    public void paintDomainEdges(Vec U, Vec V, Vec O,int det) {
        Vec A = O.add(U);
        Vec B = Vec.linComb(2, O, 1,U,2);
        Vec C = Vec.linComb(2, O, 1, U,2,V,2);
        Vec D = O.add(V);
        this.drawSimpleEdge(O,A);
        this.drawSimpleEdge(B,C);
        this.drawSimpleEdge(O,D);
    }

    /** Glide reflection, rectangular domain. 
     * If(b>.5) { a = 1-a; b = b-.5; } */

    static public TessRule rectPG = new RectRule("PG",
            "A Glide-reflection.\n"
            +"This transformation is performed by first translating the domain\n"
            +"and then reflecting it in the line of the translation.\n") {
        //@Override
        public void calcFund(FundamentalDomain fd)
        {
            int u1,u2,v1,v2; //,w1,w2;

            u1 =        frameV.x;
            u2 =        frameV.y;
            v1 =        frameU.x;
            v2 =        frameU.y;
            
            if(fd.det>0) {
                fd.fund[0].x = frameO.x;            fd.fund[0].y = frameO.y;
                fd.fund[1].x = frameO.x + u1/2;     fd.fund[1].y = frameO.y+u2/2;
                fd.fund[2].x = frameO.x + u1/2+v1;  fd.fund[2].y = frameO.y+u2/2+v2;
                fd.fund[3].x = frameO.x + v1;       fd.fund[3].y = frameO.y+v2;
                fd.numFund=4;
            }
            else
            {
                fd.fund[0].x = frameO.x;            fd.fund[0].y = frameO.y;
                fd.fund[1].x = frameO.x + v1/2;     fd.fund[1].y = frameO.y+v2/2;
                fd.fund[2].x = frameO.x + v1/2+u1;  fd.fund[2].y = frameO.y+v2/2+u2;
                fd.fund[3].x = frameO.x + u1;       fd.fund[3].y = frameO.y+u2;
                fd.numFund=4;
            }

        }

        //@Override
        public void fun(int[] in,int[] out,int det)
        {
            int alpha = in[0] % det; if(alpha < 0) alpha = alpha + det;
            int beta = in[1] % det; if(beta < 0) beta = beta + det;
            if(2 * alpha > det ) //(b)%2 != 0)
            {
                alpha -= det/2;
                beta = det - beta;
            }
            out[0] = alpha;
            out[1] = beta;
        }

        //@Override
        protected void paintSymetries(Vec U, Vec V, Vec O) {
            drawGlideLine(O,U.add(O));
            drawGlideLine(Vec.linComb(1,V,2,O,2),
                    Vec.linComb(2,U,1,V,2,O,2));
        }

        //@Override
        public double approxArea() { return 0.25; }
        //@Override
        public double approxAspect() { return 2.0; }

    };

    /** Two parallel mirrors. **/
    public static TessRule rectPM = new RectRule("PM",
            "A Reflection along one of the translation directions.\n"
            +"The other translation is at right angles giving a rectangular " 
            +"fundamental domain."
    )	{
        //@Override
        public void calcFund(FundamentalDomain fd)
        {
            int u1,u2,v1,v2; //,w1,w2;

            u1 =	frameV.x;
            u2 =	frameV.y;
            v1 = 	frameU.x;
            v2 = 	frameU.y;
            if(fd.det>0) {
                fd.fund[0].x = frameO.x;            fd.fund[0].y = frameO.y;
                fd.fund[1].x = frameO.x + u1/2;     fd.fund[1].y = frameO.y+u2/2;
                fd.fund[2].x = frameO.x + u1/2+v1;  fd.fund[2].y = frameO.y+u2/2+v2;
                fd.fund[3].x = frameO.x + v1;       fd.fund[3].y = frameO.y+v2;
                fd.numFund=4;
            }
            else
            {
                fd.fund[0].x = frameO.x;            fd.fund[0].y = frameO.y;
                fd.fund[1].x = frameO.x + v1/2;     fd.fund[1].y = frameO.y+v2/2;
                fd.fund[2].x = frameO.x + v1/2+u1;  fd.fund[2].y = frameO.y+v2/2+u2;
                fd.fund[3].x = frameO.x + u1;       fd.fund[3].y = frameO.y+u2;
                fd.numFund=4;
            }
        }

        //@Override
        public void fun(int[] in,int[] out,int det)
        {
            int alpha = in[0] % det; if(alpha < 0) alpha = alpha + det;
            int beta = in[1] % det; if(beta < 0) beta = beta + det;

            if(2 * alpha > det )
            {
                alpha = det - alpha;
            }
            out[0] = alpha;
            out[1] = beta;
        }

        //@Override
        protected void paintSymetries(Vec U, Vec V, Vec O) {
            drawReflectionLine(O,O.add(V));
            drawReflectionLine(Vec.linComb(1,U,2,O,2),
                    Vec.linComb(1,U,2,V,2,O,2));
        }

        //@Override
        public double approxArea() { return 0.25; }
        //@Override
        public double approxAspect() { return 2.0; }

    };


    public static TessRule rectPGG = new RectRule("PGG",
            "Two glide reflections at right angles.\n"
            +"This pattern also shows a 180\u00ba rotation."
            +"A rather unsatisfactory pattern visually")
    {
        //@Override
        public void calcFund(FundamentalDomain fd)
        {
            int u1,u2,v1,v2; //,w1,w2;

            u1 =	frameV.x;
            u2 =	frameV.y;
            v1 = 	frameU.x;
            v2 = 	frameU.y;
            fd.fund[0].x = frameO.x; 				fd.fund[0].y = frameO.y;
            fd.fund[1].x = frameO.x + v1/2; 		fd.fund[1].y = frameO.y+v2/2;
            fd.fund[2].x = frameO.x + v1/2+u1/2; 	fd.fund[2].y = frameO.y+v2/2+u2/2;
            fd.fund[3].x = frameO.x + u1/2;	 		fd.fund[3].y = frameO.y+u2/2;
            fd.numFund = 4;
        }

        //@Override
        public void fun(int[] in,int[] out,int det)
        {
            int alpha = in[0] % det; if(alpha < 0) alpha = alpha + det;
            int beta = in[1] % det; if(beta < 0) beta = beta + det;
            if(2*alpha > det)
            {
                if(2*beta>det)
                {
                    alpha = det - alpha;
                    beta = det - beta;
                }
                else
                {
                    alpha -= det/2;
                    beta = det/2 - beta;
                }
            }
            else
            {
                if(2*beta>det)
                {
                    alpha = det/2 - alpha;
                    beta -= det/2;
                }
            }
            out[0] = alpha;
            out[1] = beta;
        }

        //@Override
        protected void paintSymetries(Vec U, Vec V, Vec O) {
            drawGlideLine(
                    Vec.linComb(1,U,4,O,4),
                    Vec.linComb(1,U,4,V,4,O,4));
            drawGlideLine(
                    Vec.linComb(3,U,4,O,4),
                    Vec.linComb(3,U,4,V,4,O,4));

            drawGlideLine(
                    Vec.linComb(1,V,4,O,4),
                    Vec.linComb(1,V,4,U,4,O,4));
            drawGlideLine(
                    Vec.linComb(3,V,4,O,4),
                    Vec.linComb(3,V,4,U,4,O,4));
            drawRotationPoint(O,2);
            drawRotationPoint(Vec.linComb(1,U,2,O,2),2);
            drawRotationPoint(Vec.linComb(1,V,2,O,2),2);
            drawRotationPoint(Vec.linComb(1,U,1,V,2,O,2),2);
        }

        //@Override
        public void paintDomainEdges(Vec U, Vec V, Vec O, int det) {
            super.paintDomainEdges(U, V, O, det);
            Vec A = Vec.linComb(2, O, 1,V,2);
            Vec B = Vec.linComb(2, O, 2, U,1,V,2);
            this.drawSimpleEdge(A,B);

        }

        //@Override
        public double approxArea() { return 0.125; }

    };

    public static TessRule rectPMG = new RectRule("PMG",
            "A Glide reflection and a reflection."
            +"This pattern also shows a 180\u00ba rotation."
    )
    {
        //@Override
        public void calcFund(FundamentalDomain fd)
        {
            int u1,u2,v1,v2; //,w1,w2;

            u1 =	frameV.x;
            u2 =	frameV.y;
            v1 = 	frameU.x;
            v2 = 	frameU.y;
            fd.fund[0].x = frameO.x; 			fd.fund[0].y = frameO.y;
            fd.fund[1].x = frameO.x + v1/2;	 	fd.fund[1].y = frameO.y+v2/2;
            fd.fund[2].x = frameO.x + v1/2+u1/2; fd.fund[2].y = frameO.y+v2/2+u2/2;
            fd.fund[3].x = frameO.x + u1/2;	 	fd.fund[3].y = frameO.y+u2/2;
            fd.numFund = 4;
        }

        //@Override
        public void fun(int[] in,int[] out,int det)
        {
            int alpha = in[0] % det; if(alpha < 0) alpha = alpha + det;
            int beta = in[1] % det; if(beta < 0) beta = beta + det;
            if( 2 * alpha > det )
            {
                alpha = det - alpha;
            }
            if(2 * beta > det)
            {
                beta = det - beta;
                alpha = det/2 - alpha;
            }
            out[0] = alpha;
            out[1] = beta;
        }

        //@Override
        protected void paintSymetries(Vec U, Vec V, Vec O) {
            drawReflectionLine(O,O.add(V));
            drawReflectionLine(Vec.linComb(1,U,2,O,2),
                    Vec.linComb(1,U,2,V,2,O,2));
            drawGlideLine(O,Vec.linComb(1,U,1,O));
            drawGlideLine(Vec.linComb(1,V,2,O,2),Vec.linComb(1,V,2,U,2,O,2));
            drawRotationPoint(Vec.linComb(1,U,4,O,4),2);
            drawRotationPoint(Vec.linComb(3,U,4,O,4),2);
            drawRotationPoint(Vec.linComb(1,U,2,V,4,O,4),2);
            drawRotationPoint(Vec.linComb(3,U,2,V,4,O,4),2);
        }

        //@Override
        public void paintDomainEdges(Vec U, Vec V, Vec O, int det) {
            super.paintDomainEdges(U, V, O, det);
            Vec A = Vec.linComb(2, O, 1,V,2);
            Vec B = Vec.linComb(2, O, 2, U,1,V,2);
            this.drawSimpleEdge(A,B);

        }


        //@Override
        public double approxArea() { return 0.25; }

    };

    public static TessRule rectPMM = new RectRule("PMM",
            "Two reflections at right angles"
            +"This pattern also shows a 180\u00ba rotation."
    )
    {
        //@Override
        public void calcFund(FundamentalDomain fd)
        {
            int u1,u2,v1,v2; //,w1,w2;

            u1 =	frameV.x;
            u2 =	frameV.y;
            v1 = 	frameU.x;
            v2 = 	frameU.y;
            fd.fund[0].x = frameO.x; 				fd.fund[0].y = frameO.y;
            fd.fund[1].x = frameO.x + v1/2; 		fd.fund[1].y = frameO.y+v2/2;
            fd.fund[2].x = frameO.x + v1/2+u1/2; 	fd.fund[2].y = frameO.y+v2/2+u2/2;
            fd.fund[3].x = frameO.x + u1/2;	 		fd.fund[3].y = frameO.y+u2/2;
            fd.numFund = 4;
        }

        //@Override
        public void fun(int[] in,int[] out,int det)
        {
            int alpha = in[0] % det; if(alpha < 0) alpha = alpha + det;
            int beta = in[1] % det; if(beta < 0) beta = beta + det;
            if( 2 * alpha > det )
            {
                alpha = det - alpha;
            }
            if(2 * beta > det)
            {
                beta = det - beta;
            }
            out[0] = alpha;
            out[1] = beta;
        }

        //@Override
        protected void paintSymetries(Vec U, Vec V, Vec O) {
            drawReflectionLine(O,U.add(O));
            drawReflectionLine(O,V.add(O));
            drawReflectionLine(Vec.linComb(1,V,2,O,2),
                    Vec.linComb(2,U,1,V,2,O,2));
            drawReflectionLine(Vec.linComb(1,U,2,O,2),
                    Vec.linComb(1,U,2,V,2,O,2));
            drawRotationPoint(O,2);
            drawRotationPoint(Vec.linComb(1,U,2,O,2),2);
            drawRotationPoint(Vec.linComb(1,V,2,O,2),2);
            drawRotationPoint(Vec.linComb(1,U,1,V,2,O,2),2);
        }

        //@Override
        public void paintDomainEdges(Vec U, Vec V, Vec O, int det) {
            super.paintDomainEdges(U, V, O, det);
            Vec A = Vec.linComb(2, O, 1,V,2);
            Vec B = Vec.linComb(2, O, 2, U,1,V,2);
            this.drawSimpleEdge(A,B);

        }


        //@Override
        public double approxArea() { return 0.25; }

    };

}
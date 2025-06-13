package org.singsurf.wallpaper.tessrules;

import java.awt.Polygon;

import org.singsurf.wallpaper.DrawableRegion;
import org.singsurf.wallpaper.FundamentalDomain;
import org.singsurf.wallpaper.Messages;
import org.singsurf.wallpaper.Vec;
import org.singsurf.wallpaper.Wallpaper;


/** super class of transformations based on hexagons. */
public abstract class HexiRule extends TessRule
{

    private static final double ROOT_THREE = Math.sqrt(3.0);
    Vec frameW = new Vec(0,0);

    public HexiRule(String s,String m) {super(s,m);}

    @Override
    public void calcFrame(FundamentalDomain fd,int selectedVertex, boolean constrained)
    {
        int u1,u2,v1,v2; //,w1,w2;
        double d1,d2;
        u1 =	fd.cellVerts[0].x - fd.cellVerts[1].x;
        u2 =	fd.cellVerts[0].y - fd.cellVerts[1].y;
        d1 = ROOT_THREE * u2 - u1;
        d2 = -ROOT_THREE * u1 - u2;
        v1 = (int) Math.round(d1 / 2.0);
        v2 = (int) Math.round(d2 / 2.0);
        //System.out.println("calcFrame ("+u1+","+u2+") ("+v1+","+v2+") ("+(d1/2)+","+(d2/2)+")");
        frameO.set(fd.cellVerts[1]);
        frameU.x= v1;
        frameU.y= v2;
        frameV.x=u1;
        frameV.y=u2;
        frameW.setLC(-1,frameU,-1,frameV);
    }
    
    boolean checkTileable(Vec u) {
        if (u.x == 0) {
            return (u.y % 2 == 0);
        } else if (u.y == 0) {
            return (u.x % 2 == 0);
        }
        double d1 = ROOT_THREE * u.y - u.x;
        double d2 = -ROOT_THREE * u.x - u.y;
        Vec v = new Vec((int) Math.round(d1 / 2.0),(int) Math.round(d2 / 2.0));
        if (v.x == 0) {
            return (v.y % 2 == 0);
        } else if (v.y == 0) {
            return (v.x % 2 == 0);
        }
        
        Vec w = u.add(v);
        if (w.x == 0) {
            return (w.y % 2 == 0);
        } else if (w.y == 0) {
            return (w.x % 2 == 0);
        }
        return false;
    }

    static final int[][] off = new int[][]{{0,0},{1,0},{0,1},{-1,0},{0,-1},{1,1},{-1,1},{-1,-1},{1,-1}};
    public void constrainVertices(Vec[] verts, int selectedVertex) {
        Vec u;
        switch(selectedVertex) {
        case 0:
            u = verts[0].sub(verts[1]);
            break;
        case 1:
            u = verts[1].sub(verts[0]);
            break;
        default:
            System.out.println(Messages.getString("Rule.Msg.Only_red_or_green")); //$NON-NLS-1$
            return;
        }

        
        u = u.constrainedVec(Math.PI/6);
        Vec u2=new Vec(0,0);
        for(int i=0;i<off.length;++i) {
            u2.setLocation(u.x+off[i][0],u.y+off[i][1]);
            if(checkTileable(u2))
                break;
        }
        switch(selectedVertex) {
        case 0:
            verts[0].set(verts[1].add(u2));
            break;
        case 1:
            verts[1].set(verts[0].add(u2));
            break;
        default:
            System.out.println(Messages.getString("Rule.Msg.Only_red_or_green")); //$NON-NLS-1$
            return;
        }

    }


    @Override
    public void fixVerticies(FundamentalDomain fd)
    {
        fd.cellVerts[2].set(frameO.add(frameU));
        fd.cellVerts[3].setLC(1, frameO, 2, frameU, 1, frameV);
        fd.cellVerts[4].setLC(1, frameO, 2, frameU, 2, frameV);
        fd.cellVerts[5].setLC(1, frameO, 1, frameU, 2, frameV);

        fd.numOuterPoints = 6;
        fd.numSelPoints = 2;
        fd.setLatticeType(FundamentalDomain.HEXAGON);

        if(DEBUG) for(int i=0;i<6;++i)
            System.out.println("polygon ("+fd.cellVerts[i].x+","+fd.cellVerts[i].y+")"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
    }


    @Override
    public void fixFlip(String code, FundamentalDomain fd) {
        if((code == Wallpaper.FLIP_X || code == Wallpaper.FLIP_Y)) {
            calcFrame(fd,0, true);
            int x = fd.cellVerts[0].x;
            fd.cellVerts[0].x = fd.cellVerts[1].x; 
            fd.cellVerts[1].x = x;
            int y = fd.cellVerts[0].y;	
            fd.cellVerts[0].y = fd.cellVerts[1].y; 
            fd.cellVerts[1].y = y;
            calcFrame(fd,0, true);
        }
    }

    /** Calculates 120 degree rotation around (.5,.5). **/
    public void calcRot3(int a,int b,int alpha,int beta,int det,int res[])
    {
    	int index = (a+b)%3;
    	index = index>=0? index : index+3;

        switch( index )
        {			
        case 0:
            res[0] = alpha;
            res[1] = beta;
            break;
        case 1:
            if( alpha < beta )
            {
                res[0] = beta - alpha;
                res[1] = det - alpha;
            }
            else
            {
                res[0] = det - beta;
                res[1] = alpha - beta;
            }
            break;
        case 2:
            if( alpha < beta  )
            {
                res[0] = det - beta;
                res[1] = alpha - beta + det;
            }
            else
            {
                res[0] =  beta - alpha + det;
                res[1] = det - alpha;
            }
            break;
        }
    }

    @Override
	public void paintTileEdges(Vec U, Vec V, Vec p2, FundamentalDomain fd) {
		fd.drawLatticeLine(p2, p2.add(Vec.linComb(-1, U, -1, V, 3)));
		fd.drawLatticeLine(p2, p2.add(Vec.linComb(2, U, -1, V, 3)));
		fd.drawLatticeLine(p2, p2.add(Vec.linComb(-1, U, 2, V, 3)));
	}



    public static TessRule triP3h = new HexiRule(Messages.getString("Rule.P3h"),Messages.getString("Rule.P3h.descript")){ //$NON-NLS-1$ //$NON-NLS-2$
    	@Override
        public void calcFund(FundamentalDomain fd)
        {
            fd.fund[0].set(fd.cellVerts[1]); 
            fd.fund[1].setLC(3, fd.cellVerts[1], 1, frameU, 2, frameV, 3); 
            fd.fund[2].setLC(1, fd.cellVerts[1], 1, frameU, 1, frameV); 
            fd.fund[3].setLC(3, fd.cellVerts[1], 4, frameU, 2, frameV, 3); 
            fd.fund[4].setLC(1, fd.cellVerts[1], 1, frameU, 1); 
            fd.fund[5].setLC(3, fd.cellVerts[1], 1, frameU, -1, frameV, 3); 
            fd.numFund=6;
        }

        @Override
        public void fun(int[] in,int[] out,int det)
        {
            int a = (in[0]<0 ? (in[0]+1)/det -1 : in[0]/det); 
            int b = (in[1]<0 ? (in[1]+1)/det -1 : in[1]/det); 
            int alpha = in[0] % det; if(alpha < 0) alpha = alpha + det;
            int beta = in[1] % det; if(beta < 0) beta = beta + det;
            calcRot3(a,b,alpha,beta,det,out);
            if( -out[0] + 2*out[1] > det && out[0] + out[1] > det) {
                int c =  2* det - out[1] ;
                int d =   det - out[1] + out[0];
            	out[0]=c; out[1]= d;
            }
            if( 2* out[0] < out[1]  && out[0] + out[1] < det) {
                int c =  out[1] - out[0];
                int d =  - out[0];
            	out[0]=c; out[1]= d;
            }
        }

        @Override
        protected void paintSymetries(Vec U, Vec V, Vec O) {
            drawRotationPoint(O,3);
            drawRotationPoint(frameU.add(O),3);
            drawRotationPoint(Vec.linComb(2,frameU,1,O),3);
        }

        @Override
        public void paintDomainEdges(Vec U, Vec V,
                Vec O, int det) {
            drawSimpleEdge(O,O.add(U.div(3)));
            drawSimpleEdge(O,Vec.linComb(3, O, -1,U,  1,V,3));
            drawSimpleEdge(Vec.linComb(3, O, 2,U, -1,V,3),O.add(U.div(3)));
            drawSimpleEdge(
            		Vec.linComb(3, O, 2,U, -1,V,3),
            		Vec.linComb(3, O, 3,U, -1,V,3));
            drawSimpleEdge(
            		Vec.linComb(3, O, 3,U, -1,V,3),
            		Vec.linComb(3, O, 3,U,  0,V,3));
            drawSimpleEdge(
            		Vec.linComb(3, O, 1,U,  0,V,3),
            		Vec.linComb(3, O, 1,U,  1,V,3));
            drawSimpleEdge(
            		Vec.linComb(3, O, -1,U, 1,V,3),
            		Vec.linComb(3, O, -1,U, 2,V,3));
            drawSimpleEdge(
            		Vec.linComb(3, O, 2,U,  1,V,3),
            		Vec.linComb(3, O, 1,U,  1,V,3));
            drawSimpleEdge(
            		Vec.linComb(3, O, 0,U,  2,V,3),
            		Vec.linComb(3, O, 1,U,  1,V,3));
        }

        
        @Override
		public void paintTileEdges(Vec U, Vec V, Vec O, FundamentalDomain fd) {
        	paintFd = fd;
            fd.drawLatticeLine(O,O.add(U.div(3)));
            fd.drawLatticeLine(O,Vec.linComb(3, O, -1,U,  1,V,3));
            fd.drawLatticeLine(Vec.linComb(3, O, 2,U, -1,V,3),O.add(U.div(3)));
            fd.drawLatticeLine(
            		Vec.linComb(3, O, 2,U, -1,V,3),
            		Vec.linComb(3, O, 3,U, -1,V,3)); 
            fd.drawLatticeLine(
            		Vec.linComb(3, O, 3,U, -1,V,3),
            		Vec.linComb(3, O, 3,U,  0,V,3));
            fd.drawLatticeLine(
            		Vec.linComb(3, O, -1,U,  1,V,3),
            		Vec.linComb(3, O, -1,U,  2,V,3));

		}

		@Override
		public Polygon make_tile_polygon(FundamentalDomain fd) {
        	int npts = 12;
        	int x[]=new int[npts];
        	int y[]=new int[npts];
        	x[0] = frameO.x;
        	y[0] = frameO.y;
        	Vec A = Vec.linComb(3, frameO, 1, frameU, -1, frameV, 3);
        	x[1] = A.x; y[1] = A.y;
        	A = Vec.linComb(1, frameO, 1, frameU);
        	x[2] = A.x; y[2] = A.y;
        	A = Vec.linComb(3, frameO, 4, frameU, 2, frameV, 3);
        	x[3] = A.x; y[3] = A.y;
        	A = Vec.linComb(1, frameO, 2, frameU, 1, frameV);
        	x[4] = A.x; y[4] = A.y;
        	A = Vec.linComb(3, frameO, 7, frameU, 5, frameV, 3);
        	x[5] = A.x; y[5] = A.y;
        	A = Vec.linComb(1, frameO, 2, frameU, 2, frameV);
        	x[6] = A.x; y[6] = A.y;
        	A = Vec.linComb(3, frameO, 4, frameU, 5, frameV, 3);
        	x[7] = A.x; y[7] = A.y;
        	A = Vec.linComb(1, frameO, 1, frameU, 2, frameV);
        	x[8] = A.x; y[8] = A.y;
        	A = Vec.linComb(3, frameO, 1, frameU, 5, frameV, 3);
        	x[9] = A.x; y[9] = A.y;
        	A = Vec.linComb(1, frameO, 1, frameV);
        	x[10] = A.x; y[10] = A.y;
        	A = Vec.linComb(3, frameO, 1, frameU, 2, frameV, 3);
        	x[11] = A.x; y[11] = A.y;
        	return new Polygon(x,y,npts);
		}

		@Override
        public double approxArea() { return Math.sqrt(3)/4; }

    };

    public static TessRule triP3a = new HexiRule(Messages.getString("Rule.P3A"),Messages.getString("Rule.P3A.descript")){ //$NON-NLS-1$ //$NON-NLS-2$
        @Override
        public void calcFund(FundamentalDomain fd)
        {
            int u1,u2,v1,v2; //,w1,w2;

            u1 =	frameV.x;
            u2 =	frameV.y;
            v1 = 	frameU.x;
            v2 = 	frameU.y;
            fd.fund[0].x = fd.cellVerts[0].x; 
            fd.fund[0].y = fd.cellVerts[0].y;
            fd.fund[1].x = fd.cellVerts[1].x + v1+u1; 
            fd.fund[1].y = fd.cellVerts[1].y + v2+u2;
            fd.fund[2].x = fd.cellVerts[1].x; 
            fd.fund[2].y = fd.cellVerts[1].y;
            fd.fund[3].x = fd.cellVerts[1].x - v1; 
            fd.fund[3].y = fd.cellVerts[1].y - v2;
            fd.numFund=4;
        }

        @Override
        public void fun(int[] in,int[] out,int det)
        {
            int a = (in[0]<0 ? (in[0]+1)/det -1 : in[0]/det); 
            int b = (in[1]<0 ? (in[1]+1)/det -1 : in[1]/det); 
            /*			int a = (int) Math.floor((float) in[0]/det);
			int b = (int) Math.floor((float) in[1]/det);
             */			
            int alpha = in[0] % det; if(alpha < 0) alpha = alpha + det;
            int beta = in[1] % det; if(beta < 0) beta = beta + det;
            int[] res = new int[2];
            calcRot3(a,b,alpha,beta,det,res);
            alpha = res[0]; beta = res[1];
            
            if( beta > alpha )
            {
            }
            else
            {
                // Now rotate 120 degrees to form thin triangle
                int gamma = -beta;
                int delta  = alpha - beta;
                alpha = gamma;
                beta = delta;
            }
            out[0] = alpha; out[1] = beta;
        }

        @Override
        protected void paintSymetries(Vec U, Vec V, Vec O) {
            drawRotationPoint(O,3);
            drawRotationPoint(frameU.add(O),3);
            drawRotationPoint(Vec.linComb(2,frameU,1,O),3);
        }

        @Override
        public void paintDomainEdges(Vec U, Vec V,
                Vec O, int det) {
            Vec A = Vec.linComb(3, O, -1, U,-1,V,3);
            Vec B = Vec.linComb(3, O, 2, U,-1,V,3);
            Vec C = Vec.linComb(3, O, -1, U,2,V,3);
            Vec M = Vec.linComb(3, O, 1, U,1,V,3);
            drawSimpleEdge(O,A);
            drawSimpleEdge(O,B);
            drawSimpleEdge(O,C);
            drawSimpleEdge(O.add(U),M);
            drawSimpleEdge(O.add(V),M);
            drawSimpleEdge(O,M);
        }

        @Override
        public double approxArea() { return Math.sqrt(3)/4; }

    };

    public static TessRule triP3 = new HexiRule(Messages.getString("Rule.P3"),Messages.getString("Rule.P3.descript")){ //$NON-NLS-1$ //$NON-NLS-2$
        @Override
        public void calcFund(FundamentalDomain fd)
        {
            int u1,u2,v1,v2; //,w1,w2;

            u1 =	frameV.x;
            u2 =	frameV.y;
            v1 = 	frameU.x;
            v2 = 	frameU.y;
            fd.fund[0].x = fd.cellVerts[0].x; 
            fd.fund[0].y = fd.cellVerts[0].y;
            fd.fund[1].x = fd.cellVerts[1].x; 
            fd.fund[1].y = fd.cellVerts[1].y;
            fd.fund[2].x = fd.cellVerts[1].x + v1; 
            fd.fund[2].y = fd.cellVerts[1].y + v2;
            fd.fund[3].x = fd.cellVerts[1].x + u1+v1; 
            fd.fund[3].y = fd.cellVerts[1].y + u2+v2;
            fd.numFund=4;
        }

        @Override
        public void fun(int[] in,int[] out,int det)
        {
            int a = (in[0]<0 ? (in[0]+1)/det -1 : in[0]/det); 
            int b = (in[1]<0 ? (in[1]+1)/det -1 : in[1]/det); 
            int alpha = in[0] % det; if(alpha < 0) alpha = alpha + det;
            int beta = in[1] % det; if(beta < 0) beta = beta + det;
            calcRot3(a,b,alpha,beta,det,out);
        }

        @Override
        protected void paintSymetries(Vec U, Vec V, Vec O) {
            drawRotationPoint(O,3);
            drawRotationPoint(frameU.add(O),3);
            drawRotationPoint(Vec.linComb(2,frameU,1,O),3);
        }

        @Override
        public void paintDomainEdges(Vec U, Vec V,
                Vec O, int det) {
            Vec A = Vec.linComb(3, O, -1, U,-1,V,3);
            Vec B = Vec.linComb(3, O, 2, U,-1,V,3);
            Vec C = Vec.linComb(3, O, -1, U,2,V,3);
            Vec M = Vec.linComb(3, O, 1, U,1,V,3);
            drawSimpleEdge(O,A);
            drawSimpleEdge(O,B);
            drawSimpleEdge(O,C);
            drawSimpleEdge(O.add(U),M);
            drawSimpleEdge(O.add(V),M);
            drawSimpleEdge(O,M);
            
            
            
        }

        @Override
        public double approxArea() { return Math.sqrt(3)/4; }

    };

    public static TessRule triP3b = new HexiRule(Messages.getString("Rule.P3A"),Messages.getString("Rule.P3A.descript")){ //$NON-NLS-1$ //$NON-NLS-2$
        @Override
        public void calcFund(FundamentalDomain fd)
        {
            int u1,u2,v1,v2; //,w1,w2;

            u1 =	frameV.x;
            u2 =	frameV.y;
            v1 = 	frameU.x;
            v2 = 	frameU.y;
            fd.fund[0].x = fd.cellVerts[0].x; 
            fd.fund[0].y = fd.cellVerts[0].y;
            fd.fund[1].x = fd.cellVerts[1].x + v1+u1; 
            fd.fund[1].y = fd.cellVerts[1].y + v2+u2;
            fd.fund[2].x = fd.cellVerts[1].x; 
            fd.fund[2].y = fd.cellVerts[1].y;
            fd.fund[3].x = fd.cellVerts[1].x - v1; 
            fd.fund[3].y = fd.cellVerts[1].y - v2;
            fd.numFund=4;
        }

        @Override
        public void fun(int[] in,int[] out,int det)
        {
            int a = (in[0]<0 ? (in[0]+1)/det -1 : in[0]/det); 
            int b = (in[1]<0 ? (in[1]+1)/det -1 : in[1]/det); 
            /*			int a = (int) Math.floor((float) in[0]/det);
			int b = (int) Math.floor((float) in[1]/det);
             */			
            int alpha = in[0] % det; if(alpha < 0) alpha = alpha + det;
            int beta = in[1] % det; if(beta < 0) beta = beta + det;
            int[] res = new int[2];
            calcRot3(a,b,alpha,beta,det,res);
            alpha = res[0]; beta = res[1];
            
            if( beta > alpha )
            {
            }
            else
            {
                // Now rotate 120 degrees to form thin triangle
                int gamma = -beta;
                int delta  = alpha - beta;
                alpha = gamma;
                beta = delta;
            }
            out[0] = alpha; out[1] = beta;
        }

        @Override
        protected void paintSymetries(Vec U, Vec V, Vec O) {
            drawRotationPoint(O,3);
            drawRotationPoint(frameU.add(O),3);
            drawRotationPoint(Vec.linComb(2,frameU,1,O),3);
        }

        @Override
        public void paintDomainEdges(Vec U, Vec V,
                Vec O, int det) {
            Vec A = Vec.linComb(3, O, -1, U,-1,V,3);
            Vec B = Vec.linComb(3, O, 2, U,-1,V,3);
            Vec C = Vec.linComb(3, O, -1, U,2,V,3);
            Vec M = Vec.linComb(3, O, 1, U,1,V,3);
            drawSimpleEdge(O,A);
            drawSimpleEdge(O,B);
            drawSimpleEdge(O,C);
            drawSimpleEdge(O.add(U),M);
            drawSimpleEdge(O.add(V),M);
            drawSimpleEdge(O,M);
        }

        @Override
        public double approxArea() { return Math.sqrt(3)/4; }

    };


    public static TessRule triP3m1 = new HexiRule(Messages.getString("Rule.P3M1"), //$NON-NLS-1$
    Messages.getString("Rule.P3M1.descript")) //$NON-NLS-1$
    {
        @Override
        public void calcFund(FundamentalDomain fd)
        {
            int u1,u2,v1,v2;

            u1 =	frameV.x;
            u2 =	frameV.y;
            v1 = 	frameU.x;
            v2 = 	frameU.y;
            fd.fund[0].x = fd.cellVerts[0].x; fd.fund[0].y = fd.cellVerts[0].y;
            fd.fund[1].x = fd.cellVerts[1].x; fd.fund[1].y = fd.cellVerts[1].y;
            fd.fund[2].x = fd.cellVerts[1].x + u1+v1; fd.fund[2].y = fd.cellVerts[1].y + u2+v2;
            fd.numFund=3;
        }

        @Override
        public void fun(int[] in,int[] out,int det)
        {
            /*			int a = (int) Math.floor((float) in[0]/det);
			int b = (int) Math.floor((float) in[1]/det);
             */			int a = (in[0]<0 ? (in[0]+1)/det -1 : in[0]/det); 
             int b = (in[1]<0 ? (in[1]+1)/det -1 : in[1]/det); 
             int alpha = in[0] % det; if(alpha < 0) alpha = alpha + det;
             int beta = in[1] % det; if(beta < 0) beta = beta + det;
             int res[] = new int[2];
             calcRot3(a,b,alpha,beta,det,res);
             alpha = res[0]; beta = res[1];
             // reflect top left onto bottom right

             if( beta < alpha )
             {
                 int gamma = alpha;
                 alpha = beta;
                 beta = gamma;
             }
             out[0] = alpha;
             out[1] = beta;
        }

        @Override
        protected void paintSymetries(Vec U, Vec V, Vec O) {
            drawReflectionLine(O,Vec.linComb(3,frameU,1,O));
            drawReflectionLine(Vec.linComb(-1,frameV,1,O),Vec.linComb(3,frameU,2,frameV,1,O));
            drawReflectionLine(Vec.linComb(1,frameU,-1,frameV,1,O),Vec.linComb(1,frameU,2,frameV,1,O));

            drawRotationPoint(O,3);
            drawRotationPoint(frameU.add(O),3);
            drawRotationPoint(Vec.linComb(2,frameU,1,O),3);
            
            Vec a = Vec.linComb(6, O, 2, U, -1, V, 6);
            Vec b = Vec.linComb(6, O, 5, U, -1, V, 6);
            Vec c = Vec.linComb(6, O, 5, U, 2, V, 6);
            Vec d = Vec.linComb(6, O, 2, U, 5, V, 6);
            Vec e = Vec.linComb(6, O, -1, U, 5, V, 6);
            Vec f = Vec.linComb(6, O, -1, U, 2, V, 6);
            
//            drawGlideLine(a,b);
//            drawGlideLine(b,c);
//            drawGlideLine(c,d);
//            drawGlideLine(d,e);
//            drawGlideLine(e,f);
//            drawGlideLine(f,a);
            
            drawGlideLine(a,c);
            drawGlideLine(b,d);
            drawGlideLine(c,e);
            drawGlideLine(d,f);
            drawGlideLine(e,a);
            drawGlideLine(f,b);

       }

        @Override
        public void paintDomainEdges(Vec U, Vec V,
                Vec O, int det) {
            Vec A = Vec.linComb(3, O, -1, U,-1,V,3);
            Vec B = Vec.linComb(3, O, 2, U,-1,V,3);
            Vec C = Vec.linComb(3, O, -1, U,2,V,3);
            Vec D = Vec.linComb(3, O, 2, U,2,V,3);
            drawSimpleEdge(O,A);
            drawSimpleEdge(O,B);
            drawSimpleEdge(O,C);
            drawSimpleEdge(B,O.add(V));
            drawSimpleEdge(C,O.add(U));
            drawSimpleEdge(O,D);

        }

        @Override
        public double approxArea() { return Math.sqrt(3)/4; }

    };

    public static TessRule triP31m = new HexiRule(Messages.getString("Rule.P31M"), //$NON-NLS-1$
    Messages.getString("Rule.P31M.descript")) //$NON-NLS-1$
    {
        @Override
        public void calcFund(FundamentalDomain fd)
        {
            fd.fund[0].set(Vec.linComb(1,fd.cellVerts[1],1, frameU)); 
            fd.fund[1].set(Vec.linComb(1,fd.cellVerts[1],1, frameU,1, frameV)); 
//            fd.fund[1].set(fd.cellVerts[1]); 
            fd.fund[2].set(Vec.linComb(1,fd.cellVerts[1],1, frameV)); 
            fd.numFund =3;
        }

        @Override
		public void fun(int[] in, int[] out, int det) {
			int a = (in[0] < 0 ? (in[0] + 1) / det - 1 : in[0] / det);
			int b = (in[1] < 0 ? (in[1] + 1) / det - 1 : in[1] / det);
			int alpha = in[0] % det;
			if (alpha < 0)
				alpha = alpha + det;
			int beta = in[1] % det;
			if (beta < 0)
				beta = beta + det;
			int res[] = new int[2];
			calcRot3(a, b, alpha, beta, det, res);
			alpha = res[0];
			beta = res[1];

			// reflect in line alpha + beta = 1
			if (alpha + beta < det) {
				int gamma = alpha;
				alpha = det - beta;
				beta = det - gamma;
			}

			out[0] = alpha;
			out[1] = beta;
		}

        @Override
        protected void paintSymetries(Vec U, Vec V, Vec O) {
            drawReflectionLine(
                    Vec.linComb(1,O,-1,frameU,1,frameV),
                    O);
            drawReflectionLine(
                    Vec.linComb(1,O,2,frameU,1,frameV),
                    O);
            drawReflectionLine(
                    Vec.linComb(1,O,1,frameU,2,frameV),
                    O);

            drawRotationPoint(O,3);
            drawRotationPoint(frameU.add(O),3);
            drawRotationPoint(Vec.linComb(2,frameU,1,O),3);
           
            drawGlideLine(
            		Vec.linComb(2, O, 1, frameU,2,frameV,2),
            		Vec.linComb(2, O, -3, frameU,2));
 
            drawGlideLine(
            		Vec.linComb(2, O, 1,frameU,2,frameV,2),
            		Vec.linComb(2, O, -1, frameU, 4, frameV,2));
 
            drawGlideLine(
            		Vec.linComb(2, O, -2,frameU,-1,frameV,2),
            		Vec.linComb(2, O, 0, frameU, 3, frameV,2));            
            
        }

        @Override
        public void paintDomainEdges(Vec U, Vec V,
                Vec O, int det) {
            drawSimpleEdge(O, Vec.linComb(3, O, 1, U,1,V,3));
            drawSimpleEdge(O, Vec.linComb(3, O, -2, U, 1,V,3));
            drawSimpleEdge(O, Vec.linComb(3, O, 1, U,-2,V,3));
            drawSimpleEdge(O, O.add(V));
            drawSimpleEdge(O, O.add(U));
            drawSimpleEdge(O, Vec.linComb(1, O, -1, U, 1, V));

            drawSimpleEdge(O, Vec.linComb(3, O, -1, U,-1,V,3));
            drawSimpleEdge(O, Vec.linComb(3, O, 2, U,-1,V,3));
            drawSimpleEdge(O, Vec.linComb(3, O, -1, U,2,V,3));

        }

        @Override
        public double approxArea() { return Math.sqrt(3)/4; }

    };

    public static TessRule triP31mk = new HexiRule(Messages.getString("Rule.P31Mk"), //$NON-NLS-1$
    Messages.getString("Rule.P31Mk.descript")) //$NON-NLS-1$
    {
        @Override
        public void calcFund(FundamentalDomain fd)
        {
            int u1,u2,v1,v2; //,w1,w2;

            u1 =	frameV.x;
            u2 =	frameV.y;
            v1 = 	frameU.x;
            v2 = 	frameU.y;
            fd.fund[0].x = fd.cellVerts[1].x+u1/2; fd.fund[0].y = fd.cellVerts[1].y+u2/2;
            fd.fund[1].x = fd.cellVerts[1].x; fd.fund[1].y = fd.cellVerts[1].y;
            fd.fund[2].x = fd.cellVerts[1].x + v1/2; fd.fund[2].y = fd.cellVerts[1].y + v2/2;
            fd.fund[3].x = fd.cellVerts[1].x + u1+v1; fd.fund[3].y = fd.cellVerts[1].y + u2+v2;
            fd.numFund =4;
        }

        @Override
        public void fun(int[] in,int[] out,int det)
        {
            /*			int a = (int) Math.floor((float) in[0]/det);
			int b = (int) Math.floor((float) in[1]/det);
             */			
        	int a = (in[0]<0 ? (in[0]+1)/det -1 : in[0]/det); 
             int b = (in[1]<0 ? (in[1]+1)/det -1 : in[1]/det); 
             int alpha = in[0] % det; if(alpha < 0) alpha = alpha + det;
             int beta = in[1] % det; if(beta < 0) beta = beta + det;
             int res[] = new int[2];
             calcRot3(a,b,alpha,beta,det,res);
             alpha = res[0]; beta = res[1];
             // reflect bot right in line 2 alpha - beta - 1
             // reflect top left in  2 beta - alpha - 1

             if( beta > alpha )
             {
                 if( 2 * beta - alpha - det > 0 )
                 {
                     beta = alpha - beta +det;
                 }

             }
             else
             {
                 if( 2 * alpha - beta - det > 0 )
                 {
                     alpha = beta - alpha + det;
                 }
             }

             out[0] = alpha;
             out[1] = beta;
        }

        @Override
        protected void paintSymetries(Vec U, Vec V, Vec O) {
            Vec f56 = frameV.add(frameU);


            drawReflectionLine(
                    Vec.linComb(-1,frameV,2,O,2),
                    Vec.linComb(1,frameV,4,frameU,2,O,2));
            drawReflectionLine(
                    Vec.linComb(1,f56,2,O,2),
                    Vec.linComb(2,frameU,-2,frameV,1,f56,2).add(O));
            drawReflectionLine(
                    Vec.linComb(1,frameU,-2,frameV,2,O,2),
                    Vec.linComb(1,frameU,2,f56,2,O,2));

            drawRotationPoint(O,3);
            drawRotationPoint(frameU.add(O),3);
            drawRotationPoint(Vec.linComb(2,frameU,1,O),3);

            Vec a = Vec.linComb(6, O, 2, U, -1, V, 6);
            Vec b = Vec.linComb(6, O, 5, U, -1, V, 6);
            Vec c = Vec.linComb(6, O, 5, U, 2, V, 6);
            Vec d = Vec.linComb(6, O, 2, U, 5, V, 6);
            Vec e = Vec.linComb(6, O, -1, U, 5, V, 6);
            Vec f = Vec.linComb(6, O, -1, U, 2, V, 6);
            
            drawGlideLine(a,b);
            drawGlideLine(b,c);
            drawGlideLine(c,d);
            drawGlideLine(d,e);
            drawGlideLine(e,f);
            drawGlideLine(f,a);            
        }

        @Override
        public void paintDomainEdges(Vec U, Vec V,
                Vec O, int det) {
            Vec A = Vec.linComb(3, O, -1, U,-1,V,3);
            Vec B = Vec.linComb(3, O, 2, U,-1,V,3);
            Vec B2 = Vec.linComb(6, O, 2, U,-1,V,6);
            Vec C = Vec.linComb(3, O, -1, U,2,V,3);
            Vec C2 = Vec.linComb(6, O, -1, U,2,V,6);
            Vec D = Vec.linComb(6, O, 5, U,-1,V,6);
            Vec E = Vec.linComb(6, O, -1, U,5,V,6);

            drawSimpleEdge(O,A);
            drawSimpleEdge(O,B);
            drawSimpleEdge(O,C);
            drawSimpleEdge(B2, B2.add(V));
            drawSimpleEdge(C2, C2.add(U));
            drawSimpleEdge(D, E);


        }

        @Override
        public double approxArea() { return Math.sqrt(3)/4; }

    };

    public static TessRule triP31Mt = new HexiRule(Messages.getString("Rule.P31Mt"),Messages.getString("Rule.P31Mt.descript")){ //$NON-NLS-1$ //$NON-NLS-2$
    	@Override
        public void calcFund(FundamentalDomain fd)
        {
//            fd.fund[0].set(fd.cellVerts[1]); 
            fd.fund[0].setLC(3, fd.cellVerts[1], 1, frameU, 2, frameV, 3); 
            fd.fund[1].setLC(1, fd.cellVerts[1], 1, frameU, 1, frameV); 
            fd.fund[2].setLC(3, fd.cellVerts[1], 4, frameU, 2, frameV, 3); 
            fd.fund[3].setLC(1, fd.cellVerts[1], 1, frameU, 1); 
//            fd.fund[5].setLC(3, fd.cellVerts[1], 1, frameU, -1, frameV, 3); 
            fd.numFund=4;
        }

        @Override
        public void fun(int[] in,int[] out,int det)
        {
            int a = (in[0]<0 ? (in[0]+1)/det -1 : in[0]/det); 
            int b = (in[1]<0 ? (in[1]+1)/det -1 : in[1]/det); 
            int alpha = in[0] % det; if(alpha < 0) alpha = alpha + det;
            int beta = in[1] % det; if(beta < 0) beta = beta + det;
            calcRot3(a,b,alpha,beta,det,out);
            if( -out[0] + 2*out[1] > det && out[0] + out[1] > det) {
                int c =  2* det - out[1] ;
                int d =   det - out[1] + out[0];
            	out[0]=c; out[1]= d;
            }
            if( 2* out[0] < out[1]  && out[0] + out[1] < det) {
                int c =  out[1] - out[0];
                int d =  - out[0];
            	out[0]=c; out[1]= d;
            }
            // now reflect in line alpha + beta = 1
            if(out[0] + out[1] < det) {
				int c = out[0];
				out[0] = det - out[1];
				out[1] = det - c;
			}
        }

        @Override
        protected void paintSymetries(Vec U, Vec V, Vec O) {
            drawReflectionLine(
                    Vec.linComb(1,O,-1,frameU,1,frameV),
                    O);
            drawReflectionLine(
                    Vec.linComb(1,O,2,frameU,1,frameV),
                    O);
            drawReflectionLine(
                    Vec.linComb(1,O,1,frameU,2,frameV),
                    O);

            drawRotationPoint(O,3);
            drawRotationPoint(frameU.add(O),3);
            drawRotationPoint(Vec.linComb(2,frameU,1,O),3);
           
            drawGlideLine(
            		Vec.linComb(2, O, 1, frameU,2,frameV,2),
            		Vec.linComb(2, O, -3, frameU,2));
 
            drawGlideLine(
            		Vec.linComb(2, O, 1,frameU,2,frameV,2),
            		Vec.linComb(2, O, -1, frameU, 4, frameV,2));
 
            drawGlideLine(
            		Vec.linComb(2, O, -2,frameU,-1,frameV,2),
            		Vec.linComb(2, O, 0, frameU, 3, frameV,2));            
            
        }

        @Override
        public void paintDomainEdges(Vec U, Vec V,
                Vec O, int det) {
            drawSimpleEdge(O,O.add(U));
            drawSimpleEdge(O,O.add(V));
            drawSimpleEdge(O.add(U),O.add(V));
            drawSimpleEdge(O,Vec.linComb(3, O, -1,U,  1,V,3));
            drawSimpleEdge(Vec.linComb(3, O, 2,U, -1,V,3),O.add(U.div(3)));
            drawSimpleEdge(
            		Vec.linComb(3, O, 2,U, -1,V,3),
            		Vec.linComb(3, O, 3,U, -1,V,3));
            drawSimpleEdge(
            		Vec.linComb(3, O, 1,U,  0,V,3),
            		Vec.linComb(3, O, 1,U,  1,V,3));
            drawSimpleEdge(
            		Vec.linComb(3, O, -1,U, 1,V,3),
            		Vec.linComb(3, O, -1,U, 2,V,3));
            drawSimpleEdge(
            		Vec.linComb(3, O, 2,U,  1,V,3),
            		Vec.linComb(3, O, 1,U,  1,V,3));
            drawSimpleEdge(
            		Vec.linComb(3, O, 0,U,  2,V,3),
            		Vec.linComb(3, O, 1,U,  1,V,3));
        }

        
        @Override
		public void paintTileEdges(Vec U, Vec V, Vec O, FundamentalDomain fd) {
        	paintFd = fd;
            fd.drawLatticeLine(O,O.add(U.div(3)));
            fd.drawLatticeLine(O,Vec.linComb(3, O, -1,U,  1,V,3));
            fd.drawLatticeLine(Vec.linComb(3, O, 2,U, -1,V,3),O.add(U.div(3)));
            fd.drawLatticeLine(
            		Vec.linComb(3, O, 2,U, -1,V,3),
            		Vec.linComb(3, O, 3,U, -1,V,3));
            fd.drawLatticeLine(
            		Vec.linComb(3, O, 3,U, -1,V,3),
            		Vec.linComb(3, O, 3,U,  0,V,3));
            fd.drawLatticeLine(
            		Vec.linComb(3, O, -1,U,  1,V,3),
            		Vec.linComb(3, O, -1,U,  2,V,3));

		}

		@Override
		public Polygon make_tile_polygon(FundamentalDomain fd) {
        	int npts = 12;
        	int x[]=new int[npts];
        	int y[]=new int[npts];
        	x[0] = frameO.x;
        	y[0] = frameO.y;
        	Vec A = Vec.linComb(3, frameO, 1, frameU, -1, frameV, 3);
        	x[1] = A.x; y[1] = A.y;
        	A = Vec.linComb(1, frameO, 1, frameU);
        	x[2] = A.x; y[2] = A.y;
        	A = Vec.linComb(3, frameO, 4, frameU, 2, frameV, 3);
        	x[3] = A.x; y[3] = A.y;
        	A = Vec.linComb(1, frameO, 2, frameU, 1, frameV);
        	x[4] = A.x; y[4] = A.y;
        	A = Vec.linComb(3, frameO, 7, frameU, 5, frameV, 3);
        	x[5] = A.x; y[5] = A.y;
        	A = Vec.linComb(1, frameO, 2, frameU, 2, frameV);
        	x[6] = A.x; y[6] = A.y;
        	A = Vec.linComb(3, frameO, 4, frameU, 5, frameV, 3);
        	x[7] = A.x; y[7] = A.y;
        	A = Vec.linComb(1, frameO, 1, frameU, 2, frameV);
        	x[8] = A.x; y[8] = A.y;
        	A = Vec.linComb(3, frameO, 1, frameU, 5, frameV, 3);
        	x[9] = A.x; y[9] = A.y;
        	A = Vec.linComb(1, frameO, 1, frameV);
        	x[10] = A.x; y[10] = A.y;
        	A = Vec.linComb(3, frameO, 1, frameU, 2, frameV, 3);
        	x[11] = A.x; y[11] = A.y;
        	return new Polygon(x,y,npts);
		}

		@Override
        public double approxArea() { return Math.sqrt(3)/4; }

    };


    public static TessRule triP6a = new HexiRule(Messages.getString("Rule.P6"),Messages.getString("Rule.P6.descript")){ //$NON-NLS-1$ //$NON-NLS-2$
        @Override
        public void calcFund(FundamentalDomain fd)
        {
            int u1,u2,v1,v2;

            u1 =	frameV.x;
            u2 =	frameV.y;
            v1 = 	frameU.x;
            v2 = 	frameU.y;
            fd.fund[0].x = fd.cellVerts[0].x; fd.fund[0].y = fd.cellVerts[0].y;
            fd.fund[1].x = fd.cellVerts[1].x; fd.fund[1].y = fd.cellVerts[1].y;
            fd.fund[2].x = fd.cellVerts[1].x + u1+v1; fd.fund[2].y = fd.cellVerts[1].y + u2+v2;
            fd.numFund=3;
        }

        @Override
        public void fun(int[] in,int[] out,int det)
        {
            /*			int a = (int) Math.floor((float) in[0]/det);
			int b = (int) Math.floor((float) in[1]/det);
             */
            int a = (in[0]<0 ? (in[0]+1)/det -1 : in[0]/det); 
            int b = (in[1]<0 ? (in[1]+1)/det -1 : in[1]/det); 
            int alpha = in[0] % det; if(alpha < 0) alpha = alpha + det;
            int beta = in[1] % det; if(beta < 0) beta = beta + det;
            int res[] = new int[2];
            calcRot3(a,b,alpha,beta,det,res);
            alpha = res[0]; beta = res[1];
            // rotate bot right to top left

            if( beta < alpha )
            {
                alpha = det - alpha;
                beta = det - beta;
            }

            out[0] = alpha;
            out[1] = beta;
        }

        @Override
        protected void paintSymetries(Vec U, Vec V, Vec O) {

            drawRotationPoint(O,6);
            drawRotationPoint(frameU.add(O),3);
            drawRotationPoint(Vec.linComb(2,frameU,1,O),3);

            drawRotationPoint(Vec.linComb(2,O,1,frameU,-1,frameV,2),2);
            drawRotationPoint(Vec.linComb(2,O,3,frameV,2),2);
            drawRotationPoint(Vec.linComb(2,O,-3,frameU,0,frameV,2),2);
        }

        @Override
        public void paintDomainEdges(Vec U, Vec V,
                Vec O, int det) {
            Vec A = Vec.linComb(3, O, -1, U,-1,V,3);
            Vec B = Vec.linComb(3, O, 2, U,-1,V,3);
            Vec C = Vec.linComb(3, O, -1, U,2,V,3);
            Vec D = Vec.linComb(3, O, 2, U,2,V,3);
            drawSimpleEdge(O,A);
            drawSimpleEdge(O,B);
            drawSimpleEdge(O,C);
            drawSimpleEdge(B,O.add(V));
            drawSimpleEdge(C,O.add(U));
            drawSimpleEdge(O,D);

        }

        @Override
        public double approxArea() { return Math.sqrt(3)/4; }

    };
    
    public static TessRule triP6 = new HexiRule(Messages.getString("Rule.P6"),Messages.getString("Rule.P6.descript")){
        //@Override
        public void calcFund(FundamentalDomain fd)
        {
            int u1,u2,v1,v2;

            u1 =	frameV.x;
            u2 =	frameV.y;
            v1 = 	frameU.x;
            v2 = 	frameU.y;
            fd.fund[0].x = fd.cellVerts[0].x; fd.fund[0].y = fd.cellVerts[0].y;
            fd.fund[1].x = fd.cellVerts[1].x; fd.fund[1].y = fd.cellVerts[1].y;
            fd.fund[2].x = fd.cellVerts[1].x + u1+v1; fd.fund[2].y = fd.cellVerts[1].y + u2+v2;
            fd.numFund=3;
        }

        //@Override
        public void fun(int[] in,int[] out,int det)
        {
            /*			int a = (int) Math.floor((float) in[0]/det);
			int b = (int) Math.floor((float) in[1]/det);
             */
            int a = (in[0]<0 ? (in[0]+1)/det -1 : in[0]/det); 
            int b = (in[1]<0 ? (in[1]+1)/det -1 : in[1]/det); 
            int alpha = in[0] % det; if(alpha < 0) alpha = alpha + det;
            int beta = in[1] % det; if(beta < 0) beta = beta + det;
            int res[] = new int[2];
            calcRot3(a,b,alpha,beta,det,res);
            alpha = res[0]; beta = res[1];
            // rotate bot right to top left

            if( beta < alpha )
            {
                int gamma = beta;
                beta = det - alpha + beta;
                alpha = gamma;
            }

            out[0] = alpha;
            out[1] = beta;
        }

        //@Override
        protected void paintSymetries(Vec U, Vec V, Vec O) {
            Vec f23 = new Vec(frameU.x,frameU.y);
            Vec f45 = new Vec(frameV.x,frameV.y);

            drawRotationPoint(O,3);
            drawRotationPoint(f23.add(O),6);
            drawRotationPoint(Vec.linComb(2,f23,1,O),3);

            drawRotationPoint(Vec.linComb(-1,f45,2,O,2),2);
            drawRotationPoint(Vec.linComb(1,f23,-2,f45,2,O,2),2);
            drawRotationPoint(Vec.linComb(1,f23,1,f45,2,O,2),2);

        }

        //@Override
        public void paintDomainEdges(Vec U, Vec V,
                Vec O, int det) {
            Vec A = Vec.linComb(3, O, -1, U,-1,V,3);
            Vec B = Vec.linComb(3, O, 2, U,-1,V,3);
            Vec C = Vec.linComb(3, O, -1, U,2,V,3);
            Vec D = Vec.linComb(3, O, 2, U,2,V,3);
            this.drawSimpleEdge(O,A);
            this.drawSimpleEdge(O,B);
            this.drawSimpleEdge(O,C);
            this.drawSimpleEdge(B,O.add(V));
            this.drawSimpleEdge(C,O.add(U));
            this.drawSimpleEdge(O,D);

        }

        //@Override
        public double approxArea() { return Math.sqrt(3)/4; }

    };


    public static TessRule triP6ma = new HexiRule(Messages.getString("Rule.P6M"),Messages.getString("Rule.P6M.descript")){ //$NON-NLS-1$ //$NON-NLS-2$
        @Override
        public void calcFund(FundamentalDomain fd)
        {
            fd.fund[0].set(Vec.linComb(1,fd.cellVerts[1],1, frameV)); 
            fd.fund[1].set(Vec.linComb(1,fd.cellVerts[1],1, frameU,1, frameV)); 
//            fd.fund[1].set(fd.cellVerts[1]); 
            fd.fund[2].set(Vec.linComb(2,fd.cellVerts[1],1,frameU, 1, frameV,2)); 

            fd.numFund=3;
        }
        @Override
        public void fun(int[] in,int[] out,int det)
        {
            int a = (in[0]<0 ? (in[0]+1)/det -1 : in[0]/det); 
            int b = (in[1]<0 ? (in[1]+1)/det -1 : in[1]/det); 
            int alpha = in[0] % det; if(alpha < 0) { alpha = alpha + det; }
            int beta = in[1] % det; if(beta < 0) { beta = beta + det; } 
            int res[] = new int[2];
            calcRot3(a,b,alpha,beta,det,res);
            alpha = res[0]; beta = res[1];
            // rotate bot right to top left

          if( beta < alpha )
          {
              int gamma = beta;
              beta = alpha;
              alpha = gamma;
          }

            if( alpha + beta < det )
            {
           	 int gamma = alpha;
           	 alpha = det - beta;
           	 beta = det - gamma;
            }
            


            out[0] = alpha;
            out[1] = beta;
        }
        
        

        @Override
		public void replicate_isolated_domain(DrawableRegion dr, Polygon poly) {
        	if(poly.npoints==3)
        		super.replicate_isolated_domain(dr, poly);
        	else {
                
                Vec A = frameO.sub(frameU);
                Vec B = frameO;
                Vec C = frameO.add(frameU).add(frameV);
                Vec D = Vec.linComb(1, frameO, 1, frameU, 2, frameV);
                Vec E = Vec.linComb(1, frameO, 2, frameV);
                Vec F = Vec.linComb(1, frameO, -1, frameU, 1, frameV);

        		int x[] = {A.x, B.x, C.x, D.x , E.x, F.x};
        		int y[] = {A.y, B.y, C.y, D.y , E.y, F.y};
        		
        		Polygon p2 = new Polygon(x, y, 6);
        		super.replicate_isolated_domain(dr, p2);
        	}
		}
		@Override
        protected void paintSymetries(Vec U, Vec V, Vec O) {

 
            drawReflectionLine(
                    Vec.linComb(1,O,1,frameU,-1,frameV),
                    O);
            drawReflectionLine(
                    Vec.linComb(1,O,2,frameU,1,frameV),
                    O);
            drawReflectionLine(
            		O,
                  Vec.linComb(1,O,1,frameU,2,frameV));

            drawReflectionLine(
            		O,
                    Vec.linComb(1,O,3,frameU));

            drawReflectionLine(
            		Vec.linComb(-1,frameV,1,O),
            		Vec.linComb(3,frameU,2,frameV,1,O));
            drawReflectionLine(
            		Vec.linComb(1,frameU,-1,frameV,1,O),
            		Vec.linComb(1,frameU,2,frameV,1,O));
 
            drawRotationPoint(O,6);
            drawRotationPoint(frameU.add(O),3);
            drawRotationPoint(Vec.linComb(2,frameU,1,O),3);

            drawRotationPoint(Vec.linComb(2,O,2,frameU,1,frameV,2),2);
            drawRotationPoint(Vec.linComb(2,O,1,frameU,2,frameV,2),2);
            drawRotationPoint(Vec.linComb(2,O,1,frameU,-1,frameV,2),2);
            
            drawGlideLine(
            		Vec.linComb(2, O, -1, frameU,1,frameV,2),
            		Vec.linComb(2, O, 5, frameU,1,frameV,2));

            drawGlideLine(
            		Vec.linComb(2, O, 1, frameU,-1,frameV,2),
            		Vec.linComb(2, O, 1, frameU,5,frameV,2));
            drawGlideLine(
            		Vec.linComb(2, O, -1, frameU,0,frameV,2),
            		Vec.linComb(2, O, 5, frameU,6,frameV,2));
            
            drawGlideLine(
            		Vec.linComb(2, O, 1, frameU,2,frameV,2),
            		Vec.linComb(2, O, -3, frameU,2));
 
            drawGlideLine(
            		Vec.linComb(2, O, 1,frameU,2,frameV,2),
            		Vec.linComb(2, O, -1, frameU, 4, frameV,2));
 
            drawGlideLine(
            		Vec.linComb(2, O, -2,frameU,-1,frameV,2),
            		Vec.linComb(2, O, 0, frameU, 3, frameV,2));            

        }

        @Override
        public void paintDomainEdges(Vec U, Vec V,
                Vec O, int det) {
            Vec C = Vec.linComb(3, O, -1, U,2,V,3);
            drawSimpleEdge(O,Vec.linComb(3, O, -1, U,-1,V,3));
            drawSimpleEdge(O,Vec.linComb(3, O, 2, U,-1,V,3));
            drawSimpleEdge(O,C);
            drawSimpleEdge(Vec.linComb(3, O, 2, U,-1,V,3),O.add(V));
            drawSimpleEdge(C,O.add(U));
            drawSimpleEdge(O,Vec.linComb(3, O, 2, U,2,V,3));
            drawSimpleEdge(O, O.add(V));
            drawSimpleEdge(O, O.add(U));
            drawSimpleEdge(O, Vec.linComb(1, O, -1, U, 1, V));


        }

        @Override
        public double approxArea() { return Math.sqrt(3)/4; }

    };

    public static TessRule triP6m = new HexiRule(Messages.getString("Rule.P6M"),Messages.getString("Rule.P6M.descript")){
        //@Override
        public void calcFund(FundamentalDomain fd)
        {
            int u1,u2,v1,v2; //,w1,w2;

            u1 =	frameV.x;
            u2 =	frameV.y;
            v1 = 	frameU.x;
            v2 = 	frameU.y;
            fd.fund[0].x = fd.cellVerts[1].x+u1/2; fd.fund[0].y = fd.cellVerts[1].y+u2/2;
            fd.fund[1].x = fd.cellVerts[1].x; fd.fund[1].y = fd.cellVerts[1].y;
            fd.fund[2].x = fd.cellVerts[1].x + u1+v1; fd.fund[2].y = fd.cellVerts[1].y + u2+v2;
            fd.numFund=3;
        }
        //@Override
        public void fun(int[] in,int[] out,int det)
        {
            int a = (in[0]<0 ? (in[0]+1)/det -1 : in[0]/det); 
            int b = (in[1]<0 ? (in[1]+1)/det -1 : in[1]/det); 
            /*
			int a1 = (int) Math.floor((float) in[0]/det);
			int b1 = (int) Math.floor((float) in[1]/det);
			if(a!=a1 && firstBug) {
				System.out.printf("a %d %d in %d det %d\n",a,a1,in[0],det);
				firstBug=false;
			}
             */
            int alpha = in[0] % det; if(alpha < 0) { alpha = alpha + det; }
            int beta = in[1] % det; if(beta < 0) { beta = beta + det; } 
            int res[] = new int[2];
            calcRot3(a,b,alpha,beta,det,res);
            alpha = res[0]; beta = res[1];
            // rotate bot right to top left

            if( beta < alpha )
            {
                int gamma = beta;
                beta = det - alpha + beta;
                alpha = gamma;
            }

            if( 2 * beta - alpha - det > 0 )
            {
                beta = alpha - beta +det;
            }

            out[0] = alpha;
            out[1] = beta;
        }

        //@Override
        protected void paintSymetries(Vec U, Vec V, Vec O) {
            Vec f23 = new Vec(frameU.x,frameU.y);
            Vec f45 = new Vec(frameV.x,frameV.y);
            Vec f56 = f45.add(f23);

 
            drawReflectionLine(
                    Vec.linComb(-1,f45,2,O,2),
                    Vec.linComb(1,f45,4,f23,2,O,2));
            drawReflectionLine(
                    Vec.linComb(1,f56,2,O,2),
                    Vec.linComb(2,f23,-2,f45,1,f56,2).add(O));
            drawReflectionLine(
                    Vec.linComb(1,f23,-2,f45,2,O,2),
                    Vec.linComb(1,f23,2,f56,2,O,2));

            drawReflectionLine(O,Vec.linComb(3,f23,1,O));
            drawReflectionLine(Vec.linComb(-1,f45,1,O),Vec.linComb(3,f23,2,f45,1,O));
            drawReflectionLine(Vec.linComb(1,f23,-1,f45,1,O),Vec.linComb(1,f23,2,f45,1,O));
 
            drawRotationPoint(O,3);
            drawRotationPoint(f23.add(O),6);
            drawRotationPoint(Vec.linComb(2,f23,1,O),3);

            drawRotationPoint(Vec.linComb(-1,f45,2,O,2),2);
            drawRotationPoint(Vec.linComb(1,f23,-2,f45,2,O,2),2);
            drawRotationPoint(Vec.linComb(1,f23,1,f45,2,O,2),2);

            Vec a = Vec.linComb(6, O, 2, U, -1, V, 6);
            Vec b = Vec.linComb(6, O, 5, U, -1, V, 6);
            Vec c = Vec.linComb(6, O, 5, U, 2, V, 6);
            Vec d = Vec.linComb(6, O, 2, U, 5, V, 6);
            Vec e = Vec.linComb(6, O, -1, U, 5, V, 6);
            Vec f = Vec.linComb(6, O, -1, U, 2, V, 6);
            
            drawGlideLine(a,b);
            drawGlideLine(b,c);
            drawGlideLine(c,d);
            drawGlideLine(d,e);
            drawGlideLine(e,f);
            drawGlideLine(f,a);
            
            drawGlideLine(a,c);
            drawGlideLine(b,d);
            drawGlideLine(c,e);
            drawGlideLine(d,f);
            drawGlideLine(e,a);
            drawGlideLine(f,b);

        }

        //@Override
        public void paintDomainEdges(Vec U, Vec V,
                Vec O, int det) {
            Vec A = Vec.linComb(3, O, -1, U,-1,V,3);
            Vec B = Vec.linComb(3, O, 2, U,-1,V,3);
            Vec C = Vec.linComb(3, O, -1, U,2,V,3);
            Vec D = Vec.linComb(3, O, 2, U,2,V,3);
            Vec B2 = Vec.linComb(6, O, 2, U,-1,V,6);
            Vec C2 = Vec.linComb(6, O, -1, U,2,V,6);
            Vec E = Vec.linComb(6, O, 5, U,-1,V,6);
            Vec F = Vec.linComb(6, O, -1, U,5,V,6);

            this.drawSimpleEdge(O,A);
            this.drawSimpleEdge(O,B);
            this.drawSimpleEdge(O,C);
            this.drawSimpleEdge(B,O.add(V));
            this.drawSimpleEdge(C,O.add(U));
            this.drawSimpleEdge(O,D);
            this.drawSimpleEdge(B2, B2.add(V));
            this.drawSimpleEdge(C2, C2.add(U));
            this.drawSimpleEdge(E, F);


        }

        //@Override
        public double approxArea() { return Math.sqrt(3)/4; }

    };

}
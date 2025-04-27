package org.singsurf.wallpaper.tessrules;

import org.singsurf.wallpaper.FundamentalDomain;
import org.singsurf.wallpaper.Vec;

public abstract class IrregularHexRule extends TessRule {
	//int det;
	
	public IrregularHexRule(String name, String message) {
		super(name, message);
	}

	//@Override
	public void calcFrame(FundamentalDomain fd, int selectedVertex,
			boolean constrained) {
		frameO.set(fd.cellVerts[1]);
		frameU.set(fd.cellVerts[2].sub(fd.cellVerts[1]));
		frameV.set(fd.cellVerts[0].sub(fd.cellVerts[1]));
		//det = frameU.cross(frameV);
	}


	//@Override
	public void fixVerticies(FundamentalDomain fd) {
		fd.cellVerts[0].set(frameO.add(frameV));
		fd.cellVerts[1].set(frameO);
		fd.cellVerts[2].set(frameO.add(frameU));
		fd.cellVerts[3].setLC(1, frameO, 2, frameU, 1, frameV);
		fd.cellVerts[4].setLC(1, frameO, 2, frameU, 2, frameV);
		fd.cellVerts[5].setLC(1, frameO, 1, frameU, 2, frameV);
		fd.numOuterPoints=6;
        fd.numSelPoints = 3;
        fd.setLatticeType(FundamentalDomain.HEXAGON);

	}

    public static TessRule p2hex = new IrregularHexRule("P2H",
            "A varient on the p2 pattern when the basic tile is an irregular hexagon.\n"
            +"The fundamental domain is a trapesium made by cutting the tile in half.") {
    	
        /** Calculates the fundamental domain */
        //@Override
        public void calcFund(FundamentalDomain fd)
       {
        	fd.fund[0].setLC(1, frameO, 1, frameV);
        	fd.fund[1].set(frameO);
        	fd.fund[2].setLC(1, frameO, 1, frameU);
        	if(fd.det>0)
        		fd.fund[3].setLC(1, frameO, 1,frameU, 2, frameV);
        	else
        		fd.fund[3].setLC(1, frameO, 2,frameU, 1, frameV);
        	fd.numFund = 4;
       }

		//@Override
		public void fun(int[] in, int[] out, int det) {
            int a = (in[0]<0 ? (in[0]+1)/det -1 : in[0]/det); 
            int b = (in[1]<0 ? (in[1]+1)/det -1 : in[1]/det); 
            int alpha = in[0] % det; if(alpha < 0) alpha = alpha + det;
            int beta = in[1] % det; if(beta < 0) beta = beta + det;

           	int index = (a+b)%3;
        	index = index>=0? index : index+3;
//            switch( ((a+b)%3+3)%3 )
            switch( index )
            {
            case 0:
            	out[0] = alpha;
            	out[1] = beta;
            	break;
            case 1:
            	if(alpha>beta) {
                	out[0] = alpha;
                	out[1] = det+beta;
            	}
            	else {
                	out[0] = det-alpha;
                	out[1] = 2*det-beta;
            	}
            	break;
            case 2:
            	out[0] = det - alpha;
            	out[1] = det - beta;
            	break;
            }
		}

		//@Override
		public void paintDomainEdges(Vec U, Vec V, Vec O, int det) {
			
		//	drawSimpleEdge(O, O.add(U));
		//	drawSimpleEdge(O, O.add(V));
			drawSimpleEdge(O, Vec.linComb(3, O, -1, U,-1,V,3));
			drawSimpleEdge(O, Vec.linComb(3, O, 2, U,-1,V,3));
			drawSimpleEdge(O, Vec.linComb(3, O, -1, U,2,V,3));
			if(det>0)
				drawSimpleEdge(O.add(U), Vec.linComb(3, O, -1, U,2,V,3));
			else
				drawSimpleEdge(O, Vec.linComb(3, O, 2, U,2,V,3));

			//drawSimpleEdge(O.add(U), Vec.linComb(1, O, 1, U,2,V));
		}

		//@Override
		protected void paintSymetries(Vec U, Vec V, Vec O) {
            Vec f23 = new Vec(frameU.x,frameU.y);
            Vec f45 = new Vec(frameV.x,frameV.y);
			
            drawRotationPoint(f23.add(O),2);
            drawRotationPoint(Vec.linComb(-1,f45,2,O,2),2);
            drawRotationPoint(Vec.linComb(1,f23,-2,f45,2,O,2),2);
            drawRotationPoint(Vec.linComb(1,f23,1,f45,2,O,2),2);

		}
		
		
    };

    public static TessRule p1hex = new IrregularHexRule("P1H",
            "A varient on the p1 pattern when the basic tile is an irregular hexagon.\n"
            ) {
    	
        /** Calculates the fundamental domain */
        //@Override
        public void calcFund(FundamentalDomain fd)
       {
        	fd.fund[0].setLC(1, frameO, 1, frameV);
        	fd.fund[1].set(frameO);
        	fd.fund[2].setLC(1, frameO, 1, frameU);
        	if(fd.det>0) {
        		fd.fund[3].setLC(1, frameO, 2,frameU, 1, frameV);
        		fd.fund[4].setLC(1, frameO, 2,frameU, 2, frameV);
        		fd.fund[5].setLC(1, frameO, 1,frameU, 2, frameV);
        	}
        	else {
        		fd.fund[3].setLC(1, frameO, 2,frameU, 1, frameV);
        		fd.fund[4].setLC(1, frameO, 2,frameU, 2, frameV);
        		fd.fund[5].setLC(1, frameO, 1,frameU, 2, frameV);
        	}
        	fd.numFund = 6;
       }

		//@Override
		public void fun(int[] in, int[] out, int det) {
            int a = (in[0]<0 ? (in[0]+1)/det -1 : in[0]/det); 
            int b = (in[1]<0 ? (in[1]+1)/det -1 : in[1]/det); 
            int alpha = in[0] % det; if(alpha < 0) alpha = alpha + det;
            int beta = in[1] % det; if(beta < 0) beta = beta + det;

           	int index = (a+b)%3;
        	index = index>=0? index : index+3;
            switch( index )
            {
            case 0:
            	out[0] = alpha;
            	out[1] = beta;
            	break;
            case 1:
            	if(alpha>beta) {
                	out[0] = alpha;
                	out[1] = det+beta;
            	}
            	else {
                	out[0] = det+alpha;
                	out[1] = beta;
            	}
            	break;
            case 2:
            	out[0] = det + alpha;
            	out[1] = det + beta;
            	break;
            }
		}

		@Override
		public void paintDomainEdges(Vec U, Vec V, Vec O, int det) {
			Vec A = Vec.linComb(3, O, 2, U, -1, V, 3);
			Vec B = O.add(U);
			Vec C = Vec.linComb(3, O, 2, U, 2, V, 3);
			Vec D = O.add(V);
			Vec E = Vec.linComb(3, O, -1, U, 2, V, 3);
			drawSimpleEdge(O, A);
			drawSimpleEdge(A, B);
			drawSimpleEdge(B, C);
			drawSimpleEdge(C, D);
			drawSimpleEdge(D, E);
			drawSimpleEdge(E, O);
		}

		@Override
		protected void paintSymetries(Vec U, Vec V, Vec O) {

		}
		
		
    };


}

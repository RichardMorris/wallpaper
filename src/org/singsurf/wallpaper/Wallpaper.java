package org.singsurf.wallpaper;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.InputEvent;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.net.URL;
import java.util.Timer;
import java.util.TimerTask;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextArea;

import org.singsurf.wallpaper.animation.AnimationPath;
import org.singsurf.wallpaper.tessrules.TessRule;

/**
An applet which calculates wallpaper patterns
using a portion of an image.
 **/

public class Wallpaper extends JPanel implements MouseListener, MouseMotionListener, KeyListener, ItemListener
{
    private static final long serialVersionUID = 1L;
    protected static final boolean DEBUG = false;

    /** Labels and action commands for flip/rotate */
    public static final String FLIP_X = "Flip X";
    public static final String FLIP_Y = "Flip Y";
    protected static final String FLIP_90 = "Rotate clockwise";
    protected static final String FLIP_180 = "Rotate 180";
    protected static final String FLIP_270 = "Rotate anti-clockwise";

    /** Mouse mode */
    static final int MOUSE_NORMAL = 0;
    protected static final int MOUSE_PIPET = 1;
    protected int mouseMode = MOUSE_NORMAL;

    /** the current vertex for moving the triangle */
    int curvertex=0;

    /** Whether we are in interactive mode */
    boolean interactiveMode = true;

    /** Whether to draw the symmetry lines */
    //private boolean symmetryLines = false;

    /** Whether to centerImage */
    //protected boolean centerImage = false;

    /** Offset of image in canvas */
    public Point offset=new Point(0,0);

    protected Color backgroundColour = Color.black;

    protected Controller controller;
    public DrawableRegion dr;
    protected FundamentalDomain fd;




    JTextArea ta;
    /** whether the paint method has been completed. */
    private boolean paintDone = true;
    /** If interactive kaleidoscope more */
//    private JCheckBox interactiveCB;

    /** Show the fundamental domain **/
    private JCheckBox showFund;

    public JComponent myCanvas;
    private Cursor pipet;
    // This interface is used for objects defining now to do tessellation 
    public JFrame mainFrame=null;
    protected String imageFilename=null;
    protected URL imageURL=null;

    public int clickCount = 0;

    public void paintCanvas(Graphics g) {
        if(DEBUG) System.out.println("paintCanvas" + dr.dispRect);
        
        //System.out.printf("cp %d %d %d %d %d %d\n",fd.verticies[0].x,fd.verticies[0].y,fd.verticies[1].x,fd.verticies[1].y,fd.verticies[2].x,fd.verticies[2].y);
        //System.out.printf("%d %d%n", this.offset.x,this.offset.y);
        g.translate(this.offset.x,this.offset.y);
        Rectangle bounds = g.getClipBounds();
        if(bounds != null && (bounds.x + bounds.width > dr.dispRect.x+dr.dispRect.width)) {
            g.clearRect(dr.dispRect.x+dr.dispRect.width, bounds.y,
                    bounds.x + bounds.width - (dr.dispRect.x+dr.dispRect.width), bounds.height);
        }
        if(bounds != null && (bounds.y + bounds.height > dr.dispRect.y+dr.dispRect.height)) {
            g.clearRect(bounds.x,dr.dispRect.y+dr.dispRect.height,
                    bounds.width,bounds.y + bounds.height - (dr.dispRect.y+dr.dispRect.height));
        }
        //g.clipRect(dr.dispRect.x,dr.dispRect.y,dr.dispRect.width,dr.dispRect.height);
        dr.paint(g,this);
        g.setPaintMode();

        fd.paintSymetries(g, controller.tr);
        fd.paint(g);

        if(clickCount==0)
            paintIntro(g);
        if(clickCount==1)
            paintIntro2(g);

        if(this.controller.constrainVertices)
            fd.paintRegularTile(g);


        /*                final int latticeWidth = fd.getLatticeWidth();
                final int latticeHeight = fd.getLatticeHeight();
               Vec[] points = fd.getLatticePoints(new Rectangle(
                        dr.dispRect.x-latticeWidth,dr.dispRect.y-latticeHeight,
                        dr.dispRect.width+latticeHeight,
                        dr.dispRect.height+latticeHeight));

                g.setPaintMode();
                g.setColor(Color.ORANGE);
                int c=0;
                for(Vec p:points) {
                   switch(++c%8) {
                    case 0: g.setColor(Color.BLACK); break;
                    case 1: g.setColor(Color.RED); break;
                    case 2: g.setColor(Color.GREEN); break;
                    case 3: g.setColor(Color.BLUE); break;
                    case 4: g.setColor(Color.MAGENTA); break;
                    case 5: g.setColor(Color.YELLOW); break;
                    case 6: g.setColor(Color.CYAN); break;
                    case 7: g.setColor(Color.PINK); break;
                    }
                    //g.drawRect(p.x, p.y, latticeWidth, latticeHeight);
                    g.fillOval(p.x-2, p.y-2, 5, 5);
                }
                Rectangle rect = fd.getMinimalRectangle(points);
                if(rect!=null)
                    g.drawRect(rect.x,rect.y,rect.width,rect.height);
         */
        g.translate(-this.offset.x,-this.offset.y);
        paintDone = true;
    }

    private void paintIntro(Graphics g) {
        Vec base = this.controller.tr.frameO;
        String s1 = "Click and drag the red, green or blue dots";
        String s2 = "to change the pattern.";
        Font f = new Font("SansSerif",Font.BOLD,16);
        g.setFont(f);
        FontMetrics fm = g.getFontMetrics();
        int len1 = fm.stringWidth(s1);
        int height = fm.getHeight();
        int accent = fm.getMaxAscent();
        g.setColor(Color.white);
        g.fillRoundRect(210,base.y+20,len1+20,height*2+20, 20, 20);
        g.setColor(Color.black);

        g.drawString(s1,220,base.y+30+accent);
        g.drawString(s2,220,base.y+30+accent+height);
    }

    private void paintIntro2(Graphics g) {
        String s1 = "Click a button on the left";
        String s2 = "to change the type of pattern.";
        Font f = new Font("SansSerif",Font.BOLD,16);
        g.setFont(f);
        FontMetrics fm = g.getFontMetrics();
        int len2 = fm.stringWidth(s2);
        int height = fm.getHeight();
        int accent = fm.getMaxAscent();
        g.setColor(Color.white);
        g.fillRoundRect(10, 10,len2+20,height*2+20, 20, 20);
        g.setColor(Color.black);

        g.drawString(s1,20,20+accent);
        g.drawString(s2,20,20+accent+height);
    }

    //@Override
    //@Override
    //public void paint(Graphics g) {
      //  if(DEBUG) System.out.println("Applet paint"); 
        //paintCanvas(g);  
    //}

    private boolean mousePressed = false;

    public void mouseEntered(MouseEvent e) {/*ignore*/}
    public void mouseExited(MouseEvent e) {/*ignore*/}
    public void mouseReleased(MouseEvent e) 
    {
        if(DEBUG) System.out.println("Mouse released");
        mousePressed = false;
        ++clickCount;
        if(clickCount<3) myCanvas.repaint();
    }

    public void mouseClicked(MouseEvent e) 
    {
        if(DEBUG) System.out.println("Mouse clicked");

        if(mouseMode==MOUSE_PIPET) {
            int x = e.getX()-offset.x;
            int y = e.getY()-offset.y;
            if(x>=dr.destRect.width || y>=dr.destRect.height) {
                myCanvas.setCursor(Cursor.getDefaultCursor());
                return;
            }
            int outInd = x+y*dr.destRect.width;
            Color col = new Color(dr.pixels[outInd]);
            setBGColor(col);
            mouseMode = MOUSE_NORMAL;
            controller.redraw();
        }
    }


    public void mouseMoved(MouseEvent e)
    {
        //if(DEBUG) System.out.println("Mouse moved");

        if(mouseMode==MOUSE_PIPET) {
            int x = e.getX()-offset.x;
            int y = e.getY()-offset.y;
            if(x>=dr.destRect.width || y>=dr.destRect.height) {
                myCanvas.setCursor(Cursor.getDefaultCursor());
                return;
            }
            myCanvas.setCursor(pipet);
            int outInd = x+y*dr.destRect.width;
            Color col = new Color(dr.pixels[outInd]);
            setText(col.toString());
            return;
        }
        int x = e.getX()-offset.x;
        int y = e.getY()-offset.y;
        //if(DEBUG) System.out.println("Mouse moved");
        int index = fd.getClosestVertex(x,y);
        if(index!=-1)
            myCanvas.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        else
            myCanvas.setCursor(Cursor.getDefaultCursor());
    }

    public void mousePressed(MouseEvent e)
    {
        if(DEBUG) System.out.println("Mouse pressed");
        ++clickCount;
        mousePressed = true;

        int x = e.getX()-offset.x;
        int y = e.getY()-offset.y;
        if(x>dr.destRect.width) x = dr.destRect.width;
        if(x<0) x = 0;
        if(y>dr.destRect.height) y=dr.destRect.height;
        if(y<0) y = 0;
        curvertex = fd.getClosestVertex(x,y);
        if(curvertex!=-1)
        {
            fd.saveOldVerticies();
            fd.setVertex(curvertex,x,y);
        }
        if(e.getClickCount()>1)
        {
            if(DEBUG) System.out.println("clicks "+e.getClickCount());
            // Only recalculate when paint has been completed or 1 sec passed.
            // and 0.1s has passed.
            long curtime = System.currentTimeMillis();
            //System.out.println("t "+(curtime- lasttime)+" pd "+paintDone);
            if(curtime- lasttime <100) return;
            if(!paintDone && curtime- lasttime <1000 ) return;
            lasttime = curtime;
            paintDone = false;
            if(controller.showingOriginal /* || !this.interactiveMode */ ) {
                controller.applyTessellation();
            }
            else {
                controller.showOriginal();
            }
        }
    }

    private long lasttime=0;

    public void mouseDragged(MouseEvent e)
    {
        if(DEBUG) System.out.println("mouseDragged "+e);
        if(!mousePressed) {
            System.out.println("FAKE Event "+e);
            return;
        }
        if((e.getModifiers() & InputEvent.BUTTON3_MASK )!= 0) return;
        if(curvertex == -1) return;
        fd.saveOldVerticies();

        int x = e.getX()-offset.x;
        int y = e.getY()-offset.y;
        if(x>dr.destRect.width) x = dr.destRect.width;	if(x<0) x = 0;
        if(y>dr.destRect.height) y=dr.destRect.height;	if(y<0) y = 0;
        fd.setVertex(curvertex,x,y);
        
        controller.calcGeom();
        //System.out.println(fd.toString(dr));
        //System.out.println(Arrays.toString(fd.getLatticePoints(dr.dispRect)));

        //	    if(fd.tileableRegion(dr.dispRect)==null) {
        //	        System.out.println("Not tileable");
        //	        fd.restoreOldVerticies();
        //	    }
        //	    else
        //	        System.out.println("Not tileable");

        //System.out.printf("md %d\n",fd.verticies[1].y-fd.verticies[0].y);
        if(!interactiveMode)
        {
            //repaintLines(myCanvas.getGraphics());
            myCanvas.repaint();			//redraw(false);
            return;
        }

        // Only recalculate when paint has been completed or 1 sec passed.
        // and 0.1s has pased.
        //long curtime = System.currentTimeMillis();
        //System.out.println("t "+(curtime- lasttime)+" pd "+paintDone);
        //if(curtime- lasttime <100) return;
        if(!paintDone /*&& curtime- lasttime <1000 */) return;
        //lasttime = curtime;
        paintDone = false;
        controller.applyTessellation();
    }


//    public Image getImage(String loc) {
//        return this.appletGetImage(loc);
//    }


    protected static boolean first=true;

    public void initialize(Image img,int w,int h) {
        if(DEBUG) System.out.println("img w "+w+" h "+h);
//        Image pip = null; // getImage("pipet.gif");
// TODO       if(pip!=null)
//            pipet = Toolkit.getDefaultToolkit().createCustomCursor(
//                    pip,new Point(8,25), "pipet");
//        else
//            pipet = Cursor.getDefaultCursor();

        this.setLayout(new BorderLayout());
        JPanel pan = new JPanel();
        pan.setLayout(new BorderLayout());
        ta = new JTextArea(
                "This applet calculate a symmetry pattern based on one of 17 different patterns.\n"
                +"Click and drag the Red, Green or Blue points to move the yellow polygon.\n"
                +"Select a button on left to change the type.\n"
                +"The blue polygon gives the 'Fundamental Domain'. The patten is created by\n"
                +"taking this region and reflecting, rotating and translating it.\n"
                +"The yellow polygon gives the region which is repeated by translation only.\n"
                +"Double click to redraw and right click to revert back to original image.",
                3,60);
        //TODO ta.setS,TextArea.SCROLLBARS_VERTICAL_ONLY);
        ta.setEditable(false);
        ta.setBackground(Color.white); // use lower case colors for compatability with old jdk
        ta.setBorder(BorderFactory.createEtchedBorder());
        //ta.setMargin(new Insets(2, 4, 2, 2));
        //ta.set
        JComponent mainWin = buildCanvas();

        controller = new Controller(this,dr,fd);
        tesselationPanel = new GraphicalTesselationPanel(controller);
        JPanel p2 = buildButtonBar();

        add("West",tesselationPanel);
        add("Center",pan);
        pan.add("North",p2);
        pan.add("Center",mainWin);
        pan.add("South",ta);

//        add("West",tesselationPanel);
//        add("Center",mainWin);
//        add("North",p2);
//        add("South",ta);

        
        this.validate();
        this.doLayout();
        //System.out.println(myCanvas.getBounds());

        dr = buildDrawableRegion();
        if(!dr.loadImage(img)) {
            System.out.println("Using default image");
            dr.loadImage(DefaultImage.createDefaultImage());
        }
        fd = new FundamentalDomain();
        controller.setDr(dr);
        controller.setFd(fd);
        myCanvas.setSize(dr.destRect.width,dr.destRect.height); 
        myCanvas.setPreferredSize(dr.destRect.getSize()); 
        //myCanvas.set.setPreferedSize(dr.destRect.width,dr.destRect.height); 
        dr.setViewport(dr.destRect);//new Rectangle(0,0,dr.destRect.width,dr.destRect.height));

        fd.resetDomain(dr.dispRect);
        controller.setTesselation(tesselationPanel.getCurrentTesselation());
        controller.calcGeom();
        controller.showOriginal();
        if(DEBUG) System.out.println("initialise done");
        //this.validate();
    }

    protected DrawableRegion buildDrawableRegion() {
        //return new FixedSizeDrawableRegion(this,myCanvas.getSize());
        return new DrawableRegion(this);
    }

    protected JComponent buildCanvas() {
        myCanvas = new ScrollablePicture(this);
//        myCanvas = new JPanel() {
//            /**
//             * 
//             */
//            private static final long serialVersionUID = 1L;
// 
//            @Override
//            public void update(Graphics g)
//            {
//                if(DEBUG) System.out.println("update");
//                paintCanvas(g);
//            }
//
//            @Override
//            public void paint(Graphics g) 
//            {
//                if(DEBUG) System.out.println("paint");
//                paintCanvas(g);
//            }
//
//            @Override
//            protected void paintComponent(Graphics g) {
//                if(DEBUG) System.out.println("paintComponent " + g.getClipBounds() + " "+ myCanvas.getSize());
//                paintCanvas(g);
//            }
//            
//        };
        myCanvas.addMouseListener(this);
        myCanvas.addMouseMotionListener(this);
        myCanvas.addKeyListener(this);
        
        return myCanvas;
    }

    protected JPanel buildButtonBar() {
        JPanel p2 = new JPanel();
        origTileButton = new JButton("Original Image");
        origTileButton.addActionListener(
                new ActionListener()
                {	
                    public void actionPerformed(ActionEvent e)
                    {
                    	if(DEBUG) System.out.println("OrigBut "+controller.showingOriginal);
                        if(controller.showingOriginal) {
                            controller.applyTessellation();
                            origTileButton.setText("Original Image");
                        }
                        else {
                            controller.showOriginal();
                            origTileButton.setText("Tile Image");

                        }
                    }
                } );
        p2.add(origTileButton);

        JButton b4 = new JButton("Reset");
        b4.addActionListener(
                new ActionListener()
                {	public void actionPerformed(ActionEvent e)
                {
                    fd.resetDomain(dr.dispRect);
                    controller.tr.firstCall = true;
                    controller.calcGeom();
                    controller.redraw();
                }
                } );
        p2.add(b4);

        showFund = new JCheckBox("Show Domain",true);
        p2.add(showFund);
        showFund.addItemListener(new ItemListener() {
            public void itemStateChanged(ItemEvent e) {
                fd.drawDomain = (e.getStateChange() == ItemEvent.SELECTED);
                myCanvas.repaint();
            }});

//        showCPnt = new JCheckBox("Show Control Points",true);
//        p2.add(showCPnt);
//        showCPnt.addItemListener(new ItemListener() {
//            public void itemStateChanged(ItemEvent e) {
//            	fd.drawSelectionPoints = showCPnt.isSelected();
//                myCanvas.repaint();
//            }});

//        interactiveCB = new JCheckBox("Interactive mode",interactiveMode);
//        interactiveCB.addItemListener(new ItemListener() {
//            public void itemStateChanged(ItemEvent e) {
//                if(e.getStateChange() == ItemEvent.SELECTED)
//                {
//                    interactiveMode = true;
//                    myCanvas.repaint();
//                    ta.setText("Interactive mode.");
//                }
//                else
//                {
//                    interactiveMode = false;
//                    ta.setText("Double click to redraw.");
//                    myCanvas.repaint();
//                }
//            }});
//        p2.add(interactiveCB);

        JCheckBox symmetryCB = new JCheckBox("Draw symmetry");
        symmetryCB.addItemListener(new ItemListener() {
            public void itemStateChanged(ItemEvent e) {
                if(e.getStateChange() == ItemEvent.SELECTED)
                {
                    fd.drawGlideLines = true;
                    fd.drawCells = true;
                    fd.drawReflectionLines = true;
                    fd.drawRotationPoints = true;
                    myCanvas.repaint();
                }
                else
                {
                    fd.drawGlideLines = false;
                    fd.drawCells = false;
                    fd.drawReflectionLines = false;
                    fd.drawRotationPoints = false;
                    myCanvas.repaint();
                }
            }});
        p2.add(symmetryCB);

        p2.add(new JLabel("Anim"));
        stopBut = new JButton("Start");
        stopBut.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                stopStartAnim();
            }

        });
        stopBut.setVisible(true);
	    stopBut.setEnabled(true);

        p2.add(stopBut);

        JComboBox<String> animateMenu = buildAnimationChoice();
        p2.add(animateMenu);


        return p2;
    }

    public void tickCheckbox(String name) {
        this.tesselationPanel.tickCheckbox(name);
    }


    protected void setBGColor(Color col) {
        backgroundColour = col;
        TessRule.backgroundRGB = backgroundColour.getRGB();
        dr.backgroundRGB = backgroundColour.getRGB();
        controller.redraw();
    }

    /**
     * Sets descriptive text.
     * @param message
     */
    public void setText(String message) {
        ta.setText(message);
    }


//    JFrame findParentFrame(){ 
//        // From http://www.jguru.com/faq/view.jsp?EID=27423
//        JComponent c = this; 
//        while(c != null){ 
//            if (c instanceof JFrame) 
//                return (JFrame)c; 
//
//            c = c.getParent(); 
//        } 
//        return null; 
//    } 


    public static String programInfo() {
        return "org.singsurf.wallpaper version 1.5\nCopyright R. J. Morris 2010\nhttp://www.singsurf.org/";

    }

    public static String helpInfo() {
        return "Syntax:\n" +
        "\tjava -jar wallpaper.jar imageName [width height]"; 
    }
    //@Override
    public String getAppletInfo() {
        return programInfo();
    }

    private final String[][] info = { { "image", "URL", "URL for image" }, };
    private GraphicalTesselationPanel tesselationPanel;
    JButton origTileButton;
	/** Current path of animations */
	AnimationPath path;
	/** Button to stop animations */
	protected JButton stopBut;
	private TimerTask animateTask = null;
	public Thread animate = null;
	private final Timer timer = new Timer();
	boolean animRunning = false;
	protected JComboBox<String> animateChoice;

    //@Override
    public String[][] getParameterInfo() {
        return info;
    }


    public void keyPressed(KeyEvent e) {
        int code = e.getKeyCode();
        if(code == KeyEvent.VK_LEFT) fd.shift(-1,0);
        else if(code == KeyEvent.VK_RIGHT) fd.shift(1,0);
        else if(code == KeyEvent.VK_UP) fd.shift(0,-1);
        else if(code == KeyEvent.VK_DOWN) fd.shift(0,1);
        else return;

        controller.calcGeom();
        if(!interactiveMode)
        {
            //repaintLines(myCanvas.getGraphics());
            myCanvas.repaint();			//redraw(false);
            return;
        }

        // Only recalculate when paint has been completed or 1 sec passed.
        // and 0.1s has pased.
        //long curtime = System.currentTimeMillis();
        //System.out.println("t "+(curtime- lasttime)+" pd "+paintDone);
        //if(curtime- lasttime <100) return;
        if(!paintDone /*&& curtime- lasttime <1000 */) return;
        //lasttime = curtime;
        paintDone = false;
        controller.applyTessellation();

    }
    public void keyReleased(KeyEvent e) {/*ignore*/}

    protected JComboBox<String> buildAnimationChoice() {
	    stopBut.setVisible(true);
	    stopBut.setEnabled(true);
	    animateChoice = new JComboBox<String>();
	    String animations[] = {"bounce","smooth","up","down","left","right","NE","NW","SE","SW"}; // "rotate",
	    for(int i=0;i<animations.length;++i) {
//	        JMenuItem mi = new JMenuItem(animations[i]);
//	        mi.setActionCommand("anim/"+animations[i]);
//	        mi.addActionListener(this);
	        animateChoice.addItem(animations[i]);
	    }
	    animateChoice.setSelectedIndex(0);
//	    animateMenu.addSeparator();
//	    JMenuItem mi = new JMenuItem("Stop");
//	    mi.setActionCommand("anim/Stop");
//	    mi.addActionListener(this);
//	    animateMenu.add(mi);
	    animateChoice.addItemListener(this);
	
	    return animateChoice;
	}

	public void keyTyped(KeyEvent e) {/*ignore*/}

	protected void stopStartAnim() {
		System.out.println("startStopAnim");
	    if(animRunning)
	        stopAnim();
	    else if(path!=null)
	        startAnim();
	    else
	    	startAnim((String) animateChoice.getSelectedItem());
	}

	protected void startAnim(String label) {
		System.out.println("StartAnim: "+label);
	    path = AnimationPath.getPathByName(label, 1,dr.srcRect);
	    startAnim();
	}

	private void startAnim() {
	    //if(DEBUG) 
	    	System.out.println("Start anim");
	    
	    path.firstItteration(fd);
	
	    if (animateTask != null)
	        animateTask.cancel();
	
	    myCanvas.requestFocus();
	    setText("Hit space bar to stop");
	    TessRule.tileBackground=true;
	    animateTask = new TimerTask() {
	        long count=0;
	        long sum = 0;
	        //long lastTime = System.currentTimeMillis();
	        //@Override
	        @Override
	        public void run() {
	            long t1 = System.currentTimeMillis();
	            //long diff = (t1-lastTime)/50;
	            //for(long i=count;i<=diff;++i)
	            path.nextItteration(fd);
	            controller.applyTessellation();
	            long t2 = System.currentTimeMillis();
	            long elapsed = t2-t1;
	            sum += elapsed;
	            if(count%10==0) {
	                if(DEBUG) System.out.println("Elapse " + sum);
	                if(DEBUG) System.out.flush();
	                sum=0;
	            }
	            ++count;
	            //lastTime = System.currentTimeMillis();
	        }
	
	    };
	
	    //timer.scheduleAtFixedRate(animateTask, 0, 50);
	    timer.schedule(animateTask, 0, 50);
	    animRunning = true;
	    stopBut.setEnabled(true);
	    stopBut.setText("Stop");
	}

	public void stopAnim() {
	    if(DEBUG) System.out.println("Stop anim");
	    if (animateTask != null)
	        animateTask.cancel();
	    animRunning = false;
	    stopBut.setText("Start");
	}

//	public void actionPerformed(ActionEvent ev) {
//        String com = ev.getActionCommand();
//
//		if(com.startsWith("anim/")) {
//			String label = com.substring(5);
//        //"up","down","left","right","rotate","Stop"
//        if(label.equals("Stop")) {
//            stopAnim();
//        }
//        else startAnim(label);
//
//    }

		
	public void itemStateChanged(ItemEvent ev) {
		
	    path = AnimationPath.getPathByName((String) ev.getItem(), 1,dr.srcRect);
	}


} // end of class def

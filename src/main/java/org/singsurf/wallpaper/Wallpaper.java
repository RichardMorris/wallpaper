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
import java.awt.Toolkit;
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

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.ScrollPaneConstants;

import org.singsurf.wallpaper.animation.AnimationController;
import org.singsurf.wallpaper.animation.AnimationPath;
import org.singsurf.wallpaper.tessrules.TessRule;

/**
An applet which calculates wallpaper patterns
using a portion of an image.
 **/

public class Wallpaper extends JPanel implements MouseListener, MouseMotionListener, KeyListener, ItemListener
{
    private static final long serialVersionUID = 1L;
    private static final boolean DEBUG = false;

    /** Labels and action commands for flip/rotate */
    public static final String FLIP_X = Messages.getString("Menu.image.FlipX"); //$NON-NLS-1$
    public static final String FLIP_Y = Messages.getString("Menu.image.FlipY"); //$NON-NLS-1$
    public static final String FLIP_90 = Messages.getString("Menu.image.RotClock"); //$NON-NLS-1$
    public static final String FLIP_180 = Messages.getString("Menu.image.Rot180"); //$NON-NLS-1$
    public static final String FLIP_270 = Messages.getString("Menu.image.RotAnti"); //$NON-NLS-1$

    /** the current vertex for moving the triangle */
    public int curvertex=0;

    /** Whether we are in interactive mode */
    boolean interactiveMode = true;

    /** Whether to draw the symmetry lines */
    //private boolean symmetryLines = false;

    /** Whether to centerImage */
    //protected boolean centerImage = false;

    /** Offset of image in canvas */
    public Point offset=new Point(0,0);

    protected Color backgroundColour = Color.black;

    public Controller controller;
    public DrawableRegion dr;
    public FundamentalDomain fd;

    JTextArea infoPanel;
    /** whether the paint method has been completed. */
    private boolean paintDone = true;
    /** If interactive kaleidoscope more */
//    private JCheckBox interactiveCB;

    /** Show the fundamental domain **/
    private JCheckBox showFund;

    public JComponent myCanvas;
    // This interface is used for objects defining now to do tessellation 
    public JFrame mainFrame=null;
    public String imageFilename=null;
//    protected URL imageURL=null;

    public int clickCount = 0;
	public AnimationController animController;

    private GraphicalTesselationPanel tesselationPanel;
    JButton origTileButton;
	/** Current path of animations */
//	AnimationPath path;
	/** Button to stop animations */
	public JButton stopBut;
	protected JComboBox<String> animateChoice;
	private JCheckBox symmetryCB;
	private JPanel buttonBar;
	
    /** Mouse mode */
    static final int MOUSE_NORMAL = 0;
    protected static final int MOUSE_PIPET = 1;
    protected int mouseMode = MOUSE_NORMAL;

    private boolean mousePressed = false;
	protected Cursor pipet;
	private JScrollPane infoScroll;

    public Wallpaper(Image img,int w,int h) {
        if(DEBUG) System.out.println("img w "+w+" h "+h); //$NON-NLS-1$ //$NON-NLS-2$

        setLayout(new BorderLayout());
        JPanel pan = new JPanel();
        pan.setLayout(new BorderLayout());
        infoPanel = new JTextArea(
                Messages.getString("Info.Intro"), //$NON-NLS-1$
                3,60);
        //TODO ta.setS,TextArea.SCROLLBARS_VERTICAL_ONLY);
        infoPanel.setEditable(false);
        infoPanel.setLineWrap(true);
        infoPanel.setWrapStyleWord(true);
        infoPanel.setBackground(Color.white); // use lower case colours for compatibility with old jdk
//        infoPanel.setBorder(BorderFactory.createEtchedBorder());
        infoScroll = new JScrollPane(infoPanel,
        		ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED,
        		ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        //ta.setMargin(new Insets(2, 4, 2, 2));
        //ta.set
        myCanvas = buildCanvas();
        JComponent mainWin = buildCanvasContainer(myCanvas);

        controller = new Controller(this,dr,fd);
        tesselationPanel = new GraphicalTesselationPanel(controller);
        buttonBar = buildButtonBar();

        add("West",tesselationPanel); //$NON-NLS-1$
        add("Center",pan); //$NON-NLS-1$
        pan.add("North",buttonBar); //$NON-NLS-1$
        pan.add("Center",mainWin); //$NON-NLS-1$
        pan.add("South",infoScroll); //$NON-NLS-1$
        
        validate();
        doLayout();
        //System.out.println(myCanvas.getBounds());

        dr = buildDrawableRegion();
        if(!dr.loadImage(img)) {
            System.out.println(Messages.getString("Msg.DefaultImage")); //$NON-NLS-1$
            dr.loadImage(DefaultImage.createDefaultImage());
        }
        fd = new FundamentalDomain();
        controller.setDr(dr);
        controller.setFd(fd);
        myCanvas.setSize(dr.destRect.width,dr.destRect.height); 
        myCanvas.setPreferredSize(dr.destRect.getSize()); 
        dr.setViewport(dr.destRect);
        fd.resetDomain(dr.dispRect);
        controller.setTesselation(tesselationPanel.getCurrentTesselation());
        controller.calcGeom();
        controller.showOriginal();

        Toolkit toolkit = Toolkit.getDefaultToolkit();  
        var size = toolkit.getBestCursorSize(32, 32);
        Image img2 = toolkit.getImage(getClass().getResource(Messages.getString("Resource.pipet_img"))); //$NON-NLS-1$
        var mul = size.width/32.0;
        pipet = toolkit.createCustomCursor(img2, new Point((int) (6*mul),(int) (23*mul)), "Custom Cursor");   //$NON-NLS-1$
		
		if(DEBUG) System.out.println(Messages.getString("Msg.InitDone")); //$NON-NLS-1$
    }

	public void hideControls() {
		buttonBar.setVisible(false);
		tesselationPanel.setVisible(false);
		infoScroll.setVisible(false);
	}

	public void showControls() {
		buttonBar.setVisible(true);
		tesselationPanel.setVisible(true);
		infoScroll.setVisible(true);
	}


	public void paintCanvas(Graphics g) {
        if(DEBUG) System.out.println("paintCanvas" + dr.dispRect); //$NON-NLS-1$
        
        //System.out.printf("cp %d %d %d %d %d %d\n",fd.verticies[0].x,fd.verticies[0].y,fd.verticies[1].x,fd.verticies[1].y,fd.verticies[2].x,fd.verticies[2].y);
        //System.out.printf("%d %d%n", offset.x,offset.y);
        g.translate(offset.x,offset.y);
        Rectangle bounds = g.getClipBounds();
        if(bounds != null && (bounds.x + bounds.width > dr.dispRect.x+dr.dispRect.width)) {
            g.clearRect(dr.dispRect.x+dr.dispRect.width, bounds.y,
                    bounds.x + bounds.width - (dr.dispRect.x+dr.dispRect.width), bounds.height);
        }
        if(bounds != null && (bounds.y + bounds.height > dr.dispRect.y+dr.dispRect.height)) {
            g.clearRect(bounds.x,dr.dispRect.y+dr.dispRect.height,
                    bounds.width,bounds.y + bounds.height - (dr.dispRect.y+dr.dispRect.height));
        }
        dr.paint(g,this);
        g.setPaintMode();

        fd.paintSymetries(g, controller.tr);
        fd.paint(g);

        if(clickCount==0)
            paintIntro(g);
//        if(clickCount==1)
//            paintIntro2(g);

        if(controller.constrainVertices)
            fd.paintRegularTile(g);

        g.translate(-offset.x,-offset.y);
        paintDone = true;
    }

    private void paintIntro(Graphics g) {
        Vec base = controller.tr.frameO;
        String s1 = Messages.getString("IntroBox1a"); //$NON-NLS-1$
        String s2 = Messages.getString("IntroBox1b"); //$NON-NLS-1$
        Font f = new Font("SansSerif",Font.BOLD,16); //$NON-NLS-1$
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

    /*
    private void paintIntro2(Graphics g) {
        String s1 = Messages.getString("IntroBox2a"); //$NON-NLS-1$
        String s2 = Messages.getString("IntroBox2b"); //$NON-NLS-1$
        Font f = new Font("SansSerif",Font.BOLD,16); //$NON-NLS-1$
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
*/

    public void mouseEntered(MouseEvent e) {/*ignore*/}
    public void mouseExited(MouseEvent e) {/*ignore*/}
    public void mouseReleased(MouseEvent e) 
    {
        if(DEBUG) System.out.println(Messages.getString("Mouse released")); //$NON-NLS-1$
        mousePressed = false;
        ++clickCount;
        if(clickCount<3) myCanvas.repaint();
    }

    public void mouseClicked(MouseEvent e) 
    {
        if(DEBUG) System.out.println(Messages.getString("Mouse clicked")); //$NON-NLS-1$
        
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

        myCanvas.requestFocus();
    }


    public void mouseMoved(MouseEvent e)
    {
        if(DEBUG) System.out.println(Messages.getString("Mouse moved")); //$NON-NLS-1$

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
        int index = fd.getClosestVertex(x,y);
        if(index!=-1)
            myCanvas.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        else
            myCanvas.setCursor(Cursor.getDefaultCursor());
    }

    public void mousePressed(MouseEvent e)
    {
        if(DEBUG) System.out.println(Messages.getString("Mouse pressed")); //$NON-NLS-1$
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
            if(DEBUG) System.out.println(Messages.getString("clicks")+e.getClickCount()); //$NON-NLS-1$
            // Only recalculate when paint has been completed or 1 sec passed.
            // and 0.1s has passed.
            long curtime = System.currentTimeMillis();
            //System.out.println("t "+(curtime- lasttime)+" pd "+paintDone);
            if(curtime- lasttime <100) return;
            if(!paintDone && curtime- lasttime <1000 ) return;
            lasttime = curtime;
            paintDone = false;
            if(controller.showingOriginal /* || !interactiveMode */ ) {
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
        if(DEBUG) System.out.println(Messages.getString("mouse dragged")+e); //$NON-NLS-1$
        if(!mousePressed) {
            System.out.println("FAKE Event "+e); //$NON-NLS-1$
            return;
        }
        if((e.getModifiersEx() & InputEvent.BUTTON3_DOWN_MASK )!= 0) return;
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

        if(!paintDone ) return;
        //lasttime = curtime;
        paintDone = false;
        controller.applyTessellation();
    }

    protected static boolean first=true;


    protected DrawableRegion buildDrawableRegion() {
        //return new FixedSizeDrawableRegion(this,myCanvas.getSize());
        return new DrawableRegion(this);
    }

    protected JComponent buildCanvas() {
        var canvas = new ScrollablePicture(this);
        canvas.addMouseListener(this);
        canvas.addMouseMotionListener(this);
        canvas.addKeyListener(this);
        return canvas;
    }

    protected JPanel buildButtonBar() {
        JPanel p2 = new JPanel();
        origTileButton = new JButton(Messages.getString("Button.OrigImage")); //$NON-NLS-1$
        origTileButton.addActionListener(e -> controller.flipOriginal());
        p2.add(origTileButton);

        JButton b4 = new JButton(Messages.getString("Button.Reset")); //$NON-NLS-1$
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

        showFund = new JCheckBox(Messages.getString("CheckBox.ShowDomain"),true); //$NON-NLS-1$
        p2.add(showFund);
        showFund.addItemListener(new ItemListener() {
            public void itemStateChanged(ItemEvent e) {
                fd.drawDomain = (e.getStateChange() == ItemEvent.SELECTED);
                myCanvas.repaint();
                setViewCheckboxes();
            }});


        symmetryCB = new JCheckBox(Messages.getString("CheckBox.DrawSymmetry")); //$NON-NLS-1$
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
                setViewCheckboxes();
            }});
        p2.add(symmetryCB);

        p2.add(new JLabel(Messages.getString("Label.Anim"))); //$NON-NLS-1$
        JComboBox<String> animateMenu = buildAnimationChoice();
        p2.add(animateMenu);
        stopBut = new JButton(Messages.getString("Button.Start")); //$NON-NLS-1$
        stopBut.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                animController.stopStartAnim();
            }

        });
        stopBut.setVisible(true);
	    stopBut.setEnabled(true);

        p2.add(stopBut);



        return p2;
    }

    protected void setViewCheckboxes() {
		showFund.setSelected(fd.drawDomain);
		symmetryCB.setSelected(fd.drawGlideLines &&
				fd.drawReflectionLines && fd.drawRotationPoints);

	}

	public void tickCheckbox(String name) {
        tesselationPanel.tickCheckbox(name);
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
        infoPanel.setText(message);
        infoPanel.setCaretPosition(0);
    }


    public static String programInfo() {
        return Messages.getString("Msg.ProgramInfo"); //$NON-NLS-1$

    }

    public static String helpInfo() {
        return Messages.getString("Msg.HelpInfo"); //$NON-NLS-1$
    }


    public void keyPressed(KeyEvent e) {
        int code = e.getKeyCode();
//        System.out.println("WP: Key Pressed: " + code);

        if(code == KeyEvent.VK_LEFT) fd.shift(-1,0);
        else if(code == KeyEvent.VK_RIGHT) fd.shift(1,0);
        else if(code == KeyEvent.VK_UP) fd.shift(0,-1);
        else if(code == KeyEvent.VK_DOWN) fd.shift(0,1);
        else if(code == KeyEvent.VK_W) { fd.shiftVertex(1, 0, -1); curvertex = 1;}
        else if(code == KeyEvent.VK_A) { fd.shiftVertex(1, -1, 0); curvertex = 1; }
        else if(code == KeyEvent.VK_S) { fd.shiftVertex(1, 0, 1);  curvertex = 1; }
        else if(code == KeyEvent.VK_D) { fd.shiftVertex(1, 1, 0);  curvertex = 1; }
        
        else if(code == KeyEvent.VK_T) { fd.shiftVertex(0, 0, -1);  curvertex = 0; }
		else if(code == KeyEvent.VK_F) { fd.shiftVertex(0, -1, 0);  curvertex = 0; }
		else if(code == KeyEvent.VK_G) { fd.shiftVertex(0, 0, 1);  curvertex = 0; }
		else if(code == KeyEvent.VK_H) { fd.shiftVertex(0, 1, 0);  curvertex = 0; }

        else if(code == KeyEvent.VK_I) { fd.shiftVertex(2, 0, -1);  curvertex = 2; }
		else if(code == KeyEvent.VK_J) { fd.shiftVertex(2, -1, 0);  curvertex = 2; }
		else if(code == KeyEvent.VK_K) { fd.shiftVertex(2, 0, 1);  curvertex = 2; }
		else if(code == KeyEvent.VK_L) { fd.shiftVertex(2, 1, 0);  curvertex = 2; }

		else if(code == KeyEvent.VK_O) { controller.flipOriginal(); return; }

		else {
        	return;
        }

        controller.calcGeom();
        if(!interactiveMode)
        {
            myCanvas.repaint();
            return;
        }
        if(!paintDone) return;
        paintDone = false;
        controller.applyTessellation();

    }
    public void keyReleased(KeyEvent e) {/*ignore*/}

    protected JComboBox<String> buildAnimationChoice() {
	    animateChoice = new JComboBox<String>();
	    String animations[] = AnimationPath.getPathNames();	
	    for(int i=0;i<animations.length;++i) {
	        animateChoice.addItem(animations[i]);
	    }
	    animateChoice.setSelectedIndex(0);
	    animateChoice.addItemListener(this);
	
	    return animateChoice;
	}

	public void keyTyped(KeyEvent e) {/*ignore*/}


	protected void startAnim(String label) {
	    var path = AnimationPath.getPathByName(label, 1,dr.srcRect);
	    animController.setAnimationPath(path);
	    animController.startAnim();
	}


	public void itemStateChanged(ItemEvent ev) {
		startAnim((String) ev.getItem());
	}

	protected JComponent buildCanvasContainer(JComponent c) {
		return c;
	}



	public void imageChanged() {
	    myCanvas.setSize(dr.destRect.width, dr.destRect.height);
	    myCanvas.setPreferredSize(dr.destRect.getSize());
	    AnimationPath path = AnimationPath.getPathByName(
	    		animateChoice.getSelectedItem().toString() , 1,dr.destRect);
	    path.firstItteration(fd);
	    this.controller.tr.firstCall=true;
	    animController.setAnimationPath(path);
	    dr.calcDispRegion();
	    controller.redraw();
	    setTitle();
	}

	void setTitle() {
		
	}




} // end of class def

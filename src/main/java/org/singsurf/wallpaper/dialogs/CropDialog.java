/*
Created 7 Apr 2007 - Richard Morris
*/
package org.singsurf.wallpaper.dialogs;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JSpinner;
import javax.swing.SpinnerNumberModel;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import org.singsurf.wallpaper.DrawableRegion;
import org.singsurf.wallpaper.WallpaperFramed;
import org.singsurf.wallpaper.ZoomedDrawableRegion;
import org.singsurf.wallpaper.tessrules.TessRule;

public class CropDialog extends JDialog implements ChangeListener {
	private static final long serialVersionUID = 1L;

	public int width;

    public int height;

    public int xoff;

    public int yoff;

    int right, bottom;
	public boolean ok=false;
	JLabel heading = new JLabel();
	JSpinner xoffSS,yoffSS,rSS,bSS;
	JCheckBox tileCB;
	WallpaperFramed wall;
	public CropDialog(JFrame frame,WallpaperFramed wall) {
		super(frame,"Crop/Expand",true);
		this.setPreferredSize(new Dimension(300,200));
		this.wall = wall;
		GridBagLayout gbl = new GridBagLayout();
		this.setLayout(gbl);
		GridBagConstraints gbc = new GridBagConstraints();
		gbc.gridwidth = 4;
	    gbc.weightx=1;
	    gbc.insets=new Insets(2,2,2,2);
	    gbc.fill = GridBagConstraints.HORIZONTAL;
	        
		gbc.gridx = 0; gbc.gridy = 0; 
		gbc.gridwidth = 4;
		this.add(heading,gbc);
		gbc.gridwidth = 1;

		gbc.gridwidth=4;
		gbc.gridx=0; gbc.gridy++;
		this.add(new JLabel("Crop region"),gbc);
		gbc.gridwidth = 1;
		
		gbc.gridx = 0; gbc.gridy++;
		this.add(new JLabel("Left"), gbc);
		++gbc.gridx;
		xoffSS = new JSpinner(new SpinnerNumberModel(10, null, null, 1)); //hSS.s10,1);
		this.add(xoffSS,gbc);

		gbc.gridx++;
		this.add(new JLabel("Top"), gbc);
		++gbc.gridx;
		yoffSS = new JSpinner(new SpinnerNumberModel(10, null, null, 1));
		this.add(yoffSS,gbc);

		gbc.gridx = 0; gbc.gridy++;
		this.add(new JLabel("Right"), gbc);
		++gbc.gridx;
		rSS = new JSpinner(new SpinnerNumberModel(10, null, null, 1));
		this.add(rSS,gbc);

		++gbc.gridx;
		this.add(new JLabel("Bottom"), gbc);
		++gbc.gridx;
		bSS = new JSpinner(new SpinnerNumberModel(10, null, null, 1));
		this.add(bSS,gbc);

		gbc.gridx = 0; gbc.gridy++;
		tileCB = new JCheckBox("Tile image");
		this.add(tileCB,gbc);
		
		rSS.addChangeListener(this);
		bSS.addChangeListener(this);
		xoffSS.addChangeListener(this);
		yoffSS.addChangeListener(this);

		JButton okBut = new JButton("OK");
		okBut.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent arg0) {
				close(true);
			}});
		JButton cancelBut = new JButton("Cancel");
		cancelBut.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent arg0) {
				close(false);
			}});
		
		gbc.gridx = 1; gbc.gridy++;
		this.add(okBut,gbc);
		++gbc.gridx;
		this.add(cancelBut,gbc);
		
		this.pack();
		this.addWindowListener(new WindowAdapter(){
		    @Override
            public void windowClosing(WindowEvent arg0) {
		        close(false);
		    }});
	}

	public void open(int w,int h) {
		heading.setText("Current size "+w+" X "+h);
		this.width = wall.dr.baseRect.width;
		this.height = wall.dr.baseRect.height;
		rSS.setValue(w);
		bSS.setValue(h);
		xoffSS.setValue(0);
		yoffSS.setValue(0);
		tileCB.setSelected(TessRule.tileBackground);
		this.pack();
		this.setVisible(true);
	}
	void close(boolean flag) {
		if(flag) {
			this.width = (Integer) rSS.getValue() - (Integer) xoffSS.getValue();
			this.height = (Integer) bSS.getValue() - (Integer) yoffSS.getValue();
			this.xoff = (Integer) xoffSS.getValue();
			this.yoff = (Integer) yoffSS.getValue();
			TessRule.tileBackground = tileCB.isSelected();
		}
		this.ok = flag;
		
		setVisible(false);
	}

	public void stateChanged(ChangeEvent ce) {
	    Graphics g = wall.myCanvas.getGraphics();
//		System.out.println("ExpandD " + wall.dr);
//		System.out.println("Canvas " +wall.myCanvas);
		wall.paintCanvas(g);
		clear_background(g,wall.dr);
		g.setColor(Color.black);
		g.setPaintMode();

		int oldx = (Integer) xoffSS.getValue();
		int oldy = (Integer) yoffSS.getValue();
		int oldw = (Integer) rSS.getValue() - (Integer) xoffSS.getValue();
		int oldh = (Integer) bSS.getValue() - (Integer) yoffSS.getValue();
		oldx = (oldx * ((ZoomedDrawableRegion) wall.dr).zoomNumer) / ((ZoomedDrawableRegion) wall.dr).zoomDenom;
		oldy = (oldy * ((ZoomedDrawableRegion) wall.dr).zoomNumer) / ((ZoomedDrawableRegion) wall.dr).zoomDenom;
		oldw = (oldw * ((ZoomedDrawableRegion) wall.dr).zoomNumer) / ((ZoomedDrawableRegion) wall.dr).zoomDenom;
		oldh = (oldh * ((ZoomedDrawableRegion) wall.dr).zoomNumer) / ((ZoomedDrawableRegion) wall.dr).zoomDenom;

		g.drawLine(oldx,oldy,oldx+oldw,oldy);
		g.drawLine(oldx,oldy,oldx,oldy+oldh);
		g.drawLine(oldx+oldw,oldy,oldx+oldw,oldy+oldh);
		g.drawLine(oldx,oldy+oldh,oldx+oldw,oldy+oldh);
	}

	static void clear_background(Graphics g, DrawableRegion dr) {
		g.setColor(Color.white);
		g.fillRect(dr.dispRect.width, 0, 
				dr.viewpointR - dr.dispRect.width,
				dr.viewpointB);
		g.fillRect(0, dr.dispRect.height,
				dr.viewpointR, 
				dr.viewpointB - dr.dispRect.height);
	}


}

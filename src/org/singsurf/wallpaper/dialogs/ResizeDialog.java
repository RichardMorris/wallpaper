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

import org.singsurf.wallpaper.Wallpaper;
import org.singsurf.wallpaper.ZoomedDrawableRegion;
import org.singsurf.wallpaper.tessrules.TessRule;

public class ResizeDialog extends JDialog implements ChangeListener {
	private static final long serialVersionUID = 1L;

	public int width;

    public int height;

    public int xoff;

    public int yoff;

    int right, bottom;
	public boolean ok=false;
	JLabel heading = new JLabel();
	JSpinner wSS,hSS,xoffSS,yoffSS,rSS,bSS;
	JCheckBox tileCB;
	Wallpaper wall;
	public ResizeDialog(JFrame frame,Wallpaper wall) {
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

		gbc.gridx = 0; gbc.gridy++; 
		this.add(new JLabel("Width"), gbc);
		++gbc.gridx;
                wSS = new JSpinner(new SpinnerNumberModel(10, 1, null, 1));
		this.add(wSS,gbc);
		gbc.gridx++;
		this.add(new JLabel("Height"), gbc);
		++gbc.gridx;
                hSS = new JSpinner(new SpinnerNumberModel(10, 1, null, 1)); //hSS.s10,1);
		this.add(hSS,gbc);

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
		
		wSS.addChangeListener(this);
		hSS.addChangeListener(this);
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
		    //@Override
		    @Override
            public void windowClosing(WindowEvent arg0) {
		        close(false);
		    }});
	}

	public void open(int w,int h) {
		heading.setText("Current size "+w+" X "+h);
		this.width = wall.dr.baseRect.width;
		this.height = wall.dr.baseRect.height;
		wSS.setValue(w);
		hSS.setValue(h);
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
			this.width = (Integer) wSS.getValue();
			this.height = (Integer) hSS.getValue();
			this.xoff = (Integer) xoffSS.getValue();
			this.yoff = (Integer) yoffSS.getValue();
			TessRule.tileBackground = tileCB.isSelected();
		}
		this.ok = flag;
		
		setVisible(false);
	}

	public void stateChanged(ChangeEvent ce) {
	    Graphics g = wall.myCanvas.getGraphics();

		wall.paintCanvas(g);
		g.setColor(Color.black);
		g.setPaintMode();

		if(ce.getSource() == xoffSS) {
			wSS.setValue(((Integer)rSS.getValue())-((Integer)xoffSS.getValue()));
		}
		if(ce.getSource() == yoffSS) {
			hSS.setValue(((Integer)bSS.getValue())-((Integer)yoffSS.getValue()));
		}
		if(ce.getSource() == rSS) {
			wSS.setValue(((Integer)rSS.getValue())-((Integer)xoffSS.getValue()));
		}
		if(ce.getSource() == bSS) {
			hSS.setValue(((Integer)bSS.getValue())-((Integer)yoffSS.getValue()));
		}
		if(ce.getSource() == wSS) {
			rSS.setValue(((Integer)wSS.getValue())+((Integer)xoffSS.getValue()));
		}
		if(ce.getSource() == hSS) {
			bSS.setValue(((Integer)hSS.getValue())+((Integer)yoffSS.getValue()));
		}
		int oldx = (Integer) xoffSS.getValue();
		int oldy = (Integer) yoffSS.getValue();
		int oldw = (Integer) wSS.getValue();
		int oldh = (Integer) hSS.getValue();
		oldx = (oldx * ((ZoomedDrawableRegion) wall.dr).zoomNumer) / ((ZoomedDrawableRegion) wall.dr).zoomDenom;
		oldy = (oldy * ((ZoomedDrawableRegion) wall.dr).zoomNumer) / ((ZoomedDrawableRegion) wall.dr).zoomDenom;
		oldw = (oldw * ((ZoomedDrawableRegion) wall.dr).zoomNumer) / ((ZoomedDrawableRegion) wall.dr).zoomDenom;
		oldh = (oldh * ((ZoomedDrawableRegion) wall.dr).zoomNumer) / ((ZoomedDrawableRegion) wall.dr).zoomDenom;

		g.drawLine(oldx,oldy,oldx+oldw,oldy);
		g.drawLine(oldx,oldy,oldx,oldy+oldh);
		g.drawLine(oldx+oldw,oldy,oldx+oldw,oldy+oldh);
		g.drawLine(oldx,oldy+oldh,oldx+oldw,oldy+oldh);
	}


}

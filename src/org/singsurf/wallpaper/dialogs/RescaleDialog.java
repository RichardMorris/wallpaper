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
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
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

public class RescaleDialog extends JDialog implements  ChangeListener {
	private static final long serialVersionUID = 1L;
	int newWidth,newHeight;
	private boolean ok=false;
	JLabel heading = new JLabel();
	JSpinner sxSS;
        JSpinner sySS;
	boolean locked;
	Wallpaper wall;
	public RescaleDialog(JFrame frame,Wallpaper wall) {
		super(frame,"Resize",true);
		this.setPreferredSize(new Dimension(300,200));
		this.wall = wall;
		GridBagLayout gbl = new GridBagLayout();
		this.setLayout(gbl);
		GridBagConstraints gbc = new GridBagConstraints();
		gbc.gridwidth = 2;
		gbc.insets = new Insets(2,2,2,2);
		gbc.weightx=1;
                gbc.fill = GridBagConstraints.HORIZONTAL;

                gbc.gridx = 0; gbc.gridy = 0;
		this.add(heading,gbc);
		gbc.gridwidth = 1;

		gbc.gridx = 0; gbc.gridy++; 
		this.add(new JLabel("Width"), gbc);

		++gbc.gridx;
		sxSS = new JSpinner(new SpinnerNumberModel(10, 1, null, 1));
		this.add(sxSS,gbc);

		gbc.gridx = 0; gbc.gridy++;
		this.add(new JLabel("Height"), gbc);
	
		++gbc.gridx;
		sySS = new JSpinner(new SpinnerNumberModel(10, 1, null, 1));
		this.add(sySS,gbc);
		
		gbc.gridwidth = 2;
		gbc.gridx = 0; gbc.gridy++;
		JCheckBox cb = new JCheckBox("Lock aspect ratio");
		this.add(cb,gbc);
		gbc.gridwidth = 1;
	
		gbc.gridx = 0; gbc.gridy++;
		JButton okBut = new JButton("OK");
		this.add(okBut,gbc);

		++gbc.gridx;
		JButton cancelBut = new JButton("Cancel");
		this.add(cancelBut,gbc);
		
		cb.addItemListener(new ItemListener(){
			public void itemStateChanged(ItemEvent arg0) {
				locked = (arg0.getStateChange()==ItemEvent.SELECTED);
				//sySS.setEditable(!locked);
			}});
		
		okBut.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent arg0) {
				close(true);
			}});
		cancelBut.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent arg0) {
				close(false);
			}});
		
		this.addWindowListener(new WindowAdapter(){
			//@Override
            @Override
            public void windowClosing(WindowEvent arg0) {
				close(false);
			}});
		
		sxSS.addChangeListener(this);
		sySS.addChangeListener(this);
	}

	public void open(int w,int h) {
		heading.setText("Current size "+w+" X "+h);
		this.newWidth = w;
		this.newHeight = h;
		this.drawn =false;
		sxSS.setValue(w);
		sySS.setValue(h);
		this.pack();
		//this.p
		this.setVisible(true);
	}
	void close(boolean flag) {
		if(flag) {
			if(locked) {
				int w = (Integer) sxSS.getValue();
				double scale = ((double) w)/((double) newWidth);
				int h = (int) (newHeight*scale);
				this.newWidth = w;
				this.newHeight = h;
			}
			else {
				this.newWidth = (Integer) sxSS.getValue();
				this.newHeight = (Integer) sySS.getValue();
			}
		}
		this.setOk(flag);
		
		setVisible(false);
	}

	boolean drawn=false;
	int oldx=0,oldy=0;
	public void stateChanged(ChangeEvent ce) {
		Graphics g = wall.myCanvas.getGraphics();
		wall.paintCanvas(g);
		//g.translate(wall.offset.x, wall.offset.y);
		g.setColor(Color.black);
		//g.setXORMode(Color.white);
		g.setPaintMode();
//		if(drawn) {
//			g.drawLine(oldx,0,oldx,oldy);
//			g.drawLine(0,oldy,oldx,oldy);
//		}
		if(locked) {
			oldx = (Integer) sxSS.getValue();
			double scale = ((double) oldx)/((double) newWidth);
			oldy = (int) (newHeight*scale);
			sySS.setValue(oldy);
		}
		else {
			oldx = (Integer) sxSS.getValue();
			oldy = (Integer) sySS.getValue();
		}
		oldx = (oldx * ((ZoomedDrawableRegion) wall.dr).zoomNumer) / ((ZoomedDrawableRegion) wall.dr).zoomDenom;
		oldy = (oldy * ((ZoomedDrawableRegion) wall.dr).zoomNumer) / ((ZoomedDrawableRegion) wall.dr).zoomDenom;
		g.drawLine(oldx,0,oldx,oldy);
		g.drawLine(0,oldy,oldx,oldy);
		//g.drawLine(10,10,oldx,oldy);
		drawn=true;
	}

    public void setOk(boolean ok) {
        this.ok = ok;
    }

    public boolean isOk() {
        return ok;
    }

    public int getNewWidth() {
        return newWidth;
    }

    public int getNewHeight() {
        return newHeight;
    }

}

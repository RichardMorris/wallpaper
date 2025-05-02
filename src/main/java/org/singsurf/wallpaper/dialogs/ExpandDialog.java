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

public class ExpandDialog extends JDialog implements  ChangeListener {
    private static final long serialVersionUID = 1L;

    public int imageWidth;

    public int imageHeight;

    public int xoff;

    public int yoff;

    int right, bottom;
	public boolean ok=false;
	JLabel heading = new JLabel();
	JSpinner wSS, hSS,xoffSS,yoffSS;
	JCheckBox tileCB;
        JCheckBox centerCB;
	Wallpaper wall;
	public ExpandDialog(JFrame frame,Wallpaper wall) {
		super(frame,"Expand",true);
		this.setPreferredSize(new Dimension(300,200));
		this.wall = wall;
		GridBagLayout gbl = new GridBagLayout();
		this.setLayout(gbl);
		GridBagConstraints gbc = new GridBagConstraints();
		gbc.gridwidth = 4;
		gbc.weightx=1;
		gbc.insets=new Insets(2,2,2,2);
		gbc.gridx = 0; gbc.gridy = 0; 
		gbc.gridwidth = 4;
		this.add(heading,gbc);
		gbc.gridwidth = 1;
		gbc.fill = GridBagConstraints.HORIZONTAL;
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
		this.add(new JLabel("Offset"),gbc);
		gbc.gridwidth = 1;
		
		gbc.gridx = 0; gbc.gridy++;
		this.add(new JLabel("Left"), gbc);
		++gbc.gridx;
		xoffSS = new JSpinner(new SpinnerNumberModel(10, null, null, 1));
		this.add(xoffSS,gbc);

		gbc.gridx++;
		this.add(new JLabel("Top"), gbc);
		++gbc.gridx;
		yoffSS = new JSpinner(new SpinnerNumberModel(10, null, null, 1));
		this.add(yoffSS,gbc);


		gbc.gridwidth=2;
		gbc.gridx = 0; gbc.gridy++;
		tileCB = new JCheckBox("Tile image");
		this.add(tileCB,gbc);

		gbc.gridx = 0; gbc.gridy++;
	        centerCB = new JCheckBox("Center image",false);
	        this.add(centerCB,gbc);

		wSS.addChangeListener(this);
		hSS.addChangeListener(this);
		xoffSS.addChangeListener(this);
		yoffSS.addChangeListener(this);

		gbc.gridwidth=2;
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
		
		gbc.gridx = 0; gbc.gridy++;
		this.add(okBut,gbc);
		gbc.gridx+=2;
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
		this.imageWidth = wall.dr.baseRect.width;
		this.imageHeight = wall.dr.baseRect.height;
		wSS.setValue(w);
		hSS.setValue(h);
		xoffSS.setValue(0);
		yoffSS.setValue(0);
		tileCB.setSelected(TessRule.tileBackground);
		this.pack();
		this.setVisible(true);
	}
	void close(boolean flag) {
		if(flag) {
			this.imageWidth = (Integer) wSS.getValue();
			this.imageHeight = (Integer) hSS.getValue();
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
		CropDialog.clear_background(g, wall.dr);
		g.setColor(Color.black);
		g.setPaintMode();
		
		int oldx=(Integer) xoffSS.getValue();
		int oldy=(Integer) yoffSS.getValue();
		int oldw=(Integer) wSS.getValue();
		int oldh=(Integer) hSS.getValue();
		
		if(this.centerCB.isSelected()) {
		    oldx = (oldw-imageWidth)/2;
                    oldy = (oldh-imageHeight)/2;
                    xoffSS.setValue(oldx);
                    yoffSS.setValue(oldy);
		}
		int zoomNumer = ((ZoomedDrawableRegion) wall.dr).zoomNumer;
		int zoomDenom = ((ZoomedDrawableRegion) wall.dr).zoomDenom;
		oldx = (oldx * zoomNumer) / zoomDenom;
		oldy = (oldy * zoomNumer) / zoomDenom;
		oldw = (oldw * zoomNumer) / zoomDenom;
		oldh = (oldh * zoomNumer) / zoomDenom;
		g.drawLine(-oldx, -oldy, -oldx+oldw, -oldy);
		g.drawLine(-oldx, -oldy, -oldx, -oldy+oldh);
		g.drawLine(-oldx+oldw, -oldy, -oldx+oldw, -oldy+oldh);
		g.drawLine(-oldx, -oldy+oldh, -oldx+oldw, -oldy+oldh);
		

	}

}

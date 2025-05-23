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
import java.awt.event.ItemEvent;
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
	boolean locked=true;
	Wallpaper wall;
	private JSpinner pcSS;
	public RescaleDialog(JFrame frame,Wallpaper w) {
		super(frame,"Resize",true);
		setPreferredSize(new Dimension(300,200));
		wall = w;
		GridBagLayout gbl = new GridBagLayout();
		setLayout(gbl);
		GridBagConstraints gbc = new GridBagConstraints();
		gbc.gridwidth = 2;
		gbc.insets = new Insets(2,2,2,2);
		gbc.weightx=1;
                gbc.fill = GridBagConstraints.HORIZONTAL;

                gbc.gridx = 0; gbc.gridy = 0;
		add(heading,gbc);
		gbc.gridwidth = 1;

		gbc.gridx = 0; gbc.gridy++; 
		add(new JLabel("Width"), gbc);

		++gbc.gridx;
		sxSS = new JSpinner(new SpinnerNumberModel(10, 1, null, 1));
		add(sxSS,gbc);
		sxSS.addChangeListener(this);

		gbc.gridx = 0; gbc.gridy++;
		add(new JLabel("Height"), gbc);
	
		++gbc.gridx;
		sySS = new JSpinner(new SpinnerNumberModel(10, 1, null, 1));
		add(sySS,gbc);
		sySS.addChangeListener(this);

		gbc.gridx = 0; gbc.gridy++;
		add(new JLabel("Percent"), gbc);
	
		++gbc.gridx;
		pcSS = new JSpinner(new SpinnerNumberModel(100, 1, null, 1));
		add(pcSS,gbc);
		pcSS.addChangeListener(this);

		gbc.gridwidth = 2;
		gbc.gridx = 0; gbc.gridy++;
		JCheckBox cb = new JCheckBox("Lock aspect ratio",true);
		add(cb,gbc);
		gbc.gridwidth = 1;
		cb.addItemListener(e -> {
			locked = (e.getStateChange()==ItemEvent.SELECTED);
			pcSS.setEnabled(!locked);
		});
	
		gbc.gridx = 0; gbc.gridy++;
		JButton okBut = new JButton("OK");
		add(okBut,gbc);

		++gbc.gridx;
		JButton cancelBut = new JButton("Cancel");
		add(cancelBut,gbc);
		
		
		okBut.addActionListener(e -> close(true));
		cancelBut.addActionListener(e -> close(false));
		
		addWindowListener(new WindowAdapter(){
            @Override
            public void windowClosing(WindowEvent arg0) {
				close(false);
			}});
		
	}

	public void open(int w,int h) {
		heading.setText("Current size "+w+" X "+h);
		newWidth = w;
		newHeight = h;
		drawn =false;
		sxSS.setValue(w);
		sySS.setValue(h);
		pack();
		//p
		setVisible(true);
	}
	void close(boolean flag) {
		if(flag) {
			if(locked) {
				int w = (Integer) sxSS.getValue();
				double scale = ((double) w)/((double) newWidth);
				int h = (int) (newHeight*scale);
				newWidth = w;
				newHeight = h;
			}
			else {
				newWidth = (Integer) sxSS.getValue();
				newHeight = (Integer) sySS.getValue();
			}
		}
		ok = flag;
		
		setVisible(false);
	}

	boolean drawn=false;
	int oldx=0,oldy=0;
	public void stateChanged(ChangeEvent ce) {
		Graphics g = wall.myCanvas.getGraphics();
		wall.paintCanvas(g);
		CropDialog.clear_background(g, wall.dr);
		g.setColor(Color.black);
		g.setPaintMode();
		if(locked) {
			if(ce.getSource() == sxSS) {
				int w = (Integer) sxSS.getValue();
				double scale = ((double) w)/((double) newWidth);
				int h = (int) (newHeight*scale);
				setValue(sySS,h);
				setValue(pcSS,(int) (scale*100.0));
			}
			else if(ce.getSource() == sySS) {
				int h = (Integer) sySS.getValue();
				double scale = ((double) h)/((double) newHeight);
				int w = (int) (newWidth*scale);
				setValue(sxSS,w);
				setValue(pcSS,(int) (scale*100.0));
			}
			else if(ce.getSource() == pcSS) {
				int pc = (Integer) pcSS.getValue();
				double scale = ((double) pc)/100.0;
				int w = (int) (newWidth*scale);
				int h = (int) (newHeight*scale);
				setValue(sxSS,w);
				setValue(sySS,h);
			}
		}
		oldx = (Integer) sxSS.getValue();
		oldy = (Integer) sySS.getValue();
		oldx = (oldx * ((ZoomedDrawableRegion) wall.dr).zoomNumer) / ((ZoomedDrawableRegion) wall.dr).zoomDenom;
		oldy = (oldy * ((ZoomedDrawableRegion) wall.dr).zoomNumer) / ((ZoomedDrawableRegion) wall.dr).zoomDenom;
		g.drawLine(oldx,0,oldx,oldy);
		g.drawLine(0,oldy,oldx,oldy);
		drawn=true;
	}

    private void setValue(JSpinner spinner, int h) {
    	spinner.removeChangeListener(this);
    	spinner.setValue(h);
    	spinner.addChangeListener(this);		
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

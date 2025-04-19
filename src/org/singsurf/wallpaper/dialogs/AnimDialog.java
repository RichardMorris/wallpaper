/*
Created 7 Apr 2007 - Richard Morris
*/
package org.singsurf.wallpaper.dialogs;

import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JSpinner;
import javax.swing.SpinnerNumberModel;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import org.singsurf.wallpaper.Wallpaper;

public class AnimDialog extends JDialog implements ChangeListener {
	private static final long serialVersionUID = 1L;

	public int time;


	public boolean ok=false;
	JLabel heading = new JLabel();
	JSpinner timeSS;
//	JCheckBox repeatCB;
	Wallpaper wall;

	public boolean restart = false;
	public AnimDialog(JFrame frame,Wallpaper wall) {
		super(frame,"Animation Length",true);
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
		gbc.gridwidth = 2;
		this.add(heading,gbc);
		gbc.gridwidth = 1;

		gbc.gridx = 0; gbc.gridy++; 
		this.add(new JLabel("Elapse Time"), gbc);
		++gbc.gridx;
                timeSS = new JSpinner(new SpinnerNumberModel(10, 0, null, 1));
		this.add(timeSS,gbc);
		gbc.gridx++;


//		gbc.gridx = 0; gbc.gridy++;
//		repeatCB = new JCheckBox("Restart",false);
//		this.add(repeatCB,gbc);
//		repeatCB.addItemListener((e) -> {
//				if(repeatCB.isSelected()) {
//					restart = true;
//				} else {
//					restart = false;
//				}
//			});
		
		//timeSS.addChangeListener(this);

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

	public void open() {
		this.pack();
		this.setVisible(true);
	}
	void close(boolean flag) {
		if(flag) {
			this.time = (Integer) timeSS.getValue();
//			this.restart = repeatCB.isSelected();
		}
		this.ok = flag;
		
		setVisible(false);
	}

    public boolean isOk() {
        return ok;
    }

	public void stateChanged(ChangeEvent ce) {

		if(ce.getSource() == timeSS) {
//			timeSS.setValue(((Integer)rSS.getValue())-((Integer)xoffSS.getValue()));
		}
	}


}

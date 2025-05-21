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

import org.singsurf.wallpaper.Wallpaper;

public class AnimDialog extends JDialog  {
	private static final long serialVersionUID = 1L;

	public int time;


	public boolean ok=false;
	JLabel heading = new JLabel("Time animation runs for");
	JSpinner timeSS;
//	JCheckBox repeatCB;
	Wallpaper wall;

	public AnimDialog(JFrame frame,Wallpaper w) {
		super(frame,"Animation time",true);
		setPreferredSize(new Dimension(300,120));
		wall = w;
		GridBagLayout gbl = new GridBagLayout();
		setLayout(gbl);
		GridBagConstraints gbc = new GridBagConstraints();
		gbc.gridwidth = 4;
	    gbc.weightx=1;
	    gbc.insets=new Insets(2,2,2,2);
	    gbc.fill = GridBagConstraints.HORIZONTAL;
	        
		gbc.gridx = 0; gbc.gridy = 0; 
		gbc.gridwidth = 2;
		add(heading,gbc);
		gbc.gridwidth = 1;

		gbc.gridx = 0; gbc.gridy++; 
		add(new JLabel("Elapse Time (s)"), gbc);
		++gbc.gridx;
                timeSS = new JSpinner(new SpinnerNumberModel(30, 0, null, 1));
		add(timeSS,gbc);
		gbc.gridx++;

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
		add(okBut,gbc);
		++gbc.gridx;
		add(cancelBut,gbc);
		
		pack();
		addWindowListener(new WindowAdapter(){
		    @Override
            public void windowClosing(WindowEvent arg0) {
		        close(false);
		    }});
	}

	public void open() {
		pack();
		setVisible(true);
	}
	void close(boolean flag) {
		if(flag) {
			time = (Integer) timeSS.getValue();
		}
		ok = flag;
		
		setVisible(false);
	}

    public boolean isOk() {
        return ok;
    }



}

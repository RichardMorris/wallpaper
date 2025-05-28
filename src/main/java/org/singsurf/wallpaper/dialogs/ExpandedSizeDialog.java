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
import java.text.MessageFormat;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JTextField;

import org.singsurf.wallpaper.Messages;

public class ExpandedSizeDialog extends JDialog {
	private static final long serialVersionUID = 1L;
	int imgWidth,imgHeight;
	private boolean ok=false;
	JLabel heading = new JLabel();
	JTextField wTF,hTF;
	public ExpandedSizeDialog(JFrame frame) {
		super(frame,Messages.getString("Dialog.ExpandedSize.title"),true); //$NON-NLS-1$
		GridBagLayout gbl = new GridBagLayout();
		setPreferredSize(new Dimension(250,160));
		setLayout(gbl);
		GridBagConstraints gbc = new GridBagConstraints();
		gbc.gridwidth = 2;
		gbc.insets=new Insets(2,2,2,2);
		gbc.gridx = 0; gbc.gridy = 0; gbc.gridwidth = 2;
		add(heading,gbc);
		gbc.fill = GridBagConstraints.HORIZONTAL;
		gbc.gridwidth = 1;
		gbc.gridx = 0; gbc.gridy++; 
		add(new JLabel(Messages.getString("Dialog.width")), gbc); //$NON-NLS-1$
		++gbc.gridx;
		wTF = new JTextField(10);
		
		add(wTF,gbc);
		gbc.gridx = 0; gbc.gridy++;
		add(new JLabel(Messages.getString("Dialog.height")), gbc); //$NON-NLS-1$
		++gbc.gridx;
		hTF = new JTextField(10);
		add(hTF,gbc);

		gbc.gridx = 0; gbc.gridy++;
		gbc.gridwidth = 2;
		add(new JLabel(Messages.getString("Dialog.ExpandedSize.only_bmp_ppm")),gbc); //$NON-NLS-1$
		gbc.gridwidth = 1;
		
		JButton okBut = new JButton(Messages.getString("Dialog.OK")); //$NON-NLS-1$
		okBut.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent arg0) {
				close(true);
			}});
		JButton cancelBut = new JButton(Messages.getString("Dialog.Cancel")); //$NON-NLS-1$
		cancelBut.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent arg0) {
				close(false);
			}});
		
		gbc.gridx = 0; gbc.gridy++;
		add(okBut,gbc);
		++gbc.gridx;
		add(cancelBut,gbc);
		
		validate();
		pack();
		addWindowListener(new WindowAdapter(){
		    @Override
            public void windowClosing(WindowEvent arg0) {
		        close(false);
		    }});
	}

	public void open(int w,int h) {
		heading.setText(MessageFormat.format(Messages.getString("Dialog.current_size"),w,h)); //$NON-NLS-1$ //$NON-NLS-2$
		wTF.setText(String.valueOf(w));
		hTF.setText(String.valueOf(h));

		validate();
		setVisible(true);
	}
	void close(boolean flag) {
		if(flag) {
			imgWidth = Integer.parseInt(wTF.getText());
			imgHeight = Integer.parseInt(hTF.getText());
		}
		setOk(flag);
		
		setVisible(false);
	}

    public void setOk(boolean ok1) {
        ok = ok1;
    }

    public boolean isOk() {
        return ok;
    }

    public int getImgWidth() {
       return imgWidth;
    }

    public int getImgHeight() {
        return imgHeight;
     }

}

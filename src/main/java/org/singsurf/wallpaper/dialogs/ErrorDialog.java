/*
Created 7 Apr 2007 - Richard Morris
*/
package org.singsurf.wallpaper.dialogs;

import java.awt.Component;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;

public class ErrorDialog extends JDialog {
	private static final long serialVersionUID = 1L;
	JLabel line1 = new JLabel();
	JLabel line2 = new JLabel();
	public ErrorDialog(JFrame frame,String title) {
		super(frame,title,true);
		setLayout(new GridLayout(3,1,2,2));
		add(line1);
		add(line2);
		JPanel pan = new JPanel(new FlowLayout(FlowLayout.CENTER));
		JButton okBut = new JButton("OK");
		pan.add(okBut);
		add(pan);
		okBut.addActionListener(e -> close(true));
		addWindowListener(new WindowAdapter(){
			@Override
            public void windowClosing(WindowEvent arg0) {
				close(false);
			}});
		pack();
	}
        public ErrorDialog(JFrame frame) {
            this(frame,"Error");
        }

	public void open(String text1,String text2) {
		line1.setText(text1);
		line2.setText(text2);
		pack();
		setVisible(true);
	}
        public void open(String text1) {
            String[] lines = text1.split("[\\r\\n]+");
            line1.setText(lines[0]);
            line2.setText(lines[1]);
            if(lines.length>2) {
                Component comp = getComponent(2);
                setLayout(new GridLayout(lines.length+1,1,2,2));
                for(int i=2;i<lines.length;++i)
                    add(new JLabel(lines[i]),i);
                add(comp,lines.length);
            }
            pack();
            setVisible(true);
        }

        void close(boolean flag) {
		setVisible(false);
	}

}

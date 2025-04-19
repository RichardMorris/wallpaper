/*
Created 1 Jul 2010 - Richard Morris
*/
package org.singsurf.wallpaper.dialogs;

import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JColorChooser;
import javax.swing.JDialog;
import javax.swing.JFrame;

import org.singsurf.wallpaper.Wallpaper;

public class JColourPicker  {
    JDialog jcd;
    
    public JColourPicker(JFrame parent, Wallpaper wallpaperFramed) {
        jColorChooser = new JColorChooser();
        jcd = JColorChooser.createDialog(parent, "Pick background colour", true, 
                jColorChooser, new ActionListener() {

                    public void actionPerformed(ActionEvent e) {
                        ok();
                    }} , 
                    new ActionListener() {

                        public void actionPerformed(ActionEvent e) {
                            cancle();
                            
                        }});
    }

    boolean isOK;
    private final JColorChooser jColorChooser;
    public void ok() {
        isOK = true;
        this.jcd.setVisible(false);
    }
    
    public void cancle() {
        isOK = false;
        this.jcd.setVisible(false);
    }

    public void open(Color backgroundColour) {
        isOK = false;
        this.jcd.setVisible(true);
        
    }

    public boolean isOk() {
        return isOK;
    }

    public Color getCol() {
        return jColorChooser.getColor();
    }
}

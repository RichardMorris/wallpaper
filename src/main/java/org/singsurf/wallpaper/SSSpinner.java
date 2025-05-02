/*
Created 17 Apr 2007 - Richard Morris
*/
package org.singsurf.wallpaper;

import java.awt.AWTEventMulticaster;
import java.awt.Adjustable;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Frame;
import java.awt.Panel;
import java.awt.Scrollbar;
import java.awt.TextField;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.AdjustmentEvent;
import java.awt.event.AdjustmentListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;

public class SSSpinner extends Panel implements Adjustable,AdjustmentListener, ActionListener, FocusListener{
	private static final long serialVersionUID = 1L;
	TextField tf = new TextField(6);
	Scrollbar sb;
	int value;
	int min;
	int max;
	boolean boundedBelow=true;
	boolean boundedAbove=true;
	boolean editable=true;
	public SSSpinner(int value,int min,int max) {
		this.boundedAbove = true;
		this.boundedBelow = true;
		this.min = min;
		this.max = max;
		
		this.setLayout(new BorderLayout());

		sb =  new Scrollbar(Scrollbar.VERTICAL,-value,1,1-max,1-min);
		sb.setVisibleAmount(1);
		sb.addAdjustmentListener(this);
		
		tf.addActionListener(this);
		tf.addFocusListener(this);
		this.add(tf,BorderLayout.CENTER);
		this.add(sb,BorderLayout.EAST);
		setVal(value);
		this.validate();
	}
	public SSSpinner(int value,int min) {
		this(value,min,(value>100?value*2:100));
		this.boundedAbove = false;
		this.boundedBelow = true;
		setVal(value);
	}
	public SSSpinner(int value) {
		this(value,(value<0?value*2:0), (value>100?value*2:100));
		this.boundedAbove = false;
		this.boundedBelow = false;
		setVal(value);
	}

	public void setEditable(boolean b) {
		tf.setEditable(b);
		this.editable = b;
	}

	public void adjustmentValueChanged(AdjustmentEvent e) {
		if(!this.editable) return;
		int val = e.getValue();
		if(!boundedBelow && val > sb.getMaximum() - 5)
			sb.setMaximum(sb.getMaximum()*2 - sb.getMinimum());
		if(!boundedAbove && val < sb.getMinimum() + 5)
			sb.setMinimum(sb.getMinimum()*2 - sb.getMaximum());
		tf.setText(String.valueOf(-val));
		value = -val;
		fire();
	}

	public void actionPerformed(ActionEvent arg0) {
		try {
			value = Integer.parseInt(tf.getText());
		} catch (NumberFormatException e) {
			tf.setText(String.valueOf(value));
		}
		setVal(value);
		fire();
	}

	protected void setVal(int val) {
		if(val<=min) {
			if(boundedBelow)
				{ val=min;  }
			else
				{ min = val -5; sb.setMaximum(1-min); } 
		}
		if(val>=max) {
			if(boundedAbove)
				{ val=max-1; }
			else
				{ max = val +5; sb.setMinimum(1-max); } 
			
		}
		value = val;
		sb.setValue(-value); 
		tf.setText(String.valueOf(value));
	}
	
	//@Override
    public Dimension getPreferredSize()
	{
	  return 
	    new Dimension(sb.getPreferredSize().width +
	           tf.getPreferredSize().width,
	           tf.getPreferredSize().height);
	}
	
	
	protected AdjustmentListener listeners;
	public void addAdjustmentListener(AdjustmentListener l) {
		listeners = AWTEventMulticaster.add(listeners,l);
	}
	public void removeAdjustmentListener(AdjustmentListener l) {
		listeners = AWTEventMulticaster.remove(listeners,l);

	}

	protected void fire() {
		if(listeners!=null)
			listeners.adjustmentValueChanged(
					new AdjustmentEvent(this,
							AdjustmentEvent.ADJUSTMENT_VALUE_CHANGED,
							AdjustmentEvent.TRACK,
							value));
	}
	public int getBlockIncrement() {
		return sb.getBlockIncrement();
	}
	public int getMaximum() {
		return max;
	}
	public int getMinimum() {
		return min;
	}
	public int getOrientation() {
		return Scrollbar.VERTICAL;
	}
	public int getUnitIncrement() {
		return sb.getUnitIncrement();
	}
	public int getValue() {
		return value;
	}
	public int getVisibleAmount() {
		return 1;
	}
	public void setBlockIncrement(int v) {
		sb.setBlockIncrement(v);
	}
	public void setMaximum(int m) {
		sb.setMaximum(1-m);
		max = m;
	}
	public void setMinimum(int m) {
		sb.setMinimum(1-m);
		min = m;
	}
	public void setUnitIncrement(int v) {
		sb.setUnitIncrement(v);
	}
	public void setValue(int val) {
		setVal(val);
	}
	public void setVisibleAmount(int arg0) {/*disable*/ }

	
	public static void main(String[] args) {
		Frame mainFrame = new Frame("Wallpaper patterns");
		mainFrame.setLayout(new FlowLayout());
		SSSpinner sss = new SSSpinner(20);
		sss.addAdjustmentListener(new AdjustmentListener() {
			public void adjustmentValueChanged(AdjustmentEvent e) {
				System.out.println(e.getValue());
			}});
		mainFrame.add(sss);
		mainFrame.add(new SSSpinner(20,10));
		mainFrame.add(new SSSpinner(20,10,100));

		mainFrame.setBounds(0, 0, 200, 200);
		mainFrame.setVisible(true);
	}
	public void focusGained(FocusEvent arg0) {
		// TODO Auto-generated method stub
		
	}
	public void focusLost(FocusEvent arg0) {
		try {
			value = Integer.parseInt(tf.getText());
		} catch (NumberFormatException e) {
			tf.setText(String.valueOf(value));
		}
		setVal(value);
		fire();
		
	}

}

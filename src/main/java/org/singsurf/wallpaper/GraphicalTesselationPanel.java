/*
Created 20 Feb 2009 - Richard Morris
 */
package org.singsurf.wallpaper;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.ItemSelectable;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.net.URL;
import java.util.Vector;

import javax.swing.ButtonGroup;
import javax.swing.ImageIcon;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSlider;
import javax.swing.JToggleButton;

import org.singsurf.wallpaper.tessrules.DiamondRule;
import org.singsurf.wallpaper.tessrules.FrezeRule;
import org.singsurf.wallpaper.tessrules.HexiRule;
import org.singsurf.wallpaper.tessrules.IrregularHexRule;
import org.singsurf.wallpaper.tessrules.PgramRule;
import org.singsurf.wallpaper.tessrules.PointRule;
import org.singsurf.wallpaper.tessrules.RectRule;
import org.singsurf.wallpaper.tessrules.SquRule;
import org.singsurf.wallpaper.tessrules.TessRule;


public class GraphicalTesselationPanel extends JPanel implements ItemListener {

    private static final String FRIEZE_GROUPS = Messages.getString("GTP.frieze"); //$NON-NLS-1$
	private static final String CYCLIC_GROUPS = Messages.getString("GTP.cyclic"); //$NON-NLS-1$
	private static final String BASICS_TRANSFORMATIONS = Messages.getString("GTP.basic"); //$NON-NLS-1$
	private static final String DIHEDRAL_GROUPS = Messages.getString("GTP.dihedral"); //$NON-NLS-1$
	public static final String iconPrefix = Messages.getString("GTP.icon_prefix");  //$NON-NLS-1$
	public static final String iconSuffix = Messages.getString("GTP.icon_suffix");  //$NON-NLS-1$

    final ButtonGroup cbg = new ButtonGroup();
    JComboBox<String> friezeChoice;
    JComboBox<String> cycleChoice;
    JComboBox<String> dyhChoice;
    JComboBox<String> basicChoice;
    Controller cont;

    Vector<GraphicalTesselationBox> allBoxes = new Vector<GraphicalTesselationBox>();
    /**
     * 
     */
    public GraphicalTesselationPanel(Controller controller) {
        this.cont = controller;
        GridBagLayout gbl = new GridBagLayout();
        JPanel p1 = this;
        p1.setLayout(gbl);
        GridBagConstraints gbc = gbl.getConstraints(p1);
        gbc.insets = new Insets(1,1,1,1);
        gbc.anchor = GridBagConstraints.NORTHWEST;
        // controller = new Controller(this,)
        GraphicalTesselationBox TTcb = new GraphicalTesselationBox(PgramRule.rhombusTT,Messages.getString("GTP.p1")); //$NON-NLS-1$
        GraphicalTesselationBox TTAcb = new GraphicalTesselationBox(IrregularHexRule.p1hex,Messages.getString("GTP.p1h"),Messages.getString("GTP.p1h.icon")); //$NON-NLS-1$
        GraphicalTesselationBox R1acb = new GraphicalTesselationBox(PgramRule.rhombusR1,Messages.getString("GTP.p2")); //$NON-NLS-1$
        GraphicalTesselationBox R1cb = new GraphicalTesselationBox(IrregularHexRule.p2hex,Messages.getString("GTP.p2h"),Messages.getString("GTP.p2h.icon")); //$NON-NLS-1$
        GraphicalTesselationBox CMcb = new GraphicalTesselationBox(DiamondRule.rhombCM,Messages.getString("GTP.cm")); //$NON-NLS-1$
        GraphicalTesselationBox CMMcb = new GraphicalTesselationBox(DiamondRule.rhombCMM,Messages.getString("GTP.cmm")); //$NON-NLS-1$
        GraphicalTesselationBox CMrcb = new GraphicalTesselationBox(DiamondRule.rhombCMr,Messages.getString("GTP.cmr"),
        		Messages.getString("GTP.cmr.icon")); //$NON-NLS-1$
        GraphicalTesselationBox CMMrcb = new GraphicalTesselationBox(DiamondRule.rhombCMMr,Messages.getString("GTP.cmmr"),
        		Messages.getString("GTP.cmmr.icon")); //$NON-NLS-1$
        GraphicalTesselationBox PMcb = new GraphicalTesselationBox(RectRule.rectPM,Messages.getString("GTP.pm")); //$NON-NLS-1$
        GraphicalTesselationBox PGcb = new GraphicalTesselationBox(RectRule.rectPG,Messages.getString("GTP.pg")); //$NON-NLS-1$
        GraphicalTesselationBox PMGcb = new GraphicalTesselationBox(RectRule.rectPMG,Messages.getString("GTP.pmg")); //$NON-NLS-1$
        GraphicalTesselationBox PGGcb = new GraphicalTesselationBox(RectRule.rectPGG,Messages.getString("GTP.pgg")); //$NON-NLS-1$
        GraphicalTesselationBox PMMcb = new GraphicalTesselationBox(RectRule.rectPMM,Messages.getString("GTP.pmm")); //$NON-NLS-1$
        GraphicalTesselationBox P4cb = new GraphicalTesselationBox(SquRule.squP4,Messages.getString("GTP.p4")); //$NON-NLS-1$
        GraphicalTesselationBox P4rcb = new GraphicalTesselationBox(SquRule.squP4r,Messages.getString("GTP.p4r"),
 				Messages.getString("GTP.p4r.icon")); //$NON-NLS-1$
        GraphicalTesselationBox P4Gcb = new GraphicalTesselationBox(SquRule.squP4G,Messages.getString("GTP.p4g")); //$NON-NLS-1$
        GraphicalTesselationBox P4Gscb = new GraphicalTesselationBox(SquRule.squP4Gs,Messages.getString("GTP.p4Gs"),
 				Messages.getString("GTP.p4Gs.icon")); //$NON-NLS-1$
        GraphicalTesselationBox P4Mcb = new GraphicalTesselationBox(SquRule.squP4M,Messages.getString("GTP.p4m")); //$NON-NLS-1$
        GraphicalTesselationBox P3cb = new GraphicalTesselationBox(HexiRule.triP3,Messages.getString("GTP.p3")); //$NON-NLS-1$
        GraphicalTesselationBox P3hcb = new GraphicalTesselationBox(HexiRule.triP3h,Messages.getString("GTP.p3h"),
        		Messages.getString("GTP.p3h.icon")); //$NON-NLS-1$
        GraphicalTesselationBox P3M1cb = new GraphicalTesselationBox(HexiRule.triP3m1,Messages.getString("GTP.p3m1")); //$NON-NLS-1$
        GraphicalTesselationBox P31Mcb = new GraphicalTesselationBox(HexiRule.triP31m,Messages.getString("GTP.p31m")); //$NON-NLS-1$
        GraphicalTesselationBox P31Mkcb = new GraphicalTesselationBox(HexiRule.triP31mk,Messages.getString("GTP.p31mk"),Messages.getString("GTP.p31mk.icon")); //$NON-NLS-1$
        GraphicalTesselationBox P31Mtcb = new GraphicalTesselationBox(HexiRule.triP31Mt,Messages.getString("GTP.p31mt"),
        		Messages.getString("GTP.p31mt.icon")); //$NON-NLS-1$
        GraphicalTesselationBox P6cb = new GraphicalTesselationBox(HexiRule.triP6,Messages.getString("GTP.p6")); //$NON-NLS-1$
        GraphicalTesselationBox P6kcb = new GraphicalTesselationBox(HexiRule.triP6k,Messages.getString("GTP.p6k"),
        		Messages.getString("GTP.p6k.icon")); //$NON-NLS-1$
        GraphicalTesselationBox P6Mcb = new GraphicalTesselationBox(HexiRule.triP6m,Messages.getString("GTP.p6m")); //$NON-NLS-1$
        JSlider lambdaSlider = new JSlider(JSlider.HORIZONTAL,
                0, 100, 50);
        lambdaSlider.addChangeListener(e -> 
				cont.setLambda(((JSlider)e.getSource()).getValue()/100.0));
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.gridx = 0; gbc.gridy = 0; 
        
        gbc.gridwidth = 2;
        p1.add(new JLabel(Messages.getString("GTP.title")),gbc); //$NON-NLS-1$
        ++gbc.gridy;
        p1.add(new JLabel(""),gbc); //$NON-NLS-1$
        gbc.gridwidth = 1;
        ++gbc.gridy;
        gbc.gridx = 0;					p1.add(TTcb,gbc);
        ++gbc.gridx;                    p1.add(R1acb,gbc);

        ++gbc.gridy;
        gbc.gridx = 0; 			        p1.add(CMcb,gbc);
        ++gbc.gridx;                    p1.add(CMMcb,gbc);

        ++gbc.gridy;    
        gbc.gridx = 0;                  p1.add(PMcb,gbc);
        ++gbc.gridx;                    p1.add(PGcb,gbc);
                
        ++gbc.gridy;    
        gbc.gridx = 0;					p1.add(PMMcb,gbc);
        ++gbc.gridx; 					p1.add(PMGcb,gbc);
        ++gbc.gridx;		            p1.add(PGGcb,gbc);


        ++gbc.gridy;    
        gbc.gridx = 0;              	p1.add(P4cb,gbc);
        ++gbc.gridx;             		p1.add(P4Mcb,gbc);
        ++gbc.gridx;                    p1.add(P4Gcb,gbc);

        ++gbc.gridy;     
        gbc.gridx = 0;					p1.add(P3cb,gbc);
        ++gbc.gridx;             		p1.add(P3M1cb,gbc);
        ++gbc.gridx;                    p1.add(P31Mcb,gbc);
        
        ++gbc.gridy;    
        gbc.gridx = 0;					p1.add(P6cb,gbc);
        ++gbc.gridx; 					p1.add(P6Mcb,gbc);

        friezeChoice = new JComboBox<String>();
        String descript[] = {Messages.getString("GTP.F1.code"),Messages.getString("GTP.F2.code"),Messages.getString("GTP.F3.code"),Messages.getString("GTP.F4.code"),Messages.getString("GTP.F5.code"),Messages.getString("GTP.F6.code"),Messages.getString("GTP.F7.code")}; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$ //$NON-NLS-6$ //$NON-NLS-7$
        friezeChoice.addItem(FRIEZE_GROUPS);
        for(int i=1;i<=7;++i) {
            friezeChoice.addItem(Messages.getString("GTP.F.prefix")+i+descript[i-1]); //$NON-NLS-1$
        }
        friezeChoice.addItemListener(this);

        gbc.gridx = 0; ++gbc.gridy;     gbc.gridwidth = 3; 
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(1,1,1,1);
        p1.add(friezeChoice,gbc);

        cycleChoice = new JComboBox<String>();
        cycleChoice.addItem(CYCLIC_GROUPS);
        for(int i=2;i<=Messages.getInt("GTP.C.max");++i) {
            String label = Messages.getString("GTP.C.prefix") + i; //$NON-NLS-1$
            cycleChoice.addItem(label);
        }
        cycleChoice.addItemListener(this);

        gbc.gridx = 0; ++gbc.gridy;     gbc.gridwidth = 3;
        p1.add(cycleChoice,gbc);

        dyhChoice = new JComboBox<String>();
        dyhChoice.addItem(DIHEDRAL_GROUPS);
        for(int i=1;i<=Messages.getInt("GTP.D.max");++i) {
            String label = Messages.getString("GTP.D.prefix") + i; //$NON-NLS-1$
            if(i==1) label = label + Messages.getString("GTP.D1.code"); //$NON-NLS-1$
            dyhChoice.addItem(label);
        }
        dyhChoice.addItemListener(this);

        gbc.gridx = 0; ++gbc.gridy;     gbc.gridwidth = 3;
        p1.add(dyhChoice,gbc);

        basicChoice = new JComboBox<String>();
        basicChoice.addItem(BASICS_TRANSFORMATIONS);
        for(int i=0;i<TessRule.basicNames.length;++i) {
            basicChoice.addItem(TessRule.basicNames[i]);
        }
        basicChoice.addItemListener(this);

        gbc.gridx = 0; ++gbc.gridy;     gbc.gridwidth = 3;
        p1.add(basicChoice,gbc);
        
        gbc.gridwidth = 2;
        ++gbc.gridy;    
        p1.add(new JLabel(""),gbc); //$NON-NLS-1$
        ++gbc.gridy;    
        gbc.gridx = 0;	
        p1.add(new JLabel(Messages.getString("GTP.alternate_domains")),gbc); //$NON-NLS-1$
        gbc.gridwidth = 1;
        
        ++gbc.gridy;    
        gbc.gridx = 0;					p1.add(TTAcb,gbc);
        ++gbc.gridx; 					p1.add(R1cb,gbc);
        ++gbc.gridx; 					p1.add(P3hcb,gbc);

        ++gbc.gridy;    
        gbc.gridx = 0;					p1.add(P31Mtcb,gbc);
        ++gbc.gridx; 					p1.add(P31Mkcb,gbc);
        ++gbc.gridx; 					p1.add(P6kcb,gbc);

        ++gbc.gridy;    
        gbc.gridx = 0;					p1.add(CMrcb,gbc);
        ++gbc.gridx; 					p1.add(CMMrcb,gbc);

        ++gbc.gridy;    
        gbc.gridx = 0;					p1.add(P4rcb,gbc);
        ++gbc.gridx; 					p1.add(P4Gscb,gbc);

//        gbc.gridwidth = 3;
//        ++gbc.gridy;    
//        gbc.gridx = 0;					p1.add(new JLabel(Messages.getString("GTP.lambda_param")),gbc);
//        ++gbc.gridy;    
//        gbc.gridx = 0; 					p1.add(lambdaSlider,gbc);
//        gbc.gridwidth = 1;

        ++gbc.gridy; gbc.gridx=0;
        gbc.fill = GridBagConstraints.BOTH;
        gbc.weighty=10.0;
        p1.add(new JLabel(""),gbc); //$NON-NLS-1$
    }

    boolean recurse=false;
    public void itemStateChanged(ItemEvent e) {
    	if(recurse)
    		return;
        ItemSelectable sel = e.getItemSelectable();
        String label;
            label = (String) ((JComboBox<?>) sel).getSelectedItem();
        if( label == FRIEZE_GROUPS || label == CYCLIC_GROUPS || label == DIHEDRAL_GROUPS || label == BASICS_TRANSFORMATIONS) {
			return;
		}
        TessRule currentTr = null;
        if(Character.isDigit(label.charAt(1)))
        {
            int num = Integer.parseInt(label.substring(1,
                    label.length()>2 &&Character.isDigit(label.charAt(2)) ? 3 : 2));
            if(label.startsWith(Messages.getString("GTP.C.prefix"))) { //$NON-NLS-1$
                currentTr = PointRule.cycleRules[num];
            }
            else if(label.startsWith(Messages.getString("GTP.D.prefix"))) { //$NON-NLS-1$
                currentTr = PointRule.dyhRules[num];
            }
            else if(label.startsWith(Messages.getString("GTP.F.prefix"))) { //$NON-NLS-1$
                switch(num) {
                case 1: currentTr = FrezeRule.F1; break;
                case 2: currentTr = FrezeRule.F2; break;
                case 3: currentTr = FrezeRule.F3; break;
                case 4: currentTr = FrezeRule.F4; break;
                case 5: currentTr = FrezeRule.F5; break;
                case 6: currentTr = FrezeRule.F6; break;
                case 7: currentTr = FrezeRule.F7; break;
                default:
                    return;
                }
            }
        } 
        else
        {
            currentTr = TessRule.getTessRuleByName(label);
            if(currentTr==null)
                return;
        }
        setTesselation(currentTr);
    }

    private static final long serialVersionUID = 1L;


    public void tickCheckbox(String name) {
        for(GraphicalTesselationBox tb: allBoxes) {
            if(name.equalsIgnoreCase(tb.getTessName())) {
            	setSelected(tb,true);
            	clearChoice(friezeChoice);
            	clearChoice(cycleChoice);
            	clearChoice(basicChoice);
            	clearChoice(dyhChoice);
                return;
            }
        }
        // Not a GTB so clear selection
        cbg.clearSelection();
        if(Character.isDigit(name.charAt(1)))
        {
            int num = Integer.parseInt(name.substring(1,
                    name.length()>2 &&Character.isDigit(name.charAt(2)) ? 3 : 2));
            if(name.startsWith(Messages.getString("GTP.C.prefix"))) { //$NON-NLS-1$
                setChoice(cycleChoice,num-1);
                dyhChoice.setSelectedIndex(0);
                friezeChoice.setSelectedIndex(0);
                basicChoice.setSelectedIndex(0);
            }
            else if(name.startsWith(Messages.getString("GTP.D.prefix"))) { //$NON-NLS-1$
            	setChoice(dyhChoice,num);
                clearChoice(cycleChoice);
                clearChoice(basicChoice);
                clearChoice(friezeChoice);
            }
            else if(name.startsWith(Messages.getString("GTP.F.prefix"))) { //$NON-NLS-1$
                clearChoice(dyhChoice);
                clearChoice(cycleChoice);
                clearChoice(basicChoice);
                setChoice(friezeChoice,num);
            }
        }
        else {
            for(int i=1;i<basicChoice.getItemCount();++i)
            {
                if(name.equals(basicChoice.getItemAt(i))) {
                    setChoice(basicChoice,i);
                    clearChoice(dyhChoice);
                    clearChoice(cycleChoice);
                    clearChoice(friezeChoice);
                }
            }
        }
        return;
    }


    
    private void setSelected(GraphicalTesselationBox tb, boolean b) {
    	tb.setSelected(b);	
	}


	private void setChoice(JComboBox<String> choice, int i) {
		recurse = true;
    	choice.setSelectedIndex(i);
    	recurse = false;
	}


	private void clearChoice(JComboBox<String> choice) {
		recurse = true;
    	choice.setSelectedIndex(0);
		recurse = false;
	}


	/**
     * Creates an ImageIcon if the path is valid.
     * @param String - resource path
     * @param String - description of the file
     */
    static protected ImageIcon createImageIcon(String path,
    		String description) {
    	URL imgURL = GraphicalTesselationPanel.class.getResource(path);
    	if (imgURL != null) {
    		return new ImageIcon(imgURL, description);
    	} else {
    		System.err.println(Messages.getString("GTP.msg.could_not_find_resource") + path); //$NON-NLS-1$
    		return new ImageIcon(path, description);
    	}
    }

    void setTesselation(TessRule tr) {
    	cont.firstAction();
    	cont.setTesselation(tr);
    	cont.calcGeom();
    	cont.redraw();
    }


	class GraphicalTesselationBox extends JToggleButton implements ActionListener {
        TessRule tr;
        /**
		 * Creates a button with the given TessRule button and name.
		 * The {@code name} is used for the icon file name.
		 * @param tr - the TessRule associated with this button
		 * @param name - the name of the icon file (without prefix/suffix)
		 */
        public GraphicalTesselationBox(TessRule tr, String name) {
        	this(tr, name, name);
        }
        /**
		 * Creates a button with the given TessRule button  and icon name.
		 * @param tr1 - the TessRule associated with this button
		 * @param name - the name of the button
		 * @param iconName - the name of the icon file (without prefix/suffix)
		 */
        public GraphicalTesselationBox(TessRule tr1, String name, String iconName) {
            super(name);
            String iconFileName = iconPrefix + iconName + iconSuffix;
            ImageIcon icon = GraphicalTesselationPanel.createImageIcon(iconFileName,name);
            setIcon(icon);
            setMargin(new Insets(0,0,0,0));
            setBorderPainted(true);
            setToolTipText(name);
            setVerticalTextPosition(CENTER);
            setHorizontalTextPosition(RIGHT);
            setHorizontalAlignment(LEFT);
            tr = tr1;
            addActionListener(this);
            allBoxes.add(this);
            cbg.add(this);
        }

        public String getTessName() {
            return tr.name;
        }
        
        private static final long serialVersionUID = 1L;
        public void actionPerformed(ActionEvent arg0) {
            setTesselation(tr);
        }

    }

}

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
import java.util.Vector;

import javax.swing.ButtonGroup;
import javax.swing.ImageIcon;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
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

    final ButtonGroup cbg = new ButtonGroup();
    JComboBox<String> friezeChoice;
    JComboBox<String> cycleChoice;
    JComboBox<String> dyhChoice;
    JComboBox<String> basicChoice;
    public static final String iconPrefix = "patternIcons/"; 
    public static final String iconSuffix = "S.png"; 
    Controller cont;
    GraphicalTesselationBox currentGTB = null;    

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
        System.out.println(System.getProperty("user.dir"));
        // controller = new Controller(this,)
        GraphicalTesselationBox TTcb = new GraphicalTesselationBox(PgramRule.rhombusTT,"p1");
        GraphicalTesselationBox R1acb = new GraphicalTesselationBox(PgramRule.rhombusR1,"p2a");
        GraphicalTesselationBox R1cb = new GraphicalTesselationBox(IrregularHexRule.p2hex,"p2");
        GraphicalTesselationBox CMcb = new GraphicalTesselationBox(DiamondRule.rhombCM,"cm");
        GraphicalTesselationBox CMMcb = new GraphicalTesselationBox(DiamondRule.rhombCMM,"cmm");
        GraphicalTesselationBox PMcb = new GraphicalTesselationBox(RectRule.rectPM,"pm");
        GraphicalTesselationBox PGcb = new GraphicalTesselationBox(RectRule.rectPG,"pg");
        GraphicalTesselationBox PMGcb = new GraphicalTesselationBox(RectRule.rectPMG,"pmg");
        GraphicalTesselationBox PGGcb = new GraphicalTesselationBox(RectRule.rectPGG,"pgg");
        GraphicalTesselationBox PMMcb = new GraphicalTesselationBox(RectRule.rectPMM,"pmm");
        GraphicalTesselationBox P4cb = new GraphicalTesselationBox(SquRule.squP4,"p4");
        GraphicalTesselationBox P4Gcb = new GraphicalTesselationBox(SquRule.squP4g,"p4g");
        GraphicalTesselationBox P4Mcb = new GraphicalTesselationBox(SquRule.squP4m,"p4m");
        GraphicalTesselationBox P3cb = new GraphicalTesselationBox(HexiRule.triP3,"p3");
        GraphicalTesselationBox P31Mcb = new GraphicalTesselationBox(HexiRule.triP31m,"p31m");
        GraphicalTesselationBox P3M1cb = new GraphicalTesselationBox(HexiRule.triP3m1,"p3m1");
        GraphicalTesselationBox P6cb = new GraphicalTesselationBox(HexiRule.triP6,"p6");
        GraphicalTesselationBox P6Mcb = new GraphicalTesselationBox(HexiRule.triP6m,"p6m");

        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.gridx = 0; gbc.gridy = 0; 
        
        gbc.gridwidth = 1;
        ++gbc.gridy;
        gbc.gridx = 0;					p1.add(TTcb,gbc);
        //++gbc.gridx;                    p1.add(R1cb,gbc);
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
        String descript[] = {"Frieze groups"," - pppp"," - pbpb"," - cccc"," - pqpq"," - pdpd"," - pdbq"," - xxxx"};
        friezeChoice.addItem(descript[0]);
        for(int i=1;i<=7;++i) {
            friezeChoice.addItem("F"+i+descript[i]);
        }
        friezeChoice.addItemListener(this);

        gbc.gridx = 0; ++gbc.gridy;     gbc.gridwidth = 3; 
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(1,1,1,1);
        p1.add(friezeChoice,gbc);

        cycleChoice = new JComboBox<String>();
        cycleChoice.addItem("Cyclic groups");
        for(int i=2;i<11;++i) {
            String label = "C" + i;
            cycleChoice.addItem(label);
        }
        cycleChoice.addItemListener(this);

        gbc.gridx = 0; ++gbc.gridy;     gbc.gridwidth = 3;
        p1.add(cycleChoice,gbc);

        dyhChoice = new JComboBox<String>();
        dyhChoice.addItem("Dihedral groups");
        for(int i=1;i<11;++i) {
            String label = "D" + i;
            if(i==1) label = label + "- a reflection";
            dyhChoice.addItem(label);
        }
        dyhChoice.addItemListener(this);

        gbc.gridx = 0; ++gbc.gridy;     gbc.gridwidth = 3;
        p1.add(dyhChoice,gbc);

        basicChoice = new JComboBox<String>();
        basicChoice.addItem("Basics transformations");
        for(int i=0;i<TessRule.basicNames.length;++i) {
            basicChoice.addItem(TessRule.basicNames[i]);
        }
        basicChoice.addItemListener(this);

        gbc.gridx = 0; ++gbc.gridy;     gbc.gridwidth = 3;
        p1.add(basicChoice,gbc);

        ++gbc.gridy; gbc.gridx=0;
        gbc.fill = GridBagConstraints.BOTH;
        gbc.weighty=10.0;
        p1.add(new JLabel(""),gbc);

        // done layout


        int rand = (int) (Math.random() * 17);
               //rand = 5;
        GraphicalTesselationBox box = null;
        switch (rand) {
        case 0:
            box = TTcb;
            break;
        case 1:
            box = R1cb;
            break;
        case 2:
            box = CMcb;
            break;
        case 3:
            box = CMMcb;
            break;
        case 4:
            box = PMcb;
            break;
        case 5:
            box = PGcb;
            break;
        case 6:
            box = PMGcb;
            break;
        case 7:
            box = PGGcb;
            break;
        case 8:
            box = PMMcb;
            break;
        case 9:
            box = P4cb;
            break;
        case 10:
            box = P4Gcb;
            break;
        case 11:
            box = P4Mcb;
            break;
        case 12:
            box = P3cb;
            break;
        case 13:
            box = P31Mcb;
            break;
        case 14:
            box = P3M1cb;
            break;
        case 15:
            box = P6cb;
            break;
        case 16:
            box = P6Mcb;
            break;
        }
        this.currentTr = box.tr;
        cbg.setSelected(box.getModel(), true);
        this.currentGTB = box;
    }

    //@Override
    public void itemStateChanged(ItemEvent e) {
        ItemSelectable sel = e.getItemSelectable();
        String label;
            label = (String) ((JComboBox<?>) sel).getSelectedItem();
        currentTr = null;
        if(Character.isDigit(label.charAt(1)))
        {
            int num = Integer.parseInt(label.substring(1,
                    label.length()>2 &&Character.isDigit(label.charAt(2)) ? 3 : 2));
            if(label.startsWith("C")) {
                currentTr = PointRule.cycleRules[num];
                friezeChoice.setSelectedIndex(0);
                dyhChoice.setSelectedIndex(0);
                basicChoice.setSelectedIndex(0);
            }
            else if(label.startsWith("D")) {
                currentTr = PointRule.dyhRules[num];
                friezeChoice.setSelectedIndex(0);
                cycleChoice.setSelectedIndex(0);
                basicChoice.setSelectedIndex(0);
            }
            else if(label.startsWith("F")) {
                dyhChoice.setSelectedIndex(0);
                cycleChoice.setSelectedIndex(0);
                basicChoice.setSelectedIndex(0);

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
            //friezeChoice.setSelectedIndex(0);
            //cycleChoice.setSelectedIndex(0);
            //dyhChoice.setSelectedIndex(0);

            currentTr = TessRule.getTessRuleByName(label);
            if(currentTr==null)
                return;
        }
        cbg.setSelected(cbg.getSelection(), false);
        currentGTB.setSelected(false);
        cont.setText(currentTr.message);
        cont.setTesselation(currentTr);
        //                      if(!TessRule.this.wallpaper.accumeMode.getState())
        //                              System.arraycopy(TessRule.this.wallpaper.inpixels,0,TessRule.this.wallpaper.pixels,0,this.wallpaper.inpixels.length);
        //                      TessRule.this.fixVerticies(vertexX,vertexY);
        cont.applyTessellation();
        currentTr.firstCall=false;
        cont.repaint();

    }

    private static final long serialVersionUID = 1L;
    TessRule currentTr;

    public TessRule getCurrentTesselation() {
        return currentTr;
    }

    public void tickCheckbox(String name) {
        for(int i=0;i<allBoxes.size();++i) {
            GraphicalTesselationBox tb = allBoxes.elementAt(i);
            if(name.equals(tb.getTessName())) {
                //TODO cbg.setSelectedCheckbox(tb);
                currentTr = tb.tr;
                friezeChoice.setSelectedIndex(0);
                cycleChoice.setSelectedIndex(0);
                basicChoice.setSelectedIndex(0);
                dyhChoice.setSelectedIndex(0);
                return;
            }
        }
        if(Character.isDigit(name.charAt(1)))
        {
            int num = Integer.parseInt(name.substring(1,
                    name.length()>2 &&Character.isDigit(name.charAt(2)) ? 3 : 2));
            if(name.startsWith("C")) {
                currentTr = PointRule.cycleRules[num];
                cycleChoice.setSelectedIndex(num-1);
                dyhChoice.setSelectedIndex(0);
                friezeChoice.setSelectedIndex(0);
                basicChoice.setSelectedIndex(0);
                cbg.setSelected(cbg.getSelection(), false);
            }
            else if(name.startsWith("D")) {
                currentTr = PointRule.dyhRules[num];
                cycleChoice.setSelectedIndex(0);
                dyhChoice.setSelectedIndex(num);
                friezeChoice.setSelectedIndex(0);
                basicChoice.setSelectedIndex(0);
                cbg.setSelected(cbg.getSelection(), false);
            }
            else if(name.startsWith("F")) {
                dyhChoice.setSelectedIndex(0);
                cycleChoice.setSelectedIndex(0);
                basicChoice.setSelectedIndex(0);
                friezeChoice.setSelectedIndex(num);

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
                cbg.setSelected(cbg.getSelection(), false);
            }
        }
        else {
            for(int i=1;i<basicChoice.getItemCount();++i)
            {
                if(name.equals(basicChoice.getItemAt(i))) {
                    basicChoice.setSelectedIndex(i);
                    currentTr = TessRule.getTessRuleByName(name);
                    dyhChoice.setSelectedIndex(0);
                    cycleChoice.setSelectedIndex(0);
                    friezeChoice.setSelectedIndex(0);
                    cbg.setSelected(cbg.getSelection(), false);
                }
            }
        }
        return;
    }


    
    /**
	         * Creates an ImageIcon if the path is valid.
	         * @param String - resource path
	         * @param String - description of the file
	         */
	        static protected ImageIcon createImageIcon(String path,
	                String description) {
	            java.net.URL imgURL = GraphicalTesselationPanel.class.getResource(path);
	            if (imgURL != null) {
	            	return new ImageIcon(imgURL, description);
	            } else {
	//                System.err.println("Couldn't find file: " + path);
	                return new ImageIcon(path, description);
	            }
	        }



	class GraphicalTesselationBox extends JToggleButton implements ActionListener {
        TessRule tr;
        public GraphicalTesselationBox(TessRule tr, String iconName) {
            super(iconName);
            String iconFileName = iconPrefix + iconName + iconSuffix;
            ImageIcon icon = GraphicalTesselationPanel.createImageIcon(iconFileName,iconName);
            this.setIcon(icon);
            this.setMargin(new Insets(0,0,0,0));
            this.setBorderPainted(true);
            //this.setBorder(BorderFactory.createEtchedBorder());
            this.setToolTipText(iconName);
            this.setVerticalTextPosition(CENTER);
            this.setHorizontalTextPosition(RIGHT);
            this.setHorizontalAlignment(LEFT);
            this.tr = tr;
            //this.addItemListener(this);
            this.addActionListener(this);
            allBoxes.add(this);
            cbg.add(this);
        }

        public Object getTessName() {
            return tr.name;
        }

        public void itemStateChanged(ItemEvent e) {
            if(e.getStateChange() == ItemEvent.SELECTED)
            {
                currentTr = tr;
                tr.firstCall=true;
                cont.setText(tr.message);
                cont.setTesselation(tr);
                //                  if(!TessRule.this.wallpaper.accumeMode.getState())
                //                          System.arraycopy(TessRule.this.wallpaper.inpixels,0,TessRule.this.wallpaper.pixels,0,this.wallpaper.inpixels.length);
                //                  TessRule.this.fixVerticies(vertexX,vertexY);
                cont.applyTessellation();
                tr.firstCall=false;
                cont.wallpaper.clickCount++;
                friezeChoice.setSelectedIndex(0);
                cycleChoice.setSelectedIndex(0);
                dyhChoice.setSelectedIndex(0);
                cont.repaint();
            }
        }
        
        
        private static final long serialVersionUID = 1L;
        public void actionPerformed(ActionEvent arg0) {
            currentTr = tr;
            currentGTB = this;
            tr.firstCall=true;
            cont.setText(tr.name + ": " + tr.message);
            cont.setTesselation(tr);
            //                  if(!TessRule.this.wallpaper.accumeMode.getState())
            //                          System.arraycopy(TessRule.this.wallpaper.inpixels,0,TessRule.this.wallpaper.pixels,0,this.wallpaper.inpixels.length);
            //                  TessRule.this.fixVerticies(vertexX,vertexY);
            cont.applyTessellation();
            tr.firstCall=false;
            cont.wallpaper.clickCount++;
            friezeChoice.setSelectedIndex(0);
            cycleChoice.setSelectedIndex(0);
            dyhChoice.setSelectedIndex(0);
            cont.repaint();
 
        }

    }

}

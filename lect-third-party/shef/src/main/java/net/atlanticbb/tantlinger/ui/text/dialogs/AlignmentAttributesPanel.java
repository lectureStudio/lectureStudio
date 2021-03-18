/*
 * Created on Dec 21, 2005
 *
 */
package net.atlanticbb.tantlinger.ui.text.dialogs;


import java.awt.GridBagLayout;
import javax.swing.JCheckBox;
import java.awt.GridBagConstraints;
import javax.swing.JComboBox;
import java.util.*;

/**
 *  A panel for editing table alignment attributes
 * 
 * @author Bob Tantlinger
 *
 */
public class AlignmentAttributesPanel extends HTMLAttributeEditorPanel
{
    /**
     * 
     */
    private static final long serialVersionUID = 1L;
    private static final String VERT_ALIGNMENTS[] = {"top", "middle", "bottom"}; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
    private static final String HORIZ_ALIGNMENTS[] =
    {
        "left", "center", "right", "justify" //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
    };
    
    
    private JCheckBox vAlignCB = null;
    private JCheckBox hAlignCB = null;
    private JComboBox vLocCombo = null;
    private JComboBox hLocCombo = null;   
        

    public AlignmentAttributesPanel()
    {        
        this(new Hashtable());
    }   
    
    public AlignmentAttributesPanel(Hashtable attr)
    {
        super(attr);        
        initialize();
        updateComponentsFromAttribs();
    }
    
    public void updateComponentsFromAttribs()
    {
        if(attribs.containsKey("align")) //$NON-NLS-1$
        {
            hAlignCB.setSelected(true);
            hLocCombo.setEnabled(true);
            hLocCombo.setSelectedItem(attribs.get("align")); //$NON-NLS-1$
        }
        else
        {
            hAlignCB.setSelected(false);
            hLocCombo.setEnabled(false);
        }
        
        if(attribs.containsKey("valign")) //$NON-NLS-1$
        {
            vAlignCB.setSelected(true);
            vLocCombo.setEnabled(true);
            vLocCombo.setSelectedItem(attribs.get("valign")); //$NON-NLS-1$
        }
        else
        {
            vAlignCB.setSelected(false);
            vLocCombo.setEnabled(false);
        }
    }
    
    
    public void updateAttribsFromComponents()
    {
        if(vAlignCB.isSelected())
            attribs.put("valign", vLocCombo.getSelectedItem().toString()); //$NON-NLS-1$
        else
            attribs.remove("valign"); //$NON-NLS-1$
        
        if(hAlignCB.isSelected())
            attribs.put("align", hLocCombo.getSelectedItem().toString()); //$NON-NLS-1$
        else
            attribs.remove("align"); //$NON-NLS-1$
    }
    
    public void setComponentStates(Hashtable attribs)
    {
        if(attribs.containsKey("align")) //$NON-NLS-1$
        {
            hAlignCB.setSelected(true);
            hLocCombo.setEnabled(true);
            hLocCombo.setSelectedItem(attribs.get("align")); //$NON-NLS-1$
        }
        else
        {
            hAlignCB.setSelected(false);
            hLocCombo.setEnabled(false);
        }
        
        if(attribs.containsKey("valign")) //$NON-NLS-1$
        {
            vAlignCB.setSelected(true);
            vLocCombo.setEnabled(true);
            vLocCombo.setSelectedItem(attribs.get("valign")); //$NON-NLS-1$
        }
        else
        {
            vAlignCB.setSelected(false);
            vLocCombo.setEnabled(false);
        }
    }
    

    /**
     * This method initializes this
     * 
     * @return void
     */
    private void initialize()
    {
        GridBagConstraints gridBagConstraints3 = new GridBagConstraints();
        gridBagConstraints3.fill = java.awt.GridBagConstraints.NONE;
        gridBagConstraints3.gridy = 1;
        gridBagConstraints3.weightx = 1.0;
        gridBagConstraints3.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints3.gridx = 1;
        GridBagConstraints gridBagConstraints2 = new GridBagConstraints();
        gridBagConstraints2.fill = java.awt.GridBagConstraints.NONE;
        gridBagConstraints2.gridy = 0;
        gridBagConstraints2.weightx = 1.0;
        gridBagConstraints2.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints2.insets = new java.awt.Insets(0,0,5,0);
        gridBagConstraints2.gridx = 1;
        GridBagConstraints gridBagConstraints1 = new GridBagConstraints();
        gridBagConstraints1.gridx = 0;
        gridBagConstraints1.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints1.insets = new java.awt.Insets(0,0,0,5);
        gridBagConstraints1.gridy = 1;
        GridBagConstraints gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(0,0,5,5);
        gridBagConstraints.gridy = 0;
        this.setLayout(new GridBagLayout());
        this.setSize(185, 95);
        this.setBorder(javax.swing.BorderFactory.createCompoundBorder(javax.swing.BorderFactory.createTitledBorder(i18n.str("content_alignment")), javax.swing.BorderFactory.createEmptyBorder(2,5,2,5))); //$NON-NLS-1$
        
        this.setPreferredSize(new java.awt.Dimension(185,95));
        this.setMaximumSize(this.getPreferredSize());
        this.setMinimumSize(this.getPreferredSize());
        this.add(getVAlignCB(), gridBagConstraints);
        this.add(getHAlignCB(), gridBagConstraints1);
        this.add(getVLocCombo(), gridBagConstraints2);
        this.add(getHLocCombo(), gridBagConstraints3);
    }

    /**
     * This method initializes vAlignCB	
     * 	
     * @return javax.swing.JCheckBox	
     */
    private JCheckBox getVAlignCB()
    {
        if(vAlignCB == null)
        {
            vAlignCB = new JCheckBox();
            vAlignCB.setText(i18n.str("vertical")); //$NON-NLS-1$
            
            vAlignCB.addActionListener(new java.awt.event.ActionListener()
            {
                public void actionPerformed(java.awt.event.ActionEvent e)
                {
                    vLocCombo.setEnabled(vAlignCB.isSelected());
                }
            });
        }
        return vAlignCB;
    }

    /**
     * This method initializes hAlignCB	
     * 	
     * @return javax.swing.JCheckBox	
     */
    private JCheckBox getHAlignCB()
    {
        if(hAlignCB == null)
        {
            hAlignCB = new JCheckBox();
            hAlignCB.setText(i18n.str("horizontal")); //$NON-NLS-1$
            
            hAlignCB.addActionListener(new java.awt.event.ActionListener()
            {
                public void actionPerformed(java.awt.event.ActionEvent e)
                {
                    hLocCombo.setEnabled(hAlignCB.isSelected());
                }
            });
        }
        return hAlignCB;
    }

    /**
     * This method initializes vLocCombo	
     * 	
     * @return javax.swing.JComboBox	
     */
    private JComboBox getVLocCombo()
    {
        if(vLocCombo == null)
        {
            vLocCombo = new JComboBox(VERT_ALIGNMENTS);            
        }
        return vLocCombo;
    }

    /**
     * This method initializes hLocCombo	
     * 	
     * @return javax.swing.JComboBox	
     */
    private JComboBox getHLocCombo()
    {
        if(hLocCombo == null)
        {
            hLocCombo = new JComboBox(HORIZ_ALIGNMENTS);
            
        }
        return hLocCombo;
    }

}  //  @jve:decl-index=0:visual-constraint="10,10"

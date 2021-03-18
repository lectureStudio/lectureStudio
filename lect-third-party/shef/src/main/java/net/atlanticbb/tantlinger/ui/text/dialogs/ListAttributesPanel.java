/*
 * Created on Jan 18, 2006
 *
 */
package net.atlanticbb.tantlinger.ui.text.dialogs;

import java.awt.GridBagLayout;
import javax.swing.*;
import java.awt.GridBagConstraints;
import javax.swing.JComboBox;
import javax.swing.JCheckBox;
import javax.swing.SpinnerNumberModel;
import java.util.*;

public class ListAttributesPanel extends HTMLAttributeEditorPanel
{
    /**
     * 
     */
    private static final long serialVersionUID = 1L;
    public static final int UL_LIST = 0;
    public static final int OL_LIST = 1;
    
    private static final String UL = i18n.str("unordered_list"); //$NON-NLS-1$
    private static final String OL = i18n.str("ordered_list");     //$NON-NLS-1$
    private static final String LIST_TYPES[] = {UL, OL};    
    
    private static final String OL_TYPES[] = {"1", "a", "A", "i", "I"}; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$
    private static final String OL_TYPE_LABELS[] =
    {
        "1, 2, 3, ...", //$NON-NLS-1$
        "a, b, c, ...", //$NON-NLS-1$
        "A, B, C, ...", //$NON-NLS-1$
        "i, ii, iii, ...", //$NON-NLS-1$
        "I, II, III, ..." //$NON-NLS-1$
    };
    
    private static final String UL_TYPES[] = {"disc", "square", "circle"}; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
    private static final String UL_TYPE_LABELS[] =
    {
        i18n.str("solid_circle"), i18n.str("solid_square"), i18n.str("open_circle") //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
    };
    
    private JLabel typeLabel = null;
    private JComboBox typeCombo = null;
    private JComboBox styleCombo = null;
    private JSpinner startAtField = null;
    private JCheckBox styleCB = null;
    private JCheckBox startAtCB = null;

    /**
     * This method initializes 
     * 
     */
    public ListAttributesPanel() 
    {
    	this(new Hashtable());
    }
    
    public ListAttributesPanel(Hashtable ht)
    {
        super();
        initialize();
        setAttributes(ht);
        updateComponentsFromAttribs();
    }
    
    public void setListType(int t)
    {
        typeCombo.setSelectedIndex(t);
        updateForType();
    }
    
    public int getListType()
    {
        return typeCombo.getSelectedIndex();
    }
    
    private void updateForType()
    {
        styleCombo.removeAllItems();
        if(typeCombo.getSelectedItem().equals(UL))
        {
            for(int i = 0; i < UL_TYPE_LABELS.length; i++)
                styleCombo.addItem(UL_TYPE_LABELS[i]);
            startAtCB.setEnabled(false);
            startAtField.setEnabled(false);
        }
        else 
        {
            for(int i = 0; i < OL_TYPE_LABELS.length; i++)
                styleCombo.addItem(OL_TYPE_LABELS[i]);
            startAtCB.setEnabled(true);
            startAtField.setEnabled(startAtCB.isSelected());
        }
    }
    
    private int getIndexForStyle(String s)
    {
        if(typeCombo.getSelectedIndex() == UL_LIST)
        {
            for(int i = 0; i < UL_TYPES.length; i++)
                if(UL_TYPES[i].equals(s))
                    return i;
        }
        else
        {
            for(int i = 0; i < OL_TYPES.length; i++)
                if(OL_TYPES[i].equals(s))
                    return i;
        }
        
        return 0;
    }

    /**
     * This method initializes this
     * 
     */
    private void initialize() {
        GridBagConstraints gridBagConstraints2 = new GridBagConstraints();
        gridBagConstraints2.gridx = 0;
        gridBagConstraints2.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints2.gridy = 2;
        GridBagConstraints gridBagConstraints1 = new GridBagConstraints();
        gridBagConstraints1.gridx = 0;
        gridBagConstraints1.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints1.insets = new java.awt.Insets(0,0,5,5);
        gridBagConstraints1.gridy = 1;
        GridBagConstraints gridBagConstraints5 = new GridBagConstraints();
        gridBagConstraints5.fill = java.awt.GridBagConstraints.NONE;
        gridBagConstraints5.gridy = 2;
        gridBagConstraints5.weightx = 1.0;
        gridBagConstraints5.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints5.gridx = 1;
        GridBagConstraints gridBagConstraints4 = new GridBagConstraints();
        gridBagConstraints4.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints4.gridy = 1;
        gridBagConstraints4.weightx = 1.0;
        gridBagConstraints4.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints4.insets = new java.awt.Insets(0,0,5,0);
        gridBagConstraints4.gridx = 1;
        GridBagConstraints gridBagConstraints3 = new GridBagConstraints();
        gridBagConstraints3.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints3.gridy = 0;
        gridBagConstraints3.weightx = 1.0;
        gridBagConstraints3.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints3.insets = new java.awt.Insets(0,0,5,0);
        gridBagConstraints3.gridx = 1;
        GridBagConstraints gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(0,0,5,5);
        gridBagConstraints.gridy = 0;
        typeLabel = new JLabel();
        typeLabel.setText(i18n.str("list_type")); //$NON-NLS-1$
        this.setLayout(new GridBagLayout());
        this.setSize(new java.awt.Dimension(234,159));
        this.setBorder(javax.swing.BorderFactory.createEmptyBorder(5,5,5,5));
        this.add(typeLabel, gridBagConstraints);
        this.add(getTypeCombo(), gridBagConstraints3);
        this.add(getStyleCombo(), gridBagConstraints4);
        this.add(getStartAtField(), gridBagConstraints5);
        this.add(getStyleCB(), gridBagConstraints1);
        this.add(getStartAtCB(), gridBagConstraints2);    		
    }

    public void updateComponentsFromAttribs()
    {
        //updateForType();
        if(attribs.containsKey("type")) //$NON-NLS-1$
        {
            styleCB.setSelected(true);
            styleCombo.setEnabled(true);
            int i = getIndexForStyle(attribs.get("type").toString()); //$NON-NLS-1$
            styleCombo.setSelectedIndex(i);            
        }
        else
        {
            styleCB.setSelected(false);
            styleCombo.setEnabled(false);
        }
        
        if(attribs.containsKey("start")) //$NON-NLS-1$
        {
            startAtCB.setSelected(true);
            startAtField.setEnabled(true);
            try
            {
                int n = Integer.parseInt(attribs.get("start").toString()); //$NON-NLS-1$
                startAtField.getModel().setValue(Integer.valueOf(n));
            }
            catch(Exception ex){}
        }
        else
        {
            startAtCB.setSelected(false);
            startAtField.setEnabled(false);
        }
    }

    public void updateAttribsFromComponents()
    {
        if(styleCB.isSelected())
        {
            if(typeCombo.getSelectedIndex() == UL_LIST)
                attribs.put("type", UL_TYPES[styleCombo.getSelectedIndex()]); //$NON-NLS-1$
            else
                attribs.put("type", OL_TYPES[styleCombo.getSelectedIndex()]); //$NON-NLS-1$
        }
        else
            attribs.remove("type"); //$NON-NLS-1$
        
        if(startAtCB.isSelected())
            attribs.put("start", startAtField.getModel().getValue().toString()); //$NON-NLS-1$
        else
            attribs.remove("start"); //$NON-NLS-1$
    }

    /**
     * This method initializes typeCombo	
     * 	
     * @return javax.swing.JComboBox	
     */
    private JComboBox getTypeCombo()
    {
        if(typeCombo == null)
        {
            typeCombo = new JComboBox(LIST_TYPES);
            typeCombo.addItemListener(new java.awt.event.ItemListener()
            {
                public void itemStateChanged(java.awt.event.ItemEvent e)
                {
                    updateForType();
                }
            });
        }
        return typeCombo;
    }


    /**
     * This method initializes styleCombo	
     * 	
     * @return javax.swing.JComboBox	
     */
    private JComboBox getStyleCombo()
    {
        if(styleCombo == null)
        {
            styleCombo = new JComboBox(UL_TYPE_LABELS);
        }
        return styleCombo;
    }

    /**
     * This method initializes startAtField	
     * 	
     * @return javax.swing.JSpinner	
     */
    private JSpinner getStartAtField()
    {
        if(startAtField == null)
        {
            startAtField = new JSpinner(new SpinnerNumberModel(1, 1, 999, 1));
            
        }
        return startAtField;
    }

    /**
     * This method initializes styleCB	
     * 	
     * @return javax.swing.JCheckBox	
     */
    private JCheckBox getStyleCB()
    {
        if(styleCB == null)
        {
            styleCB = new JCheckBox();
            styleCB.setText(i18n.str("style")); //$NON-NLS-1$
            styleCB.addItemListener(new java.awt.event.ItemListener()
            {
                public void itemStateChanged(java.awt.event.ItemEvent e)
                {
                    styleCombo.setEnabled(styleCB.isSelected());
                }
            });
        }
        return styleCB;
    }

    /**
     * This method initializes startAtCB	
     * 	
     * @return javax.swing.JCheckBox	
     */
    private JCheckBox getStartAtCB()
    {
        if(startAtCB == null)
        {
            startAtCB = new JCheckBox();
            startAtCB.setText(i18n.str("start_at")); //$NON-NLS-1$
            startAtCB.addItemListener(new java.awt.event.ItemListener()
            {
                public void itemStateChanged(java.awt.event.ItemEvent e)
                {
                    startAtField.setEnabled(startAtCB.isSelected());
                }
            });
        }
        return startAtCB;
    }

}  //  @jve:decl-index=0:visual-constraint="10,10"

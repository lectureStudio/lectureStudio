/*
 * Created on Dec 21, 2005
 *
 */
package net.atlanticbb.tantlinger.ui.text.dialogs;

import java.awt.GridBagLayout;
import javax.swing.JCheckBox;
import java.awt.GridBagConstraints;
import javax.swing.JSpinner;
import javax.swing.JComboBox;
import javax.swing.SpinnerNumberModel;
import java.util.*;


/**
 * Panel for editing the size of a table cell
 * 
 * @author Bob Tantlinger
 *
 */
public class SizeAttributesPanel extends HTMLAttributeEditorPanel
{
    /**
     * 
     */
    private static final long serialVersionUID = 1L;

    private static final String MEASUREMENTS[] = {"percent", "pixels"};     //$NON-NLS-1$ //$NON-NLS-2$
    
    private JCheckBox widthCB = null;
    private JCheckBox heightCB = null;
    private JSpinner widthField = null;
    private JSpinner heightField = null;
    private JComboBox wMeasurementCombo = null;
    private JComboBox hMeasurementCombo = null;
        
    public SizeAttributesPanel()
    {
        this(new Hashtable());
    }
    
    public SizeAttributesPanel(Hashtable attr)
    {
        super(attr);        
        initialize();
        updateComponentsFromAttribs();
    }
    
    public void updateComponentsFromAttribs()
    {
        if(attribs.containsKey("width")) //$NON-NLS-1$
        {
            widthCB.setSelected(true);
            String w = attribs.get("width").toString(); //$NON-NLS-1$
            if(w.endsWith("%"))                             //$NON-NLS-1$
                w = w.substring(0, w.length() - 1);            
            else
                wMeasurementCombo.setSelectedIndex(1);
            try
            {
                widthField.getModel().setValue(Integer.valueOf(w));
            }
            catch(Exception ex)
            {
                ex.printStackTrace();
            }
            wMeasurementCombo.setEnabled(true);
            widthField.setEnabled(true);
        }
        else
        {
            widthCB.setSelected(false);
            widthField.setEnabled(false);
            wMeasurementCombo.setEnabled(false);
        }
        
        if(attribs.containsKey("height")) //$NON-NLS-1$
        {
            heightCB.setSelected(true);
            String h = attribs.get("height").toString(); //$NON-NLS-1$
            if(h.endsWith("%"))                             //$NON-NLS-1$
                h = h.substring(0, h.length() - 1);            
            else
                hMeasurementCombo.setSelectedIndex(1);
            try
            {
                heightField.getModel().setValue(Integer.valueOf(h));
            }
            catch(Exception ex)
            {
                ex.printStackTrace();
            }
            hMeasurementCombo.setEnabled(true);
            heightField.setEnabled(true);
        }
        else
        {
            heightCB.setSelected(false);
            heightField.setEnabled(false);
            hMeasurementCombo.setEnabled(false);
        }
    }
    
    
    public void updateAttribsFromComponents()
    {
        if(widthCB.isSelected())
        {
            String w = widthField.getModel().getValue().toString();
            if(wMeasurementCombo.getSelectedIndex() == 0)
                w += "%"; //$NON-NLS-1$
            attribs.put("width", w); //$NON-NLS-1$
        }
        else
            attribs.remove("width"); //$NON-NLS-1$
        
        if(heightCB.isSelected())
        {
            String h = heightField.getModel().getValue().toString();
            if(hMeasurementCombo.getSelectedIndex() == 0)
                h += "%"; //$NON-NLS-1$
            attribs.put("height", h); //$NON-NLS-1$
        }
        else
            attribs.remove("height"); //$NON-NLS-1$
    }
    

    
    public void setComponentStates(Hashtable attribs)
    {
        if(attribs.containsKey("width")) //$NON-NLS-1$
        {
            widthCB.setSelected(true);
            String w = attribs.get("width").toString(); //$NON-NLS-1$
            if(w.endsWith("%"))                             //$NON-NLS-1$
                w = w.substring(0, w.length() - 1);            
            else
                wMeasurementCombo.setSelectedIndex(1);
            try
            {
                widthField.getModel().setValue(Integer.valueOf(w));
            }
            catch(Exception ex)
            {
                ex.printStackTrace();
            }
            wMeasurementCombo.setEnabled(true);
            widthField.setEnabled(true);
        }
        else
        {
            widthCB.setSelected(false);
            widthField.setEnabled(false);
            wMeasurementCombo.setEnabled(false);
        }
        
        if(attribs.containsKey("height")) //$NON-NLS-1$
        {
            heightCB.setSelected(true);
            String h = attribs.get("height").toString(); //$NON-NLS-1$
            if(h.endsWith("%"))                             //$NON-NLS-1$
                h = h.substring(0, h.length() - 1);            
            else
                hMeasurementCombo.setSelectedIndex(1);
            try
            {
                heightField.getModel().setValue(Integer.valueOf(h));
            }
            catch(Exception ex)
            {
                ex.printStackTrace();
            }
            hMeasurementCombo.setEnabled(true);
            heightField.setEnabled(true);
        }
        else
        {
            heightCB.setSelected(false);
            heightField.setEnabled(false);
            hMeasurementCombo.setEnabled(false);
        }        
    }
    


    /**
     * This method initializes this
     * 
     * @return void
     */
    private void initialize()
    {
        GridBagConstraints gridBagConstraints5 = new GridBagConstraints();
        gridBagConstraints5.fill = java.awt.GridBagConstraints.NONE;
        gridBagConstraints5.gridy = 1;
        gridBagConstraints5.weightx = 0.0;
        gridBagConstraints5.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints5.ipadx = 0;
        gridBagConstraints5.gridx = 2;
        GridBagConstraints gridBagConstraints4 = new GridBagConstraints();
        gridBagConstraints4.fill = java.awt.GridBagConstraints.NONE;
        gridBagConstraints4.gridy = 0;
        gridBagConstraints4.weightx = 1.0;
        gridBagConstraints4.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints4.insets = new java.awt.Insets(0,0,5,0);
        gridBagConstraints4.gridx = 2;
        GridBagConstraints gridBagConstraints3 = new GridBagConstraints();
        gridBagConstraints3.fill = java.awt.GridBagConstraints.NONE;
        gridBagConstraints3.gridy = 1;
        gridBagConstraints3.weightx = 0.0;
        gridBagConstraints3.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints3.insets = new java.awt.Insets(0,0,0,5);
        gridBagConstraints3.ipadx = 0;
        gridBagConstraints3.gridx = 1;
        GridBagConstraints gridBagConstraints2 = new GridBagConstraints();
        gridBagConstraints2.fill = java.awt.GridBagConstraints.NONE;
        gridBagConstraints2.gridy = 0;
        gridBagConstraints2.weightx = 0.0;
        gridBagConstraints2.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints2.insets = new java.awt.Insets(0,0,0,5);
        gridBagConstraints2.ipadx = 0;
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
        this.setSize(215, 95);
        this.setPreferredSize(new java.awt.Dimension(215,95));
        this.setMaximumSize(getPreferredSize());
        this.setMinimumSize(getPreferredSize());
        this.setBorder(javax.swing.BorderFactory.createCompoundBorder(javax.swing.BorderFactory.createTitledBorder(null, i18n.str("size"), javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.DEFAULT_POSITION, null, null), javax.swing.BorderFactory.createEmptyBorder(2,5,2,5))); //$NON-NLS-1$
        this.add(getWidthCB(), gridBagConstraints);
        this.add(getHeightCB(), gridBagConstraints1);
        this.add(getWidthField(), gridBagConstraints2);
        this.add(getHeightField(), gridBagConstraints3);
        this.add(getWMeasurementCombo(), gridBagConstraints4);
        this.add(getHMeasurementCombo(), gridBagConstraints5);
    }

    /**
     * This method initializes widthCB	
     * 	
     * @return javax.swing.JCheckBox	
     */
    private JCheckBox getWidthCB()
    {
        if(widthCB == null)
        {
            widthCB = new JCheckBox();
            widthCB.setText(i18n.str("width")); //$NON-NLS-1$
                        
            widthCB.addItemListener(new java.awt.event.ItemListener()
            {
                public void itemStateChanged(java.awt.event.ItemEvent e)
                {
                    widthField.setEnabled(widthCB.isSelected());
                    wMeasurementCombo.setEnabled(widthCB.isSelected());
                }
            });
        }
        return widthCB;
    }

    /**
     * This method initializes heightCB	
     * 	
     * @return javax.swing.JCheckBox	
     */
    private JCheckBox getHeightCB()
    {
        if(heightCB == null)
        {
            heightCB = new JCheckBox();
            heightCB.setText(i18n.str("height")); //$NON-NLS-1$
                        
            heightCB.addItemListener(new java.awt.event.ItemListener()
            {
                public void itemStateChanged(java.awt.event.ItemEvent e)
                {
                    heightField.setEnabled(heightCB.isSelected());
                    hMeasurementCombo.setEnabled(heightCB.isSelected());
                }
            });
        }
        return heightCB;
    }

    /**
     * This method initializes widthField	
     * 	
     * @return javax.swing.JSpinner	
     */
    private JSpinner getWidthField()
    {        
        if(widthField == null)
        {
            
            widthField = new JSpinner(new SpinnerNumberModel(1, 1, 999, 1));
                      
        }
        
        return widthField;
    }

    /**
     * This method initializes heightField	
     * 	
     * @return javax.swing.JSpinner
     */
    private JSpinner getHeightField()
    {        
        if(heightField == null)
        {
            
            heightField = new JSpinner(new SpinnerNumberModel(1, 1, 999, 1));
            
        }
        
        return heightField;
    }

    /**
     * This method initializes wMeasurementCombo	
     * 	
     * @return javax.swing.JComboBox	
     */
    private JComboBox getWMeasurementCombo()
    {
        if(wMeasurementCombo == null)
        {
            wMeasurementCombo = new JComboBox(MEASUREMENTS);
           
        }
        return wMeasurementCombo;
    }

    /**
     * This method initializes hMeasurementCombo	
     * 	
     * @return javax.swing.JComboBox	
     */
    private JComboBox getHMeasurementCombo()
    {
        if(hMeasurementCombo == null)
        {
            hMeasurementCombo = new JComboBox(MEASUREMENTS);
            
        }
        return hMeasurementCombo;
    }

}  //  @jve:decl-index=0:visual-constraint="10,10"

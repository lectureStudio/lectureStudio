/*
 * Created on Dec 22, 2005
 *
 */
package net.atlanticbb.tantlinger.ui.text.dialogs;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.util.Hashtable;

import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.SpinnerNumberModel;

public class TableAttributesPanel extends HTMLAttributeEditorPanel
{

    /**
     * 
     */
    private static final long serialVersionUID = 1L;
    private static final String ALIGNMENTS[] = {"left", "center", "right"}; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
    private static final String MEASUREMENTS[] = {"percent", "pixels"}; //$NON-NLS-1$ //$NON-NLS-2$
    
    private JCheckBox widthCB = null;
    private JSpinner widthField = null;
    private JComboBox widthCombo = null;
    private JCheckBox alignCB = null;
    private JCheckBox cellSpacingCB = null;
    private JSpinner cellSpacingField = null;
    private JCheckBox borderCB = null;
    private JSpinner borderField = null;
    private JCheckBox cellPaddingCB = null;
    private JSpinner cellPaddingField = null;
    private JComboBox alignCombo = null;
    private BGColorPanel bgPanel = null;
    private JPanel expansionPanel = null;    
       
    
    /**
     * This is the default constructor
     */
    public TableAttributesPanel()
    {
        this(new Hashtable());
        //super();
        //initialize();
        //Hashtable ht = new Hashtable();
        //ht.put("width", "100%");
        //ht.put("border", "1");        
        //setComponentStates(ht);
    }
    
    public TableAttributesPanel(Hashtable attribs)
    {
        super(attribs);
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
                widthCombo.setSelectedIndex(1);
            widthField.setEnabled(true);
            
            try
            {
                widthField.getModel().setValue(Integer.valueOf(w));
            }
            catch(Exception ex)
            {
                ex.printStackTrace();
            }            
        }
        else
        {
            widthCB.setSelected(false);
            widthField.setEnabled(false);
            widthCombo.setEnabled(false);
        }
        
        if(attribs.containsKey("align")) //$NON-NLS-1$
        {
            alignCB.setSelected(true);
            alignCombo.setEnabled(true);
            alignCombo.setSelectedItem(attribs.get("align")); //$NON-NLS-1$
        }
        else
        {
            alignCB.setSelected(false);
            alignCombo.setEnabled(false);
        }
        
        if(attribs.containsKey("border")) //$NON-NLS-1$
        {
            borderCB.setSelected(true);
            borderField.setEnabled(true);
            try
            {
                borderField.getModel().setValue(
                        Integer.valueOf(attribs.get("border").toString())); //$NON-NLS-1$
            }
            catch(Exception ex)
            {
                ex.printStackTrace();
            }
        }
        else
        {
            borderCB.setSelected(false);
            borderField.setEnabled(false);
        }
        
        if(attribs.containsKey("cellpadding")) //$NON-NLS-1$
        {
            cellPaddingCB.setSelected(true);
            cellPaddingField.setEnabled(true);
            try
            {
                cellPaddingField.getModel().setValue(
                        Integer.valueOf(attribs.get("cellpadding").toString())); //$NON-NLS-1$
            }
            catch(Exception ex)
            {
                ex.printStackTrace();
            }
        }
        else
        {
           cellPaddingCB.setSelected(false);
           cellPaddingField.setEnabled(false);
        }
        
        if(attribs.containsKey("cellspacing")) //$NON-NLS-1$
        {
            cellSpacingCB.setSelected(true);
            cellSpacingField.setEnabled(true);
            try
            {
                cellSpacingField.getModel().setValue(
                        Integer.valueOf(attribs.get("cellspacing").toString())); //$NON-NLS-1$
            }
            catch(Exception ex)
            {
                ex.printStackTrace();
            }
        }
        else
        {
            cellSpacingCB.setSelected(false);
            cellSpacingField.setEnabled(false);
        }
        
        if(attribs.containsKey("bgcolor")) //$NON-NLS-1$
        {
            bgPanel.setSelected(true);
            bgPanel.setColor(attribs.get("bgcolor").toString()); //$NON-NLS-1$
        }
        else
        {
            bgPanel.setSelected(false);
        }
    }    
    
    public void updateAttribsFromComponents()
    {
        if(widthCB.isSelected())
        {
            String w = widthField.getModel().getValue().toString();
            if(widthCombo.getSelectedIndex() == 0)
                w += "%"; //$NON-NLS-1$
            attribs.put("width", w); //$NON-NLS-1$
        }
        else
            attribs.remove("width"); //$NON-NLS-1$
        
        if(alignCB.isSelected())
            attribs.put("align", alignCombo.getSelectedItem().toString()); //$NON-NLS-1$
        else
            attribs.remove("align"); //$NON-NLS-1$
        
        if(borderCB.isSelected())
            attribs.put("border",  //$NON-NLS-1$
                borderField.getModel().getValue().toString());
        else
            attribs.remove("border"); //$NON-NLS-1$
        
        if(cellSpacingCB.isSelected())
            attribs.put("cellspacing",  //$NON-NLS-1$
                cellSpacingField.getModel().getValue().toString());
        else
            attribs.remove("cellspacing"); //$NON-NLS-1$
        
        if(cellPaddingCB.isSelected())
            attribs.put("cellpadding",  //$NON-NLS-1$
                cellPaddingField.getModel().getValue().toString());
        else
            attribs.remove("cellpadding"); //$NON-NLS-1$
        
        if(bgPanel.isSelected())
            attribs.put("bgcolor", bgPanel.getColor()); //$NON-NLS-1$
        else
            attribs.remove("bgcolor"); //$NON-NLS-1$
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
                widthCombo.setSelectedIndex(1);
            try
            {
                widthField.getModel().setValue(Integer.valueOf(w));
            }
            catch(Exception ex)
            {
                ex.printStackTrace();
            }            
        }
        else
        {
            widthCB.setSelected(false);
            widthField.setEnabled(false);
            widthCombo.setEnabled(false);
        }

        
        if(attribs.containsKey("align")) //$NON-NLS-1$
        {
            alignCB.setSelected(true);
            alignCombo.setSelectedItem(attribs.get("align")); //$NON-NLS-1$
        }
        else
        {
            alignCB.setSelected(false);
            alignCombo.setEnabled(false);
        }
        
        if(attribs.containsKey("border")) //$NON-NLS-1$
        {
            borderCB.setSelected(true);
            try
            {
                borderField.getModel().setValue(
                        Integer.valueOf(attribs.get("border").toString())); //$NON-NLS-1$
            }
            catch(Exception ex)
            {
                ex.printStackTrace();
            }
        }
        else
        {
            borderCB.setSelected(false);
            borderField.setEnabled(false);
        }
        
        if(attribs.containsKey("cellpadding")) //$NON-NLS-1$
        {
            cellPaddingCB.setSelected(true);
            try
            {
                cellPaddingField.getModel().setValue(
                        Integer.valueOf(attribs.get("cellpadding").toString())); //$NON-NLS-1$
            }
            catch(Exception ex)
            {
                ex.printStackTrace();
            }
        }
        else
        {
           cellPaddingCB.setSelected(false);
           cellPaddingField.setEnabled(false);
        }
        
        if(attribs.containsKey("cellspacing")) //$NON-NLS-1$
        {
            cellSpacingCB.setSelected(true);
            try
            {
                cellSpacingField.getModel().setValue(
                        Integer.valueOf(attribs.get("cellspacing").toString())); //$NON-NLS-1$
            }
            catch(Exception ex)
            {
                ex.printStackTrace();
            }
        }
        else
        {
            cellSpacingCB.setSelected(false);
            cellSpacingField.setEnabled(false);
        }
        
        if(attribs.containsKey("bgcolor")) //$NON-NLS-1$
        {
            bgPanel.setSelected(true);
            bgPanel.setColor(attribs.get("bgcolor").toString()); //$NON-NLS-1$
        }
        else
        {
            bgPanel.setSelected(false);
        }
        
    }
    

    /**
     * This method initializes this
     * 
     * @return void
     */
    private void initialize()
    {
        GridBagConstraints gridBagConstraints12 = new GridBagConstraints();
        gridBagConstraints12.gridx = 0;
        gridBagConstraints12.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints12.gridwidth = 4;
        gridBagConstraints12.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints12.weightx = 0.0;
        gridBagConstraints12.weighty = 1.0;
        gridBagConstraints12.gridy = 4;
        GridBagConstraints gridBagConstraints11 = new GridBagConstraints();
        gridBagConstraints11.gridx = 0;
        gridBagConstraints11.gridwidth = 4;
        gridBagConstraints11.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints11.weighty = 0.0;
        gridBagConstraints11.gridy = 3;
        GridBagConstraints gridBagConstraints4 = new GridBagConstraints();
        gridBagConstraints4.fill = java.awt.GridBagConstraints.NONE;
        gridBagConstraints4.gridy = 1;
        gridBagConstraints4.weightx = 0.0;
        gridBagConstraints4.gridwidth = 2;
        gridBagConstraints4.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints4.insets = new java.awt.Insets(0,0,5,15);
        gridBagConstraints4.gridx = 1;
        GridBagConstraints gridBagConstraints10 = new GridBagConstraints();
        gridBagConstraints10.fill = java.awt.GridBagConstraints.NONE;
        gridBagConstraints10.gridy = 2;
        gridBagConstraints10.weightx = 1.0;
        gridBagConstraints10.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints10.insets = new java.awt.Insets(0,0,10,0);
        gridBagConstraints10.gridx = 4;
        GridBagConstraints gridBagConstraints9 = new GridBagConstraints();
        gridBagConstraints9.gridx = 3;
        gridBagConstraints9.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints9.insets = new java.awt.Insets(0,0,10,3);
        gridBagConstraints9.gridy = 2;
        GridBagConstraints gridBagConstraints8 = new GridBagConstraints();
        gridBagConstraints8.fill = java.awt.GridBagConstraints.NONE;
        gridBagConstraints8.gridy = 2;
        gridBagConstraints8.weightx = 0.0;
        gridBagConstraints8.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints8.insets = new java.awt.Insets(0,0,10,15);
        gridBagConstraints8.gridx = 1;
        GridBagConstraints gridBagConstraints7 = new GridBagConstraints();
        gridBagConstraints7.gridx = 0;
        gridBagConstraints7.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints7.insets = new java.awt.Insets(0,0,10,3);
        gridBagConstraints7.gridy = 2;
        GridBagConstraints gridBagConstraints6 = new GridBagConstraints();
        gridBagConstraints6.fill = java.awt.GridBagConstraints.NONE;
        gridBagConstraints6.gridy = 1;
        gridBagConstraints6.weightx = 1.0;
        gridBagConstraints6.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints6.insets = new java.awt.Insets(0,0,2,0);
        gridBagConstraints6.gridx = 4;
        GridBagConstraints gridBagConstraints5 = new GridBagConstraints();
        gridBagConstraints5.gridx = 3;
        gridBagConstraints5.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints5.insets = new java.awt.Insets(0,0,2,3);
        gridBagConstraints5.gridy = 1;
        GridBagConstraints gridBagConstraints3 = new GridBagConstraints();
        gridBagConstraints3.gridx = 0;
        gridBagConstraints3.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints3.insets = new java.awt.Insets(0,0,2,3);
        gridBagConstraints3.gridy = 1;
        GridBagConstraints gridBagConstraints2 = new GridBagConstraints();
        gridBagConstraints2.fill = java.awt.GridBagConstraints.NONE;
        gridBagConstraints2.gridy = 0;
        gridBagConstraints2.weightx = 0.0;
        gridBagConstraints2.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints2.gridwidth = 2;
        gridBagConstraints2.insets = new java.awt.Insets(0,0,10,0);
        gridBagConstraints2.gridx = 2;
        GridBagConstraints gridBagConstraints1 = new GridBagConstraints();
        gridBagConstraints1.fill = java.awt.GridBagConstraints.NONE;
        gridBagConstraints1.gridy = 0;
        gridBagConstraints1.weightx = 0.0;
        gridBagConstraints1.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints1.insets = new java.awt.Insets(0,0,10,0);
        gridBagConstraints1.gridx = 1;
        GridBagConstraints gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(0,0,10,3);
        gridBagConstraints.gridy = 0;
        this.setLayout(new GridBagLayout());
        this.setSize(320, 140);
        this.setPreferredSize(new java.awt.Dimension(320,140));
        
        this.add(getWidthCB(), gridBagConstraints);
        this.add(getWidthField(), gridBagConstraints1);
        this.add(getWidthCombo(), gridBagConstraints2);
        this.add(getAlignCB(), gridBagConstraints3);
        this.add(getCellSpacingCB(), gridBagConstraints5);
        this.add(getCellSpacingField(), gridBagConstraints6);
        this.add(getBorderCB(), gridBagConstraints7);
        this.add(getBorderField(), gridBagConstraints8);
        this.add(getCellPaddingCB(), gridBagConstraints9);
        this.add(getCellPaddingField(), gridBagConstraints10);
        this.add(getAlignCombo(), gridBagConstraints4);
        this.add(getBGPanel(), gridBagConstraints11);
        this.add(getExpansionPanel(), gridBagConstraints12);
        
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
            widthCB.setText(i18n.str("width"));             //$NON-NLS-1$
            widthCB.addItemListener(new java.awt.event.ItemListener()
            {
                public void itemStateChanged(java.awt.event.ItemEvent e)
                {
                    widthField.setEnabled(widthCB.isSelected());
                    widthCombo.setEnabled(widthCB.isSelected());
                }
            });
        }
        return widthCB;
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
            widthField = new JSpinner(new SpinnerNumberModel(100, 1, 999, 1));
                      
        }
        return widthField;
    }

    /**
     * This method initializes widthCombo	
     * 	
     * @return javax.swing.JComboBox	
     */
    private JComboBox getWidthCombo()
    {
        if(widthCombo == null)
        {
            widthCombo = new JComboBox(MEASUREMENTS);
        }
        return widthCombo;
    }

    /**
     * This method initializes alignCB	
     * 	
     * @return javax.swing.JCheckBox	
     */
    private JCheckBox getAlignCB()
    {
        if(alignCB == null)
        {
            alignCB = new JCheckBox();
            alignCB.setText(i18n.str("align")); //$NON-NLS-1$
            alignCB.addItemListener(new java.awt.event.ItemListener()
            {
                public void itemStateChanged(java.awt.event.ItemEvent e)
                {
                    alignCombo.setEnabled(alignCB.isSelected());
                }
            });
        }
        return alignCB;
    }

    /**
     * This method initializes cellSpacingCB	
     * 	
     * @return javax.swing.JCheckBox	
     */
    private JCheckBox getCellSpacingCB()
    {
        if(cellSpacingCB == null)
        {
            cellSpacingCB = new JCheckBox();
            cellSpacingCB.setText(i18n.str("cellspacing")); //$NON-NLS-1$
            cellSpacingCB.addItemListener(new java.awt.event.ItemListener()
            {
                public void itemStateChanged(java.awt.event.ItemEvent e)
                {
                    cellSpacingField.setEnabled(cellSpacingCB.isSelected());
                }
            });
        }
        return cellSpacingCB;
    }

    /**
     * This method initializes cellSpacingField	
     * 	
     * @return javax.swing.JSpinner
     */
    private JSpinner getCellSpacingField()
    {
        if(cellSpacingField == null)
        {
            cellSpacingField = new JSpinner(new SpinnerNumberModel(1, 0, 999, 1));
           
        }
        return cellSpacingField;
    }

    /**
     * This method initializes borderCB	
     * 	
     * @return javax.swing.JCheckBox	
     */
    private JCheckBox getBorderCB()
    {
        if(borderCB == null)
        {
            borderCB = new JCheckBox();
            borderCB.setText(i18n.str("border")); //$NON-NLS-1$
            borderCB.addItemListener(new java.awt.event.ItemListener()
            {
                public void itemStateChanged(java.awt.event.ItemEvent e)
                {
                    borderField.setEnabled(borderCB.isSelected());
                }
            });
        }
        return borderCB;
    }

    /**
     * This method initializes borderField	
     * 	
     * @return javax.swing.JSpinner	
     */
    private JSpinner getBorderField()
    {
        if(borderField == null)
        {
            borderField = new JSpinner(new SpinnerNumberModel(1, 0, 999, 1));
            
        }
        return borderField;
    }

    /**
     * This method initializes cellPaddingCB	
     * 	
     * @return javax.swing.JCheckBox	
     */
    private JCheckBox getCellPaddingCB()
    {
        if(cellPaddingCB == null)
        {
            cellPaddingCB = new JCheckBox();
            cellPaddingCB.setText(i18n.str("cellpadding")); //$NON-NLS-1$
            cellPaddingCB.addItemListener(new java.awt.event.ItemListener()
            {
                public void itemStateChanged(java.awt.event.ItemEvent e)
                {
                    cellPaddingField.setEnabled(cellPaddingCB.isSelected());
                }
            });
        }
        return cellPaddingCB;
    }

    /**
     * This method initializes cellPaddingField	
     * 	
     * @return javax.swing.JSpinner	
     */
    private JSpinner getCellPaddingField()
    {
        if(cellPaddingField == null)
        {
            cellPaddingField = new JSpinner(new SpinnerNumberModel(1, 0, 999, 1));
            
        }
        return cellPaddingField;
    }

    /**
     * This method initializes alignCombo	
     * 	
     * @return javax.swing.JComboBox	
     */
    private JComboBox getAlignCombo()
    {
        if(alignCombo == null)
        {
            alignCombo = new JComboBox(ALIGNMENTS);
        }
        return alignCombo;
    }

    /**
     * This method initializes tempPanel	
     * 	
     * @return javax.swing.JPanel	
     */
    private JPanel getBGPanel()
    {
        if(bgPanel == null)
        {
            bgPanel = new BGColorPanel();
           
        }
        return bgPanel;
    }

    /**
     * This method initializes expansionPanel	
     * 	
     * @return javax.swing.JPanel	
     */
    private JPanel getExpansionPanel()
    {
        if(expansionPanel == null)
        {
            expansionPanel = new JPanel();
        }
        return expansionPanel;
    }

}  //  @jve:decl-index=0:visual-constraint="16,10"

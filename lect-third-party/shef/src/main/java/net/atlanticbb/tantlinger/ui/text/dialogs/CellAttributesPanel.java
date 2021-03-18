/*
 * Created on Dec 23, 2005
 *
 */
package net.atlanticbb.tantlinger.ui.text.dialogs;

import javax.swing.JPanel;
import java.awt.GridBagLayout;
import java.awt.GridBagConstraints;
import javax.swing.JCheckBox;
import javax.swing.JSpinner;
import javax.swing.SpinnerNumberModel;

import java.util.*;

public class CellAttributesPanel extends HTMLAttributeEditorPanel
{

    /**
     * 
     */
    private static final long serialVersionUID = 1L;
    private AlignmentAttributesPanel alignPanel = null;
    private SizeAttributesPanel sizePanel = null;
    private JCheckBox dontWrapCB = null;
    private BGColorPanel bgColorPanel = null;
    private JPanel spanPanel = null;
    private JCheckBox colSpanCB = null;
    private JCheckBox rowSpanCB = null;
    private JSpinner colSpanField = null;
    private JSpinner rowSpanField = null;
    private JPanel expansionPanel = null;

    /**
     * This is the default constructor
     */
    public CellAttributesPanel()
    {
        this(new Hashtable());
    }
    
    public CellAttributesPanel(Hashtable attr)
    {
        super(attr);
        initialize();
        alignPanel.setAttributes(getAttributes());
        sizePanel.setAttributes(getAttributes());
        updateComponentsFromAttribs(); 
    }
    
    public void updateComponentsFromAttribs()
    {
        alignPanel.updateComponentsFromAttribs();
        sizePanel.updateComponentsFromAttribs();
        
        if(attribs.containsKey("colspan")) //$NON-NLS-1$
        {
            colSpanCB.setSelected(true);
            colSpanField.setEnabled(true);
            try
            {
                colSpanField.getModel().setValue(
                        Integer.valueOf(attribs.get("colspan").toString())); //$NON-NLS-1$
            }
            catch(Exception ex)
            {
                ex.printStackTrace();
            }
        }
        else
        {
            colSpanCB.setSelected(false);
            colSpanField.setEnabled(false);
        }
        
        if(attribs.containsKey("rowspan")) //$NON-NLS-1$
        {
            rowSpanCB.setSelected(true);
            rowSpanField.setEnabled(true);
            try
            {
                rowSpanField.getModel().setValue(
                        Integer.valueOf(attribs.get("rowspan").toString())); //$NON-NLS-1$
            }
            catch(Exception ex)
            {
                ex.printStackTrace();
            }
        }
        else
        {
            rowSpanCB.setSelected(false);
            rowSpanField.setEnabled(false);
        }
        
        if(attribs.containsKey("bgcolor")) //$NON-NLS-1$
        {
            bgColorPanel.setSelected(true);
            bgColorPanel.setColor(attribs.get("bgcolor").toString()); //$NON-NLS-1$
        }
        else
        {
            bgColorPanel.setSelected(false);
        }
        
        dontWrapCB.setSelected(attribs.containsKey("nowrap"));  //$NON-NLS-1$
    }
    
    
    public void updateAttribsFromComponents()
    {        
        alignPanel.updateAttribsFromComponents();
        sizePanel.updateAttribsFromComponents();
        if(dontWrapCB.isSelected())
            attribs.put("nowrap", "nowrap"); //$NON-NLS-1$ //$NON-NLS-2$
        else
            attribs.remove("nowrap"); //$NON-NLS-1$
        
        if(bgColorPanel.isSelected())
            attribs.put("bgcolor", bgColorPanel.getColor()); //$NON-NLS-1$
        else
            attribs.remove("bgcolor"); //$NON-NLS-1$
        
        if(colSpanCB.isSelected())
            attribs.put("colspan", colSpanField.getModel().getValue().toString()); //$NON-NLS-1$
        else
            attribs.remove("colspan"); //$NON-NLS-1$
        
        if(rowSpanCB.isSelected())
            attribs.put("rowspan", rowSpanField.getModel().getValue().toString()); //$NON-NLS-1$
        else
            attribs.remove("rowspan"); //$NON-NLS-1$
    }
    

    
    public void setAttributes(Map attr)
    {
        alignPanel.setAttributes(attr);
        sizePanel.setAttributes(attr);
        super.setAttributes(attr);
    }

    /**
     * This method initializes this
     * 
     * @return void
     */
    private void initialize()
    {
        GridBagConstraints gridBagConstraints31 = new GridBagConstraints();
        gridBagConstraints31.gridx = 0;
        gridBagConstraints31.gridwidth = 3;
        gridBagConstraints31.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints31.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints31.weighty = 1.0;
        gridBagConstraints31.gridy = 3;
        GridBagConstraints gridBagConstraints21 = new GridBagConstraints();
        gridBagConstraints21.gridx = 0;
        gridBagConstraints21.gridheight = 2;
        gridBagConstraints21.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints21.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints21.insets = new java.awt.Insets(0,0,0,5);
        gridBagConstraints21.gridy = 1;
        GridBagConstraints gridBagConstraints3 = new GridBagConstraints();
        gridBagConstraints3.gridx = 1;
        gridBagConstraints3.gridwidth = 2;
        gridBagConstraints3.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints3.gridy = 2;
        GridBagConstraints gridBagConstraints2 = new GridBagConstraints();
        gridBagConstraints2.gridx = 1;
        gridBagConstraints2.anchor = java.awt.GridBagConstraints.SOUTHWEST;
        gridBagConstraints2.insets = new java.awt.Insets(0,0,5,0);
        gridBagConstraints2.gridwidth = 2;
        gridBagConstraints2.gridy = 1;
        GridBagConstraints gridBagConstraints1 = new GridBagConstraints();
        gridBagConstraints1.gridx = 1;
        gridBagConstraints1.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints1.weightx = 1.0;
        gridBagConstraints1.insets = new java.awt.Insets(0,0,5,0);
        gridBagConstraints1.gridy = 0;
        GridBagConstraints gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(0,0,5,5);
        gridBagConstraints.gridwidth = 1;
        gridBagConstraints.gridy = 0;
        this.setLayout(new GridBagLayout());
        this.setSize(420, 200);
        this.setPreferredSize(new java.awt.Dimension(410,200));
        this.add(getAlignPanel(), gridBagConstraints);
        this.add(getSizePanel(), gridBagConstraints1);
        this.add(getDontWrapCB(), gridBagConstraints2);
        this.add(getBgColorPanel(), gridBagConstraints3);
        this.add(getSpanPanel(), gridBagConstraints21);
        this.add(getExpansionPanel(), gridBagConstraints31);
        
        
    }

    /**
     * This method initializes alignPanel	
     * 	
     * @return javax.swing.JPanel	
     */
    private AlignmentAttributesPanel getAlignPanel()
    {
        if(alignPanel == null)
        {
            alignPanel = new AlignmentAttributesPanel();
            alignPanel.setPreferredSize(new java.awt.Dimension(180,95));
            
        }
        return alignPanel;
    }

    /**
     * This method initializes sizePanel	
     * 	
     * @return javax.swing.JPanel	
     */
    private JPanel getSizePanel()
    {
        if(sizePanel == null)
        {
            sizePanel = new SizeAttributesPanel();
        }
        return sizePanel;
    }

    /**
     * This method initializes dontWrapCB	
     * 	
     * @return javax.swing.JCheckBox	
     */
    private JCheckBox getDontWrapCB()
    {
        if(dontWrapCB == null)
        {
            dontWrapCB = new JCheckBox();
            dontWrapCB.setText(i18n.str("dont_wrap_text")); //$NON-NLS-1$
        }
        return dontWrapCB;
    }

    /**
     * This method initializes bgColorPanel	
     * 	
     * @return javax.swing.JPanel	
     */
    private BGColorPanel getBgColorPanel()
    {
        if(bgColorPanel == null)
        {
            bgColorPanel = new BGColorPanel();
        }
        return bgColorPanel;
    }

    /**
     * This method initializes spanPanel	
     * 	
     * @return javax.swing.JPanel	
     */
    private JPanel getSpanPanel()
    {
        if(spanPanel == null)
        {
            GridBagConstraints gridBagConstraints7 = new GridBagConstraints();
            gridBagConstraints7.fill = java.awt.GridBagConstraints.NONE;
            gridBagConstraints7.gridy = 1;
            gridBagConstraints7.weightx = 1.0;
            gridBagConstraints7.anchor = java.awt.GridBagConstraints.WEST;
            gridBagConstraints7.gridx = 1;
            GridBagConstraints gridBagConstraints6 = new GridBagConstraints();
            gridBagConstraints6.fill = java.awt.GridBagConstraints.NONE;
            gridBagConstraints6.gridy = 0;
            gridBagConstraints6.weightx = 0.0;
            gridBagConstraints6.anchor = java.awt.GridBagConstraints.WEST;
            gridBagConstraints6.gridx = 1;
            GridBagConstraints gridBagConstraints5 = new GridBagConstraints();
            gridBagConstraints5.gridx = 0;
            gridBagConstraints5.anchor = java.awt.GridBagConstraints.WEST;
            gridBagConstraints5.gridheight = 1;
            gridBagConstraints5.insets = new java.awt.Insets(0,0,0,0);
            gridBagConstraints5.gridy = 1;
            GridBagConstraints gridBagConstraints4 = new GridBagConstraints();
            gridBagConstraints4.gridx = 0;
            gridBagConstraints4.anchor = java.awt.GridBagConstraints.WEST;
            gridBagConstraints4.insets = new java.awt.Insets(0,0,5,0);
            gridBagConstraints4.gridy = 0;
            spanPanel = new JPanel();
            spanPanel.setLayout(new GridBagLayout());
            spanPanel.setBorder(javax.swing.BorderFactory.createCompoundBorder(javax.swing.BorderFactory.createTitledBorder(null, i18n.str("span"), javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.DEFAULT_POSITION, null, null), javax.swing.BorderFactory.createEmptyBorder(2,2,2,2))); //$NON-NLS-1$
            spanPanel.add(getColSpanCB(), gridBagConstraints4);
            spanPanel.add(getRowSpanCB(), gridBagConstraints5);
            spanPanel.add(getColSpanField(), gridBagConstraints6);
            spanPanel.add(getRowSpanField(), gridBagConstraints7);
        }
        return spanPanel;
    }

    /**
     * This method initializes colSpanCB	
     * 	
     * @return javax.swing.JCheckBox	
     */
    private JCheckBox getColSpanCB()
    {
        if(colSpanCB == null)
        {
            colSpanCB = new JCheckBox();
            colSpanCB.setText(i18n.str("colspan")); //$NON-NLS-1$
            colSpanCB.setPreferredSize(new java.awt.Dimension(85,25));
            colSpanCB.addItemListener(new java.awt.event.ItemListener()
            {
                public void itemStateChanged(java.awt.event.ItemEvent e)
                {
                    colSpanField.setEnabled(colSpanCB.isSelected());
                }
            });
        }
        return colSpanCB;
    }

    /**
     * This method initializes rowSpanCB	
     * 	
     * @return javax.swing.JCheckBox	
     */
    private JCheckBox getRowSpanCB()
    {
        if(rowSpanCB == null)
        {
            rowSpanCB = new JCheckBox();
            rowSpanCB.setText(i18n.str("rowspan")); //$NON-NLS-1$
            rowSpanCB.addItemListener(new java.awt.event.ItemListener()
            {
                public void itemStateChanged(java.awt.event.ItemEvent e)
                {
                    rowSpanField.setEnabled(rowSpanCB.isSelected());
                }
            });
        }
        return rowSpanCB;
    }

    /**
     * This method initializes colSpanField	
     * 	
     * @return javax.swing.JSpinner	
     */
    private JSpinner getColSpanField()
    {
        if(colSpanField == null)
        {
            colSpanField = new JSpinner(new SpinnerNumberModel(1, 1, 999, 1));
            
        }
        return colSpanField;
    }

    /**
     * This method initializes rowSpanField	
     * 	
     * @return javax.swing.JSpinner	
     */
    private JSpinner getRowSpanField()
    {
        if(rowSpanField == null)
        {
            rowSpanField = new JSpinner(new SpinnerNumberModel(1, 1, 999, 1));
            
        }
        return rowSpanField;
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

}  //  @jve:decl-index=0:visual-constraint="42,7"

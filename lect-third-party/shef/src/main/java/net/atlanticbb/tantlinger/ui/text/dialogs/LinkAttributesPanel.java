/*
 * Created on Jan 15, 2006
 *
 */
package net.atlanticbb.tantlinger.ui.text.dialogs;

import java.awt.GridBagLayout;
import javax.swing.JCheckBox;
import java.awt.GridBagConstraints;
import javax.swing.JTextField;
import javax.swing.JComboBox;
import javax.swing.JPanel;

import net.atlanticbb.tantlinger.ui.text.TextEditPopupManager;




public class LinkAttributesPanel extends HTMLAttributeEditorPanel
{
    /**
     * 
     */
    private static final long serialVersionUID = 1L;
    private static final String NEW_WIN = "New Window"; //$NON-NLS-1$
    private static final String SAME_WIN = "Same Window"; //$NON-NLS-1$
    private static final String SAME_FRAME = "Same Frame"; //$NON-NLS-1$
    private static final String TARGET_LABELS[] =
    {NEW_WIN, SAME_WIN, SAME_FRAME};
    private static final String TARGETS[] = {"_blank", "_top", "_self"}; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
    private JCheckBox nameCB = null;
    private JCheckBox titleCB = null;
    private JCheckBox openInCB = null;
    private JTextField nameField = null;
    private JTextField titleField = null;
    private JComboBox openInCombo = null;
    private JPanel spacerPanel = null; 
    
    

    /**
     * This method initializes 
     * 
     */
    public LinkAttributesPanel() 
    {
        super();        
        initialize();
        updateComponentsFromAttribs();
    }
    
    
    
    public void setEnabled(boolean b)
    {
        super.setEnabled(b);
        nameCB.setEnabled(b);
        titleCB.setEnabled(b);
        openInCB.setEnabled(b);
        nameField.setEditable(nameCB.isSelected() && b);
        titleField.setEditable(titleCB.isSelected()&& b);
        openInCombo.setEnabled(openInCB.isSelected() && b);
    }

    /**
     * This method initializes this
     * 
     */
    private void initialize() 
    {
        GridBagConstraints gridBagConstraints6 = new GridBagConstraints();
        gridBagConstraints6.gridx = 0;
        gridBagConstraints6.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints6.weighty = 1.0;
        gridBagConstraints6.weightx = 0.0;
        gridBagConstraints6.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints6.gridwidth = 2;
        gridBagConstraints6.gridy = 3;
        GridBagConstraints gridBagConstraints5 = new GridBagConstraints();
        gridBagConstraints5.fill = java.awt.GridBagConstraints.NONE;
        gridBagConstraints5.gridy = 2;
        gridBagConstraints5.weightx = 1.0;
        gridBagConstraints5.insets = new java.awt.Insets(0,0,5,0);
        gridBagConstraints5.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints5.gridx = 1;
        GridBagConstraints gridBagConstraints4 = new GridBagConstraints();
        gridBagConstraints4.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints4.gridy = 1;
        gridBagConstraints4.weightx = 1.0;
        gridBagConstraints4.insets = new java.awt.Insets(0,0,5,0);
        gridBagConstraints4.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints4.gridx = 1;
        GridBagConstraints gridBagConstraints3 = new GridBagConstraints();
        gridBagConstraints3.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints3.gridy = 0;
        gridBagConstraints3.weightx = 1.0;
        gridBagConstraints3.insets = new java.awt.Insets(0,0,5,0);
        gridBagConstraints3.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints3.gridx = 1;
        GridBagConstraints gridBagConstraints2 = new GridBagConstraints();
        gridBagConstraints2.gridx = 0;
        gridBagConstraints2.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints2.insets = new java.awt.Insets(0,0,5,5);
        gridBagConstraints2.gridy = 2;
        GridBagConstraints gridBagConstraints1 = new GridBagConstraints();
        gridBagConstraints1.gridx = 0;
        gridBagConstraints1.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints1.insets = new java.awt.Insets(0,0,5,5);
        gridBagConstraints1.gridy = 1;
        GridBagConstraints gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(0,0,5,5);
        gridBagConstraints.gridy = 0;
        this.setLayout(new GridBagLayout());
        this.setSize(new java.awt.Dimension(320,118));
        this.setBorder(javax.swing.BorderFactory.createCompoundBorder(javax.swing.BorderFactory.createTitledBorder(null, i18n.str("attributes"), javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.DEFAULT_POSITION, null, null), javax.swing.BorderFactory.createEmptyBorder(5,5,5,5))); //$NON-NLS-1$
        this.add(getNameCB(), gridBagConstraints);
        this.add(getTitleCB(), gridBagConstraints1);
        this.add(getOpenInCB(), gridBagConstraints2);
        this.add(getNameField(), gridBagConstraints3);
        this.add(getTitleField(), gridBagConstraints4);
        this.add(getOpenInCombo(), gridBagConstraints5);
        this.add(getSpacerPanel(), gridBagConstraints6);
        
        
        TextEditPopupManager.getInstance().registerJTextComponent(nameField);
        TextEditPopupManager.getInstance().registerJTextComponent(titleField);
        
        setOpaque(false);
    }

    public void updateComponentsFromAttribs()
    {
        if(attribs.containsKey("name")) //$NON-NLS-1$
        {
            nameCB.setSelected(true);
            nameField.setEditable(true);
            nameField.setText(attribs.get("name").toString()); //$NON-NLS-1$
        }
        else 
        {
            nameCB.setSelected(false);
            nameField.setEditable(false);
        }
        
        
        if(attribs.containsKey("title")) //$NON-NLS-1$
        {
            titleCB.setSelected(true);
            titleField.setEditable(true);
            titleField.setText(attribs.get("title").toString()); //$NON-NLS-1$
        }
        else
        {
            titleCB.setSelected(false);
            titleField.setEditable(false);
        }
        
        if(attribs.containsKey("target")) //$NON-NLS-1$
        {
            openInCB.setSelected(true);
            String val = attribs.get("target").toString(); //$NON-NLS-1$
            openInCombo.setEnabled(true);
            for(int i = 0; i < TARGETS.length; i++)
            {
                if(val.equals(TARGETS[i]))
                {
                    openInCombo.setSelectedIndex(i);
                    break;
                }
            }            
        }
        else
        {
            openInCB.setSelected(false);
            openInCombo.setEnabled(false);
        }
    }

    public void updateAttribsFromComponents()
    {
        if(openInCB.isSelected())
            attribs.put("target", TARGETS[openInCombo.getSelectedIndex()]); //$NON-NLS-1$
        else
            attribs.remove("target"); //$NON-NLS-1$
        
        if(titleCB.isSelected())
            attribs.put("title", titleField.getText()); //$NON-NLS-1$
        else
            attribs.remove("title"); //$NON-NLS-1$
        
        if(nameCB.isSelected())
            attribs.put("name", nameField.getText()); //$NON-NLS-1$
        else
            attribs.remove("name"); //$NON-NLS-1$
    }

    /**
     * This method initializes nameCB	
     * 	
     * @return javax.swing.JCheckBox	
     */
    private JCheckBox getNameCB()
    {
        if(nameCB == null)
        {
            nameCB = new JCheckBox();
            nameCB.setText(i18n.str("name")); //$NON-NLS-1$
            nameCB.setOpaque(false);
            nameCB.addItemListener(new java.awt.event.ItemListener()
            {
                public void itemStateChanged(java.awt.event.ItemEvent e)
                {
                    nameField.setEditable(nameCB.isSelected());
                }
            });
        }
        return nameCB;
    }

    /**
     * This method initializes titleCB	
     * 	
     * @return javax.swing.JCheckBox	
     */
    private JCheckBox getTitleCB()
    {
        if(titleCB == null)
        {
            titleCB = new JCheckBox();
            titleCB.setText(i18n.str("title")); //$NON-NLS-1$
            titleCB.setOpaque(false);
            titleCB.addItemListener(new java.awt.event.ItemListener()
            {
                public void itemStateChanged(java.awt.event.ItemEvent e)
                {
                    titleField.setEditable(titleCB.isSelected());
                }
            });
        }
        return titleCB;
    }

    /**
     * This method initializes openInCB	
     * 	
     * @return javax.swing.JCheckBox	
     */
    private JCheckBox getOpenInCB()
    {
        if(openInCB == null)
        {
            openInCB = new JCheckBox();
            openInCB.setText(i18n.str("open_in")); //$NON-NLS-1$
            openInCB.setOpaque(false);
            openInCB.addItemListener(new java.awt.event.ItemListener()
            {
                public void itemStateChanged(java.awt.event.ItemEvent e)
                {
                    openInCombo.setEnabled(openInCB.isSelected());
                }
            });
        }
        return openInCB;
    }

    /**
     * This method initializes nameField	
     * 	
     * @return javax.swing.JTextField	
     */
    private JTextField getNameField()
    {
        if(nameField == null)
        {
            nameField = new JTextField();
        }
        return nameField;
    }

    /**
     * This method initializes titleField	
     * 	
     * @return javax.swing.JTextField	
     */
    private JTextField getTitleField()
    {
        if(titleField == null)
        {
            titleField = new JTextField();
        }
        return titleField;
    }

    /**
     * This method initializes openInCombo	
     * 	
     * @return javax.swing.JComboBox	
     */
    private JComboBox getOpenInCombo()
    {
        if(openInCombo == null)
        {
            openInCombo = new JComboBox(TARGET_LABELS);
        }
        return openInCombo;
    }

    /**
     * This method initializes spacerPanel	
     * 	
     * @return javax.swing.JPanel	
     */
    private JPanel getSpacerPanel()
    {
        if(spacerPanel == null)
        {
            spacerPanel = new JPanel();
            spacerPanel.setOpaque(false);
        }
        return spacerPanel;
    }

}  //  @jve:decl-index=0:visual-constraint="10,10"

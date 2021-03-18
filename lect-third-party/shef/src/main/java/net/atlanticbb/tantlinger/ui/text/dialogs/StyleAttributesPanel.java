/*
 * Created on Jan 17, 2006
 *
 */
package net.atlanticbb.tantlinger.ui.text.dialogs;

import java.awt.GridBagLayout;
import javax.swing.JLabel;
import java.awt.GridBagConstraints;
import javax.swing.JTextField;

import net.atlanticbb.tantlinger.ui.text.TextEditPopupManager;

import java.util.*;


public class StyleAttributesPanel extends HTMLAttributeEditorPanel
{
    /**
     * 
     */
    private static final long serialVersionUID = 1L;
    private JLabel classLabel = null;
    private JLabel idLabel = null;
    private JTextField classField = null;
    private JTextField idField = null;
    
    /**
     * This method initializes 
     * 
     */
    public StyleAttributesPanel() 
    {
    	this(new Hashtable());
    }
    
    public StyleAttributesPanel(Hashtable attr) 
    {
        super();
        initialize();
        setAttributes(attr);
        this.updateComponentsFromAttribs();
    }

    /**
     * This method initializes this
     * 
     */
    private void initialize() {
        GridBagConstraints gridBagConstraints4 = new GridBagConstraints();
        gridBagConstraints4.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints4.gridy = 1;
        gridBagConstraints4.weightx = 1.0;
        gridBagConstraints4.insets = new java.awt.Insets(0,0,5,0);
        gridBagConstraints4.gridx = 1;
        GridBagConstraints gridBagConstraints3 = new GridBagConstraints();
        gridBagConstraints3.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints3.gridy = 0;
        gridBagConstraints3.weightx = 1.0;
        gridBagConstraints3.insets = new java.awt.Insets(0,0,5,0);
        gridBagConstraints3.weighty = 0.0;
        gridBagConstraints3.gridx = 1;
        GridBagConstraints gridBagConstraints1 = new GridBagConstraints();
        gridBagConstraints1.gridx = 0;
        gridBagConstraints1.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints1.insets = new java.awt.Insets(0,0,5,5);
        gridBagConstraints1.gridy = 1;
        idLabel = new JLabel();
        idLabel.setText(i18n.str("id")); //$NON-NLS-1$
        GridBagConstraints gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(0,0,5,5);
        gridBagConstraints.gridy = 0;
        classLabel = new JLabel();
        classLabel.setText(i18n.str("class")); //$NON-NLS-1$
        this.setLayout(new GridBagLayout());
        this.setSize(new java.awt.Dimension(210,60));
        this.setPreferredSize(new java.awt.Dimension(210,60));
        this.setBorder(javax.swing.BorderFactory.createEmptyBorder(5,5,5,5));
        this.add(classLabel, gridBagConstraints);
        this.add(idLabel, gridBagConstraints1);
        this.add(getClassField(), gridBagConstraints3);
        this.add(getIdField(), gridBagConstraints4);
        
        TextEditPopupManager popupMan = TextEditPopupManager.getInstance();
        popupMan.registerJTextComponent(classField);
        popupMan.registerJTextComponent(idField);
    		
    }

    public void updateComponentsFromAttribs()
    {
        if(attribs.containsKey("class")) //$NON-NLS-1$
            classField.setText(attribs.get("class").toString()); //$NON-NLS-1$
        else
            classField.setText(""); //$NON-NLS-1$
        
        if(attribs.containsKey("id")) //$NON-NLS-1$
            idField.setText(attribs.get("id").toString()); //$NON-NLS-1$
        else
            idField.setText("");         //$NON-NLS-1$
    }

    public void updateAttribsFromComponents()
    {
        if(!classField.getText().equals("")) //$NON-NLS-1$
            attribs.put("class", classField.getText()); //$NON-NLS-1$
        else
            attribs.remove("class"); //$NON-NLS-1$
        
        if(!idField.getText().equals("")) //$NON-NLS-1$
            attribs.put("id", idField.getText()); //$NON-NLS-1$
        else
            attribs.remove("id"); //$NON-NLS-1$

    }

    /**
     * This method initializes classField	
     * 	
     * @return javax.swing.JTextField	
     */
    private JTextField getClassField()
    {
        if(classField == null)
        {
            classField = new JTextField();
        }
        return classField;
    }

    /**
     * This method initializes idField	
     * 	
     * @return javax.swing.JTextField	
     */
    private JTextField getIdField()
    {
        if(idField == null)
        {
            idField = new JTextField();
        }
        return idField;
    }

}  //  @jve:decl-index=0:visual-constraint="10,10"

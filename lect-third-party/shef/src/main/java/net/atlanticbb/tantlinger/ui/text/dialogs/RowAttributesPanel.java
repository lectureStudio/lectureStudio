/*
 * Created on Dec 24, 2005
 *
 */
package net.atlanticbb.tantlinger.ui.text.dialogs;

import javax.swing.JPanel;
import java.awt.GridBagLayout;
import java.awt.GridBagConstraints;
import java.util.*;

public class RowAttributesPanel extends HTMLAttributeEditorPanel
{
    /**
     * 
     */
    private static final long serialVersionUID = 1L;
    private AlignmentAttributesPanel alignPanel = null;
    private BGColorPanel bgColorPanel = null;
    private JPanel expansionPanel = null;

    /**
     * This is the default constructor
     */
    public RowAttributesPanel()
    {
        this(new Hashtable());
    }
    
    public RowAttributesPanel(Hashtable attr)
    {
        super(attr);
        initialize();
        alignPanel.setAttributes(getAttributes());
        updateComponentsFromAttribs();  
    }
    
    public void updateComponentsFromAttribs()
    {
        if(attribs.containsKey("bgcolor"))
        {
            bgColorPanel.setSelected(true);
            bgColorPanel.setColor(attribs.get("bgcolor").toString());
        }
        
        alignPanel.updateComponentsFromAttribs();
    }
    
    
    public void updateAttribsFromComponents()
    {
        if(bgColorPanel.isSelected())
            attribs.put("bgcolor", bgColorPanel.getColor());
        else
            attribs.remove("bgcolor");
        alignPanel.updateAttribsFromComponents();
    }
    
    public void setComponentStates(Hashtable attribs)
    {
        if(attribs.containsKey("bgcolor"))
        {
            bgColorPanel.setSelected(true);
            bgColorPanel.setColor(attribs.get("bgcolor").toString());
        }
        
        alignPanel.setComponentStates(attribs);
        
    }
    
    public void setAttributes(Map attr)
    {
        alignPanel.setAttributes(attr);
        super.setAttributes(attr);
    }

    /**
     * This method initializes this
     * 
     * @return void
     */
    private void initialize()
    {
        GridBagConstraints gridBagConstraints2 = new GridBagConstraints();
        gridBagConstraints2.gridx = 0;
        gridBagConstraints2.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints2.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints2.weighty = 1.0;
        gridBagConstraints2.gridy = 2;
        GridBagConstraints gridBagConstraints1 = new GridBagConstraints();
        gridBagConstraints1.gridx = 0;
        gridBagConstraints1.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints1.weightx = 1.0;
        gridBagConstraints1.gridy = 1;
        GridBagConstraints gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(0,0,5,0);
        gridBagConstraints.weightx = 0.0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.gridy = 0;
        this.setLayout(new GridBagLayout());
        this.setSize(279, 140);
        this.setPreferredSize(new java.awt.Dimension(215,140));
        this.add(getAlignPanel(), gridBagConstraints);
        this.add(getBgColorPanel(), gridBagConstraints1);
        this.add(getExpansionPanel(), gridBagConstraints2);
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
        }
        return alignPanel;
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

}  //  @jve:decl-index=0:visual-constraint="10,10"

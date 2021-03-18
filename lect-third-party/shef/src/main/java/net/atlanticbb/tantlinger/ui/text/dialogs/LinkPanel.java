/*
 * Created on Jan 13, 2006
 *
 */
package net.atlanticbb.tantlinger.ui.text.dialogs;

import java.awt.BorderLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.util.Hashtable;
import java.util.Map;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

import net.atlanticbb.tantlinger.ui.text.TextEditPopupManager;



public class LinkPanel extends HTMLAttributeEditorPanel
{
    /**
     * 
     */
    private static final long serialVersionUID = 1L;
    private JPanel hlinkPanel = null;
    private JLabel urlLabel = null;
    private JLabel textLabel = null;
    private JTextField urlField = null;
    private JTextField textField = null;
    private HTMLAttributeEditorPanel linkAttrPanel = null;
    
    private boolean urlFieldEnabled;    
    
    /**
     * This is the default constructor
     */
    public LinkPanel()
    {
        this(true);           
    }
    
    /**
     * @param attr
     */
    public LinkPanel(boolean urlFieldEnabled)
    {
        this(new Hashtable(), urlFieldEnabled);
    }
    
    public LinkPanel(Hashtable attr, boolean urlFieldEnabled)
    {
        super();
        this.urlFieldEnabled = urlFieldEnabled;
        initialize();
        setAttributes(attr);
        updateComponentsFromAttribs();
    }
    

    public void updateComponentsFromAttribs()
    {
        linkAttrPanel.updateComponentsFromAttribs();
        if(attribs.containsKey("href"))         //$NON-NLS-1$
            urlField.setText(attribs.get("href").toString()); //$NON-NLS-1$
        else 
            urlField.setText("");                        //$NON-NLS-1$
    }    
    
    public void updateAttribsFromComponents()
    {
        linkAttrPanel.updateAttribsFromComponents();        
        attribs.put("href", urlField.getText());               //$NON-NLS-1$
    }
    
    public void setAttributes(Map at)
    {       
        super.setAttributes(at);
        linkAttrPanel.setAttributes(attribs);
    }
    
    public void setLinkText(String text)
    {
        textField.setText(text);
    }
    
    public String getLinkText()
    {
        return textField.getText();
    }

    /**
     * This method initializes this
     * 
     * @return void
     */
    private void initialize()
    {
        this.setLayout(new BorderLayout(5, 5));
        this.setSize(328, 218);
        this.add(getHlinkPanel(), java.awt.BorderLayout.NORTH);
        this.add(getLinkAttrPanel(), BorderLayout.CENTER);
        
        TextEditPopupManager popupMan = TextEditPopupManager.getInstance();//new TextEditPopupManager();
        popupMan.registerJTextComponent(urlField);
        popupMan.registerJTextComponent(textField);
    }

    /**
     * This method initializes hlinkPanel	
     * 	
     * @return javax.swing.JPanel	
     */
    private JPanel getHlinkPanel()
    {
        if(hlinkPanel == null)
        {
            GridBagConstraints gridBagConstraints3 = new GridBagConstraints();
            gridBagConstraints3.fill = java.awt.GridBagConstraints.HORIZONTAL;
            gridBagConstraints3.gridy = 1;
            gridBagConstraints3.weightx = 1.0;
            gridBagConstraints3.gridx = 1;
            GridBagConstraints gridBagConstraints2 = new GridBagConstraints();
            gridBagConstraints2.fill = java.awt.GridBagConstraints.HORIZONTAL;
            gridBagConstraints2.gridy = 0;
            gridBagConstraints2.weightx = 1.0;
            gridBagConstraints2.insets = new java.awt.Insets(0,0,5,0);
            gridBagConstraints2.gridx = 1;
            GridBagConstraints gridBagConstraints1 = new GridBagConstraints();
            gridBagConstraints1.gridx = 0;
            gridBagConstraints1.anchor = java.awt.GridBagConstraints.WEST;
            gridBagConstraints1.insets = new java.awt.Insets(0,0,0,5);
            gridBagConstraints1.gridy = 1;
            textLabel = new JLabel();
            textLabel.setText(i18n.str("text")); //$NON-NLS-1$
            GridBagConstraints gridBagConstraints = new GridBagConstraints();
            gridBagConstraints.gridx = 0;
            gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
            gridBagConstraints.insets = new java.awt.Insets(0,0,0,5);
            gridBagConstraints.gridy = 0;
            urlLabel = new JLabel();
            urlLabel.setText(i18n.str("url")); //$NON-NLS-1$
            hlinkPanel = new JPanel();
            hlinkPanel.setBorder(javax.swing.BorderFactory.createCompoundBorder(javax.swing.BorderFactory.createTitledBorder(null, i18n.str("link"), javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.DEFAULT_POSITION, null, null), javax.swing.BorderFactory.createEmptyBorder(5,5,5,5))); //$NON-NLS-1$
            hlinkPanel.setLayout(new GridBagLayout());
            hlinkPanel.add(urlLabel, gridBagConstraints);
            hlinkPanel.add(textLabel, gridBagConstraints1);
            hlinkPanel.add(getUrlField(), gridBagConstraints2);
            hlinkPanel.add(getTextField(), gridBagConstraints3);
        }
        return hlinkPanel;
    }

    /**
     * This method initializes urlField	
     * 	
     * @return javax.swing.JTextField	
     */
    private JTextField getUrlField()
    {
        if(urlField == null)
        {
            urlField = new JTextField();
            urlField.setEditable(urlFieldEnabled);
            //urlField.setEditable(true);
        }
        return urlField;
    }

    /**
     * This method initializes textField	
     * 	
     * @return javax.swing.JTextField	
     */
    private JTextField getTextField()
    {
        if(textField == null)
        {
            textField = new JTextField();
        }
        return textField;
    }
    
    private JPanel getLinkAttrPanel()
    {
        if(linkAttrPanel == null)
        {
            linkAttrPanel = new LinkAttributesPanel();
        }
        
        return linkAttrPanel;
    }

}  //  @jve:decl-index=0:visual-constraint="29,25"

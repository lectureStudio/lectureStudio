/*
 * Created on Jan 16, 2006
 *
 */
package net.atlanticbb.tantlinger.ui.text.dialogs;

import java.awt.BorderLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.util.*;

import javax.swing.BorderFactory;
import javax.swing.JCheckBox;
import javax.swing.JTextField;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;

import net.atlanticbb.tantlinger.ui.text.HTMLUtils;
import net.atlanticbb.tantlinger.ui.text.TextEditPopupManager;

public class ImagePanel extends HTMLAttributeEditorPanel
{
	private static final long serialVersionUID = 7233959418175136382L;
	private ImageAttributesPanel imageAttrPanel;
    private LinkAttributesPanel linkAttrPanel;
    private JTextField linkUrlField;
    private JCheckBox linkCB;    
    
    public ImagePanel()
    {
        this(new Hashtable());    
    }
    
    public ImagePanel(Hashtable at)
    {
        super();
        initialize();
        setAttributes(at);
        updateComponentsFromAttribs();
    }
    
    private String createAttribs(Map ht)
    {
        String html = ""; //$NON-NLS-1$
        for(Iterator e = ht.keySet().iterator(); e.hasNext();)
        {
            Object k = e.next();
            html += " " + k + "=" + "\"" + ht.get(k) + "\""; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
        }
        
        return html;
    }    

    public void updateComponentsFromAttribs()
    {
        imageAttrPanel.setAttributes(attribs);              
        if(attribs.containsKey("a")) //$NON-NLS-1$
        {
            linkCB.setSelected(true);
            linkAttrPanel.setEnabled(true);
            linkUrlField.setEditable(true);
            Map ht = HTMLUtils.tagAttribsToMap(attribs.get("a").toString()); //$NON-NLS-1$
            if(ht.containsKey("href")) //$NON-NLS-1$
                linkUrlField.setText(ht.get("href").toString()); //$NON-NLS-1$
            else
                linkUrlField.setText(""); //$NON-NLS-1$
            linkAttrPanel.setAttributes(ht);
        }
        else
        {
            linkCB.setSelected(false);
            linkAttrPanel.setEnabled(false);
            linkUrlField.setEditable(false); 
            linkAttrPanel.setAttributes(new HashMap());
        }        
    }   
    
    public void updateAttribsFromComponents()
    {
        imageAttrPanel.updateAttribsFromComponents();
        linkAttrPanel.updateAttribsFromComponents();
        if(linkCB.isSelected())
        {
            Map ht = linkAttrPanel.getAttributes();
            ht.put("href", linkUrlField.getText()); //$NON-NLS-1$
            attribs.put("a", createAttribs(ht)); //$NON-NLS-1$
        }
        else
        {            
            attribs.remove("a"); //$NON-NLS-1$
        }
    }
    
    private void initialize()
    {
    	setOpaque(false);
    	
        JTabbedPane tabs = new JTabbedPane();
        linkAttrPanel = new LinkAttributesPanel();
        
        linkCB = new JCheckBox(i18n.str("link")); //$NON-NLS-1$
        linkCB.setOpaque(false);
        
        linkUrlField = new JTextField();
        //linkUrlField.setEditable(true);
        JPanel urlPanel = new JPanel(new GridBagLayout());
        urlPanel.setOpaque(false);
        
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.anchor = java.awt.GridBagConstraints.WEST;
        gbc.insets = new java.awt.Insets(0,0,5,5);
        gbc.gridy = 0;
        urlPanel.add(linkCB, gbc);
                
        gbc.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gbc.gridy = 0;
        gbc.weightx = 1.0;
        gbc.insets = new java.awt.Insets(0,0,5,0);
        gbc.gridx = 1;
        urlPanel.add(linkUrlField, gbc);        
        
        JPanel linkPanel = new JPanel(new BorderLayout(5, 5));
        linkPanel.add(urlPanel, BorderLayout.NORTH);
        linkPanel.add(linkAttrPanel, BorderLayout.CENTER);        
        linkPanel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));     
        linkPanel.setOpaque(false);
        
        imageAttrPanel = new ImageAttributesPanel();
        imageAttrPanel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
        tabs.addTab(i18n.str("image"), imageAttrPanel); //$NON-NLS-1$
        tabs.addTab(i18n.str("link"), linkPanel);         //$NON-NLS-1$
        
        setLayout(new BorderLayout());
        add(tabs);
        
        linkAttrPanel.setEnabled(linkCB.isSelected());
        linkUrlField.setEditable(linkCB.isSelected());
        linkCB.addItemListener(new java.awt.event.ItemListener()
        {
            public void itemStateChanged(java.awt.event.ItemEvent e)
            {
                linkAttrPanel.setEnabled(linkCB.isSelected());
                linkUrlField.setEditable(linkCB.isSelected());
            }
        });
        
        TextEditPopupManager.getInstance().registerJTextComponent(linkUrlField);
    }

}

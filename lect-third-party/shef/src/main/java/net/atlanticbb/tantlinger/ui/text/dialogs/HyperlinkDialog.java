/*
 * Created on Jan 14, 2006
 *
 */
package net.atlanticbb.tantlinger.ui.text.dialogs;

import java.awt.Dialog;
import java.awt.Frame;
import java.util.Iterator;
import java.util.Map;

import javax.swing.BorderFactory;

import net.atlanticbb.tantlinger.i18n.I18n;

public class HyperlinkDialog extends HTMLOptionDialog
{
	private static final long serialVersionUID = -7801122624571295692L;

	private static final I18n i18n = I18n.getInstance("net.atlanticbb.tantlinger.ui.text.dialogs");
    
    private static String title = i18n.str("hyperlink"); //$NON-NLS-1$
    
    private LinkPanel linkPanel;
    
    public HyperlinkDialog(Frame parent)
    {
        this(parent, title, true);
    }
    
    public HyperlinkDialog(Dialog parent)
    {
        this(parent, title, true);        
    }

    public HyperlinkDialog(Dialog parent, String title, boolean urlFieldEnabled)
    {
        super(parent, title);
        init(urlFieldEnabled);
    }

    public HyperlinkDialog(Frame parent, String title, boolean urlFieldEnabled)
    {
        super(parent, title);
        init(urlFieldEnabled);
    }    
    
    private void init(boolean urlFieldEnabled)
    {
        linkPanel = new LinkPanel(urlFieldEnabled);
        linkPanel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
        setContentPane(linkPanel);
        setSize(315, 370);
        setResizable(false);
    }
    
    public Map getAttributes()
    {
        return linkPanel.getAttributes();
    }
    
    public void setAttributes(Map attribs)
    {
        linkPanel.setAttributes(attribs);
    }
    
    public void setLinkText(String text)
    {
        linkPanel.setLinkText(text);
    }
    
    public String getLinkText()
    {
        return linkPanel.getLinkText();
    }

    public String getHTML()
    {        
        String html = "<a"; //$NON-NLS-1$
        Map ht = getAttributes();
        for(Iterator e = ht.keySet().iterator(); e.hasNext();)
        {
            Object k = e.next();
            html += " " + k + "=" + "\"" + ht.get(k) + "\""; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
        }
        
        html += ">" + getLinkText() + "</a>"; //$NON-NLS-1$ //$NON-NLS-2$
               
        
        return html;
    }

}

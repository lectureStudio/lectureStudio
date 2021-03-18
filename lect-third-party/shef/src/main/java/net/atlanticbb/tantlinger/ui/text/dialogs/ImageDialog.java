/*
 * Created on Jan 14, 2006
 *
 */
package net.atlanticbb.tantlinger.ui.text.dialogs;

import java.awt.Dialog;
import java.awt.Frame;
import java.util.Iterator;
import java.util.Map;

import net.atlanticbb.tantlinger.i18n.I18n;

public class ImageDialog extends HTMLOptionDialog
{
    /**
     * 
     */
    private static final long serialVersionUID = 1L;

    private static final I18n i18n = I18n.getInstance("net.atlanticbb.tantlinger.ui.text.dialogs");
    
    private static String title = i18n.str("image"); //$NON-NLS-1$

    private ImagePanel imagePanel;
    
    public ImageDialog(Frame parent)
    {
        super(parent, title);
        init();
    }

    public ImageDialog(Dialog parent)
    {
        super(parent, title);   
        init();
    }
    
    private void init()
    {
        imagePanel = new ImagePanel();
        setContentPane(imagePanel);
        setSize(500, 345);
        setResizable(false);
    }
    
    public void setImageAttributes(Map attr)
    {
        imagePanel.setAttributes(attr);
    }  
    
    public Map getImageAttributes()
    {
        return imagePanel.getAttributes();
    }

    private String createImgAttributes(Map ht)
    {
        String html = "";
        for(Iterator e = ht.keySet().iterator(); e.hasNext();)
        {
            Object k = e.next();
            
            if (k.toString().equals("a") || k.toString().equals("name"))
                continue;
            
            String v = ht.get(k).toString();
            
            html += " " + k + "=" + "\"" + v + "\"";
        }
        
        return html;
    }
    
    public String getHTML()
    {
		Map imgAttr = imagePanel.getAttributes();
		boolean hasLink = imgAttr.containsKey("a");
		boolean hasBorder = imgAttr.containsKey("border");
		String html = "";
		String style = "";
        
        if (hasBorder) {
        	style = " style=\"border-style: solid; border-width: " + imgAttr.get("border").toString() + "px;\"";
        }
        
		if (hasLink) {
			html = "<a " + imgAttr.get("a") + ">";
		}

		html += "<img" + createImgAttributes(imgAttr) + style + ">";

		if (hasLink)
			html += "</a>";

		return html;
    }
    
}

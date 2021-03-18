/*
 * Created on Mar 4, 2005
 *
 */
package net.atlanticbb.tantlinger.ui.text.dialogs;

import java.awt.Dialog;
import java.awt.Frame;

import net.atlanticbb.tantlinger.ui.OptionDialog;

/**
 * An abstract OptionDialog for HTML editor dialog boxes.
 * 
 * Subclasses should implement dialogs for inserting HTML elements
 * such as tables, links, images, etc.
 * 
 * @author Bob Tantlinger
 *
 */
public abstract class HTMLOptionDialog extends OptionDialog
{    
	private static final long serialVersionUID = 2031840973153103951L;

	public HTMLOptionDialog(Frame parent, String title)
    {
        super(parent, title);        
    }
    
    public HTMLOptionDialog(Dialog parent, String title)
    {
        super(parent, title);        
    }    
    
    /**
     * Gets the generated HTML from the dialog
     * 
     * @return the HTML
     */
    public abstract String getHTML();
}

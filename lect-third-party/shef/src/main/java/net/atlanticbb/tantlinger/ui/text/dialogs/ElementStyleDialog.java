/*
 * Created on Jan 17, 2006
 *
 */
package net.atlanticbb.tantlinger.ui.text.dialogs;

import java.awt.Dialog;
import java.awt.Frame;
import java.util.Map;

import net.atlanticbb.tantlinger.i18n.I18n;
import net.atlanticbb.tantlinger.ui.OptionDialog;

public class ElementStyleDialog extends OptionDialog
{
	private static final long serialVersionUID = 6150645308848319855L;

	private static final I18n i18n = I18n.getInstance("net.atlanticbb.tantlinger.ui.text.dialogs");
    
    private static String title = i18n.str("element_style"); //$NON-NLS-1$
    
    private StyleAttributesPanel stylePanel;

    public ElementStyleDialog(Frame parent)
    {
        super(parent, title);   
        init();
    }

    public ElementStyleDialog(Dialog parent)
    {
        super(parent, title);
        init();
    }
    
    private void init()
    {
        stylePanel = new StyleAttributesPanel();
        setContentPane(stylePanel);        
        pack();
        setSize(300, getHeight());
        setResizable(false);
    }
    
    public void setStyleAttributes(Map attr)
    {
        stylePanel.setAttributes(attr);
    }
    
    public Map getStyleAttributes()
    {
        return stylePanel.getAttributes();
    }
}

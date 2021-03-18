/*
 * Created on Jan 18, 2006
 *
 */
package net.atlanticbb.tantlinger.ui.text.dialogs;

import java.awt.Dialog;
import java.awt.Frame;

import net.atlanticbb.tantlinger.i18n.I18n;
import net.atlanticbb.tantlinger.ui.OptionDialog;

import java.util.*;

public class ListDialog extends OptionDialog
{
	private static final long serialVersionUID = 1234181726595072715L;

	private static final I18n i18n = I18n.getInstance("net.atlanticbb.tantlinger.ui.text.dialogs");
    
    public static final int UNORDERED = ListAttributesPanel.UL_LIST;
    public static final int ORDERED = ListAttributesPanel.OL_LIST;
    
    private static String title = i18n.str("list_properties"); //$NON-NLS-1$
    
    private ListAttributesPanel listAttrPanel;

    public ListDialog(Frame parent)
    {
        super(parent, title);
        init();
    }

    public ListDialog(Dialog parent)
    {
        super(parent, title);
        init();
    }
    
    private void init()
    {
        listAttrPanel = new ListAttributesPanel();
        setContentPane(listAttrPanel);
        pack();
        setSize(220, getHeight());
        setResizable(false);
    }
    
    public void setListType(int t)
    {
        listAttrPanel.setListType(t);
    }
    
    public int getListType()
    {
        return listAttrPanel.getListType();
    }
    
    public void setListAttributes(Map attr)
    {
        listAttrPanel.setAttributes(attr);
    }
    
    public Map getListAttributes()
    {
        return listAttrPanel.getAttributes();
    }
}

/*
 * Created on Jan 24, 2006
 *
 */
package net.atlanticbb.tantlinger.ui.text.actions;

import java.awt.Component;
import java.awt.Dialog;
import java.awt.Frame;
import java.awt.event.ActionEvent;

import javax.swing.Action;
import javax.swing.JEditorPane;
import javax.swing.SwingUtilities;
import net.atlanticbb.tantlinger.ui.UIUtils;
import net.atlanticbb.tantlinger.ui.text.dialogs.SpecialCharDialog;



public class SpecialCharAction extends BasicEditAction
{
    /**
     * 
     */
    private static final long serialVersionUID = 1L;
    SpecialCharDialog dialog;

    public SpecialCharAction()
    {
        super(i18n.str("special_character_"));  //$NON-NLS-1$
        putValue(SMALL_ICON, UIUtils.getIcon(UIUtils.X16, "copyright.png")); //$NON-NLS-1$
        putValue(Action.SHORT_DESCRIPTION, getValue(Action.NAME));
    }
    
    protected void doEdit(ActionEvent e, JEditorPane ed)
    {
        Component c = SwingUtilities.getWindowAncestor(ed);  
        if(dialog == null)
        {
            if(c instanceof Frame)
            {           
                dialog = new SpecialCharDialog((Frame)c, ed);
            }
            else if(c instanceof Dialog)
            {           
                dialog = new SpecialCharDialog((Dialog)c, ed);
            }
            else 
                return;
        }
        
        dialog.setInsertEntity(getEditMode() == SOURCE);   
        if(!dialog.isVisible())
        {
            dialog.setLocationRelativeTo(c);
            dialog.setVisible(true);
        }
    }
    
    protected void updateContextState(JEditorPane editor)
    {
        if(dialog != null)
        {
            dialog.setInsertEntity(getEditMode() == SOURCE);
            dialog.setJTextComponent(editor);
        }
    }

}

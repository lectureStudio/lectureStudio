package net.atlanticbb.tantlinger.ui.text.actions;

import java.awt.event.ActionEvent;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;

import javax.swing.Action;
import javax.swing.JEditorPane;
import javax.swing.KeyStroke;


/**
 * @author Bob 
 * Select all action
 */
public class SelectAllAction extends BasicEditAction
{
    /**
     * 
     */
    private static final long serialVersionUID = 1L;

    public SelectAllAction()
    {
        super(i18n.str("select_all"));
        
        putValue(ACCELERATOR_KEY, 
            KeyStroke.getKeyStroke(KeyEvent.VK_A, InputEvent.CTRL_DOWN_MASK));
        
        putValue(Action.SHORT_DESCRIPTION, getValue(Action.NAME));
    }

    /* (non-Javadoc)
     * @see net.atlanticbb.tantlinger.ui.text.actions.BasicEditAction#doEdit(java.awt.event.ActionEvent, javax.swing.JEditorPane)
     */
    protected void doEdit(ActionEvent e, JEditorPane editor)
    {
        editor.selectAll();
    }   
}
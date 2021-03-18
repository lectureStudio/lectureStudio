/*
 * Created on Feb 28, 2005
 *
 */
package net.atlanticbb.tantlinger.ui.text.actions;

import java.awt.event.ActionEvent;

import javax.swing.Action;
import javax.swing.JEditorPane;
import javax.swing.KeyStroke;
import javax.swing.text.MutableAttributeSet;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyleConstants;
import javax.swing.text.html.HTML;
import javax.swing.text.html.HTMLDocument;
import javax.swing.text.html.HTMLEditorKit;

import org.bushe.swing.action.ShouldBeEnabledDelegate;

/**
 * Action which clears inline text styles
 * 
 * @author Bob Tantlinger
 *
 */
public class ClearStylesAction extends HTMLTextEditAction
{    
    /**
     * 
     */
    private static final long serialVersionUID = 1L;

    public ClearStylesAction()
    {
        super(i18n.str("clear_styles"));
        putValue(ACCELERATOR_KEY, KeyStroke.getKeyStroke("shift ctrl Y"));
        
        addShouldBeEnabledDelegate(new ShouldBeEnabledDelegate()
        {
            public boolean shouldBeEnabled(Action a)
            {                          
                return getEditMode() != SOURCE;
            }
        });
    }

    /* (non-Javadoc)
     * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
     */
    protected void wysiwygEditPerformed(ActionEvent e, JEditorPane editor)
    {
        HTMLDocument document = (HTMLDocument)editor.getDocument();
        HTMLEditorKit kit = (HTMLEditorKit)editor.getEditorKit();
        
        //Element el = document.getCharacterElement(editor.getCaretPosition());
        MutableAttributeSet attrs = new SimpleAttributeSet();
        attrs.addAttribute(StyleConstants.NameAttribute, HTML.Tag.CONTENT);
        
        //int cpos = editor.getCaretPosition();
        int selStart = editor.getSelectionStart();
        int selEnd = editor.getSelectionEnd();        
        
        if(selEnd > selStart)
        {
            document.setCharacterAttributes(selStart, selEnd - selStart, attrs, true);
        }
        
        kit.getInputAttributes().removeAttributes(kit.getInputAttributes());
        kit.getInputAttributes().addAttributes(attrs);
        
        /*//boolean shouldClearSel = false;
        if(editor.getSelectedText() == null)
        {
            editor.replaceSelection("  ");
            editor.setSelectionStart(editor.getCaretPosition() - 1);
            editor.setSelectionEnd(editor.getSelectionStart() + 1); 
            document.setCharacterAttributes(editor.getSelectionStart(), 
                editor.getSelectionEnd() - editor.getSelectionStart(), attrs, true);
            editor.setSelectionStart(editor.getCaretPosition());
            editor.setSelectionEnd(editor.getCaretPosition());
        }
        else
        {
            document.setCharacterAttributes(editor.getSelectionStart(), 
                editor.getSelectionEnd() - editor.getSelectionStart(), attrs, true);
        }*/
    }

    /* (non-Javadoc)
     * @see net.atlanticbb.tantlinger.ui.text.actions.HTMLTextEditAction#sourceEditPerformed(java.awt.event.ActionEvent, javax.swing.JEditorPane)
     */
    protected void sourceEditPerformed(ActionEvent e, JEditorPane editor)
    {
        
    }  
}

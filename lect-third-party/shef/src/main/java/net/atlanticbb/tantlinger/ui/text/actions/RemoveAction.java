/*
 * Created on Jun 12, 2005
 *
 */
package net.atlanticbb.tantlinger.ui.text.actions;

import java.awt.event.ActionEvent;

import javax.swing.Action;
import javax.swing.JEditorPane;
import javax.swing.text.Element;
import javax.swing.text.html.HTML;
import javax.swing.text.html.HTMLDocument;

import net.atlanticbb.tantlinger.ui.text.CompoundUndoManager;
import net.atlanticbb.tantlinger.ui.text.HTMLUtils;

/**
 * Remove Action for Wysiwyg HTML editing
 * 
 * @author Bob Tantlinger
 *
 */
public class RemoveAction extends DecoratedTextAction
{
    /**
     * 
     */
    private static final long serialVersionUID = 1L;
    public static final int BACKSPACE = 0;
    public static final int DELETE = 1;
    
    private int type = BACKSPACE;
    //private Action delegate = null;
    
    public RemoveAction(int type, Action defaultAction)
    {
        super("RemoveAction", defaultAction);
        //delegate = defaultAction;
        this.type = type;
    }
    
    /* (non-Javadoc)
     * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
     */
    public void actionPerformed(ActionEvent e)
    {        
        JEditorPane editor;
        HTMLDocument document;
            
        try
        {
            editor = (JEditorPane)getTextComponent(e);
            document = (HTMLDocument)editor.getDocument();            
        }
        catch(ClassCastException ex)
        {
            delegate.actionPerformed(e);
            return;
        }
        
        Element elem = document.getParagraphElement(editor.getCaretPosition());
        int caretPos = editor.getCaretPosition();
        int start = elem.getStartOffset();
        int end = elem.getEndOffset();        
        boolean noSelection = editor.getSelectedText() == null;                
        
        if(type == DELETE && (end - 1) == caretPos && 
            caretPos != document.getLength() && noSelection)
        {            
            Element nextElem = document.getParagraphElement(caretPos + 1); 
            
            //Do not delete table cells
            Element tdElem = HTMLUtils.getParent(elem, HTML.Tag.TD);            
            if(tdElem != null && caretPos >= (tdElem.getEndOffset() - 1))            
                return;            
            
            Element nextTDElem = HTMLUtils.getParent(nextElem, HTML.Tag.TD);
            if(tdElem == null && nextTDElem != null)
                return;
            
            String curPara = HTMLUtils.getElementHTML(elem, false);
            String html = HTMLUtils.getElementHTML(nextElem, false);            
            html = curPara + html;
            
            CompoundUndoManager.beginCompoundEdit(document);
            try
            {                
                document.setInnerHTML(elem, html);
                HTMLUtils.removeElement(nextElem);
                
                editor.setCaretPosition(caretPos);
            }
            catch(Exception ex)
            {
                ex.printStackTrace();
            }
            CompoundUndoManager.endCompoundEdit(document);
            
            return;           
        }
        
        if(type == BACKSPACE && start == caretPos && caretPos > 1 && noSelection)
        {           
            Element prevElem = document.getParagraphElement(start - 1);            
            
            //do not delete table cells
            Element tdElem = HTMLUtils.getParent(elem, HTML.Tag.TD);
            if(tdElem != null && caretPos < tdElem.getStartOffset() + 1)            
                return;            
            
            Element prevTDElem = HTMLUtils.getParent(prevElem, HTML.Tag.TD);
            if(tdElem == null && prevTDElem != null)
                return;            
            
            int newPos = prevElem.getEndOffset();
            String html = HTMLUtils.getElementHTML(prevElem, false);
            String curPara = HTMLUtils.getElementHTML(elem, false);            
            html = html + curPara;
            
            CompoundUndoManager.beginCompoundEdit(document);
            try
            {                
                document.setInnerHTML(prevElem, html);
                HTMLUtils.removeElement(elem);                
                
                editor.setCaretPosition(newPos - 1);
            }
            catch(Exception ex)
            {
                ex.printStackTrace();
            }
            CompoundUndoManager.endCompoundEdit(document);
            
            return;            
        }
        
        delegate.actionPerformed(e); 
    }    

}

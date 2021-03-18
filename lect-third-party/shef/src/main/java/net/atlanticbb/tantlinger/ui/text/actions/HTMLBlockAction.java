/*
 * Created on Feb 26, 2005
 *
 */
package net.atlanticbb.tantlinger.ui.text.actions;

import java.awt.Event;
import java.awt.event.ActionEvent;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.swing.Action;
import javax.swing.JEditorPane;
import javax.swing.KeyStroke;
import javax.swing.text.BadLocationException;
import javax.swing.text.Element;
import javax.swing.text.html.HTML;
import javax.swing.text.html.HTMLDocument;
import javax.swing.text.html.HTMLEditorKit;

import net.atlanticbb.tantlinger.ui.UIUtils;
import net.atlanticbb.tantlinger.ui.text.CompoundUndoManager;
import net.atlanticbb.tantlinger.ui.text.ElementWriter;
import net.atlanticbb.tantlinger.ui.text.HTMLUtils;

import org.bushe.swing.action.ActionManager;

/**
 * Action which formats HTML block level elements
 * 
 * @author Bob Tantlinger
 *
 */
public class HTMLBlockAction extends HTMLTextEditAction
{    
    private static final long serialVersionUID = 1L;
    public static final int DIV = 0;
    public static final int P = 1;
    public static final int H1 = 2;
    public static final int H2 = 3;
    public static final int H3 = 4;
    public static final int H4 = 5;
    public static final int H5 = 6;
    public static final int H6 = 7;
    public static final int PRE = 8;
    public static final int BLOCKQUOTE = 9;
    public static final int OL = 10;
    public static final int UL = 11;
    public static final int SPAN = 12;
    
        
    private static final int KEYS[] =
    {
        KeyEvent.VK_D, KeyEvent.VK_ENTER, KeyEvent.VK_1, KeyEvent.VK_2, 
        KeyEvent.VK_3, KeyEvent.VK_4, KeyEvent.VK_5, KeyEvent.VK_6, 
        KeyEvent.VK_R, KeyEvent.VK_Q, KeyEvent.VK_N, KeyEvent.VK_U, KeyEvent.VK_H
    };

    public static final String[] ELEMENT_TYPES =
    {
        i18n.str("body_text"), //$NON-NLS-1$
        i18n.str("paragraph"), //$NON-NLS-1$
        i18n.str("heading") + " 1", //$NON-NLS-1$ //$NON-NLS-2$
        i18n.str("heading") + " 2", //$NON-NLS-1$ //$NON-NLS-2$
        i18n.str("heading") + " 3", //$NON-NLS-1$ //$NON-NLS-2$
        i18n.str("heading") + " 4", //$NON-NLS-1$ //$NON-NLS-2$
        i18n.str("heading") + " 5", //$NON-NLS-1$ //$NON-NLS-2$
        i18n.str("heading") + " 6", //$NON-NLS-1$ //$NON-NLS-2$
        i18n.str("preformatted"),          //$NON-NLS-1$
        i18n.str("blockquote"), //$NON-NLS-1$
        i18n.str("ordered_list"), //$NON-NLS-1$
        i18n.str("unordered_list"),         //$NON-NLS-1$       
        i18n.str("hide")
    };   
    
    private int type;
    
    /**
     * Creates a new HTMLBlockAction
     * 
     * @param type A block type - P, PRE, BLOCKQUOTE, H1, H2, etc
     * 
     * @throws IllegalArgumentException
     */
    public HTMLBlockAction(int type) throws IllegalArgumentException
    {        
        super(""); //$NON-NLS-1$
        if(type < 0 || type >= ELEMENT_TYPES.length)
            throw new IllegalArgumentException("Illegal argument"); //$NON-NLS-1$
        
        this.type = type; 
        putValue(NAME, ELEMENT_TYPES[type]);
        putValue(Action.ACCELERATOR_KEY, 
            KeyStroke.getKeyStroke(KEYS[type], InputEvent.ALT_DOWN_MASK));
        if(type == OL)
        {
            putValue(SMALL_ICON,
                UIUtils.getIcon(UIUtils.X16, "listordered.png")); //$NON-NLS-1$
        }
        else if(type == UL)
        {
            putValue(SMALL_ICON,
                UIUtils.getIcon(UIUtils.X16, "listunordered.png")); //$NON-NLS-1$
        }
        else
        {
            
        }
        putValue(ActionManager.BUTTON_TYPE, ActionManager.BUTTON_TYPE_VALUE_RADIO);
        putValue(Action.SHORT_DESCRIPTION, getValue(Action.NAME));
    }
    
    protected void updateWysiwygContextState(JEditorPane ed)
    {
        HTMLDocument document = (HTMLDocument)ed.getDocument();
        Element elem = document.getParagraphElement(ed.getCaretPosition());
               
        String elemName = elem.getName();     
        if(elemName.equals("p-implied")) //$NON-NLS-1$
            elemName = elem.getParentElement().getName();
        
        if(type == DIV || type == SPAN && (elemName.equals("div") || elemName.equals("body") || elemName.equals("td"))) //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
        {
            setSelected(true);
        }
        else if(type == UL)
        {
            Element listElem = HTMLUtils.getListParent(elem);
            setSelected(listElem != null && (listElem.getName().equals("ul")));                   //$NON-NLS-1$
        }
        else if(type == OL)
        {
            Element listElem = HTMLUtils.getListParent(elem);
            setSelected(listElem != null && (listElem.getName().equals("ol")));  //$NON-NLS-1$
        }
        else if(elemName.equals(getTag().toString().toLowerCase()))
        {
            setSelected(true);
        }        
        else
        {
            setSelected(false);
        }
    }
    
    protected void updateSourceContextState(JEditorPane ed)
    {
        setSelected(false);
    }

    protected void sourceEditPerformed(ActionEvent e, JEditorPane editor)
    {
        String tag = getTag().toString();
        String prefix = "\n<" + tag + ">\n\t"; //$NON-NLS-1$ //$NON-NLS-2$
        String postfix = "\n</" + tag + ">\n"; //$NON-NLS-1$ //$NON-NLS-2$
        if(type == OL || type == UL)
        {
            prefix += "<li>"; //$NON-NLS-1$
            postfix = "</li>" + postfix; //$NON-NLS-1$
        }
        
        String sel = editor.getSelectedText();
        if(sel == null)
        {
            editor.replaceSelection(prefix + postfix);
            
            int pos = editor.getCaretPosition() - postfix.length();
            if(pos >= 0)
                editor.setCaretPosition(pos);                             
        }
        else
        {
            sel = prefix + sel + postfix;
            editor.replaceSelection(sel);                
        }
    }

    
    protected void wysiwygEditPerformed(ActionEvent e, JEditorPane editor)
    {               
        HTMLDocument document = (HTMLDocument)editor.getDocument();
        int caret = editor.getCaretPosition();
        CompoundUndoManager.beginCompoundEdit(document);        
        try
        {            
            if(type == OL || type == UL)
            {
                insertList(editor, e);
            }            
            else 
            { 
                changeBlockType(editor, e);
            }
            editor.setCaretPosition(caret);
        }
        catch(Exception awwCrap)
        {
            awwCrap.printStackTrace();
        }        

        CompoundUndoManager.endCompoundEdit(document);      
    }
    
    
    private HTML.Tag getRootTag(Element elem)
    {
        HTML.Tag root = HTML.Tag.BODY;
        if(HTMLUtils.getParent(elem, HTML.Tag.TD) != null)
            root = HTML.Tag.TD;
        return root;
    }
     
    /*private String cutOutElement(Element el) throws BadLocationException
    {
        String txt = HTMLUtils.getElementHTML(el, false);       
        HTMLUtils.removeElement(el);        
        return txt;
    }*/
    
    private void insertHTML(String html, HTML.Tag tag, HTML.Tag root, ActionEvent e)
    {        
        HTMLEditorKit.InsertHTMLTextAction a = 
            new HTMLEditorKit.InsertHTMLTextAction("insertHTML", html, root, tag);             //$NON-NLS-1$
        a.actionPerformed(e);        
    }
    
    private void changeListType(Element listParent, HTML.Tag replaceTag, HTMLDocument document)
    {        
        StringWriter out = new StringWriter();
        ElementWriter w = new ElementWriter(out, listParent);        
        try
        {
            w.write();
            String html = out.toString();        
            html = html.substring(html.indexOf('>') + 1, html.length());
            html = html.substring(0, html.lastIndexOf('<'));        
            html = '<' + replaceTag.toString() + '>' + html + "</" + replaceTag.toString() + '>'; //$NON-NLS-1$
            document.setOuterHTML(listParent, html);    
        }
        catch(Exception idiotic){}
    }
    
    private void insertList(JEditorPane editor, ActionEvent e) 
    throws BadLocationException
    {
        HTMLDocument document = (HTMLDocument)editor.getDocument();
        int caretPos = editor.getCaretPosition();
        Element elem = document.getParagraphElement(caretPos);
        HTML.Tag parentTag = HTML.getTag(elem.getParentElement().getName());
        
        //check if we need to change the list from one type to another
        Element listParent = elem.getParentElement().getParentElement();
        HTML.Tag listTag = HTML.getTag(listParent.getName());        
        if(listTag.equals(HTML.Tag.UL) || listTag.equals(HTML.Tag.OL))
        {
            HTML.Tag t = HTML.getTag(listParent.getName());
            if(type == OL && t.equals(HTML.Tag.UL))
            {                
                changeListType(listParent, HTML.Tag.OL, document);                
                return;
            }
            else if(type == UL && listTag.equals(HTML.Tag.OL))
            {                
                changeListType(listParent, HTML.Tag.UL, document);                
                return;                
            } 
        }        

        if(!parentTag.equals(HTML.Tag.LI))//don't allow nested lists
        {            
            //System.err.println("INSERT LIST");
            changeBlockType(editor, e);
        }       
        else//is already a list, so turn off list
        {   
            HTML.Tag root = getRootTag(elem);
            String txt = HTMLUtils.getElementHTML(elem, false);
            editor.setCaretPosition(elem.getEndOffset());            
            insertHTML("<p>" + txt + "</p>", HTML.Tag.P, root, e); //$NON-NLS-1$ //$NON-NLS-2$
            HTMLUtils.removeElement(elem);
        }
        
    }
    
    
    
    private void changeBlockType(JEditorPane editor, ActionEvent e) 
    throws BadLocationException
    {
        HTMLDocument doc = (HTMLDocument)editor.getDocument();
        Element curE = doc.getParagraphElement(editor.getSelectionStart());
        Element endE = doc.getParagraphElement(editor.getSelectionEnd());
        
        Element curTD = HTMLUtils.getParent(curE, HTML.Tag.TD);
        HTML.Tag tag = getTag();
        HTML.Tag rootTag = getRootTag(curE);
        String html = ""; //$NON-NLS-1$
        
        if(isListType())
        {
            html = "<" + getTag() + ">"; //$NON-NLS-1$ //$NON-NLS-2$
            tag = HTML.Tag.LI;
        }        
                        
        //a list to hold the elements we want to change
        List elToRemove = new ArrayList();
        elToRemove.add(curE);
        
        while(true)
        {            
            html += HTMLUtils.createTag(tag, 
                curE.getAttributes(), HTMLUtils.getElementHTML(curE, false));
            if(curE.getEndOffset() >= endE.getEndOffset()
                || curE.getEndOffset() >= doc.getLength())
                break;
            curE = doc.getParagraphElement(curE.getEndOffset() + 1);
            elToRemove.add(curE);
            
            //did we enter a (different) table cell?
            Element ckTD = HTMLUtils.getParent(curE, HTML.Tag.TD);
            if(ckTD != null && !ckTD.equals(curTD))
                break;//stop here so we don't mess up the table
        }
                
        if(isListType())
            html += "</" + getTag() + ">"; //$NON-NLS-1$ //$NON-NLS-2$
        
        //set the caret to the start of the last selected block element
        editor.setCaretPosition(curE.getStartOffset());
        
        //insert our changed block
        //we insert first and then remove, because of a bug in jdk 6.0
        insertHTML(html, getTag(), rootTag, e);
        
        //now, remove the elements that were changed.
        for(Iterator it = elToRemove.iterator(); it.hasNext();)
        {
            Element c = (Element)it.next();
            HTMLUtils.removeElement(c);
        }
    } 
    
    private boolean isListType()
    {
        return type == OL || type == UL;
    }

    /**
     * Gets the tag 
     * @return
     */
    public HTML.Tag getTag()
    {
        HTML.Tag tag = HTML.Tag.DIV;

        switch(type) 
        {
            case P :
                tag = HTML.Tag.P;
                break;
            case H1 :
                tag = HTML.Tag.H1;
                break;
            case H2 :
                tag = HTML.Tag.H2;
                break;
            case H3 :
                tag = HTML.Tag.H3;
                break;
            case H4 :
                tag = HTML.Tag.H4;
                break;
            case H5 :
                tag = HTML.Tag.H5;
                break;
            case H6 :
                tag = HTML.Tag.H6;
                break;
            case PRE :
                tag = HTML.Tag.PRE;
                break;
            case UL :
                tag = HTML.Tag.UL;
                break;
            case OL :
                tag = HTML.Tag.OL;
                break;
            case BLOCKQUOTE :
                tag = HTML.Tag.BLOCKQUOTE;
                break;
            case DIV :
                tag = HTML.Tag.DIV;
                break;
            case SPAN :
                tag = HTML.Tag.SPAN;
                break;
        }
        
        return tag;
    }
}

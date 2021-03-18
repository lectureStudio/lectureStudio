/*
 * Created on Nov 19, 2007
 */
package net.atlanticbb.tantlinger.ui.text.actions;

import java.awt.event.ActionEvent;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.swing.JEditorPane;
import javax.swing.text.BadLocationException;
import javax.swing.text.Element;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyleConstants;
import javax.swing.text.html.HTML;
import javax.swing.text.html.HTMLDocument;
import javax.swing.text.html.HTMLEditorKit;

import net.atlanticbb.tantlinger.ui.text.CompoundUndoManager;
import net.atlanticbb.tantlinger.ui.text.HTMLUtils;


/**
 * @author Bob Tantlinger
 *
 */
public class IndentAction extends HTMLTextEditAction
{   
    private static final long serialVersionUID = 1L;
    
    public static final int INDENT = 0;
    public static final int OUTDENT = 1;
    
    protected int direction;

    /**
     * @param name
     */
    public IndentAction(int direction) throws IllegalArgumentException
    {
        super("");
        if(direction == INDENT)
            putValue(NAME, "Indent");
        else if(direction == OUTDENT)
            putValue(NAME, "Outdent");
        else 
            throw new IllegalArgumentException("Invalid indentation direction");
        this.direction = direction;
        
    }

    /* (non-Javadoc)
     * @see net.atlanticbb.tantlinger.ui.text.actions.HTMLTextEditAction#sourceEditPerformed(java.awt.event.ActionEvent, javax.swing.JEditorPane)
     */
    protected void sourceEditPerformed(ActionEvent e, JEditorPane editor)
    {
        // TODO Auto-generated method stub

    }
    
    private void insertHTML(String html, HTML.Tag tag, HTML.Tag root, ActionEvent e)
    {        
        HTMLEditorKit.InsertHTMLTextAction a = 
            new HTMLEditorKit.InsertHTMLTextAction("insertHTML", html, root, tag);//$NON-NLS-1$
        a.actionPerformed(e);        
    }
    
    private HTML.Tag getRootTag(Element elem)
    {
        HTML.Tag root = HTML.Tag.BODY;
        if(HTMLUtils.getListParent(elem) != null)
        {
            root = HTML.Tag.UL;
        }
        else if(HTMLUtils.getParent(elem, HTML.Tag.TD) != null)
            root = HTML.Tag.TD;
        else if(HTMLUtils.getParent(elem, HTML.Tag.BLOCKQUOTE) != null)
            root = HTML.Tag.BLOCKQUOTE;
        return root;
    }
    
   
    
    private int getIndentationLevel(Element el)
    {
        int level = 0;
        while((!el.getName().equals("body")) && (!el.getName().equals("td")))
        {
            if(el.getName().equals("blockquote"))
                level++;
            el = el.getParentElement();
        }
        
        return level;
    }
    
    private Map getListElems(List elems)
    {
        Map lis = new HashMap();
        for(Iterator it = elems.iterator(); it.hasNext();)
        {
            Element li = HTMLUtils.getParent((Element)it.next(), HTML.Tag.LI);
            if(li != null)
            {
                Element listEl = HTMLUtils.getListParent(li);
                if(!lis.containsKey(listEl))
                {
                    lis.put(listEl, new ArrayList());
                }
                
                List elList = (List)lis.get(listEl);
                elList.add(li);
            }
        }
        
        return lis;
    }
            
    private void unindent(ActionEvent e, JEditorPane editor)
    {
        List elems = getParagraphElements(editor);
        if(elems.size() == 0)
            return;
        
        List listElems = getLeadingTralingListElems(elems);
        elems.removeAll(listElems);
        
        Set elsToIndent = new HashSet();
        Set elsToOutdent = new HashSet();
        Element lastBqParent = null;
        for(int i = 0; i < elems.size(); i++)
        {
            Element el = (Element)elems.get(i);
            Element bqParent  = HTMLUtils.getParent(el, HTML.Tag.BLOCKQUOTE);
            if(bqParent == null)
                continue;
            
            if(lastBqParent == null || bqParent.getStartOffset() >= lastBqParent.getEndOffset())
            {
                elsToOutdent.add(bqParent);
                lastBqParent = bqParent;    
            }
            
            if(i == 0 || i == elems.size() - 1)
            {
                int c = bqParent.getElementCount();
                for(int j = 0; j < c; j++)
                {
                    Element bqChild = bqParent.getElement(j);
                    int start = bqChild.getStartOffset();
                    int end = bqChild.getEndOffset();
                    if(end < editor.getSelectionStart() || start > editor.getSelectionEnd())
                        elsToIndent.add(bqChild);
                }
            }
        }
           
        HTMLDocument doc = (HTMLDocument)editor.getDocument();
        adjustListElemsIndent(listElems, doc);
        blockquoteElements(new ArrayList(elsToIndent), doc);
        unblockquoteElements(new ArrayList(elsToOutdent), doc);
        
       
    }
    
    private void adjustListElemsIndent(List elems, HTMLDocument doc)
    {
        Set rootLists = new HashSet();
        Set liElems = new HashSet();
        for(int i = 0; i < elems.size(); i++)
        {
            Element liEl = HTMLUtils.getParent((Element)elems.get(i), HTML.Tag.LI);
            if(liEl == null)
                continue;
            liElems.add(liEl);
            Element rootList = HTMLUtils.getListParent(liEl);
            if(rootList != null)
            {
                while(HTMLUtils.getListParent(rootList.getParentElement()) != null)
                {
                    rootList = HTMLUtils.getListParent(rootList.getParentElement());
                }            
                rootLists.add(rootList);
            }
        }
        
        for(Iterator it = rootLists.iterator(); it.hasNext();)
        {
            Element rl = (Element)it.next();
            String newHtml = buildListHTML(rl, new ArrayList(liElems));
            System.err.println(newHtml);
            try
            {
                doc.setInnerHTML(rl, newHtml);
            }
            catch(Exception ex)
            {                
                ex.printStackTrace();
            }
        }
    }
    
    private List getItems(Element list, List selLiElems, int level)
    {
        int c = list.getElementCount();
        List items = new ArrayList();
        for(int i = 0; i < c; i++)
        {
            Element e = list.getElement(i);
            if(e.getName().equals("li"))
            {
                ListItem item = new ListItem();
                item.listTag = HTML.getTag(list.getName());
                item.level = level;
                if(selLiElems.contains(e))
                { 
                    if(direction == INDENT)
                    {
                        item.level++;
                    }
                    else
                    {
                        if(item.level > 0)
                        {
                            item.level--;
                        }
                    }
                }
                item.html = HTMLUtils.getElementHTML(e, true);
                items.add(item);
            }
            else if(HTMLUtils.getListParent(e) == e)
            {
                items.addAll(getItems(e, selLiElems, level + 1));
            }
        }
        return items;
    }
    
    private String buildListHTML(Element list, List liItems)
    {
        List items = getItems(list, liItems, 0);
        ListItem lastItem = null;
        StringBuffer html = new StringBuffer();
        for(int i = 0; i < items.size(); i++)
        {
            ListItem item = (ListItem)items.get(i);
            if(lastItem != null && (lastItem.level != item.level || !lastItem.listTag.equals(item.listTag)))
            {
                if(lastItem.level > item.level)
                {
                    html.append(openOrCloseList(lastItem.listTag, -1 * (lastItem.level - item.level)));
                    html.append(item.html);                    
                }
                else if(item.level > lastItem.level)
                {
                    html.append(openOrCloseList(item.listTag, (item.level - lastItem.level)));
                    html.append(item.html);    
                }
                else
                {
                    //html.append("</" + lastItem.listTag + ">");
                    //html.append("<" + item.listTag + ">");
                    html.append(item.html);
                }
            }
            else
            {
                if(lastItem == null)
                    html.append(openOrCloseList(item.listTag, item.level));
                html.append(item.html);
            }
            
            lastItem = item;            
        }
        
        if(lastItem != null)
            html.append(openOrCloseList(lastItem.listTag, -1 * lastItem.level));
        
        return html.toString();
    }
    
    private String openOrCloseList(HTML.Tag ltag, int level)
    {
        String tag;
        if(level < 0)
            tag = "</" + ltag + ">\n";
        else
            tag = "<" + ltag + ">\n";
        int c = Math.abs(level);
        StringBuffer sb = new StringBuffer();
        for(int i = 0; i < c; i++)
            sb.append(tag);
        return sb.toString();
    }
    
    private class ListItem
    {
        String html;
        int level;
        HTML.Tag listTag;
    }
    
    private List getLeadingTralingListElems(List elems)
    {
        Set listElems = new HashSet();        
        for(int i = 0; i < elems.size(); i++)
        {            
            Element el = (Element)elems.get(i);
            if(HTMLUtils.getListParent(el) != null)
                listElems.add(el);
            else
                break;
        }
        
        for(int i = elems.size() - 1; i >= 0 ; i--)
        {
            Element el = (Element)elems.get(i);
            if(HTMLUtils.getListParent(el) != null)
                listElems.add(el);
            else
                break;
        }
        
        return new ArrayList(listElems);
    }
    
    private void indent1(ActionEvent e, JEditorPane editor)
    {
        List elems = getParagraphElements(editor);
        if(elems.size() == 0)
            return;
        
        List listElems = this.getLeadingTralingListElems(elems);
        elems.removeAll(listElems);
        HTMLDocument doc = (HTMLDocument)editor.getDocument();
        blockquoteElements(elems, doc);
        adjustListElemsIndent(listElems, doc);
    }
    
    private void indent(ActionEvent e, JEditorPane editor)
    {
        List elems = getParagraphElements(editor);
        if(elems.size() == 0)
            return;
        
        HTMLDocument doc = (HTMLDocument)editor.getDocument();
        List nonListElems = new LinkedList();
        for(Iterator it = elems.iterator(); it.hasNext();)
        {
            Element el = (Element)it.next();
            if(HTMLUtils.getListParent(el) == null)
            {
                nonListElems.add(el);
                it.remove();
            }
        }        
        blockquoteElements(nonListElems, doc);
        
        //now list elements are left over        
        Map listEls = getListElems(elems);
        for(Iterator it = listEls.keySet().iterator(); it.hasNext();)
        {
            Element listParent = (Element)it.next();            
            List liElems = (List)listEls.get(listParent);
            StringBuffer sb = new StringBuffer();
            sb.append("<" + listParent.getName() + ">\n");
            for(int i = 0; i < liElems.size(); i++)
            {
                Element liElem = (Element)liElems.get(i);
                sb.append(HTMLUtils.getElementHTML(liElem, true));                
            }
            sb.append("</" + listParent.getName() + ">\n");
            System.err.println(sb);
            
            for(int i = liElems.size() - 1; i >= 0; i--)
            {
                Element liElem = (Element)liElems.get(i);
                try
                {
                    if(i == 0)
                    {
                        doc.setOuterHTML(liElem, sb.toString());
                    }
                    else
                    {
                        HTMLUtils.removeElement(liElem);
                    }
                }
                catch(Exception ble)
                {
                    ble.printStackTrace();
                }
            }
        }        
    }
    
    private void unblockquoteElements(List elems, HTMLDocument doc)
    {
        for(Iterator it = elems.iterator(); it.hasNext();)
        {            
            Element curE = (Element)it.next();
            if(!curE.getName().equals("blockquote"))
                continue;
            
            String eleHtml = HTMLUtils.getElementHTML(curE, false);            
            HTML.Tag t = HTMLUtils.getStartTag(eleHtml);
            if(t == null || !t.breaksFlow())
                eleHtml = "<p>\n" + eleHtml + "</p>\n";            
                     
            try
            {      
                doc.setOuterHTML(curE, eleHtml);                
            }            
            catch(Exception ex)
            {
                ex.printStackTrace();
            }
        }        
    }
    
    private void blockquoteElements(List elems, HTMLDocument doc)
    {
        for(Iterator it = elems.iterator(); it.hasNext();)
        {            
            Element curE = (Element)it.next();            
            String eleHtml = HTMLUtils.getElementHTML(curE, true);
            StringBuffer sb = new StringBuffer();
            sb.append("<blockquote>\n");
            sb.append(eleHtml);
            sb.append("</blockquote>\n");            
            try
            {      
                doc.setOuterHTML(curE, sb.toString());                
            }            
            catch(Exception ex)
            {
                ex.printStackTrace();
            }
        }        
    }
    
    
    
    public List getParagraphElements(JEditorPane editor)
    {
        List elems = new ArrayList();
        try
        {
            HTMLDocument doc = (HTMLDocument)editor.getDocument();        
            Element curE = getParaElement(doc, editor.getSelectionStart());
            Element endE = getParaElement(doc, editor.getSelectionEnd());
            
            while(curE.getEndOffset() <= endE.getEndOffset())
            {               
                elems.add(curE);
                curE = getParaElement(doc, curE.getEndOffset() + 1);
                if(curE.getEndOffset() >= doc.getLength())
                    break;
            }
        }
        catch(ClassCastException cce){}
        
        return elems;
    }
    
    private Element getParaElement(HTMLDocument doc, int pos)
    {
        Element curE = doc.getParagraphElement(pos);
        /*while(HTMLUtils.isImplied(curE))
        {
            curE = curE.getParentElement();
        }*/
        
        /*Element lp = HTMLUtils.getListParent(curE);
        if(lp != null)
            curE = lp;*/
        
        
        return curE;
    }
    
    
    
    

    /* (non-Javadoc)
     * @see net.atlanticbb.tantlinger.ui.text.actions.HTMLTextEditAction#wysiwygEditPerformed(java.awt.event.ActionEvent, javax.swing.JEditorPane)
     */
    protected void wysiwygEditPerformed(ActionEvent e, JEditorPane editor) 
    {
        int cp = editor.getCaretPosition();
        CompoundUndoManager.beginCompoundEdit(editor.getDocument());
        if(direction == INDENT)
            indent1(e, editor);
        else
            unindent(e, editor);            
        CompoundUndoManager.endCompoundEdit(editor.getDocument());
        editor.setCaretPosition(cp);
    }

}

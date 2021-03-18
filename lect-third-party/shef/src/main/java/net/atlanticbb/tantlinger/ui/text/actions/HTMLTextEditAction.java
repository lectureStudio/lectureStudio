/*
 * Created on Feb 26, 2005
 *
 */
package net.atlanticbb.tantlinger.ui.text.actions;

import java.awt.event.ActionEvent;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import javax.swing.Action;
import javax.swing.JEditorPane;
import javax.swing.text.AttributeSet;
import javax.swing.text.Element;
import javax.swing.text.StyleConstants;
import javax.swing.text.html.HTML;
import javax.swing.text.html.HTMLDocument;
import javax.swing.text.html.HTMLEditorKit;

import org.bushe.swing.action.ShouldBeEnabledDelegate;

import net.atlanticbb.tantlinger.i18n.I18n;
import net.atlanticbb.tantlinger.ui.DefaultAction;
import net.atlanticbb.tantlinger.ui.text.CompoundUndoManager;
import net.atlanticbb.tantlinger.ui.text.HTMLUtils;


/**
 * @author Bob Tantlinger
 *
 */
public abstract class HTMLTextEditAction extends DefaultAction
{
	private static final long serialVersionUID = 7336575716765780135L;

	static final I18n i18n = I18n.getInstance("net.atlanticbb.tantlinger.ui.text.actions");
    
    public static final String EDITOR = "editor";
    
    public static final int DISABLED = -1;
    public static final int WYSIWYG = 0;
    public static final int SOURCE = 1;
    
    public static final int TABLE_PROPS = 0;
    public static final int LIST_PROPS = 1;
    public static final int IMG_PROPS = 2;
    public static final int LINK_PROPS = 3;
    public static final int ELEM_PROPS = 4; 
    
    public HTMLTextEditAction(String name)
    {
        super(name);
        addShouldBeEnabledDelegate(new ShouldBeEnabledDelegate()
        {
            public boolean shouldBeEnabled(Action a)
            {                          
                return getEditMode() != DISABLED;
            }
        });
        updateEnabledState();
    }   
    
    /* (non-Javadoc)
     * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
     */
    public void execute(ActionEvent e) throws Exception
    {      
       if(getEditMode() == WYSIWYG)           
           wysiwygEditPerformed(e, getCurrentEditor());
       else if(getEditMode() == SOURCE)
           sourceEditPerformed(e, getCurrentEditor());            
    }
    
    public int getEditMode()
    {        
        JEditorPane ep = getCurrentEditor();
        if(ep == null)
            return DISABLED;
        if(ep.getDocument() instanceof HTMLDocument && ep.getEditorKit() instanceof HTMLEditorKit)           
            return WYSIWYG;
        return SOURCE;        
    }
    
    protected JEditorPane getCurrentEditor()
    {
        try
        {
            JEditorPane ep = (JEditorPane)getContextValue(EDITOR);
            return ep;
        }
        catch(ClassCastException cce)
        {
            //cce.printStackTrace();
        }
        
        return null;
    }
    
    protected void actionPerformedCatch(Throwable t)
    {        
        t.printStackTrace();
    }
    
    protected void contextChanged()
    {
        if(getEditMode() == WYSIWYG)
            updateWysiwygContextState(getCurrentEditor());
        else if(getEditMode() == SOURCE)
            updateSourceContextState(getCurrentEditor());
    }
    
    protected Map getAttribs(Element elem)
    {
        Map at = new HashMap();
        
        AttributeSet a = elem.getAttributes();
        for(Enumeration e = a.getAttributeNames(); e.hasMoreElements();)
        {
            Object n = e.nextElement();
            //dont return the name attribute
            if(n.toString().equals("name") && !elem.getName().equals("a"))
                continue;
            at.put(n.toString(), a.getAttribute(n).toString());
        }            
                
        return at;
    }
    
    protected String getElementHTML(Element el, Map attribs)
    {
        String html = "<" + el.getName();
        for(Iterator e = attribs.keySet().iterator(); e.hasNext();)
        {
            Object name = e.next();
            Object val = attribs.get(name);
            html += " " + name + "=\"" + val + "\"";
        }        

        String txt = HTMLUtils.getElementHTML(el, false);
        html += ">\n" + txt + "\n</" + el.getName() + ">"; 
        
        return html;
    }
    
    /**
     * Computes the (inline or block) element at the focused editor's caret position
     * @return the element, or null of the element cant be retrieved
     */
    protected Element elementAtCaretPosition(JEditorPane ed)
    {                        
        if(ed == null)
        	return null;
    	
    	HTMLDocument doc = (HTMLDocument)ed.getDocument();
        int caret = ed.getCaretPosition();
        
        Element elem = doc.getParagraphElement(caret);
        HTMLDocument.BlockElement blockElem = (HTMLDocument.BlockElement)elem;
        return blockElem.positionToElement(caret);            
    }
    
    protected void replace(Element elem, String html)
    {
        HTMLDocument document = null;
        try
        {
            document = (HTMLDocument)elem.getDocument();
        }
        catch(Exception ex)
        {
            ex.printStackTrace();
        }
        
        CompoundUndoManager.beginCompoundEdit(document);
        try
        {
            document.setOuterHTML(elem, html);
        }
        catch(Exception ex)
        {
            ex.printStackTrace();
        }
        CompoundUndoManager.endCompoundEdit(document);
    }
    
    protected int getElementType(Element elem)
    {
        AttributeSet att = elem.getAttributes();
        String name = att.getAttribute(StyleConstants.NameAttribute).toString();
        
        //is it an image?
        if(name.equals("img"))
            return IMG_PROPS;
        
        //is it a link?
        for(Enumeration ee = att.getAttributeNames(); ee.hasMoreElements();)        
            if(ee.nextElement().toString().equals("a"))
                return LINK_PROPS;        
        
        //is it a list?
        if(HTMLUtils.getParent(elem, HTML.Tag.UL) != null)
            return LIST_PROPS;
        
        if(HTMLUtils.getParent(elem, HTML.Tag.OL) != null)
            return LIST_PROPS;
        
        //is it a table?
        if(HTMLUtils.getParent(elem, HTML.Tag.TD) != null)
            return TABLE_PROPS;
        
        //return the default
        return ELEM_PROPS;
    }
    
    protected void updateWysiwygContextState(JEditorPane wysEditor)
    {
        
    }
    
    protected void updateSourceContextState(JEditorPane srcEditor)
    {
        
    }
    
    protected abstract void wysiwygEditPerformed(ActionEvent e, JEditorPane editor);
        
    protected abstract void sourceEditPerformed(ActionEvent e, JEditorPane editor);
       
}

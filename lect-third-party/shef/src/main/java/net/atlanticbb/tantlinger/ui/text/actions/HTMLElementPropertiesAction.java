/*
 * Created on Jan 14, 2006
 *
 */
package net.atlanticbb.tantlinger.ui.text.actions;

import java.awt.Component;
import java.awt.Dialog;
import java.awt.Frame;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import javax.swing.Action;
import javax.swing.JEditorPane;
import javax.swing.SwingUtilities;
import javax.swing.text.BadLocationException;
import javax.swing.text.Element;
import javax.swing.text.html.HTML;
import javax.swing.text.html.HTMLDocument;

import net.atlanticbb.tantlinger.ui.text.CompoundUndoManager;
import net.atlanticbb.tantlinger.ui.text.HTMLUtils;
import net.atlanticbb.tantlinger.ui.text.dialogs.ElementStyleDialog;
import net.atlanticbb.tantlinger.ui.text.dialogs.HyperlinkDialog;
import net.atlanticbb.tantlinger.ui.text.dialogs.ImageDialog;
import net.atlanticbb.tantlinger.ui.text.dialogs.ListDialog;
import net.atlanticbb.tantlinger.ui.text.dialogs.TablePropertiesDialog;

import org.bushe.swing.action.ShouldBeEnabledDelegate;

/**
 * Action for editing an element's properties depending on the 
 * current caret position.
 * 
 * Currently supports links, images, tables, lists, and paragraphs.
 * 
 * @author Bob Tantlinger
 *
 */
public class HTMLElementPropertiesAction extends HTMLTextEditAction
{
    /**
     * 
     */
    private static final long serialVersionUID = 1L;
    public static final int TABLE_PROPS = 0;
    public static final int LIST_PROPS = 1;
    public static final int IMG_PROPS = 2;
    public static final int LINK_PROPS = 3;
    public static final int ELEM_PROPS = 4;        
    
    public static final String PROPS[] =
    {
        i18n.str("table_properties_"),
        i18n.str("list_properties_"),
        i18n.str("image_properties_"),
        i18n.str("hyperlink_properties_"),
        i18n.str("object_properties_")
    };
    
    public HTMLElementPropertiesAction()
    {
        super(PROPS[ELEM_PROPS]); 
        addShouldBeEnabledDelegate(new ShouldBeEnabledDelegate()
        {
            public boolean shouldBeEnabled(Action a)
            {                          
                return getEditMode() != SOURCE && elementAtCaretPosition(getCurrentEditor()) != null;
            }
        });
    }

    //public void actionPerformed(ActionEvent e)
    protected void wysiwygEditPerformed(ActionEvent e, JEditorPane ed)
    {
        Element elem = elementAtCaretPosition(ed);
        int type = getElementType(elem);
        int caret = ed.getCaretPosition();
        
        if(type == LINK_PROPS)
        {
            editLinkProps(elem);
        }
        else if(type == IMG_PROPS)
        {
            editImageProps(elem);
        }
        else if(type == TABLE_PROPS)
        {
            editTableProps(elem);
        }
        else if(type == LIST_PROPS)
        {
            editListProps(elem);
        }
        else if(type == ELEM_PROPS)
        {
            editStyleProps(elem.getParentElement());
        }
                
        try
        {
            ed.setCaretPosition(caret);
        }
        catch(Exception ex){}
    }
    
    private Map getLinkAttributes(Element elem)
    {
        String link = HTMLUtils.getElementHTML(elem, true).trim();
        Map attribs = new HashMap();
        if(link.startsWith("<a"))
        {
            link = link.substring(0, link.indexOf('>'));
            link = link.substring(link.indexOf(' '), link.length()).trim();                    
            
            attribs = HTMLUtils.tagAttribsToMap(link);            
        }
        
        return attribs;
    }
    
    private void editImageProps(Element elem)
    {
        ImageDialog d = createImageDialog();        
        
        if(d != null)
        {           
            Map imgAttribs = getAttribs(elem);            
            d.setImageAttributes(imgAttribs);            
            d.setLocationRelativeTo(d.getParent());            
            d.setVisible(true);
            if(!d.hasUserCancelled())
            {
                replace(elem, d.getHTML());
            }
        }        
    }
    
    private void editLinkProps(Element elem)
    {        
        HyperlinkDialog d = createLinkDialog();
        if(d != null)
        {
            d.setAttributes(getLinkAttributes(elem));
            d.setLocationRelativeTo(d.getParent());
                      
            try
            {            
                //get the link text...
                String text = elem.getDocument().getText(
                    elem.getStartOffset(), 
                    elem.getEndOffset() - elem.getStartOffset());
                d.setLinkText(text);
            }
            catch(BadLocationException ex){}
            d.setVisible(true);
            if(!d.hasUserCancelled())
            {
                replace(elem, d.getHTML());
            }
        }
    }
    
    private void editTableProps(Element paraElem)
    {
        HTMLDocument doc = null;
        try
        {
            doc = (HTMLDocument)paraElem.getDocument();
        }
        catch(Exception ex)
        {
            ex.printStackTrace();
            return;
        }
        
        Element tdElem = HTMLUtils.getParent(paraElem, HTML.Tag.TD);       
        Element trElem = HTMLUtils.getParent(paraElem, HTML.Tag.TR);
        Element tableElem = HTMLUtils.getParent(paraElem, HTML.Tag.TABLE);
        
        TablePropertiesDialog dlg = createTablePropertiesDialog();
        
        if(dlg == null || tdElem == null || trElem == null || tableElem == null)
            return; //no dialog or malformed table! Just return...
                
        dlg.setCellAttributes(getAttribs(tdElem));        
        dlg.setRowAttributes(getAttribs(trElem));        
        dlg.setTableAttributes(getAttribs(tableElem));        
        dlg.setLocationRelativeTo(dlg.getParent());        
        dlg.setVisible(true);
        
        if(!dlg.hasUserCancelled())
        {           
            CompoundUndoManager.beginCompoundEdit(doc);
            try
            {
                String html = getElementHTML(tdElem, dlg.getCellAttributes());            
                doc.setOuterHTML(tdElem, html);            
                
                html = getElementHTML(trElem, dlg.getRowAttribures());
                doc.setOuterHTML(trElem, html);            
                
                html = getElementHTML(tableElem, dlg.getTableAttributes());            
                doc.setOuterHTML(tableElem, html);
            }
            catch(Exception ex)
            {
                ex.printStackTrace();
            }
            CompoundUndoManager.endCompoundEdit(doc);
        }
    }
    
    private void editListProps(Element elem)
    {
        elem = HTMLUtils.getListParent(elem);
        if(elem == null)
           return;
        int type;
        if(elem.getName().equals("ul"))
            type = ListDialog.UNORDERED;
        else if(elem.getName().equals("ol"))
            type = ListDialog.ORDERED;
        else
            return;        
        
        Map attr = getAttribs(elem);
        ListDialog d = createListDialog();
        if(d == null)
            return;
        
        d.setListType(type);
        d.setListAttributes(attr);
        d.setLocationRelativeTo(d.getParent());
        d.setVisible(true);
        if(!d.hasUserCancelled())
        {
            attr = d.getListAttributes();
            String html = "";
            if(d.getListType() != type)
            {
                HTML.Tag tag = HTML.Tag.UL;
                if(d.getListType() == ListDialog.ORDERED)
                    tag = HTML.Tag.OL;
                String txt = HTMLUtils.getElementHTML(elem, false);
                html = "<" + tag;
                for(Iterator ee = attr.keySet().iterator(); ee.hasNext();)
                {
                    Object o = ee.next();
                    html += " " + o + "=" + attr.get(o);
                }
                html += ">" + txt + "</" + tag + ">";
            }
            else
            {
                html = getElementHTML(elem, attr);                
            }         
            
            replace(elem, html);
        }
        
    }
    
    private void editStyleProps(Element elem)
    {
        if(elem.getName().equals("p-implied"))
            elem = elem.getParentElement();
        Map attr = getAttribs(elem);
        ElementStyleDialog d = createStyleDialog();
        if(d == null)
            return;
        
        d.setLocationRelativeTo(d.getParent());
        d.setStyleAttributes(attr);
        d.setVisible(true);
        
        if(!d.hasUserCancelled())
        {
            System.err.println(elem.getName());
            String html = getElementHTML(elem, d.getStyleAttributes());
            System.err.println(html);
            replace(elem, html);
        }
    }
    
    protected HyperlinkDialog createLinkDialog()
    {
        Component c = getCurrentEditor();
        HyperlinkDialog d = null;
        if(c != null)
        {
            Window w = SwingUtilities.getWindowAncestor(c);            
            if(w != null && w instanceof Frame)
                d = new HyperlinkDialog((Frame)w);
            else if(w != null && w instanceof Dialog)
                d = new HyperlinkDialog((Dialog)w);
        }
        
        return d;
    }
    
    protected ImageDialog createImageDialog()
    {
        Component c = getCurrentEditor();
        ImageDialog d = null;
        if(c != null)
        {
            Window w = SwingUtilities.getWindowAncestor(c);            
            if(w != null && w instanceof Frame)
                d = new ImageDialog((Frame)w);
            else if(w != null && w instanceof Dialog)
                d = new ImageDialog((Dialog)w);
        }
        
        return d;
    }
    
    protected TablePropertiesDialog createTablePropertiesDialog()
    {
        Component c = getCurrentEditor();
        TablePropertiesDialog d = null;
        if(c != null)
        {
            Window w = SwingUtilities.getWindowAncestor(c);            
            if(w != null && w instanceof Frame)
                d = new TablePropertiesDialog((Frame)w);
            else if(w != null && w instanceof Dialog)
                d = new TablePropertiesDialog((Dialog)w);  
        }
        
        return d;
    }
    
    protected ListDialog createListDialog()
    {
        Component c = getCurrentEditor();
        ListDialog d = null;
        if(c != null)
        {
            Window w = SwingUtilities.getWindowAncestor(c);            
            if(w != null && w instanceof Frame)
                d = new ListDialog((Frame)w);
            else if(w != null && w instanceof Dialog)
                d = new ListDialog((Dialog)w);  
        }
        
        return d;
    }
    
    protected ElementStyleDialog createStyleDialog()
    {
        Component c = getCurrentEditor();
        ElementStyleDialog d = null;
        if(c != null)
        {
            Window w = SwingUtilities.getWindowAncestor(c);            
            if(w != null && w instanceof Frame)
                d = new ElementStyleDialog((Frame)w);
            else if(w != null && w instanceof Dialog)
                d = new ElementStyleDialog((Dialog)w);  
        }
        
        return d;
    }
    
    protected void updateWysiwygContextState(JEditorPane ed)
    {
        int t = ELEM_PROPS;
        Element elem = elementAtCaretPosition(ed);
        if(elem != null)
        {           
            t = getElementType(elem);            
        }
                
        putValue(NAME, PROPS[t]);
        //Messages.setMnemonic(PROPS[t], this); TODO this won't set the right mnemonic
    }
    
    protected void sourceEditPerformed(ActionEvent e, JEditorPane editor)
    {
        
    }    
}

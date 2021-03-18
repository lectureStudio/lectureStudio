/*
 * Created on Jun 16, 2005
 *
 */
package net.atlanticbb.tantlinger.ui.text.actions;

import java.awt.event.ActionEvent;
import java.io.StringWriter;

import javax.swing.Action;
import javax.swing.JEditorPane;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import javax.swing.text.Element;
import javax.swing.text.html.HTML;
import javax.swing.text.html.HTMLDocument;

import org.bushe.swing.action.ShouldBeEnabledDelegate;

import net.atlanticbb.tantlinger.ui.text.CompoundUndoManager;
import net.atlanticbb.tantlinger.ui.text.ElementWriter;
import net.atlanticbb.tantlinger.ui.text.HTMLUtils;


/**
 * 
 * Action for adding and removing table elements
 * 
 * @author Bob Tantlinger
 *
 */
public class TableEditAction extends HTMLTextEditAction
{
    private static final long serialVersionUID = 1L;
    public static final int INSERT_CELL = 0;
    public static final int DELETE_CELL = 1;    
    public static final int INSERT_ROW = 2;
    public static final int DELETE_ROW = 3;
    public static final int INSERT_COL = 4;
    public static final int DELETE_COL = 5;
    
    
    private static final String NAMES[] =
    {
        i18n.str("insert_cell"), 
        i18n.str("delete_cell"), 
        i18n.str("insert_row"), 
        i18n.str("delete_row"), 
        i18n.str("insert_column"), 
        i18n.str("delete_column")
    };
    
    
    private int type;
    
    public TableEditAction(int type) throws IllegalArgumentException
    {
        super("");
        if(type < 0 || type >= NAMES.length)
            throw new IllegalArgumentException("Invalid type");
        this.type = type;
        putValue(NAME, NAMES[type]);
        addShouldBeEnabledDelegate(new ShouldBeEnabledDelegate()
        {
            public boolean shouldBeEnabled(Action a)
            {                          
                return (getEditMode() != SOURCE) && isInTD(getCurrentEditor());
            }
        });
    }
    
        
    protected void wysiwygEditPerformed(ActionEvent e, JEditorPane ed)
    {        
        HTMLDocument document = (HTMLDocument)ed.getDocument();
        
        Element curElem = document.getParagraphElement(ed.getCaretPosition());
        Element td = HTMLUtils.getParent(curElem, HTML.Tag.TD);
        Element tr = HTMLUtils.getParent(curElem, HTML.Tag.TR);
        //HTMLDocument document = getDocument();
        if(td == null || tr == null || document == null)
            return;
              
        CompoundUndoManager.beginCompoundEdit(document);
        try
        {            
            if(type == INSERT_CELL)                
                document.insertAfterEnd(td, "<td></td>");            
            else if(type == DELETE_CELL)           
                removeCell(td);           
            else if(type == INSERT_ROW)
               insertRowAfter(tr);
            else if(type == DELETE_ROW)
                removeRow(tr);
            else if(type == INSERT_COL)
                insertColumnAfter(td);
            else if(type == DELETE_COL)
                removeColumn(td);                                
        }
        catch(Exception ex)
        {
            ex.printStackTrace();
        }
        CompoundUndoManager.endCompoundEdit(document);
    }
    
    private void removeCell(Element td) throws Exception
    {
        Element tr = HTMLUtils.getParent(td, HTML.Tag.TR);
        if(tr != null && td.getName().equals("td"))
        {
            if(td.getEndOffset() != tr.getEndOffset())
                remove(td);
            else if(getRowCellCount(tr) <= 1)        
                remove(tr);        
            else
            {
                StringWriter out = new StringWriter();
                ElementWriter w = new ElementWriter(out, tr, tr.getStartOffset(), td.getStartOffset());                    
                w.write();
                           
                HTMLDocument doc = (HTMLDocument)tr.getDocument();
                doc.setOuterHTML(tr, out.toString());              
            }
        }
    }
    
    private void insertRowAfter(Element tr) throws Exception
    {
        Element table = HTMLUtils.getParent(tr, HTML.Tag.TABLE);
        
        if(table != null && tr.getName().equals("tr"))
        {
            HTMLDocument doc = (HTMLDocument)tr.getDocument();
            if(tr.getEndOffset() != table.getEndOffset())
                doc.insertAfterEnd(tr, getRowHTML(tr));
            else
            {
                AttributeSet atr = table.getAttributes();
                String tbl = HTMLUtils.getElementHTML(table, false);
                tbl += getRowHTML(tr);
                
                tbl = HTMLUtils.createTag(HTML.Tag.TABLE, atr, tbl);
                doc.setOuterHTML(table, tbl);
            }
        }
    }
    
    private void removeRow(Element tr) throws Exception
    {
        Element table = HTMLUtils.getParent(tr, HTML.Tag.TABLE);
        if(table != null && tr.getName().equals("tr"))
        {
            if(tr.getEndOffset() != table.getEndOffset())
                remove(tr);
            else if(getTableRowCount(table) <= 1)        
                remove(table);        
            else
            {
                StringWriter out = new StringWriter();
                ElementWriter w = new ElementWriter(out, table, table.getStartOffset(), tr.getStartOffset());                    
                w.write();
                           
                HTMLDocument doc = (HTMLDocument)tr.getDocument();
                doc.setOuterHTML(table, out.toString()); 
            }
        }
    }
    
    private int getTableRowCount(Element table)
    {
        int count = 0;
        for(int i = 0; i < table.getElementCount(); i++)
        {
            Element e = table.getElement(i);
            if(e.getName().equals("tr"))
                count++;
        }
            
        return count;
    }
    
    private int getRowCellCount(Element tr)
    {
        int count = 0;
        for(int i = 0; i < tr.getElementCount(); i++)
        {
            Element e = tr.getElement(i);
            if(e.getName().equals("td"))
                count++;
        }
            
        return count;
    }
    
    private void remove(Element el) throws BadLocationException
    {
        int start = el.getStartOffset();
        int len = el.getEndOffset() - start;        
        Document document = el.getDocument();    
        
        if(el.getEndOffset() > document.getLength())            
            len = document.getLength() - start;
        document.remove(start, len);        
    }
    
    private int getCellIndex(Element tr, Element td)
    {
        int tdIndex = -1;
        for(int i = 0; i < tr.getElementCount(); i++)
        {
            Element e = tr.getElement(i);
            if(e.getStartOffset() == td.getStartOffset())
            {
                tdIndex = i;
                break;
            }
        }
        
        return tdIndex;
    }
    
    private void removeColumn(Element td) throws Exception
    {
        Element tr = HTMLUtils.getParent(td, HTML.Tag.TR);                

        int tdIndex = getCellIndex(tr, td);
        if(tdIndex == -1)           
            return;
        
        Element table = HTMLUtils.getParent(tr, HTML.Tag.TABLE);
        for(int i = 0; i < table.getElementCount(); i++)
        {
            Element row = table.getElement(i);
            if(row.getName().equals("tr"))
            {
                Element e = row.getElement(tdIndex);
                if(e != null && e.getName().equals("td"))
                    removeCell(e);
            }            
        }
    }
    
    private void insertColumnAfter(Element td) throws Exception
    {
        Element tr = HTMLUtils.getParent(td, HTML.Tag.TR);
        HTMLDocument doc = (HTMLDocument)tr.getDocument();        

        int tdIndex = getCellIndex(tr, td);
        if(tdIndex == -1)           
            return;        
        
        Element table = HTMLUtils.getParent(tr, HTML.Tag.TABLE);
        for(int i = 0; i < table.getElementCount(); i++)
        {
            Element row = table.getElement(i);
            if(row.getName().equals("tr"))
            {
                AttributeSet attr = row.getAttributes();
                int cellCount = row.getElementCount();
                
                String rowHTML = "";
                String cell = "<td></td>";
                for(int j = 0; j < cellCount; j++)
                {
                    Element e = row.getElement(j);
                    rowHTML += HTMLUtils.getElementHTML(e, true);
                    if(j == tdIndex)
                        rowHTML += cell;
                }
                
                int tds = row.getElementCount() - 1;
                if(tds < tdIndex)
                {                                  
                    for(; tds <= tdIndex; tds++)
                        rowHTML += cell;
                }
                
                rowHTML = HTMLUtils.createTag(HTML.Tag.TR, attr, rowHTML);
                doc.setOuterHTML(row, rowHTML);
            }            
        }
    }    
    
    private String getRowHTML(Element tr)
    {
        String trTag = "<tr>";
        if(tr.getName().equals("tr"))
        {       
            for(int i = 0; i < tr.getElementCount(); i++)
                if (tr.getElement(i).getName().equals("td"))
                    trTag += "<td></td>";
        }
		trTag += "</tr>";
		return trTag;
    }
    
    private boolean isInTD(JEditorPane tc)
    {        
        Element td = null; 
        if(tc != null)
        {
	        HTMLDocument doc = (HTMLDocument)tc.getDocument();
	        try
	        {
	            Element curElem = doc.getParagraphElement(tc.getCaretPosition());
	            td = HTMLUtils.getParent(curElem, HTML.Tag.TD);            
	        }
	        catch(Exception ex){}
        }
        
        return td != null;        
    }
    
    protected void updateWysiwygContextState(JEditorPane wysEditor)
	{	    
	    boolean isInTd = isInTD(wysEditor);
	    if((isInTd && !isEnabled()) || (isEnabled() && !isInTd))
	    	updateEnabled();
	}


    /* (non-Javadoc)
     * @see net.atlanticbb.tantlinger.ui.text.actions.HTMLTextEditAction#sourceEditPerformed(java.awt.event.ActionEvent, javax.swing.JEditorPane)
     */
    protected void sourceEditPerformed(ActionEvent e, JEditorPane editor)
    {        
        
    }
}

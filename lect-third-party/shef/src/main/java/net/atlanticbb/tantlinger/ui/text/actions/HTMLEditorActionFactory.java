/*
 * Created on Nov 2, 2007
 */
package net.atlanticbb.tantlinger.ui.text.actions;

import net.atlanticbb.tantlinger.ui.text.CompoundUndoManager;

import org.bushe.swing.action.ActionList;


/**
 * @author Bob Tantlinger
 *
 */
public class HTMLEditorActionFactory
{
    public static ActionList createEditActionList()
    {
        ActionList list = new ActionList("edit");
        list.add(CompoundUndoManager.UNDO);
        list.add(CompoundUndoManager.REDO);
        list.add(null);
        list.add(new CutAction());
        list.add(new CopyAction());
        list.add(new PasteAction());
        //list.add(new PasteFormattedAction());
        list.add(null);
        list.add(new SelectAllAction()); 
        //list.add(new IndentAction(IndentAction.INDENT));
        //list.add(new IndentAction(IndentAction.OUTDENT));
        return list;        
    }
    
    public static ActionList createInlineActionList()
    {
        ActionList list = new ActionList("style");
        list.add(new HTMLInlineAction(HTMLInlineAction.BOLD));
        list.add(new HTMLInlineAction(HTMLInlineAction.ITALIC));
        list.add(new HTMLInlineAction(HTMLInlineAction.UNDERLINE));
        list.add(null);
        list.add(new HTMLInlineAction(HTMLInlineAction.CITE));
        list.add(new HTMLInlineAction(HTMLInlineAction.CODE));
        list.add(new HTMLInlineAction(HTMLInlineAction.SPAN));
        list.add(new HTMLInlineAction(HTMLInlineAction.STRONG));
        list.add(new HTMLInlineAction(HTMLInlineAction.SUB));
        list.add(new HTMLInlineAction(HTMLInlineAction.SUP));
        list.add(new HTMLInlineAction(HTMLInlineAction.STRIKE));
        
        return list;
    }
    
    public static ActionList createAlignActionList()
    {
        ActionList list = new ActionList("align");
        String[] t = HTMLAlignAction.ALIGNMENTS;
        for(int i = 0; i < t.length; i++)
        {
            list.add(new HTMLAlignAction(i));
        }
        
        return list;
    }
    
    public static ActionList createFontSizeActionList()
    {
        ActionList list = new ActionList("font-size");
        int[] t = HTMLFontSizeAction.FONT_SIZES;
        for(int i = 0; i < t.length; i++)
        {
            list.add(new HTMLFontSizeAction(i));
        }
        
        return list;
    }
    
    public static ActionList createBlockElementActionList()
    {
        ActionList list = new ActionList("paragraph");
        list.add(new HTMLBlockAction(HTMLBlockAction.DIV));
        list.add(new HTMLBlockAction(HTMLBlockAction.P));
        list.add(null);
        list.add(new HTMLBlockAction(HTMLBlockAction.BLOCKQUOTE));
        list.add(new HTMLBlockAction(HTMLBlockAction.PRE));
                
        return list;
    }
    
    public static ActionList createListElementActionList()
    {
        ActionList list = new ActionList("list");
        list.add(new HTMLBlockAction(HTMLBlockAction.UL));
        list.add(new HTMLBlockAction(HTMLBlockAction.OL));
        
        return list;
    }
    
    public static ActionList createInsertActionList()
    {
        ActionList list = new ActionList("insertActions");
        list.add(new HTMLLinkAction());
        list.add(new HTMLImageAction());
        list.add(new HTMLTableAction());
        list.add(null);
        list.add(new HTMLLineBreakAction());
        list.add(new HTMLHorizontalRuleAction());
        list.add(new SpecialCharAction());
        return list;
    }
    
    public static ActionList createInsertTableElementActionList()
    {
        ActionList list = new ActionList("Insert into table");
        list.add(new TableEditAction(TableEditAction.INSERT_CELL));
        list.add(new TableEditAction(TableEditAction.INSERT_ROW));
        list.add(new TableEditAction(TableEditAction.INSERT_COL));
        return list;
    }
    
    public static ActionList createDeleteTableElementActionList()
    {
        ActionList list = new ActionList("Insert into table");
        list.add(new TableEditAction(TableEditAction.DELETE_CELL));
        list.add(new TableEditAction(TableEditAction.DELETE_ROW));
        list.add(new TableEditAction(TableEditAction.DELETE_COL));
        return list;
    }
    
}

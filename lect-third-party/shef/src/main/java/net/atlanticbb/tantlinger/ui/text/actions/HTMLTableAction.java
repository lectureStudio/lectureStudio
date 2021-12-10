package net.atlanticbb.tantlinger.ui.text.actions;

import java.awt.Component;
import java.awt.Dialog;
import java.awt.Frame;
import java.awt.Window;
import java.awt.event.ActionEvent;

import javax.swing.Action;
import javax.swing.JEditorPane;
import javax.swing.SwingUtilities;
import javax.swing.text.Element;
import javax.swing.text.JTextComponent;
import javax.swing.text.html.HTML;
import javax.swing.text.html.HTMLDocument;

import net.atlanticbb.tantlinger.ui.UIUtils;
import net.atlanticbb.tantlinger.ui.text.CompoundUndoManager;
import net.atlanticbb.tantlinger.ui.text.HTMLUtils;
import net.atlanticbb.tantlinger.ui.text.dialogs.NewTableDialog;
import net.atlanticbb.tantlinger.ui.text.dialogs.TablePropertiesDialog;

/**
 * Action which shows a dialog to insert an HTML table
 *
 * @author Bob Tantlinger
 *
 */
public class HTMLTableAction extends HTMLTextEditAction
{

	private static final long serialVersionUID = 9203798526333927707L;

	public HTMLTableAction()
    {
        super(i18n.str("table_"));

        putValue(SMALL_ICON, UIUtils.getIcon("table.svg"));
        putValue(Action.SHORT_DESCRIPTION, getValue(Action.NAME));
    }

    protected void sourceEditPerformed(ActionEvent e, JEditorPane editor)
    {
        NewTableDialog dlg = createNewTableDialog(editor);
        if(dlg == null)
            return;
        dlg.setLocationRelativeTo(dlg.getParent());
        dlg.setVisible(true);
        if(dlg.hasUserCancelled())
            return;

        editor.replaceSelection(dlg.getHTML());
    }

    protected void wysiwygEditPerformed(ActionEvent e, JEditorPane editor)
    {
    	Element element = elementAtCaretPosition(editor);
    	Element tdElem = HTMLUtils.getParent(element, HTML.Tag.TD);
        Element trElem = HTMLUtils.getParent(element, HTML.Tag.TR);
        Element tableElem = HTMLUtils.getParent(element, HTML.Tag.TABLE);

        int caret = editor.getCaretPosition();

        if (tdElem == null || trElem == null || tableElem == null) {
        	// No table selected.

        	NewTableDialog dlg = createNewTableDialog(editor);
            if(dlg == null)
                return;
            dlg.setLocationRelativeTo(dlg.getParent());
            dlg.setVisible(true);
            if(dlg.hasUserCancelled())
                return;

            HTMLDocument document = (HTMLDocument)editor.getDocument();
            String html = dlg.getHTML();

            // Workaround: If the table is created at the beginning of the document
            // the cursor cannot be moved in front of the table.
            if (document.getLength() <= 1)
            	html = "<br/>" + html;

            if (editor.getCaretPosition() == editor.getDocument().getLength())
            	html = html + "&nbsp;";

            Element elem = document.getParagraphElement(editor.getCaretPosition());
			CompoundUndoManager.beginCompoundEdit(document);

			try {
				if (HTMLUtils.isElementEmpty(elem))
					document.setOuterHTML(elem, html);
				else if (elem.getName().equals("p-implied"))
					document.insertAfterEnd(elem, html);
				else
					HTMLUtils.insertHTML(html, HTML.Tag.TABLE, editor);
			}
			catch (Exception ex) {
				ex.printStackTrace();
			}
            CompoundUndoManager.endCompoundEdit(document);
        }
        else {
            editTableProps(element);
        }

        try
        {
        	editor.setCaretPosition(caret);
        }
        catch(Exception ex) {}
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

        if(dlg == null || tdElem == null || trElem == null || tableElem == null) {
        	//no dialog or malformed table! Just return...
            return;
        }

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

    private TablePropertiesDialog createTablePropertiesDialog()
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

    /**
     * Creates the dialog
     * @param ed
     * @return the dialog
     */
    private NewTableDialog createNewTableDialog(JTextComponent ed)
    {
        Window w = SwingUtilities.getWindowAncestor(ed);
        NewTableDialog d = null;
        if(w != null && w instanceof Frame)
            d = new NewTableDialog((Frame)w);
        else if(w != null && w instanceof Dialog)
            d = new NewTableDialog((Dialog)w);

        return d;
    }
}

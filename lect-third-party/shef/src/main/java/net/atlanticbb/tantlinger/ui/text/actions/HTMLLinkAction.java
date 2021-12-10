/*
 * Created on Feb 26, 2005
 *
 */
package net.atlanticbb.tantlinger.ui.text.actions;

import java.awt.Dialog;
import java.awt.Frame;
import java.awt.Window;
import java.awt.event.ActionEvent;

import javax.swing.Action;
import javax.swing.JEditorPane;
import javax.swing.SwingUtilities;
import javax.swing.text.JTextComponent;
import javax.swing.text.html.HTML;

import net.atlanticbb.tantlinger.ui.UIUtils;
import net.atlanticbb.tantlinger.ui.text.HTMLUtils;
import net.atlanticbb.tantlinger.ui.text.dialogs.HyperlinkDialog;


/**
 * Action which displays a dialog to insert a hyperlink
 *
 * @author Bob Tantlinger
 *
 */
public class HTMLLinkAction extends HTMLTextEditAction
{
    /**
     *
     */
    private static final long serialVersionUID = 1L;

    public HTMLLinkAction()
    {
        super(i18n.str("hyperlink"));

        putValue(SMALL_ICON, UIUtils.getIcon("link.svg"));
        putValue(Action.SHORT_DESCRIPTION, getValue(Action.NAME));
    }

    protected void sourceEditPerformed(ActionEvent e, JEditorPane editor)
    {
        HyperlinkDialog dlg = createDialog(editor);
        if(dlg == null)
            return;

        dlg.setLocationRelativeTo(dlg.getParent());
		//dlg.setName(editor.getSelectedText());
		dlg.setLinkText(editor.getSelectedText());
		dlg.setVisible(true);
		if(dlg.hasUserCancelled())
			return;

		editor.requestFocusInWindow();
		editor.replaceSelection(dlg.getHTML());
    }

    protected void wysiwygEditPerformed(ActionEvent e, JEditorPane editor)
    {
        HyperlinkDialog dlg = createDialog(editor);
        if(dlg == null)
            return;

        if(editor.getSelectedText() != null)
            dlg.setLinkText(editor.getSelectedText());
        dlg.setLocationRelativeTo(dlg.getParent());
		dlg.setVisible(true);
		if(dlg.hasUserCancelled())
			return;

		String tagText = dlg.getHTML();
		//if(editor.getCaretPosition() == document.getLength())
		if(editor.getSelectedText() == null)
			tagText += "&nbsp;";

		editor.replaceSelection("");
		HTMLUtils.insertHTML(tagText, HTML.Tag.A, editor);
        dlg = null;
    }

    protected HyperlinkDialog createDialog(JTextComponent ed)
    {
        Window w = SwingUtilities.getWindowAncestor(ed);
        HyperlinkDialog d = null;
        if(w != null && w instanceof Frame)
            d = new HyperlinkDialog((Frame)w);
        else if(w != null && w instanceof Dialog)
            d = new HyperlinkDialog((Dialog)w);


        return d;
    }
}

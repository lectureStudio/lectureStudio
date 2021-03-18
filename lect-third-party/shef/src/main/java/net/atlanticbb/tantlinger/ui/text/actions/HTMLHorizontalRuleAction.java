/*
 * Created on Mar 3, 2005
 *
 */
package net.atlanticbb.tantlinger.ui.text.actions;

import java.awt.event.ActionEvent;

import javax.swing.Action;
import javax.swing.JEditorPane;
import javax.swing.text.Element;
import javax.swing.text.html.HTML;
import javax.swing.text.html.HTMLDocument;
import javax.swing.text.html.HTMLEditorKit;

import net.atlanticbb.tantlinger.ui.UIUtils;



/**
 * Action which inserts a horizontal rule
 * 
 * @author Bob Tantlinger
 *
 */
public class HTMLHorizontalRuleAction extends HTMLTextEditAction
{
    /**
     * 
     */
    private static final long serialVersionUID = 1L;

    public HTMLHorizontalRuleAction()
    {
        super(i18n.str("horizontal_rule"));
        putValue(SMALL_ICON, UIUtils.getIcon(UIUtils.X16, "hrule.png")); 
        putValue(Action.SHORT_DESCRIPTION, getValue(Action.NAME));
    }

    protected void sourceEditPerformed(ActionEvent e, JEditorPane editor)
    {
        editor.replaceSelection("<hr>");
    }
    
    protected void wysiwygEditPerformed(ActionEvent e, JEditorPane editor)
    {
        HTMLDocument document = (HTMLDocument)editor.getDocument();
        int caret = editor.getCaretPosition();
        Element elem = document.getParagraphElement(caret);

        HTML.Tag tag = HTML.getTag(elem.getName());
        if(elem.getName().equals("p-implied"))
            tag = HTML.Tag.IMPLIED;

        HTMLEditorKit.InsertHTMLTextAction a =
            new HTMLEditorKit.InsertHTMLTextAction("", "<hr>", tag, HTML.Tag.HR);
        a.actionPerformed(e);
    }
}

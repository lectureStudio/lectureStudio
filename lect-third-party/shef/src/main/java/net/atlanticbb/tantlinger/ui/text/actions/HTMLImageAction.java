/*
 * Created on Jan 13, 2006
 *
 */
package net.atlanticbb.tantlinger.ui.text.actions;

import java.awt.Component;
import java.awt.Dialog;
import java.awt.Frame;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.util.Map;

import javax.swing.Action;
import javax.swing.JEditorPane;
import javax.swing.SwingUtilities;
import javax.swing.text.Element;
import javax.swing.text.JTextComponent;
import javax.swing.text.html.HTML;

import net.atlanticbb.tantlinger.ui.UIUtils;
import net.atlanticbb.tantlinger.ui.text.HTMLUtils;
import net.atlanticbb.tantlinger.ui.text.dialogs.ImageDialog;


/**
 * Action which desplays a dialog to insert an image
 *
 * @author Bob Tantlinger
 *
 */
public class HTMLImageAction extends HTMLTextEditAction
{
	private static final long serialVersionUID = 803374409532637788L;


	public HTMLImageAction()
    {
        super(i18n.str("image"));
        putValue(SMALL_ICON, UIUtils.getIcon("image.svg"));
        putValue(Action.SHORT_DESCRIPTION, getValue(Action.NAME));
    }

    protected void sourceEditPerformed(ActionEvent e, JEditorPane editor)
    {
        ImageDialog d = createDialog(editor);
        //d.setSize(300, 300);
        d.setLocationRelativeTo(d.getParent());
        d.setVisible(true);
        if(d.hasUserCancelled())
            return;

        editor.requestFocusInWindow();
        editor.replaceSelection(d.getHTML());
    }

    protected void wysiwygEditPerformed(ActionEvent e, JEditorPane editor)
    {
		Element element = elementAtCaretPosition(editor);

		int type = getElementType(element);
		if (type == IMG_PROPS) {
			editImageProps(element);

			try {
				int caret = editor.getCaretPosition();
				editor.setCaretPosition(caret);
			}
			catch (Exception ex) {
			}
		}
		else {
	        ImageDialog d = createDialog(editor);
	        d.setLocationRelativeTo(d.getParent());
	        d.setVisible(true);
	        if(d.hasUserCancelled())
	            return;

	        String tagText = d.getHTML();
	        if(editor.getCaretPosition() == editor.getDocument().getLength())
	            tagText += "&nbsp;";

	        editor.replaceSelection("");
	        HTML.Tag tag = HTML.Tag.IMG;

	        if(tagText.startsWith("<a"))
	            tag = HTML.Tag.A;

	        HTMLUtils.insertHTML(tagText, tag, editor);
		}
    }

    private void editImageProps(Element elem)
    {
        ImageDialog d = createImageDialog();

        if(d != null)
        {
            Map<?, ?> imgAttribs = getAttribs(elem);
            d.setImageAttributes(imgAttribs);
            d.setLocationRelativeTo(d.getParent());
            d.setVisible(true);

            if(!d.hasUserCancelled())
            {
                replace(elem, d.getHTML());
            }
        }
    }

    private ImageDialog createDialog(JTextComponent ed)
    {
        Window w = SwingUtilities.getWindowAncestor(ed);
        ImageDialog d = null;
        if(w != null && w instanceof Frame)
            d = new ImageDialog((Frame)w);
        else if(w != null && w instanceof Dialog)
            d = new ImageDialog((Dialog)w);


        return d;
    }

    private ImageDialog createImageDialog()
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

}

package net.atlanticbb.tantlinger.ui.text.actions;

import java.awt.event.ActionEvent;

import javax.swing.JEditorPane;
import javax.swing.text.AttributeSet;
import javax.swing.text.Element;
import javax.swing.text.MutableAttributeSet;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.html.HTML;
import javax.swing.text.html.HTMLDocument;

import org.bushe.swing.action.ActionManager;

import net.atlanticbb.tantlinger.ui.UIUtils;
import net.atlanticbb.tantlinger.ui.text.CompoundUndoManager;
import net.atlanticbb.tantlinger.ui.text.HTMLUtils;

/**
 * Action which aligns HTML elements
 */
public class HTMLHideAction extends HTMLTextEditAction
{

	private static final long serialVersionUID = 1L;

	public static final int VISIBLE = 0;
	public static final int HIDE = 1;
	public static final int HIDDEN = 2;

    public static final String ALIGNMENT_NAMES[] =
    {
        i18n.str("hide"),
    };

	public static final String OPS[] =
    {
		"",
        "hide",
        "hidden"
    };

    private static final String IMGS[] =
    {
        "template.png"
    };

    /**
     * Creates a new HTMLHideAction
     */
    public HTMLHideAction() throws IllegalArgumentException
    {
        super("");

        putValue(NAME, (ALIGNMENT_NAMES[0]));
        putValue(SMALL_ICON, UIUtils.getIcon(IMGS[0]));
        putValue(ActionManager.BUTTON_TYPE, ActionManager.BUTTON_TYPE_VALUE_RADIO);
    }

    protected void updateWysiwygContextState(JEditorPane ed)
    {
    	setSelected(shouldBeSelected(ed));
    }

    private boolean shouldBeSelected(JEditorPane ed)
    {
        HTMLDocument document = (HTMLDocument)ed.getDocument();
        Element elem = document.getParagraphElement(ed.getCaretPosition());
        if(HTMLUtils.isImplied(elem))
            elem = elem.getParentElement();

        AttributeSet at = elem.getAttributes();
        return at.containsAttribute(HTML.Attribute.FACE, OPS[HIDE]);
    }

    protected void updateSourceContextState(JEditorPane ed)
    {
        setSelected(false);
    }

    protected void wysiwygEditPerformed(ActionEvent e, JEditorPane editor)
    {
        HTMLDocument doc = (HTMLDocument)editor.getDocument();
        Element curE = doc.getParagraphElement(editor.getSelectionStart());
        Element endE = doc.getParagraphElement(editor.getSelectionEnd());

        CompoundUndoManager.beginCompoundEdit(doc);
        while(true)
        {
            changeElement(curE, !shouldBeSelected(editor));
            if(curE.getEndOffset() >= endE.getEndOffset() || curE.getEndOffset() >= doc.getLength())
                break;
            curE = doc.getParagraphElement(curE.getEndOffset() + 1);
        }
        CompoundUndoManager.endCompoundEdit(doc);
    }

    private void changeElement(Element elem, boolean hide)
    {
        HTMLDocument doc = (HTMLDocument)elem.getDocument();
        String op = hide ? OPS[HIDE] : OPS[VISIBLE];
        String cls = hide ? OPS[HIDDEN] : OPS[VISIBLE];

        if(HTMLUtils.isImplied(elem))
        {
            HTML.Tag tag = HTML.getTag(elem.getParentElement().getName());
            //pre tag doesn't support an align attribute
            //http://www.w3.org/TR/REC-html32#pre
            if(tag != null && (!tag.equals(HTML.Tag.BODY)) &&
                (!tag.isPreformatted() && !tag.equals(HTML.Tag.DD)))
            {
                SimpleAttributeSet as = new SimpleAttributeSet(elem.getAttributes());
//                as.removeAttribute(HTML.Attribute.FACE);
//                as.addAttribute(HTML.Attribute.FACE, op);

                as.removeAttribute(HTML.Attribute.CLASS);
                as.addAttribute(HTML.Attribute.CLASS, cls);

                Element parent = elem.getParentElement();
                String html = HTMLUtils.getElementHTML(elem, false);
                html = HTMLUtils.createTag(tag, as, html);
                String snipet = "";
                for(int i = 0; i < parent.getElementCount(); i++)
                {
                    Element el = parent.getElement(i);
                    if(el == elem)
                        snipet += html;
                    else
                        snipet += HTMLUtils.getElementHTML(el, true);
                }

                try
                {
                    doc.setOuterHTML(parent, snipet);
                }
                catch(Exception ex)
                {
                    ex.printStackTrace();
                }
            }
        }
        else
        {
            //Set the HTML attribute on the paragraph...
            MutableAttributeSet set = new SimpleAttributeSet(elem.getAttributes());
//            set.removeAttribute(HTML.Attribute.FACE);
//            set.addAttribute(HTML.Attribute.FACE, op);

            set.removeAttribute(HTML.Attribute.CLASS);
            set.addAttribute(HTML.Attribute.CLASS, cls);

             //Set the paragraph attributes...
            int start = elem.getStartOffset();
            int length = elem.getEndOffset() - elem.getStartOffset();
            doc.setParagraphAttributes(start, length - 1, set, true);
        }
    }

    protected void sourceEditPerformed(ActionEvent e, JEditorPane editor)
    {

    }
}

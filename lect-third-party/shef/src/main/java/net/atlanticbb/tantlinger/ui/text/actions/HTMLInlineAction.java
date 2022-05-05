/*
 * Created on Feb 25, 2005
 *
 */
package net.atlanticbb.tantlinger.ui.text.actions;

import java.awt.event.ActionEvent;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;

import javax.swing.Action;
import javax.swing.Icon;
import javax.swing.JEditorPane;
import javax.swing.KeyStroke;
import javax.swing.text.AttributeSet;
import javax.swing.text.MutableAttributeSet;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyleConstants;
import javax.swing.text.html.CSS;
import javax.swing.text.html.HTML;

import net.atlanticbb.tantlinger.ui.UIUtils;
import net.atlanticbb.tantlinger.ui.text.CompoundUndoManager;
import net.atlanticbb.tantlinger.ui.text.HTMLUtils;

import org.bushe.swing.action.ActionManager;



/**
 * Action which toggles inline HTML elements
 *
 * @author Bob Tantlinger
 *
 */
public class HTMLInlineAction extends HTMLTextEditAction
{
    /**
     *
     */
    private static final long serialVersionUID = 1L;

    public static final int SPAN = 0;
    public static final int STRONG = 1;
    public static final int CODE = 2;
    public static final int CITE = 3;
    public static final int SUP = 4;
    public static final int SUB = 5;
    public static final int BOLD = 6;
    public static final int ITALIC = 7;
    public static final int UNDERLINE = 8;
    public static final int STRIKE = 9;

    public static final String[] INLINE_TYPES =
    {
        i18n.str("emphasis"),
        i18n.str("strong"),
        i18n.str("code"),
        i18n.str("cite"),
        i18n.str("superscript"),
        i18n.str("subscript"),
        i18n.str("bold"),
        i18n.str("italic"),
        i18n.str("underline"),
        i18n.str("strikethrough")
    };

    private int type;

    /**
     * Creates a new HTMLInlineAction
     *
     * @param itype an inline element type (BOLD, ITALIC, STRIKE, etc)
     * @throws IllegalArgumentException
     */
    public HTMLInlineAction(int itype) throws IllegalArgumentException
    {
        super("");
        type = itype;
        if(type < 0 || type >= INLINE_TYPES.length)
            throw new IllegalArgumentException("Illegal Argument");
        putValue(NAME, (INLINE_TYPES[type]));

        Icon ico = null;
        KeyStroke ks = null;
        if(type == BOLD)
        {
            ico = UIUtils.getIcon("bold.svg");
            ks = KeyStroke.getKeyStroke(KeyEvent.VK_B, InputEvent.CTRL_DOWN_MASK);
        }
        else if(type == ITALIC)
        {
            ico = UIUtils.getIcon("italic.svg");
            ks = KeyStroke.getKeyStroke(KeyEvent.VK_I, InputEvent.CTRL_DOWN_MASK);
        }
        else if(type == UNDERLINE)
        {
            ico = UIUtils.getIcon("underline.svg");
            ks = KeyStroke.getKeyStroke(KeyEvent.VK_U, InputEvent.CTRL_DOWN_MASK);
        }

        putValue(SMALL_ICON, ico);
        putValue(ACCELERATOR_KEY, ks);
        putValue(ActionManager.BUTTON_TYPE, ActionManager.BUTTON_TYPE_VALUE_CHECKBOX);
        putValue(Action.SHORT_DESCRIPTION, getValue(Action.NAME));
    }

    protected void updateWysiwygContextState(JEditorPane ed)
    {
        setSelected(isDefined(HTMLUtils.getCharacterAttributes(ed)));
    }

    protected void sourceEditPerformed(ActionEvent e, JEditorPane editor)
    {
        HTML.Tag tag = getTag();

        String prefix = "<" + tag.toString() + ">";
        String postfix = "</" + tag.toString() + ">";
        String sel = editor.getSelectedText();

        if(sel == null)
        {
            editor.replaceSelection(prefix + postfix);

            int pos = editor.getCaretPosition() - postfix.length();
            if(pos >= 0)
            	editor.setCaretPosition(pos);
        }
        else
        {
            sel = prefix + sel + postfix;
            editor.replaceSelection(sel);
        }
    }

    public HTML.Tag getTag()
    {
        return getTagForType(type);
    }

    private HTML.Tag getTagForType(int type)
    {
        HTML.Tag tag = null;

        switch(type)
        {
            case SPAN:
                tag = HTML.Tag.SPAN;
                break;
            case STRONG:
                tag = HTML.Tag.STRONG;
                break;
            case CODE:
                tag = HTML.Tag.PRE;
                break;
            case SUP:
                tag = HTML.Tag.SUP;
                break;
            case SUB:
                tag = HTML.Tag.SUB;
                break;
            case CITE:
                tag = HTML.Tag.CITE;
                break;
            case BOLD:
                tag = HTML.Tag.B;
                break;
            case ITALIC:
                tag = HTML.Tag.I;
                break;
            case UNDERLINE:
                tag = HTML.Tag.U;
                break;
            case STRIKE:
                tag = HTML.Tag.STRIKE;
                break;
        }
        return tag;
    }

    protected void wysiwygEditPerformed(ActionEvent e, JEditorPane editor)
    {
        CompoundUndoManager.beginCompoundEdit(editor.getDocument());
        toggleStyle(editor);
        CompoundUndoManager.endCompoundEdit(editor.getDocument());

        //HTMLUtils.printAttribs(HTMLUtils.getCharacterAttributes(editor));
    }


    private boolean isDefined(AttributeSet attr)
    {
        boolean hasSC = false;
        if(type == SUP)
            hasSC = StyleConstants.isSuperscript(attr);
        else if(type == SUB)
            hasSC = StyleConstants.isSubscript(attr);
        else if(type == BOLD)
            hasSC = StyleConstants.isBold(attr);
        else if(type == ITALIC)
            hasSC = StyleConstants.isItalic(attr);
        else if(type == UNDERLINE)
            hasSC = StyleConstants.isUnderline(attr);
        else if(type == STRIKE)
            hasSC = StyleConstants.isStrikeThrough(attr);

        return hasSC || (attr.getAttribute(getTag()) != null);
    }

    private void toggleStyle(JEditorPane editor)
    {
        MutableAttributeSet attr = new SimpleAttributeSet();
        attr.addAttributes(HTMLUtils.getCharacterAttributes(editor));
        boolean enable = !isDefined(attr);
        HTML.Tag tag = getTag();

        if(enable)
        {
            //System.err.println("adding style");
        	SimpleAttributeSet set = new SimpleAttributeSet();
        	//set.addAttribute(HTML.Attribute.CLASS, "hidden");

            attr = new SimpleAttributeSet();
            attr.addAttribute(tag, set);

            //doesn't replace any attribs, just adds the new one
            HTMLUtils.setCharacterAttributes(editor, attr);

            HTMLUtils.removeCharacterAttribute(editor, CSS.Attribute.FONT_STYLE, "italic");
        }
        else
        {
            //System.err.println("clearing style");
            //Kind of a ham-fisted way to do this, but sometimes there are
            //CSS attributes, someties there are HTML.Tag attributes, and sometimes
            //there are both. So, we have to remove 'em all to make sure this type
            //gets completely disabled

            //remove the CSS style
            //STRONG, EM, CITE, CODE have no CSS analogs
            if(type == BOLD)
                HTMLUtils.removeCharacterAttribute(editor, CSS.Attribute.FONT_WEIGHT, "bold");
            else if(type == ITALIC)
                HTMLUtils.removeCharacterAttribute(editor, CSS.Attribute.FONT_STYLE, "italic");
            else if(type == UNDERLINE)
                HTMLUtils.removeCharacterAttribute(editor, CSS.Attribute.TEXT_DECORATION, "underline");
            else if(type == STRIKE)
                HTMLUtils.removeCharacterAttribute(editor, CSS.Attribute.TEXT_DECORATION, "line-through");
            else if(type == SUP)
                HTMLUtils.removeCharacterAttribute(editor, CSS.Attribute.VERTICAL_ALIGN, "sup");
            else if(type == SUB)
                HTMLUtils.removeCharacterAttribute(editor, CSS.Attribute.VERTICAL_ALIGN, "sub");

            HTMLUtils.removeCharacterAttribute(editor, tag); //make certain the tag is also removed
        }

        setSelected(enable);
    }

    protected void updateSourceContextState(JEditorPane ed)
    {
        setSelected(false);
    }
}

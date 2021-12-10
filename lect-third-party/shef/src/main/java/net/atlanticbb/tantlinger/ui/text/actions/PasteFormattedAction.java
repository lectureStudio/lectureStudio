/*
 * Created on Nov 25, 2007
 */
package net.atlanticbb.tantlinger.ui.text.actions;

import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.event.ActionEvent;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.Reader;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.swing.Action;
import javax.swing.JEditorPane;
import javax.swing.KeyStroke;
import javax.swing.text.html.HTML;

import net.atlanticbb.tantlinger.ui.UIUtils;
import net.atlanticbb.tantlinger.ui.text.CompoundUndoManager;
import net.atlanticbb.tantlinger.ui.text.HTMLUtils;

import org.bushe.swing.action.ActionManager;
import org.bushe.swing.action.ShouldBeEnabledDelegate;


/**
 * @author Bob Tantlinger
 *
 */
public class PasteFormattedAction extends HTMLTextEditAction
{
    private static final long serialVersionUID = 1L;

    public PasteFormattedAction()
    {

        super(i18n.str("paste_formatted"));
        putValue(SMALL_ICON, UIUtils.getIcon("paste.png"));
        putValue(ActionManager.LARGE_ICON, UIUtils.getIcon("paste.png"));
        putValue(ACCELERATOR_KEY,KeyStroke.getKeyStroke("shift ctrl V"));
        addShouldBeEnabledDelegate(new ShouldBeEnabledDelegate()
        {
            public boolean shouldBeEnabled(Action a)
            {
                if(getCurrentEditor() == null)
                    return false;

                Transferable content =
                    Toolkit.getDefaultToolkit().getSystemClipboard().getContents(PasteFormattedAction.this);

                if(content == null)
                    return false;
                DataFlavor flv = DataFlavor.selectBestTextFlavor(content.getTransferDataFlavors());
                return flv != null && flv.getMimeType().startsWith("text/html");
            }
        });

        putValue(Action.SHORT_DESCRIPTION, getValue(Action.NAME));

    }

    protected void updateWysiwygContextState(JEditorPane wysEditor)
    {
        this.updateEnabledState();
    }

    protected void updateSourceContextState(JEditorPane srcEditor)
    {
        this.updateEnabledState();
    }

    /* (non-Javadoc)
     * @see net.atlanticbb.tantlinger.ui.text.actions.HTMLTextEditAction#sourceEditPerformed(java.awt.event.ActionEvent, javax.swing.JEditorPane)
     */
    protected void sourceEditPerformed(ActionEvent e, JEditorPane editor)
    {
        String htmlFragment = null;
        try
        {
            htmlFragment = getHTMLFragment();
        }
        catch(Exception ex)
        {
            ex.printStackTrace();
        }

        if(htmlFragment != null)
        {
            CompoundUndoManager.beginCompoundEdit(editor.getDocument());
            editor.replaceSelection(htmlFragment);
            CompoundUndoManager.endCompoundEdit(editor.getDocument());
        }
    }

    /* (non-Javadoc)
     * @see net.atlanticbb.tantlinger.ui.text.actions.HTMLTextEditAction#wysiwygEditPerformed(java.awt.event.ActionEvent, javax.swing.JEditorPane)
     */
    protected void wysiwygEditPerformed(ActionEvent e, JEditorPane editor)
    {
        String htmlFragment = null;
        try
        {
            htmlFragment = getHTMLFragment();
        }
        catch(Exception ex)
        {
            ex.printStackTrace();
        }

        if(htmlFragment != null)
        {
            CompoundUndoManager.beginCompoundEdit(editor.getDocument());
            HTMLUtils.insertHTML("<div>" + htmlFragment + "</div>", HTML.Tag.DIV, editor);
            CompoundUndoManager.endCompoundEdit(editor.getDocument());
        }
    }



    /**
     * Get the HTML text from the content if any
     *
     * @return returns the html fragment, or null if this content isn't HTML
     * @throws UnsupportedFlavorException
     * @throws IOException
     */
    private String getHTMLFragment() throws IOException, UnsupportedFlavorException
    {
        Clipboard clip = Toolkit.getDefaultToolkit().getSystemClipboard();
        Transferable c = clip.getContents(this);
        if(c == null)
            return null;

        DataFlavor flv = DataFlavor.selectBestTextFlavor(c.getTransferDataFlavors());
        if(!flv.getMimeType().startsWith("text/html"))
            return null;

        String text = read((flv.getReaderForText(c)));

        //when html content is retrieved from the transferable, the copied part
        //is enclosed in a <body> tag, so only get the contents we want...
        int flags = Pattern.CASE_INSENSITIVE | Pattern.UNICODE_CASE;
        Pattern p = Pattern.compile(("<\\s*body\\b([^<>]*)>"), flags);
        Matcher m = p.matcher(text);
        if(m.find())
        {
            text = text.substring(m.end(), text.length());
        }

        p = Pattern.compile("<\\s*/\\s*body\\s*>", flags);
        m = p.matcher(text);
        if(m.find())
        {
            text = text.substring(0, m.start());
        }

        //when html content is retrieved from the transferable, the copied part
        //is surrounded with the comments <!--StartFragment--> and <!--EndFragment--> on windows
        text = text.replaceAll("<\\!\\-\\-StartFragment\\-\\->", "");
        text = text.replaceAll("<\\!\\-\\-EndFragment\\-\\->", "");

        //gets rid of 'class' and 'id' attributes in the tags.
        //It really doesn't make much sense to include these attribs in HTML
        //pasted in from the wild.
        String r = "<([^>]*)(?:class|id)\\s*=\\s*(?:'[^']*'|\"\"[^\"\"]*\"\"|[^\\s>]+)([^>]*)>";
        p = Pattern.compile(r, flags);
        //run it twice for each attrib
        m = p.matcher(text);
        text = m.replaceAll("<$1$2>");
        m = p.matcher(text);
        text = m.replaceAll("<$1$2>");

        return text;
    }

    public String read(Reader input) throws IOException
    {
        BufferedReader reader = new BufferedReader(input);
        StringBuffer sb = new StringBuffer();
        int ch;

        try
        {
            while((ch = reader.read()) != -1)
            {
                //System.err.print((char)ch);
                sb.append((char)ch);
            }
        }
        catch(IOException ex)
        {
            throw ex;
        }
        finally
        {
            try
            {
                reader.close();
            }
            catch(IOException ioe){}
        }

        return sb.toString();
    }

}

package net.atlanticbb.tantlinger.shef;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.ResourceBundle;

import javax.inject.Inject;
import javax.swing.AbstractAction;
import javax.swing.AbstractButton;
import javax.swing.Action;
import javax.swing.DefaultListCellRenderer;
import javax.swing.JComboBox;
import javax.swing.JEditorPane;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JToolBar;
import javax.swing.KeyStroke;
import javax.swing.event.CaretEvent;
import javax.swing.event.CaretListener;
import javax.swing.text.Document;
import javax.swing.text.html.HTMLDocument;
import javax.swing.text.html.HTMLEditorKit;
import javax.swing.text.html.StyleSheet;
import javax.swing.undo.UndoManager;

import net.atlanticbb.tantlinger.i18n.I18n;
import net.atlanticbb.tantlinger.ui.DefaultAction;
import net.atlanticbb.tantlinger.ui.UIUtils;
import net.atlanticbb.tantlinger.ui.text.CompoundUndoManager;
import net.atlanticbb.tantlinger.ui.text.HTMLUtils;
import net.atlanticbb.tantlinger.ui.text.WysiwygHTMLEditorKit;
import net.atlanticbb.tantlinger.ui.text.actions.ClearStylesAction;
import net.atlanticbb.tantlinger.ui.text.actions.FindReplaceAction;
import net.atlanticbb.tantlinger.ui.text.actions.HTMLEditorActionFactory;
import net.atlanticbb.tantlinger.ui.text.actions.HTMLElementPropertiesAction;
import net.atlanticbb.tantlinger.ui.text.actions.HTMLFontAction;
import net.atlanticbb.tantlinger.ui.text.actions.HTMLFontColorAction;
import net.atlanticbb.tantlinger.ui.text.actions.HTMLImageAction;
import net.atlanticbb.tantlinger.ui.text.actions.HTMLInlineAction;
import net.atlanticbb.tantlinger.ui.text.actions.HTMLLinkAction;
import net.atlanticbb.tantlinger.ui.text.actions.HTMLTableAction;
import net.atlanticbb.tantlinger.ui.text.actions.HTMLTextEditAction;

import org.bushe.swing.action.ActionList;
import org.bushe.swing.action.ActionManager;
import org.bushe.swing.action.ActionUIFactory;

public class HTMLEditorPane extends JPanel
{
	private static final long serialVersionUID = 8877305778514251233L;

	private final ResourceBundle resourceBundle;

    private static final String INVALID_TAGS[] = {"html", "head", "body", "title"};

    private JEditorPane wysEditor;
    private JComboBox<?> paragraphCombo;
    private JToolBar formatToolBar;

    private ActionList actionList;

    private FocusListener focusHandler = new FocusHandler();
    private ActionListener paragraphComboHandler = new ParagraphComboHandler();
    private CaretListener caretHandler = new CaretHandler();


    @Inject
    public HTMLEditorPane(ResourceBundle resourceBundle)
    {
    	this.resourceBundle = resourceBundle;

    	I18n.setDictionary(resourceBundle);

    	initUI();
    }

    public void setCaretPosition(int pos)
    {
    	wysEditor.setCaretPosition(pos);
    	wysEditor.requestFocusInWindow();
    }

    private void initUI()
    {
    	wysEditor = createWysiwygEditor();

        createEditorActions();

        setLayout(new BorderLayout());

        add(wysEditor, BorderLayout.CENTER);
    }

    public JToolBar getFormatToolBar()
    {
        return formatToolBar;
    }

    public void requestFocus()
    {
    	wysEditor.requestFocusInWindow();
    	wysEditor.requestFocus();
    }

    public JEditorPane getEditorPane() {
    	return wysEditor;
    }

    public void undo()
    {
    	try
    	{
    		CompoundUndoManager.UNDO.actionPerformed(null);
    	}
    	catch (Exception e) {}
    }

    public void redo()
    {
    	try
    	{
    		CompoundUndoManager.REDO.actionPerformed(null);
    	}
    	catch (Exception e) {}
    }


    private void createEditorActions()
    {
        actionList = new ActionList("editor-actions");

        ActionList paraActions = new ActionList("paraActions");
        ActionList fontSizeActions = new ActionList("fontSizeActions");
        ActionList editActions = HTMLEditorActionFactory.createEditActionList();
        Action objectPropertiesAction = new HTMLElementPropertiesAction();

        // create edit menu
        ActionList lst = new ActionList("edits");
        lst.add(null);//separator
        lst.addAll(editActions);
        lst.add(null);
        lst.add(new FindReplaceAction(false));
        actionList.addAll(lst);


        //create format menu
        lst = HTMLEditorActionFactory.createFontSizeActionList();
        actionList.addAll(lst);
        fontSizeActions.addAll(lst);

        lst = HTMLEditorActionFactory.createInlineActionList();
        actionList.addAll(lst);

        Action act = new HTMLFontColorAction();
        actionList.add(act);

        act = new HTMLFontAction();
        actionList.add(act);

        act = new ClearStylesAction();
        actionList.add(act);

        lst = HTMLEditorActionFactory.createBlockElementActionList();
        actionList.addAll(lst);
        paraActions.addAll(lst);

        lst = HTMLEditorActionFactory.createListElementActionList();
        actionList.addAll(lst);

        lst = HTMLEditorActionFactory.createAlignActionList();
        actionList.addAll(lst);

        lst = HTMLEditorActionFactory.createInsertTableElementActionList();
        actionList.addAll(lst);

        lst = HTMLEditorActionFactory.createDeleteTableElementActionList();
        actionList.addAll(lst);

        actionList.add(objectPropertiesAction);

        createFormatToolBar(paraActions);
    }

    private void createFormatToolBar(ActionList blockActs)
    {
        formatToolBar = new JToolBar();
        formatToolBar.setFloatable(false);
        formatToolBar.setFocusable(false);

        PropertyChangeListener propLst = new PropertyChangeListener()
        {
            public void propertyChange(PropertyChangeEvent evt)
            {
                if(evt.getPropertyName().equals("selected"))
                {
                    if(evt.getNewValue().equals(Boolean.TRUE))
                    {
                        paragraphCombo.removeActionListener(paragraphComboHandler);
                        paragraphCombo.setSelectedItem(evt.getSource());
                        paragraphCombo.addActionListener(paragraphComboHandler);
                    }
                }
            }
        };

        for (Iterator it = blockActs.iterator(); it.hasNext();) {
            Object o = it.next();
            if(o instanceof DefaultAction)
                ((DefaultAction)o).addPropertyChangeListener(propLst);
        }

        paragraphCombo = new JComboBox<>(toArray(blockActs));
        paragraphCombo.setPreferredSize(new Dimension(120, UIUtils.BUTTON_SIZE));
        paragraphCombo.setMinimumSize(new Dimension(120, UIUtils.BUTTON_SIZE));
        paragraphCombo.setMaximumSize(new Dimension(120, UIUtils.BUTTON_SIZE));
        paragraphCombo.addActionListener(paragraphComboHandler);
        paragraphCombo.setRenderer(new ParagraphComboRenderer());

        Action act = CompoundUndoManager.UNDO;
        actionList.add(act);
        addToToolBar(formatToolBar, act);

        act = CompoundUndoManager.REDO;
        actionList.add(act);
        addToToolBar(formatToolBar, act);

        formatToolBar.addSeparator();

        act = new HTMLFontColorAction();
        actionList.add(act);
        addToToolBar(formatToolBar, act);
        formatToolBar.addSeparator();

        act = new HTMLInlineAction(HTMLInlineAction.BOLD);
        act.putValue(ActionManager.BUTTON_TYPE, ActionManager.BUTTON_TYPE_VALUE_TOGGLE);
        actionList.add(act);
        addToToolBar(formatToolBar, act);

        act = new HTMLInlineAction(HTMLInlineAction.ITALIC);
        act.putValue(ActionManager.BUTTON_TYPE, ActionManager.BUTTON_TYPE_VALUE_TOGGLE);
        actionList.add(act);
        addToToolBar(formatToolBar, act);

        act = new HTMLInlineAction(HTMLInlineAction.UNDERLINE);
        act.putValue(ActionManager.BUTTON_TYPE, ActionManager.BUTTON_TYPE_VALUE_TOGGLE);
        actionList.add(act);
        addToToolBar(formatToolBar, act);

        act = new HTMLInlineAction(HTMLInlineAction.STRIKE);
        act.putValue(ActionManager.BUTTON_TYPE, ActionManager.BUTTON_TYPE_VALUE_TOGGLE);
        act.putValue(Action.SMALL_ICON, UIUtils.getIcon("strike.svg"));
        actionList.add(act);
        addToToolBar(formatToolBar, act);

        act = new HTMLInlineAction(HTMLInlineAction.CODE);
        act.putValue(ActionManager.BUTTON_TYPE, ActionManager.BUTTON_TYPE_VALUE_TOGGLE);
        act.putValue(Action.SMALL_ICON, UIUtils.getIcon("code.svg"));
        actionList.add(act);
        addToToolBar(formatToolBar, act);

        formatToolBar.addSeparator();

        act = new HTMLInlineAction(HTMLInlineAction.SUB);
        act.putValue(ActionManager.BUTTON_TYPE, ActionManager.BUTTON_TYPE_VALUE_TOGGLE);
        act.putValue(Action.SMALL_ICON, UIUtils.getIcon("subscript.svg"));
        actionList.add(act);
        addToToolBar(formatToolBar, act);

        act = new HTMLInlineAction(HTMLInlineAction.SUP);
        act.putValue(ActionManager.BUTTON_TYPE, ActionManager.BUTTON_TYPE_VALUE_TOGGLE);
        act.putValue(Action.SMALL_ICON, UIUtils.getIcon("superscript.svg"));
        actionList.add(act);
        addToToolBar(formatToolBar, act);
        formatToolBar.addSeparator();

        List alst = HTMLEditorActionFactory.createListElementActionList();
        for(Iterator it = alst.iterator(); it.hasNext();)
        {
            act = (Action)it.next();
            act.putValue(ActionManager.BUTTON_TYPE, ActionManager.BUTTON_TYPE_VALUE_TOGGLE);
            actionList.add(act);
            addToToolBar(formatToolBar, act);
        }
        formatToolBar.addSeparator();

        alst = HTMLEditorActionFactory.createAlignActionList();
        for(Iterator it = alst.iterator(); it.hasNext();)
        {
            act = (Action)it.next();
            act.putValue(ActionManager.BUTTON_TYPE, ActionManager.BUTTON_TYPE_VALUE_TOGGLE);
            actionList.add(act);
            addToToolBar(formatToolBar, act);
        }
        formatToolBar.addSeparator();

        act = new HTMLLinkAction();
        actionList.add(act);
        addToToolBar(formatToolBar, act);

        act = new HTMLImageAction();
        actionList.add(act);
        addToToolBar(formatToolBar, act);

        act = new HTMLTableAction();
        actionList.add(act);
        addToToolBar(formatToolBar, act);
    }

    private void addToToolBar(JToolBar toolbar, Action act)
    {
        AbstractButton button = ActionUIFactory.getInstance().createButton(act);
        configToolbarButton(button);
        toolbar.add(button);
    }

    /**
     * Converts an action list to an array.
     * Any of the null "separators" or sub ActionLists are ommited from the array.
     * @param lst
     * @return
     */
    private Action[] toArray(ActionList lst)
    {
        List acts = new ArrayList();
        for(Iterator it = lst.iterator(); it.hasNext();)
        {
            Object v = it.next();
            if(v != null && v instanceof Action)
                acts.add(v);
        }

        return (Action[])acts.toArray(new Action[acts.size()]);
    }

    private void configToolbarButton(AbstractButton button)
    {
        button.setText(null);
        button.setMnemonic(0);
        button.setMargin(new Insets(1, 1, 1, 1));
        button.setMaximumSize(new Dimension(UIUtils.BUTTON_SIZE, UIUtils.BUTTON_SIZE));
        button.setMinimumSize(new Dimension(UIUtils.BUTTON_SIZE, UIUtils.BUTTON_SIZE));
        button.setPreferredSize(new Dimension(UIUtils.BUTTON_SIZE, UIUtils.BUTTON_SIZE));
        button.setFocusable(false);
        button.setFocusPainted(false);
        button.setName("toolbar.button");

        Action a = button.getAction();
        if(a != null)
            button.setToolTipText(a.getValue(Action.NAME).toString());
    }

    private JEditorPane createWysiwygEditor()
    {
    	WysiwygHTMLEditorKit kit = new WysiwygHTMLEditorKit();
    	StyleSheet styleSheet = kit.getStyleSheet();
		styleSheet.addRule("code {background: #DAE6E6;}");

		// Add custom span rule.
		StyleSheet styleSheetTmp = new StyleSheet();
		styleSheetTmp.addRule("span {background:#FFCCE6;}");
		styleSheet.addStyleSheet(styleSheetTmp);

		kit.setStyleSheet(styleSheet);

        JEditorPane ed = new JEditorPane();
		ed.setEditorKitForContentType("text/html", kit);
		ed.setContentType("text/html");
		ed.getInputMap().put(KeyStroke.getKeyStroke(' '), "nbsp");
		ed.getActionMap().put("nbsp", new AbstractAction() {
			@Override
			public void actionPerformed(ActionEvent e) {
				ed.replaceSelection("\u00A0");
			}
		});

        insertHTML(ed, "<div></div>", 0);

        ed.addCaretListener(caretHandler);
        ed.addFocusListener(focusHandler);

        HTMLDocument document = (HTMLDocument) ed.getDocument();
        CompoundUndoManager cuh = new CompoundUndoManager(document, new UndoManager());
        document.addUndoableEditListener(cuh);

        return ed;
    }

    //  inserts html into the wysiwyg editor TODO remove JEditorPane parameter
    private void insertHTML(JEditorPane editor, String html, int location)
    {
        try
        {
            HTMLEditorKit kit = (HTMLEditorKit) editor.getEditorKit();
            Document doc = editor.getDocument();
            StringReader reader = new StringReader(HTMLUtils.jEditorPaneizeHTML(html));
            kit.read(reader, doc, location);
        }
        catch (Exception ex)
        {
            ex.printStackTrace();
        }
    }

    public void setText(String text)
    {
    	String topText = removeInvalidTags(text);

        wysEditor.setText("");
        insertHTML(wysEditor, topText, 0);

        CompoundUndoManager.discardAllEdits(wysEditor.getDocument());
    }

    public String getText()
    {
    	String topText = removeInvalidTags(wysEditor.getText());
    	return topText;
    }


    /* *******************************************************************
     *  Methods for dealing with HTML between wysiwyg and source editors
     * ******************************************************************/

    private String removeInvalidTags(String html)
    {
        for(int i = 0; i < INVALID_TAGS.length; i++)
        {
            html = deleteOccurance(html, '<' + INVALID_TAGS[i] + '>');
            html = deleteOccurance(html, "</" + INVALID_TAGS[i] + '>');
        }

        return html.trim();
    }

    private String deleteOccurance(String text, String word)
    {
        StringBuffer sb = new StringBuffer(text);
        int p;
        while((p = sb.toString().toLowerCase().indexOf(word.toLowerCase())) != -1)
        {
            sb.delete(p, p + word.length());
        }
        return sb.toString();
    }

    private void updateState()
    {
        actionList.putContextValueForAll(HTMLTextEditAction.EDITOR, wysEditor);
        actionList.updateEnabledForAll();
    }






    private class CaretHandler implements CaretListener
    {
        public void caretUpdate(CaretEvent e)
        {
            updateState();
        }
    }

    private class FocusHandler implements FocusListener
    {
        public void focusGained(FocusEvent e)
        {
            if(e.getComponent() instanceof JEditorPane)
            {
                JEditorPane ed = (JEditorPane)e.getComponent();
                CompoundUndoManager.updateUndo(ed.getDocument());

                updateState();
            }
        }

        public void focusLost(FocusEvent e)
        {

        }
    }

    private class ParagraphComboHandler implements ActionListener
    {
        public void actionPerformed(ActionEvent e)
        {
            if(e.getSource() == paragraphCombo)
            {
                Action a = (Action)(paragraphCombo.getSelectedItem());
                a.actionPerformed(e);
            }
        }
    }

    private class ParagraphComboRenderer extends DefaultListCellRenderer
    {
        private static final long serialVersionUID = 1L;

        public Component getListCellRendererComponent(JList list, Object value, int index,
            boolean isSelected, boolean cellHasFocus)
        {
            if(value instanceof Action)
            {
                value = ((Action)value).getValue(Action.NAME);
            }

            return super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
        }
    }

}

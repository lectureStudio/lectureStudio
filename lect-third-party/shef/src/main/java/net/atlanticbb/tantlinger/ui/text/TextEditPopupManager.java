/*
 * Created on Aug 18, 2006
 */
package net.atlanticbb.tantlinger.ui.text;

import java.lang.ref.WeakReference;
import java.awt.event.ActionEvent;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.HashMap;
import java.util.List;
import java.util.Vector;
import java.util.Iterator;
import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JPopupMenu;
import javax.swing.event.CaretEvent;
import javax.swing.event.CaretListener;
import javax.swing.event.UndoableEditEvent;
import javax.swing.event.UndoableEditListener;
import javax.swing.text.DefaultEditorKit;
import javax.swing.text.JTextComponent;
import javax.swing.text.TextAction;
import javax.swing.undo.UndoManager;
import javax.swing.undo.UndoableEdit;

import net.atlanticbb.tantlinger.i18n.I18n;
import net.atlanticbb.tantlinger.ui.UIUtils;



/**
 * Manages an application-wide popup menu for JTextComponents. Any
 * JTextComponent registered with the manager will have a right-click invokable
 * popup menu, which provides options to undo, redo, cut, copy, paste, and
 * select-all. The popup manager is a singleton and must be retrieved with the
 * getInstance() method:
 *
 * <pre><code>
 * JTextField textField = new JTextField(20);
 * TextEditPopupManager.getInstance().registerJTextComponent(textField);
 * </code></pre>
 *
 * @author Bob Tantlinger
 * TODO Internationalize, add mnemonics, etc
 */

public class TextEditPopupManager
{
    private static final I18n i18n = I18n.getInstance("net.atlanticbb.tantlinger.ui.text");

    private static TextEditPopupManager singleton = null;

    public static final String CUT = "cut";
    public static final String COPY = "copy";
    public static final String PASTE = "paste";
    public static final String SELECT_ALL = "selectAll";
    public static final String UNDO = "undo";
    public static final String REDO = "redo";

    private HashMap actions = new HashMap();

    // The actions we add to the popup menu
    private Action cut = new DefaultEditorKit.CutAction();
    private Action copy = new DefaultEditorKit.CopyAction();
    private Action paste = new DefaultEditorKit.PasteAction();
    private Action selectAll = new NSelectAllAction();
    private Action undo = new UndoAction();
    private Action redo = new RedoAction();

    // maintains a list of the currently registered JTextComponents
    private List textComps = new Vector();
    private List undoers = new Vector();

    private JTextComponent focusedComp;// the registered JTextComponent that is
                                        // focused
    private UndoManager undoer; // The undomanager for the focused
                                // JTextComponent

    // Listeners for the JTextComponents
    private FocusListener focusHandler = new PopupFocusHandler();
    private MouseListener popupHandler = new PopupHandler();
    private UndoListener undoHandler = new UndoListener();
    private CaretListener caretHandler = new CaretHandler();
    private JPopupMenu popup = new JPopupMenu();// The one and only popup menu

    private TextEditPopupManager()
    {
        cut.putValue(Action.NAME, i18n.str("cut"));
        cut.putValue(Action.SMALL_ICON, UIUtils.getIcon("cut.png"));
        copy.putValue(Action.NAME, i18n.str("copy"));
        copy.putValue(Action.SMALL_ICON, UIUtils.getIcon("copy.png"));
        paste.putValue(Action.NAME, i18n.str("paste"));
        paste.putValue(Action.SMALL_ICON, UIUtils.getIcon("paste.png"));
        selectAll.putValue(Action.ACCELERATOR_KEY, null);

        popup.add(undo);
        popup.add(redo);
        popup.addSeparator();
        popup.add(cut);
        popup.add(copy);
        popup.add(paste);
        popup.addSeparator();
        popup.add(selectAll);

        actions.put(CUT, cut);
        actions.put(COPY, copy);
        actions.put(PASTE, paste);
        actions.put(SELECT_ALL, selectAll);
        actions.put(UNDO, undo);
        actions.put(REDO, redo);

    }

    /**
     * Gets the singleton instance of TextEditPopupManager
     *
     * @return The one and only TextEditPopupManager
     */
    public static TextEditPopupManager getInstance()
    {
        if(singleton == null)
            singleton = new TextEditPopupManager();
        return singleton;
    }

    public Action getAction(String name)
    {
        return (Action)actions.get(name);
    }

    /**
     * Registers a JTextComponent with the manager. Note that if you change the
     * document of the JTextComponent, you should unregister it with method
     * unregisterJTextComponent, and then re-register it with this method.
     * e.g...
     *
     * <pre><code>
     * TextEditPopupManager.getInstance().registerJTextComponent(comp);
     * ...
     * ...
     * TextEditPopupManager.getInstance().unregisterJTextComponent(comp);
     * comp.setDocument(new PlainDocument());
     * TextEditPopupManager.getInstance().registerJTextComponent(comp);
     * </code></pre>
     *
     * @param tc The JTextComponent to register
     * @throws IllegalArgumentException If the component is null, or already
     * registered
     */
    public void registerJTextComponent(JTextComponent tc) throws IllegalArgumentException
    {
        registerJTextComponent(tc, new UndoManager());
    }

    /**
     * Registers a JTextComponent and UndoManager with the manager. This is
     * useful if you wish to supply a custom UndoManager
     *
     * @param tc The JTextComponent to register
     * @param um The UndoManger to register
     * @throws IllegalArgumentException If the component is null, or already
     * registered
     */
    public void registerJTextComponent(JTextComponent tc, UndoManager um) throws IllegalArgumentException
    {
        if(tc == null || um == null)
            throw new IllegalArgumentException("null arguments aren't allowed");

        if(getIndexOfJTextComponent(tc) != -1)
            throw new IllegalArgumentException("Component already registered");

        tc.addFocusListener(focusHandler);
        tc.addCaretListener(caretHandler);
        tc.addMouseListener(popupHandler);
        tc.getDocument().addUndoableEditListener(undoHandler);

        textComps.add(new WeakReference(tc));
        undoers.add(um);
    }

    /**
     * Unregisters a JTextComponent from the manager.
     *
     * @param tc The JTextComponent to unregister
     */
    public void unregisterJTextComponent(JTextComponent tc)
    {
        int index = getIndexOfJTextComponent(tc);
        if(index != -1)
        {
            tc.removeFocusListener(focusHandler);
            tc.removeCaretListener(caretHandler);
            tc.removeMouseListener(popupHandler);
            tc.getDocument().removeUndoableEditListener(undoHandler);

            textComps.remove(index);
            undoers.remove(index);
        }
    }

    /**
     * Gets the index of a registered JTextComponent
     *
     * @param tc
     * @return
     */
    protected int getIndexOfJTextComponent(JTextComponent tc)
    {
        for(int i = 0; i < textComps.size(); i++)
        {
            WeakReference wr = (WeakReference)textComps.get(i);
            if(wr.get() == tc)
                return i;
        }

        return -1;
    }

    /**
     * Clears any JTextComponent references from the manager that have been
     * garbage collected.
     */
    private void clearEmptyReferences()
    {
        for(int i = 0; i < textComps.size(); i++)
        {
            WeakReference wr = (WeakReference)textComps.get(i);
            if(wr.get() == null)
                undoers.set(i, null);
        }

        for(Iterator it = textComps.iterator(); it.hasNext();)
        {
            WeakReference w = (WeakReference)it.next();
            if(w.get() == null)
                it.remove();
        }

        for(Iterator it = undoers.iterator(); it.hasNext();)
        {
            if(it.next() == null)
                it.remove();
        }
    }

    /**
     * Updates the enabled state of the actions
     */
    private void updateActions()
    {
        if(focusedComp != null && focusedComp.hasFocus())
        {
            undo.setEnabled(undoer.canUndo());
            redo.setEnabled(undoer.canRedo());
            boolean hasSel = focusedComp.getSelectedText() != null;
            copy.setEnabled(hasSel);
            cut.setEnabled(hasSel);
        }
    }

    /*
     * Listens for undoable edits on the documents of registered JTextComponents
     */
    private class UndoListener implements UndoableEditListener
    {
        public void undoableEditHappened(UndoableEditEvent e)
        {
            UndoableEdit edit = e.getEdit();
            if(undoer != null)
            {
                undoer.addEdit(edit);
                updateActions();
            }
        }
    }

    /*
     * Undo and redo actions
     */
    private class RedoAction extends AbstractAction
    {
        private static final long serialVersionUID = 1L;

        public RedoAction()
        {
            super(i18n.str("redo"),
                UIUtils.getIcon("redo-tool.svg"));
        }

        public void actionPerformed(ActionEvent e)
        {
            try
            {
                if(undoer != null)
                {
                    undoer.redo();
                    updateActions();
                }
            }
            catch (Exception ex)
            {
                System.out.println("Cannot Redo");
            }
        }
    }

    private class UndoAction extends AbstractAction
    {
        private static final long serialVersionUID = 1L;

        public UndoAction()
        {
            super(i18n.str("undo"),
                UIUtils.getIcon("undo-tool.svg"));
        }

        public void actionPerformed(ActionEvent e)
        {
            try
            {
                if(undoer != null)
                {
                    undoer.undo();
                    updateActions();
                }
            }
            catch (Exception ex)
            {
                System.out.println("Cannot Undo");
            }
        }
    }

    /*
     * Select all action for the registered JTextComponents
     */
    private class NSelectAllAction extends TextAction
    {
        private static final long serialVersionUID = 1L;

        public NSelectAllAction()
        {
            super(i18n.str("select_all"));
        }

        public void actionPerformed(ActionEvent e)
        {
            getTextComponent(e).selectAll();
        }
    }

    /*
     * Listens for focus changes on the registered components and updates the
     * UndoManager accordingly
     */
    private class PopupFocusHandler implements FocusListener
    {
        public void focusGained(FocusEvent e)
        {
            if(!e.isTemporary())
            {
                JTextComponent tc = (JTextComponent)e.getComponent();
                int index = getIndexOfJTextComponent(tc);
                if(index != -1)
                {
                    // set the current UndoManager for the currently focused
                    // JTextComponent
                    undoer = (UndoManager)undoers.get(index);
                    focusedComp = tc;
                    updateActions();
                }

                // clean up any dead refs that have been garbage collected
                clearEmptyReferences();
            }
        }

        public void focusLost(FocusEvent e)
        {
        }
    }

    /*
     * Listens for caret changes on the registered JTextComponents
     */
    private class CaretHandler implements CaretListener
    {
        public void caretUpdate(CaretEvent e)
        {
            updateActions();
        }
    }

    /*
     * Handles right clicks on the component to popup the menu
     */
    private class PopupHandler extends MouseAdapter
    {
        public void mousePressed(MouseEvent e)
        {
            checkForPopupTrigger(e);
        }

        public void mouseReleased(MouseEvent e)
        {
            checkForPopupTrigger(e);
        }

        private void checkForPopupTrigger(MouseEvent e)
        {
            JTextComponent tc = (JTextComponent)e.getComponent();
            if(e.isPopupTrigger() && tc.isEditable())
            {
                if(!tc.isFocusOwner())
                    tc.requestFocusInWindow();

                popup.show(tc, e.getX(), e.getY());
            }
        }
    }
}

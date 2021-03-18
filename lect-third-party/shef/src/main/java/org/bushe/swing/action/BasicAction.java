/**
 * Copyright 2005 Bushe Enterprises, Inc., Hopkinton, MA, USA, www.bushe.com
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.bushe.swing.action;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import javax.swing.AbstractAction;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.KeyStroke;
import javax.swing.event.EventListenerList;

/**
 * A useful base class to use for Actions.  See package documentation.
 * <p>
 * The Swing Action Manager libray makes no requirements for its use, but if you use
 * BasicAction or its interfaces itegrating with the Action Framework is a lot easier.
 * <p>
 * Typical usage of BasicAction is to extend and override execute(), or
 * add a an ActionListener to the action. To handle enablement, either
 * shouldBeEnabled() is overriden or an EnabledUpdater delegate is added to the
 * action.
 * <p>
 * BasicAction implements:
 * <ul>
 * <li>{@link Actionable} so that actionPerformed can be delegated, typically to a
 * controller
 * <li>{@link ItemAction} so the framework (specifically ActionUIFactory) can keep
 * multiple JCheckBoxMenuItems (and similar) checked when they share the
 * same action.  Typically applications will not need to use ItemAction or
 * ItemEvents, use ActionEvents instead (i.e. override execute() or call
 * addActionListener()).  To find out whether an action is selected, call
 * BasicAction's isSelected() method.
 * <li>{@link EnabledUpdater} to allow an application controller to tell the action
 * to set itself as enabled or disabled, typically by calling it's enabled
 * delegates or checking it's context.
 * <li>{@link DelegatesEnabled} to allow the action to defer to a delegate (typically an
 * application controller) when it computes whether it should be enabled or
 * disabled.
 * <li>{@link ContextAware} to allow a context to be set on an action, the context can
 * contain anything the action needs to do its work - data, active components,
 * etc.  By default, the action calls updateEnabledState() when the context changes.
 * </ul>
 * <p>
 * It is recommended that BasicAction be extended for all an application's
 * actions, particularly to improve exception handling.  An easy way to
 * accomplish this is to create the class and set the defaultActionClass
 * attribute on the action-set element to your applications derivative of
 * BasicAction.
 * @see Package Documentation for a complete description
 * @author Michael Bushe
 */
public class BasicAction extends AbstractAction implements Actionable, ItemAction,
        DelegatesEnabled, ContextAware  {
    /** The value returned by shouldBeEnabled() by default.*/
    protected static boolean DEFAULT_ENABLED_STATE = true;

    //Used for multiple event types - actions, events, for the action
    EventListenerList listenerList = new EventListenerList();

    /** If set, calls to shouldBeEnabled are delegated to the list.*/
    private List enabledDelegates;
    /** A map of context values that can be set by the user and used for any purpose.
     * Most typically it would be set to a model the component would use
     * or a component or window that the action must run in the context of.*/
    private Map context;
    /** JUST FOR DEBUGGING PURPOSES - so the action is easily identifiable
     * in a watch list */
    private String idForDebugging = null;

    /**
     * Default Constructor, nice for use with XML.
     */
    public BasicAction() {
    }

    /**
     * Simple name-only constructor
     * @param id the action id, name, command name (button name)
     * @see AbstractAction
     */
    public BasicAction(String id) {
        this(id, null);
    }

    /**
     * Simple id and icon constructor
     * @param id the id, name, and command name of the action
     * @param icon the icon used in the actoin's toolbar button or menu items
     * @see javax.swing.AbstractAction
     */
    public BasicAction(String id, Icon icon) {
        this(id, null, null, null, null, icon);
    }

   /**
    * Commonlu used constructor
    * @param id the id, name, and command name of the action
    * @param mnemonic the action's mnemonic - for menu traversal
    * @param accelerator the action's accelerator - "quick key", no menu
    * @param icon the icon used in the actoin's toolbar button or menu items
    * @see javax.swing.AbstractAction
    */
   public BasicAction(String id, Integer mnemonic, KeyStroke accelerator, Icon icon) {
       this(id, null, null, mnemonic, accelerator, icon);
   }


   /**
    * Same as nearly Full Constructor, but the id, name and command name are
    * all set to the id.
    * @param id the action's id, name (menu text), and command name
    * @param shortDesc a short description of the action (tooltip)
    * @param longDesc the long description for the action (help possibility)
    * @param mnemonic the action's mnemonic - for menu traversal
    * @param accelerator the action's accelerator - "quick key", no menu
    * @param icon the action's icon (toolbar/menu icon)
    */
   public BasicAction(String id, String shortDesc, String longDesc, Integer mnemonic,
                     KeyStroke accelerator, Icon icon) {
      this(id, id, id, shortDesc, longDesc, mnemonic, accelerator, icon);
   }


   /**
    * Nearly Full Constructor
    * @param actionName the action's name (menu text)
    * @param actionCommandName the name of the action event
    * @param shortDesc a short description of the action (tooltip)
    * @param longDesc the long description for the action (help possibility)
    * @param mnemonic the action's mnemonic - for menu traversal
    * @param accelerator the action's accelerator - "quick key", no menu
    * @param icon the action's icon (toolbar/menu icon)
    */
   public BasicAction(String id, String actionName, String actionCommandName,
                     String shortDesc, String longDesc, Integer mnemonic,
                     KeyStroke accelerator, Icon icon) {
      this(id, id, id, shortDesc, longDesc, mnemonic, accelerator, icon, false, false);
   }

    /**
     * Full Constructor
     * @param actionName the action's name (menu text)
     * @param actionCommandName the name of the action event
     * @param shortDesc a short description of the action (tooltip)
     * @param longDesc the long description for the action (help possibility)
     * @param mnemonic the action's mnemonic - for menu traversal
     * @param accelerator the action's accelerator - "quick key", no menu
     * @param icon the action's icon (toolbar/menu icon)
     * @param toolbarShowsText do toolbar buttons created from this action show their text
     * @param menuShowsIcon do menu items created from this action show their icon
     */
    public BasicAction(String id, String actionName, String actionCommandName,
                      String shortDesc, String longDesc, Integer mnemonic,
                      KeyStroke accelerator, Icon icon,
            boolean toolbarShowsText, boolean menuShowsIcon) {
       setId(id);
       setActionName(actionName);
       setActionCommandName(actionCommandName);
       setShortDescription(shortDesc);
       setLongDescription(longDesc);
       setMnemonic(mnemonic);
       setAccelerator(accelerator);
       setSmallIcon(icon);
       this.setToolbarShowsText(toolbarShowsText);
       this.setMenuShowsIcon(menuShowsIcon);
    }


    /*
     * ACTION-RELATED
     */

    /**
     * Adds a "callback" action listener to this action.  The callback allows the
     * handling of the action to be delegated to other objects.  BasicActions override
     * their actionPerformed method() propogate the event to teh execute() method and
     * the callback delegates
     * @param l the callback action listener
     */
    public void addActionListener(ActionListener l) {
        listenerList.add(ActionListener.class, l);
    }

    /**
     * Removes an callback action listener for this action.
     * @param l ActionListener
     */
    public void removeActionListener(ActionListener l) {
        listenerList.remove(ActionListener.class, l);
    }

    /**
     * Implements actionPerformed by calling templateMethod().  Derived classes
     * can override {@link #actionPerformedTemplate(ActionEvent) actionPerformedTemplate} to change the
     * default template, but usually don't have to.  Instead, derived classes
     * can override execute() to handle actionPerformed responsiblilities, or
     * rely on a callback action listener being set.
     * @param evt the action event
     * @see #actionPerformedTemplate(java.awt.event.ActionEvent)
     */
    public final void actionPerformed(ActionEvent evt) {
        actionPerformedTemplate(evt);
    }

    /**
     * The template method for {@link #actionPerformed(ActionEvent)}.
     * The default template is defined to allow either delegated or derived
     * implementation.  The execute() method is always called.  If action listeners are
     * added to the action, then they are called too.  The actionPerformedTry/Catch/Finally methods are
     * called around the execute()/propogateActionEvent() methods.
     * <p>To handle application issues such as cursors and threading, the
     * actionPerformedTry/Catch/Finally methods can be overridden, or the
     * template method for actionPerformed itself can be changed by overridding
     * this method.
     * @param evt the action event sent to actionPerformed()
     */
    protected void actionPerformedTemplate(ActionEvent evt) {
        try {
            actionPerformedTry();
            execute(evt);
            propogateActionEvent(evt);
        } catch (Throwable t) {
            actionPerformedCatch(t);
        } finally {
            actionPerformedFinally();
        }
    }

    /**
     * Called during the {@link #actionPerformedTemplate(ActionEvent)}
     * <p>
     * Propogates the ActionEvent to ActionListener delegates added via
     * {@link #addActionListener(ActionListener)}
     * Called after execute, allowing underlying actions to override
     * (selectively not calling delegates, for example).
     *
     * @param evt ActionEvent received by actionPerformed(ActionEvent).
     */
    protected void propogateActionEvent(ActionEvent evt) {
        // Guaranteed to return a non-null array
        Object[] listeners = listenerList.getListenerList();
        // Process the listeners last to first, notifying
        // those that are interested in this event
        for (int i = listeners.length - 2; i >= 0; i -= 2) {
            if (listeners[i] == ActionListener.class) {
                // Lazily create the event:
                ((ActionListener) listeners[i + 1]).actionPerformed(evt);
            }
        }
    }


    /**
     * This method is the first method called during {@link #actionPerformedTemplate(ActionEvent)}.
     * By default it does nothing.
     */
    protected void actionPerformedTry() {
    }

    /**
     * Typically overridden to do the action's work.
     * Called during {@link #actionPerformedTemplate(ActionEvent)}.
     * Derived classes can choose to override this no-op template method or provide an
     * ActionListener delgate instead by calling {@link #addActionListener(ActionListener)}.
     * @throws Exception if an exception is throws, actionPerformedTemplate calls
     * {@link #actionPerformedCatch(Throwable t)} with it.
     */
    protected void execute(ActionEvent evt) throws Exception {
    }

    /**
     * This method is called during {@link #actionPerformedTemplate(ActionEvent)} if the
     * {@link #execute(ActionEvent)} or any callback ActionListeners.
     * <p>
     * The default is pretty lame, it prints the error and stack trace to System.err, then
     * throws a RuntimeException.
     * <p>
     * It is recommended that BasicAction be extended for all an application's
     * actions, particularly to improve handle exceptions.  An easy way to
     * accomplish this is to create the class and set the defaultActionClass
     * attribute on the action-set element.
     */
    protected void actionPerformedCatch(Throwable t) {
        System.err.println("Exception in action "+this+".  Exception:"+t);
        t.printStackTrace(System.err);
        throw new RuntimeException(t);
    }

    /**
     * This method is the last method called during {@link #actionPerformedTemplate(ActionEvent)} .
     * By default it does nothing.   Derived classes may override this method
     * to clean up from actionPerformed calls.
     */
    protected void actionPerformedFinally() {
    }

    /*
     * ENABLED-RELATED
     */

    /**
     * Adds a delegate object to determine enablement during updateEnabledState()
     * @param enabledDelegate the enablement callback delegate
     */
    public void addShouldBeEnabledDelegate(ShouldBeEnabledDelegate enabledDelegate) {
        if (enabledDelegates == null) {
            enabledDelegates = new ArrayList(3);
        }
        enabledDelegates.add(enabledDelegate);
    }

    /**
     * Removes a delegate object that determines enablement during updateEnabledState()
     * @param enabledDelegate the enablement callback delegate
     */
    public void removeShouldBeEnabledDelegate(ShouldBeEnabledDelegate enabledDelegate) {
        if (enabledDelegates == null) {
            return;
        }
        enabledDelegates.remove(enabledDelegate);
    }

    /**
     * Called to force the action to call setEnabled(boolean).
     * <p>
     * If the action can figure it out, it should call setEnabled(false or true)
     * on itself appropriately.
     * <p>
     * By default, this method simply calls setEnabled(shouldBeEnabled())
     */
    public void updateEnabledState() {
        boolean shouldBe = shouldBeEnabled();
        setEnabled(shouldBe);
    }

    /**
     * Called by clients to ask the action whether it should be enabled or
     * disabled given the current "state of affairs."
     * <p>
     * Makes no change to the action.
     * <p>
     * By default, the {@link ShouldBeEnabledDelegate}'s added via addShouldBeEnabledDelegate() are used.
     * If no ShouldBeEnabledDelegates are set, then true is return (actually DEFAULT_ENABLED_STATE).
     * If there are any delegates, then if <em>any</em> delegate's shouldBeEnabled() return false
     * (or !DEFAULT_ENABLED_STATE), then false is called.  Only if they all return true (DEFAULT_ENABLED_STATE)
     * is the true (DEFAULT_ENABLED_STATE) returned.
     * @return whether setEnabled should be called with false or true
     */
    public boolean shouldBeEnabled() {
        if (enabledDelegates == null || enabledDelegates.isEmpty()) {
            return DEFAULT_ENABLED_STATE;
        }
        List copy = new ArrayList(enabledDelegates);
        Iterator iter = copy.iterator();
        while (iter.hasNext()) {
            ShouldBeEnabledDelegate enabledUpdater = (ShouldBeEnabledDelegate) iter.next();
            if (enabledUpdater != null) {
                if (enabledUpdater.shouldBeEnabled(this) != DEFAULT_ENABLED_STATE) {
                    return !DEFAULT_ENABLED_STATE;
                }
            }
        }
        return DEFAULT_ENABLED_STATE;
    }

    /*
     * ITEM-RELATED
     */

    /**
     * Adds an item listener for toggle action.  Usually not needed since actionPerformed() is
     * called.  Used by the {@link org.bushe.swing.action.ActionSelectionSynchronizer} to synchronize
     * components that share an action.
     * @param l and item listener
     */
    public void addItemListener(ItemListener l) {
        listenerList.add(ItemListener.class, l);
    }

    /**
     * Removes an item listener.  Typically not needed.
     * @param l listener to remove
     */
    public void removeItemListener(ItemListener l) {
        listenerList.remove(ItemListener.class, l);
    }

    /**
     * Calls all ItemListeners with the ItemEvent
     * @param evt the event to propogate to listeners.
     */
    protected void propogateItemEvent(ItemEvent evt) {
        // Guaranteed to return a non-null array
        Object[] listeners = listenerList.getListenerList();
        // Process the listeners last to first, notifying
        // those that are interested in this event
        for (int i = listeners.length - 2; i >= 0; i -= 2) {
            if (listeners[i] == ItemListener.class) {
                // Lazily create the event:
                ((ItemListener) listeners[i + 1]).itemStateChanged(evt);
            }
        }
    }


  /**
   * @return true if the action is in the selected state  value of ActionManager.SELECTED is TRUE
   */
  public boolean isSelected()  {
      Object actionSelected = getValue(ActionManager.SELECTED);
      return Boolean.TRUE.equals(actionSelected);
  }

  /**
   * Changes the selected state of the action. If selected is different
   * from the current state, then a "selected" property change is fired.
   * @param selected true to set the action as selected of the action.
   */
  public synchronized void setSelected(boolean selected) {
      boolean oldValue = isSelected();
      if (oldValue != selected) {
          Boolean selectedBool = selected?Boolean.TRUE:Boolean.FALSE;
          putValue(ActionManager.SELECTED, selectedBool);
          firePropertyChange("selected", oldValue?Boolean.TRUE:Boolean.FALSE,
                             selectedBool);
      }
  }

   /**
    * This overrides AbstractAction.getValue() to allow LONG_DESCRIPTION to fallback to
    * SHORT_DESCRIPTION if no long description has been set.
    * @param key the name of the attribute to lookup
    * @return the value of the key, which may be null if no value is set
    */
    public Object getValue(String key) {
        if (!LONG_DESCRIPTION.equals(key)) {
           return super.getValue(key);
        }
        Object longDesc = super.getValue(LONG_DESCRIPTION);
        if (longDesc != null) {
           return longDesc;
        }
        return super.getValue(SHORT_DESCRIPTION);
    }

   /**
    * @return the action's ActionManager.GROUP value
    */
    public Object getGroup() {
        return getValue(ActionManager.GROUP);
    }


  /*
  * CONTEXT
  */

  /**
   * Allows an action to be "contextualized" with a map of object-value pairs
   * (separate from the the putValue()/getValue() string-value properties of
   * the action).  The context can be set via {@link ActionList#setContextForAll(java.util.Map)}
   * {@link ActionList#putContextValueForAll(Object, Object)},
   * {@link ActionManager#createAction(Object, java.util.Map)},
   * {@link ActionManager#createActionList(Object, java.util.Map)}, or
   * {@link ActionManager#putContextValueForAll(Object, Object)}.
   * methods or ActionManager's getList and createList methods, or
   * ActionUIFactory's createMethods.
   * <p>
   * The <name-value-pair> elements in Action XML do NOT go into the context, they
   * go into the normal action putValue()/getValue() set.
   * <p>
   * Calls contextChanged(), which calls updateEnabledState
   * @param context a context object
   * @see ContextAware
   */
    public void setContext(Map context) {
        this.context = context;
        contextChanged();
    }

    /**
     * Gets the context map for the action.
     * <p>
     * Different from put/getValue() string-value pairs and <name-value-pair> elements.
     * @return the context, can be null
     */
    public Map getContext() {
        return context;
    }

    /**
     * Clears the action's context.
     */
    public void clearContext() {
        if (context != null) {
            context.clear();
        }
        contextChanged();
    }

    /**
     * Puts a single value in the action's context map.
     * <p>
     * Calls contextChanged().
     * <p>
     * Different from put/getValue() string-value pairs and <name-value-pair> elements.
     * @param key the context key
     * @param contextValue the context value
     */
    public void putContextValue(Object key, Object contextValue) {
        if (context == null) {
            context = new HashMap(3);
        }
        context.put(key, contextValue);
        contextChanged();
    }

    /**
     * Gets a value out of the context.
     * <p>
     * Different from put/getValue() string-value pairs and <name-value-pair> elements.
     * @return the context value set on this action for the given key, null if not set
     */
    public Object getContextValue(Object key) {
        if (context == null) {
            return null;
        }
        return context.get(key);
    }

    /**
     * Called when the context is set or a value is added ro removed.
     * Override to take action on a changing context, such as
     * enabling or disabling.  Default does calls updateEnabledState()
     */
    protected void contextChanged() {
        updateEnabledState();
    }


    /*
     * PROPERTIES
     */

    /**
     * The action name is the name of the action in the application.
     * Should be unique to it's ActionManager.
     * @return the id of the action
     */
    public String getId() {
        return (String) getValue(ActionManager.ID);
    }

    /**
     * Sets the id of the action.
     * @param id action's ActionManager.ID property value
     */
    public void setId(String id) {
        putValue(ActionManager.ID, id);
    }

    /**
     * The action name is the name of the action, usually the name of buttons created from the action.
     * Should be unique to it's universe of listeners. Same as AbstractActions's NAME value.
     * @return the name of the action
     */
    public String getActionName() {
        return (String) getValue(NAME);
    }

    /**
     * Sets the NAME of the action.
     * @param actionName the action's name property
     */
    public void setActionName(String actionName) {
        putValue(NAME, actionName);
    }

    /**
     * The command string for the ActionEvent that will be created when
     * this Action is fired.
     * Should be unique to it's universe of listeners. Same as AbstractActions's
     * ACTION_COMMAND_KEY value.
     * @return the name of the action
     */
    public String getActionCommandName() {
        return (String) getValue(ACTION_COMMAND_KEY);
    }

    /**
     * Sets the ACTION_COMMAND_KEY of the action's command.
     * @param actionCommandName the action's command's name property
     */
    public void setActionCommandName(String actionCommandName) {
        putValue(ACTION_COMMAND_KEY, actionCommandName);
    }

    /**
     * The short description for this action, used in toolip text.
     * @return the SHORT_DESCRIPTION of this action.
     */
    public String getShortDescription() {
        return (String) getValue(SHORT_DESCRIPTION);
    }

    /**
     * Sets the short description of the action.
     * @param shortDesc the action's SHORT_DESCRIPTION property
     */
    public void setShortDescription(String shortDesc) {
        putValue(SHORT_DESCRIPTION, shortDesc);
    }

    /**
     * Used for storing a longer description for the
     * action, could be used for context-sensitive help
     * @return longDesc get the long description of this action.
     */
    public String getLongDescription() {
        return (String) getValue(LONG_DESCRIPTION);
    }

    /**
     * Sets the long description of the action.
     * @param longDesc the action's long description property
     */
    public void setLongDescription(String longDesc) {
        putValue(LONG_DESCRIPTION, longDesc);
    }

    /**
     * Mnemonics offer a way to use the keyboard to navigate the menu hierarchy,
     * increasing the accessibility of programs. Accelerators, on the other hand,
     * offer keyboard shortcuts to bypass navigating the menu hierarchy.
     * Mnemonics are for all users; accelerators are for power users.
     * @return the MNEMONIC_KEY for this action.
     */
    public Integer getMnemonic() {
        return (Integer) getValue(MNEMONIC_KEY);
    }

    /**
     * @param mnemonic  make like so: new Integer(KeyEvent.VK_L)
     */
    protected void setMnemonic(Integer mnemonic) {
        putValue(MNEMONIC_KEY, mnemonic);
    }

    /**
     * Accelerators offer keyboard shortcuts to bypass navigating the menu
     * hierarchy.  Mnemonics, on the other hand, offer a way to use the
     * keyboard to navigate the menu hierarchy, increasing the accessibility
     * of programs.  The action must have a menu for accelerators to work.
     * Mnemonics are for all users; accelerators are for power users.
     * @return this action's ACCELERATOR_KEY
     */
    public KeyStroke getAccelerator() {
        return (KeyStroke) getValue(ACCELERATOR_KEY);
    }

    /**
     * Sets the ACCELERATOR_KEY key of the action.
     * @param accelerator the action's shortcut
     */
    public void setAccelerator(KeyStroke accelerator) {
        putValue(ACCELERATOR_KEY, accelerator);
    }

    /**
     * @return the SMALL_ICON for the action (used in toolbars)
     */
    public ImageIcon getSmallIcon() {
        return (ImageIcon) getValue(SMALL_ICON);
    }

    /**
     * Sets the icon of the action.
     * @param smallIcon the action's icon property
     */
    public void setSmallIcon(Icon smallIcon) {
        putValue(SMALL_ICON, smallIcon);
    }

    /**
     * Should a toolbar that has this action show the text and the icon or
     * just the icon?
     * <p>The default for TOOLBAR_SHOWS_TEXT is false</p>
     * @param toolbarShowsText true to show text and icon
     */
    public void setToolbarShowsText(boolean toolbarShowsText) {
        if (toolbarShowsText) {
            putValue(ActionManager.TOOLBAR_SHOWS_TEXT, Boolean.TRUE);
        } else {
            putValue(ActionManager.TOOLBAR_SHOWS_TEXT, Boolean.FALSE);
        }
    }

    /**
     * Does the toolbar constructed with this action show the text?
     * @return true if it shows text (TOOLBAR_SHOWS_TEXT property is TRUE)
     */
    public boolean getToolbarShowsText() {
       Object value = getValue(ActionManager.TOOLBAR_SHOWS_TEXT);
       if (value == null) {
          return false;
       }
       return ((Boolean)value).booleanValue();
    }

    /**
     * Should a menu that has this action show the icon and the text or
     * just the text?
     * <p>The default for MENU_SHOWS_ICON is false</p>
     * @param menuShowsIcon true to show text and icon
     */
    public void setMenuShowsIcon(boolean menuShowsIcon) {
        if (menuShowsIcon) {
            putValue(ActionManager.MENU_SHOWS_ICON, Boolean.TRUE);
        } else {
            putValue(ActionManager.MENU_SHOWS_ICON, Boolean.FALSE);
        }
    }

    /**
     * Do menus constructed with this action show the icon?
     * @return true is shows the icon
     */
    public boolean getMenuShowsIcon() {
       Object value = getValue(ActionManager.MENU_SHOWS_ICON);
       if (value == null) {
          return false;
       }
       return ((Boolean)value).booleanValue();
    }

    /**
     * Get the list of role names that this action should be restricted to.
     * <p>
     * The action doesn't use this (though it could disable).  Instead the ActionManager and
     * ActionUIFactory use this method (@tood - not yet!)
     * @return a List of String's
     */
    public List getRoles() {
        return (List) getValue(ActionManager.ACTION_ROLES);
    }

    /**
     * Set the list of role names that this action should be restricted to.
     * @param roles a List of String's
     */
    public void setRoles(List roles) {
        putValue(ActionManager.ACTION_ROLES, roles);
    }

    /**
     * Returns an ImageIcon, or null if the path was invalid.
     * Example "toolbarButtonGraphics/myImage.gif"
     * @param resourcePath A path passed this.getClass().getResource(String);
     * @return a brand new icon, avoiding calling twice for same path
     */
    protected ImageIcon createIcon(String resourcePath) {
        return createIcon(getClass().getResource(resourcePath));
    }

    /**
     * Returns an ImageIcon, or null if the path was invalid.
     * Example "toolbarButtonGraphics/myImage.gif"
     * @param imageURL A URL to the image
     * @return a brand new icon, avoiding calling twice for same path
     */
    protected ImageIcon createIcon(java.net.URL imageURL) {
        if (imageURL == null) {
            return null;
        } else {
            return new ImageIcon(imageURL);
        }
    }

    /**
     * Overridden to set the idForDebugging for easy debugging and to
     * use the result of value.intern() if the value is a String.  This
     * allows all keys to be compared using == instead of .equals().
     * @param key the key of the property
     * @param value the value of the property
     * @see java.lang.String#intern()
     */
    public void putValue(String key, Object value) {
        if (key != null && value != null) {
           if (key.equals(ActionManager.ID)) {
               idForDebugging = value.toString();
           }
           if (value instanceof String) {
               value = ((String)value).intern();
           }
        }
        super.putValue(key, value);
    }

    /**
     * Overridden to give a nice string
     * @return a descriptive string.
     */
    public String toString() {
        StringBuffer buf = new StringBuffer(100);
        buf.append(super.toString()+" [id=");
        buf.append(getId());
        buf.append(", enabled=");
        buf.append(isEnabled());
        buf.append(", values={");
        Object[] keys = getKeys();
        if (keys != null) {
            for (int i = 0; i < keys.length; i++) {
                buf.append(keys[i]);
                buf.append("->");
                buf.append(getValue(String.valueOf(keys[i])));
                buf.append(";");
            }
        }
        buf.append("}, context=");
        buf.append(getContext());
        buf.append(", enabled delegates=");
        buf.append(enabledDelegates);
        buf.append("]");
        return buf.toString();
    }
}

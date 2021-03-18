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

import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.awt.Color;
import java.awt.Dimension;
import javax.swing.AbstractButton;
import javax.swing.Action;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.JRadioButton;
import javax.swing.JRadioButtonMenuItem;
import javax.swing.JSeparator;
import javax.swing.JToggleButton;
import javax.swing.JToolBar;

/**
 * Creates user interface elements based on Actions.
 * <p>
 * Menus, popup menus and toolbars are created with ActionLists or ids of
 * ActionLists.  When using ids, the ActionLists are retrieved from the
 * ActionManager.  The default ActionManager is used, but another ActionManager
 * can be set on this class.
 * <p>
 * When creating toolbars, ActionLists are treated as single-dimentional.  When
 * creating menu's sub lists in ActionLists caus rethe creation of sub menus.
 * <p>
 * When creating menus, the ActionList's tigger action is respected if non-null.
 * When present, the corresponding Action becomes the Menu's Action, which
 * differs from the Menu Item actions, since it is the action that gets fired
 * when the menu is displayed.  This gives a chance to customize the menu before
 * it is displayed.
 * <p>
 * Besides the standard Swing AbstractAction values, the ActionUIManager
 * respects the following Action attributes (action.getValue(Object)):
 * <pre>
 * ActionManager.MENU_SHOWS_ICON - sometimes an action has an icon defined that
 * you want to appear in the toolbar, but not in the menu item.  Setting this
 * value to Boolean.FALSE will have this effect.
 * ActionManager.BUTTON_TYPE - when set to "radio", "checkbox" or "toggle"
 * then a radio button, toggle button, or checkbox is created.  GROUP overrides
 * this property.
 * ActionManager.WEIGHT - orders menu items and separators
 * ActionManager.GROUP - when set, groups buttons together so that only one
 * is selected at a time.  Internally a ButtonGroup is created (if you really
 * want the button's button group, you can get it from a button's model).  A
 * toggle button is created for toolbars and check button is created for menus.
 * @todo - allow override of hard coded group rule for toolbars or menus
 * </pre>
 * <p>
 * The instantiate* methods are provided to allow the custom menus and buttons
 * to be created instead of the default Swing classes.
 * <p>
 * Inspired partly from <a href="http://www.javadesktop.org/articles/actions/">
 * Mark Davidson's Easy Actions'</a> UIFactory, though Mark and Michael Bushe
 * developed similar frameworks simultaneously.  Renamed since apps
 * have many UIFactories and this one doesn't create all you would need.
 *
 * @todo - Popups are different from regular menus since they don't have actions
 * they figure you don't need them since you are deciding when the popup
 * appears and can customize then.  So how about adding a popup menu listener
 * that asks the actions if they should be removed or
 * @see ActionManager
 */
public class ActionUIFactory {

    private static ActionUIFactory INSTANCE;

    /** Used to hold named instances*/
    private static Map NAMED_INSTANCES;

    private static final Object INSTANCE_LOCK = new Object();

    /** If 1.4, need to set request focus enabled on toolbar buttons*/
    private static boolean sToolbarRequestFocusEnabled14 = !(System.getProperty(
        "java.vm.version").indexOf("1.3") > -1);
    /** Check the VM version to determine some default settings.  JDK 1.4
     * works nicely, but 1.3 needs some hand holding.*/
    private static boolean sSetButtonSizeFor13 = System.getProperty(
        "java.vm.version").indexOf("1.3") > -1;

    /** Allows configuration of size of toolbar buttons.*/
    private Dimension toolbarButtonPreferredSize = null;

    // Pass -Ddebug=true to enable debugging
    private static boolean DEBUG = false;

    private ActionManager actionManager;

    /**
     * Cannot instantiate this directly, only because if someone is using the API
     * quickly, then they are steered to the commonly used getInstance() method.
     * <p>
     * However, since more than one ActionUIFactory may be useful in certain situations, the constructor
     * is made protected to allow multiple ActionUIFactory's to exist by extension.  It's a bit of a pain,
     * but it's a bigger pain to expose this publicly when it would rarely be needed.
     * @param manager the manager to use with the factory, if null the ActionManager.getInstance() will be used.
     */
    protected ActionUIFactory(ActionManager manager) {
        if (manager == null) {
            manager = ActionManager.getInstance();
        }
        this.actionManager = manager;
    }

    /**
     * Return the instance of the ActionUIFactory if this will be used as a singleton. The instance will be created
     * if it hasn't previously been set.
     *
     * @return the UIFactory instance.
     * @see #setInstance
     */
    public static ActionUIFactory getInstance() {
        synchronized (INSTANCE_LOCK) {
            if (INSTANCE == null) {
                INSTANCE = new ActionUIFactory(ActionManager.getInstance());
                setInstance(null, INSTANCE);
            }
        }
        return INSTANCE;
    }

   /**
    * Creates a new default ActionUIFactory instance, associates it with the provided ActionManager
    * and adds it as an instance with the given name.  Typically not needed, but can be useful
    * if using mulitple named factories with multiple managers (without specialized factories).
    * <p>
    * Calling this method twice with the same name silently replaces the old factory with the new one.
    * @param name the name to use in getInstance(String) to get the instance, if null it replaces
    * the default ActionUIFactory returned via getInstance().
    * @param manager the ActionManager to associate with the newly created ActionUIFactory
    */
   public static ActionUIFactory createNamedInstance(String name, ActionManager manager) {
      ActionUIFactory factory = new ActionUIFactory(manager);
      setInstance(name, factory);
      return factory;
   }

    /**
     * Add a named static ActionUIFactory instance.  Typically not needed, but can be useful
     * if using mulitple named factories and the factories are customized.
     * <p>
     * Calling this method twice with the same name silently replaces the old factory with the new one.
     * @param name the name to use in getInstance(String) to get the instance, if null it replaces
     * the default ActionUIFactory returned via getInstance().
     * @param factory the ActionUIFactory to match to the name
     */
    public static void setInstance(String name, ActionUIFactory factory) {
       setInstance(name, factory, factory==null?null:factory.actionManager);
    }

   /**
    * Add a named static ActionUIFactory instance with an associated manager.  Typically not needed,
    * but can be useful if using mulitple named managers.
    * <p>
    * Calling this method twice with the same name silently replaces the old factory with the new one.
    * @param name the name to use in getInstance(String) to get the instance, if null it replaces
    * the default ActionUIFactory returned via getInstance().
    * @param factory the ActionUIFactory to match to the name
    */
    public static void setInstance(String name, ActionUIFactory factory, ActionManager manager) {
      synchronized (INSTANCE_LOCK) {
            if (NAMED_INSTANCES == null) {
                NAMED_INSTANCES = Collections.synchronizedMap(new HashMap());
            }
            if (factory != null) {
               factory.actionManager = manager;
            }
            NAMED_INSTANCES.put(name, factory);
            if (name == null) {
                INSTANCE = factory;
            }
        }
    }

    /**
     * Get a named static ActionUIFactory instance.
     * @param name the name of the instance as added by setInstance(), if null call is
     * same as getInstance();
     * @return the named ActionUIFactory instance, null if not found.
     */
    public static ActionUIFactory getInstance(String name) {
        synchronized (INSTANCE_LOCK) {
            if (NAMED_INSTANCES == null) {
                if (name == null) {
                    return getInstance();
                } else {
                    return null;
                }
            } else {
                return (ActionUIFactory)NAMED_INSTANCES.get(name);
            }
        }
    }

    /**
     * Gets the ActionManager this factory uses. If the ActionManager has not been
     * explicitly set then the default ActionManager instance will be used.
     *
     * @return the ActionManager used by the ActionUIFactory.
     */
    public ActionManager getActionManager() {
        return actionManager;
    }

    /**
     * Set the preferred size of toolbar buttons that are created by the factory.
     */
    public void setToolbarButtonPreferredSize(Dimension toolbarButtonPreferredSize) {
        this.toolbarButtonPreferredSize = toolbarButtonPreferredSize;
    }

    /**
     * Constructs a new toolbar from an action-list id by getting an ActionList
     * from the ActionManager and calling {@link #createToolBar(ActionList)}.
     * By convention, the identifier of the main toolbar should be
     * "main-toolbar".
     * <p>
     * This method depends on the ActionManager and calls getActionList(listId),
     * on the ActionManager, which creates the actions on first use and reuse
     * the same actions when another component is created using the same id.
     * If you want a new set of actions (say, one toolbar for each of two
     * panels of the same type), then use
     * {@link #createToolBar(ActionList) createToolBar(List)} with a list
     * of new Actions (use {@link ActionManager#createActionList(Object)
     * createActionList(Object)} to create the new set of Actions from an action
     * list id.)
     * <p>
     * @param actionListId action-list id which should be used to construct the toolbar.
     * The id must have been registered with the ActionManager
     * @return a new toolbar or null if the id doesn't match a registered
     * list in the ActionManager
     */
    public JToolBar createToolBar(Object actionListId) {
        ActionList actions = getActionManager().getActionList(actionListId);
        if (actions == null) {
            return null;
        }

        //note: treated as 1 dimensional list
        return createToolBar(actions);
    }


    /**
     * Constructs a new menubar from an action-list id by getting its ActionList
     * from the ActionManager and calling {@link #createMenuBar(ActionList)}.
     * By convention, the identifier of the main toolbar should be
     * "main-toolbar".
     * <p>
     * This method depends on the ActionManager and calls getActionList(listId),
     * on the ActionManager, which creates the actions on first use and reuse
     * the same actions when another component is created using the same id.
     * If you want a new set of actions (say, one toolbar for each of two
     * panels of the same type), then use
     * {@link #createMenuBar(ActionList) createMenuBar(List)} with a list
     * of new Actions (use {@link ActionManager#createActionList(Object)
     * createActionList(Object)} to create the new set of Actions from an action
     * list id.)
     * <p>
     * @param actionListId action-list id which should be used to construct the menubar.
     * The id must have been registered with the ActionManager
     * @return a new menubar or null if the id doesn't match a registered
     * list in the ActionManager
     */
    public JMenuBar createMenuBar(Object actionListId) {
        ActionList list = getActionManager().getActionList(actionListId);
        if (list == null) {
            return null;
        }
        JMenuBar menubar = instantiateJMenuBar();
        loadActions(list, menubar);
        return menubar;
    }

    /**
     * Create a toolbar using the list of actions provided.
     * <p>
     * Toolbars are 1 dimensional, so sublists in ActionLists are ignored.
     * <p>
     * See class documentation as to what is created for different Actions.
     * <p>
     * @param actions an ActionList of actions, put a Separator or
     * null in the list if you want separators
     * @return a new JToolBar with the actions set.
     */
    public JToolBar createToolBar(ActionList actions) {
        JToolBar toolBar = instantiateJToolBar();
        loadActions(actions, toolBar);
        return toolBar;
    }

    /**
     * Constructs a new menu bar from an an ActionList. By convention,
     * the identifier of the main menu bar should be "main-menu-bar".
     * <p>
     * Menus and menubars are multi-dimensional, so if the List of Actions has
     * sub-Lists implemented by ActionList, then menus and submenus are created.
     * <p>
     * @param actions action list id which should be used to construct the menu.
     * Must have been registered with the ActionManager
     * @return a new JMenuBar or null if the id doesn't match a registered
     * list in the ActionManager
     * @return a new JMenu with actions
     */
    public JMenuBar createMenuBar(ActionList actions) {
        JMenuBar menuBar = instantiateJMenuBar();
        loadActions(actions, menuBar);
        return menuBar;
    }


    /**
     * Constructs a new menu from an action-list id.
     * <p>
     * Menus are multi-dimensional, so if the List of Actions has
     * sub-Lists implemented by ActionList, then submenus are created.
     * <p>
     * This method depends on the ActionManager and calls getActionList(listId),
     * which creates the actions on first use.  If you want a new
     * set of actions (say, for a local menu or popup menu instead of the
     * frame's main menu, then use {@link #createMenu(ActionList)}
     * with a list of new Actions
     * (use {@link ActionManager#createActionList(Object)
     * createActionList(Object)} to create a new set of Actions from an action
     * list id.)
     * <p>
     * @param listId action-list id which should be used to construct the menu.
     * Must have been registered with the ActionManager
     * @return a new JMenu or null if the id doesn't match a registered
     * list in the ActionManager
     */
    public JMenu createMenu(Object listId) {
        return createMenu(getActionManager().getActionList(listId));
    }


    /**
     * Constructs a new menu from an action-list.  The action list's
     * getTriggerActionId() (in XML the idref or a reuse of the aciton-list id
     * is respected, see the dtd).  If the action is not created from the XML,
     * use createMenu(name, actions) or  new ActionList(Object id, String menuName).
     * <p>
     * Menus are multi-dimensional, so if the List of Actions has
     * sub-Lists implemented by ActionList, then submenus are created.
     * <p>
     * This method depends on the ActionManager and calls getActionList(listId),
     * which creates the actions on first use.  If you want a new
     * set of actions (say, for a local menu or popup menu instead of the
     * frame's main menu, then use {@link #createMenu(ActionList)}
     * with a list of new Actions
     * (use {@link ActionManager#createActionList(Object)
     * createActionList(Object)} to create a new set of Actions from an action
     * list id.)
     * <p>
     * @param actions action list which should be used to construct the menu.
     * Must have been registered with the ActionManager
     * @return a new JMenu or null if the id doesn't match a registered
     * list in the ActionManager
     */
    public JMenu createMenu(ActionList actions) {
        if (actions == null) {
            return null;
        }
        //This extra if exists to the menu can respect the ActionList's
        //trigger action.
        Action triggerAction = getActionManager().getAction(actions.getTriggerActionId());
        return createMenu(triggerAction, actions);
    }

    /**
     * Creates a (multilevel) menu and calls loadActions for it with the list
     * of actions.
     * @param menuTriggerAction the action for the menu itself (the File Action
     * or Edit Action ...)
     * @param actions a list of actions for each menu item.  If an entry in
     * the list is an {@link ActionList}, then a submenu is created with the
     * actions in the sub List as entries.
     * @return a new JMenu with the actions set, no actions set if there
     * are no actions for the group (and empty menubar is returned)
     */
    public JMenu createMenu(Action menuTriggerAction, ActionList actions) {
        JMenu menu = instantiateJMenu(menuTriggerAction);
        loadActions(menu, actions);
        return menu;
    }

    /**
     * Same as {@link #createMenu(javax.swing.Action, ActionList)},
     * except the menu itself only has a name, not an action.  If set, the
     * action list's trigger id is ignored.
     * @param menuName The name of the menu.
     * @param actions an ActionList of actions, ActionLists,
     * or a Separator  or null for separators
     * @return a new JMenu with the actions set.
     */
    public JMenu createMenu(String menuName, ActionList actions) {
        JMenu menu = instantiateJMenu(menuName);
        loadActions(menu, actions);
        return menu;
    }

    /**
     * Constructs a new popup menu from an action-list id.
     * <p>
     * Menus are multi-dimensional, so if the List of Actions has
     * sub-Lists implemented by ActionList, then submenus are created.
     * <p>
     * This method depends on the ActionManager and calls getActionList(listId),
     * which creates the actions on first use.  If you want a new
     * set of actions (say, for a two popup menus on different views instead of,
     * then use {@link #createPopupMenu(ActionList)} twice
     * with a list of new Actions for each call
     * (use {@link ActionManager#createActionList(Object)
     * createActionList(Object)} to create a new set of Actions from an action
     * list id).
     * <p>
     * @param listId action-list id which should be used to construct the menu.
     * Must have been registered with the ActionManager
     * @return a new JPopupMenu or null if the id doesn't match a registered
     * list in the ActionManager
    */
    public JPopupMenu createPopupMenu(Object listId) {
        ActionList actions = getActionManager().getActionList(listId);
        if (actions == null) {
            return null;
        }
        return createPopupMenu(actions);
    }

    /**
     * Create a popup menu using the list of actions provided.
     * @param list an ActionList of actions, put a Separator or
     * null in the list if you want separators
     * @return a new JToolBar with the actions set.
     */
    public JPopupMenu createPopupMenu(ActionList list) {
        JPopupMenu popup = new JPopupMenu();
        loadActions(popup, list);
        return popup;
    }

    /**
     * Creates a menu item based on an action id by looking up the Action in
     * the ActionManager and calling {@link #createMenuItem(Action)}
     * @param id the id of the action as registered with the ActionManager
     * @return a JMenuItem based onthe action
     */
    private JMenuItem createMenuItem(Object id) {
        Action action = getActionManager().getAction(id);
        if (action == null) {
            return null;
        }
        JMenuItem menuItem = createMenuItem(action);
        return menuItem;
    }

    /**
     * Creates a menu item based on the action.
     * Will return a JMenuItem, JRadioButtonMenuItem or a JCheckBoxMenuItem.
     * <p>
     * Will return a JRadioButtonMenuItem  if action.getValue(ActionManager.GROUP)
     * is non-null.  If ActionManager.GROUP is not null then the type depends
     * on the ActionManager.BUTTON_TYPE property:
     * <ul>
     * <li>null                                     -> JButton
     * <li>ActionManager.BUTTON_TYPE_VALUE_TOGGLE   -> JCheckBoxMenuItem
     * <li>ActionManager.BUTTON_TYPE_VALUE_RADIO    -> JRadioMenuItem
     * <li>ActionManager.BUTTON_TYPE_VALUE_CHECKBOX -> JCheckBoxMenuItem
     * <p>
     * <p>
     * The menu items are created via the instantiateXXX methods, allowing
     * derived classes a hook into the types of button this facotry creates.
     * After the button is created, the configureButton methods is called
     * allowing derived classes a hook into setting up created buttons.
     *
     * @return a JMenuItem or subclass depending on type.
     * @todo throw excpetion for useCheckBox and group - but in DTD/Schema
     */
    public JMenuItem createMenuItem(Action action) {
        JMenuItem menuItem;
        String buttonType = ""+action.getValue(ActionManager.BUTTON_TYPE);
        if (action.getValue(ActionManager.GROUP) != null) {
            menuItem = instantiateCheckBoxMenuItem(action);
        } else if (ActionManager.BUTTON_TYPE_VALUE_TOGGLE.equals(buttonType)) {
            menuItem = instantiateCheckBoxMenuItem(action);
        } else if (ActionManager.BUTTON_TYPE_VALUE_RADIO.equals(buttonType)) {
            menuItem = instantiateRadioButtonMenuItem(action);
        } else if (ActionManager.BUTTON_TYPE_VALUE_CHECKBOX.equals(buttonType)) {
            menuItem = instantiateCheckBoxMenuItem(action);
        } else {
            menuItem = instantiateJMenuItem(action);
        }
        configureMenuItem(menuItem);
        return menuItem;
    }


    /**
     * Creates a button based on an action id by looking up the Action in
     * the ActionManager and calling {@link #createButton(Action)}
     * @param id the id of the action as registered with the ActionManager
     * @return a JButton based on the action
     */
    public AbstractButton createButton(Object id) {
        Action action = getActionManager().getAction(id);
        if (action == null) {
            return null;
        } else {
            return createButton(action);
        }
    }

    /**
     * Creates a button based on the action.
     * Will return a JButton, JRadioButton or a JCheckBox.
     * <p>
     * Will return a JRadioButton if action.geValue(ActionManager.GROUP)
     * is non-null.  If ActionManager.GROUP is not null then the type depends
     * on the ActionManager.BUTTON_TYPE property:
     * <ul>
     * <li>null                                     -> JButton
     * <li>ActionManager.BUTTON_TYPE_VALUE_TOGGLE   -> JToggleButton
     * <li>ActionManager.BUTTON_TYPE_VALUE_RADIO    -> JRadioButton
     * <li>ActionManager.BUTTON_TYPE_VALUE_CHECKBOX -> JCheckBox
     * <p>
     * The buttons are created via the instantiateXXX methods, allowing
     * derived classes a hook into the types of button this factory creates.
     * After the button is created, the configureButton methods is called
     * allowing derived classes a hook into setting up created buttons.
     *
     * @return a JButton or subclass depending on above
     * @todo throw excpetion for useCheckBox and group - but in DTD/Schema
     */
    public AbstractButton createButton(Action action)  {
        AbstractButton button = null;
        Object buttonType = action.getValue(ActionManager.BUTTON_TYPE);
        if (action.getValue(ActionManager.GROUP) != null) {
            button = instantiateToggleButton(action);
        } else if (ActionManager.BUTTON_TYPE_VALUE_TOGGLE.equals(buttonType)) {
            button = instantiateToggleButton(action);
        } else if (ActionManager.BUTTON_TYPE_VALUE_RADIO.equals(buttonType)) {
            button = instantiateRadioButton(action);
        } else if (ActionManager.BUTTON_TYPE_VALUE_CHECKBOX.equals(buttonType)) {
            button = instantiateJCheckBox(action);
        } else {
            button = instantiateJButton(action);
        }
        configureToolBarButton(button);
        return button;
    }

    /**
     * This hook method will be called after buttons are instantiated from an
     * action.  Override for custom configuration.
     *
     * @param button the button to be configured
     */
    protected void configureToolBarButton(AbstractButton button)  {
        commonConfig(button);
        // Don't show the text under the toolbar buttons.
        button.setText("");
        Action action = button.getAction();
        if (action != null) {
            if (action.getValue(Action.SHORT_DESCRIPTION) == null) {
                button.setToolTipText((String) action.getValue(Action.NAME));
            }
            if (action instanceof BasicAction &&
                !((BasicAction) action).getToolbarShowsText()) {
                button.setText("");
            }
        }
        if (toolbarButtonPreferredSize != null) {
            button.setPreferredSize(toolbarButtonPreferredSize);
            button.setMaximumSize(toolbarButtonPreferredSize);
        } else if (sSetButtonSizeFor13) {
            button.setPreferredSize(new Dimension(32, 32)); //the size of the 1.4 icons
            button.setMaximumSize(new Dimension(32, 32)); //the size of the 1.4 icons
        }
        if (sToolbarRequestFocusEnabled14) {
            button.setRequestFocusEnabled(false);
        }
    }

    /**
     * This method will be called after menu items are created.
     * Override for custom configuration.  Overrides should start with
     * super.configureMenuItem(), since this method does significant work.
     * <p>
     * Specifically, menu items are configured by
     * <ul>
     * <li>Setting the menu's action command to the action's Action.ACTION_COMMAND_KEY property
     * <li>Respecting the ActionManager.MENU_SHOWS_ICON so that toolbars can
     * show the action's icon and not the menu.
     * <li>If the menu's action is an
     * <p>
     *
     * @param menuItem the menu item to be configured
     * @see #createMenuItem(Action)
     * @todo except if action with TOGGLE does not implement ItemListener
     */
    protected void configureMenuItem(JMenuItem menuItem) {
        commonConfig(menuItem);
        Action action = menuItem.getAction();
        if (action == null) {
            return;
        }
        //respect the action's show-the-icon-in-a-menu property
        try {
            Boolean shows = (Boolean) action.getValue(ActionManager.MENU_SHOWS_ICON);
            if (shows != null && !shows.booleanValue()) {
                menuItem.setIcon(null);
            }
        } catch (ClassCastException ex) {
            //todo -warn
        }
    }

    /**
     * Common configuration for buttons and menus
     * @param buttonOrMenuItem the button to configure
     */
    private void commonConfig(AbstractButton buttonOrMenuItem) {
        Action action = buttonOrMenuItem.getAction();
        if (action == null) {
            return;
        }
        // Need to explicitly set the action command key property as
        //this is not set by default in AbstractButton.
        buttonOrMenuItem.setActionCommand((String) action.getValue(Action.ACTION_COMMAND_KEY));
        if (isShouldSyncSelectedProperty(buttonOrMenuItem, action)) {
            ActionSelectionSynchronizer syncher = new ActionSelectionSynchronizer(buttonOrMenuItem, action);
        }
        Boolean selected = (Boolean)action.getValue(ActionManager.SELECTED);
        if (selected != null) {
            buttonOrMenuItem.setSelected(selected.booleanValue());
        }
    }

    /**
     * This method determines if an action and the components created from it
     * have their selected states synchronized.  For example, a toggle button and
     * a check box menu item sharing an action should have their selected
     * states synchronized (one checked the other pushed in).  By default all
     * components created for actions are synched.
     * @param buttonOrMenuItem that will or will not be synced
     * @param action the action in question
     */
    protected boolean isShouldSyncSelectedProperty(AbstractButton buttonOrMenuItem, Action action) {
        return true;
    }

    /**
     * When a JToolBar needs to be created by the factory, this
     * method is called.  Available to allow derived classes to create their
     * own specialized toolbars.
     * @return new JToolBar();
     */
    protected JToolBar instantiateJToolBar() {
        JToolBar toolBar = new JToolBar();
        return toolBar;
    }

    /**
     * When a JMenuBar needs to be created by the factory, this
     * method is called.  Available to allow derived classes to create their
     * own specialized menu bars.
     * @return new JMenuBar();
     */
    protected JMenuBar instantiateJMenuBar() {
        JMenuBar menubar = new JMenuBar();
        return menubar;
    }

    /**
     * When a JMenu needs to be created by the factory, this
     * method is called.  Available to allow derived classes to create their
     * own specialized menus.
     * @param menuAction the menu's action that gets triggered when the
     * menu pops up, can be null
     * @return new JMenu(itemAction);
     */
    protected JMenu instantiateJMenu(Action menuAction) {
        JMenu menu = new JMenu(menuAction);
        return menu;
    }

    /**
     * When a JMenu needs to be created by the factory, this
     * method is called.  Available to allow derived classes to create their
     * own specialized menus.
     * @param menuName the menu's name
     * @return new JMenu(itemAction);
     */
    protected JMenu instantiateJMenu(String menuName) {
        JMenu menu = new JMenu(menuName);
        return menu;
    }

    /**
     * When a regular JMenuItem needs to be created by the factory, this
     * method is called.  Available to allow derived classes to create their
     * own specialized menu items.
     * @return new JMenuItem(itemAction);
     */
    protected JMenuItem instantiateJMenuItem(Action itemAction)  {
        return new JMenuItem(itemAction);
    }

    /**
     * When a RadioButtonMenuItem needs to be created by the factory, this
     * method is called.  Available to allow derived classes to create their
     * own specialized menu items.
     * @return new JRadioButtonMenuItem(itemAction);
     */
    protected JRadioButtonMenuItem instantiateRadioButtonMenuItem(Action itemAction)  {
        return new JRadioButtonMenuItem(itemAction);
    }

    /**
     * When a CheckBoxMenuItem needs to be created by the factory, this
     * method is called.  Available to allow derived classes to create their
     * own specialized menu items.
     * @return new JCheckBoxMenuItem(itemAction);
     */
    protected JCheckBoxMenuItem instantiateCheckBoxMenuItem(Action itemAction)  {
        return new JCheckBoxMenuItem(itemAction);
    }

    /**
     * When a regular JButton needs to be created by the factory, this
     * method is called.  Available to allow derived classes to create their
     * own specialized toolbar buttons.
     * @return new JButton(action);
     */
    protected JButton instantiateJButton(Action action)  {
        return new JButton(action);
    }

    /**
     * When a JCheckBox toobar button needs to be created by the factory, this
     * method is called.  Available to allow derived classes to create their
     * own specialized toolbar buttons.
     * @param action the buttons's action
     * @return new JButton(action);
     */
    protected JCheckBox instantiateJCheckBox(Action action) {
        JCheckBox menu = new JCheckBox(action);
        return menu;
    }

    /**
     * When a JToggleButton needs to be created by the factory, this
     * method is called.  Available to allow derived classes to create their
     * own specialized toolbar buttons
     * @return new JToggleButton(action);
     */
    protected JToggleButton instantiateToggleButton(Action action)  {
        return new JToggleButton(action);
    }

    /**
     * When a JRadioButton needs to be created by the factory, this
     * method is called.  Available to allow derived classes to create their
     * own specialized toolbar buttons
     * @return new JRadioButton(action);
     */
    protected JRadioButton instantiateRadioButton(Action action)  {
        return new JRadioButton(action);
    }

    /**
     * When a JSeparator needs to be created by the factory, this
     * method is called.  Available to allow derived classes to create their
     * own specialized toolbar buttons
     * @return a new instance of the JToolbar's default separator
     */
    protected JSeparator instantiateSeparator() {
        return new JToolBar.Separator(null);
    }

    /**
     * When an invisible JSeaprator needs to be created by the factory, this
     * method is called.  Available to allow derived classes to create their
     * own specialized toolbar buttons
     * @return a new instance of the JToolbar's default separator, but with
     * the foreground color set to the background color.  This is used
     * byt hte JSeparatorUI
     */
    protected JSeparator instantiateInvisibleSeparator() {
        return new JToolBar.Separator(null) {
            public Color getForeground() {
                return getBackground();
            }
        };
    }

   /**
    * Loads a toolbar with a list of actions.
    * @param actions a List of actions, add in null or a Separator
    * if you want separators
    * @param toolBar the JToolBar to set the actions on
    */
   public void loadActions(ActionList actions, JToolBar toolBar) {
       if (actions == null) {
           return;
       }
       HashMap buttonGroupsByGroupID = new HashMap();
       Iterator iter = actions.iterator();
       while (iter.hasNext()) {
           Object elem = iter.next();
           if (elem == null || elem instanceof Separator) {
               if (elem instanceof Separator && !((Separator)elem).isLineVisible()) {
                   toolBar.add(instantiateSeparator());
               } else {
                   toolBar.add(instantiateInvisibleSeparator());
               }
           } else {
               Action action = (Action) elem;
               AbstractButton button = createButton(action);
               toolBar.add(button);
               if (action.getValue(ActionManager.GROUP) != null) {
                   ButtonGroup buttonGroup = (ButtonGroup)buttonGroupsByGroupID.get(action.getValue(ActionManager.GROUP));
                   if (buttonGroup == null) {
                       buttonGroup = new ButtonGroup();
                       buttonGroupsByGroupID.put(action.getValue(ActionManager.GROUP), buttonGroup);
                   }
                   buttonGroup.add(button);
               }
           }
       }
       if (sToolbarRequestFocusEnabled14) {
           toolBar.setRequestFocusEnabled(false);
       }
   }

    private void loadActions(ActionList list, JMenuBar menubar) {
       JMenu menu = null;

       Iterator iter = list.iterator();
       while (iter.hasNext()) {
           Object element = iter.next();

           if (element == null || element instanceof Separator) {
               if (menu != null) {
                   menu.addSeparator();
               }
           } else if (element instanceof ActionList) {
               menu = createMenu((ActionList) element);
               if (menu != null) {
                   menubar.add(menu);
               }
           } else {
               if (menu != null) {
                   if (element instanceof Action) {
                       menu.add(createMenuItem((Action) element));
                   } else {
                       //assume it is an action id
                       menu.add(createMenuItem(element));
                   }
               }
           }
       }
   }

   private void loadActions(JMenu menu, ActionList actions) {
       Iterator iter = actions.iterator();
       while(iter.hasNext()) {
           Object element = iter.next();
           if (element == null || element instanceof Separator) {
               menu.addSeparator();
           } else if (element instanceof ActionList) {
               JMenu newMenu = createMenu((ActionList)element);
               if (newMenu != null) {
                   menu.add(newMenu);
               }
           } else if (element instanceof Action) {
               menu.add(createMenuItem((Action)element));
           } else {
               //assume it is an action id
               menu.add(createMenuItem(element));
           }
       }
   }

   private void loadActions(JPopupMenu popup, ActionList actions) {
       HashMap buttonGroups = new HashMap();
       Iterator iter = actions.iterator();
       while(iter.hasNext()) {
           Object element = iter.next();
           if (element == null || element instanceof Separator) {
               popup.addSeparator();
           } else if (element instanceof ActionList) {
               JMenu newMenu = createMenu((ActionList)element);
               if (newMenu != null) {
                   popup.add(newMenu);
               }
           } else if (element instanceof Action) {
               JMenuItem mi = createMenuItem((Action)element);
               handleButtonGroups(mi, buttonGroups);
               popup.add(mi);
           } else {
               //assume it is an action id
               JMenuItem mi = createMenuItem(element);
               handleButtonGroups(mi, buttonGroups);
               popup.add(mi);
           }
       }
   }

   private void handleButtonGroups(AbstractButton buttonOrMenu, Map buttonGroups) {
       if (buttonOrMenu == null) {
           return;
       }
       if (buttonOrMenu.getAction() != null) {
           Object group = buttonOrMenu.getAction().getValue(ActionManager.GROUP);
           if (group != null) {
               ButtonGroup bg = (ButtonGroup) buttonGroups.get(group);
               if (bg == null) {
                   bg = new ButtonGroup();
                   buttonGroups.put(group, bg);
               }
               bg.add(buttonOrMenu);
           }
       }
   }

}

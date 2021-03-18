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

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import javax.swing.Action;
import javax.swing.Icon;
import javax.swing.JComponent;
import javax.swing.KeyStroke;

import org.xml.sax.SAXException;

import org.bushe.lang.reflect.MethodCallbackInvocationHandler;
import org.bushe.swing.SwingUtils;

/**
 * A Manager of Swing Actions
 * <p>
 * A singleton class used for managing application Actions.  Components
 * can create and register actions with the ActionManager.  Other components
 * can grab the actions and customize them by setting callbacks for action event
 * handling, enabled state control, group control, toggle control and more.
 * <p>
 * The ActionXMLReader can read an XML file that describe Actions and register
 * them with the ActionManager.
 * <p>
 * The ActionUIFactory uses the ActionManager's lists to create menus and
 * toolbars.
 * <p>
 * <em>Please see the package documentation to understand the important difference
 * between global and local actions.
 * <p>
 * <h3>Global vs. Local Actions and Action Lists (or getXXX() vs. createXXX()) </h3>
 * <p>
 * In general usage, there is one global Action instance for each registered id.  The
 * action instance is returned for each getAction() call and is shared amongst
 * every list that referes to the action in every getActionList() result.  Similarly,
 * a call to getActionList() returns the same global instance, whose Actions it refers
 * to are also global.
 * <p>
 * In same cases, this motif is insufficient.  Think about a good editor that allows
 * one to view two different files side by side.  Each side has a popup menu, but the
 * menu choices in the left popup apply to the document on the left and the menu choices
 * on the right effect the document on the right.  Depending on the type or contents of
 * the document the menus may change in enablement, selection, contents, etc.  Sharing one
 * action list would be inappropriate - adding In this case, the developer may want to
 * create two different ActionLists that have different instances of types of Action,
 * each pointing to their respective documents (perhaps as the Actions' context).  These
 * are <em>local</em> Actions and <em>local</em> Action Lists.
 * <p>
 * Action prototypes, represented by instances of the class ActionAttributes,
 * can be registered with the ActionManager.  The prototypes are used to create
 * actions.  Prototypes are always registered when using an XML file.
 * Prototypes can be registered when not using an XML file as well.  Prototypes
 * are sufficient in most cases, and necessary when a global action is
 * insuffiecient, i.e. - when a single action id can have more than one
 * instance created for it.  An example of this usage is a toolbar that belongs
 * to a specific component where the component can appear multiple times on the
 * screen at once.  Each toolbar must run in the context of its component and
 * would typically have the component's state, thus necessitating two instances.
 * <p>
 * <h3>Actions Lists, Action Id Lists, and Instance</h3>
 * <p>
 * Similarly, Action ID lists serve as prototypes for Action lists.  An Action
 * Id list contains action ids.  A global action list exists for each created
 * action id list, and is available from getActionList(Object listId).  Action
 * id lists only contain ids.  Action lists contain actual actions.  Components
 * such as toolbars and menu bars can be created by passing an action list to
 * the ActionUIFactory.
 * <p>
 * More than one instance of an action list can be created by calling
 * createActionList(id).  This may be handy if there are two menus who start
 * out the same but have their contents change, though this is behavior is
 * not managed by the ActionManager or respected by the ActionUIFactory.
 * <p>
 * Both action id lists and action lists can be multileveled.  They may contain
 * lists as members.  In the case of action id lists, these sub-lists contain
 * other action ids (or more sub lists).  Action lists contain lists of actions
 * or sublists of action (or more sub lists).
 * <p>
 * Any of the above Lists can be ActionList's.  An ActionList is a special List
 * that can describe the action (by id) of action that is associated with
 * entire list.  This allows, for example, an Action to be associated with a
 * top-level menu.  The Action is fired when menu becomes visible.
 * <p>
 * Please see package documentation for thorough usage descriptions.
 * <p>
 * Valid for JDK 1.3 and later.
 * <p>
 * @author Michael Bushe - owner.  Wrote, refactored, converged or re-wrote
 * most of it. Blame all issues on him.
 * @author Mark Davidson - Sun Swing team member - donated an ActionManager
 * included into this one (by reworking it) ex post facto.
 * @version 1.0
 */
public class ActionManager implements IconResolver {

    /** Key used in Action putValue/getValue to set the id property - must be
     * unique within ActionManager instance.  Allows two Actions to have the
     * same command key (which is OK when they are not used together)*/
    public static final String ID = "ID";
    /** Key used in putValue/getValue to set the toolbbarShowsText property */
    public static final String TOOLBAR_SHOWS_TEXT = "TOOLBAR_SHOWS_TEXT";
    /** Key used in putValue/getValue to set the toolbbarShowsText property */
    public static final String MENU_SHOWS_ICON = "MENU_SHOWS_ICON";
    /** Key used in putValue/getValue to set the type of button that's created
     * for the action.*/
    public static final String BUTTON_TYPE="BUTTON_TYPE";
    /** Value of the buttonType property - allows for the creation of toggle buttons*/
    public static final String BUTTON_TYPE_VALUE_TOGGLE = "toggle";
    /** Value of the buttonType property - allows for the creation of radio buttons*/
    public static final String BUTTON_TYPE_VALUE_RADIO = "radio";
    /** Value of the buttonType property - allows for the creation of checkbox buttons*/
    public static final String BUTTON_TYPE_VALUE_CHECKBOX = "checkbox";
    /** Key used in putValue/getValue to set the group id String of the action.*/
    public static final String GROUP = "GROUP";
    /** Key used in putValue/getValue to set the icon property - allows
     * specification and use of a larger icon than the smicon property*/
    public static final String LARGE_ICON = "LARGE_ICON";
    /** Key used in putValue/getValue to set the selected property - for toggle
     * or group buttons detemines which are selected or unselected*/
    public static final String SELECTED = "SELECTED";
    /** Key used to store the roles the action applies to.*/
    public static final String ACTION_ROLES = "ROLES";
    /** Key used in putValue/getValue to set the action class - allows
     * for the creation of a specific action type*/
    public static final String ACTION_CLASS = "ACTION_CLASS";
    /** Key for the Number property used for ordering members of action lists.*/
    public static final String WEIGHT = "weight";

    private static Class[] ARRAY_OF_ACTION_LISTENER_CLASS = new Class[]{ActionListener.class};
    private static Class[] ARRAY_OF_ACTION_EVENT_CLASS = new Class[]{ActionEvent.class};
    private static Class[] ARRAY_OF_ITEM_LISTENER_CLASS = new Class[]{ItemListener.class};
    private static Class[] ARRAY_OF_ITEM_EVENT_CLASS = new Class[]{ItemEvent.class};

    /** The set of prototypes by id.*/
    private Map s_prototypes = Collections.synchronizedMap(new HashMap());
    /** A keyed set of lists, each list is a mixed-type list of object ids
     * or sub-Lists (typically ActionLists), each id points to a registered
     * action. The ids on the map can be any object, but Strings are typical.*/
    private Map s_actionIdLists = Collections.synchronizedMap(new HashMap());
    /** The global set of actions by id.*/
    private Map s_globalActions = Collections.synchronizedMap(new HashMap());
    /** A keyed set of lists, each list is a mixed-type list of Actions
     * or Lists (typically ActionLists). The ids on the map can be any object,
     * but Strings are typical.*/
    private Map s_globalActionLists = Collections.synchronizedMap(new HashMap());

    /** A role names for the user using the manager is managing.  Actions will
     * only be available for specified roles.*/
    private List roles;

    /** Resolves icon paths to icons for custom resolution. */
    private IconResolver iconResolver;

    /** The one default instance.*/
    private static ActionManager INSTANCE;

    /** Used to hold named instances*/
    private static Map NAMED_INSTANCES;

    private static final Object INSTANCE_LOCK = new Object();

    /**
     * Cannot instantiate this directly, only because if someone is using the API
     * quickly, then they are steered to the commonly used getInstance() method.
     * <p>
     * However, since more than one ActionManager may be useful in certain situations, the constructor
     * is made protected to allow multiple ActionManagers to exist by extension.  It's a bit of a pain,
     * but it's a bigger pain to expose this publicly when it would rarely be needed.
     */
    protected ActionManager() {
    }

    /**
     * Return the instance of the ActionManager if this will be used
     * as a singleton. The instance will be created if it hasn't
     * previously been set.
     *
     * @return the ActionManager instance.
     * @see #setInstance(String, ActionManager)
     */
    public static ActionManager getInstance() {
        synchronized (INSTANCE_LOCK) {
            if (INSTANCE == null) {
                INSTANCE = new ActionManager();
                setInstance(null, INSTANCE);
            }
        }
        return INSTANCE;
    }

    /**
     * Add a named static ActionManager instance.  Typically not needed, but can be useful
     * to separate groups of actions into their own managers.
     * <p>
     * Calling this method twice with the same name silently replaces the old manager with the new one.
     * @param name the name to use in getInstance(String) to get the instance, if null it replaces
     * the default ActionManager returned via getInstance().
     * @param actionManager the ActionManager to match to the name
     */
    public static void setInstance(String name, ActionManager actionManager) {
        synchronized (INSTANCE_LOCK) {
            if (NAMED_INSTANCES == null) {
                NAMED_INSTANCES = Collections.synchronizedMap(new HashMap());
            }
            NAMED_INSTANCES.put(name, actionManager);
            if (name == null) {
                INSTANCE = actionManager;
            }
        }
    }

   /**
    * Add a named static ActionManager instance.  Typically not needed, but can be useful
    * to separate groups of actions into their own managers.
    * <p>
    * Calling this method twice with the same name silently replaces the old manager with the new one.
    * @param name the name to use in getInstance(String) to get the instance, if null it replaces
    * the default ActionManager returned via getInstance().
    */
   public static ActionManager createNamedInstance(String name) {
      ActionManager actionManager = new ActionManager();
      setInstance(name, actionManager);
      return actionManager;
   }

    /**
     * Get a named static ActionManager instance.
     * @param name the name of the instance as added by setInstance(), if null call is
     * same as getInstance();
     * @return the named ActionManager instance, null if not found.
     */
    public static ActionManager getInstance(String name) {
        synchronized (INSTANCE_LOCK) {
            if (NAMED_INSTANCES == null) {
                if (name == null) {
                    return getInstance();
                } else {
                    return null;
                }
            } else {
                return (ActionManager)NAMED_INSTANCES.get(name);
            }
        }
    }

    /**
     * Resets the ActionManager to its created state.
     */
    public void reset() {
        s_prototypes = Collections.synchronizedMap(new HashMap());
        s_actionIdLists = Collections.synchronizedMap(new HashMap());
        s_globalActions = Collections.synchronizedMap(new HashMap());
        s_globalActionLists = Collections.synchronizedMap(new HashMap());
    }

    /**
     * Set the role names (Strings) the user act as.  The Action Manager
     * will only return roles with no role declared or with at least one
     * role matching declared matching one of the the set List of roles.
     * @param roles a List of Strings
     */
    public void setRoles(List roles) {
        this.roles = roles;
    }

    /**@return an unmodifiable List of Strings of the role names the user plays.*/
    public List getRoles() {
        if (roles == null) {
            return null;
        }
        return Collections.unmodifiableList(roles);
    }

    public synchronized void register(File f) throws IOException, SAXException {
        ActionXMLReader actionXMLReader = new ActionXMLReader(this);
        actionXMLReader.loadActions(f);
    }

    /**
     * Registers the actions attributes (prototypes), action lists, and
     * action sets from an XML file.  Same as creating an ActionXMLReader,
     * loading action attributes and registering them.
     *
     * @param actionXMLURL A URL to an action XML file, must not be null.
     * @throws IOException on unhandlable conditions such as not opening the file,
     * invalid file
     * @throws SAXException if there is an XML error, such as an action with the same name is already registered,
     * etc.
     */
    public synchronized void register(URL actionXMLURL) throws IOException, SAXException {
        register(actionXMLURL, false);
    }

    /**
     * Same as registerFromURL(URL), except it allows debug output from the XML
     * reader.
     * @param actionXMLURL A URL to an action XML file, must not be null.
     * @param debug whether or not the reader outputs debug level
     */
    public synchronized void register(URL actionXMLURL, boolean debug) throws IOException, SAXException {
        ActionXMLReader actionXMLReader = new ActionXMLReader(this);
        actionXMLReader.setDebug(debug);
        //read in
        actionXMLReader.loadActions(actionXMLURL);
    }


    /**
     * Registers the actions attributes (prototypes), action lists, and
     * action sets from an XML file.  Same as creating an ActionXMLReader,
     * loading action attributes and registering them.
     *
     * @param actionXMLStream A stream to an action XML file, must not be null.
     * @throws IOException on unhandlable conditions such as not opening the file,
     * invalid file
     * @throws SAXException if there is an XML error, such as an action with the same name is already registered,
     * etc.
     */
    public synchronized void register(InputStream actionXMLStream) throws IOException, SAXException {
        register(actionXMLStream, false);
    }

    /**
     * Same as registerFromURL(URL), except it allows debug output from the XML
     * reader.
     * @param actionXMLStream A stream to an action XML file, must not be null.
     * @param debug whether or not the reader outputs debug level
     */
    public synchronized void register(InputStream actionXMLStream, boolean debug) throws IOException, SAXException {
        ActionXMLReader actionXMLReader = new ActionXMLReader(this);
        actionXMLReader.setDebug(debug);
        //read in
        actionXMLReader.loadActions(actionXMLStream);
    }

    /**
     * Registers a global action with the ActionManager.  If a prototype for the
     * id exists, it is ignored for future getAction() calls
     * (but the prototype is used for createAction() calls).
     * @param id the key for the action (usually a String)
     * @param action The action to be registered, must not be null.
     * @throws IllegalArgumentException if an action with the same name is
     * already registered, or if the action param is null.
     */
    public synchronized void registerAction(Object id, Action action) throws
        IllegalArgumentException {
        if (action == null) {
            throw new IllegalArgumentException("Null action registered to ActionManager.");
        }
        if (s_globalActions.containsKey(id)) {
            throw new IllegalArgumentException("An action by the name " +
                                               id + "already exists.");
        }
        if (s_prototypes.containsKey(id)) {
            throw new IllegalArgumentException("An prototype action by the name " +
                                               id + "already exists.");
        }
        s_globalActions.put(id, action);
    }

    /**
     * Deregisters the specified global action from the ActionManager.
     * @todo - how to update the list that hold it?
     * @param id The id of the action to be deregistered, equal to the id used
     * to register it.
     * @return true if an action with the id was removed.  If not
     * found, false is returned.
     */
    public synchronized boolean deregisterAction(Object id) {
        Object o = s_globalActions.remove(id);
        return o != null;
    }


    /**
     * Registers an action protoype with the ActionManager.  An action prototype
     * is a set of attributes that the ActionManager can use to create and
     * Action when getAction(), createAction(), or getNewAction() are called
     * (directly or for action lists).
     * @param id The id of the action to be registered, must not be null.
     * @param actionAtts the prototype data
     * @throws IllegalArgumentException if an action with the same name is
     * already registered, or if the action param is null.
     */
    public synchronized void registerActionPrototype(Object id, ActionAttributes actionAtts) throws
        IllegalArgumentException {
        if (actionAtts == null) {
            throw new IllegalArgumentException("Null action registered to ActionManager.");
        }
        if (s_prototypes.containsKey(id)) {
            throw new IllegalArgumentException("An action protoype by the name " +
                                               id + "already exists.");
        }
        if (s_globalActions.containsKey(id)) {
            throw new IllegalArgumentException("An action by the name " +
                                               id + "already exists.");
        }
        s_prototypes.put(id, actionAtts);
    }


    /**
     * Gets all the action ids available for use.  Includes all the registered
     * global actions and the ids of action prototypes that have not been
     * created globally.  Does not filters out actions based on roles.
     * @return A Set of all the actionIds.
     */
    public Set getActionIds() {
        HashSet actionIds = new HashSet();
        actionIds.addAll(s_globalActions.keySet());
        actionIds.addAll(s_prototypes.keySet());
        return actionIds;
    }

    /**
     * Gets the action ids for all registered global actions.  Does not include
     * ids of action prototypes that have not been created globally.
     * @return A Set of all the actionIds
     */
    public Set getGlobalActionIds() {
        return s_globalActions.keySet();
    }

    /**
     * Gets the action ids for all registered action prototypes.  Does not
     * include the ids of global actions registered directly (not created via
     * a prototype)
     * @return A Set of all the actionIds
     */
    public Set getPrototypeActionIds() {
        return s_prototypes.keySet();
    }

    /**
     * @param id Object an id of a registered prototype
     * @return ActionAttributes the attrbutes of that prototype
     */
    public ActionAttributes getPrototype(Object id) {
        return (ActionAttributes)s_prototypes.get(id);
    }

    /**
     * Gets the registered Action by action id.  If the action has not been
     * registered, then an Action is created from the prototype (an
     * ActionAttributes instance) matching the id, and registered as a
     * global action.
     * <p>
     * Filters out actions based on roles.  If the action doesn't define
     * a role, it is returned, if it does define one or more roles, then
     * the it is returned only if at least one of this manager's roles match
     * one of the roles defined by the action.
     * @param actionId The action id for the registered Action
     * @return The global Action instance for this id or null if the id
     * is not registered as an Action or a prototype.
     */
    public Action getAction(Object actionId) {
        Action action = (Action) s_globalActions.get(actionId);
        if (action == null) {
            action = createAction(actionId);
            if (action != null) {
                s_globalActions.put(actionId, action);
            }
        }
        if (action != null) {
            List list = (List)action.getValue(ACTION_ROLES);
            if (list != null && !containsAny(roles, list)) {
                return null;
            }
        }
        return action;
    }

    /**
     * Same as createAction(prototypeId, null).
     * @param prototypeId The action id for the registered prototype
     * @return A new Action instance for this id or null if the id
     * is not registered as a prototype.
     */
    public Action createAction(Object prototypeId) {
        return createAction(prototypeId, null);
    }

    /**
     * Creates a new instance of Action given an id of a registered prototype.
     * Does not register the action with the ActionManager.  If the prototype
     * has not been registered, then null is returned.
     * <p>
     * If the action to be created has roles defined, and at least one of the
     * ActionManager's roles don't match one of the action's role, then null is
     * returned.
     * <p>
     * Uses ActionAttributes to create the action.  For more information on
     * how to set the default action class or how actions are created, see
     * {@link ActionAttributes}
     * <p>
     * See class and package documentation for usage description.
     * @param prototypeId The action id for the registered prototype
     * @param context If the action is {@link ContextAware}, then the set of
     * name-value piars is set on it.
     * @return A new Action instance for this id or null if the id
     * is not registered as a prototype or the current roles are no appropriate
     */
    public Action createAction(Object prototypeId, Map context) {
        ActionAttributes attrs = (ActionAttributes) s_prototypes.get(prototypeId);
        if (attrs == null) {
            return null;
        }
        Action action = attrs.createAction();
        if (!passesRoleTest(action)) {
            return null;
        }
        if (action instanceof ContextAware) {
            ContextAware caa = (ContextAware) action;
            caa.setContext(context);
        }
        return action;
    }

    /**
     * @param action the action to test roles against, if null, false returned
     * @return true if the action has no roles or has roles and has one of the
     * roles specified in this ActionManager*/
    protected final boolean passesRoleTest(Action action) {
        if (action == null) {
            return false;
        }
        return passesRoleTest((List)action.getValue(ActionManager.ACTION_ROLES));
    }

    /**@return true if the actionList has no roles or has roles and has one of
     * the roles specified in this ActionManager*/
    protected final boolean passesRoleTest(ActionList actionList) {
        return passesRoleTest(actionList.getRoles());
    }

    /**@return true if the List of defined roles is null or has one of the
     * roles specified in this ActionManager*/
    private boolean passesRoleTest(List defRoles) {
        if (defRoles == null) {
            return true;
        }
        return containsAny(roles, defRoles);
    }

    /**@return true if any of the target collection contains any elements in
     *  source collection */
    public boolean containsAny(Collection src, Collection target) {
        if (src == null) {
            return false;
        }
        Iterator iter = src.iterator();
        while (iter.hasNext()) {
            if (target.contains(iter.next())) {
                return true;
            }
        }
        return false;
    }


   /**
    * An action id list is a list containing action ids.  The list can be
    * multileveled by containing other Lists (typically ActionLists containing
    * ids).  You can register a list with the ActionManager, and then later
    * get a toolbar or menu created matching the list by passing the list id
    * (typically a name) to the ActionUIFactory.
    * @param actionList a hierarchical list of actions, ActionLists, separators or
    * or null (for separators).
    * @throws IllegalArgumentException if the listhas already been registered.
    */
   public synchronized void registerActionList(ActionList actionList) throws IllegalArgumentException {
       if (s_globalActionLists.get(actionList.getId()) != null) {
           throw new IllegalArgumentException("Already registered action list named:" + actionList.getId());
       }
       s_globalActionLists.put(actionList.getId(), actionList);
   }

    /**
     * An action id list is a list containing action ids.  The list can be
     * multileveled by containing other Lists (typically ActionLists containing
     * ids).  You can register a list with the ActionManager, and then later
     * get a toolbar or menu created matching the list by passing the list id
     * (typically a name) to the ActionUIFactory.
     * @param actionIds the ids of the actions in the list, in order, use
     * "SEPARATOR" or null to add a separator.
     * @throws IllegalArgumentException if the listhas already been registered.
     */
    public synchronized void registerActionIdList(ActionList actionIds) throws
        IllegalArgumentException {
        if (s_actionIdLists.get(actionIds.getId()) != null) {
            throw new IllegalArgumentException("Already registered action id list named:" + actionIds.getId());
        }
        s_actionIdLists.put(actionIds.getId(), actionIds);
    }

    /**
     * Gets the action ids for all registered actions.
     * <p>Not filtered by role
     * @return A Set of all the ids for all registered action lists
     */
    public Set getActionListIds() {
        return s_actionIdLists.keySet();
    }

    /**
     * Get the List of ids registered to the ActionManager for the
     * given list id for which they were registered (in order).  Typically an
     * ActionList.
     * <p>
     * The list contains ids for actions, not the actions themselves.
     * <p>
     * The result may be multileveled.  It may contain other id Lists
     * (or ActionLists), which in turn contain other ids or ActionLists.
     * <p>
     * The ActionList is returned only if it has no roles defined or it has
     * roles and has at least one of the ActionManager's roles.  Each element
     * of the List is filtered according to the element's roles as well.
     *
     * @param listId The id of the list as set when registered
     * @return the List as registered via registerActionIdList for the given
     * list id.  Null if no list was registered with that id.  The list is
     * the original lists, it has ids, not Actions.  Id lists with roles
     * defined that don't match at least one role of the ActionManager are
     * not returned (null is returned)
     */
    public synchronized ActionList getActionIdList(Object listId) {
        ActionList idList = (ActionList) s_actionIdLists.get(listId);
        if (idList == null || !passesRoleTest(idList)) {
            return null;
        }
        return idList;
    }

    /**
     * Deregisters the specified action list from the ActionManager.
     * @param listId The id of the action id list to be deregistered, equal to
     * the key used when it was registered.
     * @return true if an action list with the actionId was removed.  If not
     * found, false is returned.
     */
    public synchronized boolean deregisterIdList(Object listId) {
        Object o = s_actionIdLists.remove(listId);
        s_globalActionLists.remove(listId);
        return o != null;
    }


    /**
     * Gets or creates the Global ActionList with their Global Actions.
     *
     * Use this method instead of {@link #createActionList(Object)} if
     * the action list will only ever be created once (like a static
     * toolbar).  The list of actions is cached the first time it is created.
     * <p>
     * The Actions in the list are the same action instances as the global
     * actions available via getAction(id).
     * <p>
     * The ActionList is returned only if it has no roles defined or it has
     * roles and has at least one of the ActionManager's roles.  Each element
     * of the List is filtered according to the element's roles as well.
     *
     * @param listId The id of the list as set when registered
     * @return the List of Actions for this list id.  Created on first call
     * for the id and cached thereafter.  Null if no list was registered with
     * that id.  The list made via a call to createActionList, so it
     * contains Actions (and sub-Lists and nulls), rather than ids.
     */
    public synchronized ActionList getActionList(Object listId) {
        ActionList result = (ActionList) s_globalActionLists.get(listId);
        if (result == null) {
            result = createActionList(listId, true, null);
            if (result == null) {
                return null;
            } else {
                s_globalActionLists.put(listId, result);
                return result;
            }
        } else {
            if (!passesRoleTest(result)) {
                return null;
            } else {
                return result;
            }
        }
    }

    /**
     * Creates a new Local ActionList with Global Actions specified by the
     * action list id.
     * <p>
     * Same as createActionList(actionListId, true, null)}
     * @param actionListId the id of the list as registered with registerActionIdList
     * @return an ActionList of Actions, Lists and nulls, sublists can have Actions,
     * Lists, ActionLists, and nulls.  All Actions are references to the global
     * Actions available via getAction(id).
     */
    public synchronized ActionList createActionList(Object actionListId) {
        return createActionList(actionListId, true, null);
    }

    /**
     * Creates a new Local ActionList with a context and Global Actions as
     * specified by the action list id.
     * <p>
     * Same as createActionList(Object, true, Object)}
     * @param actionListId the id of the list as registered with registerActionIdList
     * @param context If the action is {@link ContextAware}, then the set of
     * name-value piars is set on it.
     * @return an ActionList of Actions, Lists and nulls, sublists can have Actions,
     * Lists, ActionLists, and nulls.  All Actions are references to the global
     * Actions available via getAction(id).
     */
    public synchronized ActionList createActionList(Object actionListId, Map context) {
        return createActionList(actionListId, true, context);
    }

    /**
     * Creates a new Local ActionList with a context and either new
     * Local Actions or Global Actions.
     * <p>
     * Same as createActionList(Object, boolean, null)}
     * @param actionListId the id of the list as registered with registerActionIdList
     * @param useGlobalActions true if the list should be filled with references
     * to the global actions (getAction()), false is new action lists should
     * be created from the prototype available via createAction().
     * @return an ActionList of Actions, Lists and nulls, sublists can have Actions,
     * Lists, ActionLists, and nulls.  All Actions are references to the global
     * Actions available via getAction(id).
     */
    public synchronized ActionList createActionList(Object actionListId, boolean useGlobalActions) {
        return createActionList(actionListId, useGlobalActions, null);
    }

    /**
     * Turns Action Id Lists into a List of Actions.
     * <p>
     * Creates a new List of Actions from the id of the action
     * list registered with the Action Manager.
     * <p>
     * The instances of the Actions in the newly created.  They are not the
     * same instances in other list and must be managed
     * <p>
     * The List is multileveled - in addition to Actions, it may contain Lists
     * that contain Actions.  It may also contain null, which should be
     * interpreted as a separator.  Sublists may also contain Lists and null.
     * If the Lists or sublists are ActionLists, then the result uses
     * ActionLists for those lists as well.
     * <p>
     * The ActionList is returned only if it has no roles defined or it has
     * roles and has at least one of the ActionManager's roles.  Each element
     * of the List is filtered according to the element's roles as well.
     *
     * @param actionListId the id of the list as registered with registerActionIdList
     * @param useGlobalActions true if the list should be filled with references
     * to the global actions (getAction()), false is new action lists should
     * be created from the prototype available via createAction().
     * @param context If the action is {@link ContextAware}, then the set of
     * name-value piars is set on it.
     * @return An ActionList of Actions, Lists and nulls, sublists can have
     * Actions, Lists, ActionLists, and nulls.
     * @see #getActionList(Object) for an alternative
     */
    public synchronized ActionList createActionList(Object actionListId,
            boolean useGlobalActions, Map context) {
        ActionList actionIdList = getActionIdList(actionListId);
        if (actionIdList == null) {
            return null;
        }
        ActionList actionList = new ActionList(actionIdList.getId(),
                                               actionIdList.getTriggerActionId());
        actionList.setWeight(actionIdList.getWeight());
        fillListWithActions(actionList, actionIdList, useGlobalActions, context);
        return actionList;
    }

    /**
     * Rercusive method for turning action id lists into Action Lists
     * @param toFill the ActionList to fill with ACtions
     * @param actionIds the list of action ids to use in filling it
     * @param useGlobalActions true if the list should be filled with references
     * @param context If the action is {@link ContextAware}, then the set of
     * name-value pairs is set on it.
     * implement ContextAware, only if useGlobalActions is false.
     * to the global actions (getAction()), false is new action lists should
     * be created from the prototype available via createAction().
     */
    private synchronized void fillListWithActions(List toFill, List actionIds,
                                                  boolean useGlobalActions,
                                                  Map context) {
        for (int i = 0; i < actionIds.size(); i++) {
            Object elem = actionIds.get(i);
            Number weight = null;
            if (elem == null) {
                toFill.add(null);
            } else if (elem instanceof Separator) {
                weight = ((Separator)elem).getWeight();
                insertWithRespectToWeights(weight, toFill, elem);
            } else if (elem instanceof List) {
                //use recursion for sublist
                List subIds = (List) elem;
                List subList = null;
                if (subIds instanceof ActionList) {
                    ActionList subAL = (ActionList) subIds;
                    if (!passesRoleTest((ActionList)subAL)) {
                        continue;
                    }
                    subList = new ActionList(subAL.getId(),
                              subAL.getTriggerActionId());
                    weight = subAL.getWeight();
                    ((ActionList)subList).setWeight(weight);
                } else {
                    subList = new ArrayList(subIds.size());
                }
                insertWithRespectToWeights(weight, toFill, subList);
                fillListWithActions(subList, subIds, useGlobalActions, context);
            } else {
                //get the action for the id and add it to the list
                Action action = null;
                if (useGlobalActions) {
                    action = getAction(actionIds.get(i));
                } else {
                    action = createAction(actionIds.get(i), context);
                }
                if (action != null) {
                    Object weightVal = action.getValue(ActionManager.WEIGHT);
                    if (weightVal != null) {
                        weight = Double.valueOf(weightVal.toString());
                    }
                    insertWithRespectToWeights(weight, toFill, action);
                }
            }
        }
    }

    /**
     * @param weight Number:, not null
     * @param toFill List to find insertion point
     */
    private void insertWithRespectToWeights(Number weight, List toFill, Object toInsert) throws NumberFormatException {
        for (int i = 0; i < toFill.size(); i++) {
            Number childWeight = null;
            Object child = toFill.get(i);
            if (child instanceof ActionList) {
                childWeight = ((ActionList)child).getWeight();
            } else if (child instanceof Action) {
                Object val = ((Action)child).getValue(ActionManager.WEIGHT);
                if (val != null) {
                    childWeight = Double.valueOf(val + "");
                }
            } else if (child instanceof Separator) {
                Object val = ((Separator)child).getWeight();
                if (val != null) {
                    childWeight = Double.valueOf(val + "");
                }
            }
            if (childWeight == null) {
                //Treat child weight as 0
                if (weight != null && weight.doubleValue() < 0.0d) {
                    //This is the weight == -1
                    toFill.add(i, toInsert);
                    return;
                }
            } else {
                //Treat weight as 0
                if (weight == null) {
                    if (childWeight.doubleValue() > 0.0d) {
                        toFill.add(i, toInsert);
                        return;
                    }
                } else if (weight.doubleValue() < childWeight.doubleValue()) {
                    toFill.add(i, toInsert);
                    return;
                }
            }
        }
        toFill.add(toInsert);
    }

    /**
     * Registers a callback method called when the Global Action corresponding
     * to the action id gets invoked.
     * <p>
     * Handy method to specify a method to call on an object when and action
     * event is fired without having to implement ActionListener (while still
     * getting the ActionEvent if necessary).  The typical use of this method
     * is a controller that listens to multiple actionas and wants a method
     * to be called for each (while avoiding large "if" dispatching
     * structures in an actionPerformed()).
     * <p>
     * If the method of the handler class identified by the "method" parameter
     * takes an ActionEvent as a parameter, then the
     * ActionEvent for the action is passed to the method, otherwise, it must
     * be a no-arg method.
     * <p>
     * @todo Overload to use the javax.beans.EventHandler-type string to handle
     * parameters and even implementation, i.e. - ..., "source.name", "text"
     * calls setText on the hanlder with the event source's getName() result.
     * <p>
     * If handler is a Class, then the action is called on the static method of
     * that class named by method.  If the handler is not a class, then the
     * method can be static or non-static (instance).
     * <p>
     * If the method is overloaded (static or instance) then the signature that
     * defines the ActionEvent takes precedence and will be called, the others
     * will be ignored.
     * <p>
     * Normal rules for Java Security, Classloading, and Reflection apply.
     * <p>
     * @param actionId value of the action id
     * @param callback the object which will perform the action, if a Class,
     * then method must be a static method of the class
     * @param method the name of the method on the handler which will be called.
     * @throws IllegalArgumentException if any of the parameters except actionId
     * are null (an actionId of null - the null action - is conceivable), or if
     * getAction(actionId) returns null, or if the Action is not a
     * ActionListenerDelegator
     * @throws NoSuchMethodException if the method is not found in the class, or
     * if the method is non-static, but the callback is an instance of Class.
     */
    public void registerActionCallback(Object actionId, Object callback, String method) throws NoSuchMethodException {
        Action action = getAction(actionId);
        if (action == null) {
            throw new IllegalArgumentException("No action found for action id " + actionId
                                               + ", registering method " + method + ", callback "
                                               + callback);
        }
       if (action instanceof Actionable) {
          registerActionCallback((Actionable)action, callback, method) ;
       } else {
          throw new NoSuchMethodException("Action does not implement Actionable, no addActionListener(0 to use: action " + action
                                             + ", registering method " + method + ", callback "
                                             + callback);
       }
    }

    /**
     * Same as registerActionCallback(Object actionId, Object callback,
     * String method), except it takes the Action instead of the action id
     * (for use with non-global actions).
     *
     * @param action the action to which a dynamic listener will be added
     * @param callback the object which will perform the action, if a Class,
     * then method must be a static method of the class
     * @param method the name of the method on the handler which will be called.
     * @throws NoSuchMethodException if the method is not found, or not static
     * if it should be (if callback is a Class)
     * @throws IllegalArgumentException if any of the parameters are null,
     * if the Action is not a ActionListenerDelegator
     * @todo unregister
     */
    public void registerActionCallback(Actionable action, Object callback, String method) throws NoSuchMethodException {
        ActionListener listenerProxy = (ActionListener) registerCallback(action, callback, method,
            ARRAY_OF_ACTION_EVENT_CLASS, ActionListener.class, ARRAY_OF_ACTION_LISTENER_CLASS);
        action.addActionListener(listenerProxy);
    }

    /**
     * Same as registerCallback(Object actionId, Object callback, String method)},
     * except adds type saftey for static methods.
     * @param actionId the id of the global action
     * @param callbackClass the Class whose's static method willbe called
     * @param staticMethod the name of the class's staticMethod
     */
    public void registerStaticActionCallback(Object actionId, Class callbackClass, String staticMethod) throws NoSuchMethodException{
        registerActionCallback(actionId, callbackClass, staticMethod);
    }

    /**
     * Handles all the callback registrations
     * @param action the id of the action
     * @param callback the proxy to callback, can be null for static methods
     * @param method the method to call, not null
     * @param action the action to work on.
     * @throws IllegalArgumentException if action, handler or method is null
     * @throws SecurityException
     * @throws NoSuchMethodException
     */
    private Object registerCallback(Actionable action, Object callback, String method, Class[] args,
                                    Class implementingInterface, Class[] arrayOfInterfaces)
        throws IllegalArgumentException, SecurityException, NoSuchMethodException {

        if (action == null) {
            throw new IllegalArgumentException("Action cannot be null.");
        }

        try {
            return MethodCallbackInvocationHandler.createMethodCallbackProxy(callback, method, args,
                implementingInterface, arrayOfInterfaces);
        } catch (NoSuchMethodException ex) {
            String err = "Callback Method named " + method;
            if (callback instanceof Class) {
                err = err + " either not found or is not static ";
            } else {
                err = err + " is not found ";
            }
            err = err + " not found in callback " + callback
                  + ", registering for action =" + action;
            throw new NoSuchMethodException(err);
        }
    }

    /**
     * Calls setEnabled(boolean) on all the created Global Actions .
     * Use this actions to enabled or disable all Global Actions.
     * @param enable true to enable the action, false to disable it.
     */
    public void setEnabledAll(boolean enable) {
        if (s_globalActions == null || s_globalActions.isEmpty()) {
            return;
        }
        Iterator iter = s_globalActions.keySet().iterator();
        while (iter.hasNext()) {
            setEnabled(iter.next(), enable);
        }
    }

    /**
     * Calls setEnabled() on the created Global Action corresponding to the
     * given id.
     * <p>
     * The action must have already been created.  Use this method to
     * enable or disable a Global Action only if it has been created.
     * @param enable true to enable the action, false to disable it.
     */
    public void setEnabled(Object id, boolean enable) {
        if (s_globalActions == null || s_globalActions.isEmpty()) {
            return;
        }
        Action action = (Action) s_globalActions.get(id);
        if (action == null) {
            return;
        }
        action.setEnabled(enable);
    }

    /**
     * Calls updateEnabledState() on all the created Global Actions that implement
     * {@link EnabledUpdater}.  Use this actions to refresh the enabled state of
     * all Global Actions, if your global actions know how to check themselves.
     */
    public void updateEnabledAll() {
        if (s_globalActions == null || s_globalActions.isEmpty()) {
            return;
        }
        Iterator iter = s_globalActions.keySet().iterator();
        while (iter.hasNext()) {
            updateEnabled(iter.next());
        }
    }

    /**
     * Sets a context value for all the created actions if the
     * Action is ContextAware (BasicAction is ContextAware).
     * Depending on the action, this will force an updateEnabledState()
     * (it will by default for any BasicAction).
     * @see ContextAware
     * @see BasicAction
     * @param key context key for actions.
     * @param contextValue the context value for actions.
     */
    public void putContextValueForAll(Object key, Object contextValue) {
        if (s_globalActions == null || s_globalActions.isEmpty()) {
            return;
        }
        Iterator iter = s_globalActions.keySet().iterator();
        while (iter.hasNext()) {
            Object action = s_globalActions.get(iter.next());
            if (action instanceof ContextAware) {
                ContextAware contextAwareAction = (ContextAware)action;
                contextAwareAction.putContextValue(key, contextValue);
            }
        }
    }

    /**
     * Calls updateEnabledState() on the Global Actions corresponding to the given id.
     * The action must have already been created.  Only called if the action
     * implements {@link EnabledUpdater}.  Use this method to refresh the enabled
     * state of a Global Action, if your Global Actions knows how to
     * check itself in such a mannner.
     */
    public void updateEnabled(Object id) {
        if (s_globalActions == null || s_globalActions.isEmpty()) {
            return;
        }
        Action action = (Action) s_globalActions.get(id);
        if (action instanceof EnabledUpdater) {
            ((EnabledUpdater) action).updateEnabled();
        }
    }

   /**
    * Utility method to put a mapping in a component's input and action maps
    * for an action. Assume
    * @param comp JComponent any component that want to
    * @param action Action
    * @param condition JComponent constant one of
    * JComponent.WHEN_IN_FOCUSED_WINDOW
    * JComponent.WHEN_FOCUSED
    * JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT
    */
   public static void mapKeystrokeForAction(JComponent comp, Action action, int condition) {
       KeyStroke keystroke = (KeyStroke)action.getValue(Action.ACCELERATOR_KEY);
       if (keystroke == null) {
           return;
       }
       comp.getActionMap().put(action.getValue(Action.ACTION_COMMAND_KEY), action);
       comp.getInputMap(condition).put(keystroke, action.getValue(Action.ACTION_COMMAND_KEY));
   }

   /**
    * Same as mapKeystrokeForAction(JComponent, Action, JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT}
    * @param comp JComponent any component that want to
    * @param action Action to map the keystroke accelrator for
    */
   public static void mapKeystrokeForAction(JComponent comp, Action action) {
       mapKeystrokeForAction(comp, action, JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);
   }


    /**
     * @return the icon resolver set on this class, will be "this" if not explictly set
     */
    public IconResolver getIconResolver() {
        return iconResolver;
    }

    /**
     * @param iconResolver a resolver that can take an imageURL string and return an Icon
     */
    public void setIconResolver(IconResolver iconResolver) {
        this.iconResolver = iconResolver;
    }

    public Icon resolveIcon(String imageURL) {
        if (iconResolver != null) {
            return iconResolver.resolveIcon(imageURL);
        } else {
            return SwingUtils.getIcon(imageURL);
        }
    }

}

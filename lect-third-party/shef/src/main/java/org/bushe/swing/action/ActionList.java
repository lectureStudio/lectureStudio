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
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.awt.event.ActionListener;
import javax.swing.Action;

/**
 * Acts as a list or tree for actions or action ids.
 * <p>
 * Represents a List of Actions, ActionList elements or Separators. The null element may represent
 * a separator.  Sometimes the list is used as a lazy initialization structure internal to the Action
 * framework, however, public use will find typically show Action and
 * ActionLists only.
 * <p>
 * The list itself describes it's id, and also the id of the action
 * that triggers it.
 * <p>
 * The list has an associated set of roles that the ActionManager respects.
 */
public class ActionList implements List {

    private Object id;
    private Object triggerActionId;
    private Number weight;
    //list of actions
    private List list = new ArrayList();
    //The roles required for the user to see the list
    private List roles;


    /**
     * Supply the id for this list.
     * @param id the id of the list
     */
    public ActionList(Object id) {
        this.id = id;
    }

    /**
     * Supply the id for this list and the id for the action that
     * triggers this list (in XML the actionList's idref attribute).
     * @param id the id of the list
     * @param triggerActionId the id of the action that
     * is fired when a menu is shown, can be null.
     */
    public ActionList(Object id, Object triggerActionId) {
        this(id);
        this.triggerActionId = triggerActionId;
    }

    /**
     * Retuns the action-list id that this class represents.
     */
    public Object getId() {
        return id;
    }

    /**
     * @return the id for the Action that triggers this action list, in
     * the XML the actionList's idref or id
     */
    public Object getTriggerActionId() {
        return triggerActionId;
    }

    /**@return an unmodifiable list of Strings, each one a role name for the list*/
    public List getRoles() {
        if (roles == null) {
            return null;
        }
        return Collections.unmodifiableList(roles);
    }

    /**
     * @return the weight of this list relative to siblings in another list
     */
    public Number getWeight() {
        return weight;
    }

    /**
     * @param weight the weight of this list relative to siblings in another list
     */
    public void setWeight(Number weight) {
        this.weight = weight;
    }

    /**
     * Set the List of roles names (Strings) for the ActionList.  The user will
     * not see the list unless they play in at least one of the roles of the
     * ActionList.
     */
    public void setRoles(List roles) {
        this.roles = roles;
    }

   /**
     * Finds the action matching the id in the tree-like list.
     * @param id the id of the action, should be unique within
     * the tree, otherwise, first one found is returned (breadth first)
     * @return an Action whose ActionManager.ID property is .equal() to the
     * provided id.  Null if not found.
     */
     public Action getActionById(Object id) {
        return findActionInList(id, list);
    }

    /**
     * Calls addActionListener(delegate) on all action in the action
     * list (even on actions deep in a hierarchy)
     * @todo needs test
     */
    public void addActionListenerToAll(ActionListener delegate) {
        BasicAction action = null;
        Iterator iter = iterator();
        while (iter.hasNext()) {
            Object item = iter.next();
            if (item == null) {
                continue;
            }
            if (item instanceof BasicAction) {
                action = (BasicAction) item;
                action.addActionListener(delegate);
            }
        }
        //Do submenus too
        iter = iterator();
        while (iter.hasNext()) {
            Object subList = iter.next();
            if (subList == null) {
                continue;
            }
            if (subList instanceof ActionList) {
                ((ActionList)subList).addActionListenerToAll(delegate);
            }
        }
    }

    /**
     * Finds an action in the ActionList (and the ActionLists in the
     * ActionList, recursively, depth first.
     * @param id the id of the action
     * @param actionList the action list to look in (for recursion)
     * @return the action matching the id or null if not found.
     */
    private Action findActionInList(Object id, List actionList) {
        Iterator iter = actionList.iterator();
        while (iter.hasNext()) {
            Object item = iter.next();
            if (item == null) {
                continue;
            }
            if (item instanceof Action) {
                Action action = (Action) item;
                if (id.equals(action.getValue(ActionManager.ID))) {
                    return action;
                }
            }
        }
        //twice because depth first is probably preferable
        //since submenus are used less often
        iter = actionList.iterator();
        while (iter.hasNext()) {
            Object subList = iter.next();
            if (subList == null) {
                continue;
            }
            if (subList instanceof ActionList) {
                Action result = findActionInList(id, (ActionList) subList);
                if (result != null) {
                    return result;
                }
            }
        }
        return null;
    }

    /**
     * Sets the context for all the actions in an ActionList if the
     * Action is ContextAware (BasicAction is ContextAware).
     * Depending on the action, this will force an updateEnabledState()
     * (it will by default in BasicAction)
     * @see ContextAware
     * @see BasicAction
     * @param context Map a map of key/value pairs for actions.
     */
    public void setContextForAll(Map context) {
        for (Iterator iterator = this.iterator(); iterator.hasNext();) {
            Object action = iterator.next();
            if (action instanceof ContextAware) {
                ContextAware contextAwareAction = (ContextAware)action;
                contextAwareAction.setContext(context);
            }
        }
    }

    /**
     * Sets a context value for all the actions in an ActionList if the Action is ContextAware
     * (BasicAction is ContextAware).
     * <p>
     * Context values are separate from putValue()/getValue() values from javax.swing.Action.
     * <p>
     * In action XML, <context name="foo" value="bar"> turns into a context value.
     * <p>
     * Depending on the action, this will force an updateEnabledState() (it will by default for any BasicAction).
     * @see ContextAware
     * @see BasicAction
     * @param key key for the context value.
     * @todo Action XML have name-value-par for action-list, which calls this method (? When if merging?)
     */
    public void putContextValueForAll(Object key, Object contextValue) {
        for (Iterator iterator = this.iterator(); iterator.hasNext();) {
            Object action = iterator.next();
            if (action instanceof ContextAware) {
                ContextAware contextAwareAction = (ContextAware)action;
                contextAwareAction.putContextValue(key, contextValue);
            }
        }
    }

    /**
     * Asks all the BasicActions in the list to update their enabled states.
     * <p>
     * If setContextForAll() is used, this is usually not necessary, since
     * contextChanged() in BasicAction calls updateEnabledState by default.
     * @see BasicAction
     */
    public void updateEnabledForAll() {
        for (Iterator iterator = this.iterator(); iterator.hasNext();) {
            Object action = iterator.next();
            if (action instanceof EnabledUpdater) {
                EnabledUpdater updateEnabledAction = (EnabledUpdater)action;
                updateEnabledAction.updateEnabled();
            }
        }
    }

    /**
     * Sets the enabled state for all the Actions in the list.
     */
    public void setEnabledForAll(boolean enabled) {
        for (Iterator iterator = this.iterator(); iterator.hasNext();) {
            Object action = iterator.next();
            if (action instanceof Action) {
                ((Action)action).setEnabled(enabled);
            }
        }
    }

    //List implementation
    public int size() {
        return list.size();
    }
    public boolean isEmpty() {
        return list.isEmpty();
    }
    public boolean contains(Object o) {
        return list.contains(o);
    }
    public Iterator iterator() {
        return list.iterator();
    }
    public Object[] toArray() {
        return list.toArray();
    }
    public Object[] toArray(Object[] a) {
        return list.toArray(a);
    }
    public boolean add(Object o) {
        return list.add(o);
    }
    public boolean remove(Object o) {
        return list.remove(o);
    }
    public boolean containsAll(Collection c) {
        return list.containsAll(c);
    }
    public boolean addAll(Collection c) {
        return list.addAll(c);
    }
    public boolean addAll(int index, Collection c) {
        return list.addAll(index, c);
    }
    public boolean removeAll(Collection c) {
        return list.removeAll(c);
    }
    public boolean retainAll(Collection c) {
        return list.retainAll(c);
    }
    public void clear() {
        list.clear();
    }
    public Object get(int index) {
        return list.get(index);
    }
    public Object set(int index, Object element) {
        return list.set(index, element);
    }
    public void add(int index, Object element) {
        list.add(index, element);
    }
    public Object remove(int index) {
        return list.remove(index);
    }
    public int indexOf(Object o) {
        return list.indexOf(o);
    }
    public int lastIndexOf(Object o) {
        return list.lastIndexOf(o);
    }
    public ListIterator listIterator() {
        return list.listIterator();
    }
    public ListIterator listIterator(int index) {
        return list.listIterator(index);
    }
    public List subList(int fromIndex, int toIndex) {
        return list.subList(fromIndex, toIndex);
    }
}


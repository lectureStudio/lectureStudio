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

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import javax.swing.Action;

/**
 * A class that has a map of key-value pairs for an Action. It can create an Action from the values, this acting as an
 * Action prototype for the Action Manager.
 * <p/>
 * Action's could have served as their own prototypes, but getKeys() is not in the interface.
 */
public class ActionAttributes {
   /** A map of the action's attributes. */
   private Map attMap = new HashMap(10);

   private static Class defaultActionClass;

   /**
    * @return the default action class, if null, BasicAction is created by default.
    */
   public static Class getDefaultActionClass() {
      return defaultActionClass;
   }

   /**
    * Sets the default action class to create for an action when the
    * ActionAttirbutes's ActionManager.ACITON_CLASS value if not set.
    * <p>
    * If null or never set, a BasicAction is created by default.
    * @param defaultActionClass the action class to create by default.
    */
   public static void setDefaultActionClass(Class defaultActionClass) {
      ActionAttributes.defaultActionClass = defaultActionClass;
   }

   /** Default constructor.  Creates an empty set of attributes. */
   public ActionAttributes() {
   }

   /**
    * Copy constructor
    *
    * @param attrs the attributes to copy
    */
   public ActionAttributes(ActionAttributes attrs) {
      Set keys = attrs.getKeys();
      Iterator iter = keys.iterator();
      while (iter.hasNext()) {
         Object key = iter.next();
         this.putValue(key, attrs.getValue((String) key));
      }
   }

   /**
    * @param key typically one of ActionManager.._INDEX
    *
    * @return the Attribute value for the given key
    */
   public Object getValue(String key) {
      return attMap.get(key);
   }

   /** @return the set of keys of this set of ActionAttribute values. */
   public Set getKeys() {
      return attMap.keySet();
   }

   /**
    * Sets a key value pair
    *
    * @param key the key
    * @param value the value of the key
    */
   public void putValue(Object key, Object value) {
      attMap.put(key, value);
   }

   /**
    * Creates an action from the attributes.  Unless otherwise specified, a BasicAction is created and configured.  If
    * the attributes specify another class via the ActionManager.ACTION_CLASS property, then that class is created.  If
    * ActionManager.ACTION_CLASS property is not set, and ActionManager.TOGGLE is set to "true", then a
    * StateChangeAction is created.
    */
   public Action createAction() {
      String specificClass = (String) attMap.get(ActionManager.ACTION_CLASS);
      Action action = null;
      if (specificClass != null) {
         try {
            Class actionClass = Class.forName(specificClass);
            action = (Action) actionClass.getConstructor().newInstance();
         } catch (Exception ex) {
            return null;
         }
      }
      if (action == null) {
         action = instantiateDefaultAction();
      }
      configureAction(action);
      return action;
   }

   /**
    * Allows implementers to return actions other than BasicAction by default whne
    * the ACTION_CLASS is not specified in an action.
    * <p/>
    * If the defaultActionClass property is set, then that class is used as the default
    * instead of BasicAction, otherwise a new BasicAction is returned.
    *
    * @return a new instance of hte defailftActionClass, or a BasicAction if not set.
    */
   protected Action instantiateDefaultAction() {
      if (defaultActionClass != null) {
         try {
            return (Action) defaultActionClass.getConstructor().newInstance();
         } catch (Exception e) {
            throw new RuntimeException("Could not create action.", e);
         }
      }
      return new BasicAction();
   }

   /**
    * Configures an action from the attributes.  Copies all non-null attributes to the action, except
    * enabled, which it sets on the Action
    *
    * @param action the action to configure
    */
   public void configureAction(Action action) {
      Set keys = attMap.keySet();
      Iterator iter = keys.iterator();
      while (iter.hasNext()) {
         String key = (String) iter.next();
         Object value = attMap.get(key);
         if (ActionXMLReader.ATTRIBUTE_ENABLED.equals(value)) {
            action.setEnabled(Boolean.valueOf(value.toString()).booleanValue());
         } else {
            action.putValue((String) key, value);
         }
      }
   }

   /** @return a string of our the internal map */
   public String toString() {
      return attMap.toString();
   }
}

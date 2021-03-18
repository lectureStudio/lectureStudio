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

import java.util.Map;

/**
 * An interface that allows a context objects to be set on another object.
 * @author Michael Bushe
 * @version 1.0
 */
public interface ContextAware {
    /**
     * Sets the context on the action.
     * @param context Map of name-value pairs
     */
    public void setContext(Map context);

    /**
     * Get the context for the action
     * @return Map a set of name-value pairs
     */
    public Map getContext();
    /**
     * Clear all name-value pairs from the action's context
     */
    public void clearContext();
    /**
     * Add a name-value pair to the action's context.
     * @param key Object
     * @param contextValue Object
     */
    public void putContextValue(Object key, Object contextValue);
    /**
     * Get the value for a name in the action's context.
     * @param key Object
     * @return Object
     */
    public Object getContextValue(Object key);
}

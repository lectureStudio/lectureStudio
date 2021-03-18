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

import java.awt.event.ActionListener;

/**
 * Interface to delegation of action listeners
 * @author Michael Bushe
 * @version 1.0
 */
public interface Actionable {
    /**
     * Adds the action listener callback delegate.
     * @param actionListenerDelgate the callback object
     */
    public void addActionListener(ActionListener actionListenerDelgate);
    /**
     * Removes the action listener callback delegate.
     * @param actionListenerDelgate the callback object
     */
    public void removeActionListener(ActionListener actionListenerDelgate);
}

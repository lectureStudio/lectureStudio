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

/**
 * An interface that actions can implement to have themselves determine and set
 * their own enablement state.
 * @see org.bushe.swing.action.DelegatesEnabled
 * @author Michael Bushe
 */
public interface EnabledUpdater extends ShouldBeEnabledDelegate {
    /**
     * Called to force an action to enable or disable itself.
     * <p>
     * If the action can figure it out (either directly or through a delegate)
     * then it should call setEnabled(false or true) on itself appropriately.
     * <p>
     * A typical implementation would simply call setEnabled(shouldBeEnabled())
     * @return whether setEnabled was called with false or true
     */
    public boolean updateEnabled();

}

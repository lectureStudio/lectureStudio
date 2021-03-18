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

import java.util.EventListener;
import javax.swing.Action;

/**
 * An interface that allows actions to defer the enabled state computation to
 * another class.
 * <p>
 * An application controller may implement this method and set itself on the action
 * to enable and disable the action whenever updateEnabledState is called on the action.
 * @see org.bushe.swing.action.DelegatesEnabled
 * @author Michael Bushe
 */
public interface ShouldBeEnabledDelegate extends EventListener {

    /**
     * Called by the action to ask the action whether it should be enabled or
     * disabled given the current "state of affairs."
     * <p>
     * Makes no change to the action.
     * @return whether setEnabled should be called with false or true
     */
    public boolean shouldBeEnabled(Action action);
}

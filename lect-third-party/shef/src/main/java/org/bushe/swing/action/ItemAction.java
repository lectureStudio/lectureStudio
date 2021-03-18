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

import java.awt.event.ItemListener;
import javax.swing.Action;

/**
 * Interface for Actions that can deal with ItemEvents, such as
 * toggle and group buttons.
 * <p>
 * Typically this interface is not used.  One would typically add an
 * actionListener to toggle-like actions, since the action will get fired
 * when the buttons toggle selection.  This interface is to implement the
 * ability to set the selection state on the action and have it propogate to
 * widgets that share the action (toolbar button and right-click menu would
 * both look selected).  This ability is provided by the ActionUIFactory, but
 * can be implemented by any widget.
 * @author Michael Bushe
 * @version 1.0
 */
public interface ItemAction extends Action {

    /**
     * Changes the state of the action
     * @param selected true to set the action as selected of the action.
     */
    public void setSelected(boolean selected);

    /**
     * @return true if the action is in the selected state
     */
    public boolean isSelected();

    /**
     * Adds the ItemListener delegate as a callback when the action receives
     * an ItemEvent.
     * The listener allows the handling of state changes (like toggle and
     * group buttons) to be delegated to another object.
     * @param listener the item callback adapter
     */
    public void addItemListener(ItemListener listener);

    /**
     * Sets the ItemListener to delegate this ItemSelectable action to.
     * The listener allows the handling of state changes (like toggle and
     * group buttons) to be delegated to another object.
     * @param listener the item callback adapter
     */
    public void removeItemListener(ItemListener listener);
}

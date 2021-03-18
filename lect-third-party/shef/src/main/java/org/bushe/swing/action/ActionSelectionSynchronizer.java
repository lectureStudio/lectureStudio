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

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.lang.ref.WeakReference;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import javax.swing.AbstractButton;
import javax.swing.Action;

/**
 * Synchronizes the selected states between an action and a buttons or
 * menu items created from the action.
 * <p>
 * Both the Action and the button are stored as WeakReferences so as to not
 * leak memory (e.g., create loitering objects).  If either is garbage
 * collected, the listener is removed on the other.
 * <p>
 * The ActionSelectionSynchronizer adds itself as an ItemListener to the button
 * and adds itself as a PropertyChangeListener on the action.  When the button's
 * fires an ItemChangedEvent, ActionSelectionSynchronizer sets the action's
 * ActionManager.SELECTED property value to the button's selected property value
 * (if different).  Simlarly, when the action's ActionManager.SELECTED property
 * is set, then the ActionSelectionSynchronizer sets the selected state on the
 * button (if different), which should then propogate to all the other buttons.
 */
class ActionSelectionSynchronizer implements PropertyChangeListener, ItemListener {

    private WeakReference buttonRef;
    private WeakReference actionRef;

    /**
     * Creates a synchronizer that keeps the button and action in sync
     * @param button AbstractButton the button to keep in sync
     * @param action Action the action to keep in sync
     */
    public ActionSelectionSynchronizer(AbstractButton button, Action action) {
        if (button != null) {
            this.buttonRef = new WeakReference(button);
            button.addItemListener(this);
        }
        if (action != null) {
            this.actionRef = new WeakReference(action);
            action.addPropertyChangeListener(this);
        }
    }


    /**
     * Invoked when an item has been selected or deselected by the user.
     * @param e ItemEvent
     */
    public void itemStateChanged(ItemEvent e) {
        if (!checkValidity()) {
            return;
        }
        AbstractButton button = (AbstractButton) buttonRef.get();
        Action action = (Action) actionRef.get();
        boolean buttonSelected = button.isSelected();
        Object value = action.getValue(ActionManager.SELECTED);
        boolean changeNeeded = true;
        if (value instanceof Boolean) {
            boolean isActionSelected = ((Boolean) value).booleanValue();
           int change = e.getStateChange();
            if (change == ItemEvent.SELECTED && isActionSelected) {
                changeNeeded = false;
            } else if (change == ItemEvent.DESELECTED && !isActionSelected) {
                changeNeeded = false;
            }
        }
        if (changeNeeded) {
            action.putValue(ActionManager.SELECTED, Boolean.valueOf(buttonSelected));
        }
    }

    public void propertyChange(PropertyChangeEvent evt) {
        if (!checkValidity()) {
            return;
        }
        String propertyName = evt.getPropertyName();
        if (propertyName.equals(ActionManager.SELECTED)) {
            if (!checkValidity()) {
                return;
            }

            AbstractButton button = (AbstractButton) buttonRef.get();
            Action action = (Action) actionRef.get();
            boolean buttonSelected = button.isSelected();
            Object value = action.getValue(ActionManager.SELECTED);
            if (value instanceof Boolean) {
                boolean actionSelected = ((Boolean) value).booleanValue();
                if (actionSelected != buttonSelected) {
                    button.setSelected(actionSelected);
                }
            }
        }
    }

    private boolean checkValidity() {
        if (buttonRef == null || actionRef == null ||
            buttonRef.get() == null || actionRef.get() == null) {
            //has been garbage collected, remove self from action.
            //Doubt this would ever happen normally, but could if called directly
            removeListeners();
            return false;
        } else {
            return true;
        }
    }

    private void removeListeners() {
        Action action = (Action)actionRef.get();
        if (action != null) {
            action.removePropertyChangeListener(this);
        }
        actionRef = null;
        AbstractButton button = (AbstractButton)buttonRef.get();
        if (button != null) {
            button.removeItemListener(this);
        }
        buttonRef = null;
    }
}

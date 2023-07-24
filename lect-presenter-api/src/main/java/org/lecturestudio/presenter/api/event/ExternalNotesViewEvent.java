/*
 *
 *  * Copyright (C) 2020 TU Darmstadt, Department of Computer Science,
 *  * Embedded Systems and Applications Group.
 *  *
 *  * This program is free software: you can redistribute it and/or modify
 *  * it under the terms of the GNU General Public License as published by
 *  * the Free Software Foundation, either version 3 of the License, or
 *  * (at your option) any later version.
 *  *
 *  * This program is distributed in the hope that it will be useful,
 *  * but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  * GNU General Public License for more details.
 *  *
 *  * You should have received a copy of the GNU General Public License
 *  * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *
 */

package org.lecturestudio.presenter.api.event;
/**
 * Handles events for external notes views
 *
 * @author Dustin Ringel
 */
public class ExternalNotesViewEvent  extends ExternalViewEvent {
    /**
     * Creates a new {@code ExternalNotesViewEvent} with the provided parameters.
     *
     * @param enabled      Enable the MenuItem
     */
    public ExternalNotesViewEvent(boolean enabled) {
        super(enabled);
    }

    /**
     * Creates a new {@code ExternalNotesViewEvent} with the provided parameters.
     *
     * @param enabled      Enable the MenuItem
     * @param show         Open the external notesViewWindow
     */
    public ExternalNotesViewEvent(boolean enabled, boolean show) {
        super(enabled, show);
    }
}

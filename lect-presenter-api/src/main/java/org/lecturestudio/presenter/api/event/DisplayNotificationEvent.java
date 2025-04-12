/*
 * Copyright (C) 2025 TU Darmstadt, Department of Computer Science,
 * Embedded Systems and Applications Group.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.lecturestudio.presenter.api.event;

import org.lecturestudio.core.bus.event.BusEvent;

/**
 * Event class that represents a state change for displaying notifications.
 * This event is dispatched through the application's event bus when the notification display state should change.
 *
 * @author Alex Andres
 */
public class DisplayNotificationEvent extends BusEvent {

	/** Flag indicating whether notifications should be displayed. */
	private final boolean showNotification;


	/**
	 * Creates a new DisplayNotificationState event.
	 *
	 * @param show true to show notifications, false to hide them.
	 */
	public DisplayNotificationEvent(boolean show) {
		showNotification = show;
	}

	/**
	 * Returns whether notifications should be displayed.
	 *
	 * @return true if notifications should be shown, false otherwise.
	 */
	public boolean showNotification() {
		return showNotification;
	}
}

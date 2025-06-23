/*
 * Copyright (C) 2020 TU Darmstadt, Department of Computer Science,
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

package org.lecturestudio.core.view;

/**
 * Interface for a notification view component that displays notifications to users.
 * Extends the base {@link View} interface with notification-specific functionality.
 *
 * @author Alex Andres
 */
public interface NotificationView extends View {

	/**
	 * Sets the type of notification.
	 *
	 * @param type the notification type to set.
	 */
	void setType(NotificationType type);

	/**
	 * Sets the title of the notification.
	 *
	 * @param title the title text to display.
	 */
	void setTitle(String title);

	/**
	 * Sets the message content of the notification.
	 *
	 * @param message the message text to display.
	 */
	void setMessage(String message);

	/**
	 * Sets the action to execute when the notification is closed.
	 *
	 * @param action the action to execute on close.
	 */
	void setOnClose(Action action);

}

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

package org.lecturestudio.core.presenter.command;

import org.lecturestudio.core.presenter.NotificationPresenter;
import org.lecturestudio.core.view.NotificationType;

/**
 * Notification view command implementation. Notifications will be shown on the
 * {@code Notification} layer at the top-most view layer.
 *
 * @author Alex Andres
 */
public class NotificationCommand extends ShowPresenterCommand<NotificationPresenter> {

	/** The notification type, error, warning etc. */
	private final NotificationType type;

	/** The title of the notification. */
	private final String title;

	/** The message of the notification. */
	private final String message;


	/**
	 * Create a new NotificationCommand with the specified notification type and
	 * title. The message is empty.
	 *
	 * @param type  The type of the notification.
	 * @param title The title of the notification.
	 */
	public NotificationCommand(NotificationType type, String title) {
		this(type, title, null);
	}

	/**
	 * Create a new NotificationCommand with the specified notification type,
	 * title and message.
	 *
	 * @param type    The type of the notification.
	 * @param title   The title of the notification.
	 * @param message The message of the notification.
	 */
	public NotificationCommand(NotificationType type, String title, String message) {
		super(NotificationPresenter.class);

		this.type = type;
		this.title = title;
		this.message = message;
	}

	@Override
	public void execute(NotificationPresenter presenter) {
		presenter.setNotificationType(type);
		presenter.setTitle(title);
		presenter.setMessage(message);
	}
}

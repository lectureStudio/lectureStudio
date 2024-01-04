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

import org.lecturestudio.core.geometry.Position;
import org.lecturestudio.core.presenter.NotificationPopupPresenter;
import org.lecturestudio.core.view.NotificationType;

/**
 * NotificationPopup view command implementation. NotificationPopups will be
 * shown on the {@code NotificationPopup} layer at the top-most view layer with
 * configurable position in the main window.
 *
 * @author Alex Andres
 */
public class NotificationPopupCommand extends ShowPresenterCommand<NotificationPopupPresenter> {

	/** The position of the popup in the main window. */
	private final Position position;

	/** The notification type, error, warning etc. */
	private final NotificationType type;

	/** The title of the notification. */
	private final String title;

	/** The message of the notification. */
	private final String message;


	/**
	 * Create a new {@link NotificationPopupCommand} with the specified
	 * notification type and title. The message is empty.
	 *
	 * @param position The position of the popup in the main window.
	 * @param title    The title of the notification.
	 * @param message  The message of the notification.
	 */
	public NotificationPopupCommand(Position position, String title,
			String message) {
		this(position, NotificationType.DEFAULT, title, message);
	}

	/**
	 * Create a new {@link NotificationPopupCommand} with the specified
	 * notification type, title and message at the given position.
	 *
	 * @param position The position of the popup in the main window.
	 * @param type     The type of the notification.
	 * @param title    The title of the notification.
	 * @param message  The message of the notification.
	 */
	public NotificationPopupCommand(Position position, NotificationType type,
			String title, String message) {
		super(NotificationPopupPresenter.class);

		this.position = position;
		this.type = type;
		this.title = title;
		this.message = message;
	}

	@Override
	public void execute(NotificationPopupPresenter presenter) {
		presenter.setPosition(position);
		presenter.setNotificationType(type);
		presenter.setTitle(title);
		presenter.setMessage(message);
	}
}

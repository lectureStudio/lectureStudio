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

package org.lecturestudio.core.presenter;

import javax.inject.Inject;

import org.lecturestudio.core.app.ApplicationContext;
import org.lecturestudio.core.view.NotificationType;
import org.lecturestudio.core.view.NotificationView;
import org.lecturestudio.core.view.ViewLayer;

/**
 * Notification presenter implementation. Notifications will be shown on the
 * {@code Notification} layer at the top-most view layer.
 *
 * @author Alex Andres
 */
public class NotificationPresenter extends Presenter<NotificationView> {

	/**
	 * Creates a new notification presenter.
	 *
	 * @param context The application context.
	 * @param view    The notification views to be managed by this presenter.
	 */
	@Inject
	protected NotificationPresenter(ApplicationContext context, NotificationView view) {
		super(context, view);
	}

	/**
	 * Sets the notification type which determines the visual appearance.
	 *
	 * @param type The type of notification to display.
	 */
	public void setNotificationType(NotificationType type) {
		view.setType(type);
	}

	/**
	 * Sets the title of the notification.
	 *
	 * @param title The title text to display.
	 */
	public void setTitle(String title) {
		view.setTitle(title);
	}

	/**
	 * Sets the message body of the notification.
	 *
	 * @param message The message text to display.
	 */
	public void setMessage(String message) {
		view.setMessage(message);
	}

	@Override
	public void initialize() {
		view.setOnClose(this::close);
	}

	@Override
	public ViewLayer getViewLayer() {
		return ViewLayer.Notification;
	}
}

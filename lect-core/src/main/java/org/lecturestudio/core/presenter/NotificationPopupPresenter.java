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
import org.lecturestudio.core.geometry.Position;
import org.lecturestudio.core.view.NotificationPopupView;
import org.lecturestudio.core.view.NotificationType;
import org.lecturestudio.core.view.ViewLayer;

public class NotificationPopupPresenter extends Presenter<NotificationPopupView> {

	@Inject
	NotificationPopupPresenter(ApplicationContext context, NotificationPopupView view) {
		super(context, view);
	}

	public void setNotificationType(NotificationType type) {
		view.setType(type);
	}

	public void setTitle(String title) {
		view.setTitle(title);
	}

	public void setMessage(String message) {
		view.setMessage(message);
	}

	public void setPosition(Position position) {
		view.setPosition(position);
	}

	@Override
	public ViewLayer getViewLayer() {
		return ViewLayer.NotificationPopup;
	}
}

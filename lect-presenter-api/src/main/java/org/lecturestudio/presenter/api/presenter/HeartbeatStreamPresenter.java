/*
 * Copyright (C) 2021 TU Darmstadt, Department of Computer Science,
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

package org.lecturestudio.presenter.api.presenter;

import javax.inject.Inject;

import org.lecturestudio.core.app.ApplicationContext;
import org.lecturestudio.core.app.dictionary.Dictionary;
import org.lecturestudio.core.presenter.NotificationPresenter;
import org.lecturestudio.core.view.NotificationType;
import org.lecturestudio.core.view.NotificationView;

public class HeartbeatStreamPresenter extends NotificationPresenter {

	@Inject
	protected HeartbeatStreamPresenter(ApplicationContext context, NotificationView view) {
		super(context, view);
	}

	@Override
	public void initialize() {
		Dictionary dict = context.getDictionary();

		setNotificationType(NotificationType.WARNING);
		setTitle(dict.get("heartbeat.error"));
		setMessage(dict.get("heartbeat.error.message"));
	}
}

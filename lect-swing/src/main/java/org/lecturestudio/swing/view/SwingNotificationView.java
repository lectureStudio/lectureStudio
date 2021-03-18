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

package org.lecturestudio.swing.view;

import java.util.ResourceBundle;

import javax.inject.Inject;
import javax.swing.JButton;

import org.lecturestudio.core.view.Action;
import org.lecturestudio.core.view.NotificationType;
import org.lecturestudio.core.view.NotificationView;
import org.lecturestudio.swing.components.NotificationPane;
import org.lecturestudio.swing.util.SwingUtils;

public class SwingNotificationView extends NotificationPane implements NotificationView {

	private Action closeAction;

	private JButton closeButton;


	@Inject
	SwingNotificationView(ResourceBundle resources) {
		super();

		initialize(resources);
	}

	@Override
	public void setOnClose(Action action) {
		closeAction = Action.concatenate(closeAction, action);

		SwingUtils.bindAction(closeButton, action);
	}

	@Override
	public void setType(NotificationType type) {
		// Disallow closing in waiting mode.
		if (type == NotificationType.WAITING) {
			removeButton(closeButton);
		}
		else if (getType() == NotificationType.WAITING) {
			addButton(closeButton);
		}

		super.setType(type);
	}

	private void initialize(ResourceBundle resources) {
		closeButton = new JButton(resources.getString("button.close"));

		addButton(closeButton);

		closeButton.requestFocus();
	}
}

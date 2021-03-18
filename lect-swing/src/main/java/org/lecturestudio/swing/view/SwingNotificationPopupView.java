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

import java.awt.Color;

import javax.swing.border.LineBorder;

import org.lecturestudio.core.geometry.Position;
import org.lecturestudio.core.view.Action;
import org.lecturestudio.core.view.NotificationPopupView;
import org.lecturestudio.swing.components.NotificationPane;

public class SwingNotificationPopupView extends NotificationPane implements NotificationPopupView {

	private Action closeAction;

	private Position position;


	public SwingNotificationPopupView() {
		super();

		setBorder(new LineBorder(Color.gray, 2));
	}

	@Override
	public void setPosition(Position position) {
		this.position = position;
	}

	@Override
	public Position getPosition() {
		return position;
	}

	@Override
	public void setOnClose(Action action) {
		this.closeAction = Action.concatenate(closeAction, action);
	}
}

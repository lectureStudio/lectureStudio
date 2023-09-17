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

package org.lecturestudio.javafx.dialog;

import java.util.ResourceBundle;

import javafx.beans.property.ObjectProperty;
import javafx.collections.ObservableList;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.stage.Window;

import org.lecturestudio.core.view.NotificationType;
import org.lecturestudio.javafx.control.NotificationPane;

public class NotificationDialog extends Dialog {

	protected NotificationPane notificationPane;


	public NotificationDialog(ResourceBundle resources) {
		super(resources);
	}

	public NotificationDialog(Window owner, ResourceBundle resources) {
		super(owner, resources);
	}

	public ObjectProperty<NotificationType> typeProperty() {
		return notificationPane.typeProperty();
	}

	public NotificationType getType() {
		return notificationPane.getType();
	}

	public void setType(NotificationType type) {
		notificationPane.setType(type);
	}

	public void setIcon(Node node) {
		notificationPane.setIcon(node);
	}

	public void setMessageTitle(String title) {
		notificationPane.setTitle(title);
	}

	public void setMessage(String message) {
		notificationPane.setMessage(message);
	}

	protected ObservableList<Node> getButtons() {
		return notificationPane.getButtons();
	}

	@Override
	protected Parent createRoot() {
		notificationPane = new NotificationPane(resources);

		return notificationPane;
	}

}

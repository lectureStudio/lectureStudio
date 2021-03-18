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

package org.lecturestudio.javafx.view;

import static java.util.Objects.nonNull;

import java.util.ResourceBundle;

import javax.inject.Inject;

import javafx.fxml.FXML;
import javafx.scene.control.Button;

import org.lecturestudio.core.view.Action;
import org.lecturestudio.core.view.NotificationType;
import org.lecturestudio.core.view.NotificationView;
import org.lecturestudio.javafx.control.NotificationPane;
import org.lecturestudio.javafx.util.FxUtils;

public class FxNotificationView extends NotificationPane implements NotificationView, FxTopView {

	private Action closeAction;

	private Button closeButton;


	@Inject
	public FxNotificationView(ResourceBundle resources) {
		super(resources);
	}

	@Override
	public void setOnClose(Action action) {
		this.closeAction = Action.concatenate(closeAction, action);

		FxUtils.bindAction(closeButton, action);
	}

	@Override
	public void onShortcutClose() {
		// Disallow closing in waiting mode.
		if (getType() == NotificationType.WAITING) {
			return;
		}

		if (nonNull(closeAction)) {
			closeAction.execute();
		}
	}

	@Override
	public void onSceneSet() {
		FxTopView.super.onSceneSet();

		// Request focus to enable shortcut listeners.
		closeButton.requestFocus();
	}

	@FXML
	protected void initialize() {
		super.initialize();

		closeButton = new Button(getResourceBundle().getString("button.close"));

		getButtons().add(closeButton);

		typeProperty().addListener((observable, oldType, newType) -> {
			// Disallow closing in waiting mode.
			if (newType == NotificationType.WAITING) {
				getButtons().remove(closeButton);
			}
			else if (oldType == NotificationType.WAITING) {
				getButtons().add(closeButton);
			}
		});

		registerOnSceneSet();
	}
}

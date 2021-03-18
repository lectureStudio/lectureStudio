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

import java.util.ResourceBundle;

import javax.inject.Inject;

import org.lecturestudio.core.view.Action;
import org.lecturestudio.core.view.NotificationType;
import org.lecturestudio.core.view.ProgressView;
import org.lecturestudio.javafx.control.NotificationPane;
import org.lecturestudio.javafx.util.FxUtils;

import javafx.application.Platform;
import javafx.scene.control.Button;
import javafx.scene.control.ProgressIndicator;

public class FxProgressView extends NotificationPane implements ProgressView, FxTopView {

	private Action closeAction;

	private Action viewShownAction;

	private ProgressIndicator progressIndicator;

	private Button closeButton;


	@Inject
	FxProgressView(ResourceBundle resources) {
		super(resources);

		initialize(resources);
	}

	@Override
	public void setError(String message) {
		setType(NotificationType.ERROR);
		setMessage(message);
	}

	@Override
	public void setProgress(double progress) {
		FxUtils.invoke(() -> progressIndicator.setProgress(progress));
	}

	@Override
	public void setOnClose(Action action) {
		closeAction = Action.concatenate(closeAction, action);

		FxUtils.bindAction(closeButton, action);
	}

	@Override
	public void setOnViewShown(Action action) {
		viewShownAction = Action.concatenate(viewShownAction, action);
	}

	@Override
	public void onShortcutClose() {

	}

	@Override
	public void onSceneSet() {
		Platform.runLater(() -> {
			closeButton.requestFocus();
			executeAction(viewShownAction);
		});
	}

	private void initialize(ResourceBundle resources) {
		registerOnSceneSet();

		progressIndicator = new ProgressIndicator();
		closeButton = new Button(resources.getString("button.close"));

		setIcon(progressIndicator);
		getButtons().add(closeButton);
	}
}

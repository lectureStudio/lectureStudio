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

package org.lecturestudio.presenter.javafx.view;

import static java.util.Objects.nonNull;

import java.io.File;
import java.util.ResourceBundle;

import javax.inject.Inject;

import javafx.application.Platform;
import javafx.scene.control.Button;
import javafx.scene.control.ProgressIndicator;

import org.lecturestudio.core.view.Action;
import org.lecturestudio.core.view.NotificationType;
import org.lecturestudio.javafx.control.NotificationPane;
import org.lecturestudio.javafx.util.FxUtils;
import org.lecturestudio.javafx.view.FxTopView;
import org.lecturestudio.javafx.view.FxmlView;
import org.lecturestudio.presenter.api.view.SaveRecordingView;

@FxmlView(name = "save-recording")
public class FxSaveRecordingView extends NotificationPane implements SaveRecordingView, FxTopView {

	private Action closeAction;

	private Action viewShownAction;

	private Button closeButton;

	private ProgressIndicator progressIndicator;


	@Inject
	public FxSaveRecordingView(ResourceBundle resources) {
		super(resources);
	}

	@Override
	public void setDestinationFile(File file) {
		FxUtils.invoke(() -> {
			setMessage(file.getPath());
		});
	}

	@Override
	public void setError(String message) {
		FxUtils.invoke(() -> {
			setType(NotificationType.ERROR);
			setTitle(getResourceBundle().getString("recording.save.error"));
			setMessage(message);
		});
	}

	@Override
	public void setSuccess() {
		FxUtils.invoke(() -> {
			setTitle(getResourceBundle().getString("recording.save.success"));
		});
	}

	@Override
	public void setProgress(double progress) {
		FxUtils.invoke(() -> {
			progressIndicator.setProgress(progress);

			if (Double.compare(progressIndicator.getProgress(), 1.0) >= 0) {
				closeButton.setDisable(false);

				// Request focus to enable shortcut listeners.
				Platform.runLater(this::requestFocus);
			}
		});
	}

	@Override
	public void setOnClose(Action action) {
		this.closeAction = Action.concatenate(closeAction, action);

		if (nonNull(closeAction)) {
			FxUtils.bindAction(closeButton, closeAction);
		}
	}

	@Override
	public void setOnViewShown(Action action) {
		this.viewShownAction = Action.concatenate(viewShownAction, action);
	}

	@Override
	public void onSceneSet() {
		FxTopView.super.onSceneSet();

		setIcon(progressIndicator);
		setTitle(getResourceBundle().getString("recording.save"));
		getButtons().add(closeButton);

		executeAction(viewShownAction);
	}

	@Override
	public void onShortcutClose() {
		if (Double.compare(progressIndicator.getProgress(), 1.0) < 0 && getType() != NotificationType.ERROR) {
			// Don't allow to close until the process has finished.
			return;
		}

		if (nonNull(closeAction)) {
			closeAction.execute();
		}
	}

	@Override
	protected void initialize() {
		super.initialize();

		progressIndicator = new ProgressIndicator();

		closeButton = new Button(getResourceBundle().getString("button.close"));
		closeButton.setDisable(true);

		registerOnSceneSet();
	}
}

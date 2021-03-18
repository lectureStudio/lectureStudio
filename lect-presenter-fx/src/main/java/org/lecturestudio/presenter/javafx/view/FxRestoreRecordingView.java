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

import java.util.ResourceBundle;

import javax.inject.Inject;

import javafx.beans.InvalidationListener;
import javafx.beans.Observable;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ProgressIndicator;

import org.lecturestudio.core.view.Action;
import org.lecturestudio.core.view.NotificationType;
import org.lecturestudio.javafx.control.NotificationPane;
import org.lecturestudio.javafx.util.FxUtils;
import org.lecturestudio.javafx.view.FxmlView;
import org.lecturestudio.presenter.api.view.RestoreRecordingView;

@FxmlView(name = "restore-recording")
public class FxRestoreRecordingView extends NotificationPane implements RestoreRecordingView {

	private ProgressIndicator progressIndicator;

	@FXML
	private Button discardButton;

	@FXML
	private Button saveButton;


	@Inject
	public FxRestoreRecordingView(ResourceBundle resources) {
		super(resources);
	}

	@Override
	public void setSavePath(String path) {
		FxUtils.invoke(() -> {
			setMessage(path);
		});
	}

	@Override
	public void setError(String message) {
		FxUtils.invoke(() -> {
			setType(NotificationType.ERROR);
			setTitle(getResourceBundle().getString("restore.recording.write.error"));
			setMessage(message);
		});
	}

	@Override
	public void setSuccess() {
		FxUtils.invoke(() -> {
			setTitle(getResourceBundle().getString("restore.recording.write.success"));

			getButtons().clear();
		});
	}

	@Override
	public void setProgress(double progress) {
		FxUtils.invoke(() -> {
			progressIndicator.setProgress(progress);
		});
	}

	@Override
	public void setOnDiscard(Action action) {
		FxUtils.bindAction(discardButton, action);
	}

	@Override
	public void setOnSave(Action action) {
		FxUtils.bindAction(saveButton, action);
	}

	@Override
	public void showProgress() {
		FxUtils.invoke(() -> {
			setTitle(getResourceBundle().getString("restore.recording.write"));
			setIcon(progressIndicator);
		});
	}

	@Override
	public void showWarning() {
		FxUtils.invoke(() -> {
			setType(NotificationType.WARNING);
			setMessage(getResourceBundle().getString("restore.recording.discard.warning"));
		});
	}

	@FXML
	protected void initialize() {
		super.initialize();

		progressIndicator = new ProgressIndicator();

		sceneProperty().addListener(new InvalidationListener() {

			@Override
			public void invalidated(Observable observable) {
				if (nonNull(getScene())) {
					sceneProperty().removeListener(this);

					setType(NotificationType.QUESTION);
					setTitle(getResourceBundle().getString("restore.recording.save"));
					setMessage(getResourceBundle().getString("restore.recording.question"));

					saveButton.requestFocus();
				}
			}
		});
	}

}

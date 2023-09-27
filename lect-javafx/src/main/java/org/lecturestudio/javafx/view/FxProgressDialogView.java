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

import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.ProgressIndicator;

import javax.inject.Inject;

import org.lecturestudio.core.view.NotificationType;
import org.lecturestudio.core.view.ProgressDialogView;
import org.lecturestudio.javafx.dialog.NotificationDialog;
import org.lecturestudio.javafx.util.FxUtils;

public class FxProgressDialogView extends NotificationDialog implements ProgressDialogView {

	private Button closeButton;

	private ProgressIndicator progressIndicator;


	@Inject
	public FxProgressDialogView(ResourceBundle resources) {
		super(resources);
	}

	@Override
	public void setError(String message) {
		FxUtils.invoke(() -> {
			setType(NotificationType.ERROR);

			setMessageTitle(message);
		});
	}

	@Override
	public void setError(String message, String error) {
		FxUtils.invoke(() -> {
			setType(NotificationType.ERROR);

			setMessageTitle(message);
			setMessage(error);
		});
	}

	@Override
	public void setProgress(double progress) {
		FxUtils.invoke(() -> {
			progressIndicator.setProgress(progress);

			if (Double.compare(progressIndicator.getProgress(), 1.0) >= 0) {
				closeButton.setDisable(false);
			}
		});
	}

	@Override
	public void setOnShown(Runnable runnable) {
		super.setOnShown(event -> runnable.run());
	}

	@Override
	public void setOnHidden(Runnable runnable) {
		super.setOnHidden(event -> runnable.run());
	}

	@Override
	public void open() {
		show();
	}

	@Override
	public void close() {
		if (progressIndicator.getProgress() < 1.0 && getType() != NotificationType.ERROR) {
			// Don't allow to close until the process has finished.
			return;
		}

		super.close();
	}

	@Override
	protected Parent createRoot() {
		Parent root = super.createRoot();

		progressIndicator = new ProgressIndicator();

		closeButton = new Button(resources.getString("button.close"));
		closeButton.setDisable(true);
		closeButton.setOnAction(event -> close());

		setIcon(progressIndicator);
		getButtons().add(closeButton);

		typeProperty().addListener(observable -> {
			if (getType() == NotificationType.ERROR) {
				closeButton.setDisable(false);
			}
		});

		return root;
	}
}

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

package org.lecturestudio.editor.javafx.view;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.layout.StackPane;

import org.lecturestudio.core.view.Action;
import org.lecturestudio.editor.api.view.NoiseReductionProgressView;
import org.lecturestudio.javafx.util.FxUtils;
import org.lecturestudio.javafx.view.FxmlView;

@FxmlView(name = "noise-reduction-progress")
public class FxNoiseReductionProgressView extends StackPane implements NoiseReductionProgressView {

	@FXML
	private Label titleLabel;

	@FXML
	private ProgressBar progressBar;

	@FXML
	private Button cancelButton;

	@FXML
	private Button closeButton;


	public FxNoiseReductionProgressView() {
		super();
	}

	@Override
	public void setTitle(String title) {
		FxUtils.invoke(() -> titleLabel.setText(title));
	}

	@Override
	public void setProgress(double progress) {
		FxUtils.invoke(() -> progressBar.setProgress(progress));
	}

	@Override
	public void setCanceled() {
		FxUtils.invoke(() -> {
			closeButton.setDisable(false);
			cancelButton.setDisable(true);
		});
	}

	@Override
	public void setFinished() {
		FxUtils.invoke(() -> {
			closeButton.setDisable(false);
			cancelButton.setDisable(true);
		});
	}

	@Override
	public void setOnCancel(Action action) {
		FxUtils.bindAction(cancelButton, action);
	}

	@Override
	public void setOnClose(Action action) {
		FxUtils.bindAction(closeButton, action);
	}
}

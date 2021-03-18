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

import java.util.ResourceBundle;

import javax.inject.Inject;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.StackPane;

import org.lecturestudio.core.view.Action;
import org.lecturestudio.javafx.control.LevelMeter;
import org.lecturestudio.javafx.util.FxUtils;
import org.lecturestudio.javafx.view.FxmlView;
import org.lecturestudio.presenter.api.view.AdjustAudioCaptureLevelView;

@FxmlView(name = "adjust-audio-capture-level")
public class FxAdjustAudioCaptureLevelView extends StackPane implements AdjustAudioCaptureLevelView {

	private final ResourceBundle resources;

	private Action finishAction;

	@FXML
	private LevelMeter levelMeter;

	@FXML
	private Label captureDeviceLabel;

	@FXML
	private Button beginButton;

	@FXML
	private Button cancelButton;


	@Inject
	public FxAdjustAudioCaptureLevelView(ResourceBundle resources) {
		super();

		this.resources = resources;
	}

	@Override
	public void setAudioLevel(double value) {
		FxUtils.invoke(() -> levelMeter.setLevel(value));
	}

	@Override
	public void setAudioLevelCaptureStarted(boolean started) {
		String buttonText;

		if (started) {
			buttonText = resources.getString("button.finish");
		}
		else {
			buttonText = resources.getString("button.begin");
		}

		FxUtils.invoke(() -> beginButton.setText(buttonText));

		FxUtils.bindAction(beginButton, finishAction);
	}

	@Override
	public void setCaptureDeviceName(String name) {
		FxUtils.invoke(() -> captureDeviceLabel.setText(name));
	}

	@Override
	public void setOnBegin(Action action) {
		FxUtils.bindAction(beginButton, action);
	}

	@Override
	public void setOnCancel(Action action) {
		FxUtils.bindAction(cancelButton, action);
	}

	@Override
	public void setOnFinish(Action action) {
		this.finishAction = action;
	}

}

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

import org.lecturestudio.core.view.Action;
import org.lecturestudio.javafx.control.NotificationPane;
import org.lecturestudio.javafx.util.FxUtils;
import org.lecturestudio.presenter.api.view.ConfirmStopRecordingView;

public class FxConfirmStopRecordingView extends NotificationPane implements ConfirmStopRecordingView {

	private Button stopButton;

	private Button continueButton;


	@Inject
	public FxConfirmStopRecordingView(ResourceBundle resources) {
		super(resources);
	}

	@Override
	public void setOnStopRecording(Action action) {
		FxUtils.bindAction(stopButton, action);
	}

	@Override
	public void setOnContinueRecording(Action action) {
		FxUtils.bindAction(continueButton, action);
	}

	@Override
	public void setOnClose(Action action) {

	}

	@FXML
	protected void initialize() {
		super.initialize();

		stopButton = new Button(getResourceBundle().getString("button.end"));
		continueButton = new Button(getResourceBundle().getString("button.continue"));

		getButtons().addAll(stopButton, continueButton);
	}
}

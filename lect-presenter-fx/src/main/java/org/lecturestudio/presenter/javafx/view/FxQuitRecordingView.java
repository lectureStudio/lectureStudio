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

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.layout.StackPane;

import org.lecturestudio.core.view.Action;
import org.lecturestudio.javafx.util.FxUtils;
import org.lecturestudio.javafx.view.FxmlView;
import org.lecturestudio.presenter.api.view.QuitRecordingView;

@FxmlView(name = "quit-recording")
public class FxQuitRecordingView extends StackPane implements QuitRecordingView {

	@FXML
	private Button abortButton;

	@FXML
	private Button discardButton;

	@FXML
	private Button saveButton;


	public FxQuitRecordingView() {
		super();
	}

	@Override
	public void setOnAbort(Action action) {
		FxUtils.bindAction(abortButton, action);
	}

	@Override
	public void setOnDiscardRecording(Action action) {
		FxUtils.bindAction(discardButton, action);
	}

	@Override
	public void setOnSaveRecording(Action action) {
		FxUtils.bindAction(saveButton, action);
	}

}

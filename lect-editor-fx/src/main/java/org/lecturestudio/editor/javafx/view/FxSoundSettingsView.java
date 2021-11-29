/*
 * Copyright (C) 2021 TU Darmstadt, Department of Computer Science,
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

import java.util.List;

import org.lecturestudio.core.beans.StringProperty;
import org.lecturestudio.core.view.Action;
import org.lecturestudio.editor.api.view.SoundSettingsView;
import org.lecturestudio.javafx.beans.LectStringProperty;
import org.lecturestudio.javafx.util.FxUtils;
import org.lecturestudio.javafx.view.FxmlView;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.layout.GridPane;

@FxmlView(name = "sound-settings", presenter = org.lecturestudio.editor.api.presenter.SoundSettingsPresenter.class)
public class FxSoundSettingsView extends GridPane implements SoundSettingsView {

	@FXML
	private ComboBox<String> playbackDeviceCombo;

	@FXML
	private Button closeButton;

	@FXML
	private Button resetButton;


	public FxSoundSettingsView() {
		super();
	}

	@Override
	public void setAudioPlaybackDevice(StringProperty deviceName) {
		playbackDeviceCombo.valueProperty()
				.bindBidirectional(new LectStringProperty(deviceName));
	}

	@Override
	public void setAudioPlaybackDevices(List<String> devices) {
		FxUtils.invoke(() -> playbackDeviceCombo.getItems().setAll(devices));
	}

	@Override
	public void setOnClose(Action action) {
		FxUtils.bindAction(closeButton, action);
	}

	@Override
	public void setOnReset(Action action) {
		FxUtils.bindAction(resetButton, action);
	}
}

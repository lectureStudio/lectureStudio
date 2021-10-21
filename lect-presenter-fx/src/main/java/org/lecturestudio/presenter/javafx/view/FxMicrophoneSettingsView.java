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

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Hyperlink;
import javafx.scene.layout.GridPane;

import org.lecturestudio.core.audio.device.AudioInputDevice;
import org.lecturestudio.core.audio.device.AudioOutputDevice;
import org.lecturestudio.core.beans.BooleanProperty;
import org.lecturestudio.core.beans.FloatProperty;
import org.lecturestudio.core.beans.StringProperty;
import org.lecturestudio.core.view.Action;
import org.lecturestudio.core.view.ConsumerAction;
import org.lecturestudio.javafx.beans.LectStringProperty;
import org.lecturestudio.javafx.control.LevelMeter;
import org.lecturestudio.javafx.util.FxUtils;
import org.lecturestudio.javafx.view.FxmlView;
import org.lecturestudio.presenter.api.presenter.SoundSettingsPresenter;
import org.lecturestudio.presenter.api.view.SoundSettingsView;

@FxmlView(name = "microphone-settings", presenter = SoundSettingsPresenter.class)
public class FxMicrophoneSettingsView extends GridPane implements
		SoundSettingsView {

	@FXML
	private ComboBox<String> audioCaptureDevicesCombo;

	@FXML
	private LevelMeter levelMeter;

	@FXML
	private Hyperlink adjustAudioInputLevelButton;

	@FXML
	private Button closeButton;

	@FXML
	private Button resetButton;

	private ConsumerAction<Boolean> viewVisibleAction;


	public FxMicrophoneSettingsView() {
		super();
	}

	@Override
	public void setViewEnabled(boolean enabled) {
		FxUtils.invoke(() -> setDisable(!enabled));
	}

	@Override
	public void setAudioCaptureDevice(StringProperty captureDeviceName) {
		audioCaptureDevicesCombo.valueProperty().bindBidirectional(new LectStringProperty(captureDeviceName));
	}

	@Override
	public void setAudioCaptureDevices(AudioInputDevice[] captureDevices) {
		FxUtils.invoke(() -> {
			ObservableList<String> deviceList = audioCaptureDevicesCombo.getItems();
			deviceList.clear();

			for (AudioInputDevice device : captureDevices) {
				deviceList.add(device.getName());
			}
		});
	}

	@Override
	public void setAudioPlaybackDevice(StringProperty playbackDeviceName) {

	}

	@Override
	public void setAudioPlaybackDevices(AudioOutputDevice[] playbackDevices) {

	}

	@Override
	public void setAudioCaptureLevel(double value) {
		FxUtils.invoke(() -> {
			levelMeter.setLevel(value);
		});
	}

	@Override
	public void bindAudioCaptureLevel(FloatProperty levelProperty) {
		throw new RuntimeException("Not implemented");
	}

	@Override
	public void bindTestCaptureEnabled(BooleanProperty enable) {
		throw new RuntimeException("Not implemented");
	}

	@Override
	public void bindTestPlaybackEnabled(BooleanProperty enable) {
		throw new RuntimeException("Not implemented");
	}

	@Override
	public void setOnTestCapture(BooleanProperty recordProperty) {
		throw new RuntimeException("Not implemented");
	}

	@Override
	public void setOnTestCapturePlayback(BooleanProperty playProperty) {
		throw new RuntimeException("Not implemented");
	}

	@Override
	public void setOnAdjustAudioCaptureLevel(Action action) {
		FxUtils.bindAction(adjustAudioInputLevelButton, action);
	}

	@Override
	public void setOnViewVisible(ConsumerAction<Boolean> action) {
		this.viewVisibleAction = action;
	}

	@Override
	public void setOnClose(Action action) {
		FxUtils.bindAction(closeButton, action);
	}

	@Override
	public void setOnReset(Action action) {
		FxUtils.bindAction(resetButton, action);
	}

	@FXML
	private void initialize() {
		// Handle audio-level capture state.
		sceneProperty().addListener((observable, oldScene, newScene) -> {
			boolean capture = nonNull(newScene) && getParent().isVisible();

			if (nonNull(oldScene)) {
				executeAction(viewVisibleAction, capture);
			}
			if (nonNull(newScene)) {
				executeAction(viewVisibleAction, capture);
			}
		});

		parentProperty().addListener(new ChangeListener<>() {

			@Override
			public void changed(ObservableValue<? extends Parent> observable, Parent oldParent, Parent newParent) {
				if (nonNull(newParent)) {
					parentProperty().removeListener(this);

					newParent.visibleProperty().addListener(observable1 -> {
						boolean capture = nonNull(getParent()) && getParent().isVisible();

						executeAction(viewVisibleAction, capture);
					});
				}
			}
		});
	}

}

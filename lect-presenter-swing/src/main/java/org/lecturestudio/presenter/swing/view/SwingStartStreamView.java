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

package org.lecturestudio.presenter.swing.view;

import static java.util.Objects.isNull;

import java.awt.Color;
import java.awt.Container;
import java.util.List;
import java.util.Vector;

import javax.swing.AbstractButton;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JToggleButton;

import org.lecturestudio.core.audio.device.AudioInputDevice;
import org.lecturestudio.core.audio.device.AudioOutputDevice;
import org.lecturestudio.core.beans.BooleanProperty;
import org.lecturestudio.core.beans.ObjectProperty;
import org.lecturestudio.core.beans.StringProperty;
import org.lecturestudio.core.camera.Camera;
import org.lecturestudio.core.camera.CameraFormat;
import org.lecturestudio.core.view.Action;
import org.lecturestudio.presenter.api.view.StartStreamView;
import org.lecturestudio.swing.components.CameraPreviewPanel;
import org.lecturestudio.swing.util.SwingUtils;
import org.lecturestudio.swing.view.SwingView;
import org.lecturestudio.swing.view.ViewPostConstruct;
import org.lecturestudio.web.api.stream.model.Course;

@SwingView(name = "start-stream")
public class SwingStartStreamView extends JPanel implements StartStreamView {

	private Container contentContainer;

	private JComboBox<Course> courseCombo;

	private JComboBox<String> audioCaptureDevicesCombo;

	private JComboBox<String> audioPlaybackDevicesCombo;

	private JComboBox<String> camerasCombo;

	private CameraPreviewPanel cameraView;

	private JToggleButton muteMicrophoneButton;

	private JToggleButton enableCameraButton;

	private JCheckBox messengerCheckBox;

	private JLabel errorLabel;

	private JButton settingsButton;

	private JButton closeButton;

	private JButton startButton;


	SwingStartStreamView() {
		super();
	}

	@Override
	public void setCourse(ObjectProperty<Course> course) {
		SwingUtils.invoke(() -> {
			SwingUtils.bindBidirectional(courseCombo, course);
		});
	}

	@Override
	public void setCourses(List<Course> courses) {
		SwingUtils.invoke(() -> {
			courseCombo.setLightWeightPopupEnabled(false);
			courseCombo.setModel(new DefaultComboBoxModel<>(new Vector<>(courses)));
		});
	}

	@Override
	public void setAudioCaptureDevice(StringProperty captureDeviceName) {
		SwingUtils.invoke(() -> {
			SwingUtils.bindBidirectional(audioCaptureDevicesCombo, captureDeviceName);
		});
	}

	@Override
	public void setAudioCaptureDevices(AudioInputDevice[] captureDevices) {
		DefaultComboBoxModel<String> model = new DefaultComboBoxModel<>();

		for (AudioInputDevice device : captureDevices) {
			model.addElement(device.getName());
		}

		SwingUtils.invoke(() -> {
			audioCaptureDevicesCombo.setModel(model);
		});
	}

	@Override
	public void setAudioPlaybackDevice(StringProperty playbackDeviceName) {
		SwingUtils.invoke(() -> {
			SwingUtils.bindBidirectional(audioPlaybackDevicesCombo, playbackDeviceName);
		});
	}

	@Override
	public void setAudioPlaybackDevices(AudioOutputDevice[] playbackDevices) {
		DefaultComboBoxModel<String> model = new DefaultComboBoxModel<>();

		for (AudioOutputDevice device : playbackDevices) {
			model.addElement(device.getName());
		}

		SwingUtils.invoke(() -> {
			audioPlaybackDevicesCombo.setModel(model);
		});
	}

	@Override
	public void setCameraName(StringProperty cameraName) {
		SwingUtils.bindBidirectional(camerasCombo, cameraName);
	}

	@Override
	public void setCameraNames(String[] cameraNames) {
		if (isNull(cameraNames)) {
			return;
		}

		SwingUtils.invoke(() -> camerasCombo
				.setModel(new DefaultComboBoxModel<>(cameraNames)));
	}

	@Override
	public void setEnableMicrophone(BooleanProperty enable) {
		SwingUtils.bindBidirectional(muteMicrophoneButton, enable);

		setComponentColor(muteMicrophoneButton);
	}

	@Override
	public void setEnableCamera(BooleanProperty enable) {
		SwingUtils.bindBidirectional(enableCameraButton, enable);

		setComponentColor(enableCameraButton);
	}

	@Override
	public void setEnableMessenger(BooleanProperty enable) {
		SwingUtils.bindBidirectional(messengerCheckBox, enable);
	}

	@Override
	public void setCamera(Camera camera) {
		cameraView.setCamera(camera);
	}

	@Override
	public void setCameraFormat(CameraFormat cameraFormat) {
		cameraView.setCameraFormat(cameraFormat);
	}

	@Override
	public void setCameraStatus(String statusMessage) {
		cameraView.setStatusMessage(statusMessage);
	}

	@Override
	public void startCameraPreview() {
		cameraView.startCapture();
	}

	@Override
	public void stopCameraPreview() {
		cameraView.stopCapture();
	}

	@Override
	public void setError(String message) {
		SwingUtils.invoke(() -> {
			errorLabel.setText(message);
			errorLabel.setVisible(true);

			contentContainer.setVisible(false);
			startButton.setVisible(false);
			settingsButton.setVisible(true);
		});
	}

	@Override
	public void setOnSettings(Action action) {
		SwingUtils.bindAction(settingsButton, action);
	}

	@Override
	public void setOnClose(Action action) {
		SwingUtils.bindAction(closeButton, action);
	}

	@Override
	public void setOnStart(Action action) {
		SwingUtils.bindAction(startButton, action);
	}

	@ViewPostConstruct
	private void initialize() {
		muteMicrophoneButton.addActionListener(e -> {
			setComponentColor(muteMicrophoneButton);
		});
		enableCameraButton.addActionListener(e -> {
			setComponentColor(enableCameraButton);
		});
	}

	private void setComponentColor(AbstractButton button) {
		button.setBackground(button.isSelected() ?
				Color.decode("#D1FAE5") :
				Color.decode("#FEE2E2"));
	}
}

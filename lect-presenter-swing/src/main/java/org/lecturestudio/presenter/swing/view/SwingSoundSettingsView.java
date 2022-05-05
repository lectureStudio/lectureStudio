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

package org.lecturestudio.presenter.swing.view;

import com.formdev.flatlaf.util.UIScale;

import java.util.List;
import java.util.ResourceBundle;

import javax.inject.Inject;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JPanel;
import javax.swing.JSlider;
import javax.swing.JToggleButton;
import javax.swing.event.AncestorEvent;
import javax.swing.event.AncestorListener;

import org.lecturestudio.core.audio.AudioProcessingSettings.NoiseSuppressionLevel;
import org.lecturestudio.core.audio.device.AudioDevice;
import org.lecturestudio.core.beans.BooleanProperty;
import org.lecturestudio.core.beans.DoubleProperty;
import org.lecturestudio.core.beans.FloatProperty;
import org.lecturestudio.core.beans.ObjectProperty;
import org.lecturestudio.core.beans.StringProperty;
import org.lecturestudio.core.converter.DoubleIntegerConverter;
import org.lecturestudio.core.converter.FloatIntegerConverter;
import org.lecturestudio.core.view.Action;
import org.lecturestudio.core.view.ConsumerAction;
import org.lecturestudio.presenter.api.presenter.SoundSettingsPresenter;
import org.lecturestudio.presenter.api.view.SoundSettingsView;
import org.lecturestudio.presenter.swing.combobox.NoiseSuppressionLevelRenderer;
import org.lecturestudio.swing.beans.ConvertibleNumberProperty;
import org.lecturestudio.swing.components.LevelMeter;
import org.lecturestudio.swing.util.SwingUtils;
import org.lecturestudio.swing.view.SwingView;
import org.lecturestudio.swing.view.ViewPostConstruct;

@SwingView(name = "sound-settings", presenter = SoundSettingsPresenter.class)
public class SwingSoundSettingsView extends JPanel implements SoundSettingsView {

	private final ResourceBundle resources;

	private JComboBox<String> audioCaptureDevicesCombo;

	private JComboBox<String> audioPlaybackDevicesCombo;

	private JComboBox<NoiseSuppressionLevel> noiseSuppressionCombo;

	private LevelMeter levelMeter;

	private JButton adjustAudioInputLevelButton;

	private JToggleButton testCaptureButton;

	private JToggleButton playCaptureButton;

	private JToggleButton testSpeakerSoundButton;

	private JSlider micVolumeSlider;

	private JSlider speakerVolumeSlider;

	private JButton closeButton;

	private JButton resetButton;

	private ConsumerAction<Boolean> viewVisibleAction;


	@Inject
	SwingSoundSettingsView(ResourceBundle resources) {
		super();

		this.resources = resources;
	}

	@Override
	public void setViewEnabled(boolean enabled) {
		SwingUtils.invoke(() -> SwingUtils.setEnabled(enabled,
				audioCaptureDevicesCombo, adjustAudioInputLevelButton,
				testCaptureButton, playCaptureButton, testSpeakerSoundButton,
				micVolumeSlider, speakerVolumeSlider));
	}

	@Override
	public void setAudioCaptureDevice(StringProperty captureDeviceName) {
		SwingUtils.invoke(() -> {
			SwingUtils.bindBidirectional(audioCaptureDevicesCombo, captureDeviceName);
		});
	}

	@Override
	public void setAudioCaptureDevices(AudioDevice[] captureDevices) {
		DefaultComboBoxModel<String> model = new DefaultComboBoxModel<>();

		for (AudioDevice device : captureDevices) {
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
	public void setAudioPlaybackDevices(AudioDevice[] playbackDevices) {
		DefaultComboBoxModel<String> model = new DefaultComboBoxModel<>();

		for (AudioDevice device : playbackDevices) {
			model.addElement(device.getName());
		}

		SwingUtils.invoke(() -> {
			audioPlaybackDevicesCombo.setModel(model);
		});
	}

	@Override
	public void setAudioCaptureNoiseSuppressionLevel(ObjectProperty<NoiseSuppressionLevel> level) {
		SwingUtils.invoke(() -> {
			DefaultComboBoxModel<NoiseSuppressionLevel> model = new DefaultComboBoxModel<>();
			model.addAll(List.of(NoiseSuppressionLevel.values()));

			noiseSuppressionCombo.setModel(model);

			SwingUtils.bindBidirectional(noiseSuppressionCombo, level);
		});
	}

	@Override
	public void setAudioCaptureLevel(double value) {
		SwingUtils.invoke(() -> levelMeter.setLevel(value));
	}

	@Override
	public void bindAudioCaptureLevel(FloatProperty level) {
		var levelProperty = new ConvertibleNumberProperty<>(level,
				new FloatIntegerConverter(100));

		SwingUtils.bindBidirectional(micVolumeSlider, levelProperty);
	}

	@Override
	public void bindAudioPlaybackLevel(DoubleProperty level) {
		var levelProperty = new ConvertibleNumberProperty<>(level,
				new DoubleIntegerConverter(100));

		SwingUtils.bindBidirectional(speakerVolumeSlider, levelProperty);
	}

	@Override
	public void bindTestCaptureEnabled(BooleanProperty enable) {
		boolean enabled = enable.get();

		if (!enabled) {
			SwingUtils.invoke(() -> {
				testCaptureButton.setSelected(false);
				testCaptureButton.setEnabled(false);
			});
		}

		enable.addListener((o, oldValue, newValue) -> testCaptureButton.setEnabled(newValue));
	}

	@Override
	public void bindTestPlaybackEnabled(BooleanProperty enable) {
		boolean enabled = enable.get();

		if (!enabled) {
			SwingUtils.invoke(() -> {
				playCaptureButton.setSelected(false);
				playCaptureButton.setEnabled(false);
			});
		}

		enable.addListener((observable, oldValue, newValue) -> playCaptureButton.setEnabled(newValue));
	}

	@Override
	public void setOnTestCapture(BooleanProperty recordProperty) {
		SwingUtils.bindBidirectional(testCaptureButton, recordProperty);
	}

	@Override
	public void setOnTestCapturePlayback(BooleanProperty playProperty) {
		SwingUtils.bindBidirectional(playCaptureButton, playProperty);
	}

	@Override
	public void setOnTestSpeakerPlayback(BooleanProperty playProperty) {
		SwingUtils.bindBidirectional(testSpeakerSoundButton, playProperty);
	}

	@Override
	public void setOnAdjustAudioCaptureLevel(Action action) {
		SwingUtils.bindAction(adjustAudioInputLevelButton, action);
	}

	@Override
	public void setOnViewVisible(ConsumerAction<Boolean> action) {
		this.viewVisibleAction = action;
	}

	@Override
	public void setOnClose(Action action) {
		SwingUtils.bindAction(closeButton, action);
	}

	@Override
	public void setOnReset(Action action) {
		SwingUtils.bindAction(resetButton, action);
	}

	@ViewPostConstruct
	private void initialize() {
		noiseSuppressionCombo.setRenderer(new NoiseSuppressionLevelRenderer(
				resources, "sound.settings.noise.suppression."));

		levelMeter.setPreferredSize(UIScale.scale(levelMeter.getPreferredSize()));

		addAncestorListener(new AncestorListener() {

			@Override
			public void ancestorAdded(AncestorEvent event) {
				executeAction(viewVisibleAction, true);
			}

			@Override
			public void ancestorRemoved(AncestorEvent event) {
				executeAction(viewVisibleAction, false);
			}

			@Override
			public void ancestorMoved(AncestorEvent event) {
			}
		});
	}
}

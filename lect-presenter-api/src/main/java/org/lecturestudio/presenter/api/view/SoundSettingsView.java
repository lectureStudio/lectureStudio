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

package org.lecturestudio.presenter.api.view;

import org.lecturestudio.core.audio.AudioProcessingSettings.NoiseSuppressionLevel;
import org.lecturestudio.core.audio.device.AudioDevice;
import org.lecturestudio.core.beans.BooleanProperty;
import org.lecturestudio.core.beans.DoubleProperty;
import org.lecturestudio.core.beans.FloatProperty;
import org.lecturestudio.core.beans.ObjectProperty;
import org.lecturestudio.core.beans.StringProperty;
import org.lecturestudio.core.view.Action;
import org.lecturestudio.core.view.ConsumerAction;

public interface SoundSettingsView extends SettingsBaseView {

	void setViewEnabled(boolean enabled);

	void setAudioCaptureDevice(StringProperty captureDeviceName);

	void setAudioCaptureDevices(AudioDevice[] captureDevices);

	void setAudioPlaybackDevice(StringProperty playbackDeviceName);

	void setAudioPlaybackDevices(AudioDevice[] playbackDevices);

	void bindAudioCaptureNoiseSuppressionLevelEnabled(BooleanProperty enable);

	void setAudioCaptureNoiseSuppressionLevel(ObjectProperty<NoiseSuppressionLevel> level);

	void setAudioCaptureLevel(double value);

	void bindAudioCaptureLevel(DoubleProperty levelProperty);

	void bindAudioPlaybackLevel(DoubleProperty levelProperty);

	void bindTestCaptureEnabled(BooleanProperty enable);

	void bindTestPlaybackEnabled(BooleanProperty enable);

	void setOnTestCapture(BooleanProperty recordProperty);

	void setOnTestCapturePlayback(BooleanProperty playProperty);

	void setOnTestSpeakerPlayback(BooleanProperty playProperty);

	void setOnAdjustAudioCaptureLevel(Action action);

	void setOnViewVisible(ConsumerAction<Boolean> action);

}

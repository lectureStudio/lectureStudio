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

package org.lecturestudio.core.audio;

import org.lecturestudio.core.Executable;
import org.lecturestudio.core.ExecutableStateListener;
import org.lecturestudio.core.audio.sink.AudioSink;

/**
 * An AudioRecorder manages the audio resources to record audio from an audio
 * input device, e.g. a microphone.
 *
 * @author Alex Andres
 */
public interface AudioRecorder extends Executable {

	/**
	 * Sets the device name of the audio recording device which will capture
	 * audio for this recorder.
	 *
	 * @param deviceName The audio capture device name.
	 */
	void setAudioDeviceName(String deviceName);

	/**
	 * Sets the {@code AudioSink} that will receive the captured audio samples.
	 *
	 * @param sink The audio sink to set.
	 */
	void setAudioSink(AudioSink sink);

	/**
	 * Sets the recording audio volume. The value must be in the range of
	 * [0,1].
	 *
	 * @param volume The recording audio volume.
	 */
	void setAudioVolume(double volume);

	/**
	 * Get audio processing statistics. This method will only return valid
	 * statistics if {@link #setAudioProcessingSettings} has been called prior
	 * recording.
	 *
	 * @return The audio processing statistics.
	 */
	AudioProcessingStats getAudioProcessingStats();

	/**
	 * Sets which software audio processing filters to be applied to recorded
	 * audio samples.
	 *
	 * @param settings The {@code AudioProcessingSettings} to be applied.
	 */
	void setAudioProcessingSettings(AudioProcessingSettings settings);

	/**
	 * Add an {@code ExecutableStateListener} to this player.
	 *
	 * @param listener The listener to add.
	 */
	void addStateListener(ExecutableStateListener listener);

	/**
	 * Removes an {@code ExecutableStateListener} from this player.
	 *
	 * @param listener The listener to remove.
	 */
	void removeStateListener(ExecutableStateListener listener);

}

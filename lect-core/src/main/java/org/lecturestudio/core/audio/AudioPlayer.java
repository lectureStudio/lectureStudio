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
import org.lecturestudio.core.audio.source.AudioSource;

/**
 * An AudioPlayer manages the audio resources to play audio on an audio output
 * device, e.g. a speaker or headset.
 *
 * @author Alex Andres
 */
public interface AudioPlayer extends Executable {

	/**
	 * Sets the device name of the audio playback device which will play audio
	 * for this player.
	 *
	 * @param deviceName The audio output device name.
	 */
	void setAudioDeviceName(String deviceName);

	/**
	 * Sets the {@code AudioSource} that will read the audio samples to play.
	 *
	 * @param source The audio source to set.
	 */
	void setAudioSource(AudioSource source);

	/**
	 * Sets the recording audio volume. The value must be in the range of
	 * [0,1].
	 *
	 * @param volume The recording audio volume.
	 */
	void setAudioVolume(double volume);

	/**
	 * Set the playback progress listener.
	 *
	 * @param listener The listener to set.
	 */
	void setAudioProgressListener(AudioPlaybackProgressListener listener);

	/**
	 * Jump to the specified time position in the audio playback stream.
	 *
	 * @param timeMs The absolute time in milliseconds to jump to.
	 *
	 * @throws Exception If the playback stream failed to read the start of the
	 *                   specified position.
	 */
	void seek(int timeMs) throws Exception;

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

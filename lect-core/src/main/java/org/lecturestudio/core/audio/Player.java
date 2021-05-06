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

package org.lecturestudio.core.audio;

import org.lecturestudio.core.Executable;
import org.lecturestudio.core.ExecutableStateListener;

/**
 * Common interface to provide a consistent mechanism for media players.
 *
 * @author Alex Andres
 */
public interface Player extends Executable {

	/**
	 * Set the audio volume for playback. The volume value must be in the range of [0,1].
	 *
	 * @param volume The new volume value.
	 */
	void setVolume(float volume);

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
	 * Set the playback progress listener.
	 *
	 * @param listener The listener to set.
	 */
	void setProgressListener(AudioPlaybackProgressListener listener);

	/**
	 * Set the state listener.
	 *
	 * @param listener The listener to set.
	 */
	void setStateListener(ExecutableStateListener listener);

}

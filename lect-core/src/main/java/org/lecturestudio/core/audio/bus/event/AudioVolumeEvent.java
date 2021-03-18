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

package org.lecturestudio.core.audio.bus.event;

import org.lecturestudio.core.bus.event.BusEvent;

/**
 * This event is published when the audio volume of an audio capture device or
 * an audio playback device should be set.
 *
 * @author Alex Andres
 */
public class AudioVolumeEvent extends BusEvent {

	/** The audio volume in the range of [0,1]. */
	private float volume;


	/**
	 * Create a AudioVolumeEvent instance with the specified audio volume value.
	 * The value must be in the range of [0,1].
	 *
	 * @param volume The audio volume.
	 */
	public AudioVolumeEvent(float volume) {
		this.volume = volume;
	}

	/**
	 * Get the audio volume.
	 *
	 * @return the audio volume.
	 */
	public float getVolume() {
		return volume;
	}

}

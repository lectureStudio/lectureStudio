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
 * This event is published when an audio capture device has received audio data
 * and computed the audio signal level for the input data.
 *
 * @author Alex Andres
 */
public class AudioSignalEvent extends BusEvent {

	/** The audio signal level in the range of [0,1]. */
	private double value;


	/**
	 * Create a AudioSignalEvent instance with the specified audio signal level
	 * value. The value must be in the range of [0,1].
	 *
	 * @param value The audio signal level.
	 */
	public AudioSignalEvent(double value) {
		setSignalValue(value);
	}

	/**
	 * Set audio signal level value. The value must be in the range of [0,1].
	 *
	 * @param value The audio signal level.
	 */
	public void setSignalValue(double value) {
		this.value = value;
	}

	/**
	 * Get the audio signal level.
	 *
	 * @return the audio signal level.
	 */
	public double getSignalValue() {
		return value;
	}

}

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

import org.lecturestudio.core.audio.device.AudioDevice;

/**
 * This device listener receives device change events, each time an audio device
 * is connected or disconnected to or from the system.
 *
 * @author Alex Andres
 */
public interface AudioDeviceChangeListener {

	/**
	 * An audio device has been connected to the system.
	 *
	 * @param device The connected device.
	 */
	void deviceConnected(AudioDevice device);

	/**
	 * An audio device has been disconnected from the system.
	 *
	 * @param device The disconnected device.
	 */
	void deviceDisconnected(AudioDevice device);

}

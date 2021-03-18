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
 * This event is published when an audio device has been connected or
 * disconnected.
 *
 * @author Alex Andres
 */
public class AudioDeviceHotplugEvent extends BusEvent {

	/**
	 * The event type indicating what kind of the audio device event occurred.
	 */
	public enum Type {
		/** An audio device has been connected. */
		Connected,

		/** An audio device has been disconnected. */
		Disconnected
	}


	/** The audio device name. */
	private String name;

	/** The event type. */
	private Type type;


	/**
	 * Create a new AudioDeviceHotplugEvent with the given audio device name and
	 * the event type.
	 *
	 * @param name The audio device name.
	 * @param type The event type.
	 */
	public AudioDeviceHotplugEvent(String name, Type type) {
		this.name = name;
		this.type = type;
	}

	/**
	 * Get the audio device name.
	 *
	 * @return the audio device name.
	 */
	public String getDeviceName() {
		return name;
	}

	/**
	 * Get the event type indicating what kind of the audio device event
	 * occurred.
	 *
	 * @return the event type.
	 */
	public Type getType() {
		return type;
	}

}

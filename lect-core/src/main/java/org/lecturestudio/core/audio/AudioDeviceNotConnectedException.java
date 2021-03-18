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

import org.lecturestudio.core.ExecutableException;

/**
 * This exception is thrown to indicate a problem while operating with an audio
 * device. Throwing this exception should be considered fatal to the operation
 * of the application.
 *
 * @author Alex Andres
 */
public class AudioDeviceNotConnectedException extends ExecutableException {

	private final String deviceName;


	/**
	 * Construct a new AudioDeviceNotConnectedException with no other
	 * information.
	 *
	 * @param deviceName The name of the device.
	 */
	public AudioDeviceNotConnectedException(String deviceName) {
		super();

		this.deviceName = deviceName;
	}

	/**
	 * Construct a new AudioDeviceNotConnectedException with the specified
	 * message.
	 *
	 * @param message    A Message describing this exception.
	 * @param deviceName The name of the device.
	 */
	public AudioDeviceNotConnectedException(String message, String deviceName) {
		super(message);

		this.deviceName = deviceName;
	}

	/**
	 * Construct a new AudioDeviceNotConnectedException with the specified
	 * formatted message.
	 *
	 * @param message    A Message describing this exception.
	 * @param deviceName The name of the device.
	 * @param args       Arguments used in the formatted message.
	 */
	public AudioDeviceNotConnectedException(String message, String deviceName,
			Object... args) {
		super(String.format(message, args));

		this.deviceName = deviceName;
	}

	/**
	 * Construct a new AudioDeviceNotConnectedException with the specified
	 * throwable.
	 *
	 * @param throwable  A Throwable that caused this exception.
	 * @param deviceName The name of the device.
	 */
	public AudioDeviceNotConnectedException(Throwable throwable,
			String deviceName) {
		super(throwable);

		this.deviceName = deviceName;
	}

	/**
	 * Construct a new AudioDeviceNotConnectedException with the specified
	 * message and throwable.
	 *
	 * @param message    A Message describing this exception.
	 * @param throwable  A Throwable that caused this exception.
	 * @param deviceName The name of the device.
	 */
	public AudioDeviceNotConnectedException(String message, Throwable throwable,
			String deviceName) {
		super(message, throwable);

		this.deviceName = deviceName;
	}

	/**
	 * Construct a new AudioDeviceNotConnectedException with the specified
	 * formatted message and provided throwable.
	 *
	 * @param message    A Message describing this exception.
	 * @param throwable  A Throwable that caused this exception.
	 * @param deviceName The name of the device.
	 * @param args       Arguments used in the formatted message.
	 */
	public AudioDeviceNotConnectedException(String message, Throwable throwable,
			String deviceName, Object... args) {
		super(String.format(message, args), throwable);

		this.deviceName = deviceName;
	}

	/**
	 * @return The name of the device that is not connected.
	 */
	public String getDeviceName() {
		return deviceName;
	}
}

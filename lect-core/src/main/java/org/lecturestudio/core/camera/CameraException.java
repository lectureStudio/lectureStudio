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

package org.lecturestudio.core.camera;

import java.io.IOException;

/**
 * The CameraException is thrown whenever a camera failed to function.
 *
 * @author Alex Andres
 */
public class CameraException extends IOException {

	private static final long serialVersionUID = 7880817823440802127L;


	/**
	 * Create an CameraException with the specified detail message.
	 *
	 * @param message The error detail message.
	 */
	public CameraException(String message) {
		super(message);
	}

	/**
	 * Create an CameraException with the specified detail message and cause.
	 *
	 * @param message The error detail message.
	 * @param cause   The cause.
	 */
	public CameraException(String message, Throwable cause) {
		super(message, cause);
	}

}

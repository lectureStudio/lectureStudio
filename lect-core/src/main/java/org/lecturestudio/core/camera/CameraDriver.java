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

/**
 * Common interface to provide a consistent mechanism for the implementation of
 * camera drivers. The camera driver retrieves connected cameras to the system
 * and provides access to the cameras for video capturing.
 *
 * @author Alex Andres
 */
public interface CameraDriver {

	/**
	 * Retrieve all connected cameras to the system.
	 *
	 * @return an array of all connected cameras.
	 */
	Camera[] getCameras();

}

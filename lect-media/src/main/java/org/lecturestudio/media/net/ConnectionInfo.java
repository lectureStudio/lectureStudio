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

package org.lecturestudio.media.net;

import org.lecturestudio.web.api.model.Classroom;

/**
 * The {@code ConnectionInfo} is a wrapper for a classroom connection setup.
 * 
 * @author Alex Andres
 */
public class ConnectionInfo {

	/**
	 * The classroom to connect to.
	 */
	private final Classroom classroom;

	private boolean showCamera = false;


	/**
	 * Creates a new {@link ConnectionInfo} with specified parameters.
	 * 
	 * @param classroom The classroom to connect to.
	 * @param showCamera True if open camera stream on joining the classroom.
	 */
	public ConnectionInfo(Classroom classroom, boolean showCamera) {
		this.classroom = classroom;
		this.showCamera = showCamera;
	}

	/**
	 * Returns the classroom to connect to.
	 * 
	 * @return the Classroom.
	 */
	public Classroom getClassroom() {
		return classroom;
	}

	public boolean showCamera() {
		return showCamera;
	}

	@Override
	public String toString() {
		return getClass() + ": " + classroom + " Show camera: " + showCamera;
	}

}

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

package org.lecturestudio.web.api.janus;

/**
 * A listener observing the {@link JanusStateHandler} connection state.
 *
 * @author Alex Andres
 */
public interface JanusStateHandlerListener {

	/**
	 * Invoked when the WebRTC peer connection is up and sending or receiving
	 * media.
	 */
	void connected();

	/**
	 * Invoked when the WebRTC peer connection is closed and does not send or
	 * receive media anymore.
	 */
	void disconnected();

	/**
	 * Invoked whenever an error during state handling has occurred. This method
	 * can be invoked multiple times without blocking or terminating the current
	 * state.
	 *
	 * @param throwable Describing the cause of the error.
	 */
	void error(Throwable throwable);

}

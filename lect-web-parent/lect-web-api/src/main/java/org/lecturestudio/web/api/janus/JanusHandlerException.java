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
 * Exception thrown by a {@link JanusStateHandler} that manages WebRTC peer
 * connections. Two roles of {@code JanusStateHandler} are implemented: a
 * publisher and a subscriber. Each role is defined by {@link Type} and must be
 * used with this exception in order to distinguish which one caused it.
 *
 * @author Alex Andres
 */
public class JanusHandlerException extends Exception {

	/**
	 * Defines types of a {@link JanusStateHandler}.
	 */
	public enum Type {

		/**
		 * Active participant who publishes media during a streaming session.
		 */
		PUBLISHER,

		/**
		 * Passive participant who receives media during a streaming session.
		 */
		SUBSCRIBER;

	}



	private final Type type;


	/**
	 * Create a new JanusHandlerException with the given handler type and the
	 * cause for this exception.
	 *
	 * @param type  The {@code JanusStateHandler} type that thrown this
	 *              exception.
	 * @param cause The cause for this exception.
	 */
	public JanusHandlerException(Type type, Throwable cause) {
		super(cause);

		this.type = type;
	}

	/**
	 * @return The {@code JanusStateHandler} type that thrown this exception.
	 */
	public Type getType() {
		return type;
	}
}

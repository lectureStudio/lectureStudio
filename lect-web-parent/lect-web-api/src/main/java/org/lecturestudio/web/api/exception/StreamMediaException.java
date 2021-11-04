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

package org.lecturestudio.web.api.exception;

import org.lecturestudio.core.net.MediaType;

/**
 * Exception thrown when a type of media cannot be used for a streaming session.
 *
 * @author Alex Andres
 */
public class StreamMediaException extends Exception {

	private final MediaType mediaType;


	/**
	 * Creates a new StreamMediaException with the media type that is not
	 * working and the cause for this exception,
	 *
	 * @param mediaType The type of media that is not working for streaming.
	 * @param cause     The cause for this exception.
	 */
	public StreamMediaException(MediaType mediaType, Throwable cause) {
		super(cause);

		this.mediaType = mediaType;
	}

	/**
	 * @return The type of media that is not working for streaming.
	 */
	public MediaType getMediaType() {
		return mediaType;
	}
}

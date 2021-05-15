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

package org.lecturestudio.core.codec;

import java.awt.image.BufferedImage;
import java.nio.ByteBuffer;

/**
 * A {@link VideoEncoder} encodes uncompressed video frames to the encoder specific bit
 * stream and provides encoder specific statistics.
 *
 * @author Alex Andres
 */
public interface VideoEncoder {

	/**
	 * Encode the provided uncompressed video frame.
	 *
	 * @param image The uncompressed video frame.
	 *
	 * @return The encoded bit stream stored in the {@link ByteBuffer}.
	 */
	ByteBuffer encode(BufferedImage image);

	/**
	 * Get the timestamp of the last encoded video frame.
	 *
	 * @return the timestamp of the last encoded video frame.
	 */
	long getTimestamp();

}

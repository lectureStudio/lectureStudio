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

import java.nio.ByteBuffer;

/**
 * A VideoDecoder decodes video frames to uncompressed images and provides
 * decoder specific statistics.
 *
 * @author Alex Andres
 */
public interface VideoDecoder {

	/**
	 * Decode a video frame buffer to an uncompressed image.
	 *
	 * @param data The buffer that contains the video frame to decode.
	 */
	void decode(ByteBuffer data);

	/**
	 * Get the number of frames decoded per second.
	 *
	 * @return the number of frames decoded per second.
	 */
	float getFPS();

	/**
	 * Get the bitrate of uncompressed images. The number is expressed in kbps.
	 *
	 * @return the bitrate of uncompressed images.
	 */
	float getBitrate();

	/**
	 * Get the total number of bytes the decoder has processed.
	 *
	 * @return the total number of bytes the decoder has processed.
	 */
	long getTotalBytesReceived();

}

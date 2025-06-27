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

package org.lecturestudio.media.video;

import java.awt.image.BufferedImage;
import java.io.IOException;

import org.lecturestudio.core.Executable;

/**
 * Interface for a video muxer that combines video and audio frames into a single output.
 * Extends Executable to provide start and stop functionality for the muxing process.
 *
 * @author Alex Andres
 */
public interface VideoMuxer extends Executable {

	/**
	 * Adds a video frame to the muxing process.
	 *
	 * @param image The BufferedImage representing the video frame to add.
	 *
	 * @throws IOException If an I/O error occurs during the muxing process.
	 */
	void addVideoFrame(BufferedImage image) throws IOException;

	/**
	 * Adds audio data to the muxing process.
	 *
	 * @param samples The audio sample data as a byte array.
	 * @param offset  The offset in the sample array where the data starts.
	 * @param length  The number of bytes to use from the sample array.
	 *
	 * @throws IOException If an I/O error occurs during the muxing process.
	 */
	void addAudioFrame(byte[] samples, int offset, int length) throws IOException;

}

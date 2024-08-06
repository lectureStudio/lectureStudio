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

import org.bytedeco.javacv.Frame;

/**
 * Converts video frames coming from decoded video into the desired image type.
 *
 * @param <T> The converted output type.
 *
 * @author Alex Andres
 */
public interface VideoFrameConverter<T> {

	/**
	 * Converts the provided video frame into the specified image type.
	 *
	 * @param frame The video frame to convert.
	 *
	 * @return The frame converted into the desired image type.
	 *
	 * @throws Exception If the frame could not be converted.
	 */
	T convert(Frame frame) throws Exception;

	/**
	 * Release resources allocated by this converter.
	 */
	void dispose();

}

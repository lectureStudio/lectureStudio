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

package org.lecturestudio.core.audio.filter;

/**
 * An {@link AudioFilter} manipulates audio samples. Audio filters are meant to be used
 * in a non-invasive manner, e.g. with input streams.
 *
 * @author Alex Andres
 */
public interface AudioFilter {

	/**
	 * Filter the input data in-place.
	 *
	 * @param data   The audio samples.
	 * @param offset The data offset.
	 * @param length The length of data to process.
	 */
	void process(byte[] data, int offset, int length);

}

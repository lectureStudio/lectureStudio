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

package org.lecturestudio.core.io;

public class PlaybackData<T> {

	private final T data;

	private final long timestamp;

	/**
	 * Creates a new instance of {@link PlaybackData} with the specified data and timestamp.
	 *
	 * @param data The data.
	 * @param timestamp The timestamp.
	 */
	public PlaybackData(T data, long timestamp) {
		this.data = data;
		this.timestamp = timestamp;
	}

	/**
	 * Get the data.
	 *
	 * @return The data.
	 */
	public T getData() {
		return data;
	}

	/**
	 * Get the timestamp.
	 *
	 * @return The timestamp.
	 */
	public long getTimestamp() {
		return timestamp;
	}

}

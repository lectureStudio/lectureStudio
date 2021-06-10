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

package org.lecturestudio.core.net;

public final class Sync {

	private static long streamStart = 0;


	/**
	 * Set a new value for {@link #streamStart}.
	 *
	 * @param start The new value for {@link #streamStart}.
	 */
	public static void setStreamStart(long start) {
		streamStart = start;
	}

	/**
	 * Get the value of {@link #streamStart}.
	 *
	 * @return The value of {@link #streamStart}.
	 */
	public static long getStreamStart() {
		return streamStart;
	}

	/**
	 * Get the timestamp.
	 *
	 * @return The timestamp.
	 */
	public static long getTimestamp() {
		return System.currentTimeMillis() - streamStart;
	}

}

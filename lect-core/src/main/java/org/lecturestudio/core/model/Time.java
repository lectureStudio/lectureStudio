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

package org.lecturestudio.core.model;

public class Time {

	/** The number of milliseconds. */
	private long timeMillis;

	/** Specifies whether the milliseconds should be shown.  */
	private boolean showMillis;

	/**
	 * Create a new {@link Time} with {@code 0} as {@link #timeMillis} and {@code false} as {@link #showMillis}.
	 */
	public Time() {
		this(0, false);
	}

	/**
	 * Create a new {@link Time} with the specified number of milliseconds.
	 * ({@link #showMillis} will be set to {@code false})
	 *
	 * @param millis The number of milliseconds.
	 */
	public Time(long millis) {
		this(millis, false);
	}

	/**
	 * Create a new {@link Time} with the specified number of milliseconds and
	 * a boolean that specifies whether the milliseconds should be shown.
	 *
	 * @param millis The number of milliseconds.
	 * @param showMillis The boolean that specifies whether the milliseconds should be shown.
	 */
	public Time(long millis, boolean showMillis) {
		this.timeMillis = millis;
		this.showMillis = showMillis;
	}

	/**
	 * Set a new number of milliseconds.
	 *
	 * @param millis The number of milliseconds.
	 */
	public void setMillis(long millis) {
		this.timeMillis = millis;
	}

	/**
	 * Get the number of milliseconds.
	 *
	 * @return The number of milliseconds.
	 */
	public long getMillis() {
		return timeMillis;
	}

	/**
	 * Set a new boolean that specifies whether the milliseconds should be shown.
	 *
	 * @param show The new boolean.
	 */
	public void setShowMillis(boolean show) {
		this.showMillis = show;
	}

	@Override
	public String toString() {
		long time = timeMillis;

		int millis = (int) (time % 1000);
		time /= 1000;
		int seconds = (int) (time % 60);
		time /= 60;
		int minutes = (int) (time % 60);
		time /= 60;
		int hours = (int) (time);

		String str;

		if (showMillis) {
			str = String.format("%01d:%02d:%02d.%03d", hours, minutes, seconds, millis);
		}
		else {
			str = String.format("%01d:%02d:%02d", hours, minutes, seconds);
		}

		return str;
	}

}

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

package org.lecturestudio.core.recording;

import org.lecturestudio.core.ExecutableBase;
import org.lecturestudio.core.ExecutableException;

/**
 * Abstract class for executing recording events with capabilities for navigation
 * through recording timeline and pages.
 *
 * @author Alex Andres
 */
public abstract class EventExecutor extends ExecutableBase {

	/**
	 * Gets the elapsed time of the current recording playback.
	 *
	 * @return The elapsed time in milliseconds.
	 */
	public abstract long getElapsedTime();

	/**
	 * Gets the page number at a specified time position in the recording.
	 *
	 * @param timeMillis The time position in milliseconds.
	 *
	 * @return The corresponding page number.
	 */
	public abstract int getPageNumber(int timeMillis);

	/**
	 * Seeks to a specific time position in the recording.
	 *
	 * @param timeMillis The time position in milliseconds to seek to.
	 *
	 * @return The page number after seeking.
	 *
	 * @throws ExecutableException If seeking operation fails.
	 */
	public abstract int seekByTime(int timeMillis) throws ExecutableException;

	/**
	 * Seeks to a specific page in the recording.
	 *
	 * @param pageNumber The page number to seek to.
	 *
	 * @return The time position after seeking, in milliseconds.
	 *
	 * @throws ExecutableException If seeking operation fails.
	 */
	public abstract Integer seekByPage(int pageNumber) throws ExecutableException;

	/**
	 * Executes the recording events.
	 *
	 * @throws Exception If execution of events fails.
	 */
	protected abstract void executeEvents() throws Exception;

}

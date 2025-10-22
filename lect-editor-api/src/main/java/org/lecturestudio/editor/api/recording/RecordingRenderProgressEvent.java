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

package org.lecturestudio.editor.api.recording;

import org.lecturestudio.core.model.Time;

/**
 * Event carrying progress information for a recording render operation.
 * Contains current and total render times as well as page progress.
 *
 * @author Alex Andres
 */
public class RecordingRenderProgressEvent {

	/** Current render time position. */
	private Time current;

	/** Total duration of the render. */
	private Time total;

	/** Current page number in the rendered output. */
	private int pageNumber;

	/** Total number of pages in the rendered output. */
	private int pageCount;


	/**
	 * Returns the current render time.
	 *
	 * @return current time position.
	 */
	public Time getCurrentTime() {
		return current;
	}

	/**
	 * Sets the current render time.
	 *
	 * @param current current time position.
	 */
	public void setCurrentTime(Time current) {
		this.current = current;
	}

	/**
	 * Returns the total render time.
	 *
	 * @return total duration.
	 */
	public Time getTotalTime() {
		return total;
	}

	/**
	 * Sets the total render time.
	 *
	 * @param total total duration.
	 */
	public void setTotalTime(Time total) {
		this.total = total;
	}

	/**
	 * Returns the current page number.
	 *
	 * @return page number.
	 */
	public int getPageNumber() {
		return pageNumber;
	}

	/**
	 * Sets the current page number.
	 *
	 * @param pageNumber current page number.
	 */
	public void setPageNumber(int pageNumber) {
		this.pageNumber = pageNumber;
	}

	/**
	 * Returns the total page count.
	 *
	 * @return page count.
	 */
	public int getPageCount() {
		return pageCount;
	}

	/**
	 * Sets the total page count.
	 *
	 * @param pageCount total number of pages.
	 */
	public void setPageCount(int pageCount) {
		this.pageCount = pageCount;
	}
}

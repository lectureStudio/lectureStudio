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

package org.lecturestudio.core.audio;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

/**
 * The synchronization state for media playback to keep different media players
 * in sync. The audio stream is considered to be the main stream and all other
 * media stream must be in sync with it.
 *
 * @author Alex Andres
 */
public final class SyncState {

	/** The current audio time in milliseconds. */
	private final AtomicLong audioTime = new AtomicLong(0);

	/** The current page number being processed. */
	private final AtomicInteger pageNumber = new AtomicInteger();

	/** The current event number being processed in the current page. */
	private final AtomicInteger eventNumber = new AtomicInteger();


	/**
	 * Set the current audio time in milliseconds of the audio main stream.
	 *
	 * @param time The audio time in milliseconds.
	 */
	public void setAudioTime(long time) {
		audioTime.set(time);
	}

	/**
	 * Get current audio time in milliseconds of the audio main stream.
	 *
	 * @return the audio time in milliseconds.
	 */
	public long getAudioTime() {
		return audioTime.get();
	}

	/**
	 * Get current event number being processed in the current page.
	 *
	 * @return The current event number.
	 */
	public int getEventNumber() {
		return eventNumber.get();
	}

	/**
	 * Set current event number being processed in the current page.
	 *
	 * @param number The new event number.
	 */
	public void setEventNumber(int number) {
		eventNumber.set(number);
	}

	/**
	 * Get current number of the page that is being processed.
	 *
	 * @return The current page number.
	 */
	public int getPageNumber() {
		return pageNumber.get();
	}

	/**
	 * Set current number of the page that is being processed.
	 *
	 * @param number The new page number.
	 */
	public void setPageNumber(int number) {
		pageNumber.set(number);
	}

	/**
	 * Reset the audio time, page number and event number to {@code 0}.
	 */
	public void reset() {
		audioTime.set(0);
		pageNumber.set(0);
		eventNumber.set(0);
	}

}

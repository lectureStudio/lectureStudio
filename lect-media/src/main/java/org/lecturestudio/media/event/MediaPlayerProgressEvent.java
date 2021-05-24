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

package org.lecturestudio.media.event;

import org.lecturestudio.core.bus.event.BusEvent;
import org.lecturestudio.core.model.Time;

public class MediaPlayerProgressEvent extends BusEvent {

	private Time current;
	private Time total;
	
	private int pageNumber;
	private int pageCount;

	private int prevEventNumber;
	private int eventNumber;
	
	
	public MediaPlayerProgressEvent(Time current, Time total, int pageNumber, int pageCount) {
		this.current = current;
		this.total = total;
		this.pageNumber = pageNumber;
		this.pageCount = pageCount;
	}

	public Time getCurrentTime() {
		return current;
	}
	
	public void setCurrentTime(Time current) {
		this.current = current;
	}

	public Time getTotalTime() {
		return total;
	}
	
	public void setTotalTime(Time total) {
		this.total = total;
	}

	public int getPageNumber() {
		return pageNumber;
	}

	public void setPageNumber(int pageNumber) {
		this.pageNumber = pageNumber;
	}
	
	public int getPageCount() {
		return pageCount;
	}

	public void setPageCount(int pageCount) {
		this.pageCount = pageCount;
	}

	public int getEventNumber() {
		return eventNumber;
	}

	public void setEventNumber(int eventNumber) {
		this.eventNumber = eventNumber;
	}

	public int getPrevEventNumber() {
		return prevEventNumber;
	}

	public void setPrevEventNumber(int prevEventNumber) {
		this.prevEventNumber = prevEventNumber;
	}
}

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
package org.lecturestudio.core.bus.event;

public abstract class BusEvent {

	/** The {@link EventProcessedHandler} instance. */
	private EventProcessedHandler processedHandler;

	/** The {@link EventErrorHandler} instance. */
	private EventErrorHandler errorHandler;


	/**
	 * Get the {@link #processedHandler}.
	 *
	 * @return The {@link #processedHandler}.
	 */
	public EventProcessedHandler getEventProcessedHandler() {
		return processedHandler;
	}

	/**
	 * Set the new {@link #processedHandler}.
	 *
	 * @param handler the new {@link EventProcessedHandler}.
	 */
	public void setEventProcessedHandler(EventProcessedHandler handler) {
		this.processedHandler = handler;
	}

	/**
	 * Get the {@link #errorHandler}.
	 *
	 * @return The {@link #errorHandler}.
	 */
	public EventErrorHandler getEventErrorHandler() {
		return errorHandler;
	}

	/**
	 * Set the new {@link #errorHandler}.
	 *
	 * @param errorHandler the new {@link EventErrorHandler}.
	 */
	public void setEventErrorHandler(EventErrorHandler errorHandler) {
		this.errorHandler = errorHandler;
	}

}

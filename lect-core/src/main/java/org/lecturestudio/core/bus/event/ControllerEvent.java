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

public abstract class ControllerEvent extends BusEvent {

	/** The data of the {@link ControllerEvent}. */
	private Object data;

	/** Boolean that indicates whether {@link ControllerEvent} is synchronous. */
	private boolean synchronous;


	/**
	 * Create the {@link ControllerEvent} with specified data.
	 *
	 * @param data The data.
	 */
	protected ControllerEvent(Object data) {
		this.data = data;
	}

	/**
	 * Get the value of {@link #synchronous}.
	 *
	 * @return {@code true} if {@link #synchronous} is {@code true}, otherwise {@code false}.
	 */
	public boolean isSynchronous() {
		return synchronous;
	}

	/**
	 * Set a new value for {@link #synchronous}.
	 *
	 * @param sync The new value.
	 */
	public void setSynchronous(boolean sync) {
		this.synchronous = sync;
	}

	/**
	 * Get the {@link #data}.
	 *
	 * @return The {@link #data}.
	 */
	public Object getData() {
		return data;
	}

}

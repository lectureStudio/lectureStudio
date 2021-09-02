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

import org.lecturestudio.core.app.configuration.DisplayConfiguration;

public class DisplayConfigEvent extends BusEvent {

	/** The display configuration */
	private DisplayConfiguration config;

	/**
	 * Create the {@link DisplayConfigEvent} with specified {@link DisplayConfiguration}.
	 *
	 * @param config The {@link DisplayConfiguration}.
	 */
	public DisplayConfigEvent(DisplayConfiguration config) {
		this.config = config;
	}

	/**
	 * Get the display configuration.
	 *
	 * @return The display configuration.
	 */
	public DisplayConfiguration getConfig() {
		return config;
	}

}

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

package org.lecturestudio.core.app.configuration;

import org.lecturestudio.core.geometry.Rectangle2D;

/**
 * The WindowConfiguration specifies window related properties of all opened
 * windows and dialogs.
 *
 * @author Alex Andres
 */
public class WindowConfiguration {

	/** The class of the window object. */
	private Class<?> windowClass;

	/**
	 * The device ID of the display device on which the window was recently
	 * shown.
	 */
	private String deviceId;

	/** The window bounds. */
	private Rectangle2D windowBounds;


	/**
	 * Obtain the class of the window object.
	 *
	 * @return the class of the window object.
	 */
	public Class<?> getWindowClass() {
		return windowClass;
	}

	/**
	 * Set the class of the window object.
	 *
	 * @param windowClass The class of the window object.
	 */
	public void setWindowClass(Class<?> windowClass) {
		this.windowClass = windowClass;
	}

	/**
	 * Obtain the device ID of the display device on which the window was
	 * recently shown.
	 *
	 * @return the device ID of the display device.
	 */
	public String getDeviceId() {
		return deviceId;
	}

	/**
	 * Set the device ID of the display device on which the window was recently
	 * shown.
	 *
	 * @param deviceId The device ID of the display device.
	 */
	public void setDeviceId(String deviceId) {
		this.deviceId = deviceId;
	}

	/**
	 * Obtain the window bounds.
	 *
	 * @return the window bounds.
	 */
	public Rectangle2D getWindowBounds() {
		return windowBounds;
	}

	/**
	 * Set the new window bounds.
	 *
	 * @param bounds The new window bounds.
	 */
	public void setWindowBounds(Rectangle2D bounds) {
		this.windowBounds = bounds;
	}

	@Override
	public int hashCode() {
		return 31 + ((windowClass == null) ? 0 : windowClass.hashCode());
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (getClass() != obj.getClass()) {
			return false;
		}

		WindowConfiguration other = (WindowConfiguration) obj;

		if (windowClass == null) {
			if (other.windowClass != null) {
				return false;
			}
		}
		else if (!windowClass.equals(other.windowClass)) {
			return false;
		}
		return true;
	}

}

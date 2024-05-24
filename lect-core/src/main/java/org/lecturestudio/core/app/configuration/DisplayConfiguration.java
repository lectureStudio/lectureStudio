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

import org.lecturestudio.core.beans.BooleanProperty;
import org.lecturestudio.core.beans.ObjectProperty;
import org.lecturestudio.core.geometry.Position;
import org.lecturestudio.core.graphics.Color;
import org.lecturestudio.core.util.ObservableArrayList;
import org.lecturestudio.core.util.ObservableList;

/**
 * The DisplayConfiguration specifies display related properties for the
 * application.
 *
 * @author Alex Andres
 */
public class DisplayConfiguration {

	/**
	 * The list of screen configurations that describe the properties of all
	 * connected displays.
	 */
	private final ObservableList<ScreenConfiguration> screens = new ObservableArrayList<>();

	/** The background color of windows that are shown on connected displays. */
	private final ObjectProperty<Color> backgroundColor = new ObjectProperty<>();

	/** The position of the IP address that is shown on connected displays. */
	private final ObjectProperty<Position> ipPosition = new ObjectProperty<>();

	/**
	 * Indicates whether to automatically enable connected displays when the
	 * application starts.
	 */
	private final BooleanProperty autostart = new BooleanProperty();

	/**
	 * Show a reminder notification, if desired, to activate connected displays.
	 */
	private final BooleanProperty notifyToActivate = new BooleanProperty(true);


	/**
	 * Obtain the observable list of screen configurations that describe the
	 * properties of all connected displays. Each time a new display is
	 * connected to the system this list should be updated in order to show the
	 * displays with the recently used properties.
	 *
	 * @return the observable list of screen configurations.
	 */
	public ObservableList<ScreenConfiguration> getScreens() {
		return screens;
	}

	/**
	 * Obtain the position of the IP address that is shown on connected displays.
	 *
	 * @return the position of the IP address.
	 *
	 * @see #setIpPosition(Position)
	 */
	public Position getIpPosition() {
		return ipPosition.get();
	}

	/**
	 * Set the new position of the IP address. The IP address must be the one of
	 * the system that runs the web services.
	 *
	 * @param position The new position to set.
	 */
	public void setIpPosition(Position position) {
		this.ipPosition.set(position);
	}

	/**
	 * Obtain the IP position property.
	 *
	 * @return the IP position property.
	 */
	public ObjectProperty<Position> ipPositionProperty() {
		return ipPosition;
	}

	/**
	 * Obtain the background color of windows that are shown on connected
	 * displays.
	 *
	 * @return the background color of the windows.
	 */
	public Color getBackgroundColor() {
		return backgroundColor.get();
	}

	/**
	 * Set the background color of windows that are shown on connected displays.
	 *
	 * @param color The new background color to set.
	 */
	public void setBackgroundColor(Color color) {
		this.backgroundColor.set(color);
	}

	/**
	 * Obtain the background color property.
	 *
	 * @return the background color property.
	 */
	public ObjectProperty<Color> backgroundColorProperty() {
		return backgroundColor;
	}

	/**
	 * Check whether to automatically enable connected displays when the
	 * application starts.
	 *
	 * @return {@code true} to automatically enable connected displays, otherwise {@code false}.
	 */
	public Boolean getAutostart() {
		return autostart.get();
	}

	/**
	 * Set whether to automatically enable connected displays when the
	 * application starts.
	 *
	 * @param enable True to automatically enable connected displays, false
	 *               otherwise.
	 */
	public void setAutostart(boolean enable) {
		this.autostart.set(enable);
	}

	/**
	 * Obtain the autostart property.
	 *
	 * @return the autostart property.
	 */
	public BooleanProperty autostartProperty() {
		return autostart;
	}

	/**
	 * Check whether to show a notification to activate connected displays when the
	 * presentation starts.
	 *
	 * @return {@code true} to notify to activate connected displays, otherwise {@code false}.
	 */
	public Boolean getNotifyToActivate() {
		return notifyToActivate.get();
	}

	/**
	 * Set whether to show a notification to activate connected displays when the
	 * presentation starts.
	 *
	 * @param notify True to notify to activate connected displays, false
	 *               otherwise.
	 */
	public void setNotifyToActivate(Boolean notify) {
		this.notifyToActivate.set(notify);
	}

	/**
	 * Obtain the notify-to-activate property.
	 *
	 * @return the notify-to-activate property.
	 */
	public BooleanProperty notifyToActivateProperty() {
		return notifyToActivate;
	}
}

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
import org.lecturestudio.core.view.Screen;

/**
 * The ScreenConfiguration specifies screen related properties of connected
 * displays.
 *
 * @author Alex Andres
 */
public class ScreenConfiguration {

	/** The screen object defining the screen bounds of the connected display. */
	private final ObjectProperty<Screen> screen = new ObjectProperty<>();

	/** Indicates whether the connected display should be activated or not. */
	private final BooleanProperty enabled = new BooleanProperty();


	/**
	 * Obtain screen property.
	 *
	 * @return screen property.
	 */
	public ObjectProperty<Screen> screenProperty() {
		return screen;
	}

	/**
	 * Obtain the associated screen information.
	 *
	 * @return the screen information.
	 */
	public Screen getScreen() {
		return screen.get();
	}

	/**
	 * Set the new Screen information.
	 *
	 * @param screen The new Screen to set.
	 */
	public void setScreen(Screen screen) {
		this.screen.set(screen);
	}

	/**
	 * Obtain enabled property.
	 *
	 * @return enabled property.
	 */
	public BooleanProperty enabledProperty() {
		return enabled;
	}

	/**
	 * Check whether the connected display should be activated or not.
	 *
	 * @return true to activate the display, false otherwise.
	 */
	public Boolean getEnabled() {
		return enabled.get();
	}

	/**
	 * Set whether the connected display should be activated or not.
	 *
	 * @param enabled True to activate the display, false otherwise.
	 */
	public void setEnabled(Boolean enabled) {
		this.enabled.set(enabled);
	}

}

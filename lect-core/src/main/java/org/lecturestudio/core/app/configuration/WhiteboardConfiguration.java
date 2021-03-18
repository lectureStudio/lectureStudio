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

import org.lecturestudio.core.beans.ObjectProperty;
import org.lecturestudio.core.graphics.Color;

/**
 * The WhiteboardConfiguration specifies whiteboard related properties.
 *
 * @author Alex Andres
 */
public class WhiteboardConfiguration {

	/** The background color of the whiteboard. */
	private final ObjectProperty<Color> backgroundColor = new ObjectProperty<>();


	/**
	 * Obtain background color of the whiteboard.
	 *
	 * @return the background color.
	 */
	public Color getBackgroundColor() {
		return backgroundColor.get();
	}

	/**
	 * Set the new background color of the whiteboard.
	 *
	 * @param color The new background color to set.
	 */
	public void setBackgroundColor(Color color) {
		this.backgroundColor.set(color);
	}

	/**
	 * Obtain background color property.
	 *
	 * @return background color property.
	 */
	public ObjectProperty<Color> backgroundColorProperty() {
		return backgroundColor;
	}

}

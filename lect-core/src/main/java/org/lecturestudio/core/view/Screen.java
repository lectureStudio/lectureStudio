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

package org.lecturestudio.core.view;

import java.awt.GraphicsDevice;
import java.util.Objects;
import java.util.StringJoiner;

import com.fasterxml.jackson.annotation.JsonIgnore;

import org.lecturestudio.core.geometry.Rectangle2D;

/**
 * The Screen defines screen-related properties of a connected display.
 *
 * @author Alex Andres
 */
public final class Screen {

	/** The screen bounds. */
	private final Rectangle2D bounds;

	/** The graphics device associated with this screen. */
	@JsonIgnore
	private final transient GraphicsDevice device;


	/**
	 * Create a new Screen instance with empty bounds.
	 */
	public Screen() {
		this(new Rectangle2D(), null);
	}

	/**
	 * Create a new Screen instance with the specified bounds.
	 *
	 * @param x      The x-coordinate of the screen bounds.
	 * @param y      The x-coordinate of the screen bounds.
	 * @param width  The width of the screen bounds.
	 * @param height The height of the screen bounds.
	 * @param device The graphics device associated with this screen.
	 */
	public Screen(int x, int y, int width, int height, GraphicsDevice device) {
		this(new Rectangle2D(x, y, width, height), device);
	}

	/**
	 * Create a new Screen instance with the specified bounds.
	 *
	 * @param bounds The bounding rectangle of this screen.
	 * @param device The graphics device associated with this screen.
	 */
	public Screen(Rectangle2D bounds, GraphicsDevice device) {
		this.bounds = bounds;
		this.device = device;
	}

	/**
	 * Get the bounding rectangle of this screen.
	 *
	 * @return the bounding rectangle of this screen.
	 */
	public Rectangle2D getBounds() {
		return new Rectangle2D(bounds);
	}

	/**
	 * Get the graphics device associated with this screen.
	 *
	 * @return the graphics device associated with this screen.
	 */
	public GraphicsDevice getDevice() {
		return device;
	}

	@Override
	public boolean equals(Object other) {
		if (this == other) {
			return true;
		}
		if (other == null || getClass() != other.getClass()) {
			return false;
		}

		Screen screen = (Screen) other;

		return Objects.equals(bounds, screen.bounds);
	}

	@Override
	public int hashCode() {
		return Objects.hash(bounds);
	}

	@Override
	public String toString() {
		return new StringJoiner(", ", getClass().getSimpleName() + "[", "]")
				.add("bounds=" + bounds).toString();
	}

}

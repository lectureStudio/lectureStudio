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

package org.lecturestudio.core.graphics;

public interface GraphicsContext {

	/**
	 * Fills the specified rectangle using the current fill paint.
	 * 
	 * @param x The X position of the upper left corner of the rectangle.
	 * @param y The Y position of the upper left corner of the rectangle.
	 * @param width The width of the rectangle.
	 * @param height The height of the rectangle.
	 */
	void fillRect(double x, double y, double width, double height);

	/**
	 * Restores the {@link GraphicsContext} state by setting the attributes to their
	 * value at the time when that state was pushed onto the stack.
	 */
	void restore();

	/**
	 * Saves the current {@link GraphicsContext} attributes state onto a stack.
	 */
	void save();

	/**
	 * Sets the current clipping area to a rectangle clip shape.
	 * Rendering operations have no effect outside of the clipping area.
	 *
	 * @param x The x coordinate of the clip rectangle.
	 * @param y The y coordinate of the clip rectangle.
	 * @param width The width of the clip rectangle.
	 * @param height The height of the clip rectangle.
	 */
	void setClip(double x, double y, double width, double height);

	/**
	 * Sets the current fill color attribute.
	 * 
	 * @param color The {@link Color} to be used by fill operations.
	 */
	void setFill(Color color);

	/**
	 * Concatenates the current transform with a scaling transformation.
	 * Subsequent rendering is resized according to the specified scaling factors relative to the previous scaling.
	 * 
	 * @param sx The factor by which x coordinates are multiplied.
	 * @param sy The factor by which y coordinates are multiplied.
	 */
	void scale(double sx, double sy);

	/**
	 * Concatenates the current transform with a translation transformation.
	 * Subsequent rendering is translated by the specified distance relative to the previous position.
	 * 
	 * @param tx The distance to translate along the x-axis.
	 * @param ty The distance to translate along the y-axis.
	 */
	void translate(double tx, double ty);

	/**
	 * Retrieves the specific graphics context implemented by the system.
	 * 
	 * @return The system graphics context.
	 */
	Object get();

}

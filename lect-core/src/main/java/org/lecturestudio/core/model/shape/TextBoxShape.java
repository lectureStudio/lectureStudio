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

package org.lecturestudio.core.model.shape;

import org.lecturestudio.core.geometry.Point2D;
import org.lecturestudio.core.graphics.Color;
import org.lecturestudio.core.text.TextAttributes;

public interface TextBoxShape<T> {

	/**
	 * Set new text.
	 *
	 * @param text The new text.
	 */
	void setText(String text);

	/**
	 * Set the new text attributes.
	 *
	 * @param attributes The new text attributes.
	 */
	void setTextAttributes(TextAttributes attributes);

	/**
	 * Set the new font color.
	 *
	 * @param color The new font color.
	 */
	void setTextColor(Color color);

	/**
	 * Set the new font.
	 *
	 * @param font The new font.
	 */
	void setFont(T font);

	/**
	 * Set the new location of the bounding rectangle of the shape.
	 *
	 * @param location The new location of the bounding rectangle of the shape.
	 */
	void setLocation(Point2D location);
	
}

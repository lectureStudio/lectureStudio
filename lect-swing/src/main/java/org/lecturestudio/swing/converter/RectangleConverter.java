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

package org.lecturestudio.swing.converter;

import java.awt.Rectangle;

import org.lecturestudio.core.beans.Converter;

public class RectangleConverter implements Converter<org.lecturestudio.core.geometry.Rectangle2D, Rectangle> {

	public static final RectangleConverter INSTANCE = new RectangleConverter();


	@Override
	public Rectangle to(org.lecturestudio.core.geometry.Rectangle2D rect) {
		return new Rectangle((int) rect.getX(), (int) rect.getY(), (int) rect.getWidth(), (int) rect.getHeight());
	}

	@Override
	public org.lecturestudio.core.geometry.Rectangle2D from(Rectangle rect) {
		return new org.lecturestudio.core.geometry.Rectangle2D(rect.getMinX(), rect.getMinY(), rect.getWidth(), rect.getHeight());
	}

}

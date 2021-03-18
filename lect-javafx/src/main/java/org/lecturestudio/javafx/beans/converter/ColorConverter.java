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

package org.lecturestudio.javafx.beans.converter;

import javafx.scene.paint.Color;

import org.lecturestudio.core.beans.Converter;

public class ColorConverter implements Converter<org.lecturestudio.core.graphics.Color, Color> {

	public static final ColorConverter INSTANCE = new ColorConverter();


	@Override
	public Color to(org.lecturestudio.core.graphics.Color color) {
		return Color.rgb(color.getRed(), color.getGreen(), color.getBlue(), color.getOpacity() / 255.0);
	}

	@Override
	public org.lecturestudio.core.graphics.Color from(Color color) {
		int r = (int) Math.round(color.getRed() * 255);
		int g = (int) Math.round(color.getGreen() * 255);
		int b = (int) Math.round(color.getBlue() * 255);
		int a = (int) Math.round(color.getOpacity() * 255);

		return new org.lecturestudio.core.graphics.Color(r, g, b, a);
	}

}

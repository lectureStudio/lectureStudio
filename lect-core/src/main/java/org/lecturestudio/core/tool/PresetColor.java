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

package org.lecturestudio.core.tool;

import org.lecturestudio.core.graphics.Color;

/**
 * Predefined tool colors.
 * 
 * @author Tobias
 */
public enum PresetColor {

	// Stroke colors
	PURPLE(new Color(135, 25, 224)),
	BLUE(new Color(0, 0, 255)),
	GREEN(new Color(0, 210, 0)),
	RED(new Color(255, 0, 0)),
	YELLOW(new Color(255, 255, 0)),
	BLACK(new Color(0, 0, 0)),

	// Highlighter colors
	BLUE_VIVID(new Color(33, 134, 235)),
	CYAN(new Color(72, 248, 248)),
	LIGHT_GREEN(new Color(145, 255, 21)),
	MAGENTA(new Color(255, 86, 255)),
	ORANGE(new Color(255, 209, 25)),
	PINK(new Color(255, 216, 249));


	private final Color color;


	PresetColor(Color rgb) {
		this.color = rgb;
	}

	public Color getColor() {
		return color;
	}

}

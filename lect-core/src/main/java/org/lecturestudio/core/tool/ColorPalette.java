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

import static java.util.Objects.nonNull;

import java.util.HashMap;
import java.util.Map;

import org.lecturestudio.core.graphics.Color;

public final class ColorPalette {

	private static final Map<ToolType, Map<Integer, Color>> paletteMap = new HashMap<>();
	
	static {
		PresetColor[] defaultColors = {
				PresetColor.LIGHT_GREEN,
				PresetColor.BLUE,
				PresetColor.GREEN,
				PresetColor.RED,
				PresetColor.YELLOW,
				PresetColor.BLACK,
				PresetColor.PURPLE
		};
		PresetColor[] alphaColors = {
				PresetColor.PINK,
				PresetColor.CYAN,
				PresetColor.LIGHT_GREEN,
				PresetColor.MAGENTA,
				PresetColor.YELLOW,
				PresetColor.ORANGE,
				PresetColor.BLUE_VIVID
		};
		
		paletteMap.put(ToolType.PEN, createPalette(defaultColors));
		paletteMap.put(ToolType.HIGHLIGHTER, createPalette(alphaColors));
		paletteMap.put(ToolType.POINTER, createPalette(defaultColors));
		paletteMap.put(ToolType.TEXT, createPalette(defaultColors));
		paletteMap.put(ToolType.TEXT_SELECTION, createPalette(alphaColors));
		paletteMap.put(ToolType.LINE, createPalette(defaultColors));
		paletteMap.put(ToolType.ARROW, createPalette(defaultColors));
		paletteMap.put(ToolType.RECTANGLE, createPalette(defaultColors));
		paletteMap.put(ToolType.ELLIPSE, createPalette(defaultColors));
	}
	
	public static Color getColor(ToolType type, int index) {
		Map<Integer, Color> palette = paletteMap.get(type);
		
		if (nonNull(palette) && palette.size() > index && index > -1) {
			return palette.get(index);
		}
		
		return null;
	}

	public static void setColor(ToolType type, Color color, int index) {
		Map<Integer, Color> palette = paletteMap.get(type);
		
		if (nonNull(palette)) {
			palette.put(index, color);
		}
	}
	
	public static boolean hasPalette(ToolType type) {
		Map<Integer, Color> palette = paletteMap.get(type);
		
		return nonNull(palette);
	}
	
	private static Map<Integer, Color> createPalette(PresetColor[] colors) {
		Map<Integer, Color> palette = new HashMap<>();
		
		for (int i = 0; i < colors.length; i++) {
			PresetColor bColor = colors[i];
			palette.put(i, bColor.getColor());
		}
		
		return palette;
	}
	
}

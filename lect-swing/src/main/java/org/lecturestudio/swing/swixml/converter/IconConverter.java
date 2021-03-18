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

package org.lecturestudio.swing.swixml.converter;

import javax.swing.ImageIcon;

import org.lecturestudio.swing.AwtResourceLoader;

import org.swixml.SwingEngine;
import org.swixml.converters.ImageIconConverter;
import org.swixml.dom.Attribute;

public class IconConverter extends ImageIconConverter {

	@Override
	public ImageIcon convert(String value, Class<?> type, final Attribute attr, SwingEngine<?> engine) {
		ImageIcon icon = null;
		Integer size = null;

		String[] parts = value.split(",");
		String iconPath = parts[0];

		if (parts.length > 1) {
			size = Integer.valueOf(parts[1].strip());
		}

		try {
			icon = (ImageIcon) AwtResourceLoader.getIcon(iconPath, size);
		}
		catch (Exception e) {
			// Intentionally empty
		}

		return icon;
	}

}

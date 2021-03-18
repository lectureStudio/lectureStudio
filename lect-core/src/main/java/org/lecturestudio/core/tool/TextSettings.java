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

import org.lecturestudio.core.text.Font;
import org.lecturestudio.core.text.TextAttributes;

public class TextSettings extends PaintSettings {

	private Font font;

	private TextAttributes attributes;


	public TextSettings() {

	}

	public TextSettings(TextSettings settings) {
		super(settings);

		setFont(settings.getFont());
		setTextAttributes(settings.getTextAttributes().clone());
	}

	public Font getFont() {
		return font;
	}

	public void setFont(Font font) {
		this.font = font;
	}

	public TextAttributes getTextAttributes() {
		return attributes;
	}

	public void setTextAttributes(TextAttributes attributes) {
		this.attributes = attributes;
	}

	public TextSettings clone() {
		TextSettings settings = new TextSettings();
		settings.setColor(getColor().clone());
		settings.setTextAttributes(attributes.clone());
		settings.setFont(font.clone());

		return settings;
	}

}

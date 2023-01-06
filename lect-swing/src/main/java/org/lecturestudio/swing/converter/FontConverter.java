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

import static java.util.Objects.nonNull;

import java.awt.Font;
import java.awt.font.TextAttribute;
import java.util.HashMap;
import java.util.Map;

import org.lecturestudio.core.beans.Converter;
import org.lecturestudio.core.text.FontPosture;
import org.lecturestudio.core.text.FontWeight;
import org.lecturestudio.core.text.TextAttributes;

public class FontConverter implements Converter<org.lecturestudio.core.text.Font, Font> {

	public static final FontConverter INSTANCE = new FontConverter();


	@Override
	public Font to(org.lecturestudio.core.text.Font font) {
		TextAttributes textAttributes = font.getTextAttributes();

		Map<TextAttribute, Object> attrs = new HashMap<>();
		attrs.put(TextAttribute.FAMILY, font.getFamilyName());
		attrs.put(TextAttribute.SIZE, font.getSize());
		attrs.put(TextAttribute.POSTURE, toAwtFontPosture(font.getPosture()));
		attrs.put(TextAttribute.WEIGHT, toAwtFontWeight(font.getWeight()));

		if (nonNull(textAttributes)) {
			attrs.put(TextAttribute.STRIKETHROUGH, textAttributes.isStrikethrough());
			attrs.put(TextAttribute.UNDERLINE, textAttributes.isUnderline() ? 0 : -1);
		}

		return new Font(attrs);
	}

	@Override
	public org.lecturestudio.core.text.Font from(Font font) {
		Map<TextAttribute, ?> attributes = font.getAttributes();

		TextAttributes textAttributes = new TextAttributes();
		textAttributes.setUnderline(attributes.get(TextAttribute.UNDERLINE) == TextAttribute.UNDERLINE_ON);
		textAttributes.setStrikethrough(attributes.get(TextAttribute.STRIKETHROUGH) == TextAttribute.STRIKETHROUGH_ON);

		org.lecturestudio.core.text.Font f = new org.lecturestudio.core.text.Font(font.getName(), font.getSize());
		f.setPosture(toLectFontPosture(font));
		f.setWeight(toLectFontWeight(font));
		f.setTextAttributes(textAttributes);

		return f;
	}

	public static Number toAwtFontPosture(FontPosture posture) {
		switch (posture) {
			case ITALIC:
				return TextAttribute.POSTURE_OBLIQUE;

			case REGULAR:
			default:
				return TextAttribute.POSTURE_REGULAR;
		}
	}

	public static Number toAwtFontWeight(FontWeight weight) {
		switch (weight) {
			case THIN:
			case EXTRA_LIGHT:
				return TextAttribute.WEIGHT_EXTRA_LIGHT;

			case LIGHT:
				return TextAttribute.WEIGHT_LIGHT;

			case NORMAL:
				return TextAttribute.WEIGHT_REGULAR;

			case MEDIUM:
				return TextAttribute.WEIGHT_MEDIUM;

			case SEMI_BOLD:
				return TextAttribute.WEIGHT_SEMIBOLD;

			case BOLD:
				return TextAttribute.WEIGHT_BOLD;

			case EXTRA_BOLD:
				return TextAttribute.WEIGHT_EXTRABOLD;

			case BLACK:
				return TextAttribute.WEIGHT_ULTRABOLD;

			default:
				return TextAttribute.POSTURE_REGULAR;
		}
	}

	public static FontPosture toLectFontPosture(java.awt.Font font) {
		Map<TextAttribute, ?> attrs = font.getAttributes();
		Number posture = (Number) attrs.get(TextAttribute.POSTURE);

		if (TextAttribute.POSTURE_REGULAR.equals(posture)) {
			return FontPosture.REGULAR;
		}
		else if (TextAttribute.POSTURE_OBLIQUE.equals(posture)) {
			return FontPosture.ITALIC;
		}

		return FontPosture.REGULAR;
	}

	public static FontWeight toLectFontWeight(java.awt.Font font) {
		Map<TextAttribute, ?> attrs = font.getAttributes();
		Number weight = (Number) attrs.get(TextAttribute.WEIGHT);

		if (TextAttribute.WEIGHT_EXTRA_LIGHT.equals(weight)) {
			return FontWeight.EXTRA_LIGHT;
		}
		else if (TextAttribute.WEIGHT_LIGHT.equals(weight)) {
			return FontWeight.LIGHT;
		}
		else if (TextAttribute.WEIGHT_REGULAR.equals(weight)) {
			return FontWeight.NORMAL;
		}
		else if (TextAttribute.WEIGHT_MEDIUM.equals(weight)) {
			return FontWeight.MEDIUM;
		}
		else if (TextAttribute.WEIGHT_SEMIBOLD.equals(weight)) {
			return FontWeight.SEMI_BOLD;
		}
		else if (TextAttribute.WEIGHT_BOLD.equals(weight)) {
			return FontWeight.BOLD;
		}
		else if (TextAttribute.WEIGHT_EXTRABOLD.equals(weight)) {
			return FontWeight.EXTRA_BOLD;
		}
		else if (TextAttribute.WEIGHT_ULTRABOLD.equals(weight)) {
			return FontWeight.BLACK;
		}

		return FontWeight.NORMAL;
	}
}

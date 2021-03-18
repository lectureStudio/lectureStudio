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

package org.lecturestudio.core.text;

import org.scilab.forge.jlatexmath.TeXFormula;

public class TeXFont {

	public enum Type {

		SERIF		(TeXFormula.SERIF),
		SANSSERIF	(TeXFormula.SANSSERIF),
		BOLD		(TeXFormula.BOLD),
		ITALIC		(TeXFormula.ITALIC),
		BOLD_ITALIC	(TeXFormula.BOLD | TeXFormula.ITALIC),
		ROMAN		(TeXFormula.ROMAN),
		TYPEWRITER	(TeXFormula.TYPEWRITER);


		private final Integer value;


		Type(Integer type) {
			this.value = type;
		}

		public Integer getValue() {
			return value;
		}

		public static Type fromValue(int value) {
			for (Type type : values()) {
				if (type.value == value) {
					return type;
				}
			}
			return null;
		}
	}

	private Type type;

	private float size;


	public TeXFont() {

	}

	public TeXFont(Type type, float size) {
		this.type = type;
		this.size = size;
	}

	public Type getType() {
		return type;
	}

	public void setType(Type type) {
		this.type = type;
	}

	public float getSize() {
		return size;
	}

	public void setSize(float size) {
		this.size = size;
	}

	public TeXFont clone() {
		return new TeXFont(getType(), getSize());
	}

	@Override
	public String toString() {
		return getClass() + ": " + type + ", " + size;
	}
}

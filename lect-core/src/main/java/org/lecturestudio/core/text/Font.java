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

import java.io.Serializable;
import java.util.Objects;

public class Font implements Cloneable, Serializable {

	private static final long serialVersionUID = 2227630950171606113L;

	private String familyName;

	private double size;

	private FontWeight weight;

	private FontPosture posture;

	private TextAttributes attributes;


	public Font() {

	}

	public Font(String name, double size) {
		this.familyName = name;
		this.size = size;
		this.weight = FontWeight.NORMAL;
		this.posture = FontPosture.REGULAR;
	}

	public String getFamilyName() {
		return familyName;
	}

	public void setFamilyName(String name) {
		this.familyName = name;
	}

	public void setSize(double size) {
		this.size = size;
	}

	public double getSize() {
		return size;
	}

	public void setWeight(FontWeight weight) {
		this.weight = weight;
	}

	public FontWeight getWeight() {
		return weight;
	}

	public void setPosture(FontPosture posture) {
		this.posture = posture;
	}

	public FontPosture getPosture() {
		return posture;
	}

	public TextAttributes getTextAttributes() {
		return attributes;
	}

	public void setTextAttributes(TextAttributes textAttributes) {
		this.attributes = textAttributes;
	}

	@Override
	public Font clone() {
		Font font = new Font(familyName, size);
		font.setPosture(posture);
		font.setWeight(weight);
		font.setTextAttributes(attributes);

		return font;
	}

	@Override
	public boolean equals(Object other) {
		if (this == other) {
			return true;
		}
		if (other == null || getClass() != other.getClass()) {
			return false;
		}

		Font font = (Font) other;

		boolean sizeEqual = Double.compare(font.size, size) == 0;
		boolean familyEqual = Objects.equals(familyName, font.familyName);
		boolean attributesEqual = Objects.equals(attributes, font.attributes);
		boolean weightEqual = weight == font.weight;
		boolean postureEqual = posture == font.posture;

		return sizeEqual && familyEqual && attributesEqual && weightEqual && postureEqual;
	}

	@Override
	public int hashCode() {
		return Objects.hash(familyName, size, weight, posture, attributes);
	}

	@Override
	public String toString() {
		return familyName + " " + size + " " + posture + " " + weight + " " + attributes;
	}
}

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

package org.lecturestudio.core.graphics;

import java.io.Serializable;

public class Color implements Cloneable, Serializable {

	private static final long serialVersionUID = -2703233901325908526L;

	/** The color white in the default sRGB space. */
	public final static Color WHITE = new Color(255, 255, 255);

	/** The color black in the default sRGB space. */
	public final static Color BLACK = new Color(0, 0, 0);

	/** The color value containing RGBA components. */
	private final int value;


	/**
	 * Copy-constructor.
	 *
	 * @param color The color to copy.
	 */
	public Color(Color color) {
		this(color.getRGBA());
	}

	/**
	 * Creates a new instance of {@link Color}. The values must be in the range 0 - 255.
	 * 
	 * @param red The red component of the {@link Color}.
	 * @param green The green component of the {@link Color}.
	 * @param blue The blue component of the {@link Color}.
	 */
	public Color(int red, int green, int blue) {
		this(red, green, blue, 255);
	}

	/**
	 * Creates a new instance of {@link Color}. The values must be in the range 0 - 255.
	 * 
	 * @param red The red component of the {@link Color}.
	 * @param green The green component of the {@link Color}.
	 * @param blue The blue component of the {@link Color}.
	 * @param opacity The opacity of the {@link Color}.
	 */
	public Color(int red, int green, int blue, int opacity) {
		value = ((opacity & 0xFF) << 24) |
				((red & 0xFF) << 16) |
				((green & 0xFF) << 8) |
				((blue & 0xFF));
	}

	/**
	 * Creates a new instance of {@link Color} with the specified combined RGBA value
	 * consisting of the alpha component in bits 24-31, the red component in bits 16-23,
	 * the green component in bits 8-15, and the blue component in bits 0-7.
	 * 
	 * @param rgba The combined RGBA components.
	 */
	public Color(int rgba) {
		this.value = rgba;
	}

	/**
	 * Creates a new {@link Color} with the specified opacity.
	 * 
	 * @param opacity The new opacity of the {@link Color}.
	 * 
	 * @return A new {@link Color} with the given opacity.
	 */
	public Color derive(int opacity) {
		return new Color(((opacity & 0xFF) << 24) | (value & 0x00FFFFFF));
	}

	/**
	 * Calculates an interpolated {@link Color} along the fraction between {@code 0.0}
	 * and {@code 1.0}. If {@code fraction == 1.0}, {@code endColor} is returned.
	 *
	 * @param endColor The color the interpolation ends with.
	 * @param fraction The fraction between {@code 0.0} and {@code 1.0}
	 *
	 * @return The interpolated {@link Color}.
	 */
	public Color interpolate(Color endColor, double fraction) {
		if (fraction <= 0.0) {
			return this;
		}
		if (fraction >= 1.0) {
			return endColor;
		}

		int r = getRed();
		int g = getGreen();
		int b = getBlue();
		int a = getOpacity();

		return new Color(
				(int) (r + (endColor.getRed() - r) * fraction),
				(int) (g + (endColor.getGreen() - g) * fraction),
				(int) (b + (endColor.getBlue() - b) * fraction),
				(int) (a + (endColor.getOpacity() - a) * fraction));
	}

	/**
	 * Returns the RGBA value representing the color.
	 * 
	 * @return The RGBA value of the color.
	 */
	public int getRGBA() {
		return value;
	}

	/**
	 * The red component of the {@link Color}, in the range 0 - 255.
	 * 
	 * @return The red component.
	 */
	public int getRed() {
		return (getRGBA() >> 16) & 0xFF;
	}

	/**
	 * The green component of the {@link Color}, in the range 0 - 255.
	 * 
	 * @return The green component.
	 */
	public int getGreen() {
		return (getRGBA() >> 8) & 0xFF;
	}

	/**
	 * The blue component of the {@link Color}, in the range 0 - 255.
	 * 
	 * @return The blue component.
	 */
	public int getBlue() {
		return getRGBA() & 0xFF;
	}

	/**
	 * The opacity of the {@link Color}, in the range 0 - 255.
	 * 
	 * @return The opacity.
	 */
	public int getOpacity() {
		return (getRGBA() >> 24) & 0xFF;
	}

	/**
	 * Indicates if the opacity equals {@code 255}.
	 *
	 * @return True if the opacity equals {@code 255}, otherwise false.
	 */
	public boolean isOpaque() {
		return getOpacity() == 255;
	}

	@Override
	public int hashCode() {
		return value;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (getClass() != obj.getClass()) {
			return false;
		}

		Color other = (Color) obj;

		return this.value == other.value;
	}

	@Override
	public String toString() {
		return "Color (" + getRed() + " " + getGreen() + " " + getBlue() + " " + getOpacity() + ")";
	}

	@Override
	public Color clone() {
		try {
			return (Color) super.clone();
		}
		catch (CloneNotSupportedException e) {
			throw new InternalError();
		}
	}
}

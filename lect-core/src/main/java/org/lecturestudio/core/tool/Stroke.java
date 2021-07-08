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
import org.lecturestudio.core.graphics.StrokeLineCap;

import java.util.Objects;

/**
 * Class holding information about a pen (color, size, ...)
 * 
 * @author Tobias
 * 
 */
public class Stroke implements Cloneable {

	private Color color;

	private StrokeLineCap lineCapStyle;

	private double width;
	

	public Stroke() {
		this(Color.BLACK);
	}

	public Stroke(Color color) {
		this(color, 1);
	}

	public Stroke(Color color, double size) {
		this(color, size, 255);
	}

	public Stroke(Color color, double size, int alpha) {
		this.color = new Color(color.getRed(), color.getGreen(), color.getBlue(), alpha);
		this.width = size;
		this.lineCapStyle = StrokeLineCap.ROUND;
	}

	/**
	 * Sets the line cap style, refer to {@link StrokeLineCap}
	 * 
	 * @param style
	 */
	public void setStrokeLineCap(StrokeLineCap style) {
		this.lineCapStyle = style;
	}

	/**
	 * Returns the line cap style
	 * 
	 * @return
	 */
	public StrokeLineCap getStrokeLineCap() {
		return lineCapStyle;
	}

	/**
	 * Returns the color
	 * 
	 * @return
	 */
	public Color getColor() {
		return color;
	}

	/**
	 * Sets the color
	 * 
	 * @param color
	 */
	public void setColor(Color color) {
		this.color = color;
	}

	/**
	 * Returns the size in page metrics
	 * 
	 * @return
	 */
	public double getWidth() {
		return width;
	}

	/**
	 * Returns the size
	 * 
	 * @param width
	 */
	public void setWidth(double width) {
		this.width = width;
	}
	
	/**
	 * Scales the size of this {@code Stroke}.
	 * 
	 * @param scale the scale
	 */
	public void scale(double scale) {
		this.width *= scale;
	}

	@Override
	public Stroke clone() {
		try {
			return (Stroke) super.clone();
		}
		catch (CloneNotSupportedException e) {
			// should never happen.
		}
		return null;
	}

	@Override
	public boolean equals(Object obj) {
		if (obj == null || obj.getClass() != this.getClass()) {
			return false;
		}
		final Stroke otherStroke = (Stroke) obj;

		return (Objects.equals(this.getColor(), otherStroke.getColor()) &&
				Objects.equals(this.getStrokeLineCap(), otherStroke.getStrokeLineCap()) &&
				Objects.equals(this.getWidth(), otherStroke.getWidth()));
	}

}

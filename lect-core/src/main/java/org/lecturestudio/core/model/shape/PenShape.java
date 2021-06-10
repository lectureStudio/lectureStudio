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

package org.lecturestudio.core.model.shape;

import org.lecturestudio.core.geometry.PenPoint2D;
import org.lecturestudio.core.geometry.Rectangle2D;
import org.lecturestudio.core.tool.Stroke;

public abstract class PenShape extends Shape {

	private Stroke stroke;
	
	
	/**
	 * Creates a new {@link PenShape} with the specified {@link Stroke}.
	 * 
	 * @param stroke The {@link Stroke} of this shape.
	 */
	public PenShape(Stroke stroke) {
		setStroke(stroke);
	}
	
	/**
	 * Returns the {@link Stroke} of this shape.
	 *
	 * @return The {@link Stroke} of this shape.
	 */
	public Stroke getStroke() {
		return stroke;
	}
	
	/**
	 * Set a new {@link Stroke} for this shape.
	 * 
	 * @param stroke The new {@link Stroke}.
	 */
	protected void setStroke(Stroke stroke) {
		this.stroke = stroke;
	}

	protected void updateBounds(PenPoint2D point) {
		double w = stroke.getWidth();
		double w2 = w * 2;

		Rectangle2D bounds = getBounds();
		bounds.setRect(point.getX() - w, point.getY() - w, w2, w2);
	}

	protected void updateBounds(PenPoint2D p1, PenPoint2D p2) {
		double w = stroke.getWidth() * 2;

		Rectangle2D bounds = getBounds();
		bounds.setFromDiagonal(p1.getX() - w, p1.getY() - w, p2.getX() + w, p2.getY() + w);
	}
}

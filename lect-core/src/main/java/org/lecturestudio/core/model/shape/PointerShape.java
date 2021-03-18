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

import java.io.IOException;
import java.nio.ByteBuffer;

import org.lecturestudio.core.geometry.PenPoint2D;
import org.lecturestudio.core.geometry.Rectangle2D;
import org.lecturestudio.core.graphics.Color;
import org.lecturestudio.core.graphics.StrokeLineCap;
import org.lecturestudio.core.tool.Stroke;

/**
 * Shape representing a temporary pointer.
 *
 * @author Alex Andres
 */
public class PointerShape extends PenShape {

	public PointerShape(Stroke stroke) {
		super(stroke);
	}

	public PointerShape(byte[] input) throws IOException {
		super(null);

		parseFrom(input);
	}

	public void setPoint(PenPoint2D point) {
		if (addPoint(point)) {
			Rectangle2D clip = getBounds().clone();

			// Update bounds.
			double r = getStroke().getWidth();
			double dt = r / 2;

			Rectangle2D bounds = getBounds();
			bounds.setRect(point.getX() - dt, point.getY() - dt, r, r);

			clip.union(bounds);

			fireShapeChanged(clip);
		}
	}

	public PenPoint2D getPoint() {
		if (getPoints().isEmpty()) {
			return null;
		}

		// Return last added point.
		return getPoints().get(getPoints().size() - 1);
	}

	@Override
	public PointerShape clone() {
		PointerShape shape = new PointerShape(getStroke().clone());
		shape.setHandle(getHandle());
		shape.setKeyEvent(getKeyEvent());

		for (PenPoint2D point : getPoints()) {
			shape.setPoint(point.clone());
		}

		return shape;
	}

	@Override
	public byte[] toByteArray() throws IOException {
		Stroke stroke = getStroke();

		int length = 13;

		ByteBuffer buffer = createBuffer(length);

		// Stroke data: 13 bytes.
		buffer.putInt(stroke.getColor().getRGBA());
		buffer.put((byte) stroke.getStrokeLineCap().ordinal());
		buffer.putDouble(stroke.getWidth());

		return buffer.array();
	}

	@Override
	protected void parseFrom(byte[] input) throws IOException {
		ByteBuffer buffer = createBuffer(input);

		// Stroke
		Color color = new Color(buffer.getInt());
		StrokeLineCap lineCap = StrokeLineCap.values()[buffer.get()];
		double penWidth = buffer.getDouble();

		Stroke stroke = new Stroke(color, penWidth);
		stroke.setStrokeLineCap(lineCap);

		setStroke(stroke);
	}

}

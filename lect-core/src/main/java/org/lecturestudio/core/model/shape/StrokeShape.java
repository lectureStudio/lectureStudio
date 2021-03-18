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

import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;

import java.awt.geom.Path2D;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.Iterator;
import java.util.List;

import org.lecturestudio.core.geometry.Line2D;
import org.lecturestudio.core.geometry.PenPoint2D;
import org.lecturestudio.core.geometry.PenStroker;
import org.lecturestudio.core.geometry.Point2D;
import org.lecturestudio.core.geometry.Rectangle2D;
import org.lecturestudio.core.graphics.Color;
import org.lecturestudio.core.graphics.StrokeLineCap;
import org.lecturestudio.core.tool.Stroke;

/**
 * Shape representing a pen stroke.
 * 
 * @author Tobias
 * @author Alex Andres
 */
public class StrokeShape extends PenShape {

	private PenStroker stroker;


	public StrokeShape(Stroke stroke) {
		super(stroke);
	}

	public StrokeShape(byte[] input) throws IOException {
		super(null);

		parseFrom(input);
	}

	public PenStroker getPenStroker() {
		return stroker;
	}

	@Override
	public void moveByDelta(PenPoint2D delta) {
		stroker.moveByDelta(delta);

		super.moveByDelta(delta);
	}

	@Override
	public boolean contains(Point2D p) {
		double delta = getStroke().getWidth() / 2;
		List<PenPoint2D> points = getPoints();
		
		// Handle simple cases.
		if (points.isEmpty()) {
			return false;
		}
		else if (points.size() == 1) {
			return points.get(0).distance(p) <= delta;
		}

		// One of these lines must be crossed by a segment of our stroke.
		Line2D l1 = new Line2D(p.getX() + delta, p.getY() + delta, p.getX() - delta, p.getY() - delta);
		Line2D l2 = new Line2D(p.getX() - delta, p.getY() + delta, p.getX() + delta, p.getY() - delta);

		Iterator<PenPoint2D> it = points.iterator();
		Point2D p1 = it.next();
		Point2D p2;
		Line2D segment;

		while (it.hasNext()) {
			p2 = it.next();
			segment = new Line2D(p1, p2);
			
			if (segment.intersects(l1)) {
				return true;
			}
			if (segment.intersects(l2)) {
				return true;
			}
			
			p1 = p2;
		}

		return false;
	}
	
	@Override
	public boolean intersects(Rectangle2D rect) {
		Path2D path = stroker.getStrokePath();
		
		return path.intersects(rect.getX(), rect.getY(), rect.getWidth(), rect.getHeight());
	}
	
	/**
	 * Adds a point to the shape
	 * 
	 * @param point
	 */
	@Override
	public boolean addPoint(PenPoint2D point) {
		if (point.getPressure() == 0) {
			return false;
		}
		
		List<PenPoint2D> points = getPoints();
		
		synchronized (points) {
			PenPoint2D last = null;

			if (!points.isEmpty()) {
				last = points.get(points.size() - 1);
			}

			if (!super.addPoint(point)) {
				return false;
			}

			stroker.addPoint(point);

			// Adjust bounding.
			if (isNull(last)) {
				updateBounds(point);
			}
			else {
				updateBounds(last, point);
			}

			fireShapeChanged(getBounds());
		}
		return true;
	}

	@Override
	public StrokeShape clone() {
		StrokeShape shape = new StrokeShape(getStroke().clone());
		shape.setHandle(getHandle());
		shape.setKeyEvent(getKeyEvent());

		for (PenPoint2D point : getPoints()) {
			shape.addPoint(point.clone());
		}

		return shape;
	}

	@Override
	public byte[] toByteArray() throws IOException {
		Stroke stroke = getStroke();

		int length = 13 + 24 * getPoints().size();

		ByteBuffer buffer = createBuffer(length);

		// Stroke data: 13 bytes.
		buffer.putInt(stroke.getColor().getRGBA());
		buffer.put((byte) stroke.getStrokeLineCap().ordinal());
		buffer.putDouble(stroke.getWidth());

		// Points: 24 bytes each.
		for (PenPoint2D point : getPoints()) {
			buffer.putDouble(point.getX());
			buffer.putDouble(point.getY());
			buffer.putDouble(point.getPressure());
		}

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

		// Points
		while (buffer.remaining() >= 24) {
			PenPoint2D point = new PenPoint2D(buffer.getDouble(), buffer.getDouble(), buffer.getDouble());
			addPoint(point);
		}
	}

	@Override
	protected void setStroke(Stroke stroke) {
		super.setStroke(stroke);

		if (nonNull(stroke)) {
			stroker = new PenStroker(stroke.getWidth());
		}
	}
}

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

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.Iterator;
import java.util.concurrent.ConcurrentLinkedDeque;

import org.lecturestudio.core.geometry.PenPoint2D;
import org.lecturestudio.core.geometry.Point2D;
import org.lecturestudio.core.geometry.Rectangle2D;
import org.lecturestudio.core.graphics.Color;

public class TextSelectionShape extends Shape {

	/** The space threshold to separate character groups. */
	private static final double GAP = 0.2;

	private final ConcurrentLinkedDeque<Rectangle2D> selection = new ConcurrentLinkedDeque<>();

	private Color color;


	/**
	 * Describes the distance from a character rectangle to a character group
	 * box (container).
	 */
	private static class RectDist {

		double dist;
		Rectangle2D container;

	}


	public TextSelectionShape(Color color) {
		setColor(color);
	}

	public TextSelectionShape(byte[] input) throws IOException {
		parseFrom(input);
	}

	public void addSelection(Rectangle2D rect) {
		if (selection.isEmpty()) {
			// Create new selection box.
			selection.add(rect.clone());

			setBounds(rect.clone());
		}
		else {
			RectDist rectDist = getClosestSelectionBox(rect);
			if (isNull(rectDist)) {
				return;
			}

			if (rectDist.dist > GAP) {
				// Create new selection box, if there is a quite large gap between characters.
				selection.add(rect.clone());

				getBounds().union(rect);
			}
			else {
				// Add character rectangle into a selection box, which represents a group of characters.
				rectDist.container.union(rect);

				getBounds().union(rectDist.container);
			}
		}

		fireShapeChanged(getBounds());
	}

	public boolean hasSelection() {
		return !selection.isEmpty();
	}

	public Iterator<Rectangle2D> getSelection() {
		return selection.iterator();
	}

	public void setColor(Color color) {
		this.color = color;
	}

	public Color getColor() {
		return color;
	}

	@Override
	public boolean contains(Point2D p) {
		for (Rectangle2D rect : selection) {
			if (rect.contains(p)) {
				return true;
			}
		}

		return false;
	}

	@Override
	public TextSelectionShape clone() {
		TextSelectionShape shape = new TextSelectionShape(color.clone());
		shape.setHandle(getHandle());
		shape.setKeyEvent(getKeyEvent());

		for (Rectangle2D rect : selection) {
			shape.addSelection(rect.clone());
		}

		for (PenPoint2D point : getPoints()) {
			shape.addPoint(point.clone());
		}

		return shape;
	}

	@Override
	public byte[] toByteArray() throws IOException {
		int length = 8 + 32 * selection.size();

		ByteBuffer buffer = createBuffer(length);

		buffer.putInt(selection.size());

		// Color
		buffer.putInt(getColor().getRGBA());

		// Rectangles: 32 bytes each.
		for (Rectangle2D rect : selection) {
			buffer.putDouble(rect.getX());
			buffer.putDouble(rect.getY());
			buffer.putDouble(rect.getWidth());
			buffer.putDouble(rect.getHeight());
		}

		return buffer.array();
	}

	@Override
	protected void parseFrom(byte[] input) throws IOException {
		ByteBuffer buffer = createBuffer(input);

		int rectCount = buffer.getInt();

		// Color
		Color color = new Color(buffer.getInt());

		setColor(color);

		// Rectangles
		while (rectCount > 0 && buffer.remaining() >= 32) {
			Rectangle2D rect = new Rectangle2D(buffer.getDouble(), buffer.getDouble(), buffer.getDouble(), buffer.getDouble());
			selection.add(rect);

			rectCount--;
		}
	}

	private RectDist getClosestSelectionBox(Rectangle2D rect) {
		RectDist rectDist = new RectDist();
		rectDist.dist = Double.MAX_VALUE;

		for (Rectangle2D container : selection) {
			if (container.contains(rect)) {
				return null;
			}

			// Distance to the left.
			double dist = Math.abs(container.getX() - (rect.getX() + rect.getWidth()));

			if (dist < rectDist.dist) {
				rectDist.container = container;
				rectDist.dist = dist;
			}

			// Distance to the right.
			dist = Math.abs(rect.getX() - (container.getX() + container.getWidth()));

			if (dist < rectDist.dist) {
				rectDist.container = container;
				rectDist.dist = dist;
			}
		}

		return rectDist;
	}

}

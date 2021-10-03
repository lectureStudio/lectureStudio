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

import static java.util.Objects.nonNull;

import java.io.IOException;
import java.nio.ByteBuffer;

import org.lecturestudio.core.geometry.Dimension2D;
import org.lecturestudio.core.geometry.GeometryUtils;
import org.lecturestudio.core.geometry.PenPoint2D;
import org.lecturestudio.core.geometry.Rectangle2D;
import org.lecturestudio.core.graphics.Color;
import org.lecturestudio.core.graphics.StrokeLineCap;
import org.lecturestudio.core.input.KeyEvent;
import org.lecturestudio.core.tool.Stroke;

public abstract class FormShape extends PenShape {

	private final Dimension2D ratio = new Dimension2D(1, 1);

	private final Rectangle2D rect = new Rectangle2D();


	/**
	 * Creates a {@link FormShape} with the specified stroke.
	 * (Calls {@link PenShape#PenShape(Stroke)} with the stroke.)
	 *
	 * @param stroke The stroke.
	 */
	public FormShape(Stroke stroke) {
		super(stroke);
	}

	/**
	 * Get the first point of the form.
	 *
	 * @return The first point of the form.
	 */
	public PenPoint2D getStartPoint() {
		if (getPoints().isEmpty()) {
			return null;
		}

		return getPoints().get(0);
	}

	/**
	 * Get the last point of the form.
	 *
	 * @return The last point of the form.
	 */
	public PenPoint2D getEndPoint() {
		if (getPoints().size() < 1) {
			return null;
		}

		// Return last added point.
		return getPoints().get(getPoints().size() - 1);
	}

	/**
	 * Sets the root location
	 *
	 * @param point The start point.
	 */
	public void setStartPoint(PenPoint2D point) {
		addPoint(point);
		updateBounds(point, point);
	}

	/**
	 * Sets a new end point and updates the diagonal.
	 *
	 * @param point The new end point.
	 */
	public void setEndPoint(PenPoint2D point) {
		if (addPoint(point)) {
			Rectangle2D dirtyArea = getBounds().clone();

			updateBounds(getStartPoint(), point);

			dirtyArea.union(getBounds());

			fireShapeChanged(dirtyArea);
		}
	}

	/**
	 * Get the rectangle.
	 *
	 * @return The rectangle.
	 */
	public Rectangle2D getRect() {
		return rect;
	}

	/**
	 * Set new ratio.
	 *
	 * @param ratio The new ratio.
	 */
	public void setRatio(Dimension2D ratio) {
		this.ratio.setSize(ratio.getWidth(), ratio.getHeight());
	}

	/**
	 * Indicates whether to fill the interior of the shape.
	 *
	 * @return {@code true} if Alt is pressed, otherwise {@code false}.
	 */
	public boolean fill() {
		KeyEvent keyEvent = getKeyEvent();

		return nonNull(keyEvent) && keyEvent.isAltDown();
	}

	/**
	 * Indicates whether to keep the size ratio of the shape.
	 */
	public boolean keepRatio() {
		KeyEvent keyEvent = getKeyEvent();

		return nonNull(keyEvent) && keyEvent.isShiftDown();
	}

	@Override
	public void moveByDelta(PenPoint2D delta) {
		for (PenPoint2D point : getPoints()) {
			point.subtract(delta);
		}

		// Update bounds.
		PenPoint2D p1 = getStartPoint();
		PenPoint2D p2 = getEndPoint();

		updateBounds(p1, p2);
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

	@Override
	protected void updateBounds(PenPoint2D p1, PenPoint2D p2) {
		Dimension2D origin = new Dimension2D(p2.getX() - p1.getX(), p2.getY() - p1.getY());

		double sw = getStroke().getWidth();
		double startX = p1.getX();
		double startY = p1.getY();
		double width;
		double height;

		if (keepRatio()) {
			Dimension2D dim = GeometryUtils.keepAspectRatio(origin, ratio);

			width = dim.getWidth();
			height = dim.getHeight();
		}
		else {
			width = origin.getWidth();
			height = origin.getHeight();
		}

		rect.setFromDiagonal(startX, startY, startX + width, startY + height);

		Rectangle2D bounds = getBounds();
		bounds.setFromDiagonal(startX, startY, startX + width, startY + height);
		bounds.setRect(bounds.getX() - sw, bounds.getY() - sw,
				bounds.getWidth() + sw * 2, bounds.getHeight() + sw * 2);
	}

}

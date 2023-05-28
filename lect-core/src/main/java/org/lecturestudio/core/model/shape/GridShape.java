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

import org.lecturestudio.core.geometry.Dimension2D;
import org.lecturestudio.core.graphics.Color;

public class GridShape extends Shape {

	private boolean verticalLinesVisible;

	private double verticalLinesInterval;

	private boolean horizontalLinesVisible;

	private double horizontalLinesInterval;

	private Color gridColor;

	private Dimension2D viewRatio;

	/**
	 * Creates a {@link GridShape}.
	 * (Calls the default constructor of {@link Shape}.)
	 */
	public GridShape() {
		super();
	}

	/**
	 * Creates a new {@link GridShape} with the specified input byte array containing
	 * the data for the {@link GridShape}.
	 *
	 * @param input The input byte array.
	 */
	public GridShape(byte[] input) throws IOException {
		super();

		parseFrom(input);
	}

	/**
	 * Get the verticalLinesVisible.
	 *
	 * @return The verticalLinesVisible.
	 */
	public boolean getVerticalLinesVisible() {
		return verticalLinesVisible;
	}

	/**
	 * Set the verticalLinesVisible.
	 *
	 * @param verticalLinesVisible The verticalLinesVisible to set.
	 */
	public void setVerticalLinesVisible(boolean verticalLinesVisible) {
		this.verticalLinesVisible = verticalLinesVisible;

		fireShapeChanged();
	}

	/**
	 * Get the verticalLinesInterval.
	 *
	 * @return The verticalLinesInterval.
	 */
	public double getVerticalLinesInterval() {
		return verticalLinesInterval;
	}

	/**
	 * Set the verticalLinesInterval.
	 *
	 * @param verticalLinesInterval The verticalLinesInterval to set.
	 */
	public void setVerticalLinesInterval(double verticalLinesInterval) {
		this.verticalLinesInterval = verticalLinesInterval;

		fireShapeChanged();
	}

	/**
	 * Get the horizontalLinesVisible.
	 *
	 * @return The horizontalLinesVisible.
	 */
	public boolean getHorizontalLinesVisible() {
		return horizontalLinesVisible;
	}

	/**
	 * Set new horizontalLinesVisible.
	 *
	 * @param horizontalLinesVisible The horizontalLinesVisible to set.
	 */
	public void setHorizontalLinesVisible(boolean horizontalLinesVisible) {
		this.horizontalLinesVisible = horizontalLinesVisible;

		fireShapeChanged();
	}

	/**
	 * Get the horizontalLinesInterval.
	 *
	 * @return The horizontalLinesInterval.
	 */
	public double getHorizontalLinesInterval() {
		return horizontalLinesInterval;
	}

	/**
	 * Set new horizontalLinesInterval.
	 *
	 * @param horizontalLinesInterval The horizontalLinesInterval to set.
	 */
	public void setHorizontalLinesInterval(double horizontalLinesInterval) {
		this.horizontalLinesInterval = horizontalLinesInterval;

		fireShapeChanged();
	}

	/**
	 * Get the grid color.
	 *
	 * @return The color.
	 */
	public Color getColor() {
		return gridColor;
	}

	/**
	 * Set new grid color.
	 *
	 * @param color The color to set.
	 */
	public void setColor(Color color) {
		this.gridColor = color;

		fireShapeChanged();
	}

	/**
	 * Get the view ratio.
	 *
	 * @return The view ratio.
	 */
	public Dimension2D getViewRatio() {
		return viewRatio;
	}

	/**
	 * Set new view ratio.
	 *
	 * @param viewRatio The view ratio to set.
	 */
	public void setViewRatio(Dimension2D viewRatio) {
		this.viewRatio = viewRatio;

		fireShapeChanged();
	}

	@Override
	public Shape clone() {
		GridShape shape = new GridShape();
		shape.setColor(gridColor);
		shape.setVerticalLinesVisible(verticalLinesVisible);
		shape.setVerticalLinesInterval(verticalLinesInterval);
		shape.setHorizontalLinesVisible(horizontalLinesVisible);
		shape.setHorizontalLinesInterval(horizontalLinesInterval);

		return shape;
	}

	@Override
	public byte[] toByteArray() throws IOException {
		int length = 22;

		ByteBuffer buffer = createBuffer(length);

		buffer.putInt(getColor().getRGBA());
		buffer.put((byte) (getVerticalLinesVisible() ? 1 : 0));
		buffer.putDouble(getVerticalLinesInterval());
		buffer.put((byte) (getHorizontalLinesVisible() ? 1 : 0));
		buffer.putDouble(getHorizontalLinesInterval());

		return buffer.array();
	}

	@Override
	protected void parseFrom(byte[] input) throws IOException {
		ByteBuffer buffer = createBuffer(input);

		Color color = new Color(buffer.getInt());

		boolean vLinesVisible = buffer.get() != 0;
		int vLinesInterval = buffer.getInt();

		boolean hLinesVisible = buffer.get() != 0;
		int hLinesInterval = buffer.getInt();

		setColor(color);
		setVerticalLinesVisible(vLinesVisible);
		setVerticalLinesInterval(vLinesInterval);
		setHorizontalLinesVisible(hLinesVisible);
		setHorizontalLinesInterval(hLinesInterval);
	}

}

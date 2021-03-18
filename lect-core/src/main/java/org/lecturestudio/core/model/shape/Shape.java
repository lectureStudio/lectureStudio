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
import java.util.ArrayList;
import java.util.List;

import org.lecturestudio.core.input.KeyEvent;
import org.lecturestudio.core.geometry.PenPoint2D;
import org.lecturestudio.core.geometry.Point2D;
import org.lecturestudio.core.geometry.Rectangle2D;
import org.lecturestudio.core.model.listener.ShapeChangeListener;

/**
 * Basic class for a shape. Some basic functionality is implemented here
 *
 * @author Alex Andres
 */
public abstract class Shape implements Cloneable {

	private static final int KEY_EVENT_MASK = 1;

	private final List<PenPoint2D> points = new ArrayList<>();

	private final transient List<ShapeChangeListener> listeners = new ArrayList<>();

	private Rectangle2D bounds = new Rectangle2D();

	private int handle = hashCode();

	private boolean selected = false;

	private KeyEvent keyEvent;


	public abstract Shape clone();

	public abstract byte[] toByteArray() throws IOException;

	protected abstract void parseFrom(byte[] input) throws IOException;


	public boolean addPoint(PenPoint2D point) {
		synchronized (points) {
			Point2D last = null;

			if (!points.isEmpty()) {
				last = points.get(points.size() - 1);
			}

			if (point.equals(last)) {
				return false;
			}

			points.add(point);
		}
		return true;
	}

	public List<PenPoint2D> getPoints() {
		return points;
	}

	public boolean hasPoints() {
		return !points.isEmpty();
	}

	public int getHandle() {
		return handle;
	}

	public void setHandle(int handle) {
		this.handle = handle;
	}

	public synchronized boolean isSelected() {
		return selected;
	}

	public synchronized void setSelected(boolean selected) {
		if (this.selected == selected) {
			return;
		}

		this.selected = selected;

		fireShapeChanged(getBounds());
	}

	public void setKeyEvent(KeyEvent event) {
		this.keyEvent = event;
	}

	public KeyEvent getKeyEvent() {
		return keyEvent;
	}

	public void moveByDelta(PenPoint2D delta) {
		for (PenPoint2D point : getPoints()) {
			point.subtract(delta);
		}

		fireShapeChanged(null);
	}

	public boolean intersects(Rectangle2D rect) {
		return false;
	}

	/**
	 * Checks whether or not the bounding rectangle of the shape contains the
	 * point.
	 *
	 * @param point a {@code Point2D}.
	 *
	 * @return true if the bounding rectangle of the shape contains the point,
	 * false otherwise.
	 */
	public boolean contains(Point2D point) {
		return bounds.contains(point);
	}

	/**
	 * Listener is notified whenever the shape is changed
	 *
	 * @param listener The listener to add.
	 */
	public void addShapeChangedListener(ShapeChangeListener listener) {
		if (!listeners.contains(listener)) {
			listeners.add(listener);
		}
	}

	public void removeShapeChangedListener(ShapeChangeListener listener) {
		listeners.remove(listener);
	}

	/**
	 * Returns the bounding rectangle of the shape.
	 *
	 * @return the bounding rectangle of this shape.
	 */
	public Rectangle2D getBounds() {
		return bounds;
	}

	protected void setBounds(Rectangle2D bounds) {
		this.bounds = bounds;
	}

	protected void setBounds(double x, double y, double width, double height) {
		this.bounds = new Rectangle2D(x, y, width, height);
	}

	protected void fireShapeChanged(Rectangle2D dirtyArea) {
		for (ShapeChangeListener listener : listeners) {
			listener.shapeChanged(this, dirtyArea);
		}
	}

	protected void fireShapeChanged() {
		fireShapeChanged(getBounds());
	}

	/**
	 * Creates a new {@code ByteBuffer} with the specified length of the payload
	 * and inserts default shape fields. The buffer will be of the size of the
	 * specified length + the length of the inserted default fields.
	 *
	 * @param length The length of the payload of the specific shape.
	 *
	 * @return A new {@code ByteBuffer} with pre-filled default shape fields.
	 */
	protected ByteBuffer createBuffer(int length) {
		int header = 0;

		KeyEvent keyEvent = getKeyEvent();

		if (keyEvent != null) {
			length += 9;

			// Set the flag in the header.
			header |= KEY_EVENT_MASK;
		}

		ByteBuffer buffer = ByteBuffer.allocate(length + 8);

		// Set header.
		buffer.putInt(header);
		// Shape handle.
		buffer.putInt(getHandle());

		if (keyEvent != null) {
			// KeyEvent: 9 bytes.
			buffer.putInt(keyEvent.getKeyCode());
			buffer.putInt(keyEvent.getModifiers());
			buffer.put((byte) keyEvent.getEventType().ordinal());
		}

		return buffer;
	}

	/**
	 * Creates a new {@code ByteBuffer} with the specified payload to read from
	 * and reads default shape fields, if any present.
	 *
	 * @param input The shape payload data.
	 *
	 * @return A new {@code ByteBuffer} to read specific shape fields.
	 */
	protected ByteBuffer createBuffer(byte[] input) {
		ByteBuffer buffer = ByteBuffer.wrap(input);

		int header = buffer.getInt();
		int handle = buffer.getInt();

		setHandle(handle);

		if ((header & KEY_EVENT_MASK) == KEY_EVENT_MASK) {
			// KeyEvent
			int keyCode = buffer.getInt();
			int modifiers = buffer.getInt();

			KeyEvent.EventType eventType = KeyEvent.EventType.values()[buffer.get()];
			KeyEvent keyEvent = new KeyEvent(keyCode, modifiers, eventType);

			setKeyEvent(keyEvent);
		}

		return buffer;
	}

}

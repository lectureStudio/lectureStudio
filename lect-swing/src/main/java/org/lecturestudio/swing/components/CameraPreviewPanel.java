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

package org.lecturestudio.swing.components;

import static java.util.Objects.isNull;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.JComponent;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import org.lecturestudio.core.camera.CameraFormat;
import org.lecturestudio.core.geometry.Rectangle2D;

/**
 * Simply implementation of JPanel allowing users to render pictures taken with
 * a camera.
 *
 * @author Alex Andres
 */
public class CameraPreviewPanel extends CameraPanel {

	private static final long serialVersionUID = 3253275789144083539L;

	private CaptureRectangle captureRectangle;


	/**
	 * Creates a camera panel with user defined capture format.
	 */
	public CameraPreviewPanel() {
		super();

		initialize();
	}

	@Override
	public void setCameraFormat(CameraFormat format) {
		super.setCameraFormat(format);

		revalidate();
		repaint();
	}

	public void setListener(ChangeListener listener) {
		listenerList.add(ChangeListener.class, listener);
	}

	public Rectangle2D getCaptureRect() {
		CameraFormat captureFormat = getCameraFormat();
		Rectangle bounds = captureRectangle.getBounds();

		if (isNull(captureFormat)) {
			return null;
		}

		int width = getPreferredSize().width;
		int height = getPreferredSize().height;

		double sx = captureFormat.getWidth() / (double) width;
		double sy = captureFormat.getHeight() / (double) height;

		return new Rectangle2D(bounds.x * sx, bounds.y * sy,
				bounds.width * sx, bounds.height * sy);
	}

	public void setCaptureRect(Rectangle2D rect) {
		CameraFormat captureFormat = getCameraFormat();

		if (isNull(rect) || isNull(captureFormat)) {
			return;
		}

		int width = getPreferredSize().width;
		int height = getPreferredSize().height;

		// Transform rectangle size to local UI component size.
		double sx = width / (double) captureFormat.getWidth();
		double sy = height / (double) captureFormat.getHeight();

		int x = (int) (rect.getX() * sx);
		int y = (int) (rect.getY() * sy);
		int w = (int) (rect.getWidth() * sx);
		int h = (int) (rect.getHeight() * sy);

		if (x < 0)	x = 0;
		if (y < 0)	y = 0;
		if (x + w > width)	x = width - w;
		if (y + h > height)	y = height - h;

		// In case the provided capture rectangle is out of bounds with the capture format.
		if (x < 0)	x = 0;
		if (y < 0)	y = 0;
		if (x + w > width)	w = width;
		if (y + h > height)	h = height;

		captureRectangle.setBounds(x, y, w, h);

		revalidate();
		repaint();
	}

	private void fireStateChanged() {
		ChangeListener[] listeners = listenerList.getListeners(ChangeListener.class);
		ChangeEvent changeEvent = new ChangeEvent(this);

		for (ChangeListener listener : listeners) {
			listener.stateChanged(changeEvent);
		}
	}

	private void initialize() {
		captureRectangle = new CaptureRectangle();
		captureRectangle.setLocation(0, 0);

		add(captureRectangle, 0);
	}



	private class CaptureRectangle extends JComponent {

		private static final long serialVersionUID = 1385726739107751575L;

		private final Color color = new Color(255, 3, 0);

		private final BasicStroke stroke = new BasicStroke(2);


		public CaptureRectangle() {
			setLayout(null);
			setOpaque(false);

			MouseActionHandler mouseHandler = new MouseActionHandler(this);
			addMouseListener(mouseHandler);
			addMouseMotionListener(mouseHandler);
		}

		@Override
		public void setLocation(int x, int y) {
			// Bounds restriction.
			int w = CameraPreviewPanel.this.getPreferredSize().width;
			int h = CameraPreviewPanel.this.getPreferredSize().height;

			if (x < 0) {
				x = 0;
			}
			if (y < 0) {
				y = 0;
			}
			if (x + getWidth() > w) {
				x = w - getWidth();
			}
			if (y + getHeight() > h) {
				y = h - getHeight();
			}

			super.setLocation(x, y);

			fireStateChanged();
		}

		@Override
		public void paintComponent(Graphics g) {
			super.paintComponent(g);

			Graphics2D g2d = (Graphics2D) g;

			int width = getWidth();
			int height = getHeight();
			int lineWidth = (int) stroke.getLineWidth();

			// Draw the border.
			g2d.setColor(color);
			g2d.setStroke(stroke);
			g2d.drawRect(1, 1, width - lineWidth, height - lineWidth);
		}
	}



	private class MouseActionHandler extends MouseAdapter {

		private final CaptureRectangle rect;

		private final Cursor defaultCursor = Cursor.getDefaultCursor();

		private int lastX = 0;
		private int lastY = 0;


		public MouseActionHandler(CaptureRectangle rect) {
			this.rect = rect;
		}

		public void mouseReleased(MouseEvent e) {
			setCursor(defaultCursor);
		}

		public void mousePressed(MouseEvent e) {
			lastX = e.getX();
			lastY = e.getY();
		}

		public void mouseDragged(MouseEvent e) {
			int x = e.getX() - lastX;
			int y = e.getY() - lastY;

			Point location = rect.getLocation();
			rect.setLocation(location.x + x, location.y + y);
		}
	}
}

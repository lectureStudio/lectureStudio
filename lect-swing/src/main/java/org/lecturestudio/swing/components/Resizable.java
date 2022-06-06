/*
 * Copyright (C) 2022 TU Darmstadt, Department of Computer Science,
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

import javax.swing.JComponent;
import javax.swing.event.MouseInputAdapter;
import javax.swing.event.MouseInputListener;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.MouseEvent;

public class Resizable extends JComponent {

	public Resizable(Component comp) {
		this(comp, new ResizableBorder(8));
	}

	public Resizable(Component comp, ResizableBorder border) {
		setLayout(new BorderLayout());
		setBorder(border);

		add(comp);
		addMouseListener(resizeListener);
		addMouseMotionListener(resizeListener);
	}

	@Override
	public void setBounds(int x, int y, int width, int height) {
		if (getParent() != null && !getParent().getBounds().isEmpty()) {
			Rectangle pBounds = getParent().getBounds();

			if (x < pBounds.x) {
				x = pBounds.x;
			}
			if (y < pBounds.y) {
				y = pBounds.y;
			}
			if (width < 30) {
				width = 30;
				x = getX();
			}
			if (height < 30) {
				height = 30;
				y = getY();
			}
			if (x + width > pBounds.x + pBounds.width) {
				width = getWidth();
				x = pBounds.width - width;
			}
			if (y + height > pBounds.y + pBounds.height) {
				height = getHeight();
				y = pBounds.height - height;
			}
		}

		super.setBounds(x, y, width, height);
	}

	private void resize() {
		if (getParent() != null) {
			getParent().revalidate();
		}
	}

	MouseInputListener resizeListener = new MouseInputAdapter() {

		private int cursor;
		private Point startPos = null;


		@Override
		public void mouseMoved(MouseEvent me) {
			if (hasFocus()) {
				ResizableBorder border = (ResizableBorder) getBorder();
				setCursor(Cursor.getPredefinedCursor(border.getCursor(me)));
			}
		}

		@Override
		public void mouseExited(MouseEvent mouseEvent) {
			setCursor(Cursor.getDefaultCursor());
		}

		@Override
		public void mousePressed(MouseEvent me) {
			ResizableBorder border = (ResizableBorder) getBorder();
			cursor = border.getCursor(me);
			startPos = me.getPoint();

			requestFocus();
			repaint();
		}

		@Override
		public void mouseReleased(MouseEvent mouseEvent) {
			startPos = null;
		}

		@Override
		public void mouseDragged(MouseEvent me) {
			if (isNull(startPos)) {
				return;
			}

			int x = getX();
			int y = getY();
			int w = getWidth();
			int h = getHeight();

			int dx = me.getX() - startPos.x;
			int dy = me.getY() - startPos.y;

			switch (cursor) {
				case Cursor.N_RESIZE_CURSOR -> {
					setBounds(x, y + dy, w, h - dy);
					resize();
				}
				case Cursor.S_RESIZE_CURSOR -> {
					setBounds(x, y, w, h + dy);
					startPos = me.getPoint();
					resize();
				}
				case Cursor.W_RESIZE_CURSOR -> {
					setBounds(x + dx, y, w - dx, h);
					resize();
				}
				case Cursor.E_RESIZE_CURSOR -> {
					setBounds(x, y, w + dx, h);
					startPos = me.getPoint();
					resize();
				}
				case Cursor.NW_RESIZE_CURSOR -> {
					setBounds(x + dx, y + dy, w - dx, h - dy);
					resize();
				}
				case Cursor.NE_RESIZE_CURSOR -> {
					setBounds(x, y + dy, w + dx, h - dy);
					startPos = new Point(me.getX(), startPos.y);
					resize();
				}
				case Cursor.SW_RESIZE_CURSOR -> {
					setBounds(x + dx, y, w - dx, h + dy);
					startPos = new Point(startPos.x, me.getY());
					resize();
				}
				case Cursor.SE_RESIZE_CURSOR -> {
					setBounds(x, y, w + dx, h + dy);
					startPos = me.getPoint();
					resize();
				}
				case Cursor.MOVE_CURSOR -> {
					var bounds = getBounds();
					bounds.translate(dx, dy);
					setBounds(bounds);
					resize();
				}
			}

			setCursor(Cursor.getPredefinedCursor(cursor));
		}
	};
}

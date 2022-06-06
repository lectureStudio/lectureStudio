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

import javax.swing.SwingConstants;
import javax.swing.border.Border;

import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Insets;
import java.awt.Rectangle;
import java.awt.Shape;
import java.awt.event.MouseEvent;

public class ResizableBorder implements Border {

	private static final Color BORDER_COLOR = new Color(148, 163, 184);

	private static final Color THUMB_COLOR = new Color(248, 250, 252);

	private static final Color THUMB_BORDER_COLOR = new Color(71, 85, 105);

	private static final int[] LOCATIONS = {
			SwingConstants.NORTH,
			SwingConstants.SOUTH,
			SwingConstants.WEST,
			SwingConstants.EAST,
			SwingConstants.NORTH_WEST,
			SwingConstants.NORTH_EAST,
			SwingConstants.SOUTH_WEST,
			SwingConstants.SOUTH_EAST
	};

	private static final int[] CURSORS = {
			Cursor.N_RESIZE_CURSOR, Cursor.S_RESIZE_CURSOR,
			Cursor.W_RESIZE_CURSOR, Cursor.E_RESIZE_CURSOR,
			Cursor.NW_RESIZE_CURSOR, Cursor.NE_RESIZE_CURSOR,
			Cursor.SW_RESIZE_CURSOR, Cursor.SE_RESIZE_CURSOR };

	private final int dist;


	public ResizableBorder(int dist) {
		this.dist = dist;
	}

	@Override
	public Insets getBorderInsets(Component component) {
		return new Insets(dist, dist, dist, dist);
	}

	@Override
	public boolean isBorderOpaque() {
		return false;
	}

	@Override
	public void paintBorder(Component component, Graphics g, int x, int y,
			int w, int h) {
		g.setColor(BORDER_COLOR);
		g.drawRect(x + dist / 2, y + dist / 2, w - dist, h - dist);

		if (component.hasFocus()) {
			Graphics2D g2d = (Graphics2D) g;

			for (int location : LOCATIONS) {
				Rectangle rect = getRectangle(x, y, w, h, location);

				if (rect != null) {
					Shape shape = new Rectangle.Double(rect.x + 0.5,
							rect.y + 0.5, rect.width - 1.5, rect.height - 1.5);

					g2d.setColor(THUMB_COLOR);
					g2d.fill(shape);
					g2d.setColor(THUMB_BORDER_COLOR);
					g2d.draw(shape);
				}
			}
		}
	}

	private Rectangle getRectangle(int x, int y, int w, int h, int location) {
		return switch (location) {
			case SwingConstants.NORTH -> new Rectangle(x + w / 2 - dist / 2, y, dist, dist);
			case SwingConstants.SOUTH -> new Rectangle(x + w / 2 - dist / 2, y + h - dist, dist, dist);
			case SwingConstants.WEST -> new Rectangle(x, y + h / 2 - dist / 2, dist, dist);
			case SwingConstants.EAST -> new Rectangle(x + w - dist, y + h / 2 - dist / 2, dist, dist);
			case SwingConstants.NORTH_WEST -> new Rectangle(x, y, dist, dist);
			case SwingConstants.NORTH_EAST -> new Rectangle(x + w - dist, y, dist, dist);
			case SwingConstants.SOUTH_WEST -> new Rectangle(x, y + h - dist, dist, dist);
			case SwingConstants.SOUTH_EAST -> new Rectangle(x + w - dist, y + h - dist, dist, dist);
			default -> new Rectangle();
		};
	}

	public int getCursor(MouseEvent me) {
		Component c = me.getComponent();
		int w = c.getWidth();
		int h = c.getHeight();

		for (int i = 0; i < LOCATIONS.length; i++) {
			Rectangle rect = getRectangle(0, 0, w, h, LOCATIONS[i]);

			if (rect != null && rect.contains(me.getPoint())) {
				return CURSORS[i];
			}
		}

		return Cursor.MOVE_CURSOR;
	}
}
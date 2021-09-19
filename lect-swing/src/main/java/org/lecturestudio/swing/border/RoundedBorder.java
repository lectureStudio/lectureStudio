/*
 * Copyright (C) 2021 TU Darmstadt, Department of Computer Science,
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

package org.lecturestudio.swing.border;

import java.awt.Color;
import java.awt.Component;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Insets;
import java.awt.RenderingHints;
import java.awt.Shape;
import java.awt.geom.RoundRectangle2D;

import javax.swing.border.AbstractBorder;

public class RoundedBorder extends AbstractBorder {

	private final Color color;

	private final int radius;


	public RoundedBorder() {
		this(Color.LIGHT_GRAY, 3);
	}

	public RoundedBorder(int radius) {
		this(Color.LIGHT_GRAY, radius);
	}

	public RoundedBorder(Color color, int radius) {
		this.color = color;
		this.radius = radius;
	}

	@Override
	public void paintBorder(Component c, Graphics g, int x, int y, int width, int height) {
		Graphics2D g2 = (Graphics2D) g.create();
		g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
		g2.setPaint(color);
		g2.draw(new RoundRectangle2D.Double(x + 0.5, y + 0.5, width - 1.5, height - 1.5, radius, radius));
		g2.dispose();
	}

	@Override
	public Insets getBorderInsets(Component c) {
		return new Insets(this.radius + 1, this.radius + 1, this.radius + 1, this.radius + 1);
	}

	@Override
	public boolean isBorderOpaque() {
		return true;
	}

	public Shape getBorderShape(double width, double height) {
		return new RoundRectangle2D.Double(0.5, 0.5, width - 1.5, height - 1.5,
				radius, radius);
	}
}

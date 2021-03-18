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

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.Stroke;
import java.awt.geom.CubicCurve2D;

import javax.swing.JComponent;

public class PenToolPreview extends JComponent {

	private Color color;

	private float width;


	public PenToolPreview() {
		setBackground(Color.WHITE);
		setColor(Color.DARK_GRAY);
	}

	public void setColor(Color color) {
		this.color = color;
		repaint();
	}

	public void setWidth(float width) {
		this.width = width;
		repaint();
	}

	@Override
	protected void paintComponent(Graphics g) {
		super.paintComponent(g);

		Dimension size = getSize();

		float yHalf = size.height / 2f;
		float offset = width / 2 + 5;

		float cx1 = size.width / 3f;
		float cy1 = 0;

		float cx2 = size.width - size.width / 3f;
		float cy2 = size.height;

		CubicCurve2D curve = new CubicCurve2D.Float(offset, yHalf, cx1, cy1, cx2, cy2, size.width - offset - 2, yHalf);

		Graphics2D g2d = (Graphics2D) g;
		Stroke stroke = g2d.getStroke();

		g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
		g2d.setStroke(new BasicStroke(width, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
		g2d.setColor(color);
		g2d.draw(curve);
		g2d.setStroke(stroke);
	}
}

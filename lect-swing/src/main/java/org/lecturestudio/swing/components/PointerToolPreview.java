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

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;

import javax.swing.JComponent;

import org.lecturestudio.core.geometry.PenPoint2D;
import org.lecturestudio.core.model.shape.PointerShape;
import org.lecturestudio.core.tool.Stroke;
import org.lecturestudio.swing.renderer.PointerRenderer;
import org.lecturestudio.swing.converter.ColorConverter;

public class PointerToolPreview extends JComponent {

	private final PointerRenderer renderer;

	private final PointerShape pointerShape;


	public PointerToolPreview() {
		renderer = new PointerRenderer();
		pointerShape = new PointerShape(new Stroke());
	}

	public void setColor(Color color) {
		pointerShape.getStroke().setColor(ColorConverter.INSTANCE.from(color));
		repaint();
	}

	public void setWidth(float width) {
		pointerShape.getStroke().setWidth(width);
		repaint();
	}

	@Override
	protected void paintComponent(Graphics g) {
		super.paintComponent(g);

		pointerShape.setPoint(new PenPoint2D(getWidth() / 2d, getHeight() / 2d));

		renderer.render(pointerShape, (Graphics2D) g);
	}
}

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

package org.lecturestudio.swing.renderer;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.geom.Rectangle2D;
import java.awt.geom.Rectangle2D.Double;

import org.lecturestudio.core.model.shape.Shape;
import org.lecturestudio.core.model.shape.ZoomShape;
import org.lecturestudio.core.tool.Stroke;
import org.lecturestudio.swing.converter.Rectangle2DConverter;

/**
 * Implements a renderer for rendering ZoomShapes, e.g. it draws a
 * semi-transparent rectangle to indicate the zoomed area.
 * 
 * @author Tobias
 */
public class ZoomRenderer extends BaseRenderer {

	private static final Color BACKGROUND = new Color(0, 163, 232, 100);

	private static final Color FRAME = new Color(255, 0, 100, 255);


	@Override
	public Class<? extends Shape> forClass() {
		return ZoomShape.class;
	}

	@Override
	protected void renderPrivate(Shape shape, Graphics2D context) {
		ZoomShape zoomShape = (ZoomShape) shape;
		Stroke stroke = zoomShape.getStroke();
		float width = (float) stroke.getWidth();

		Rectangle2D.Double rect = (Double) Rectangle2DConverter.INSTANCE.to(shape.getBounds());

		context.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_OFF);
		context.setColor(BACKGROUND);
		context.fill(rect);

		context.setColor(FRAME);
		context.setStroke(new BasicStroke(width));
		context.draw(rect);
	}

}

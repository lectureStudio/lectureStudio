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

package org.lecturestudio.javafx.render;

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;

import org.lecturestudio.core.geometry.Rectangle2D;
import org.lecturestudio.core.model.shape.Shape;
import org.lecturestudio.core.model.shape.ZoomShape;
import org.lecturestudio.core.render.Renderer;

public class ZoomRenderer implements Renderer<GraphicsContext> {

	private static final Color BACKGROUND = Color.rgb(0, 163, 232, 100 / 255.0);

	private static final Color FRAME = Color.rgb(255, 0, 100, 1.0);


	@Override
	public Class<? extends Shape> forClass() {
		return ZoomShape.class;
	}

	@Override
	public void render(Shape shape, GraphicsContext context) throws Exception {
		Rectangle2D rect = shape.getBounds();

		context.setFill(BACKGROUND);
		context.fillRect(rect.getX(), rect.getY(), rect.getWidth(), rect.getHeight());

		// Stroke width of 2.
		double d = 2 / context.getTransform().getMxx();

		context.setStroke(FRAME);
		context.setLineWidth(d);
		context.strokeRect(rect.getX(), rect.getY(), rect.getWidth(), rect.getHeight());
	}
}

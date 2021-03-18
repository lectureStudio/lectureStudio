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
import javafx.scene.shape.StrokeLineCap;
import javafx.scene.shape.StrokeLineJoin;

import org.lecturestudio.core.input.KeyEvent;
import org.lecturestudio.core.geometry.Rectangle2D;
import org.lecturestudio.core.model.shape.RectangleShape;
import org.lecturestudio.core.model.shape.Shape;
import org.lecturestudio.core.render.Renderer;
import org.lecturestudio.core.tool.Stroke;
import org.lecturestudio.javafx.beans.converter.ColorConverter;

public class RectangleRenderer implements Renderer<GraphicsContext> {

	private static final Color FOCUS_COLOR = Color.rgb(255, 255, 255, 200 / 255.0);


	@Override
	public Class<? extends Shape> forClass() {
		return RectangleShape.class;
	}

	@Override
	public void render(Shape shape, GraphicsContext context) {
		RectangleShape rectShape = (RectangleShape) shape;
		KeyEvent keyEvent = shape.getKeyEvent();
		Stroke stroke = rectShape.getStroke();
		double width = stroke.getWidth();

		Rectangle2D bounds = shape.getBounds().clone();
		Color color = ColorConverter.INSTANCE.to(stroke.getColor());

		if (keyEvent != null && keyEvent.isAltDown()) {
			context.setFill(color);
			context.fillRect(bounds.getX(), bounds.getY(), bounds.getWidth(), bounds.getHeight());
		}
		else {
			context.setLineWidth(width);
			context.setStroke(color);
			context.strokeRect(bounds.getX(), bounds.getY(), bounds.getWidth(), bounds.getHeight());
		}

		// Focus
		if (shape.isSelected()) {
			if (keyEvent != null && keyEvent.isAltDown()) {
				context.setFill(FOCUS_COLOR);
				context.fillRect(bounds.getX() + width, bounds.getY() + width, bounds.getWidth() - 2 * width, bounds.getHeight() - 2 * width);
			}
			else {
				double penWidth = width * 0.5;

				context.setLineCap(StrokeLineCap.ROUND);
				context.setLineJoin(StrokeLineJoin.ROUND);
				context.setLineWidth(penWidth);
				context.setStroke(FOCUS_COLOR);
				context.strokeRect(bounds.getX(), bounds.getY(), bounds.getWidth(), bounds.getHeight());
			}
		}
	}
}

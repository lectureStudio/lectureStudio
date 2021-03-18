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
import org.lecturestudio.core.geometry.Point2D;
import org.lecturestudio.core.model.shape.LineShape;
import org.lecturestudio.core.model.shape.Shape;
import org.lecturestudio.core.render.Renderer;
import org.lecturestudio.core.tool.Stroke;
import org.lecturestudio.javafx.beans.converter.ColorConverter;

public class LineRenderer implements Renderer<GraphicsContext> {

	private static final Color FOCUS_COLOR = Color.rgb(255, 255, 255, 200 / 255.0);


	@Override
	public Class<? extends Shape> forClass() {
		return LineShape.class;
	}

	@Override
	public void render(Shape shape, GraphicsContext context) {
		LineShape lineShape = (LineShape) shape;
		KeyEvent keyEvent = shape.getKeyEvent();
		Stroke stroke = lineShape.getStroke();
		Point2D p1 = lineShape.getStartPoint();
		Point2D p2 = lineShape.getEndPoint();

		boolean bold = keyEvent != null && keyEvent.isAltDown();
		double width = bold ? stroke.getWidth() * 2 : stroke.getWidth();

		context.setStroke(ColorConverter.INSTANCE.to(stroke.getColor()));
		context.setLineWidth(width);
		context.setLineCap(StrokeLineCap.ROUND);
		context.setLineJoin(StrokeLineJoin.ROUND);
		context.strokeLine(p1.getX(), p1.getY(), p2.getX(), p2.getY());

		// Focus
		if (shape.isSelected()) {
			double penWidth = width * 0.7;

			context.setStroke(FOCUS_COLOR);
			context.setLineWidth(penWidth);
			context.strokeLine(p1.getX(), p1.getY(), p2.getX(), p2.getY());
		}
	}
}

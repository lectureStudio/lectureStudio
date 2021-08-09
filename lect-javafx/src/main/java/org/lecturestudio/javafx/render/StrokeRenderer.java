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
import javafx.scene.effect.BlendMode;
import javafx.scene.paint.Color;
import javafx.scene.shape.StrokeLineCap;
import javafx.scene.shape.StrokeLineJoin;
import org.lecturestudio.core.geometry.PenPoint2D;
import org.lecturestudio.core.geometry.PenStroker;
import org.lecturestudio.core.geometry.Point2D;
import org.lecturestudio.core.model.shape.Shape;
import org.lecturestudio.core.model.shape.StrokeShape;
import org.lecturestudio.core.render.Renderer;
import org.lecturestudio.core.tool.Stroke;
import org.lecturestudio.javafx.beans.converter.ColorConverter;

import java.util.List;

public class StrokeRenderer implements Renderer<GraphicsContext> {

	private static final Color focusColor = Color.rgb(255, 255, 255, 200 / 255.0);


	@Override
	public Class<? extends Shape> forClass() {
		return StrokeShape.class;
	}

	@Override
	public void render(Shape shape, GraphicsContext context) {
		if (!shape.hasPoints()) {
			return;
		}

		System.out.println("Stroke Renderer");

		context.save();

		StrokeShape pShape = (StrokeShape) shape;

		Stroke stroke = pShape.getStroke();
		boolean isSelected = shape.isSelected();
		boolean isOpaque = stroke.getColor().getOpacity() == 255;

		// Copy points for synchronized rendering.
		List<PenPoint2D> shapePoints = shape.getPoints();

		if (isOpaque) {
			drawPen(stroke, pShape.getPenStroker(), isSelected, context);
		}
		else {
			drawHighlighter(stroke, shapePoints, isSelected, context);
		}

		context.restore();
	}

	private void drawPen(Stroke stroke, PenStroker stroker, boolean isSelected, GraphicsContext context) {
		context.setFill(ColorConverter.INSTANCE.to(stroke.getColor()));
		context.setLineCap(StrokeLineCap.ROUND);
		context.setLineJoin(StrokeLineJoin.ROUND);

		drawStrokePath(context, stroker);
		
		// Focus
		if (isSelected) {
			context.setFill(focusColor);

			drawStrokePath(context, stroker);
		}
	}
	
	private void drawHighlighter(Stroke stroke, List<PenPoint2D> points, boolean isSelected, GraphicsContext context) {
		context.setStroke(ColorConverter.INSTANCE.to(stroke.getColor()));
		context.setLineCap(StrokeLineCap.ROUND);
		context.setLineJoin(StrokeLineJoin.ROUND);
		context.setLineWidth(stroke.getWidth());
		context.setGlobalBlendMode(BlendMode.MULTIPLY);
		
		drawPath(context, points);
		
		// Focus
		if (isSelected) {
			context.setStroke(focusColor);
			context.setLineWidth(stroke.getWidth() * 0.75);
			context.setGlobalBlendMode(BlendMode.SRC_OVER);
			
			drawPath(context, points);
		}
	}

	private static void drawStrokePath(GraphicsContext context, PenStroker stroker) {
		List<PenPoint2D> points = stroker.getStrokeList();

		int size = points.size();
		int index = 0;

		Point2D point = points.get(index++);

		context.beginPath();
		context.moveTo(point.getX(), point.getY());

		while (index < size) {
			point = points.get(index++);

			if (index < size) {
				context.lineTo(point.getX(), point.getY());
			}
		}

		context.fill();
	}

	private static void drawPath(GraphicsContext context, List<PenPoint2D> points) {
		int size = points.size();
		int index = 0;

		Point2D point = points.get(index++);

		context.beginPath();
		context.moveTo(point.getX(), point.getY());

		while (index < size) {
			point = points.get(index++);
			
			if (index < size) {
				context.lineTo(point.getX(), point.getY());
			}
		}

		context.stroke();
	}

}

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
import javafx.scene.transform.Affine;

import org.lecturestudio.core.input.KeyEvent;
import org.lecturestudio.core.geometry.PenPoint2D;
import org.lecturestudio.core.model.shape.ArrowShape;
import org.lecturestudio.core.model.shape.Shape;
import org.lecturestudio.core.render.Renderer;
import org.lecturestudio.core.tool.Stroke;
import org.lecturestudio.javafx.beans.converter.ColorConverter;

/**
 * Renders a {@link ArrowShape}.
 *
 * @author Alex Andres
 */
public class ArrowRenderer implements Renderer<GraphicsContext> {

	private static final Color FOCUS_COLOR = Color.rgb(255, 255, 255, 200 / 255.0);


	@Override
	public Class<? extends Shape> forClass() {
		return ArrowShape.class;
	}

	@Override
	public void render(Shape shape, GraphicsContext context) {
		ArrowShape arrowShape = (ArrowShape) shape;
		KeyEvent keyEvent = shape.getKeyEvent();
		Stroke stroke = arrowShape.getStroke();
		PenPoint2D p1 = arrowShape.getStartPoint().clone();
		PenPoint2D p2 = arrowShape.getEndPoint().clone();
		Color color = ColorConverter.INSTANCE.to(stroke.getColor());

		double width = stroke.getWidth();

		Affine transform = context.getTransform();

		createArrowPath(context, keyEvent, p1, p2, width);

		context.setFill(color);
		context.fill();
		context.setTransform(transform);

		// Focus
		if (shape.isSelected()) {
			boolean bold = keyEvent != null && keyEvent.isAltDown();
			boolean twoSided = keyEvent != null && keyEvent.isShiftDown();

			double penWidth = width * 0.7;
			double scale = bold ? width * 2 : width;

			PenPoint2D v1 = (PenPoint2D) p1.clone().subtract(p2).normalize().multiply(scale);
			PenPoint2D v2 = (PenPoint2D) p2.clone().subtract(p1).normalize().multiply(scale);

			if (twoSided) {
				p1 = (PenPoint2D) p1.subtract(v1);
			}

			p2 = (PenPoint2D) p2.subtract(v2);

			transform = context.getTransform();

			createArrowPath(context, keyEvent, p1, p2, penWidth);

			context.setFill(FOCUS_COLOR);
			context.fill();
			context.setTransform(transform);
		}
	}

	private void createArrowPath(GraphicsContext context, KeyEvent keyEvent, PenPoint2D p1, PenPoint2D p2, double penWidth) {
		boolean bold = keyEvent != null && keyEvent.isAltDown();
		boolean twoSided = keyEvent != null && keyEvent.isShiftDown();

		double x1 = p1.getX();
		double y1 = p1.getY();
		double x2 = p2.getX();
		double y2 = p2.getY();
		double w = bold ? penWidth * 2 : penWidth;
		double wd = w / 2;
		double dx = x2 - x1;
		double dy = y2 - y1;
		double angle = Math.atan2(dy, dx);
		double length = Math.sqrt(dx * dx + dy * dy);

		double arrowRatio = 0.5;
		double arrowLength = w * 5;
		double waisting = 0.35;
		double veeX = length - w * 0.5 / arrowRatio;

		double waistX = length - arrowLength * 0.5;
		double waistY = arrowRatio * arrowLength * 0.5 * waisting;
		double arrowWidth = arrowRatio * arrowLength;
		double x = twoSided ? w * 0.5 / arrowRatio + arrowLength * 0.75 : 0;

		context.translate(x1, y1);
		context.rotate(angle * 180 / Math.PI);

		context.beginPath();
		context.moveTo(x, -wd);
		context.lineTo(veeX - arrowLength * 0.75, -wd);
		context.lineTo(veeX - arrowLength, -arrowWidth);
		context.quadraticCurveTo(waistX, -waistY, length, 0);
		context.quadraticCurveTo(waistX, waistY, veeX - arrowLength, arrowWidth);
		context.lineTo(veeX - arrowLength * 0.75, wd);
		context.lineTo(x, wd);

		if (twoSided) {
			waistX = x - arrowLength * 0.5;
			waistY = arrowRatio * arrowLength * 0.5 * waisting;

			context.lineTo(x + arrowLength * 0.25, arrowWidth);
			context.quadraticCurveTo(waistX, waistY, 0, 0);
			context.quadraticCurveTo(waistX, -waistY, x + arrowLength * 0.25, -arrowWidth);
		}

		context.closePath();
	}

}

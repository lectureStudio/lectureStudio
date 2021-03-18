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

import static java.util.Objects.isNull;

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.effect.BlendMode;
import javafx.scene.paint.Color;

import org.lecturestudio.core.geometry.PenPoint2D;
import org.lecturestudio.core.model.shape.PointerShape;
import org.lecturestudio.core.model.shape.Shape;
import org.lecturestudio.core.render.Renderer;
import org.lecturestudio.core.tool.Stroke;
import org.lecturestudio.javafx.beans.converter.ColorConverter;

public class PointerRenderer implements Renderer<GraphicsContext> {

	@Override
	public Class<? extends Shape> forClass() {
		return PointerShape.class;
	}

	@Override
	public void render(Shape shape, GraphicsContext context) {
		PointerShape pointerShape = (PointerShape) shape;
		PenPoint2D point = pointerShape.getPoint();

		if (isNull(point)) {
			return;
		}

		Stroke stroke = pointerShape.getStroke();
		Color color = ColorConverter.INSTANCE.to(stroke.getColor());

		double w = stroke.getWidth();
		double x = point.getX() - w / 2;
		double y = point.getY() - w / 2;

		context.setGlobalBlendMode(BlendMode.MULTIPLY);
		context.setFill(color);
		context.fillOval(x, y, w, w);
	}

}

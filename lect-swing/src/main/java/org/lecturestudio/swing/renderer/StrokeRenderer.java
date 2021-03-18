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
import java.awt.Composite;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.geom.Path2D;
import java.util.ArrayList;
import java.util.List;

import org.lecturestudio.core.geometry.PathFactory;
import org.lecturestudio.core.geometry.PenPoint2D;
import org.lecturestudio.core.geometry.PenStroker;
import org.lecturestudio.core.graphics.StrokeLineCap;
import org.lecturestudio.core.model.shape.StrokeShape;
import org.lecturestudio.core.tool.Stroke;
import org.lecturestudio.core.model.shape.Shape;
import org.lecturestudio.swing.converter.ColorConverter;
import org.lecturestudio.swing.renderer.operation.MultiplyOperation;
import org.lecturestudio.swing.renderer.operation.OperationComposite;

/**
 * Renderer for rendering PenShapes. Despite its name it renders the pen stroke
 * with linear interpolation.
 *
 * @author Alex Andres
 * @author Tobias
 */
public class StrokeRenderer extends BaseRenderer {

	private static final OperationComposite MULTIPLY_COMPOSITE = new OperationComposite(new MultiplyOperation());


	@Override
	public Class<? extends Shape> forClass() {
		return StrokeShape.class;
	}

	@Override
	protected void renderPrivate(Shape shape, Graphics2D context) {
		if (!shape.hasPoints()) {
			return;
		}

		StrokeShape pShape = (StrokeShape) shape;

		Stroke stroke = pShape.getStroke();
		boolean isSelected = shape.isSelected();
		boolean isOpaque = stroke.getColor().getOpacity() == 255;

		// Copy points for synchronized rendering.
		List<PenPoint2D> shapePoints = shape.getPoints();
		List<PenPoint2D> points = new ArrayList<>();

		synchronized (shapePoints) {
			for (PenPoint2D point : shapePoints) {
				points.add(point.clone());
			}
		}

		if (isOpaque) {
			drawPen(stroke, pShape.getPenStroker(), points, isSelected, context);
		}
		else {
			drawHighlighter(stroke, points, isSelected, context);
		}
	}

	private void drawPen(Stroke stroke, PenStroker stroker, List<PenPoint2D> points, boolean isSelected, Graphics2D context) {
		Color color = ColorConverter.INSTANCE.to(stroke.getColor());
		Path2D path = stroker.getStrokePath();

		context.setColor(color);
		context.fill(path);

		// Focus
		if (isSelected) {
			double penWidth = stroke.getWidth() * 0.75;
			path = PathFactory.createPenPath(points, penWidth);

			context.setColor(FOCUS_COLOR);
			context.setStroke(new BasicStroke((float) penWidth, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
			context.fill(path);
		}
	}

	private void drawHighlighter(Stroke pen, List<PenPoint2D> points, boolean isSelected, Graphics2D context) {
		StrokeLineCap lineCap = pen.getStrokeLineCap();
		java.awt.Stroke stroke = new BasicStroke((float) pen.getWidth(), lineCap.ordinal(), BasicStroke.JOIN_ROUND);
		Color color = ColorConverter.INSTANCE.to(pen.getColor());

		// Prepare graphics.
		context.setColor(color);
		context.setStroke(stroke);

		// This should make the line look smoother.
		context.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL, RenderingHints.VALUE_STROKE_NORMALIZE);

		Path2D path = PathFactory.createHighlighterPath(points, pen.getWidth());

		Composite oldComposite = context.getComposite();

		// Use multiply blend mode for non-opaque strokes.
		context.setComposite(MULTIPLY_COMPOSITE);
		context.draw(path);

		// Focus
		if (isSelected) {
			double penWidth = pen.getWidth() * 0.75;

			path = PathFactory.createHighlighterPath(points, penWidth);

			context.setComposite(oldComposite);
			context.setColor(FOCUS_COLOR);
			context.setStroke(new BasicStroke((float) penWidth, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
			context.draw(path);
		}
	}

}

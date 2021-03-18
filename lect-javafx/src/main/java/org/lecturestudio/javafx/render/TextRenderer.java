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

import javafx.geometry.VPos;
import javafx.scene.canvas.GraphicsContext;

import org.lecturestudio.core.geometry.Rectangle2D;
import org.lecturestudio.core.model.shape.Shape;
import org.lecturestudio.core.model.shape.TextShape;
import org.lecturestudio.core.render.Renderer;
import org.lecturestudio.javafx.beans.converter.ColorConverter;
import org.lecturestudio.javafx.beans.converter.FontConverter;

public class TextRenderer implements Renderer<GraphicsContext> {

	@Override
	public Class<? extends Shape> forClass() {
		return TextShape.class;
	}

	@Override
	public void render(Shape shape, GraphicsContext context) throws Exception {
		TextShape textShape = (TextShape) shape;
		String text = textShape.getText();

		if (text.isEmpty()) {
			return;
		}

		Rectangle2D bounds = shape.getBounds();

		double x = bounds.getX();
		double y = bounds.getY();

		context.save();
		context.setFont(FontConverter.INSTANCE.to(textShape.getFont()));
		context.setFill(ColorConverter.INSTANCE.to(textShape.getTextColor()));
		context.setTextBaseline(VPos.TOP);
		context.fillText(text, x, y);
		context.restore();
	}

}

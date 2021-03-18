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

import java.util.Iterator;

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.effect.BlendMode;

import org.lecturestudio.core.geometry.Rectangle2D;
import org.lecturestudio.core.model.shape.Shape;
import org.lecturestudio.core.model.shape.TextSelectionShape;
import org.lecturestudio.core.render.Renderer;
import org.lecturestudio.javafx.beans.converter.ColorConverter;

public class TextSelectionRenderer implements Renderer<GraphicsContext> {

	@Override
	public Class<? extends Shape> forClass() {
		return TextSelectionShape.class;
	}

	@Override
	public void render(Shape shape, GraphicsContext context) throws Exception {
		TextSelectionShape selectionShape = (TextSelectionShape) shape;
		Iterator<Rectangle2D> selection = selectionShape.getSelection();

		context.setGlobalBlendMode(BlendMode.MULTIPLY);
		context.setFill(ColorConverter.INSTANCE.to(selectionShape.getColor()));

		Rectangle2D rect;

		while (selection.hasNext()) {
			rect = selection.next();

			context.fillRect(rect.getX(), rect.getY(), rect.getWidth(), rect.getHeight());
		}
	}
}

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

import java.awt.Graphics2D;
import java.util.Iterator;

import org.lecturestudio.core.geometry.Rectangle2D;
import org.lecturestudio.core.model.shape.Shape;
import org.lecturestudio.core.model.shape.TextSelectionShape;
import org.lecturestudio.swing.converter.ColorConverter;
import org.lecturestudio.swing.converter.Rectangle2DConverter;
import org.lecturestudio.swing.renderer.operation.MultiplyOperation;
import org.lecturestudio.swing.renderer.operation.OperationComposite;

public class TextSelectionRenderer extends BaseRenderer {

	protected static final OperationComposite MULTIPLY_COMPOSITE = new OperationComposite(new MultiplyOperation());


	@Override
	public Class<? extends Shape> forClass() {
		return TextSelectionShape.class;
	}

	@Override
	protected void renderPrivate(Shape shape, Graphics2D context) {
		TextSelectionShape selectionShape = (TextSelectionShape) shape;
		Iterator<Rectangle2D> selection = selectionShape.getSelection();

		context.setColor(ColorConverter.INSTANCE.to(selectionShape.getColor()));
		context.setComposite(MULTIPLY_COMPOSITE);

		while (selection.hasNext()) {
			context.fill(Rectangle2DConverter.INSTANCE.to(selection.next()));
		}
	}
}

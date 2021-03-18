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
import java.awt.Graphics2D;
import java.awt.RenderingHints;

import org.lecturestudio.core.model.shape.Shape;
import org.lecturestudio.core.render.Renderer;

public abstract class BaseRenderer implements Renderer<Graphics2D> {

	protected static final Color FOCUS_COLOR = new Color(255, 255, 255, 200);

	protected static final int CAP = BasicStroke.CAP_ROUND;

	protected static final int JOIN = BasicStroke.JOIN_ROUND;


	abstract protected void renderPrivate(Shape shape, Graphics2D context);


	@Override
	public void render(Shape shape, Graphics2D context) {
		Graphics2D g = (Graphics2D) context.create();
		g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

		renderPrivate(shape, g);

		g.dispose();
	}

}

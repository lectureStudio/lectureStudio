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

package org.lecturestudio.core.render;

import static java.util.Objects.nonNull;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.lecturestudio.core.graphics.GraphicsContext;
import org.lecturestudio.core.model.shape.Shape;

public class RenderService {

	private final Map<Class<? extends Shape>, Renderer<?>> renderers = new HashMap<>();


	/**
	 * Registers a renderer.
	 * 
	 * @param renderer
	 */
	public void registerRenderer(Renderer<?> renderer) {
		renderers.put(renderer.forClass(), renderer);
	}

	public boolean hasRenderer(Class<? extends Shape> cls) {
		return renderers.get(cls) != null;
	}

	public final void renderShapes(List<Shape> shapes, GraphicsContext graphicsContext) throws Exception {
		final Object context = graphicsContext.get();

		for (Shape shape : shapes) {
			if (nonNull(shape)) {
				Renderer renderer = renderers.get(shape.getClass());
				if (nonNull(renderer)) {
					renderer.render(shape, context);
				}
			}
		}
	}

}

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

import static java.util.Objects.isNull;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;

import org.lecturestudio.core.graphics.GraphicsContext;
import org.lecturestudio.core.model.shape.Shape;
import org.lecturestudio.core.view.ViewType;

public class RenderContext {

	private final Map<ViewType, RenderService> renderers = new EnumMap<>(ViewType.class);
	
	
	public void setRenderer(ViewType type, RenderService service) {
		renderers.put(type, service);
	}
	
	public void render(ViewType type, List<Shape> shapes, GraphicsContext graphicsContext) throws Exception {
		final RenderService renderer = renderers.get(type);

		if (isNull(renderer)) {
			return;
		}
		
		renderer.renderShapes(shapes, graphicsContext);
	}
	
}

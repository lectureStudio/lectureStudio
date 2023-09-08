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

package org.lecturestudio.core.view;

import static java.util.Objects.isNull;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.EnumMap;
import java.util.Map;

import org.lecturestudio.core.model.Page;
import org.lecturestudio.core.model.shape.Shape;
import org.lecturestudio.core.tool.ToolType;

public class PageObjectRegistry {

	private final Map<ToolType, ShapeView> toolViewMap;


	public PageObjectRegistry() {
		toolViewMap = new EnumMap<>(ToolType.class);
	}

	public void register(ToolType toolType, Class<? extends PageObjectView<? extends Shape>> objectViewClass) throws IllegalArgumentException {
		ShapeView shapeView = toolViewMap.get(toolType);

		if (isNull(shapeView)) {
			Type[] genericInterfaces = objectViewClass.getGenericInterfaces();

			if (genericInterfaces.length == 0) {
				throw new IllegalArgumentException("PageObjectView does not implement a generic interface.");
			}

			ParameterizedType parameterizedType = (ParameterizedType) genericInterfaces[0];
			Type[] typeArguments = parameterizedType.getActualTypeArguments();

			if (isNull(typeArguments) || typeArguments.length == 0) {
				throw new IllegalArgumentException("PageObjectView does not implement a generic interface with a type.");
			}

			Class<?> genericClass = (Class<?>) typeArguments[0];

			if (!Shape.class.isAssignableFrom(genericClass)) {
				throw new IllegalArgumentException("PageObjectView implements wrong generic type.");
			}

			Class<? extends Shape> shapeClass = (Class<? extends Shape>) genericClass;

			shapeView = new ShapeView(shapeClass, objectViewClass);
			toolViewMap.put(toolType, shapeView);
		}
	}

	public boolean containsViewShapes(ToolType toolType, Page page) {
		ShapeView shapeView = toolViewMap.get(toolType);

		if (isNull(shapeView)) {
			return false;
		}

		return page.contains(shapeView.shapeClass);
	}

	public Class<? extends PageObjectView<? extends Shape>> getPageObjectViewClass(ToolType toolType) {
		ShapeView shapeView = toolViewMap.get(toolType);

		if (isNull(shapeView)) {
			return null;
		}

		return shapeView.viewClass;
	}

	public Class<? extends Shape> getShapeClass(ToolType toolType) {
		ShapeView shapeView = toolViewMap.get(toolType);

		if (isNull(shapeView)) {
			return null;
		}

		return shapeView.shapeClass;
	}



	private static class ShapeView {

		final Class<? extends Shape> shapeClass;

		final Class<? extends PageObjectView<? extends Shape>> viewClass;


		ShapeView(Class<? extends Shape> shapeClass, Class<? extends PageObjectView<? extends Shape>> viewClass) {
			this.shapeClass = shapeClass;
			this.viewClass = viewClass;
		}

	}

}

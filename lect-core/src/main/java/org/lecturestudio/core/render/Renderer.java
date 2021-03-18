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

import org.lecturestudio.core.model.shape.Shape;

/**
 * Basic interface for implementing a Renderer. Each Renderer specifies a class
 * of Shapes it can render and provides a render method for rendering an
 * instance of this shape class.
 * 
 * @author Tobias
 */
public interface Renderer<T> {

	/**
	 * Returns the class the renderer is able to render.
	 * 
	 * @return the {@code Class} of the shape.
	 */
	public Class<? extends Shape> forClass();

	/**
	 * Renders an instance of the class specified by forClass() with the given
	 * scale factors to the given Graphics2D object.
	 * 
	 * @param shape
	 * @param context
	 */
	void render(Shape shape, T context) throws Exception;

}

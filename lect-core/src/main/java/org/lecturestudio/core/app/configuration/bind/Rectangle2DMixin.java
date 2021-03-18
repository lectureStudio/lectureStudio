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

package org.lecturestudio.core.app.configuration.bind;

import com.fasterxml.jackson.annotation.JsonIgnore;

import org.lecturestudio.core.geometry.Point2D;
import org.lecturestudio.core.geometry.Rectangle2D;

/**
 * A {@link Rectangle2D} mixin-class to be registered with the FasterXML/jackson
 * ObjectMapper. This annotation class defines methods to be ignored while
 * serializing and deserializing Rectangle2D objects.
 *
 * @author Alex Andres
 */
public abstract class Rectangle2DMixin {

	@JsonIgnore
	abstract Point2D getLocation();

	@JsonIgnore
	abstract boolean isEmpty();

}

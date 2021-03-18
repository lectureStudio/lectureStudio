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

package org.lecturestudio.core.geometry;

/**
 * A set of values for describing vertical and horizontal positioning.
 *
 * @author Alex Andres
 */
public enum Position {

	/** Positioning on the top vertically and on the left horizontally. */
	TOP_LEFT,

	/** Positioning on the top vertically and on the center horizontally. */
	TOP_CENTER,

	/** Positioning on the top vertically and on the right horizontally. */
	TOP_RIGHT,

	/** Positioning on the center vertically and on the left horizontally. */
	CENTER_LEFT,

	/** Positioning on the center both vertically and horizontally. */
	CENTER,

	/** Positioning on the center vertically and on the right horizontally. */
	CENTER_RIGHT,

	/** Positioning on the bottom vertically and on the left horizontally. */
	BOTTOM_LEFT,

	/** Positioning on the bottom vertically and on the center horizontally. */
	BOTTOM_CENTER,

	/** Positioning on the bottom vertically and on the right horizontally. */
	BOTTOM_RIGHT;

}

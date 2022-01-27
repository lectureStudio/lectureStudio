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

package org.lecturestudio.core.camera;

import static java.util.Objects.nonNull;

import org.lecturestudio.core.geometry.Rectangle2D;

/**
 * The aspect ratio of an image the camera should capture. The aspect ratio can
 * be used to select the desired camera format that has the same proportional
 * difference between the width and height of the captured frame.
 *
 * @author Alex Andres
 */
public enum AspectRatio {

	/** The default and old 4:3 aspect ratio. */
	Standard,

	/** Any widescreen aspect ratio, like 16:9, 16:10, etc. */
	Widescreen;


	public static AspectRatio forRect(Rectangle2D rect) {
		AspectRatio ratio = AspectRatio.Standard;

		if (nonNull(rect)) {
			if (rect.getHeight() / rect.getWidth() < 0.75) {
				ratio = AspectRatio.Widescreen;
			}
		}

		return ratio;
	}
}

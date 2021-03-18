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

package org.lecturestudio.javafx.beans.converter;

import static java.util.Objects.nonNull;

import org.lecturestudio.core.beans.Converter;
import org.lecturestudio.core.beans.ObjectProperty;
import org.lecturestudio.core.camera.CameraFormat;
import org.lecturestudio.core.geometry.Rectangle2D;

/**
 * Rectangle2D to CameraFormat and vice-versa converter.
 *
 * @author Alex Andres
 */
public class CameraFormatConverter implements Converter<Rectangle2D, CameraFormat> {

	private final ObjectProperty<Rectangle2D> property;


	public CameraFormatConverter(ObjectProperty<Rectangle2D> property) {
		this.property = property;
	}

	@Override
	public CameraFormat to(Rectangle2D value) {
		CameraFormat format = null;

		if (nonNull(value)) {
			format = new CameraFormat((int) value.getWidth(), (int) value.getHeight(), 30);
		}

		return format;
	}

	@Override
	public Rectangle2D from(CameraFormat value) {
		Rectangle2D rect = new Rectangle2D(0, 0, value.getWidth(), value.getHeight());
		Rectangle2D propRect = property.get();

		if (nonNull(propRect)) {
			rect.setLocation(propRect.getX(), propRect.getY());
		}

		return rect;
	}

}

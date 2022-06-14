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

import javafx.geometry.VPos;

import org.lecturestudio.core.beans.Converter;
import org.lecturestudio.core.geometry.Position;

public class VPositionConverter implements Converter<Position, VPos> {

	public static final VPositionConverter INSTANCE = new VPositionConverter();


	@Override
	public VPos to(Position position) {
		return switch (position) {
			case CENTER -> VPos.CENTER;
			case TOP_LEFT, TOP_CENTER, TOP_RIGHT -> VPos.TOP;
			case BOTTOM_LEFT, BOTTOM_CENTER, BOTTOM_RIGHT -> VPos.BOTTOM;
			default -> VPos.CENTER;
		};
	}

	@Override
	public Position from(VPos position) {
		return switch (position) {
			case CENTER -> Position.CENTER;
			case TOP -> Position.TOP_CENTER;
			case BOTTOM -> Position.BOTTOM_CENTER;
			default -> Position.CENTER;
		};
	}

}

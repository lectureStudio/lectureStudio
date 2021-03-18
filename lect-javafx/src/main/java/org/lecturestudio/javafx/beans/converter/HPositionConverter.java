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

import javafx.geometry.HPos;

import org.lecturestudio.core.beans.Converter;
import org.lecturestudio.core.geometry.Position;

public class HPositionConverter implements Converter<Position, HPos> {

	public static final HPositionConverter INSTANCE = new HPositionConverter();


	@Override
	public HPos to(Position position) {
		switch (position) {
			case CENTER:
				return HPos.CENTER;

			case TOP_LEFT:
			case CENTER_LEFT:
			case BOTTOM_LEFT:
				return HPos.LEFT;

			case TOP_RIGHT:
			case CENTER_RIGHT:
			case BOTTOM_RIGHT:
				return HPos.RIGHT;

			default:
				return HPos.CENTER;
		}
	}

	@Override
	public Position from(HPos position) {
		switch (position) {
			case CENTER:
				return Position.CENTER;

			case LEFT:
				return Position.CENTER_LEFT;

			case RIGHT:
				return Position.CENTER_RIGHT;

			default:
				return Position.CENTER;
		}
	}

}

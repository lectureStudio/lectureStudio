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

package org.lecturestudio.javafx.factory;

import static java.util.Objects.isNull;

import javafx.scene.control.ListCell;

import org.lecturestudio.core.geometry.Dimension2D;

public class DimensionListCell extends ListCell<Dimension2D> {

	public DimensionListCell() {
		setGraphic(null);
	}

	@Override
	protected void updateItem(Dimension2D item, boolean empty) {
		super.updateItem(item, empty);

		if (isNull(item) || empty) {
			setText("");
		}
		else {
			String sep = item.getWidth() < 1000 ? " " : "";
			setText(String.format("%s%5.0f x %.0f",
					sep, item.getWidth(), item.getHeight()));
		}
	}

}

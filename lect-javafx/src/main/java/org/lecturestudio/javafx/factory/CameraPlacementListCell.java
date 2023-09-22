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
import static java.util.Objects.nonNull;

import java.util.ResourceBundle;

import javafx.scene.control.ListCell;
import org.lecturestudio.core.geometry.Position;

import javax.inject.Inject;

public class CameraPlacementListCell extends ListCell<Position> {
	private final ResourceBundle resourceBundle;

	@Inject
	public CameraPlacementListCell(ResourceBundle resourceBundle) {
		this.resourceBundle = resourceBundle;
	}

	@Override
	protected void updateItem(Position item, boolean empty) {
		super.updateItem(item, empty);

		setGraphic(null);

		if (nonNull(item)) {
			switch (item) {
				case TOP_RIGHT -> setText(resourceBundle.getString("video.export.position.top.right"));
				case TOP_LEFT -> setText(resourceBundle.getString("video.export.position.top.left"));
				case BOTTOM_LEFT -> setText(resourceBundle.getString("video.export.position.bottom.right"));
				case BOTTOM_RIGHT -> setText(resourceBundle.getString("video.export.position.bottom.left"));
				case CENTER -> setText(resourceBundle.getString("video.export.position.none"));
			}
		}
		if (isNull(item) || empty) {
			setText("");
		}
	}

}

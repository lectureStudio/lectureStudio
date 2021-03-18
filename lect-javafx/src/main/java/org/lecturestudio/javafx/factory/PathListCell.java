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

import java.nio.file.Path;

import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.layout.VBox;

public class PathListCell extends ListCell<Path> {

	private final Label nameLabel = new Label();

	private final Label parentLabel = new Label();


	public PathListCell() {
		VBox container = new VBox(nameLabel, parentLabel);
		container.getStyleClass().add("path-container");

		nameLabel.getStyleClass().add("path-name");
		parentLabel.getStyleClass().add("path-parent");

		setGraphic(container);
	}

	@Override
	protected void updateItem(Path item, boolean empty) {
		super.updateItem(item, empty);

		if (isNull(item) || empty) {
			nameLabel.setText("");
			parentLabel.setText("");
		}
		else {
			nameLabel.setText(item.getFileName().toString());
			parentLabel.setText(item.getParent().toString());
		}
	}

}

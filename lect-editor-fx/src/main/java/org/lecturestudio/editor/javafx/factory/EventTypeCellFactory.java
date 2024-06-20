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

package org.lecturestudio.editor.javafx.factory;

import java.util.ResourceBundle;

import javax.inject.Inject;

import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.util.Callback;

import org.lecturestudio.editor.api.view.model.PageEvent;
import org.lecturestudio.javafx.control.SvgIcon;

public class EventTypeCellFactory implements Callback<TableColumn<Object, PageEvent>, TableCell<Object, PageEvent>> {

	private final ResourceBundle resources;


	@Inject
	public EventTypeCellFactory(ResourceBundle resources) {
		this.resources = resources;
	}

	@Override
	public TableCell<Object, PageEvent> call(TableColumn<Object, PageEvent> param) {
		return new EventTypeCell();
	}



	private class EventTypeCell extends TableCell<Object, PageEvent> {

		@Override
		protected void updateItem(PageEvent item, boolean empty) {
			super.updateItem(item, empty);

			if (!empty) {
				String typeName = item.getActionType().toString().toLowerCase();
				String eventName = "page.events." + typeName.replace("_", ".");
				String iconName = typeName.replace("_", "-") + "-icon";

				if (resources.containsKey(eventName)) {
					SvgIcon icon = new SvgIcon();
					icon.getStyleClass().add(iconName);

					int compositeCount = item.getCompositeActions().size();
					if (compositeCount > 1) {
						setText(resources.getString(eventName) + String.format(" (%d)", compositeCount));
					}
					else {
						setText(resources.getString(eventName));
					}

					setGraphic(icon);
				}
			}
			else {
				setText(null);
				setGraphic(null);
			}
		}
	}

}

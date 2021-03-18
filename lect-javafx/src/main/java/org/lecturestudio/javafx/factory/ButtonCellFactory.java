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

import javafx.beans.NamedArg;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonBase;
import javafx.scene.control.Label;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.ToggleButton;
import javafx.util.Callback;

import org.lecturestudio.javafx.control.SvgIcon;
import org.lecturestudio.javafx.event.CellButtonActionEvent;

public class ButtonCellFactory implements Callback<TableColumn<Object, Boolean>, TableCell<Object, Boolean>> {

	private final ButtonBase button;


	public ButtonCellFactory(@NamedArg("button") ButtonBase button) {
		this.button = button;
	}

	@Override
	public TableCell<Object, Boolean> call(TableColumn<Object, Boolean> param) {
		return new ButtonCell();
	}



	private class ButtonCell extends TableCell<Object, Boolean> {

		private final ButtonBase cellButton;


		ButtonCell() {
			if (button instanceof ToggleButton) {
				cellButton = new ToggleButton();
			}
			else {
				cellButton = new Button();
			}

			if (button.getGraphic() instanceof SvgIcon) {
				SvgIcon icon = new SvgIcon();
				icon.getStyleClass().addAll(button.getGraphic().getStyleClass());

				cellButton.setGraphic(icon);
			}
			else if (button.getGraphic() instanceof Label) {
				Label graphic = (Label) button.getGraphic();
				Label label = new Label(graphic.getText(), graphic.getGraphic());
				label.getStyleClass().addAll(graphic.getStyleClass());

				cellButton.setGraphic(label);
			}

			cellButton.setText(button.getText());
			cellButton.setTooltip(button.getTooltip());
			cellButton.setDisable(button.isDisable());
			cellButton.setVisible(button.isVisible());
			cellButton.setId(button.getId());
			cellButton.setStyle(button.getStyle());
			cellButton.getStyleClass().addAll(button.getStyleClass());
			cellButton.setPrefWidth(button.getPrefWidth());
			cellButton.setPrefHeight(button.getPrefHeight());
			cellButton.setFocusTraversable(false);
			cellButton.setOnAction(event -> {
				setButtonSelected(false);

				Object item = getTableView().getItems().get(getIndex());

				button.getOnAction().handle(new CellButtonActionEvent(cellButton, item));
			});
		}

		@Override
		protected void updateItem(Boolean item, boolean empty) {
			super.updateItem(item, empty);

			if (!empty) {
				setButtonSelected(item);
				setGraphic(cellButton);
			}
			else {
				setGraphic(null);
			}
		}

		private void setButtonSelected(boolean value) {
			if (cellButton instanceof ToggleButton) {
				ToggleButton button = (ToggleButton) cellButton;
				button.setSelected(value);
			}
		}
	}

}

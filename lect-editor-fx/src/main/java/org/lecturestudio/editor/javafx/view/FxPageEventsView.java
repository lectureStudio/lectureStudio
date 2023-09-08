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

package org.lecturestudio.editor.javafx.view;

import static java.util.Objects.nonNull;

import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.TableView;

import java.util.List;

import org.lecturestudio.core.beans.ObjectProperty;
import org.lecturestudio.core.view.ConsumerAction;
import org.lecturestudio.editor.api.view.PageEventsView;
import org.lecturestudio.editor.api.view.model.PageEvent;
import org.lecturestudio.javafx.event.CellButtonActionEvent;
import org.lecturestudio.javafx.layout.ContentPane;
import org.lecturestudio.javafx.util.FxUtils;
import org.lecturestudio.javafx.view.FxmlView;

@FxmlView(name = "page-events", presenter = org.lecturestudio.editor.api.presenter.PageEventsPresenter.class)
public class FxPageEventsView extends ContentPane implements PageEventsView {

	@FXML
	private TableView<PageEvent> eventsTableView;

	private ConsumerAction<PageEvent> deleteEventAction;

	private ObjectProperty<PageEvent> selectedPageEvent;
	private ConsumerAction<PageEvent> selectEventAction;


	public FxPageEventsView() {
		super();
	}

	@Override
	public void bindSelectedPageEvent(ObjectProperty<PageEvent> property) {
		selectedPageEvent = property;

		property.addListener((observable, oldValue, newValue) -> {
			FxUtils.invoke(() -> {
				eventsTableView.getSelectionModel().select(newValue);
			});
		});
	}

	@Override
	public void setPageEvents(List<PageEvent> events) {
		FxUtils.invoke(() -> {
			eventsTableView.getItems().setAll(events);
		});
	}

	@Override
	public void setOnSelectEvent(ConsumerAction<PageEvent> action) {
		this.selectEventAction = action;
	}

	@Override
	public void setOnDeleteEvent(ConsumerAction<PageEvent> action) {
		this.deleteEventAction = action;
	}

	@FXML
	private void onDeleteEvent(CellButtonActionEvent event) {
		PageEvent item = (PageEvent) event.getCellItem();

		executeAction(deleteEventAction, item);
	}

	@FXML
	private void initialize() {
		eventsTableView.setItems(FXCollections.observableArrayList());
		eventsTableView.getSelectionModel().selectedItemProperty().addListener(observable -> {
			PageEvent selectedItem = eventsTableView.getSelectionModel().getSelectedItem();

			if (nonNull(selectedPageEvent) && nonNull(selectedItem)) {
				selectedPageEvent.set(selectedItem);
			}
		});

		FxUtils.invoke(() -> {
			eventsTableView.setOnMouseClicked(clickEvent -> {
				if (clickEvent.getClickCount() == 2) {
					executeAction(selectEventAction, selectedPageEvent.get());
					clickEvent.consume();
				}
			});
		});
	}
}

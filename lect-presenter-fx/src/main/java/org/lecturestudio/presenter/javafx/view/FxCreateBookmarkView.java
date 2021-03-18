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

package org.lecturestudio.presenter.javafx.view;

import static java.util.Objects.isNull;

import java.util.List;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;

import org.lecturestudio.core.view.Action;
import org.lecturestudio.core.view.ConsumerAction;
import org.lecturestudio.javafx.event.CellButtonActionEvent;
import org.lecturestudio.javafx.layout.ContentPane;
import org.lecturestudio.javafx.util.FxUtils;
import org.lecturestudio.javafx.view.FxmlView;
import org.lecturestudio.presenter.api.model.Bookmark;
import org.lecturestudio.presenter.api.view.CreateBookmarkView;
import org.lecturestudio.presenter.javafx.view.model.BookmarkTableItem;

@FxmlView(name = "create-bookmark")
public class FxCreateBookmarkView extends ContentPane implements CreateBookmarkView {

	private final ObservableList<BookmarkTableItem> bookmarkItems;

	private ConsumerAction<String> createBookmarkAction;

	private ConsumerAction<Bookmark> deleteBookmarkAction;

	@FXML
	private TextField acceleratorTextField;

	@FXML
	private TableView<BookmarkTableItem> bookmarkTableView;

	@FXML
	private Button closeButton;


	public FxCreateBookmarkView() {
		super();

		bookmarkItems = FXCollections.observableArrayList();
	}

	@Override
	public void setBookmarks(List<Bookmark> bookmarkList) {
		if (isNull(bookmarkList)) {
			return;
		}

		// Simply replace all items.
		bookmarkItems.clear();

		for (Bookmark bookmark : bookmarkList) {
			bookmarkItems.add(new BookmarkTableItem(bookmark));
		}
	}

	@Override
	public void removeBookmark(Bookmark bookmark) {
		bookmarkItems.remove(new BookmarkTableItem(bookmark));
	}

	@Override
	public void setOnClose(Action action) {
		FxUtils.bindAction(closeButton, action);
	}

	@Override
	public void setOnCreateBookmark(ConsumerAction<String> action) {
		this.createBookmarkAction = action;
	}

	@Override
	public void setOnDeleteBookmark(ConsumerAction<Bookmark> action) {
		this.deleteBookmarkAction = action;
	}

	@FXML
	public void onDeleteBookmark(CellButtonActionEvent event) {
		BookmarkTableItem item = (BookmarkTableItem) event.getCellItem();
		Bookmark bookmark = item.getBookmark();

		executeAction(deleteBookmarkAction, bookmark);
	}

	@FXML
	private void initialize() {
		bookmarkTableView.setItems(bookmarkItems);

		acceleratorTextField.addEventFilter(KeyEvent.KEY_PRESSED, event -> {
			KeyCode code = event.getCode();

			if (code.isLetterKey() || code.isDigitKey()) {
				executeAction(createBookmarkAction, event.getText());
			}
		});
		acceleratorTextField.addEventFilter(KeyEvent.ANY, event -> {
			// Allow focus transfer.
			if (event.getCode() != KeyCode.TAB) {
				// Keep the TextField empty.
				event.consume();
			}
		});
	}

}

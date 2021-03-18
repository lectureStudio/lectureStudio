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

package org.lecturestudio.presenter.javafx.view.model;

import java.util.Objects;

import javafx.beans.property.IntegerProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.scene.input.KeyCombination;

import org.lecturestudio.core.model.Document;
import org.lecturestudio.core.model.Page;
import org.lecturestudio.presenter.api.model.Bookmark;

public class BookmarkTableItem {

	private final StringProperty documentTitle;
	private final IntegerProperty slideNumber;
	private final StringProperty accelerator;
	private final ObjectProperty<Page> bookmarkPage;

	private final Bookmark bookmark;


	public BookmarkTableItem(Bookmark bookmark) {
		Page page = bookmark.getPage();
		Document doc = page.getDocument();

		KeyCombination keyCombo = KeyCombination.keyCombination(bookmark.getShortcut());

		this.documentTitle = new SimpleStringProperty(doc.getName());
		this.slideNumber = new SimpleIntegerProperty(doc.getPageIndex(page) + 1);
		this.accelerator = new SimpleStringProperty(keyCombo.getDisplayText());
		this.bookmarkPage = new SimpleObjectProperty<>(page);

		this.bookmark = bookmark;
	}

	/**
	 * Returns the bookmark associated with this table item.
	 * 
	 * @return the bookmark.
	 */
	public Bookmark getBookmark() {
		return bookmark;
	}

	public String getDocumentTitle() {
		return documentTitle.get();
	}

	public StringProperty documentTitleProperty() {
		return documentTitle;
	}

	public int getSlideNumber() {
		return slideNumber.get();
	}

	public IntegerProperty slideNumberProperty() {
		return slideNumber;
	}

	public String getAccelerator() {
		return accelerator.get();
	}

	public StringProperty acceleratorProperty() {
		return accelerator;
	}

	public Page getBookmarkPage() {
		return bookmarkPage.get();
	}

	public ObjectProperty<Page> bookmarkPageProperty() {
		return bookmarkPage;
	}

	@Override
	public int hashCode() {
		return Objects.hash(bookmark);
	}

	@Override
	public boolean equals(Object obj) {
		if (obj == null) {
			return false;
		}
		if (this == obj) {
			return true;
		}
		if (getClass() != obj.getClass()) {
			return false;
		}

		BookmarkTableItem other = (BookmarkTableItem) obj;

		return Objects.equals(bookmark, other.bookmark);
	}

}

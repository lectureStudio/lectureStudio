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

package org.lecturestudio.presenter.api.model;

public interface BookmarksListener {

	/**
	 * Called whenever a new Bookmark is added.
	 *
	 * @param bookmarks The Bookmarks collection.
	 * @param bookmark The added Bookmark.
	 */
	default void bookmarkAdded(Bookmarks bookmarks, Bookmark bookmark) {
		bookmarksChanged(bookmarks);
	}

	/**
	 * Called whenever a Bookmark has been removed.
	 *
	 * @param bookmarks The Bookmarks collection.
	 * @param bookmark The removed Bookmark.
	 */
	default void bookmarkRemoved(Bookmarks bookmarks, Bookmark bookmark) {
		bookmarksChanged(bookmarks);
	}

	/**
	 * Called whenever the Bookmarks collection has been changed. This method is by
	 * default a no-op.
	 *
	 * @param bookmarks The Bookmarks collection.
	 */
	default void bookmarksChanged(Bookmarks bookmarks) {

	}

}

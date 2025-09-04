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

import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.lecturestudio.core.model.Document;
import org.lecturestudio.core.util.ObservableHashMap;
import org.lecturestudio.core.util.ObservableMap;

public class Bookmarks {

	private final List<BookmarksListener> listeners = new ArrayList<>();

	private final ObservableMap<Document, List<Bookmark>> bookmarks = new ObservableHashMap<>();


	public void add(Bookmark bookmark) {
		Document doc = bookmark.page().getDocument();

		if (isNull(doc)) {
			return;
		}

		List<Bookmark> set = bookmarks.get(doc);

		if (isNull(set)) {
			set = new ArrayList<>();
			bookmarks.put(doc, set);
		}

		int index = set.indexOf(bookmark);
		if (index != -1) {
			set.set(index, bookmark);
		}
		else {
			set.add(bookmark);
		}

		fireAdded(bookmark);
	}

	public List<Bookmark> getAllBookmarks() {
		List<Bookmark> bookmarkList = new ArrayList<>();

		for (List<Bookmark> docList : bookmarks.values()) {
			bookmarkList.addAll(docList);
		}

		return bookmarkList;
	}

	public Bookmark getLastBookmark(Document doc) {
		List<Bookmark> docBookmarks = bookmarks.get(doc);

		if (isNull(docBookmarks) || docBookmarks.isEmpty()) {
			return null;
		}

		return docBookmarks.get(docBookmarks.size() - 1);
	}

	public List<Bookmark> getDocumentBookmarks(Document doc) {
		return bookmarks.get(doc);
	}

	public Bookmark getBookmark(String shortcut) {
		for (Document doc : bookmarks.keySet()) {
			for (Bookmark bookmark : bookmarks.get(doc)) {
				String accelerator = bookmark.shortcut();

				if (nonNull(accelerator) && accelerator.equalsIgnoreCase(shortcut)) {
					return bookmark;
				}
			}
		}
		return null;
	}

	public void removeBookmark(Bookmark bookmark) {
		for (Document doc : bookmarks.keySet()) {
			Iterator<Bookmark> iter = bookmarks.get(doc).iterator();

			while (iter.hasNext()) {
				Bookmark b = iter.next();
				String accelerator = b.shortcut();

				if (nonNull(accelerator) && accelerator.equalsIgnoreCase(bookmark.shortcut())) {
					iter.remove();

					fireRemoved(b);
				}
			}
		}
	}

	public void clear(Document doc) {
		List<Bookmark> list = bookmarks.remove(doc);

		if (nonNull(list)) {
			list.clear();

			fireChanged();
		}
	}

	public int size() {
		return getAllBookmarks().size();
	}

	public void addBookmarksListener(BookmarksListener listener) {
		if (!listeners.contains(listener)) {
			listeners.add(listener);
		}
	}

	public void removeBookmarksListener(BookmarksListener listener) {
		listeners.remove(listener);
	}

	private void fireAdded(Bookmark bookmark) {
		for (BookmarksListener listener : listeners) {
			listener.bookmarkAdded(this, bookmark);
		}
	}

	private void fireRemoved(Bookmark bookmark) {
		for (BookmarksListener listener : listeners) {
			listener.bookmarkRemoved(this, bookmark);
		}
	}

	private void fireChanged() {
		for (BookmarksListener listener : listeners) {
			listener.bookmarksChanged(this);
		}
	}

}

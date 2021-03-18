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

package org.lecturestudio.presenter.api.service;

import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;

import java.util.List;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.lecturestudio.core.model.Document;
import org.lecturestudio.core.model.DocumentList;
import org.lecturestudio.core.model.Page;
import org.lecturestudio.core.service.DocumentService;
import org.lecturestudio.presenter.api.model.Bookmark;
import org.lecturestudio.presenter.api.model.BookmarkException;
import org.lecturestudio.presenter.api.model.BookmarkKeyException;
import org.lecturestudio.presenter.api.model.Bookmarks;

@Singleton
public class BookmarkService {

	private final DocumentService documentService;

	private final Bookmarks bookmarks;

	private Page prevBookmarkPage;


	@Inject
	public BookmarkService(DocumentService documentService) {
		this.documentService = documentService;
		this.bookmarks = new Bookmarks();
	}

	public Bookmarks getBookmarks() {
		return bookmarks;
	}

	public void clearBookmarks() {
		Document selectedDoc = documentService.getDocuments().getSelectedDocument();

		bookmarks.clear(selectedDoc);
	}

	public void clearBookmarks(Document document) {
		bookmarks.clear(document);
	}

	public Bookmark createBookmark(String keyStr) throws BookmarkException {
		Document selectedDoc = documentService.getDocuments().getSelectedDocument();
		Page page = selectedDoc.getCurrentPage();

		if (isNull(page)) {
			throw new BookmarkException("No document selected");
		}

		// Check if a bookmark with the given shortcut already exists.
		List<Bookmark> docBookmarks = bookmarks.getDocumentBookmarks(selectedDoc);

		if (nonNull(docBookmarks)) {
			for (Bookmark bookmark : docBookmarks) {
				if (bookmark.getShortcut().equalsIgnoreCase(keyStr)) {
					throw new BookmarkKeyException("Bookmark key is already assigned to another bookmark");
				}
			}
		}

		Bookmark bookmark = new Bookmark(keyStr, page);

		bookmarks.add(bookmark);

		return bookmark;
	}

	public void deleteBookmark(Bookmark bookmark) throws BookmarkException {
		bookmarks.removeBookmark(bookmark);
	}

	public void gotoBookmark(Bookmark bookmark) throws BookmarkException {
		if (isNull(bookmark)) {
			throw new NullPointerException("No bookmark provided");
		}

		Bookmark selectedBookmark = bookmarks.getBookmark(bookmark.getShortcut());

		if (isNull(selectedBookmark)) {
			throw new BookmarkKeyException("No bookmark with given bookmark key was not assigned");
		}

		if (nonNull(bookmark.getPage())) {
			selectBookmarkPage(bookmark.getPage());
		}
		else if (nonNull(selectedBookmark.getPage())) {
			selectBookmarkPage(selectedBookmark.getPage());
		}
		else {
			throw new NullPointerException("Selecting bookmark without assigned bookmark page");
		}
	}

	public void gotoPreviousBookmark() {
		if (nonNull(prevBookmarkPage)) {
			documentService.selectPage(prevBookmarkPage);
		}
	}

	private void selectBookmarkPage(Page page) {
		DocumentList documentList = documentService.getDocuments();
		Document selectedDoc = documentList.getSelectedDocument();
		Document document = page.getDocument();

		// Switch document if necessary.
		if (!selectedDoc.equals(document)) {
			documentService.selectDocument(document);
		}

		prevBookmarkPage = documentList.getSelectedDocument().getCurrentPage();

		documentService.selectPage(page);
	}

}

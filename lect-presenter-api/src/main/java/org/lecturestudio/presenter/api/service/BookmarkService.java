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

import java.util.Comparator;
import java.util.List;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.lecturestudio.core.app.ApplicationContext;
import org.lecturestudio.core.bus.event.BookmarkEvent;
import org.lecturestudio.core.model.Document;
import org.lecturestudio.core.model.DocumentList;
import org.lecturestudio.core.model.Page;
import org.lecturestudio.core.service.DocumentService;
import org.lecturestudio.presenter.api.model.Bookmark;
import org.lecturestudio.presenter.api.model.BookmarkException;
import org.lecturestudio.presenter.api.model.BookmarkKeyException;
import org.lecturestudio.presenter.api.model.BookmarkExistsException;
import org.lecturestudio.presenter.api.model.Bookmarks;

@Singleton
public class BookmarkService {

	private final DocumentService documentService;

	private final Bookmarks bookmarks;

	private Page prevBookmarkPage;

	private int defaultBookmarkCounter = 1;

	private final ApplicationContext context;

	@Inject
	public BookmarkService(DocumentService documentService, ApplicationContext context) {
		this.documentService = documentService;
		this.context = context;
		this.bookmarks = new Bookmarks();
	}


	public Bookmarks getBookmarks() {
		return bookmarks;
	}

	public void clearBookmarks() {
		Document selectedDoc = documentService.getDocuments().getSelectedDocument();
		context.getEventBus().post(new BookmarkEvent(documentService.getDocuments().getSelectedDocument().getCurrentPage(), BookmarkEvent.Type.REMOVED));
		bookmarks.clear(selectedDoc);
	}

	public void clearBookmarks(Document document) {
		context.getEventBus().post(new BookmarkEvent(documentService.getDocuments().getSelectedDocument().getCurrentPage(), BookmarkEvent.Type.REMOVED));
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
				if(bookmark.getPage().equals(page)){
					throw new BookmarkExistsException("Page is already bookmarked");
				}
			}
		}
		Bookmark bookmark = new Bookmark(keyStr, page);

		bookmarks.add(bookmark);
		context.getEventBus().post(new BookmarkEvent(page, BookmarkEvent.Type.CREATED));
		return bookmark;
	}

	public void deleteBookmark(Bookmark bookmark) throws BookmarkException {
		context.getEventBus().post(new BookmarkEvent(bookmark.getPage(), BookmarkEvent.Type.REMOVED));
		bookmarks.removeBookmark(bookmark);
	}

	public Bookmark getPageBookmark() throws BookmarkException {
		Document selectedDoc = documentService.getDocuments().getSelectedDocument();
		Page page = selectedDoc.getCurrentPage();
		if (isNull(page)) {
			throw new BookmarkException("No document selected");
		}

		List<Bookmark> docBookmarks = bookmarks.getDocumentBookmarks(selectedDoc);

		if (nonNull(docBookmarks)) {
			for (Bookmark bookmark : docBookmarks) {
				if(bookmark.getPage().equals(page)){
					return bookmark;
				}
			}
		}
		return null;
	}

	public boolean hasBookmark(Bookmark bookmark) {
		if (isNull(bookmark)) {
			throw new NullPointerException("No bookmark provided");
		}

		Bookmark selectedBookmark = bookmarks.getBookmark(bookmark.getShortcut());

		return nonNull(selectedBookmark);
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

	/**
	 * Creates a bookmark with a default shortcut
	 *
	 * @return the new created bookmark
	 */
	public Bookmark createDefaultBookmark()throws BookmarkException{

		Bookmark bookmark = createBookmark("L" + defaultBookmarkCounter);

		defaultBookmarkCounter++;
		return bookmark;
	}

	/**
	 * Calculates the next bookmark where the pagenumber is lower than current pagenumber.
	 *
	 * @return Page with bookmark with the next lower pagenumber
	 */
	public Page getPrevBookmarkPage(){
		Document currDoc = documentService.getDocuments().getSelectedDocument();
		List<Bookmark> allDocBookmarks = bookmarks.getDocumentBookmarks(currDoc);
		if(!nonNull(allDocBookmarks)){
			return null;
		}
		Bookmark currBookmark = getBookmarkCurrentPage(allDocBookmarks);

		if(!nonNull(currBookmark)){
			createDefaultBookmarkCurrentPage();
			currBookmark = getBookmarks().getBookmark("L0");
		}

		int maxBookmarks = allDocBookmarks.size();

		allDocBookmarks.sort(Comparator.comparingInt(a -> a.getPage().getPageNumber()));
		int bookmarkPos = allDocBookmarks.indexOf(currBookmark);

		if(maxBookmarks == 0 || bookmarkPos == 0){
			return null;
		}
		bookmarkPos--;
		Bookmark bookmark = allDocBookmarks.get(bookmarkPos);
		return bookmark.getPage();
	}

	/**
	 * Calculates the next bookmark where the pagenumber is higher than current pagenumber.
	 *
	 * @return Page with bookmark with the next higher pagenumber
	 */
	public Page getNextBookmarkPage(){
		Document currDoc = documentService.getDocuments().getSelectedDocument();
		List<Bookmark> allDocBookmarks = bookmarks.getDocumentBookmarks(currDoc);
		if(!nonNull(allDocBookmarks)){
			return null;
		}
		Bookmark currBookmark = getBookmarkCurrentPage(allDocBookmarks);

		if(!nonNull(currBookmark)){
			createDefaultBookmarkCurrentPage();
			currBookmark = getBookmarks().getBookmark("L0");
		}

		int maxBookmarks = allDocBookmarks.size();

		allDocBookmarks.sort(Comparator.comparingInt(a -> a.getPage().getPageNumber()));
		int bookmarkPos = allDocBookmarks.indexOf(currBookmark);
		if(bookmarkPos + 1 == maxBookmarks){
			return null;
		}
		bookmarkPos++;
		Bookmark bookmark = allDocBookmarks.get(bookmarkPos);
		return bookmark.getPage();
	}

	private Bookmark getBookmarkCurrentPage(List<Bookmark> allDocBookmarks){
		Page currPage = documentService.getDocuments().getSelectedDocument().getCurrentPage();
		for(Bookmark bm : allDocBookmarks){
			if(bm.getPage().equals(currPage)){
				return bm;
			}
		}
		return null;
	}

	private Bookmark createDefaultBookmarkCurrentPage(){
		try {
			if (nonNull(getBookmarks().getBookmark("L0"))) {
				deleteBookmark(getBookmarks().getBookmark("L0"));
			}
			return createBookmark("L0");
		} catch (BookmarkException e) {
			throw new RuntimeException(e);
		}
	}

}

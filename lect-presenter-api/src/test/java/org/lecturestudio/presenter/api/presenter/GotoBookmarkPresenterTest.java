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

package org.lecturestudio.presenter.api.presenter;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

import org.lecturestudio.core.model.Document;
import org.lecturestudio.core.service.DocumentService;
import org.lecturestudio.core.view.Action;
import org.lecturestudio.core.view.ConsumerAction;
import org.lecturestudio.core.view.NotificationType;
import org.lecturestudio.presenter.api.model.Bookmark;
import org.lecturestudio.presenter.api.model.BookmarkException;
import org.lecturestudio.presenter.api.service.BookmarkService;
import org.lecturestudio.presenter.api.view.GotoBookmarkView;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class GotoBookmarkPresenterTest extends PresenterTest {

	private BookmarkService bookmarkService;

	private DocumentService documentService;

	@BeforeEach
	void setup() throws IOException {
		Document document = new Document();
		document.createPage();
		document.createPage();
		document.createPage();

		documentService = new DocumentService(context);
		documentService.addDocument(document);
		documentService.selectDocument(document);

		bookmarkService = new BookmarkService(documentService, context);
	}

	@Test
	void testEmptyBookmarkList() {
		GotoBookmarkMockView view = new GotoBookmarkMockView() {
			@Override
			public void setBookmarks(List<Bookmark> bookmarkList) {
				assertNotNull(bookmarkList);
				assertTrue(bookmarkList.isEmpty());
			}
		};

		GotoBookmarkPresenter presenter = new GotoBookmarkPresenter(context, view, documentService, bookmarkService);
		presenter.initialize();
	}

	@Test
	void testBookmarkList() throws BookmarkException {
		Bookmark a = bookmarkService.createBookmark("a");
		int currPage = documentService.getDocuments().getSelectedDocument().getCurrentPage().getPageNumber();
		int maxPages = documentService.getDocuments().getSelectedDocument().getPageCount();
		documentService.getDocuments().getSelectedDocument().selectPage((currPage + 1) % maxPages);
		Bookmark z = bookmarkService.createBookmark("z");

		GotoBookmarkMockView view = new GotoBookmarkMockView() {
			@Override
			public void setBookmarks(List<Bookmark> bookmarkList) {
				assertNotNull(bookmarkList);
				assertFalse(bookmarkList.isEmpty());
				assertEquals(2, bookmarkList.size());
				assertEquals(a, bookmarkList.get(0));
				assertEquals(z, bookmarkList.get(1));
			}
		};

		GotoBookmarkPresenter presenter = new GotoBookmarkPresenter(context, view, documentService, bookmarkService);
		presenter.initialize();
	}

	@Test
	void testGotoBookmark() throws BookmarkException {
		Bookmark a = bookmarkService.createBookmark("a");

		AtomicBoolean close = new AtomicBoolean(false);

		GotoBookmarkMockView view = new GotoBookmarkMockView();

		GotoBookmarkPresenter presenter = new GotoBookmarkPresenter(context, view, documentService, bookmarkService);
		presenter.initialize();
		presenter.setOnClose(() -> {
			close.set(true);
		});

		view.gotoAction.execute(a);

		assertTrue(close.get());
	}

	@Test
	void testGotoInvalidBookmark() throws BookmarkException {
		bookmarkService.createBookmark("a");

		AtomicBoolean close = new AtomicBoolean(false);

		GotoBookmarkMockView view = new GotoBookmarkMockView();

		GotoBookmarkPresenter presenter = new GotoBookmarkPresenter(context, view, documentService, bookmarkService);
		presenter.initialize();
		presenter.setOnClose(() -> {
			close.set(true);
		});

		view.gotoAction.execute(new Bookmark("z"));

		NotificationMockView notifyView = notifyViewRef.get();

		assertFalse(close.get());
		assertNotNull(notifyView);
		assertEquals(NotificationType.ERROR, notifyView.type);
		assertEquals("bookmark.goto.error", notifyView.title);
		assertEquals("bookmark.key.not.existing", notifyView.message);
	}

	@Test
	void testDeleteBookmark() throws BookmarkException {
		Bookmark a = bookmarkService.createBookmark("a");
		int currPage = documentService.getDocuments().getSelectedDocument().getCurrentPage().getPageNumber();
		int maxPages = documentService.getDocuments().getSelectedDocument().getPageCount();
		documentService.getDocuments().getSelectedDocument().selectPage((currPage + 1) % maxPages);
		Bookmark z = bookmarkService.createBookmark("z");

		GotoBookmarkMockView view = new GotoBookmarkMockView() {
			@Override
			public void removeBookmark(Bookmark bookmark) {
				if (bookmarkService.getBookmarks().size() == 1) {
					assertEquals(a, bookmark);
				}
				else {
					assertEquals(z, bookmark);
				}
			}
		};

		GotoBookmarkPresenter presenter = new GotoBookmarkPresenter(context, view, documentService, bookmarkService);
		presenter.initialize();

		view.deleteAction.execute(a);
		assertEquals(1, bookmarkService.getBookmarks().size());

		view.deleteAction.execute(z);
		assertEquals(0, bookmarkService.getBookmarks().size());
	}

	@Test
	void testGoToNextBookmark() throws BookmarkException {
		Bookmark a = bookmarkService.createBookmark("a");
		int currPage = documentService.getDocuments().getSelectedDocument().getCurrentPage().getPageNumber();
		int maxPages = documentService.getDocuments().getSelectedDocument().getPageCount();
		documentService.getDocuments().getSelectedDocument().selectPage((currPage + 1) % maxPages);
		Bookmark z = bookmarkService.createBookmark("z");
		currPage = documentService.getDocuments().getSelectedDocument().getCurrentPage().getPageNumber();
		maxPages = documentService.getDocuments().getSelectedDocument().getPageCount();
		documentService.getDocuments().getSelectedDocument().selectPage((currPage + 1) % maxPages);
		Bookmark y = bookmarkService.createBookmark("y");

		documentService.getDocuments().getSelectedDocument().selectPage(0);
		assertEquals(bookmarkService.getPageBookmark(), a);

		currPage = documentService.getDocuments().getSelectedDocument().getCurrentPage().getPageNumber();
		maxPages = documentService.getDocuments().getSelectedDocument().getPageCount();
		documentService.getDocuments().getSelectedDocument().selectPage((currPage + 1) % maxPages);
		assertEquals(bookmarkService.getPageBookmark(), z);

		currPage = documentService.getDocuments().getSelectedDocument().getCurrentPage().getPageNumber();
		maxPages = documentService.getDocuments().getSelectedDocument().getPageCount();
		documentService.getDocuments().getSelectedDocument().selectPage((currPage + 1) % maxPages);
		assertEquals(bookmarkService.getPageBookmark(), y);
	}



	private static class GotoBookmarkMockView implements GotoBookmarkView {

		ConsumerAction<Bookmark> gotoAction;

		ConsumerAction<Bookmark> deleteAction;


		@Override
		public void setDocument(Document document) {

		}

		@Override
		public void setBookmarks(List<Bookmark> bookmarkList) {

		}

		@Override
		public void removeBookmark(Bookmark bookmark) {

		}

		@Override
		public void setOnClose(Action action) {

		}

		@Override
		public void setOnGotoPageNumber(ConsumerAction<Integer> action) {

		}

		@Override
		public void setOnDeleteBookmark(ConsumerAction<Bookmark> action) {
			assertNotNull(action);

			deleteAction = action;
		}

		@Override
		public void setOnGotoBookmark(ConsumerAction<Bookmark> action) {
			assertNotNull(action);

			gotoAction = action;
		}
	}
}
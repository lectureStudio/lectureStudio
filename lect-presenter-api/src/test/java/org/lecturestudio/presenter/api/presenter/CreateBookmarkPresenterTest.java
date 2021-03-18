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
import org.lecturestudio.presenter.api.view.CreateBookmarkView;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class CreateBookmarkPresenterTest extends PresenterTest {

	private BookmarkService bookmarkService;


	@BeforeEach
	void setup() throws IOException {
		Document document = new Document();
		document.createPage();

		DocumentService documentService = new DocumentService(context);
		documentService.addDocument(document);
		documentService.selectDocument(document);

		bookmarkService = new BookmarkService(documentService);
	}

	@Test
	void testEmptyBookmarkList() {
		CreateBookmarkMockView view = new CreateBookmarkMockView() {
			@Override
			public void setBookmarks(List<Bookmark> bookmarkList) {
				assertNotNull(bookmarkList);
				assertTrue(bookmarkList.isEmpty());
			}
		};

		CreateBookmarkPresenter presenter = new CreateBookmarkPresenter(context, view, bookmarkService);
		presenter.initialize();
	}

	@Test
	void testBookmarkList() throws BookmarkException {
		Bookmark a = bookmarkService.createBookmark("a");
		Bookmark z = bookmarkService.createBookmark("z");

		CreateBookmarkMockView view = new CreateBookmarkMockView() {
			@Override
			public void setBookmarks(List<Bookmark> bookmarkList) {
				assertNotNull(bookmarkList);
				assertFalse(bookmarkList.isEmpty());
				assertEquals(2, bookmarkList.size());
				assertEquals(a, bookmarkList.get(0));
				assertEquals(z, bookmarkList.get(1));
			}
		};

		CreateBookmarkPresenter presenter = new CreateBookmarkPresenter(context, view, bookmarkService);
		presenter.initialize();
	}

	@Test
	void testCreateBookmark() {
		AtomicBoolean close = new AtomicBoolean(false);

		CreateBookmarkMockView view = new CreateBookmarkMockView();

		CreateBookmarkPresenter presenter = new CreateBookmarkPresenter(context, view, bookmarkService);
		presenter.initialize();
		presenter.setOnClose(() -> {
			close.set(true);
		});

		view.createAction.execute("s");

		assertEquals(1, bookmarkService.getBookmarks().size());
		assertTrue(close.get());
	}

	@Test
	void testCreateInvalidBookmark() throws BookmarkException {
		bookmarkService.createBookmark("a");

		AtomicBoolean close = new AtomicBoolean(false);

		CreateBookmarkMockView view = new CreateBookmarkMockView();

		CreateBookmarkPresenter presenter = new CreateBookmarkPresenter(context, view, bookmarkService);
		presenter.initialize();
		presenter.setOnClose(() -> {
			close.set(true);
		});

		view.createAction.execute("a");

		NotificationMockView notifyView = notifyViewRef.get();

		assertFalse(close.get());
		assertNotNull(notifyView);
		assertEquals(NotificationType.ERROR, notifyView.type);
		assertEquals("bookmark.assign.error", notifyView.title);
		assertEquals("bookmark.key.exists", notifyView.message);
	}

	@Test
	void testDeleteBookmark() throws BookmarkException {
		Bookmark a = bookmarkService.createBookmark("a");
		Bookmark z = bookmarkService.createBookmark("z");

		CreateBookmarkMockView view = new CreateBookmarkMockView() {
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

		CreateBookmarkPresenter presenter = new CreateBookmarkPresenter(context, view, bookmarkService);
		presenter.initialize();

		view.deleteAction.execute(a);
		assertEquals(1, bookmarkService.getBookmarks().size());

		view.deleteAction.execute(z);
		assertEquals(0, bookmarkService.getBookmarks().size());
	}



	private static class CreateBookmarkMockView implements CreateBookmarkView {

		ConsumerAction<String> createAction;

		ConsumerAction<Bookmark> deleteAction;


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
		public void setOnCreateBookmark(ConsumerAction<String> action) {
			assertNotNull(action);

			createAction = action;
		}

		@Override
		public void setOnDeleteBookmark(ConsumerAction<Bookmark> action) {
			assertNotNull(action);

			deleteAction = action;
		}
	}
}
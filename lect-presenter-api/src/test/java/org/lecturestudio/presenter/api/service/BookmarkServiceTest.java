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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.lecturestudio.core.model.Document;
import org.lecturestudio.presenter.api.model.Bookmark;
import org.lecturestudio.presenter.api.model.BookmarkException;
import org.lecturestudio.presenter.api.model.BookmarkKeyException;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class BookmarkServiceTest extends ServiceTest {

	private BookmarkService bookmarkService;


	@BeforeEach
	void setUp() {
		bookmarkService = new BookmarkService(documentService);
	}

	@Test
	void testClearBookmarks() throws BookmarkException {
		bookmarkService.createBookmark("a");
		bookmarkService.createBookmark("b");

		documentService.selectDocument(document2);

		bookmarkService.createBookmark("c");
		bookmarkService.createBookmark("d");
		bookmarkService.clearBookmarks();

		assertEquals(2, bookmarkService.getBookmarks().getAllBookmarks().size());
		assertEquals(2, bookmarkService.getBookmarks().getDocumentBookmarks(document1).size());
		assertNull(bookmarkService.getBookmarks().getDocumentBookmarks(document2));

		documentService.selectDocument(document1);
		bookmarkService.clearBookmarks();

		assertEquals(0, bookmarkService.getBookmarks().getAllBookmarks().size());
		assertNull(bookmarkService.getBookmarks().getDocumentBookmarks(document1));
	}

	@Test
	void testClearDocumentBookmarks() throws BookmarkException {
		bookmarkService.createBookmark("a");
		bookmarkService.createBookmark("b");
		bookmarkService.clearBookmarks(document1);

		documentService.selectDocument(document2);

		bookmarkService.createBookmark("a");
		bookmarkService.createBookmark("b");

		assertEquals(2, bookmarkService.getBookmarks().getAllBookmarks().size());
		assertNull(bookmarkService.getBookmarks().getDocumentBookmarks(document1));

		bookmarkService.clearBookmarks(document2);

		assertEquals(0, bookmarkService.getBookmarks().getAllBookmarks().size());
		assertNull(bookmarkService.getBookmarks().getDocumentBookmarks(document2));
	}

	@Test
	void testCreateBookmark() throws BookmarkException {
		Bookmark bookmarkA = bookmarkService.createBookmark("a");
		Bookmark bookmarkB = bookmarkService.createBookmark("b");

		assertNotNull(bookmarkA);
		assertNotNull(bookmarkB);
		assertEquals(2, bookmarkService.getBookmarks().getAllBookmarks().size());

		assertThrows(BookmarkKeyException.class, () -> {
			bookmarkService.createBookmark("a");
		});

		documentService.selectDocument(document2);

		bookmarkA = bookmarkService.createBookmark("a");
		bookmarkB = bookmarkService.createBookmark("b");

		assertNotNull(bookmarkA);
		assertNotNull(bookmarkB);
		assertEquals(4, bookmarkService.getBookmarks().getAllBookmarks().size());
	}

	@Test
	void testDeleteBookmark() throws BookmarkException {
		Bookmark bookmarkA = bookmarkService.createBookmark("a");
		Bookmark bookmarkB = bookmarkService.createBookmark("b");

		bookmarkService.deleteBookmark(bookmarkB);

		assertEquals(1, bookmarkService.getBookmarks().getAllBookmarks().size());

		bookmarkService.deleteBookmark(bookmarkA);

		assertEquals(0, bookmarkService.getBookmarks().getAllBookmarks().size());
	}

	@Test
	void testGotoBookmark() throws BookmarkException {
		Document document = documentService.getDocuments().getSelectedDocument();

		Bookmark bookmarkA = bookmarkService.createBookmark("1");
		documentService.selectNextPage();

		assertNotEquals(document.getCurrentPage(), bookmarkA.getPage());

		Bookmark bookmarkB = bookmarkService.createBookmark("2");
		documentService.selectNextPage();

		assertNotEquals(document.getCurrentPage(), bookmarkA.getPage());

		documentService.selectDocument(document2);

		Bookmark bookmarkC = bookmarkService.createBookmark("c");

		documentService.selectDocument(document1);

		bookmarkService.gotoBookmark(bookmarkA);
		assertEquals(document.getCurrentPage(), bookmarkA.getPage());

		bookmarkService.gotoBookmark(bookmarkB);
		assertEquals(document.getCurrentPage(), bookmarkB.getPage());

		bookmarkService.gotoBookmark(bookmarkC);
		assertEquals(document2, documentService.getDocuments().getSelectedDocument());
		assertEquals(document2.getCurrentPage(), bookmarkC.getPage());

		assertThrows(NullPointerException.class, () -> {
			bookmarkService.gotoBookmark(null);
		});
		assertThrows(BookmarkKeyException.class, () -> {
			bookmarkService.gotoBookmark(new Bookmark("x"));
		});
	}

	@Test
	void testGotoPreviousBookmark() throws BookmarkException {
		Document document = documentService.getDocuments().getSelectedDocument();

		Bookmark bookmarkA = bookmarkService.createBookmark("1");
		documentService.selectNextPage();

		Bookmark bookmarkB = bookmarkService.createBookmark("2");
		documentService.selectNextPage();

		bookmarkService.gotoBookmark(bookmarkA);
		bookmarkService.gotoBookmark(bookmarkB);

		bookmarkService.gotoPreviousBookmark();

		assertEquals(document.getCurrentPage(), bookmarkA.getPage());
	}

}
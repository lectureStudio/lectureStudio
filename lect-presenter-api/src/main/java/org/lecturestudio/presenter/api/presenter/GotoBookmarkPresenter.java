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

import javax.inject.Inject;

import org.lecturestudio.core.app.ApplicationContext;
import org.lecturestudio.core.presenter.Presenter;
import org.lecturestudio.presenter.api.model.Bookmark;
import org.lecturestudio.presenter.api.model.BookmarkKeyException;
import org.lecturestudio.presenter.api.model.Bookmarks;
import org.lecturestudio.presenter.api.service.BookmarkService;
import org.lecturestudio.presenter.api.view.GotoBookmarkView;

public class GotoBookmarkPresenter extends Presenter<GotoBookmarkView> {

	private final BookmarkService bookmarkService;


	@Inject
	GotoBookmarkPresenter(ApplicationContext context, GotoBookmarkView view, BookmarkService bookmarkService) {
		super(context, view);

		this.bookmarkService = bookmarkService;
	}

	@Override
	public void initialize() {
		Bookmarks bookmarks = bookmarkService.getBookmarks();

		view.setOnClose(this::close);
		view.setOnGotoBookmark(this::gotoBookmark);
		view.setOnDeleteBookmark(this::deleteBookmark);
		view.setBookmarks(bookmarks.getAllBookmarks());
	}

	private void deleteBookmark(Bookmark bookmark) {
		try {
			bookmarkService.deleteBookmark(bookmark);
			view.removeBookmark(bookmark);
		}
		catch (Exception e) {
			handleException(e, "Delete bookmark failed", "bookmark.delete.error");
		}
	}

	private void gotoBookmark(Bookmark bookmark) {
		try {
			bookmarkService.gotoBookmark(bookmark);
			close();
		}
		catch (BookmarkKeyException e) {
			context.showError("bookmark.goto.error", "bookmark.key.not.existing", bookmark.getShortcut());
		}
		catch (Exception e) {
			handleException(e, "Go to bookmark failed", "bookmark.goto.error");
		}
	}
}

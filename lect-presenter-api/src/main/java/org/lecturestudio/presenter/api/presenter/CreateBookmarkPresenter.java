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

import java.text.MessageFormat;

import javax.inject.Inject;

import org.lecturestudio.core.app.ApplicationContext;
import org.lecturestudio.core.presenter.Presenter;
import org.lecturestudio.presenter.api.model.Bookmark;
import org.lecturestudio.presenter.api.model.BookmarkException;
import org.lecturestudio.presenter.api.model.BookmarkKeyException;
import org.lecturestudio.presenter.api.model.BookmarkExistsException;
import org.lecturestudio.presenter.api.model.Bookmarks;
import org.lecturestudio.presenter.api.service.BookmarkService;
import org.lecturestudio.presenter.api.view.CreateBookmarkView;

public class CreateBookmarkPresenter extends Presenter<CreateBookmarkView> {

	private final BookmarkService bookmarkService;


	@Inject
	CreateBookmarkPresenter(ApplicationContext context, CreateBookmarkView view, BookmarkService bookmarkService) {
		super(context, view);

		this.bookmarkService = bookmarkService;
	}

	@Override
	public void initialize() {
		Bookmarks bookmarks = bookmarkService.getBookmarks();

		view.setOnClose(this::close);
		view.setOnCreateBookmark(this::createBookmark);
		view.setOnDeleteBookmark(this::deleteBookmark);
		view.setBookmarks(bookmarks.getAllBookmarks());
	}

	private void createBookmark(String keyStr) {
		try {
			bookmarkCreated(bookmarkService.createBookmark(keyStr));
		}
		catch (BookmarkKeyException e) {
			context.showError("bookmark.assign.warning", "bookmark.key.exists", keyStr);
		}
		catch (BookmarkExistsException e){
			context.showError("bookmark.assign.warning", "bookmark.exists");
		}
		catch (BookmarkException e) {
			handleException(e, "Create bookmark failed", "bookmark.assign.warning");
		}
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

	private void bookmarkCreated(Bookmark bookmark) {
		String shortcut = bookmark.getShortcut().toUpperCase();
		String message = MessageFormat.format(context.getDictionary().get("bookmark.created"), shortcut);

		context.showNotificationPopup(message);
		close();
	}
}

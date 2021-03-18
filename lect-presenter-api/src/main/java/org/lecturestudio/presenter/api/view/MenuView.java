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

package org.lecturestudio.presenter.api.view;

import java.io.File;
import java.net.URI;
import java.util.List;

import org.lecturestudio.core.ExecutableState;
import org.lecturestudio.core.beans.BooleanProperty;
import org.lecturestudio.core.model.Document;
import org.lecturestudio.core.model.Page;
import org.lecturestudio.core.model.RecentDocument;
import org.lecturestudio.core.model.Time;
import org.lecturestudio.core.view.Action;
import org.lecturestudio.core.view.ConsumerAction;
import org.lecturestudio.core.view.PresentationParameter;
import org.lecturestudio.core.view.View;
import org.lecturestudio.presenter.api.model.Bookmark;
import org.lecturestudio.presenter.api.model.Bookmarks;
import org.lecturestudio.presenter.api.service.MessageWebServiceState;
import org.lecturestudio.presenter.api.service.QuizWebServiceState;
import org.lecturestudio.web.api.model.quiz.Quiz;

public interface MenuView extends View {

	void setDocument(Document doc);

	void setPage(Page page, PresentationParameter parameter);

	/**
	 * File Menu
	 */

	void setRecentDocuments(List<RecentDocument> recentDocs);

	void setOnOpenDocument(Action action);

	void setOnOpenDocument(ConsumerAction<File> action);

	void setOnCloseDocument(Action action);

	void setOnSaveDocuments(Action action);

	void setOnSaveQuizResults(Action action);

	void setOnExit(Action action);

	/**
	 * Edit Menu
	 */

	void setOnUndo(Action action);

	void setOnRedo(Action action);

	void setOnSettings(Action action);

	/**
	 * View Menu
	 */

	void bindShowOutline(BooleanProperty showProperty);

	void setAdvancedSettings(boolean selected);

	void setOnFullscreen(ConsumerAction<Boolean> action);

	void setOnAdvancedSettings(ConsumerAction<Boolean> action);

	/**
	 * Whiteboard Menu
	 */

	void setOnNewWhiteboard(Action action);

	void setOnNewWhiteboardPage(Action action);

	void setOnDeleteWhiteboardPage(Action action);

	void setOnShowGrid(ConsumerAction<Boolean> action);

	/**
	 * Presentation Menu
	 */

	void setOnStartRecording(Action action);

	void setOnStopRecording(Action action);

	void setOnStartStreaming(Action action);

	void setOnStopStreaming(Action action);

	void setOnStartMessenger(Action action);

	void setOnStopMessenger(Action action);

	void setOnShowMessengerWindow(ConsumerAction<Boolean> action);

	void setOnShowSelectQuizView(Action action);

	void setOnShowNewQuizView(Action action);

	void setOnCloseQuiz(Action action);

	void setMessengerWindowVisible(boolean visible);

	void setMessengerState(ExecutableState state);

	void setQuizState(ExecutableState state);

	void setRecordingState(ExecutableState state);

	void setStreamingState(ExecutableState state);

	/**
	 * Services Menu
	 */

	void setOnControlRecording(ConsumerAction<Boolean> action);

	void setOnControlRecordingSettings(Action action);

	void setOnControlStreaming(ConsumerAction<Boolean> action);

	void setOnControlStreamingSettings(Action action);

	void setOnControlMessenger(ConsumerAction<Boolean> action);

	void setOnControlMessengerWindow(ConsumerAction<Boolean> action);

	void setOnControlMessengerSettings(Action action);

	void setOnControlCamera(ConsumerAction<Boolean> action);

	void setOnControlCameraSettings(Action action);

	/**
	 * Bookmarks Menu
	 */

	void setBookmarks(Bookmarks bookmarks);

	void setOnClearBookmarks(Action action);

	void setOnShowNewBookmarkView(Action action);

	void setOnShowGotoBookmarkView(Action action);

	void setOnPreviousBookmark(Action action);

	void setOnOpenBookmark(ConsumerAction<Bookmark> action);

	/**
	 * Embedded Actions
	 */

	void setPageURIs(List<URI> uris);

	void setPageFileLinks(List<File> fileLinks);

	void setOnOpenPageURI(ConsumerAction<URI> action);

	void setOnOpenPageFileLink(ConsumerAction<File> action);

	/**
	 * Embedded Quizzes
	 */

	void setPageQuizzes(List<Quiz> quizzes);

	void setOnOpenPageQuiz(ConsumerAction<Quiz> action);

	/**
	 * Info Menu
	 */

	void setOnOpenLog(Action action);

	void setOnOpenAbout(Action action);

	/**
	 * Time Menu
	 */

	void setCurrentTime(String time);

	/**
	 * Recording time Menu
	 */

	void setRecordingTime(Time time);

	/**
	 * Indicators
	 */

	void setMessageServiceState(MessageWebServiceState state);

	void setQuizServiceState(QuizWebServiceState state);
}

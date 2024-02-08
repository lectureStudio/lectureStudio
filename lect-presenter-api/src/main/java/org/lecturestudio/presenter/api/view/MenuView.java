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

import java.awt.*;
import java.io.File;
import java.util.List;

import org.lecturestudio.core.ExecutableState;
import org.lecturestudio.core.beans.BooleanProperty;
import org.lecturestudio.core.beans.IntegerProperty;
import org.lecturestudio.core.beans.ObjectProperty;
import org.lecturestudio.core.model.*;
import org.lecturestudio.core.view.Action;
import org.lecturestudio.core.view.ConsumerAction;
import org.lecturestudio.core.view.PresentationParameter;
import org.lecturestudio.core.view.View;
import org.lecturestudio.presenter.api.context.PresenterContext.ParticipantCount;
import org.lecturestudio.presenter.api.model.*;
import org.lecturestudio.presenter.api.service.QuizWebServiceState;

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

	void bindFullscreen(BooleanProperty fullscreen);

	void setOnCustomizeToolbar(Action action);

	void setSpeechPosition(SpeechPosition position);

	void setOnSpeechPosition(ConsumerAction<SpeechPosition> action);

	void setMessagesPosition(MessageBarPosition position);

	void setOnMessagesPosition(ConsumerAction<MessageBarPosition> action);

	void setSlideNotesPosition(SlideNotesPosition position);

	void setOnSlideNotesPosition(ConsumerAction<SlideNotesPosition> action);

	void setNoteSlidePosition(NoteSlidePosition position);

	void setOnNoteSlidePosition(ConsumerAction<NoteSlidePosition> action);

	void setParticipantsPosition(ParticipantsPosition position);

	void setOnParticipantsPosition(ConsumerAction<ParticipantsPosition> action);

	void setSlidePreviewPosition(SlidePreviewPosition position);

	void setOnSlidePreviewPosition(ConsumerAction<SlidePreviewPosition> action);

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

	void bindEnableStream(BooleanProperty enable);

	void bindViewStream(BooleanProperty enable);

	void bindEnableStreamingMicrophone(BooleanProperty enable);

	void bindEnableStreamingCamera(BooleanProperty enable);

	void bindEnableMessenger(BooleanProperty enable);

	void setOnShowMessengerWindow(ConsumerAction<Boolean> action);

	void setOnShowSelectQuizView(Action action);

	void setOnShowNewQuizView(Action action);

	void setOnCloseQuiz(Action action);

	void setMessengerWindowVisible(boolean visible);

	void setMessengerState(ExecutableState state);

	void setQuizState(ExecutableState state);

	void setRecordingState(ExecutableState state);

	void setStreamingState(ExecutableState state);

	void setStreamReconnectState(ExecutableState state);

	/**
	 * Stopwatch Menu
	 */

	void setOnResetStopwatch(Action action);

	void setOnPauseStopwatch(Action action);

	void setOnConfigStopwatch(Action action);

	void setCurrentStopwatchBackgroundColor(Color color);

	/**
	 * Bookmarks Menu
	 */

	void setBookmarks(Bookmarks bookmarks);

	void setOnClearBookmarks(Action action);

	void setOnShowNewBookmarkView(Action action);

	void setOnCreateNewDefaultBookmarkView(Action action);

	void setOnRemoveBookmarkView(Action action);

	void setOnShowGotoBookmarkView(Action action);

	void setOnPreviousBookmark(Action action);

	void setOnPrevBookmark(Action action);

	void setOnNextBookmark(Action action);

	void setOnOpenBookmark(ConsumerAction<Bookmark> action);

	/**
	 * Info Menu
	 */

	void setOnOpenLog(Action action);

	void setOnOpenAbout(Action action);

	/**
	 * Time Menu
	 */

	void setCurrentTime(String time);

	void setCurrentStopwatch(String time);

	void setCurrentStopwatch(Action action);

	/**
	 * Recording time Menu
	 */

	void setRecordingTime(Time time);

	/**
	 * Indicators
	 */

	void bindMessageCount(IntegerProperty count);

	void bindSpeechRequestCount(IntegerProperty count);

	void bindCourseParticipantsCount(ObjectProperty<ParticipantCount> count);

	void setQuizServiceState(QuizWebServiceState state);

	/**
	 * Split notes
	 */

	void setSplitNotesPosition(NotesPosition position);

	void setOnSplitNotesPosition(ConsumerAction<NotesPosition> action);

}

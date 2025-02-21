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
import java.util.Collection;
import java.util.List;
import java.util.UUID;

import org.lecturestudio.core.ExecutableState;
import org.lecturestudio.core.beans.BooleanProperty;
import org.lecturestudio.core.controller.RenderController;
import org.lecturestudio.core.controller.ToolController;
import org.lecturestudio.core.geometry.Matrix;
import org.lecturestudio.core.input.KeyEvent;
import org.lecturestudio.core.input.ScrollHandler;
import org.lecturestudio.core.model.Document;
import org.lecturestudio.core.model.DocumentOutline;
import org.lecturestudio.core.model.DocumentOutlineItem;
import org.lecturestudio.core.model.Page;
import org.lecturestudio.core.view.*;
import org.lecturestudio.presenter.api.model.*;
import org.lecturestudio.presenter.api.config.SlideViewConfiguration;
import org.lecturestudio.swing.model.ExternalWindowPosition;
import org.lecturestudio.core.stylus.StylusHandler;
import org.lecturestudio.web.api.event.LocalVideoFrameEvent;
import org.lecturestudio.web.api.event.PeerStateEvent;
import org.lecturestudio.web.api.event.RemoteVideoFrameEvent;
import org.lecturestudio.web.api.message.MessengerMessage;
import org.lecturestudio.web.api.message.SpeechBaseMessage;
import org.lecturestudio.web.api.stream.model.CourseParticipant;

public interface SlidesView extends View {

	void setSlideViewConfig(SlideViewConfiguration viewState);

	void addPageObjectView(PageObjectView<?> objectView);

	void removePageObjectView(PageObjectView<?> objectView);

	void removeAllPageObjectViews();

	List<PageObjectView<?>> getPageObjectViews();

	void addDocument(Document doc, PresentationParameterProvider ppProvider);

	void removeDocument(Document doc);

	void selectDocument(Document doc, PresentationParameterProvider ppProvider);

	void setParticipants(Collection<CourseParticipant> participants);

	void addParticipant(CourseParticipant participant);

	void removeParticipant(CourseParticipant participant);

	Page getPage();

	void setPage(Page page, PresentationParameter parameter);

	void setSlideNotes(Page page, PresentationParameter parameter);

	void setPageRenderer(RenderController pageRenderer);

	void setOutline(DocumentOutline outline);

	void bindShowOutline(BooleanProperty showProperty);

	void setExtendedFullscreen(boolean extended);

	void setScrollHandler(ScrollHandler handler);

	void createStylusInput(StylusHandler handler);

	void createMouseInput(ToolController toolController);

	void setLaTeXText(String text);

	void setStreamState(ExecutableState state);

	void setScreenShareState(ExecutableState state, Document document);

	void setQuizState(ExecutableState state);

	void setMessengerState(ExecutableState state);

	void setNotesText(String notesText);

	void clearNotesViewContainer();

	void setMessengerMessage(MessengerMessage message);

	void removeMessengerMessage(String messageId);

	void setMessengerMessageAsReply(MessengerMessage message, MessengerMessage messageToReplyTo);

	void setModifiedMessengerMessage(MessengerMessage modifiedMessage);

	void addSpeechRequest(SpeechBaseMessage message);

	void removeSpeechRequest(SpeechBaseMessage message);

	void setOnDiscardMessage(ConsumerAction<MessengerMessage> action);

	void setOnCreateMessageSlide(ConsumerAction<MessengerMessage> action);

	void setOnAcceptSpeech(ConsumerAction<SpeechBaseMessage> action);

	void setOnRejectSpeech(ConsumerAction<SpeechBaseMessage> action);

	void setOnBan(ConsumerAction<CourseParticipant> action);

	void setPeerStateEvent(PeerStateEvent event);

	void setOnMutePeerAudio(ConsumerAction<Boolean> action);

	void setOnMutePeerVideo(ConsumerAction<Boolean> action);

	void setOnStopPeerConnection(ConsumerAction<UUID> action);

	void setVideoFrameEvent(LocalVideoFrameEvent event);

	void setVideoFrameEvent(RemoteVideoFrameEvent event);

	void setOnKeyEvent(ConsumerAction<KeyEvent> action);

	void setOnSelectDocument(ConsumerAction<Document> action);

	void setOnSelectPage(ConsumerAction<Page> action);

	void setOnViewTransform(ConsumerAction<Matrix> action);

	void setOnNewPage(Action action);

	void setOnDeletePage(Action action);

	void setOnStopQuiz(Action action);

	void setOnToggleScreenShare(BooleanProperty property);

	void setOnStopScreenShare(Action action);

	void setOnSendMessage(ConsumerAction<String> action);

	void setOnOutlineItem(ConsumerAction<DocumentOutlineItem> action);

	void setOnExternalMessagesPositionChanged(ConsumerAction<ExternalWindowPosition> action);

	void setOnExternalMessagesSizeChanged(ConsumerAction<Dimension> action);

	void setOnExternalMessagesClosed(Action action);

	void setOnExternalParticipantsPositionChanged(ConsumerAction<ExternalWindowPosition> action);

	void setOnExternalParticipantsSizeChanged(ConsumerAction<Dimension> action);

	void setOnExternalParticipantsClosed(Action action);

	void setOnExternalParticipantVideoPositionChanged(ConsumerAction<ExternalWindowPosition> action);

	void setOnExternalParticipantVideoSizeChanged(ConsumerAction<Dimension> action);

	void setOnExternalParticipantVideoClosed(Action action);

	void setOnExternalSlidePreviewPositionChanged(ConsumerAction<ExternalWindowPosition> action);

	void setOnExternalSlidePreviewSizeChanged(ConsumerAction<Dimension> action);

	void setOnExternalSlidePreviewClosed(Action action);

	void setOnExternalNotesPositionChanged(ConsumerAction<ExternalWindowPosition> action);

	void setOnExternalNotesSizeChanged(ConsumerAction<Dimension> action);

	void setOnExternalNotesClosed(Action action);

	void setOnExternalSlideNotesPositionChanged(ConsumerAction<ExternalWindowPosition> action);

	void setOnExternalSlideNotesSizeChanged(ConsumerAction<Dimension> action);

	void setOnExternalSlideNotesClosed(Action action);

	void showExternalMessages(Screen screen, Point position, Dimension size);

	void hideExternalMessages();

	void showExternalParticipants(Screen screen, Point position, Dimension size);

	void hideExternalParticipants();

	void showExternalParticipantVideo(Screen screen, Point position, Dimension size);

	void hideExternalParticipantVideo();

	void showExternalSlidePreview(Screen screen, Point position, Dimension size);

	void hideExternalSlidePreview();

	void showExternalNotes(Screen screen, Point position, Dimension size);

	void hideExternalNotes();

	void showExternalSlideNotes(Screen screen, Point position, Dimension size);

	void hideExternalSlideNotes();

	void setMessageBarPosition(MessageBarPosition position);

	void setNotesPosition(SlideNotesPosition position);

	void setNoteSlidePosition(NoteSlidePosition position);

	void setParticipantsPosition(ParticipantsPosition position);

	void setParticipantVideoPosition(ParticipantVideoPosition position);

	void setPreviewPosition(SlidePreviewPosition position);

}

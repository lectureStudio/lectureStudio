/*
 * Copyright (C) 2021 TU Darmstadt, Department of Computer Science,
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
import static java.util.Objects.requireNonNull;

import com.google.common.eventbus.Subscribe;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;
import javax.inject.Singleton;
import javax.ws.rs.core.MediaType;

import org.lecturestudio.core.ExecutableException;
import org.lecturestudio.core.ExecutableState;
import org.lecturestudio.core.bus.ApplicationBus;
import org.lecturestudio.core.bus.event.DocumentEvent;
import org.lecturestudio.core.bus.event.PageEvent;
import org.lecturestudio.core.bus.event.RecordActionEvent;
import org.lecturestudio.core.model.Document;
import org.lecturestudio.core.model.Page;
import org.lecturestudio.core.model.TemplateDocument;
import org.lecturestudio.core.recording.RecordedPage;
import org.lecturestudio.core.recording.action.PlaybackAction;
import org.lecturestudio.core.service.DocumentService;
import org.lecturestudio.presenter.api.event.RecordingStateEvent;
import org.lecturestudio.presenter.api.model.QuizDocument;
import org.lecturestudio.presenter.api.recording.PendingActions;
import org.lecturestudio.web.api.client.MultipartBody;
import org.lecturestudio.web.api.stream.StreamEventRecorder;
import org.lecturestudio.web.api.stream.action.StreamAction;
import org.lecturestudio.web.api.stream.action.StreamDocumentAction;
import org.lecturestudio.web.api.stream.action.StreamDocumentCloseAction;
import org.lecturestudio.web.api.stream.action.StreamDocumentCreateAction;
import org.lecturestudio.web.api.stream.action.StreamDocumentSelectAction;
import org.lecturestudio.web.api.stream.action.StreamInitAction;
import org.lecturestudio.web.api.stream.action.StreamPageActionsAction;
import org.lecturestudio.web.api.stream.action.StreamPageCreatedAction;
import org.lecturestudio.web.api.stream.action.StreamPageDeletedAction;
import org.lecturestudio.web.api.stream.action.StreamPagePlaybackAction;
import org.lecturestudio.web.api.stream.action.StreamPageSelectedAction;
import org.lecturestudio.web.api.stream.action.StreamStartAction;
import org.lecturestudio.web.api.stream.model.Course;
import org.lecturestudio.web.api.stream.service.StreamProviderService;

@Singleton
public class WebRtcStreamEventRecorder extends StreamEventRecorder {

	private final DocumentService documentService;

	private StreamProviderService streamProviderService;

	private PendingActions pendingActions;

	private Page currentPage;

	private Course course;

	private ExecutableState recordState;

	private long startTime = -1;

	private long pauseTime;

	private long halted = 0;


	@Inject
	public WebRtcStreamEventRecorder(DocumentService documentService) {
		this.documentService = documentService;
	}

	public void setCourse(Course course) {
		this.course = course;
	}

	public void setStreamProviderService(StreamProviderService streamService) {
		this.streamProviderService = streamService;
	}

	public void shareDocument(Document document) throws IOException {
		sendDocument(document);

		addPlaybackAction(new StreamDocumentSelectAction(document));
		addPlaybackAction(new StreamPageSelectedAction(document.getCurrentPage()));
	}

	@Override
	public long getElapsedTime() {
		if (startTime == -1) {
			return 0;
		}
		if (started()) {
			return System.currentTimeMillis() - startTime - halted;
		}
		if (suspended()) {
			return pauseTime - startTime - halted;
		}

		return 0;
	}

	@Override
	public List<StreamPageActionsAction> getPreRecordedActions() {
		List<StreamPageActionsAction> actions = new ArrayList<>();

		for (var entry : pendingActions.getAllPendingActions().entrySet()) {
			Page page = entry.getKey();
			Document document = page.getDocument();

			int pageNumber = document.getPageIndex(page);
			int documentId = page.getDocument().hashCode();

			RecordedPage recordedPage = new RecordedPage();
			recordedPage.setNumber(pageNumber);
			recordedPage.getPlaybackActions().addAll(entry.getValue());

			actions.add(new StreamPageActionsAction(documentId, recordedPage));
		}

		return actions;
	}

	@Subscribe
	public void onEvent(final RecordingStateEvent event) {
		final ExecutableState state = event.getState();

		if (recordState == state || state.name().contains("ing")) {
			return;
		}
		if (event.started() && recordState == ExecutableState.Suspended) {
			return;
		}

		recordState = state;

		if (!started()) {
			return;
		}

		if (event.started() || event.stopped()) {
			sendRecordingState(recordState == ExecutableState.Started);
		}
	}

	@Subscribe
	public void onEvent(final RecordActionEvent event) {
		addPendingAction(event.getAction());

		if (!started()) {
			return;
		}

		PlaybackAction action = event.getAction();
		action.setTimestamp((int) getElapsedTime());

		addPlaybackAction(new StreamPagePlaybackAction(currentPage, action));
	}

	@Subscribe
	public void onEvent(final PageEvent event) {
		pendingActions.setPendingPage(event.getPage());

		if (!started()) {
			return;
		}

		currentPage = event.getPage();

		switch (event.getType()) {
			case CREATED -> addPlaybackAction(new StreamPageCreatedAction(currentPage));
			case REMOVED -> addPlaybackAction(new StreamPageDeletedAction(currentPage));
			case SELECTED -> addPlaybackAction(new StreamPageSelectedAction(currentPage));
			default -> {
			}
		}
	}

	@Subscribe
	public void onEvent(final DocumentEvent event) {
		Document doc = event.getDocument();

		currentPage = doc.getCurrentPage();
		pendingActions.setPendingPage(doc.getCurrentPage());

		if (!started()) {
			if (event.closed()) {
				removeActionsForDocument(doc);
			}
			return;
		}

		StreamDocumentAction action = null;

		if (event.created()) {
			try {
				action = uploadDocument(doc);
			}
			catch (IOException e) {
				logException(e, "Transmit document failed");
			}
		}
		else if (event.closed()) {
			action = new StreamDocumentCloseAction(doc);

			removeActionsForDocument(doc);
		}
		else if (event.selected()) {
			currentPage = doc.getCurrentPage();

			action = new StreamDocumentSelectAction(doc);
		}
		else if (event.replaced()) {
			removeActionsForDocument(event.getOldDocument());

			// Transmit quiz documents only in initial state.
			boolean isInitialQuiz = false;

			if (doc instanceof QuizDocument quizDoc) {
				isInitialQuiz = !quizDoc.hasAnswers();
			}

			if (isInitialQuiz || doc.isMessage() || doc.isScreen()) {
				try {
					shareDocument(doc);
				}
				catch (IOException e) {
					logException(e, "Transmit document failed");
				}
			}

			// Set current pending page, as it may have been removed previously
			// by removeActionsForDocument().
			pendingActions.setPendingPage(doc.getCurrentPage());
		}

		if (nonNull(action)) {
			addPlaybackAction(action);

			// Keep the state up to date and publish the current page.
			if (event.selected()) {
				Page page = doc.getCurrentPage();
				addPlaybackAction(new StreamPageSelectedAction(page));
			}
		}
	}

	@Override
	protected void initInternal() throws ExecutableException {
		pendingActions = new PendingActions();
		pendingActions.initialize();

		ApplicationBus.register(this);
	}

	@Override
	protected void startInternal() throws ExecutableException {
		requireNonNull(course, "Course must be set");

		// Transmit initial document state.
		Document document = documentService.getDocuments().getSelectedDocument();

		addPlaybackAction(new StreamInitAction(course.getId()));

		try {
			// Upload all opened PDF documents.
			for (var doc : documentService.getDocuments().asList()) {
				sendDocument(doc);
			}

			addPlaybackAction(new StreamDocumentSelectAction(document));
			addPlaybackAction(new StreamPageSelectedAction(document.getCurrentPage()));

			getPreRecordedActions().forEach(this::addPlaybackAction);

			addPlaybackAction(new StreamStartAction(course.getId()));
		}
		catch (Exception e) {
			throw new ExecutableException("Send action failed", e);
		}

		boolean isRecorded = recordState == ExecutableState.Started;
		if (isRecorded) {
			sendRecordingState(isRecorded);
		}

		ExecutableState prevState = getPreviousState();

		if (prevState == ExecutableState.Initialized
				|| prevState == ExecutableState.Stopped) {
			startTime = System.currentTimeMillis();
		}
		else if (prevState == ExecutableState.Suspended) {
			halted += System.currentTimeMillis() - pauseTime;
		}

		pauseTime = 0;
	}

	@Override
	protected void suspendInternal() {
		if (getPreviousState() == ExecutableState.Started) {
			pauseTime = System.currentTimeMillis();
		}
	}

	@Override
	protected void stopInternal() {
		startTime = -1;
		halted = 0;
	}

	@Override
	protected void destroyInternal() {
		ApplicationBus.unregister(this);
	}

	private void sendRecordingState(boolean isRecorded) {
		try {
			streamProviderService.setCourseRecordingState(course.getId(),
					isRecorded);
		}
		catch (Throwable e) {
			logException(e, "Set course recording state failed");
		}
	}

	private void addPendingAction(PlaybackAction action) {
		if (isNull(action)) {
			return;
		}

		action.setTimestamp((int) getElapsedTime());

		pendingActions.addPendingAction(action);
	}

	private void addPlaybackAction(StreamAction action) {
		if (isNull(action) || !(started() || getState() == ExecutableState.Starting)) {
			return;
		}

		notifyActionConsumers(action);
	}

	private void removeActionsForDocument(Document document) {
		document.getPages().forEach(pendingActions::clearPendingActions);
	}

	private void sendDocument(Document document) throws IOException {
		StreamDocumentCreateAction action = uploadDocument(document);

		addPlaybackAction(action);
	}

	private StreamDocumentCreateAction uploadDocument(Document document)
			throws IOException {
		if (document.isWhiteboard()) {
			// Copy whiteboard document with initially N empty pages.
			// The empty pages are used to simplify new page creation on the client side.
			ByteArrayOutputStream data = new ByteArrayOutputStream();

			document.toOutputStream(data);

			Document whiteboard = new TemplateDocument(data.toByteArray());
			whiteboard.setTitle(document.getName());
			whiteboard.setDocumentType(document.getType());

			for (int i = 0; i < 100; i++) {
				whiteboard.createPage();
			}

			document = whiteboard;
		}

		String docFileName = document.getUid().toString() + ".pdf";
		ByteArrayOutputStream docData = new ByteArrayOutputStream();

		document.toOutputStream(docData);

		MultipartBody body = new MultipartBody();
		body.addFormData("file",
				new ByteArrayInputStream(docData.toByteArray()),
				MediaType.MULTIPART_FORM_DATA_TYPE, docFileName);

		String remoteFile = streamProviderService.uploadFile(body);

		StreamDocumentCreateAction docCreateAction = new StreamDocumentCreateAction(document);
		docCreateAction.setDocumentFile(remoteFile);
		docCreateAction.setDocumentChecksum(document.getUid().toString());

		return docCreateAction;
	}
}

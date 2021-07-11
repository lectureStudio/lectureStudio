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

import com.google.common.eventbus.Subscribe;

import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.List;

import org.lecturestudio.core.ExecutableState;
import org.lecturestudio.core.bus.ApplicationBus;
import org.lecturestudio.core.bus.event.DocumentEvent;
import org.lecturestudio.core.bus.event.PageEvent;
import org.lecturestudio.core.bus.event.RecordActionEvent;
import org.lecturestudio.core.model.Document;
import org.lecturestudio.core.model.Page;
import org.lecturestudio.core.model.listener.DocumentChangeListener;
import org.lecturestudio.core.recording.RecordedPage;
import org.lecturestudio.core.recording.action.PlaybackAction;
import org.lecturestudio.presenter.api.recording.PendingActions;
import org.lecturestudio.web.api.stream.StreamEventRecorder;
import org.lecturestudio.web.api.stream.action.StreamAction;
import org.lecturestudio.web.api.stream.action.StreamDocumentAction;
import org.lecturestudio.web.api.stream.action.StreamDocumentCloseAction;
import org.lecturestudio.web.api.stream.action.StreamDocumentCreateAction;
import org.lecturestudio.web.api.stream.action.StreamDocumentSelectAction;
import org.lecturestudio.web.api.stream.action.StreamPageActionsAction;
import org.lecturestudio.web.api.stream.action.StreamPageCreatedAction;
import org.lecturestudio.web.api.stream.action.StreamPageDeletedAction;
import org.lecturestudio.web.api.stream.action.StreamPagePlaybackAction;
import org.lecturestudio.web.api.stream.action.StreamPageSelectedAction;

public class WebRtcStreamEventRecorder extends StreamEventRecorder {

	private final DocumentChangeListener documentChangeListener = new DocumentChangeListener() {

		@Override
		public void documentChanged(Document document) {
			updateDocument(document);
		}

		@Override
		public void pageAdded(Page page) {

		}

		@Override
		public void pageRemoved(Page page) {

		}
	};

	private PendingActions pendingActions;

	private Page currentPage;

	private long startTime = -1;

	private long pauseTime;

	private long halted = 0;


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
	public void onEvent(final RecordActionEvent event) {
		if (initialized() || suspended()) {
			addPendingAction(event.getAction());
		}
		if (!started()) {
			return;
		}

		PlaybackAction action = event.getAction();
		action.setTimestamp((int) getElapsedTime());

		addPlaybackAction(new StreamPagePlaybackAction(currentPage, action));
	}

	@Subscribe
	public void onEvent(final PageEvent event) {
		if (initialized() || suspended()) {
			pendingActions.setPendingPage(event.getPage());
		}
		if (!started()) {
			return;
		}

		currentPage = event.getPage();

		switch (event.getType()) {
			case CREATED:
				addPlaybackAction(new StreamPageCreatedAction(currentPage));
				break;

			case REMOVED:
				addPlaybackAction(new StreamPageDeletedAction(currentPage));
				break;

			case SELECTED:
				addPlaybackAction(new StreamPageSelectedAction(currentPage));
				break;

			default:
				break;
		}
	}

	@Subscribe
	public void onEvent(final DocumentEvent event) {
		if (initialized() || suspended()) {
			pendingActions.setPendingPage(event.getDocument().getCurrentPage());
		}
		if (!started()) {
			return;
		}

		Document doc = event.getDocument();
		String checksum = null;

		if (!event.closed()) {
			try {
				MessageDigest digest = MessageDigest.getInstance("MD5");

				checksum = doc.getChecksum(digest);
			}
			catch (Exception e) {
				logException(e, "Get document checksum failed");
			}
		}

		StreamDocumentAction action = null;

		if (event.created()) {
			action = new StreamDocumentCreateAction(doc);
		}
		else if (event.closed()) {
			action = new StreamDocumentCloseAction(doc);
		}
		else if (event.selected()) {
			Document oldDoc = event.getOldDocument();

			if (nonNull(oldDoc)) {
				oldDoc.removeChangeListener(documentChangeListener);
			}

			doc.addChangeListener(documentChangeListener);

			action = new StreamDocumentSelectAction(doc);
		}

		if (nonNull(action)) {
			action.setDocumentChecksum(checksum);

			addPlaybackAction(action);

			// Keep the state up to date and publish the current page.
			if (event.selected()) {
				Page page = doc.getCurrentPage();
				addPlaybackAction(new StreamPageSelectedAction(page));
			}
		}
	}

	@Override
	protected void initInternal() {
		pendingActions = new PendingActions();
		pendingActions.initialize();
	}

	@Override
	protected void startInternal() {
		ExecutableState prevState = getPreviousState();

		if (prevState == ExecutableState.Initialized || prevState == ExecutableState.Stopped) {
			startTime = System.currentTimeMillis();
		}
		else if (prevState == ExecutableState.Suspended) {
			halted += System.currentTimeMillis() - pauseTime;
		}

		pauseTime = 0;

		ApplicationBus.register(this);
	}

	@Override
	protected void suspendInternal() {
		if (getPreviousState() == ExecutableState.Started) {
			pauseTime = System.currentTimeMillis();
		}
	}

	@Override
	protected void stopInternal() {
		ApplicationBus.unregister(this);

		startTime = -1;
		halted = 0;
	}

	@Override
	protected void destroyInternal() {

	}

	private void addPendingAction(PlaybackAction action) {
		if (isNull(action)) {
			return;
		}

		action.setTimestamp((int) getElapsedTime());

		pendingActions.addPendingAction(action);
	}

	private void addPlaybackAction(StreamAction action) {
		if (!started() || isNull(action)) {
			return;
		}

		notifyActionConsumers(action);
	}

	private void updateDocument(Document document) {
		String checksum = null;

		try {
			MessageDigest digest = MessageDigest.getInstance("MD5");

			checksum = document.getChecksum(digest);
		}
		catch (Exception e) {
			logException(e, "Get document checksum failed");
		}

		StreamDocumentAction action = new StreamDocumentCreateAction(document);
		action.setDocumentFile(document.getName() + ".pdf");
		action.setDocumentChecksum(checksum);

		addPlaybackAction(action);

		// Keep the state up to date and publish the current page.
		Page page = document.getCurrentPage();
		addPlaybackAction(new StreamPageSelectedAction(page));
	}
}

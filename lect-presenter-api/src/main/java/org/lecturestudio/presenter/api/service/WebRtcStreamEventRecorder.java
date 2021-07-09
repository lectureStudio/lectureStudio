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

import org.lecturestudio.core.ExecutableState;
import org.lecturestudio.core.bus.ApplicationBus;
import org.lecturestudio.core.bus.event.DocumentEvent;
import org.lecturestudio.core.bus.event.PageEvent;
import org.lecturestudio.core.bus.event.RecordActionEvent;
import org.lecturestudio.core.model.Document;
import org.lecturestudio.core.model.Page;
import org.lecturestudio.core.model.listener.DocumentChangeListener;
import org.lecturestudio.core.recording.LectureRecorder;
import org.lecturestudio.core.recording.action.CreatePageAction;
import org.lecturestudio.core.recording.action.DocumentAction;
import org.lecturestudio.core.recording.action.DocumentCloseAction;
import org.lecturestudio.core.recording.action.DocumentCreateAction;
import org.lecturestudio.core.recording.action.DocumentSelectAction;
import org.lecturestudio.core.recording.action.PageAction;
import org.lecturestudio.core.recording.action.PlaybackAction;
import org.lecturestudio.core.recording.action.RemovePageAction;

public class WebRtcStreamEventRecorder extends LectureRecorder {

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

	@Subscribe
	public void onEvent(final RecordActionEvent event) {
		if (!started()) {
			return;
		}

		addPlaybackAction(event.getAction());
	}

	@Subscribe
	public void onEvent(final PageEvent event) {
		if (!started()) {
			return;
		}

		Page page = event.getPage();

		switch (event.getType()) {
			case CREATED:
				addPlaybackAction(new CreatePageAction());
				break;

			case REMOVED:
				addPlaybackAction(new RemovePageAction(page.getDocument().getPageIndex(page)));
				break;

			case SELECTED:
				addPlaybackAction(encodePage(page, page.getDocument().getPageIndex(page)));
				break;

			default:
				break;
		}
	}

	@Subscribe
	public void onEvent(final DocumentEvent event) {
		if (!started()) {
			return;
		}

		Document doc = event.getDocument();
		String docFile = doc.getName() + ".pdf";
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

		DocumentAction action = null;

		if (event.created()) {
			action = new DocumentCreateAction(doc);
		}
		else if (event.closed()) {
			action = new DocumentCloseAction(doc);
		}
		else if (event.selected()) {
			Document oldDoc = event.getOldDocument();

			if (nonNull(oldDoc)) {
				oldDoc.removeChangeListener(documentChangeListener);
			}

			doc.addChangeListener(documentChangeListener);

			action = new DocumentSelectAction(doc);
		}

		if (nonNull(action)) {
			action.setDocumentChecksum(checksum);

			addPlaybackAction(action);

			// Keep the state up to date and publish the current page.
			if (event.selected()) {
				Page page = doc.getCurrentPage();
				addPlaybackAction(encodePage(page, page.getDocument().getPageIndex(page)));
			}
		}
	}

	@Override
	protected void initInternal() {

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

	private void addPlaybackAction(PlaybackAction action) {
		if (!started() || isNull(action)) {
			return;
		}

		action.setTimestamp((int) getElapsedTime());

		notifyActionConsumers(action);
	}

	private PageAction encodePage(Page page, int number) {
		int docId = page.getDocument().hashCode();

		return new PageAction(docId, number);
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

		DocumentAction action = new DocumentCreateAction(document);
		action.setDocumentFile(document.getName() + ".pdf");
		action.setDocumentChecksum(checksum);

		addPlaybackAction(action);

		// Keep the state up to date and publish the current page.
		Page page = document.getCurrentPage();
		addPlaybackAction(encodePage(page, page.getDocument().getPageIndex(page)));
	}
}

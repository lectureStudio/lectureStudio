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

package org.lecturestudio.media.net;

import static java.util.Objects.nonNull;

import com.google.common.eventbus.Subscribe;

import java.security.MessageDigest;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import org.lecturestudio.core.ExecutableState;
import org.lecturestudio.core.bus.ApplicationBus;
import org.lecturestudio.core.bus.event.DocumentEvent;
import org.lecturestudio.core.bus.event.PageEvent;
import org.lecturestudio.core.bus.event.RecordActionEvent;
import org.lecturestudio.core.model.Document;
import org.lecturestudio.core.model.Page;
import org.lecturestudio.core.model.listener.DocumentChangeListener;
import org.lecturestudio.core.recording.LectureRecorder;
import org.lecturestudio.core.recording.LectureRecorderListener;
import org.lecturestudio.core.recording.action.PageAction;
import org.lecturestudio.core.recording.action.PlaybackAction;
import org.lecturestudio.web.api.stream.action.StreamAction;
import org.lecturestudio.web.api.stream.action.StreamDocumentAction;
import org.lecturestudio.web.api.stream.action.StreamDocumentCloseAction;
import org.lecturestudio.web.api.stream.action.StreamDocumentCreateAction;
import org.lecturestudio.web.api.stream.action.StreamDocumentSelectAction;
import org.lecturestudio.web.api.stream.action.StreamPageCreatedAction;
import org.lecturestudio.web.api.stream.action.StreamPageDeletedAction;
import org.lecturestudio.web.api.stream.action.StreamPagePlaybackAction;
import org.lecturestudio.web.api.stream.action.StreamPageSelectedAction;

public class StreamEventRecorder extends LectureRecorder {

	private static final Logger LOG = LogManager.getLogger(StreamEventRecorder.class);

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

//	private PendingActions pendingActions;

	private Page currentPage;

	private LectureRecorderListener listener;

	private long startTime = -1;

	private long pauseTime;

	private long halted = 0;
	

	public StreamEventRecorder() {
		ApplicationBus.register(this);
	}

	@Subscribe
	public void onEvent(final RecordActionEvent event) {
		if (!started()) {
			return;
		}
		
		PlaybackAction action = event.getAction();
		action.setTimestamp((int) getElapsedTime());

		addPlaybackAction(new StreamPagePlaybackAction(currentPage, action));
	}
	
    @Subscribe
	public void onEvent(final PageEvent event) {
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
				LOG.error("Get document checksum failed.", e);
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
			action.setDocumentFile(docFile);
			action.setDocumentChecksum(checksum);

			addPlaybackAction(action);
			
			// Keep the state up to date and publish the current page.
			if (event.selected()) {
				Page page = doc.getCurrentPage();
				addPlaybackAction(new StreamPageSelectedAction(page));
			}
		}
	}
	
	public void setListener(LectureRecorderListener listener) {
		this.listener = listener;
	}
	
	@Override
	public synchronized long getElapsedTime() {
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
	protected void initInternal() {
		
	}

	@Override
	protected void startInternal() {
		ExecutableState state = getPreviousState();
		
		if (state == ExecutableState.Initialized || state == ExecutableState.Stopped) {
			startTime = System.currentTimeMillis();
			
			//recordDocumentState();
		}
		else if (state == ExecutableState.Suspended) {
			halted += System.currentTimeMillis() - pauseTime;
		}

		pauseTime = 0;
	}

	@Override
	protected void stopInternal() {
		startTime = -1;
		halted = 0;
	}
	
	@Override
	protected void suspendInternal() {
		if (getPreviousState() == ExecutableState.Started) {
			pauseTime = System.currentTimeMillis();
		}
	}

	@Override
	protected void destroyInternal() {
		listener = null;
	}

	private void addPlaybackAction(StreamAction action) {
		if (!started() || action == null) {
			return;
		}

		//listener.eventRecorded(action);
	}

	private PageAction encodePage(Page page, int number) {
		int docId = page.getDocument().hashCode();

		return new PageAction(docId, number);
	}

	private void updateDocument(Document document) {
		String docFile = document.getName() + ".pdf";
		String checksum = null;

		try {
			MessageDigest digest = MessageDigest.getInstance("MD5");

			checksum = document.getChecksum(digest);
		}
		catch (Exception e) {
			LOG.error("Get document checksum failed.", e);
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

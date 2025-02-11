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

package org.lecturestudio.presenter.api.recording;

import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;

import java.io.IOException;
import java.util.*;

import org.lecturestudio.core.ExecutableBase;
import org.lecturestudio.core.ExecutableException;
import org.lecturestudio.core.model.Document;
import org.lecturestudio.core.model.Page;
import org.lecturestudio.core.recording.PendingActions;
import org.lecturestudio.core.recording.RecordedPage;
import org.lecturestudio.core.recording.action.PlaybackAction;
import org.lecturestudio.core.recording.action.StaticShapeAction;

/**
 * Thread-safe PDF slide recorder.
 *
 * @author Alex Andres
 */
public class SlideRecorder extends ExecutableBase {

	private final Stack<RecordedPage> recordedPages;

	private final Map<Page, RecordedPage> addedPages;

	private final PendingActions pendingActions;

	private Document recordedDocument;


	public SlideRecorder() {
		recordedPages = new Stack<>();
		addedPages = new LinkedHashMap<>();
		pendingActions = new PendingActions();
	}

	public synchronized Document getRecordedDocument() {
		return recordedDocument;
	}

	public void insertPage(Page page, long currentTime, long openTime) throws IOException {
		// Do not add equal pages consecutively.
		if (isDuplicate(page) || openTime < 0) {
			return;
		}

		long timestamp = currentTime - openTime;

		recordPage(page, timestamp);
	}

	public synchronized void recordPage(Page page, long timestamp) throws IOException {
		int pageNumber = getRecordedPageCount();

		RecordedPage recPage = new RecordedPage();
		recPage.setTimestamp((int) timestamp);
		recPage.setNumber(pageNumber);

		// Copy all actions if the page was previously annotated and visited again.
		if (addedPages.containsKey(page)) {
			RecordedPage rPage = addedPages.get(page);

			if (rPage != null) {
				for (StaticShapeAction action : rPage.getStaticActions()) {
					recPage.addStaticAction(action.clone());
				}
				for (PlaybackAction action : rPage.getPlaybackActions()) {
					StaticShapeAction staticAction = new StaticShapeAction(action.clone());
					recPage.addStaticAction(staticAction);
				}
			}
		}
		if (pendingActions.hasPendingActions(page)) {
			// Unrecorded actions, e.g., during suspension.
			insertPendingPageActions(recPage, page);
		}

		recordedDocument.createPage(page);

		// Update page to last recorded page relation.
		addedPages.remove(page);
		addedPages.put(page, recPage);

		recordedPages.push(recPage);
	}

	public synchronized Stack<RecordedPage> getRecordedPages() {
		return recordedPages;
	}

	public synchronized int getRecordedPageCount() {
		return recordedPages.size();
	}

	public synchronized Map<Page, RecordedPage> getRecordedPageMap() {
		return addedPages;
	}

	public RecordedPage getRecentRecordedPage() {
		synchronized (recordedPages) {
			return recordedPages.isEmpty() ? null : recordedPages.peek();
		}
	}

	public String getBestRecordingName() {
		String name = null;

		for (Page page : addedPages.keySet()) {
			Document doc = page.getDocument();

			if (doc.isPDF() && nonNull(doc.getName())) {
				// Return the name of the first used PDF document.
				return doc.getName();
			}

			name = doc.getName();
		}

		return name;
	}

	public void addPendingAction(PlaybackAction action, long timestamp) {
		if (isNull(action)) {
			return;
		}

		action.setTimestamp((int) timestamp);

		synchronized (pendingActions) {
			pendingActions.addPendingAction(action);
		}
	}

	public synchronized Page getPendingPage() {
		return pendingActions.getPendingPage();
	}

	public synchronized void setPendingPage(Page page) {
		pendingActions.setPendingPage(page);
	}

	public void updatePendingPage(long currentTime) throws IOException {
		Page pendingPage = getPendingPage();

		if (nonNull(pendingPage)) {
			if (isDuplicate(pendingPage)) {
				insertPendingActions(getRecentRecordedPage(), pendingPage);
			}
			else {
				insertPage(pendingPage, currentTime, 0);
			}
		}
	}

	@Override
	protected void initInternal() throws ExecutableException {
		clearRecordedPages();
		clearPendingActions();
	}

	@Override
	protected void startInternal() throws ExecutableException {
		// Release resources, if not happened before.
		if (nonNull(recordedDocument)) {
			recordedDocument.close();
		}

		try {
			recordedDocument = new Document();
		}
		catch (IOException e) {
			throw new ExecutableException("Could not create document.", e);
		}

		clearRecordedPages();
	}

	@Override
	protected void stopInternal() throws ExecutableException {
		// Do not close the recorded document here, since its resources are
		// required to be available for storing after this recorder stopps.
		resetPendingActions();
	}

	@Override
	protected void destroyInternal() throws ExecutableException {
		recordedDocument.close();
	}

	private void clearRecordedPages() {
		recordedPages.clear();
		addedPages.clear();
	}

	private void insertPendingActions(RecordedPage recPage, Page page) {
		List<PlaybackAction> actions = pendingActions.getPendingActions(page);

		for (PlaybackAction action : actions) {
			recPage.addPlaybackAction(action.clone());
		}
	}

	private void insertPendingPageActions(RecordedPage recPage, Page page) {
		List<PlaybackAction> actions = pendingActions.getPendingActions(page);

		for (PlaybackAction action : actions) {
			StaticShapeAction staticAction = new StaticShapeAction(action.clone());
			recPage.addStaticAction(staticAction);
		}

		pendingActions.clearPendingActions(page);
	}

	private void clearPendingActions() {
		pendingActions.clear();
		pendingActions.initialize();
	}

	private void resetPendingActions() {
		// Reset state.
		clearPendingActions();

		// Backup recorded actions, in case the recording is restarted to use them as static actions.
		for (Map.Entry<Page, RecordedPage> entry : getRecordedPageMap().entrySet()) {
			Page page = entry.getKey();
			RecordedPage rPage = entry.getValue();

			pendingActions.setPendingPage(page);

			for (StaticShapeAction action : rPage.getStaticActions()) {
				pendingActions.addPendingAction(action.getAction().clone());
			}
			for (PlaybackAction action : rPage.getPlaybackActions()) {
				pendingActions.addPendingAction(action.clone());
			}
		}
	}

	private Page getLastRecordedPage() {
		Set<Page> pageSet = getRecordedPageMap().keySet();
		return pageSet.stream().skip(pageSet.size() - 1).findFirst().orElse(null);
	}

	private boolean isDuplicate(Page page) {
		Page lastRecorded = getLastRecordedPage();
		boolean same = page.equals(lastRecorded);

		if (!same && lastRecorded != null) {
			UUID lastId = lastRecorded.getUid();
			UUID pageId = page.getUid();

			if (nonNull(lastId) && nonNull(pageId) && lastId.equals(pageId)) {
				// Do not record duplicate pages.
				same = true;
			}
		}

		return same;
	}
}

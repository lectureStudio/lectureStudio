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

package org.lecturestudio.core.recording;

import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ConcurrentHashMap;

import org.lecturestudio.core.ExecutableBase;
import org.lecturestudio.core.ExecutableException;
import org.lecturestudio.core.model.Document;
import org.lecturestudio.core.model.Page;
import org.lecturestudio.core.model.listener.PageEditEvent;
import org.lecturestudio.core.model.listener.PageEditedListener;

/**
 * Records all opened documents and visited pages. The recorded documents can be
 * saved with all pages in the same order they were visited.
 *
 * @author Alex Andres
 */
public class DocumentRecorder extends ExecutableBase {

	private IdleTimer idleTimer;

	private Map<Document, Document> documentMap;

	private Map<Document, Page> documentPageMap;

	private List<Page> recordedPages;

	private int pageRecordingTimeout = 2000;

	private PageListener pageListener;


	/**
	 * Returns all recorded documents. The order of recorded documents is not
	 * guaranteed.
	 *
	 * @return A collection of all recorded documents.
	 */
	public Collection<Document> getRecordedDocuments() {
		if (!started()) {
			return Collections.emptyList();
		}

		return documentMap.values();
	}

	/**
	 * Returns all recorded pages with visitation order between different
	 * documents.
	 *
	 * @return A list of all recorded pages.
	 */
	public List<Page> getRecordedPages() {
		if (!started()) {
			return List.of();
		}

		return recordedPages;
	}

	/**
	 * Records the provided page. The page will be automatically assigned to a
	 * documents it belongs to. The document preserves the insertion order of
	 * pages.
	 *
	 * @param page The page to record.
	 *
	 * @throws ExecutableException If the recording has not been started, or the
	 *                             page could not be recorded.
	 */
	public void recordPage(Page page) throws ExecutableException {
		if (!started()) {
			throw new ExecutableException("Recording has not been started");
		}

		Page prevPage = documentPageMap.get(page.getDocument());

		if (nonNull(prevPage) && prevPage == page) {
			// Do not record the same page successively.
			return;
		}

		runIdleTimer(page);
	}

	/**
	 * Sets the page recording timeout in milliseconds. A page will be recorded
	 * if the timeout has elapsed on the same visited page.
	 *
	 * @param timeoutMs The timeout in milliseconds.
	 */
	public void setPageRecordingTimeout(int timeoutMs) {
		pageRecordingTimeout = timeoutMs;
	}

	@Override
	protected void initInternal() throws ExecutableException {
		documentMap = new ConcurrentHashMap<>();
		documentPageMap = new ConcurrentHashMap<>();
		recordedPages = new ArrayList<>();
	}

	@Override
	protected void startInternal() throws ExecutableException {
		// Nothing to do.
	}

	@Override
	protected void stopInternal() throws ExecutableException {
		if (nonNull(idleTimer)) {
			idleTimer.stop();
		}

		documentMap.clear();
		documentPageMap.clear();
		recordedPages.clear();
	}

	@Override
	protected void destroyInternal() throws ExecutableException {
		// Nothing to do.
	}

	private void insertPage(Page page) throws ExecutableException {
		Document pageDoc = page.getDocument();
		Document recDocument = documentMap.get(pageDoc);

		if (isNull(recDocument)) {
			try {
				recDocument = new Document();
				recDocument.setAuthor(pageDoc.getAuthor());
				recDocument.setDocumentType(pageDoc.getType());
				recDocument.setTitle(pageDoc.getName());

				documentMap.put(pageDoc, recDocument);
			}
			catch (IOException e) {
				throw new ExecutableException(e);
			}
		}

		try {
			Page recPage = recDocument.createPage(page);
			recPage.addShapes(page.getShapes());

			if (nonNull(pageListener)) {
				// Cancel observing the previous page.
				pageListener.dispose();
			}
			pageListener = new PageListener(page, recPage);

			recordedPages.add(recPage);

			// Register page to avoid successive recording.
			documentPageMap.put(pageDoc, page);
		}
		catch (IOException e) {
			throw new ExecutableException(e);
		}
	}

	private void runIdleTimer(Page page) {
		if (nonNull(idleTimer)) {
			// Ignore all previous tasks.
			idleTimer.stop();
		}

		idleTimer = new IdleTimer(page);
		idleTimer.runIdleTask();
	}



	private static class PageListener implements PageEditedListener {

		private final Page page;

		private final Page srcPage;


		PageListener(Page srcPage, Page dstPage) {
			this.srcPage = srcPage;
			this.page = dstPage;

			srcPage.addPageEditedListener(this);
		}

		@Override
		public void pageEdited(PageEditEvent event) {
			switch (event.getType()) {
				case CLEAR:
					page.clear();
					break;
				case SHAPE_ADDED:
					page.addShape(event.getShape());
					break;
				case SHAPES_ADDED:
					page.clear();
					page.addShapes(event.getPage().getShapes());
					break;
				case SHAPE_REMOVED:
					page.removeShape(event.getShape());
					break;
			}
		}

		void dispose() {
			srcPage.removePageEditedListener(this);
		}
	}



	private class IdleTimer extends Timer {

		private final Page page;

		private TimerTask idleTask;


		IdleTimer(Page page) {
			this.page = page;
		}

		void runIdleTask() {
			idleTask = new TimerTask() {

				@Override
				public void run() {
					try {
						insertPage(page);
					}
					catch (ExecutableException e) {
						e.printStackTrace();
					}
				}
			};

			schedule(idleTask, pageRecordingTimeout);
		}

		public void stop() {
			cancel();
			purge();

			idleTask = null;
		}
	}
}

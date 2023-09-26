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
import java.util.concurrent.atomic.AtomicBoolean;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import org.lecturestudio.core.ExecutableBase;
import org.lecturestudio.core.ExecutableException;
import org.lecturestudio.core.app.ApplicationContext;
import org.lecturestudio.core.beans.BooleanProperty;
import org.lecturestudio.core.geometry.Rectangle2D;
import org.lecturestudio.core.model.Document;
import org.lecturestudio.core.model.Page;
import org.lecturestudio.core.model.listener.PageEditEvent;
import org.lecturestudio.core.model.listener.PageEditedListener;
import org.lecturestudio.core.view.PresentationParameter;
import org.lecturestudio.core.view.PresentationParameterProvider;
import org.lecturestudio.core.view.ViewType;

/**
 * Records all opened documents and visited pages. The recorded documents can be
 * saved with all pages in the same order they were visited.
 *
 * @author Alex Andres
 */
@Singleton
public class DocumentRecorder extends ExecutableBase {

	private static final Logger LOG = LogManager.getLogger(DocumentRecorder.class);

	private final ApplicationContext context;

	private PresentationParameterProvider paramProvider;

	private PresentationParameterProvider recParamProvider;

	private IdleTimer idleTimer;

	private Map<Document, Document> documentMap;

	private Map<Document, PageState> documentPageMap;

	private List<Page> recordedPages;

	private BooleanProperty hasChangesProperty;

	private int pageRecordingTimeout = 2000;


	@Inject
	public DocumentRecorder(ApplicationContext context) {
		this.context = context;
	}

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
	 * Returns the recorded {@code PresentationParameter}s for each page.
	 *
	 * @return The provider for recorded {@code PresentationParameter}s.
	 */
	public PresentationParameterProvider getRecordedParamProvider() {
		if (!started()) {
			return null;
		}

		return recParamProvider;
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

		Document doc = page.getDocument();
		PageState state = documentPageMap.get(doc);

		if (nonNull(state) && state.srcPage == page) {
			// Switch to new page provision mode.
			state.setProvisionMode();

			// Do not record the same page successively.
			return;
		}

		if (pageRecordingTimeout < 1) {
			insertPage(page);
		}
		else {
			runIdleTimer(page);
		}
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

	/**
	 * Sets the property that should be updated whenever a change on a recorded
	 * page has occurred.
	 *
	 * @param property The property to update on recording changes.
	 */
	public void setHasChangesProperty(BooleanProperty property) {
		hasChangesProperty = property;
	}

	@Override
	protected void initInternal() throws ExecutableException {
		documentMap = new ConcurrentHashMap<>();
		documentPageMap = new ConcurrentHashMap<>();
		recordedPages = new ArrayList<>();

		recParamProvider = new PresentationParameterProvider(context.getConfiguration());
		paramProvider = context.getPagePropertyProvider(ViewType.User);
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

		for (var pageState : documentPageMap.values()) {
			pageState.dispose();
		}

		documentMap.clear();
		documentPageMap.clear();
		recordedPages.clear();
	}

	@Override
	protected void destroyInternal() throws ExecutableException {
		// Nothing to do.
	}

	private void setHasChanges() {
		if (nonNull(hasChangesProperty)) {
			hasChangesProperty.set(true);
		}
	}

	private void insertPage(Page page) {
		Document pageDoc = page.getDocument();
		Document recDocument = documentMap.get(pageDoc);

		if (isNull(recDocument)) {
			// Try to merge with a document with the same name.
			recDocument = documentMap.values().stream()
					.filter(doc -> doc.getName().equals(pageDoc.getName()))
					.findFirst().orElse(null);

			if (isNull(recDocument)) {
				try {
					recDocument = new Document();
					recDocument.setAuthor(pageDoc.getAuthor());
					recDocument.setDocumentType(pageDoc.getType());
					recDocument.setTitle(pageDoc.getName());
					recDocument.setUid(pageDoc.getUid());

					documentMap.put(pageDoc, recDocument);

					if (documentMap.size() > 1) {
						setHasChanges();
					}
				}
				catch (IOException e) {
					LOG.error("Record document failed", e);
					return;
				}
			}
			else {
				if (pageDoc.isQuiz() && pageDoc.getPageIndex(page) == 0) {
					// Change uid to record pages from different document source.
					recDocument.setUid(pageDoc.getUid());
				}
			}
		}
		recDocument.setActualSplitSlideNotesPositon(pageDoc.getSplitSlideNotesPositon());

		try {
			PresentationParameter param = paramProvider.getParameter(page);

			Page recPage = recDocument.createPage(page, param.getPageRect());
			recPage.addShapes(page.getShapes());

			// Remember page presentation state.
			PresentationParameter recParam = recParamProvider.getParameter(recPage);
			recParam.setPageRect(param.getPageRect());

			PageState state = documentPageMap.get(pageDoc);

			if (nonNull(state)) {
				// Cancel observing the previous page.
				state.dispose();
			}

			recordedPages.add(recPage);

			// Register page to avoid successive recording and observe the state.
			documentPageMap.put(pageDoc, new PageState(page, recPage, param));
		}
		catch (IOException e) {
			LOG.error("Record page failed", e);
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



	private class PageState {

		final AtomicBoolean provision = new AtomicBoolean();

		final Page page;
		final Page srcPage;

		final PresentationParameter param;

		final PageListener pageListener;

		Rectangle2D pageRect;


		PageState(Page srcPage, Page dstPage, PresentationParameter param) {
			this.srcPage = srcPage;
			this.page = dstPage;
			this.param = param;
			this.pageListener = new PageListener(this);
			this.pageRect = param.getPageRect();

			srcPage.addPageEditedListener(pageListener);
		}

		void dispose() {
			srcPage.removePageEditedListener(pageListener);
		}

		boolean hasChanged() {
			return !pageRect.equals(param.getPageRect()) || isProvisionMode();
		}

		boolean isProvisionMode() {
			return provision.get();
		}

		void setProvisionMode() {
			provision.set(true);
		}
	}



	private class PageListener implements PageEditedListener {

		private final PageState state;


		PageListener(PageState state) {
			this.state = state;
		}

		@Override
		public void pageEdited(PageEditEvent event) {
			if (state.hasChanged()) {
				insertPage(state.srcPage);
				return;
			}

			switch (event.getType()) {
				case CLEAR:
					state.page.clear();
					break;
				case SHAPE_ADDED:
					state.page.addShape(event.getShape());
					break;
				case SHAPES_ADDED:
					state.page.clear();
					state.page.addShapes(event.getPage().getShapes());
					break;
				case SHAPE_REMOVED:
					state.page.removeShape(event.getShape());
					break;
			}

			setHasChanges();
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
					insertPage(page);
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

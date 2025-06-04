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

import com.google.common.eventbus.Subscribe;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Stack;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import org.lecturestudio.core.ExecutableException;
import org.lecturestudio.core.app.ApplicationContext;
import org.lecturestudio.core.bus.event.PageEvent;
import org.lecturestudio.core.controller.ToolController;
import org.lecturestudio.core.model.Document;
import org.lecturestudio.core.model.Page;
import org.lecturestudio.core.recording.action.PageAction;
import org.lecturestudio.core.recording.action.PlaybackAction;
import org.lecturestudio.core.recording.action.StaticShapeAction;
import org.lecturestudio.core.service.DocumentService;
import org.lecturestudio.core.view.PresentationParameter;
import org.lecturestudio.core.view.PresentationParameterProvider;
import org.lecturestudio.core.view.ViewType;

/**
 * Executes all recorded events at once resulting in a recorded document having
 * the final playback state.
 *
 * @author Alex Andres
 */
public class CreateDocumentExecutor {

	private final static Logger LOG = LogManager.getLogger(CreateDocumentExecutor.class);

	private final ApplicationContext context;

	private final Recording recording;

	private DocumentRecorder documentRecorder;


	/**
	 * Creates a new {@code DocumentEventExecutor} with the provided
	 * parameters.
	 *
	 * @param context   The application context.
	 * @param recording The recording from which to render the current state.
	 */
	public CreateDocumentExecutor(ApplicationContext context, Recording recording) {
		this.recording = recording;
		this.context = context;

		context.getEventBus().register(new Object() {

			@Subscribe
			public void onEvent(PageEvent event) {
				if (event.isSelected()) {
					try {
						documentRecorder.recordPage(event.getPage());
					}
					catch (ExecutableException e) {
						LOG.error("Record page failed", e);
					}
				}
			}
		});
	}

	/**
	 * Runs through all recorded pages and executes all events in a newly
	 * created {@code Document}. The created document will have exactly the same
	 * page count as the recorded document and can be retrieved by {@link
	 * #getDocumentRecorder()}.
	 *
	 * @throws Exception If any event could not be executed.
	 */
	public void executeEvents() throws Exception {
		Document document = createDocument();

		documentRecorder = new DocumentRecorder(context);
		documentRecorder.setPageRecordingTimeout(0);
		documentRecorder.start();

		DocumentService docService = new DocumentService(context);
		docService.addDocument(document);

		ToolController toolController = new ToolController(context, docService);

		var recordedEvents = recording.getRecordedEvents();
		var recordedPages = recordedEvents.getRecordedPages();

		documentRecorder.recordPage(document.getCurrentPage());

		for (RecordedPage recPage : recordedPages) {
			loadStaticShapes(toolController, document, recPage);

			Stack<PlaybackAction> actions = getPlaybackActions(recPage);

			while (!actions.isEmpty()) {
				PlaybackAction action = actions.pop();

				action.execute(toolController);
			}
		}
	}

	/**
	 * Runs through recorded pages that have the specified page numbers and
	 * executes all events in a newly created {@code Document}. The created
	 * document will have exactly the same page count as the recorded
	 * document and can be retrieved by {@link #getDocumentRecorder()}.
	 *
	 * @param pageNumbers The page numbers of the source document the recorded
	 *                    document must contain.
	 * @param ppProvider  The presentation parameter provider for the original
	 *                    document.
	 *
	 * @throws Exception If any event could not be executed.
	 */
	public void executeEvents(List<Integer> pageNumbers, PresentationParameterProvider ppProvider) throws Exception {
		Document srcDocument = createDocument();
		Document document = new Document();

		documentRecorder = new DocumentRecorder(context);
		documentRecorder.setPageRecordingTimeout(0);
		documentRecorder.start();

		for (int pageNum : pageNumbers) {
			Page srcPage = srcDocument.getPage(pageNum);
			Page newPage = document.createPage(srcPage);

			PresentationParameter srcParam = ppProvider.getParameter(srcPage);
			PresentationParameter newParam = context.getPagePropertyProvider(ViewType.User).getParameter(newPage);

			newParam.setPageRect(srcParam.getPageRect());

			if (srcParam.isZoomMode()) {
				newParam.zoom(srcParam.getPageRect());
			}
		}

		DocumentService docService = new DocumentService(context);
		docService.addDocument(document);

		ToolController toolController = new ToolController(context, docService);

		var recordedEvents = recording.getRecordedEvents();
		var recordedPages = recordedEvents.getRecordedPages();

		documentRecorder.recordPage(document.getCurrentPage());

		for (int pageNum : pageNumbers) {
			RecordedPage recPage = recordedPages.get(pageNum);
			loadStaticShapes(toolController, document, recPage);

			Stack<PlaybackAction> actions = getPlaybackActions(recPage);

			while (!actions.isEmpty()) {
				PlaybackAction action = actions.pop();

				action.execute(toolController);
			}
		}
	}

	/**
	 * Get the document recorder that contains recorded pages with annotations
	 * in the final playback state. To update the document {@link
	 * #executeEvents()} may be called again.
	 *
	 * @return The document recorder with pages in their final playback state.
	 */
	public DocumentRecorder getDocumentRecorder() {
		return documentRecorder;
	}

	private Document createDocument() throws IOException {
		ByteArrayOutputStream docStream = new ByteArrayOutputStream();

		Document recDoc = recording.getRecordedDocument().getDocument();
		recDoc.toOutputStream(docStream);

		return new Document(docStream.toByteArray());
	}

	private Stack<PlaybackAction> getPlaybackActions(RecordedPage recPage) {
		// Add page change event.
		PlaybackAction action = new PageAction(0, recPage.getNumber());
		action.setTimestamp(recPage.getTimestamp());

		Stack<PlaybackAction> playbacks = new Stack<>();
		playbacks.push(action);
		playbacks.addAll(recPage.getPlaybackActions());

		Collections.reverse(playbacks);

		return playbacks;
	}

	private void loadStaticShapes(ToolController toolController,
			Document document, RecordedPage recPage) throws Exception {
		Page page = document.getPage(recPage.getNumber());

		if (isNull(page)) {
			return;
		}

		Iterator<StaticShapeAction> iter = recPage.getStaticActions().iterator();

		if (iter.hasNext()) {
			// Remember currently selected page.
			int lastPageNumber = document.getCurrentPageNumber();

			// Select the page to which to add static actions.
			document.selectPage(recPage.getNumber());

			while (iter.hasNext()) {
				StaticShapeAction staticAction = iter.next();
				PlaybackAction action = staticAction.getAction();

				// Execute static action on selected page.
				action.execute(toolController);
			}

			// Go back to the page which was selected prior preloading.
			document.selectPage(lastPageNumber);

			page.sendChangeEvent();
		}
	}
}

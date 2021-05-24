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

package org.lecturestudio.editor.api.presenter;

import static java.util.Objects.nonNull;

import com.google.common.eventbus.Subscribe;

import java.util.HashMap;
import java.util.Map;

import javax.inject.Inject;

import org.lecturestudio.core.app.ApplicationContext;
import org.lecturestudio.core.bus.EventBus;
import org.lecturestudio.core.bus.event.DocumentEvent;
import org.lecturestudio.core.bus.event.PageEvent;
import org.lecturestudio.core.controller.RenderController;
import org.lecturestudio.core.input.KeyEvent;
import org.lecturestudio.core.model.Document;
import org.lecturestudio.core.model.Page;
import org.lecturestudio.core.model.listener.ParameterChangeListener;
import org.lecturestudio.core.presenter.Presenter;
import org.lecturestudio.core.recording.DocumentEventExecutor;
import org.lecturestudio.core.recording.Recording;
import org.lecturestudio.core.view.Action;
import org.lecturestudio.core.view.PresentationParameter;
import org.lecturestudio.core.view.PresentationParameterProvider;
import org.lecturestudio.core.view.ViewType;
import org.lecturestudio.editor.api.context.EditorContext;
import org.lecturestudio.editor.api.input.Shortcut;
import org.lecturestudio.editor.api.service.RecordingFileService;
import org.lecturestudio.editor.api.service.RecordingPlaybackService;
import org.lecturestudio.editor.api.view.SlidesView;
import org.lecturestudio.media.event.MediaPlayerProgressEvent;

public class SlidesPresenter extends Presenter<SlidesView> {

	private final EventBus eventBus;

	private final Map<KeyEvent, Action> shortcutMap;

	private final Map<Document, DocumentEventExecutor> docExecMap;

	private final RenderController renderController;

	private final RecordingFileService recordingService;

	private final RecordingPlaybackService playbackService;


	@Inject
	SlidesPresenter(ApplicationContext context, SlidesView view,
			RenderController renderController,
			RecordingFileService recordingService,
			RecordingPlaybackService playbackService) {
		super(context, view);

		this.renderController = renderController;
		this.recordingService = recordingService;
		this.playbackService = playbackService;
		this.eventBus = context.getEventBus();
		this.shortcutMap = new HashMap<>();
		this.docExecMap = new HashMap<>();
	}

	@Override
	public void initialize() {
		eventBus.register(this);

		view.setOnKeyEvent(this::keyEvent);
		view.setOnDeletePage(this::deletePage);
		view.setOnSelectPage(this::selectPage);
		view.setPageRenderer(renderController);

		// Register for page parameter change updates.
		PresentationParameterProvider ppProvider = context.getPagePropertyPropvider(ViewType.User);
		ppProvider.addParameterChangeListener(new ParameterChangeListener() {

			@Override
			public Page forPage() {
				return view.getPage();
			}

			@Override
			public void parameterChanged(Page page, PresentationParameter parameter) {
				view.setPage(page, parameter);
			}
		});

		// Register shortcuts that are associated with the SlideView.
		registerShortcut(Shortcut.SLIDE_NEXT_DOWN, this::nextPage);
		registerShortcut(Shortcut.SLIDE_NEXT_PAGE_DOWN, this::nextPage);
		registerShortcut(Shortcut.SLIDE_NEXT_RIGHT, this::nextPage);
		registerShortcut(Shortcut.SLIDE_NEXT_SPACE, this::nextPage);

		registerShortcut(Shortcut.SLIDE_PREVIOUS_LEFT, this::previousPage);
		registerShortcut(Shortcut.SLIDE_PREVIOUS_PAGE_UP, this::previousPage);
		registerShortcut(Shortcut.SLIDE_PREVIOUS_UP, this::previousPage);
	}

	@Override
	public void destroy() {
		eventBus.unregister(this);
	}

	@Subscribe
	public void onEvent(DocumentEvent event) {
		Document doc = event.getDocument();

		switch (event.getType()) {
			case CREATED:
				documentCreated(doc);
				break;
			case CLOSED:
				documentClosed(doc);
				break;
			case SELECTED:
				documentSelected(doc);
				break;
			case REPLACED:
				documentReplaced(event.getOldDocument(), doc);
				break;
		}
	}

	@Subscribe
	public void onEvent(PageEvent event) {
		if (event.isSelected()) {
			setPage(event.getPage());
		}
	}

	@Subscribe
	public void onEvent(final MediaPlayerProgressEvent event) {
		EditorContext editorContext = (EditorContext) context;
		double progress = 1.0 * event.getCurrentTime().getMillis() / event.getTotalTime().getMillis();

		editorContext.setPrimarySelection(progress);

		if (event.getPrevEventNumber() != event.getEventNumber()) {
			view.repaint();
		}
	}

	private void nextPage() {
		try {
			playbackService.selectNextPage();
		}
		catch (Exception e) {
			handleException(e, "Select page failed", "select.recording.page.error");
		}
	}

	private void previousPage() {
		try {
			playbackService.selectPreviousPage();
		}
		catch (Exception e) {
			handleException(e, "Select page failed", "select.recording.page.error");
		}
	}

	private void deletePage(Page page) {
		EditorContext editorContext = (EditorContext) context;
		double timeNorm = editorContext.getPrimarySelection();

		recordingService.deletePage(timeNorm)
				.exceptionally(throwable -> {
					handleException(throwable, "Delete page failed", "delete.page.error");
					return null;
				});
	}

	private void selectPage(Page page) {
		try {
			Document doc = recordingService.getSelectedRecording()
					.getRecordedDocument().getDocument();

			playbackService.selectPage(doc.getPage(page.getPageNumber()));
		}
		catch (Exception e) {
			handleException(e, "Select page failed", "select.recording.page.error");
		}
	}

	private void registerShortcut(Shortcut shortcut, Action action) {
		shortcutMap.put(shortcut.getKeyEvent(), action);
	}

	private void keyEvent(KeyEvent event) {
		Action action = shortcutMap.get(event);

		// Shortcuts have higher priority. If no shortcut mapping is found,
		// the key-event will be distributed.
		if (nonNull(action)) {
			action.execute();
		}
	}

	private void documentCreated(Document doc) {
		// Create an execution context with own event buses to not interfere
		// with the main application context.
		ApplicationContext execContext = new ApplicationContext(null,
				context.getConfiguration(), context.getDictionary(),
				new EventBus(), new EventBus()) {

			@Override
			public void saveConfiguration() {
			}
		};

		Recording recording = recordingService.getSelectedRecording();
		DocumentEventExecutor docEventExecutor = new DocumentEventExecutor(
				execContext, recording);

		try {
			docEventExecutor.executeEvents();
		}
		catch (Exception e) {
			logException(e, "Execute recorded events failed");
		}

		setProxyDocument(doc, docEventExecutor);

		view.addDocument(getProxyDocument(doc), execContext);

		setPage(doc.getCurrentPage());
	}

	private void documentClosed(Document doc) {
		view.removeDocument(getProxyDocument(doc));

		removeProxyDocument(doc);
	}

	private void documentSelected(Document doc) {
		view.selectDocument(getProxyDocument(doc));

		setPage(doc.getCurrentPage());
	}

	private void documentReplaced(Document oldDoc, Document doc) {
		try {
			DocumentEventExecutor docEventExecutor = docExecMap.remove(oldDoc);
			docEventExecutor.executeEvents();

			setProxyDocument(doc, docEventExecutor);

			documentSelected(doc);
		}
		catch (Exception e) {
			logException(e, "Execute recorded events failed");
		}
	}

	private void setPage(Page page) {
		PresentationParameterProvider ppProvider = context.getPagePropertyPropvider(ViewType.User);
		PresentationParameter parameter = ppProvider.getParameter(page);

		view.setPage(page, parameter);
	}

	private Document getProxyDocument(Document document) {
		return docExecMap.get(document).getDocument();
	}

	private void setProxyDocument(Document document, DocumentEventExecutor docEventExecutor) {
		docExecMap.put(document, docEventExecutor);
	}

	private void removeProxyDocument(Document document) {
		docExecMap.remove(document);
	}
}

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

import com.google.common.eventbus.Subscribe;
import org.lecturestudio.core.app.ApplicationContext;
import org.lecturestudio.core.bus.EventBus;
import org.lecturestudio.core.bus.event.DocumentEvent;
import org.lecturestudio.core.bus.event.PageEvent;
import org.lecturestudio.core.controller.RenderController;
import org.lecturestudio.core.input.KeyEvent;
import org.lecturestudio.core.model.Document;
import org.lecturestudio.core.model.Page;
import org.lecturestudio.core.model.listener.DocumentChangeListener;
import org.lecturestudio.core.model.listener.ParameterChangeListener;
import org.lecturestudio.core.presenter.Presenter;
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
import org.lecturestudio.media.event.ScreenCaptureFrameEvent;
import org.lecturestudio.media.event.ScreenCaptureSequenceEndEvent;

import javax.inject.Inject;
import java.util.HashMap;
import java.util.Map;

import static java.util.Objects.nonNull;

public class SlidesPresenter extends Presenter<SlidesView> {

	private final EventBus eventBus;

	private final Map<KeyEvent, Action> shortcutMap;

	private final DocumentChangeListener documentChangeListener;

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
		this.documentChangeListener = new DocumentChangeHandler();
	}

	@Override
	public void initialize() {
		eventBus.register(this);

		view.setOnKeyEvent(this::keyEvent);
		view.setOnDeletePage(this::deletePage);
		view.setOnSelectPage(this::selectPage);
		view.setPageRenderer(renderController);

		// Register for page parameter change updates.
		PresentationParameterProvider ppProvider = context.getPagePropertyProvider(ViewType.User);
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
			case REPLACED:
				documentSelected(event.getOldDocument(), doc);
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
	}

	@Subscribe
	public void onEvent(final ScreenCaptureFrameEvent event) {
		view.setScreenCaptureFrame(event.getFrame());
	}

	@Subscribe
	public void onEvent(final ScreenCaptureSequenceEndEvent event) {
		view.setScreenCaptureFrame(null);
		System.out.println("End Sequence");
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
		recordingService.deletePage(page.getPageNumber())
				.exceptionally(throwable -> {
					handleException(throwable, "Delete page failed", "delete.page.error");
					return null;
				});
	}

	private void selectPage(Page page) {
		try {
			playbackService.selectPage(page);
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
		PresentationParameterProvider ppProvider = context.getPagePropertyProvider(ViewType.Preview);

		view.addDocument(doc, ppProvider);

		setPage(doc.getCurrentPage());
	}

	private void documentClosed(Document doc) {
		view.removeDocument(doc);
	}

	private void documentSelected(Document oldDoc, Document doc) {
		if (nonNull(oldDoc)) {
			oldDoc.removeChangeListener(documentChangeListener);
		}

		doc.addChangeListener(documentChangeListener);

		view.selectDocument(doc);

		setPage(doc.getCurrentPage());
	}

	private void setPage(Page page) {
		PresentationParameterProvider ppProvider = context.getPagePropertyProvider(ViewType.User);
		PresentationParameter parameter = ppProvider.getParameter(page);

		view.setPage(page, parameter);
	}



	private class DocumentChangeHandler implements DocumentChangeListener {

		@Override
		public void documentChanged(Document document) {
			setPage(document.getCurrentPage());
		}

		@Override
		public void pageAdded(Page page) {

		}

		@Override
		public void pageRemoved(Page page) {

		}
	}
}

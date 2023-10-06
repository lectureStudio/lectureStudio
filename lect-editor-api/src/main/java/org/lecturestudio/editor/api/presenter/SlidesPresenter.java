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

import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;

import com.google.common.eventbus.Subscribe;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutionException;

import javax.inject.Inject;

import org.lecturestudio.core.app.ApplicationContext;
import org.lecturestudio.core.audio.bus.event.TextColorEvent;
import org.lecturestudio.core.audio.bus.event.TextFontEvent;
import org.lecturestudio.core.bus.EventBus;
import org.lecturestudio.core.bus.event.DocumentEvent;
import org.lecturestudio.core.bus.event.PageEvent;
import org.lecturestudio.core.bus.event.ToolSelectionEvent;
import org.lecturestudio.core.controller.RenderController;
import org.lecturestudio.core.geometry.Matrix;
import org.lecturestudio.core.input.KeyEvent;
import org.lecturestudio.core.model.Document;
import org.lecturestudio.core.model.Page;
import org.lecturestudio.core.model.listener.PageEditedListener;
import org.lecturestudio.core.model.listener.ParameterChangeListener;
import org.lecturestudio.core.model.shape.Shape;
import org.lecturestudio.core.model.shape.TextShape;
import org.lecturestudio.core.presenter.Presenter;
import org.lecturestudio.core.recording.DocumentEventExecutor;
import org.lecturestudio.core.recording.Recording;
import org.lecturestudio.core.stylus.StylusHandler;
import org.lecturestudio.core.text.Font;
import org.lecturestudio.core.tool.ToolType;
import org.lecturestudio.core.view.Action;
import org.lecturestudio.core.view.PageObjectRegistry;
import org.lecturestudio.core.view.PageObjectView;
import org.lecturestudio.core.view.PresentationParameter;
import org.lecturestudio.core.view.PresentationParameterProvider;
import org.lecturestudio.core.view.TextBoxView;
import org.lecturestudio.core.view.ViewContextFactory;
import org.lecturestudio.core.view.ViewType;
import org.lecturestudio.editor.api.context.EditorContext;
import org.lecturestudio.editor.api.controller.EditorToolController;
import org.lecturestudio.editor.api.input.Shortcut;
import org.lecturestudio.editor.api.service.RecordingFileService;
import org.lecturestudio.editor.api.service.RecordingPlaybackService;
import org.lecturestudio.editor.api.stylus.EditorStylusHandler;
import org.lecturestudio.editor.api.view.SlidesView;
import org.lecturestudio.media.event.MediaPlayerProgressEvent;

public class SlidesPresenter extends Presenter<SlidesView> {

	private final EventBus eventBus;

	private final Map<KeyEvent, Action> shortcutMap;

	private final Map<Document, DocumentEventExecutor> docExecMap;

	private final ViewContextFactory viewFactory;
	private final RenderController renderController;
	private final EditorToolController editorToolController;

	private ToolType toolType;

	private TextBoxView lastFocusedTextBox;

	private final RecordingFileService recordingService;

	private final RecordingPlaybackService playbackService;
	private final PageObjectRegistry pageObjectRegistry;
	private PageEditedListener pageEditedListener;
	private StylusHandler stylusHandler;


	@Inject
	SlidesPresenter(ApplicationContext context, SlidesView view,
	                ViewContextFactory viewFactory,
	                RenderController renderController,
	                EditorToolController toolController,
	                RecordingFileService recordingService,
	                RecordingPlaybackService playbackService) {
		super(context, view);

		this.viewFactory = viewFactory;
		this.renderController = renderController;
		this.editorToolController = toolController;
		this.recordingService = recordingService;
		this.playbackService = playbackService;
		this.eventBus = context.getEventBus();
		this.shortcutMap = new HashMap<>();
		this.docExecMap = new ConcurrentHashMap<>();
		this.pageObjectRegistry = new PageObjectRegistry();
	}

	@Override
	public void initialize() {
		eventBus.register(this);

		stylusHandler = new EditorStylusHandler(editorToolController, () -> {
		});

		view.setOnKeyEvent(this::keyEvent);
		view.setOnDeletePage(this::deletePage);
		view.setOnSelectPage(this::selectPage);
		view.setOnSelectDocument(this::selectDocument);
		view.setPageRenderer(renderController);
		view.setStylusHandler(stylusHandler);
		view.setOnViewTransform(this::setViewTransform);
		view.bindSeekProperty(((EditorContext) context).seekingProperty());

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

		registerShortcut(Shortcut.SLIDE_PREVIOUS_PAGE_UP, this::previousPage);
		registerShortcut(Shortcut.SLIDE_PREVIOUS_UP, this::previousPage);

		registerShortcut(Shortcut.PLAYBACK_BEGINNING_POS_1, this::firstPage);
		registerShortcut(Shortcut.PLAYBACK_END_END, this::lastPage);
		registerShortcut(Shortcut.PLAYBACK_PAUSE_PLAY_SPACE, this::resumeOrPause);

		registerShortcut(Shortcut.SLIDE_MOVE_LEFT, this::moveLeft);
		registerShortcut(Shortcut.SLIDE_MOVE_RIGHT, this::moveRight);

		pageEditedListener = (event) -> {
			switch (event.getType()) {
				case CLEAR -> setPage(event.getPage());
			}
		};

		editorToolController.addPageShapeAddedListener(this::pageShapeAdded);
		pageObjectRegistry.register(ToolType.TEXT, TextBoxView.class);
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

	/**
	 * Resumes or Pauses the playback, whichever state was selected before
	 */
	private void resumeOrPause() {
		try {
			if (!playbackService.started()) {
				playbackService.start();
			}
			else if (!playbackService.suspended()) {
				playbackService.suspend();
			}
		}
		catch (Exception e) {
			handleException(e, "Resume playback failed", "resume.playback.error");
		}
	}

	/**
	 * Moves the playback to the left, the amount is determined by the current zoom level
	 */
	private void moveLeft() {
		double zoomLevel = ((EditorContext) context).getTrackZoomLevel();
		int amountMs = (int) ((1 / zoomLevel) * 5000);
		movePlayback(-amountMs);
	}

	/**
	 * Moves the playback to the left, the amount is determined by the current zoom level
	 */
	private void moveRight() {
		double zoomLevel = ((EditorContext) context).getTrackZoomLevel();
		int amountMs = (int) ((1 / zoomLevel) * 5000);
		movePlayback(amountMs);
	}

	/**
	 * Move the Playback by the selected amount in milliseconds
	 * @param amount the selected amount in milliseconds
	 */
	private void movePlayback(int amount) {
		try {
			int currentTimeMs = Math.toIntExact(playbackService.getElapsedTime());
			playbackService.seek(currentTimeMs + amount);
		}
		catch (Exception e) {
			handleException(e, "Resume playback failed", "resume.playback.error");
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

	private void firstPage() {
		try {
			playbackService.selectPage(0);
		}
		catch (Exception e) {
			handleException(e, "Select page failed", "select.recording.page.error");
		}
	}

	private void lastPage() {
		try {
			playbackService.selectPage(recordingService.getSelectedRecording().getRecordedDocument().getDocument().getPageCount() - 1);
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
				}).join();
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

	private void selectDocument(Document document) {
		try {
			Recording recording = recordingService.getRecordingWithDocument(document);

			recordingService.selectRecording(recording);
		}
		catch (Exception e) {
			handleException(e, "Select document failed", "select.recording.page.error");
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
		else {
			editorToolController.setKeyEvent(event);
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

			view.selectDocument(getProxyDocument(doc));

			setPage(doc.getCurrentPage());
		}
		catch (Exception e) {
			logException(e, "Execute recorded events failed");
		}
	}

	private void setPage(Page page) {
		PresentationParameterProvider ppProvider = context.getPagePropertyProvider(ViewType.User);
		PresentationParameter parameter = ppProvider.getParameter(page);

		stylusHandler.setPresentationParameter(parameter);

		if (nonNull(view.getPage())) {
			view.getPage().removePageEditedListener(pageEditedListener);
		}

		if (nonNull(page)) {
			page.addPageEditedListener(pageEditedListener);
		}

		view.setPage(page, parameter);
	}

	private synchronized Document getProxyDocument(Document document) {
		return docExecMap.get(document).getDocument();
	}

	private synchronized void setProxyDocument(Document document, DocumentEventExecutor docEventExecutor) {
		docExecMap.put(document, docEventExecutor);
	}

	private synchronized void removeProxyDocument(Document document) {
		docExecMap.remove(document);
	}

	private void pageShapeAdded(Shape shape) {
		Class<? extends Shape> shapeClass = pageObjectRegistry.getShapeClass(toolType);

		if (isNull(shapeClass) || !shapeClass.isAssignableFrom(shape.getClass())) {
			return;
		}

		Class<? extends PageObjectView<? extends Shape>> viewClass =
				pageObjectRegistry.getPageObjectViewClass(toolType);

		try {
			PageObjectView<?> objectView = createPageObjectView(shape, viewClass);
			objectView.setFocus(true);
		}
		catch (Exception e) {
			logException(e, "Create PageObjectView failed");
		}
	}

	private PageObjectView<? extends Shape> createPageObjectView(Shape shape,
	                                                             Class<? extends PageObjectView<? extends Shape>> viewClass) {
		PageObjectView<Shape> objectView = (PageObjectView<Shape>) viewFactory.getInstance(viewClass);
		objectView.setPageShape(shape);
		objectView.setOnClose(() -> {
			pageObjectViewClosed(objectView);
		});
		objectView.setOnFocus((focused) -> {
			if (Boolean.TRUE.equals(focused)) {
				pageObjectViewFocused(objectView);
			}
			else {
				pageObjectViewFocusRemoved(objectView);
			}
		});

		view.addPageObjectView(objectView);

		return objectView;
	}

	private void pageObjectViewClosed(PageObjectView<? extends Shape> objectView) {
		view.removePageObjectView(objectView);

		// Remove associated shape from the page.
		Shape shape = objectView.getPageShape();

		Page page = view.getPage();
		page.removeShape(shape);

		if (shape instanceof TextShape textShape) {
			// TODO: make this generic or remove at all
			objectView.setOnFocus((ignored -> {
			}));
			editorToolController.resetRecordedPlaybackActions();
			textShape.setOnRemove();
		}
	}

	private void pageObjectViewFocused(PageObjectView<? extends Shape> objectView) {
		Class<? extends Shape> shapeClass = pageObjectRegistry.getShapeClass(ToolType.TEXT);

		if (nonNull(shapeClass) && shapeClass.isAssignableFrom(objectView.getPageShape().getClass())) {
			lastFocusedTextBox = (TextBoxView) objectView;
		}
	}

	private void pageObjectViewFocusRemoved(PageObjectView<? extends Shape> objectView) {
		try {
			if (!(objectView.getPageShape() instanceof TextShape) ||
					!((TextShape) objectView.getPageShape()).getText().isEmpty()) {
				editorToolController.persistPlaybackActions().get();
			}
			else {
				// If content of the page-object is empty don't save and remove the unnecessary playback actions
				editorToolController.resetRecordedPlaybackActions();
			}
		}
		catch (InterruptedException | ExecutionException e) {
			// ignored
		}
		objectView.setOnFocus((ignored) -> {
		});
		view.removePageObjectView(objectView);
		lastFocusedTextBox = null;
	}

	@Subscribe
	public void onEvent(final ToolSelectionEvent event) {
		toolChanged(event.getToolType());
	}

	@Subscribe
	public void onEvent(TextColorEvent event) {
		if (nonNull(lastFocusedTextBox)) {
			lastFocusedTextBox.setTextColor(event.getColor());
		}
	}

	@Subscribe
	public void onEvent(TextFontEvent event) {
		if (nonNull(lastFocusedTextBox)) {
			// Scale font size to page metrics.
			Font textFont = event.getFont().clone();
			textFont.setSize(textFont.getSize() / editorToolController.getViewTransform().getScaleX());

			lastFocusedTextBox.setTextFont(textFont);
		}
	}

	private void setViewTransform(Matrix matrix) {
		editorToolController.setViewTransform(matrix.clone());
	}

	private void toolChanged(ToolType toolType) {
		this.toolType = toolType;

		view.removeAllPageObjectViews();
	}
}

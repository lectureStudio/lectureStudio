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

package org.lecturestudio.presenter.api.presenter;

import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;

import com.google.common.eventbus.Subscribe;

import java.io.File;
import java.io.IOException;
import java.text.MessageFormat;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;

import javax.inject.Inject;

import org.lecturestudio.core.ExecutableException;
import org.lecturestudio.core.ExecutableState;
import org.lecturestudio.core.app.ApplicationContext;
import org.lecturestudio.core.app.dictionary.Dictionary;
import org.lecturestudio.core.audio.AudioDeviceNotConnectedException;
import org.lecturestudio.core.audio.bus.event.TextFontEvent;
import org.lecturestudio.core.bus.EventBus;
import org.lecturestudio.core.bus.event.*;
import org.lecturestudio.core.controller.PresentationController;
import org.lecturestudio.core.controller.ToolController;
import org.lecturestudio.core.graphics.Color;
import org.lecturestudio.core.model.Document;
import org.lecturestudio.core.model.DocumentList;
import org.lecturestudio.core.model.DocumentType;
import org.lecturestudio.core.model.Page;
import org.lecturestudio.core.model.TemplateDocument;
import org.lecturestudio.core.model.listener.PageEditEvent;
import org.lecturestudio.core.model.listener.PageEditedListener;
import org.lecturestudio.core.model.listener.ParameterChangeListener;
import org.lecturestudio.core.presenter.Presenter;
import org.lecturestudio.core.presenter.command.ShowPresenterCommand;
import org.lecturestudio.core.service.DocumentService;
import org.lecturestudio.core.text.Font;
import org.lecturestudio.core.text.TeXFont;
import org.lecturestudio.core.tool.ColorPalette;
import org.lecturestudio.core.tool.PaintSettings;
import org.lecturestudio.core.tool.ToolType;
import org.lecturestudio.core.view.NotificationType;
import org.lecturestudio.core.view.PresentationParameter;
import org.lecturestudio.core.view.PresentationParameterProvider;
import org.lecturestudio.core.view.ViewType;
import org.lecturestudio.presenter.api.config.PresenterConfiguration;
import org.lecturestudio.presenter.api.context.PresenterContext;
import org.lecturestudio.presenter.api.event.RecordingStateEvent;
import org.lecturestudio.presenter.api.event.ScreenShareSelectEvent;
import org.lecturestudio.presenter.api.event.StreamingStateEvent;
import org.lecturestudio.presenter.api.model.*;
import org.lecturestudio.presenter.api.presenter.command.StartRecordingCommand;
import org.lecturestudio.presenter.api.presenter.state.ActivateDisplaysNotifyState;
import org.lecturestudio.presenter.api.service.BookmarkService;
import org.lecturestudio.presenter.api.service.RecordingService;
import org.lecturestudio.presenter.api.view.ToolbarView;

public class ToolbarPresenter extends Presenter<ToolbarView> {

	private final PageEditedListener pageEditedListener = this::pageEdited;

	private final EventBus eventBus;

	private final ActivateDisplaysNotifyState activateDisplaysNotifyState;

	private ToolType toolType;

	@Inject
	private ToolController toolController;

	@Inject
	private BookmarkService bookmarkService;

	@Inject
	private PresentationController presentationController;

	@Inject
	private DocumentService documentService;

	@Inject
	private RecordingService recordingService;


	@Inject
	public ToolbarPresenter(ApplicationContext context, ToolbarView view) {
		super(context, view);

		eventBus = context.getEventBus();
		activateDisplaysNotifyState = new ActivateDisplaysNotifyState(context, view);
	}

	@Subscribe
	public void onEvent(final DocumentEvent event) {
		if (event.closed()) {
			return;
		}

		Document doc = event.getDocument();
		Page page = nonNull(doc) ? doc.getCurrentPage() : null;

		if (event.selected() && nonNull(page)) {
			page.addPageEditedListener(pageEditedListener);
		}

		view.setDocument(doc);

		pageChanged(page);
	}

	@Subscribe
	public void onEvent(final PageEvent event) {
		final Page page = event.getPage();

		if (event.isRemoved()) {
			page.removePageEditedListener(pageEditedListener);
		}
		else if (event.isSelected()) {
			Page oldPage = event.getOldPage();

			if (nonNull(oldPage)) {
				oldPage.removePageEditedListener(pageEditedListener);
			}

			page.addPageEditedListener(pageEditedListener);

			boolean hasBookmark = false;
			for(Bookmark bookmark : bookmarkService.getBookmarks().getAllBookmarks()){
				if(bookmark.getPage().equals(page)){
					hasBookmark = true;
					break;
				}
			}
			view.selectNewBookmarkButton(hasBookmark);
			pageChanged(page);

		}
	}


	@Subscribe
	public void onEvent(final BookmarkEvent event) {
		if (event.getPage().equals(event.getPage().getDocument().getCurrentPage())) {
			switch (event.getType()) {
				case CREATED -> view.selectNewBookmarkButton(true);
				case REMOVED -> view.selectNewBookmarkButton(false);
				default -> view.selectNewBookmarkButton(false);
			}
		}
	}

	@Subscribe
	public void onEvent(final RecordingStateEvent event) {
		view.setRecordingState(event.getState());
	}

	@Subscribe
	public void onEvent(final StreamingStateEvent event) {
		view.setStreamingState(event.getState());
	}

	@Subscribe
	public void onEvent(final ToolSelectionEvent event) {
		toolChanged(event.getToolType(), event.getPaintSettings());
	}

	@Subscribe
	public void onEvent(final CustomizeToolbarEvent event) {
		view.openCustomizeToolbarDialog();
	}

	public void undo() {
		toolController.undo();
	}

	public void redo() {
		toolController.redo();
	}

	public void customPaletteColor(Color color) {
		ColorPalette.setColor(toolType, color, 0);

		toolController.selectPaintColor(color);
	}

	public void customColor() {
		toolController.selectPaintColor(ColorPalette.getColor(toolType, 0));
	}

	public void color1() {
		toolController.selectPaintColor(ColorPalette.getColor(toolType, 1));
	}

	public void color2() {
		toolController.selectPaintColor(ColorPalette.getColor(toolType, 2));
	}

	public void color3() {
		toolController.selectPaintColor(ColorPalette.getColor(toolType, 3));
	}

	public void color4() {
		toolController.selectPaintColor(ColorPalette.getColor(toolType, 4));
	}

	public void color5() {
		toolController.selectPaintColor(ColorPalette.getColor(toolType, 5));
	}

	public void color6() {
		toolController.selectPaintColor(ColorPalette.getColor(toolType, 6));
	}

	public void penTool() {
		toolController.selectPenTool();
	}

	public void highlighterTool() {
		toolController.selectHighlighterTool();
	}

	public void pointerTool() {
		toolController.selectPointerTool();
	}

	public void textSelectTool() {
		toolController.selectTextSelectionTool();
	}

	public void lineTool() {
		toolController.selectLineTool();
	}

	public void arrowTool() {
		toolController.selectArrowTool();
	}

	public void rectangleTool() {
		toolController.selectRectangleTool();
	}

	public void ellipseTool() {
		toolController.selectEllipseTool();
	}

	public void selectTool() {
		toolController.selectSelectTool();
	}

	public void eraseTool() {
		toolController.selectRubberTool();
	}

	public void textTool() {
		toolController.selectTextTool();
	}

	public void setTextBoxFont(Font font) {
		toolController.setTextFont(font);

		eventBus.post(new TextFontEvent(font));
	}

	public void texTool() {
		toolController.selectLatexTool();
	}

	public void setTeXBoxFont(TeXFont font) {
		toolController.setTeXFont(font);
	}

	public void clearTool() {
		toolController.selectDeleteAllTool();
	}

	public void showGrid() {
		toolController.toggleGrid();
	}

	public void extend() {
		toolController.selectExtendViewTool();
	}

	public void openWhiteboard() {
		DocumentList documents = documentService.getDocuments();
		Document selectedDocument = documents.getSelectedDocument();

		if (isNull(selectedDocument) || !selectedDocument.isWhiteboard()) {
			PresenterConfiguration config = (PresenterConfiguration) context.getConfiguration();
			String template = config.getTemplateConfig()
					.getWhiteboardTemplateConfig().getTemplatePath();

			documentService.openWhiteboard(template);
		}
		else {
			documentService.selectDocument(documents.getLastNonWhiteboard());
		}
	}

	public void enableDisplays(boolean enable) {
		presentationController.showPresentationViews(enable);

		view.setPresentationViewsVisible(presentationController.getPresentationViewsVisible());
	}

	public void zoomInTool() {
		toolController.selectZoomTool();
	}

	public void zoomOutTool() {
		toolController.selectZoomOutTool();
	}

	public void panTool() {
		try {
			toolController.selectPanningTool();
		}
		catch (Exception e) {
			handleException(e, "Select pan tool failed", e.getMessage());
		}
	}

	public void startRecording() {
		try {
			if (recordingService.started()) {
				recordingService.suspend();
			}
			else if (recordingService.suspended()) {
				recordingService.start();
			}
			else {
				eventBus.post(new StartRecordingCommand(() -> {
					PresenterContext pContext = (PresenterContext) context;

					CompletableFuture.runAsync(() -> {
						try {
							recordingService.start();
						}
						catch (ExecutableException e) {
							throw new CompletionException(e);
						}

						pContext.setRecordingStarted(true);
					})
					.exceptionally(e -> {
						handleRecordingStateError(e);
						pContext.setRecordingStarted(false);
						return null;
					});
				}));
			}
		}
		catch (ExecutableException e) {
			handleRecordingStateError(e);
		}
	}

	public void stopRecording() {
		PresenterConfiguration config = (PresenterConfiguration) context.getConfiguration();

		if (config.getConfirmStopRecording()) {
			eventBus.post(new ShowPresenterCommand<>(ConfirmStopRecordingPresenter.class));
		}
		else {
			try {
				recordingService.stop();

				eventBus.post(new ShowPresenterCommand<>(SaveRecordingPresenter.class));
			}
			catch (ExecutableException e) {
				handleException(e, "Stop recording failed", "recording.stop.error");
			}
		}
	}

	private void selectScreenSource() {
		eventBus.post(new ScreenShareSelectEvent());
	}

	private void selectQuiz() {
		eventBus.post(new ShowPresenterCommand<>(SelectQuizPresenter.class));
	}

	private void showAudienceMessageTemplate() {
		PresenterConfiguration config = (PresenterConfiguration) context.getConfiguration();
		Dictionary dict = context.getDictionary();
		String template = config.getTemplateConfig()
				.getHallMessageTemplateConfig().getTemplatePath();
		File templateFile = new File(nonNull(template) ? template : "");

		Document document = null;

		try {
			if (templateFile.exists()) {
				document = new TemplateDocument(templateFile);
			}
			else {
				document = new Document();
			}
		}
		catch (IOException e) {
			handleException(e, "Create whiteboard failed", "error",
					"generic.error");
		}

		if (nonNull(document)) {
			document.setTitle(dict.get("slides.audience.message"));
			document.setDocumentType(DocumentType.MESSAGE);
			document.createPage();

			Document prevDocument = null;

			for (Document doc : documentService.getDocuments().asList()) {
				if (doc.hashCode() == document.hashCode()) {
					prevDocument = doc;
					break;
				}
			}

			if (nonNull(prevDocument)) {
				documentService.replaceDocument(prevDocument, document);
			}
			else {
				documentService.addDocument(document);
			}

			documentService.selectDocument(document);
		}
	}

	private void pageParameterChanged(Page page, PresentationParameter parameter) {
		view.setPage(page, parameter);
	}

	private void pageChanged(Page page) {
		PresentationParameter parameter = null;

		if (nonNull(page)) {
			PresentationParameterProvider ppProvider = context.getPagePropertyProvider(ViewType.User);
			parameter = ppProvider.getParameter(page);
		}

		view.setPage(page, parameter);

		activateDisplaysNotifyState.setPage(page);
	}

	private void pageEdited(final PageEditEvent event) {
		if (event.shapedChanged()) {
			return;
		}

		pageChanged(event.getPage());

		activateDisplaysNotifyState.setShape();
	}

	private void toolChanged(ToolType toolType, PaintSettings settings) {
		this.toolType = toolType;

		// Update the selected tool button.
		view.selectToolButton(toolType);

		if (!ColorPalette.hasPalette(toolType)) {
			return;
		}

		// Update color palette for the selected tool.
		view.selectColorButton(toolType, settings);
	}

	private void handleRecordingStateError(Throwable e) {
		Throwable cause = nonNull(e.getCause()) ? e.getCause().getCause() : null;

		if (cause instanceof AudioDeviceNotConnectedException ex) {
			context.showError("recording.start.error", "recording.start.device.error", ex.getDeviceName());
			logException(e, "Start recording failed");
		}
		else {
			handleException(e, "Start recording failed", "recording.start.error");
		}
	}

	/**
	 * Select the previous bookmark in the bookmark list.
	 */
	public void selectPreviousBookmark() {
		Page page = bookmarkService.getPrevBookmarkPage();
		if (nonNull(page)) {
			documentService.selectPage(page);
		}
	}

	/**
	 * Select the next bookmark in the bookmark list.
	 */
	public void selectNextBookmark() {
		Page page = bookmarkService.getNextBookmarkPage();
		if (nonNull(page)) {
			documentService.selectPage(page);
		}
	}

	/**
	 * Create a new default bookmark.
	 */
	private void createNewBookmark() {
		try {
			Bookmark currBookmark = bookmarkService.getPageBookmark();
			if (nonNull(currBookmark)) {
				String shortcut = currBookmark.getShortcut();
				bookmarkService.deleteBookmark(currBookmark);
				bookmarkRemoved(shortcut);
				view.selectNewBookmarkButton(false);
			}
			else {
				bookmarkCreated(bookmarkService.createDefaultBookmark());
				view.selectNewBookmarkButton(true);
			}
		}
		catch (BookmarkExistsException e) {
			Page page = documentService.getDocuments().getSelectedDocument().getCurrentPage();
			String message = MessageFormat.format(context.getDictionary().get("bookmark.exists"), page.getPageNumber());
			context.showNotification(NotificationType.WARNING, "bookmark.assign.warning", message);
		}
		catch (BookmarkException e) {
			handleException(e, "Create bookmark failed", "bookmark.assign.warning");
		}
	}
	private void bookmarkCreated(Bookmark bookmark) {
		String shortcut = bookmark.getShortcut().toUpperCase();
		String message = MessageFormat.format(context.getDictionary().get("bookmark.created"), shortcut);

		context.showNotificationPopup(message);
		close();
	}

	private void bookmarkRemoved(String shortcut) {
		String message = MessageFormat.format(context.getDictionary().get("bookmark.removed"), shortcut);

		context.showNotificationPopup(message);
		close();
	}

	@Override
	public void initialize() {
		PresenterContext presenterContext = (PresenterContext) context;
		PresenterConfiguration config = (PresenterConfiguration) context.getConfiguration();

		eventBus.register(this);

		view.setScreensAvailable(presentationController.getScreensAvailable());
		view.setPresentationViewsVisible(presentationController.getPresentationViewsVisible());
		view.setRecordingState(ExecutableState.Stopped);
		view.setStreamingState(ExecutableState.Stopped);

		view.setOnUndo(this::undo);
		view.setOnRedo(this::redo);

		view.setOnPreviousSlide(toolController::selectPreviousPage);
		view.setOnNextSlide(toolController::selectNextPage);

		view.setOnPreviousBookmark(this::selectPreviousBookmark);
		view.setOnNextBookmark(this::selectNextBookmark);

		view.setOnNewBookmark(this::createNewBookmark);

		view.setOnCustomPaletteColor(this::customPaletteColor);
		view.setOnCustomColor(this::customColor);
		view.setOnColor1(this::color1);
		view.setOnColor2(this::color2);
		view.setOnColor3(this::color3);
		view.setOnColor4(this::color4);
		view.setOnColor5(this::color5);
		view.setOnColor6(this::color6);

		view.setOnPenTool(this::penTool);
		view.setOnHighlighterTool(this::highlighterTool);
		view.setOnPointerTool(this::pointerTool);
		view.setOnTextSelectTool(this::textSelectTool);
		view.setOnLineTool(this::lineTool);
		view.setOnArrowTool(this::arrowTool);
		view.setOnRectangleTool(this::rectangleTool);
		view.setOnEllipseTool(this::ellipseTool);
		view.setOnSelectTool(this::selectTool);
		view.setOnEraseTool(this::eraseTool);
		view.setOnTextTool(this::textTool);
		view.setOnTextBoxFont(this::setTextBoxFont);
		view.setOnTeXTool(this::texTool);
		view.setOnTeXBoxFont(this::setTeXBoxFont);
		view.setOnClearTool(this::clearTool);

		view.setOnShowGrid(this::showGrid);
		view.setOnExtend(this::extend);
		view.setOnWhiteboard(this::openWhiteboard);
		view.setOnEnableDisplays(this::enableDisplays);
		view.setOnZoomInTool(this::zoomInTool);
		view.setOnZoomOutTool(this::zoomOutTool);
		view.setOnPanTool(this::panTool);

		view.setOnStartRecording(this::startRecording);
		view.setOnStopRecording(this::stopRecording);

		view.bindEnableStream(presenterContext.streamStartedProperty());
		view.bindEnableStreamMicrophone(config.getStreamConfig().enableMicrophoneProperty());
		view.bindEnableStreamCamera(config.getStreamConfig().enableCameraProperty());
		view.bindEnableScreenSharing(this::selectScreenSource);

		view.setOnSelectQuiz(this::selectQuiz);
		view.setOnAudienceMessage(this::showAudienceMessageTemplate);

		// Register for page parameter change updates.
		PresentationParameterProvider ppProvider = context.getPagePropertyProvider(ViewType.User);
		ppProvider.addParameterChangeListener(new ParameterChangeListener() {

			@Override
			public Page forPage() {
				Document selectedDocument = documentService.getDocuments().getSelectedDocument();
				return nonNull(selectedDocument) ? selectedDocument.getCurrentPage() : null;
			}

			@Override
			public void parameterChanged(Page page, PresentationParameter parameter) {
				pageParameterChanged(page, parameter);
			}
		});

		presentationController.screensAvailableProperty().addListener((observable, oldValue, newValue) -> {
			view.setScreensAvailable(newValue);
		});
		presentationController.presentationViewsVisibleProperty().addListener((observable, oldValue, newValue) -> {
			view.setPresentationViewsVisible(newValue);
			activateDisplaysNotifyState.setPresentationViewsVisible(newValue);
		});
	}
}

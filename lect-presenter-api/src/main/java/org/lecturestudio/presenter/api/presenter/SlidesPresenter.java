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

import com.google.common.eventbus.Subscribe;
import org.lecturestudio.core.ExecutableException;
import org.lecturestudio.core.ExecutableState;
import org.lecturestudio.core.app.ApplicationContext;
import org.lecturestudio.core.app.configuration.DisplayConfiguration;
import org.lecturestudio.core.app.configuration.GridConfiguration;
import org.lecturestudio.core.bus.EventBus;
import org.lecturestudio.core.bus.event.DocumentEvent;
import org.lecturestudio.core.bus.event.PageEvent;
import org.lecturestudio.core.bus.event.ToolSelectionEvent;
import org.lecturestudio.core.controller.PresentationController;
import org.lecturestudio.core.controller.RenderController;
import org.lecturestudio.core.controller.ToolController;
import org.lecturestudio.core.geometry.Dimension2D;
import org.lecturestudio.core.geometry.Matrix;
import org.lecturestudio.core.geometry.Rectangle2D;
import org.lecturestudio.core.graphics.Color;
import org.lecturestudio.core.input.KeyEvent;
import org.lecturestudio.core.model.*;
import org.lecturestudio.core.model.listener.DocumentChangeListener;
import org.lecturestudio.core.model.listener.PageEditedListener;
import org.lecturestudio.core.model.listener.ParameterChangeListener;
import org.lecturestudio.core.model.shape.Shape;
import org.lecturestudio.core.model.shape.TeXShape;
import org.lecturestudio.core.model.shape.TextShape;
import org.lecturestudio.core.presenter.Presenter;
import org.lecturestudio.core.recording.DocumentRecorder;
import org.lecturestudio.core.service.DocumentService;
import org.lecturestudio.core.tool.ToolType;
import org.lecturestudio.core.util.ListChangeListener;
import org.lecturestudio.core.util.ObservableList;
import org.lecturestudio.core.view.*;
import org.lecturestudio.presenter.api.config.ExternalWindowConfiguration;
import org.lecturestudio.presenter.api.config.PresenterConfiguration;
import org.lecturestudio.presenter.api.context.PresenterContext;
import org.lecturestudio.presenter.api.event.*;
import org.lecturestudio.presenter.api.input.Shortcut;
import org.lecturestudio.presenter.api.model.MessageBarPosition;
import org.lecturestudio.presenter.api.pdf.PdfFactory;
import org.lecturestudio.presenter.api.pdf.embedded.SlideNoteParser;
import org.lecturestudio.presenter.api.service.RecordingService;
import org.lecturestudio.presenter.api.service.WebRtcStreamService;
import org.lecturestudio.presenter.api.service.WebService;
import org.lecturestudio.presenter.api.stylus.StylusHandler;
import org.lecturestudio.presenter.api.view.PageObjectRegistry;
import org.lecturestudio.presenter.api.view.SlidesView;
import org.lecturestudio.swing.model.ExternalWindowPosition;
import org.lecturestudio.web.api.event.PeerStateEvent;
import org.lecturestudio.web.api.event.VideoFrameEvent;
import org.lecturestudio.web.api.message.CourseParticipantMessage;
import org.lecturestudio.web.api.message.MessengerMessage;
import org.lecturestudio.web.api.message.SpeechCancelMessage;
import org.lecturestudio.web.api.message.SpeechRequestMessage;

import javax.inject.Inject;
import java.awt.*;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.function.BiConsumer;

import static java.util.Objects.*;

public class SlidesPresenter extends Presenter<SlidesView> {

	private final EventBus eventBus;

	private final Map<KeyEvent, Action> shortcutMap;

	private final PageObjectRegistry pageObjectRegistry;

	private final DocumentChangeListener documentChangeListener;

	private final DocumentRecorder documentRecorder;

	private final WebService webService;

	private final WebRtcStreamService streamService;

	private StylusHandler stylusHandler;

	private PageObjectView<?> lastFocusedObjectView;

	private PageEditedListener pageEditedListener;

	private SlideViewAddressOverlay addressOverlay;

	private ExecutableState streamingState;

	private ExecutableState messengerState;

	private ExecutableState quizState;

	private ToolType toolType;

	private final ViewContextFactory viewFactory;

	private final ToolController toolController;

	private final PresentationController presentationController;

	private final RenderController renderController;

	private final DocumentService documentService;

	private final RecordingService recordingService;


	@Inject
	SlidesPresenter(ApplicationContext context, SlidesView view,
					ViewContextFactory viewFactory,
					ToolController toolController,
					PresentationController presentationController,
					RenderController renderController,
					DocumentService documentService,
					DocumentRecorder documentRecorder,
					RecordingService recordingService,
					WebService webService,
					WebRtcStreamService streamService) {
		super(context, view);

		this.viewFactory = viewFactory;
		this.toolController = toolController;
		this.presentationController = presentationController;
		this.renderController = renderController;
		this.documentRecorder = documentRecorder;
		this.documentService = documentService;
		this.recordingService = recordingService;
		this.webService = webService;
		this.streamService = streamService;
		this.eventBus = context.getEventBus();
		this.shortcutMap = new HashMap<>();
		this.pageObjectRegistry = new PageObjectRegistry();
		this.documentChangeListener = new DocumentChangeHandler();
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
				documentSelected(event.getOldDocument(), doc);
				break;
			case REPLACED:
				documentReplaced(event.getOldDocument(), doc);
				break;
		}

		if (documentService.getDocuments().size() > 0) {
			showExternalScreens();
		}
		else {
			hideExternalScreens();
		}
	}

	@Subscribe
	public void onEvent(PageEvent event) {
		if (event.isSelected()) {
			setPage(event.getPage());
		}
	}

	@Subscribe
	public void onEvent(final RecordingStateEvent event) {
		if (!event.started()) {
			return;
		}

		// Restart document recording when a recording has been started.
		try {
			documentRecorder.stop();
			documentRecorder.start();

			Document doc = documentService.getDocuments().getSelectedDocument();

			recordPage(doc.getCurrentPage());
		}
		catch (Exception e) {
			logException(e, "Restart document recording failed");
		}
	}

	@Subscribe
	public void onEvent(final ToolSelectionEvent event) {
		toolChanged(event.getToolType());
	}

	@Subscribe
	public void onEvent(final QuizStateEvent event) {
		quizState = event.getState();

		view.setQuizState(event.getState());

		checkRemoteServiceState();
	}

	@Subscribe
	public void onEvent(final StreamingStateEvent event) {
		streamingState = event.getState();

		view.setStreamState(event.getState());

		checkRemoteServiceState();
	}

	@Subscribe
	public void onEvent(final MessengerStateEvent event) {
		messengerState = event.getState();

		view.setMessengerState(event.getState());

		checkRemoteServiceState();
	}

	@Subscribe
	public void onEvent(MessengerMessage message) {
		requireNonNull(message);

		PresenterContext presenterContext = (PresenterContext) context;
		presenterContext.getMessengerMessages().add(message);

		view.setMessengerMessage(message);
	}

	@Subscribe
	public void onEvent(SpeechRequestMessage message) {
		requireNonNull(message);

		PresenterContext presenterContext = (PresenterContext) context;
		presenterContext.getSpeechRequests().add(message);

		view.setSpeechRequestMessage(message);
	}

	@Subscribe
	public void onEvent(SpeechCancelMessage message) {
		requireNonNull(message);

		PresenterContext presenterContext = (PresenterContext) context;
		presenterContext.getSpeechRequests().removeIf(m -> Objects.equals(
				m.getRequestId(), message.getRequestId()));

		view.setSpeechCancelMessage(message);
	}

	@Subscribe
	public void onEvent(CourseParticipantMessage message) {
		PresenterContext presenterContext = (PresenterContext) context;

		if (message.getConnected()) {
			presenterContext.setAttendeesCount(presenterContext.getAttendeesCount() + 1);
		}
		else {
			presenterContext.setAttendeesCount(presenterContext.getAttendeesCount() - 1);
		}
	}

	@Subscribe
	public void onEvent(PeerStateEvent event) {
		view.setPeerStateEvent(event);
	}

	@Subscribe
	public void onEvent(VideoFrameEvent event) {
		view.setVideoFrameEvent(event);
	}

	@Subscribe
	public void onEvent(ExternalMessagesViewEvent event) {
		if (event.isEnabled()) {
			if (event.isShow()) {
				viewShowExternalMessages(event.isPersistent());
			}
			else {
				view.hideExternalMessages();
			}
		}
		else {
			viewHideExternalMessages(event.isPersistent());
		}
	}

	@Subscribe
	public void onEvent(ExternalSlidePreviewViewEvent event) {
		if (event.isEnabled()) {
			if (event.isShow()) {
				viewShowExternalSlidePreview(event.isPersistent());
			}
			else {
				view.hideExternalSlidePreview();
			}
		}
		else {
			viewHideExternalSlidePreview(event.isPersistent());
		}
	}

	@Subscribe
	public void onEvent(ExternalSpeechViewEvent event) {
		if (event.isEnabled()) {
			if (event.isShow()) {
				viewShowExternalSpeech(event.isPersistent());
			}
			else {
				view.hideExternalSpeech();
			}
		}
		else {
			viewHideExternalSpeech(event.isPersistent());
		}
	}

	@Subscribe
	public void onEvent(MessageBarPositionEvent event) {
		final MessageBarPosition position = event.position;

		view.setMessageBarPosition(position);

		getPresenterConfig().getSlideViewConfiguration().setMessageBarPosition(position);
	}

	private void externalMessagesPositionChanged(ExternalWindowPosition position) {
		final ExternalWindowConfiguration config = getExternalMessagesConfig();

		config.setPosition(position.getPosition());
		config.setScreen(position.getScreen());
	}

	private void externalMessagesSizeChanged(Dimension size) {
		getExternalMessagesConfig().setSize(size);
	}

	private void externalMessagesClosed() {
		eventBus.post(new ExternalMessagesViewEvent(false));
	}

	private void externalSlidePreviewPositionChanged(ExternalWindowPosition position) {
		final ExternalWindowConfiguration config = getExternalSlidePreviewConfig();

		config.setPosition(position.getPosition());
		config.setScreen(position.getScreen());
	}

	private void externalSlidePreviewSizeChanged(Dimension size) {
		getExternalSlidePreviewConfig().setSize(size);
	}

	private void externalSlidePreviewClosed() {
		eventBus.post(new ExternalSlidePreviewViewEvent(false));
	}

	private void externalSpeechPositionChanged(ExternalWindowPosition position) {
		final ExternalWindowConfiguration config = getExternalSpeechConfig();

		config.setPosition(position.getPosition());
		config.setScreen(position.getScreen());
	}

	private void externalSpeechSizeChanged(Dimension size) {
		getExternalSpeechConfig().setSize(size);
	}

	private void externalSpeechClosed() {
		eventBus.post(new ExternalSpeechViewEvent(false));
	}

	private void keyEvent(KeyEvent event) {
		Action action = shortcutMap.get(event);

		// Shortcuts have higher priority. If no shortcut mapping is found,
		// the key-event will be distributed.
		if (nonNull(action)) {
			action.execute();
		}
		else {
			toolController.setKeyEvent(event);
		}
	}

	private void onAcceptSpeech(SpeechRequestMessage message) {
		streamService.acceptSpeechRequest(message);

		PresenterContext presenterContext = (PresenterContext) context;
		presenterContext.getSpeechRequests().remove(message);

		Long requestId = message.getRequestId();
		String userName = String.format("%s %s", message.getFirstName(),
				message.getFamilyName());

		PeerStateEvent event = new PeerStateEvent(requestId, userName,
				ExecutableState.Starting);

		view.setPeerStateEvent(event);
	}

	private void onRejectSpeech(SpeechRequestMessage message) {
		streamService.rejectSpeechRequest(message);

		PresenterContext presenterContext = (PresenterContext) context;
		presenterContext.getSpeechRequests().remove(message);
	}

	private void onDiscardMessage(MessengerMessage message) {
		PresenterContext presenterContext = (PresenterContext) context;
		presenterContext.getMessengerMessages().remove(message);
	}

	private void onCreateMessageSlide(MessengerMessage message) {
		onDiscardMessage(message);

		try {
			Document messageDocument = createMessageDocument(message.getMessage().getText());

			Document prevMessageDocument = null;

			for (Document doc : documentService.getDocuments().asList()) {
				if (doc.isMessage()) {
					prevMessageDocument = doc;
					break;
				}
			}

			if (nonNull(prevMessageDocument)) {
				documentService.replaceDocument(prevMessageDocument, messageDocument);
			}
			else {
				documentService.addDocument(messageDocument);
			}

			documentService.selectDocument(messageDocument);
		}
		catch (Throwable e) {
			handleException(e, "Create message slide failed", "message.slide.create.error");
		}
	}

	private Document createMessageDocument(final String message) throws Exception {
		Document prevDoc = documentService.getDocuments().getSelectedDocument();

		Document doc = new Document();
		doc.setTitle(context.getDictionary().get("slides.message"));
		doc.setDocumentType(DocumentType.MESSAGE);

		if (nonNull(prevDoc)) {
			Rectangle2D rect = prevDoc.getPage(0).getPageRect();
			doc.setPageSize(new Dimension2D(rect.getWidth(), rect.getHeight()));
		}

		doc.createPage();
		PdfFactory.createMessagePage(doc.getPdfDocument(), message);
		doc.reload();

		return doc;
	}

	private void toolChanged(ToolType toolType) {
		this.toolType = toolType;

		setFocusedTeXView(null);

		view.removeAllPageObjectViews();
		view.setSelectedToolType(toolType);

		loadPageObjectViews(view.getPage());
	}

	private void pageObjectViewClosed(PageObjectView<? extends Shape> objectView) {
		view.removePageObjectView(objectView);

		// Remove associated shape from the page.
		Shape shape = objectView.getPageShape();

		Page page = view.getPage();
		page.removeShape(shape);

		if (shape instanceof TextShape) {
			// TODO: make this generic or remove at all
			TextShape textShape = (TextShape) shape;
			textShape.setOnRemove();
		}

		// Set latex text.
		Class<? extends Shape> shapeClass = pageObjectRegistry.getShapeClass(ToolType.LATEX);

		if (isNull(shapeClass) || !shapeClass.isAssignableFrom(objectView.getPageShape().getClass())) {
			return;
		}

		setFocusedTeXView(null);
		view.setLaTeXText("");
	}

	private void pageObjectViewFocused(PageObjectView<? extends Shape> objectView) {
		// Set latex text.
		Class<? extends Shape> shapeClass = pageObjectRegistry.getShapeClass(ToolType.LATEX);

		if (isNull(shapeClass) || !shapeClass.isAssignableFrom(objectView.getPageShape().getClass())) {
			return;
		}

		TeXBoxView teXBoxView = (TeXBoxView) objectView;

		if (objectView.getFocus()) {
			setFocusedTeXView(teXBoxView);
		}
	}

	private void pageObjectViewCopy(PageObjectView<? extends Shape> objectView) {
		Shape shape = objectView.getPageShape();
		Class<? extends Shape> shapeClass = pageObjectRegistry.getShapeClass(ToolType.TEXT);

		if (nonNull(shapeClass) && shapeClass.isAssignableFrom(shape.getClass())) {
			toolController.copyText((TextShape) shape.clone());
			return;
		}

		shapeClass = pageObjectRegistry.getShapeClass(ToolType.LATEX);

		if (nonNull(shapeClass) && shapeClass.isAssignableFrom(shape.getClass())) {
			toolController.copyTeX((TeXShape) shape.clone());
		}
	}

	private void setFocusedTeXView(TeXBoxView teXBoxView) {
		String texText = "";

		if (nonNull(teXBoxView)) {
			texText = teXBoxView.getText();
		}

		lastFocusedObjectView = teXBoxView;

		view.setLaTeXText(texText);
	}

	private void shareQuiz() {
		Document doc = documentService.getDocuments().getSelectedDocument();

		if (!doc.isQuiz()) {
			logException(new IllegalStateException(),
					"Selected document is not a quiz");
			return;
		}

		try {
			streamService.shareDocument(doc);
		}
		catch (IOException e) {
			logException(e, "Share document failed");
		}
	}

	private void stopQuiz() {
		try {
			webService.stopQuiz();
		}
		catch (ExecutableException e) {
			logException(e, "Stop quiz failed");
		}

		shareQuiz();
	}

	private void selectDocument(Document doc) {
		documentService.selectDocument(doc);
	}

	private void newWhiteboardPage() {
		documentService.createWhiteboardPage();
	}

	private void deleteWhiteboardPage() {
		documentService.deleteWhiteboardPage();
	}

	private void selectPage(Page page) {
		documentService.selectPage(page);
	}

	private void nextPage() {
		documentService.selectNextPage();
	}

	private void previousPage() {
		documentService.selectPreviousPage();
	}

	private void registerShortcut(Shortcut shortcut, Action action) {
		shortcutMap.put(shortcut.getKeyEvent(), action);
	}

	private void documentCreated(Document doc) {
		PresentationParameterProvider ppProvider = context.getPagePropertyProvider(ViewType.Preview);

		view.addDocument(doc, ppProvider);

		setPage(doc.getCurrentPage());

		if (documentService.getDocuments().size() == 1) {
			// The first document was created. Check display auto-start procedure.
			DisplayConfiguration displayConfig = context.getConfiguration().getDisplayConfig();
			presentationController.showPresentationViews(displayConfig.getAutostart());
		}
	}

	private void documentClosed(Document doc) {
		view.removeDocument(doc);
	}

	private void documentReplaced(Document oldDoc, Document doc) {
		view.addDocument(doc, context.getPagePropertyProvider(ViewType.Preview));

		setPage(doc.getCurrentPage());
	}

	private void documentSelected(Document oldDoc, Document doc) {
		if (nonNull(oldDoc)) {
			oldDoc.removeChangeListener(documentChangeListener);
		}

		doc.addChangeListener(documentChangeListener);

		view.selectDocument(doc, context.getPagePropertyProvider(ViewType.Preview));

		setPage(doc.getCurrentPage());
	}

	private void copyOverlay() {
		Document doc = documentService.getDocuments().getSelectedDocument();
		Page lastPage = doc.getPage(doc.getCurrentPageNumber() - 1);
		doc.getCurrentPage().adoptNoLabel(lastPage);
	}

	private void copyNextOverlay() {
		nextPage();
		copyOverlay();
	}

	private void setPage(Page page) {
		Document doc = documentService.getDocuments().getSelectedDocument();

		if (nonNull(page) && page.getDocument() != doc) {
			return;
		}

		PresentationParameterProvider ppProvider = context.getPagePropertyProvider(ViewType.User);
		PresentationParameter parameter = ppProvider.getParameter(page);

		List<SlideNote> embeddedNotes = null;

		if (nonNull(page)) {
			SlideNoteParser parser = new SlideNoteParser();
			parser.parse(page.getPageText());

			embeddedNotes = parser.getSlideNotes();
		}

		stylusHandler.setPresentationParameter(parameter);

		if (nonNull(view.getPage())) {
			view.getPage().removePageEditedListener(pageEditedListener);
		}

		if (nonNull(page)) {
			page.addPageEditedListener(pageEditedListener);
		}

		setFocusedTeXView(null);

		view.removeAllPageObjectViews();
		view.setPage(page, parameter);
		view.setPageNotes(embeddedNotes);

		loadPageObjectViews(page);

		recordPage(page);
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
			objectView.setFocus(view.getPageObjectViews().stream().noneMatch(PageObjectView::isCopying));
		}
		catch (Exception e) {
			logException(e, "Create PageObjectView failed");
		}
	}

	private void loadPageObjectViews(Page page) {
		if (pageObjectRegistry.containsViewShapes(toolType, page)) {
			Class<? extends PageObjectView<? extends Shape>> viewClass =
					pageObjectRegistry.getPageObjectViewClass(toolType);
			Class<? extends Shape> shapeClass = pageObjectRegistry.getShapeClass(toolType);

			for (Shape shape : page.getShapes()) {
				if (shapeClass.isAssignableFrom(shape.getClass())) {
					createPageObjectView(shape, viewClass);
				}
			}
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
			pageObjectViewFocused(objectView);
		});
		objectView.setOnCopy(() -> {
			pageObjectViewCopy(objectView);
		});

		view.addPageObjectView(objectView);

		return objectView;
	}

	private void setLaTeXText(String text) {
		if (isNull(lastFocusedObjectView)) {
			return;
		}

		TeXBoxView teXBoxView = (TeXBoxView) lastFocusedObjectView;
		teXBoxView.setText(text);
	}

	private void setViewTransform(Matrix matrix) {
		toolController.setViewTransform(matrix.clone());
	}

	private void setOutlineItem(DocumentOutlineItem item) {
		Integer pageNumber = item.getPageNumber();

		if (nonNull(pageNumber)) {
			documentService.selectPage(pageNumber);
		}
	}

	private PresenterConfiguration getPresenterConfig() {
		return (PresenterConfiguration) context.getConfiguration();
	}

	private ExternalWindowConfiguration getExternalMessagesConfig() {
		return getPresenterConfig().getExternalMessagesConfig();
	}

	private ExternalWindowConfiguration getExternalSlidePreviewConfig() {
		return getPresenterConfig().getExternalSlidePreviewConfig();
	}

	private ExternalWindowConfiguration getExternalSpeechConfig() {
		return getPresenterConfig().getExternalSpeechConfig();
	}

	@Override
	public void initialize() {
		stylusHandler = new StylusHandler(toolController);

		pageObjectRegistry.register(ToolType.TEXT, TextBoxView.class);
		pageObjectRegistry.register(ToolType.LATEX, TeXBoxView.class);

		eventBus.register(this);

		final PresenterContext ctx = (PresenterContext) context;
		final PresenterConfiguration config = getPresenterConfig();
		final DisplayConfiguration displayConfig = config.getDisplayConfig();
		final GridConfiguration gridConfig = config.getGridConfig();

		// Set default tool.
		toolController.selectPenTool();

		pageEditedListener = (event) -> {
			switch (event.getType()) {
				case SHAPE_ADDED:
					pageShapeAdded(event.getShape());
					break;
				case CLEAR:
					setPage(event.getPage());
					break;
			}
		};

		// Register for page parameter change updates.
		PresentationParameterProvider ppProvider = context.getPagePropertyProvider(ViewType.User);
		ppProvider.addParameterChangeListener(new ParameterChangeListener() {

			@Override
			public Page forPage() {
				return view.getPage();
			}

			@Override
			public void parameterChanged(Page page, PresentationParameter parameter) {
				stylusHandler.setPresentationParameter(parameter);

				view.setPage(page, parameter);
			}
		});

		// Observe Address overlay setting.
		displayConfig.ipPositionProperty().addListener((observable, oldValue, newValue) -> {
			checkRemoteServiceState();
		});

		gridConfig.showGridOnDisplaysProperty().addListener((observable, oldValue, newValue) -> {
			// Update grid parameter.
			PresentationParameterProvider pProvider = context.getPagePropertyProvider(ViewType.Presentation);

			if (!newValue) {
				// Hide grid if previously enabled.
				pProvider.getAllPresentationParameters().forEach(param -> param.setShowGrid(newValue));
			}
			else {
				// Sync with user's view.
				PresentationParameterProvider uProvider = context.getPagePropertyProvider(ViewType.User);

				for (PresentationParameter param : pProvider.getAllPresentationParameters()) {
					PresentationParameter userParam = uProvider.getParameter(param.getPage());

					param.setShowGrid(userParam.showGrid());
				}
			}
		});

		config.extendedFullscreenProperty().addListener((observable, oldValue, newValue) -> {
			view.setExtendedFullscreen(newValue);
		});

		view.setOnExternalMessagesPositionChanged(this::externalMessagesPositionChanged);
		view.setOnExternalMessagesSizeChanged(this::externalMessagesSizeChanged);
		view.setOnExternalMessagesClosed(this::externalMessagesClosed);
		initExternalScreenBehavior(getExternalMessagesConfig(),
				(enabled, show) -> eventBus.post(new ExternalMessagesViewEvent(enabled, show)));

		view.setOnExternalSlidePreviewPositionChanged(this::externalSlidePreviewPositionChanged);
		view.setOnExternalSlidePreviewSizeChanged(this::externalSlidePreviewSizeChanged);
		view.setOnExternalSlidePreviewClosed(this::externalSlidePreviewClosed);
		initExternalScreenBehavior(getExternalSlidePreviewConfig(),
				(enabled, show) -> eventBus.post(new ExternalSlidePreviewViewEvent(enabled, show)));

		view.setOnExternalSpeechPositionChanged(this::externalSpeechPositionChanged);
		view.setOnExternalSpeechSizeChanged(this::externalSpeechSizeChanged);
		view.setOnExternalSpeechClosed(this::externalSpeechClosed);
		initExternalScreenBehavior(getExternalSpeechConfig(),
				(enabled, show) -> eventBus.post(new ExternalSpeechViewEvent(enabled, show)));

		view.setPageRenderer(renderController);
		view.setStylusHandler(stylusHandler);
		view.setExtendedFullscreen(config.getExtendedFullscreen());
		view.setMessengerState(ExecutableState.Stopped);

		view.setSlideViewConfig(config.getSlideViewConfiguration());
		view.bindShowOutline(ctx.showOutlineProperty());
		view.setOnOutlineItem(this::setOutlineItem);

		view.setOnKeyEvent(this::keyEvent);
		view.setOnStopQuiz(this::stopQuiz);
		view.setOnNewPage(this::newWhiteboardPage);
		view.setOnDeletePage(this::deleteWhiteboardPage);
		view.setOnSelectPage(this::selectPage);
		view.setOnSelectDocument(this::selectDocument);
		view.setOnViewTransform(this::setViewTransform);
		view.setOnLaTeXText(this::setLaTeXText);

		view.setOnAcceptSpeech(this::onAcceptSpeech);
		view.setOnRejectSpeech(this::onRejectSpeech);
		view.setOnDiscardMessage(this::onDiscardMessage);
		view.setOnCreateMessageSlide(this::onCreateMessageSlide);
		view.setOnMutePeerAudio(streamService::mutePeerAudio);
		view.setOnMutePeerVideo(streamService::mutePeerVideo);
		view.setOnStopPeerConnection(streamService::stopPeerConnection);

		// Register shortcuts that are associated with the SlideView.
		registerShortcut(Shortcut.SLIDE_NEXT_DOWN, this::nextPage);
		registerShortcut(Shortcut.SLIDE_NEXT_PAGE_DOWN, this::nextPage);
		registerShortcut(Shortcut.SLIDE_NEXT_RIGHT, this::nextPage);
		registerShortcut(Shortcut.SLIDE_NEXT_SPACE, this::nextPage);

		registerShortcut(Shortcut.SLIDE_PREVIOUS_LEFT, this::previousPage);
		registerShortcut(Shortcut.SLIDE_PREVIOUS_PAGE_UP, this::previousPage);
		registerShortcut(Shortcut.SLIDE_PREVIOUS_UP, this::previousPage);

		registerShortcut(Shortcut.COPY_OVERLAY, this::copyOverlay);
		registerShortcut(Shortcut.COPY_OVERLAY_NEXT_PAGE_CTRL, this::copyNextOverlay);
		registerShortcut(Shortcut.COPY_OVERLAY_NEXT_PAGE_SHIFT, this::copyNextOverlay);

		view.setMessageBarPosition(
				getPresenterConfig().getSlideViewConfiguration()
						.getMessageBarPosition());

		try {
			recordingService.init();

			documentRecorder.setHasChangesProperty(ctx.hasRecordedChangesProperty());
			documentRecorder.start();
		}
		catch (ExecutableException e) {
			throw new RuntimeException(e);
		}
	}

	private void initExternalScreenBehavior(ExternalWindowConfiguration config, BiConsumer<Boolean, Boolean> action) {
		final ObservableList<Screen> screens = presentationController.getScreens();

		screens.addListener(new ListChangeListener<>() {
			@Override
			public void listChanged(ObservableList<Screen> list) {
				if (documentService.getDocuments().size() <= 0) {
					return;
				}

				action.accept(config.isEnabled(), checkIfScreenInList(list, config.getScreen()));
			}
		});
	}

	private void showExternalScreens() {
		showExternalScreen(getExternalMessagesConfig(),
				(enabled, show) -> eventBus.post(new ExternalMessagesViewEvent(enabled, show)));
		showExternalScreen(getExternalSlidePreviewConfig(),
				(enabled, show) -> eventBus.post(new ExternalSlidePreviewViewEvent(enabled, show)));
		showExternalScreen(getExternalSpeechConfig(),
				(enabled, show) -> eventBus.post(new ExternalSpeechViewEvent(enabled, show)));
	}

	private void showExternalScreen(ExternalWindowConfiguration config, BiConsumer<Boolean, Boolean> action) {
		final ObservableList<Screen> screens = presentationController.getScreens();

		action.accept(config.isEnabled(), checkIfScreenInList(screens, config.getScreen()));
	}

	private void hideExternalScreens() {
		view.hideExternalSlidePreview();
		view.hideExternalMessages();
		view.hideExternalSpeech();
	}

	private void viewShowExternalMessages(boolean persistent) {
		final ExternalWindowConfiguration config = getExternalMessagesConfig();

		if (persistent) {
			config.setEnabled(true);
		}

		view.showExternalMessages(config.getScreen(), config.getPosition(), config.getSize());
	}

	private void viewHideExternalMessages(boolean persistent) {
		final ExternalWindowConfiguration config = getExternalMessagesConfig();

		if (persistent) {
			config.setEnabled(false);
			config.setScreen(null);
			config.setPosition(null);
			config.setSize(null);
		}

		view.hideExternalMessages();
	}

	private void viewShowExternalSlidePreview(boolean persistent) {
		final ExternalWindowConfiguration config = getExternalSlidePreviewConfig();

		if (persistent) {
			config.setEnabled(true);
		}

		view.showExternalSlidePreview(config.getScreen(), config.getPosition(), config.getSize());
	}

	private void viewHideExternalSlidePreview(boolean persistent) {
		final ExternalWindowConfiguration config = getExternalSlidePreviewConfig();

		if (persistent) {
			config.setEnabled(false);
			config.setScreen(null);
			config.setPosition(null);
			config.setSize(null);
		}

		view.hideExternalSlidePreview();
	}

	private void viewShowExternalSpeech(boolean persistent) {
		final ExternalWindowConfiguration config = getExternalSpeechConfig();

		if (persistent) {
			config.setEnabled(true);
		}

		view.showExternalSpeech(config.getScreen(), config.getPosition(), config.getSize());
	}

	private void viewHideExternalSpeech(boolean persistent) {
		final ExternalWindowConfiguration config = getExternalSpeechConfig();

		if (persistent) {
			config.setEnabled(false);
			config.setScreen(null);
			config.setPosition(null);
			config.setSize(null);
		}

		view.hideExternalSpeech();
	}

	private boolean checkIfScreenInList(List<Screen> screens, Screen screen) {
		if (screen == null) {
			return true;
		}
		return screens.stream().anyMatch(scrn -> scrn.equals(screen));
	}

	private void recordPage(Page page) {
		try {
			documentRecorder.recordPage(page);
		}
		catch (ExecutableException e) {
			logException(e, "Record page failed");
		}
	}

	private void checkRemoteServiceState() {
		boolean streamStarted = streamingState == ExecutableState.Started;
		boolean messengerStarted = messengerState == ExecutableState.Started;
		boolean quizStarted = quizState == ExecutableState.Started;

		if (nonNull(addressOverlay)) {
			presentationController.removeSlideViewOverlay(addressOverlay);
			addressOverlay = null;
		}

		if ((!streamStarted && messengerStarted) || quizStarted) {
			addressOverlay = createRemoteAddressOverlay();

			presentationController.addSlideViewOverlay(addressOverlay);
		}
	}

	private SlideViewAddressOverlay createRemoteAddressOverlay() {
		PresenterConfiguration config = getPresenterConfig();
		DisplayConfiguration displayConfig = config.getDisplayConfig();

		SlideViewAddressOverlay overlay = viewFactory.getInstance(SlideViewAddressOverlay.class);
		overlay.setPosition(displayConfig.getIpPosition());
		overlay.setTextColor(Color.BLACK);
		overlay.setBackgroundColor(Color.WHITE);
		overlay.setFontSize(30);

		return overlay;
	}


	private class DocumentChangeHandler implements DocumentChangeListener {

		@Override
		public void documentChanged(Document document) {
			CompletableFuture.runAsync(() -> {
				setPage(document.getCurrentPage());
			});
		}

		@Override
		public void pageAdded(Page page) {

		}

		@Override
		public void pageRemoved(Page page) {

		}
	}
}

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
import static java.util.Objects.requireNonNull;

import com.google.common.eventbus.Subscribe;

import java.net.SocketException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

import javax.inject.Inject;

import org.lecturestudio.broadcast.config.BroadcastProfile;
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
import org.lecturestudio.core.geometry.Matrix;
import org.lecturestudio.core.graphics.Color;
import org.lecturestudio.core.input.KeyEvent;
import org.lecturestudio.core.model.Document;
import org.lecturestudio.core.model.DocumentOutlineItem;
import org.lecturestudio.core.model.Page;
import org.lecturestudio.core.model.SlideNote;
import org.lecturestudio.core.model.listener.DocumentChangeListener;
import org.lecturestudio.core.model.listener.PageEditEvent;
import org.lecturestudio.core.model.listener.PageEditedListener;
import org.lecturestudio.core.model.listener.ParameterChangeListener;
import org.lecturestudio.core.model.shape.Shape;
import org.lecturestudio.core.model.shape.TeXShape;
import org.lecturestudio.core.model.shape.TextShape;
import org.lecturestudio.core.presenter.Presenter;
import org.lecturestudio.core.recording.DocumentRecorder;
import org.lecturestudio.core.service.DocumentService;
import org.lecturestudio.core.tool.ToolType;
import org.lecturestudio.core.util.NetUtils;
import org.lecturestudio.core.view.Action;
import org.lecturestudio.core.view.PageObjectView;
import org.lecturestudio.core.view.PresentationParameter;
import org.lecturestudio.core.view.PresentationParameterProvider;
import org.lecturestudio.core.view.SlideViewAddressOverlay;
import org.lecturestudio.core.view.TeXBoxView;
import org.lecturestudio.core.view.TextBoxView;
import org.lecturestudio.core.view.ViewContextFactory;
import org.lecturestudio.core.view.ViewType;
import org.lecturestudio.presenter.api.config.NetworkConfiguration;
import org.lecturestudio.presenter.api.config.PresenterConfiguration;
import org.lecturestudio.presenter.api.context.PresenterContext;
import org.lecturestudio.presenter.api.event.MessengerStateEvent;
import org.lecturestudio.presenter.api.event.QuizStateEvent;
import org.lecturestudio.presenter.api.event.RecordingStateEvent;
import org.lecturestudio.presenter.api.event.StreamingStateEvent;
import org.lecturestudio.presenter.api.input.Shortcut;
import org.lecturestudio.presenter.api.pdf.embedded.SlideNoteParser;
import org.lecturestudio.presenter.api.service.RecordingService;
import org.lecturestudio.presenter.api.stylus.StylusHandler;
import org.lecturestudio.presenter.api.view.PageObjectRegistry;
import org.lecturestudio.presenter.api.view.SlidesView;
import org.lecturestudio.web.api.message.MessengerMessage;
import org.lecturestudio.web.api.message.SpeechAcceptMessage;
import org.lecturestudio.web.api.message.SpeechCancelMessage;
import org.lecturestudio.web.api.message.SpeechRejectMessage;
import org.lecturestudio.web.api.message.SpeechRequestMessage;

public class SlidesPresenter extends Presenter<SlidesView> {

	private final EventBus eventBus;

	private final Map<KeyEvent, Action> shortcutMap;

	private final PageObjectRegistry pageObjectRegistry;

	private final DocumentChangeListener documentChangeListener;

	private final DocumentRecorder documentRecorder;

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
			RecordingService recordingService) {
		super(context, view);

		this.viewFactory = viewFactory;
		this.toolController = toolController;
		this.presentationController = presentationController;
		this.renderController = renderController;
		this.documentRecorder = documentRecorder;
		this.documentService = documentService;
		this.recordingService = recordingService;
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
		this.quizState = event.getState();

		checkRemoteServiceState();
	}

	@Subscribe
	public void onEvent(final StreamingStateEvent event) {
		this.streamingState = event.getState();

		view.setMessengerState(event.getState());

		checkRemoteServiceState();
	}

	@Subscribe
	public void onEvent(final MessengerStateEvent event) {
		this.messengerState = event.getState();

		view.setMessengerState(event.getState());

		if (event.getState() == ExecutableState.Stopped) {
			// Clear messenger view.
			view.setMessengerMessage(null);
		}

		checkRemoteServiceState();
	}

	@Subscribe
	public void onEvent(MessengerMessage message) {
		requireNonNull(message);

		view.setMessengerMessage(message);
	}

	@Subscribe
	public void onEvent(SpeechRequestMessage message) {
		requireNonNull(message);

		view.setSpeechRequestMessage(message, this::onAcceptSpeech, this::onCancelSpeech);
	}

	@Subscribe
	public void onEvent(SpeechCancelMessage message) {
		requireNonNull(message);

		view.setSpeechCancelMessage(message);
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

	private void onAcceptSpeech() {
		eventBus.post(new SpeechAcceptMessage());
	}

	private void onCancelSpeech() {
		eventBus.post(new SpeechRejectMessage());
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
		if (nonNull(oldDoc)) {
			oldDoc.removeChangeListener(documentChangeListener);
		}

		doc.addChangeListener(documentChangeListener);

		view.selectDocument(doc, context.getPagePropertyProvider(ViewType.Preview));

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

		page.addPageEditedListener(pageEditedListener);

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

		Class<? extends PageObjectView<? extends Shape>> viewClass = pageObjectRegistry.getPageObjectViewClass(toolType);

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
			Class<? extends PageObjectView<? extends Shape>> viewClass = pageObjectRegistry.getPageObjectViewClass(toolType);
			Class<? extends Shape> shapeClass = pageObjectRegistry.getShapeClass(toolType);

			for (Shape shape : page.getShapes()) {
				if (shapeClass.isAssignableFrom(shape.getClass())) {
					createPageObjectView(shape, viewClass);
				}
			}
		}
	}

	private PageObjectView<? extends Shape> createPageObjectView(Shape shape, Class<? extends PageObjectView<? extends Shape>> viewClass) {
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

	@Override
	public void initialize() {
		stylusHandler = new StylusHandler(toolController);

		pageObjectRegistry.register(ToolType.TEXT, TextBoxView.class);
		pageObjectRegistry.register(ToolType.LATEX, TeXBoxView.class);

		eventBus.register(this);

		PresenterContext ctx = (PresenterContext) context;
		DisplayConfiguration displayConfig = context.getConfiguration().getDisplayConfig();
		GridConfiguration gridConfig = context.getConfiguration().getGridConfig();

		// Set default tool.
		toolController.selectPenTool();

		pageEditedListener = (event) -> {
			if (event.getType() == PageEditEvent.Type.SHAPE_ADDED) {
				pageShapeAdded(event.getShape());
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

		context.getConfiguration().extendedFullscreenProperty().addListener((observable, oldValue, newValue) -> {
			view.setExtendedFullscreen(newValue);
		});

		view.setPageRenderer(renderController);
		view.setStylusHandler(stylusHandler);
		view.setExtendedFullscreen(context.getConfiguration().getExtendedFullscreen());
		view.setMessengerState(ExecutableState.Stopped);

		view.bindShowOutline(ctx.showOutlineProperty());
		view.setOnOutlineItem(this::setOutlineItem);

		view.setOnKeyEvent(this::keyEvent);
		view.setOnNewPage(this::newWhiteboardPage);
		view.setOnDeletePage(this::deleteWhiteboardPage);
		view.setOnSelectPage(this::selectPage);
		view.setOnSelectDocument(this::selectDocument);
		view.setOnViewTransform(this::setViewTransform);
		view.setOnLaTeXText(this::setLaTeXText);

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
		registerShortcut(Shortcut.COPY_OVERLAY_NEXT_PAGE_SHIFT,this::copyNextOverlay);

		try {
			recordingService.init();

			documentRecorder.setHasChangesProperty(ctx.hasRecordedChangesProperty());
			documentRecorder.start();
		}
		catch (ExecutableException e) {
			throw new RuntimeException(e);
		}
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
		PresenterConfiguration config = (PresenterConfiguration) context.getConfiguration();
		DisplayConfiguration displayConfig = config.getDisplayConfig();
		NetworkConfiguration netConfig = config.getNetworkConfig();
		BroadcastProfile profile = netConfig.getBroadcastProfile();

		String broadcastAddress = profile.getBroadcastAddress();
		Integer broadcastPort = profile.getBroadcastPort();

		boolean isLocal = NetUtils.isLocalAddress(broadcastAddress, broadcastPort);

		if (isLocal) {
			// Get local address.
			String adapter = config.getNetworkConfig().getAdapter();

			if (isNull(adapter)) {
				adapter = NetUtils.getNetworkInterfaces().get(0).getName();
			}

			try {
				broadcastAddress = NetUtils.getHostAddress(adapter, java.net.Inet4Address.class);
			}
			catch (SocketException e) {
				logException(e, "Unknown Host");
			}
		}

		// Show only non-standard port.
		if (broadcastPort != 80) {
			broadcastAddress += ":" + broadcastPort;
		}

		SlideViewAddressOverlay overlay = viewFactory.getInstance(SlideViewAddressOverlay.class);
		overlay.setAddress(broadcastAddress);
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

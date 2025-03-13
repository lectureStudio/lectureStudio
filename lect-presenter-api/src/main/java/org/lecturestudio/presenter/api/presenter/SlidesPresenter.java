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

import javax.inject.Inject;

import java.awt.Dimension;
import java.io.File;
import java.io.IOException;
import java.math.BigInteger;
import java.text.MessageFormat;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.BiConsumer;

import com.google.common.eventbus.Subscribe;

import org.lecturestudio.core.ExecutableException;
import org.lecturestudio.core.ExecutableState;
import org.lecturestudio.core.app.ApplicationContext;
import org.lecturestudio.core.app.configuration.DisplayConfiguration;
import org.lecturestudio.core.app.configuration.WhiteboardConfiguration;
import org.lecturestudio.core.audio.bus.event.TextColorEvent;
import org.lecturestudio.core.audio.bus.event.TextFontEvent;
import org.lecturestudio.core.bus.EventBus;
import org.lecturestudio.core.bus.event.DocumentEvent;
import org.lecturestudio.core.bus.event.PageEvent;
import org.lecturestudio.core.bus.event.ToolSelectionEvent;
import org.lecturestudio.core.controller.PresentationController;
import org.lecturestudio.core.controller.RenderController;
import org.lecturestudio.core.controller.ToolController;
import org.lecturestudio.core.geometry.Matrix;
import org.lecturestudio.core.geometry.Rectangle2D;
import org.lecturestudio.core.graphics.Color;
import org.lecturestudio.core.input.KeyEvent;
import org.lecturestudio.core.input.ScrollHandler;
import org.lecturestudio.core.model.Document;
import org.lecturestudio.core.model.DocumentOutlineItem;
import org.lecturestudio.core.model.Page;
import org.lecturestudio.core.model.listener.DocumentChangeListener;
import org.lecturestudio.core.model.listener.PageEditedListener;
import org.lecturestudio.core.model.listener.ParameterChangeListener;
import org.lecturestudio.core.model.shape.Shape;
import org.lecturestudio.core.model.shape.TeXShape;
import org.lecturestudio.core.model.shape.TextShape;
import org.lecturestudio.core.presenter.Presenter;
import org.lecturestudio.core.recording.DocumentRecorder;
import org.lecturestudio.core.service.DocumentService;
import org.lecturestudio.core.stylus.StylusHandler;
import org.lecturestudio.core.text.Font;
import org.lecturestudio.core.tool.ToolType;
import org.lecturestudio.core.util.ListChangeListener;
import org.lecturestudio.core.util.ObservableList;
import org.lecturestudio.core.util.OsInfo;
import org.lecturestudio.core.view.Action;
import org.lecturestudio.core.view.PageObjectRegistry;
import org.lecturestudio.core.view.PageObjectView;
import org.lecturestudio.core.view.PresentationParameter;
import org.lecturestudio.core.view.PresentationParameterProvider;
import org.lecturestudio.core.view.PresentationViewContext;
import org.lecturestudio.core.view.Screen;
import org.lecturestudio.core.view.SlidePresentationViewContext;
import org.lecturestudio.core.view.SlideViewAddressOverlay;
import org.lecturestudio.core.view.TeXBoxView;
import org.lecturestudio.core.view.TextBoxView;
import org.lecturestudio.core.view.ViewContextFactory;
import org.lecturestudio.core.view.ViewType;
import org.lecturestudio.presenter.api.config.*;
import org.lecturestudio.presenter.api.context.PresenterContext;
import org.lecturestudio.presenter.api.event.*;
import org.lecturestudio.presenter.api.input.Shortcut;
import org.lecturestudio.presenter.api.model.*;
import org.lecturestudio.presenter.api.service.*;
import org.lecturestudio.presenter.api.view.SlidesView;
import org.lecturestudio.swing.model.ExternalWindowPosition;
import org.lecturestudio.swing.view.ParticipantView;
import org.lecturestudio.web.api.event.LocalScreenVideoFrameEvent;
import org.lecturestudio.web.api.event.PeerStateEvent;
import org.lecturestudio.web.api.janus.JanusParticipantContext;
import org.lecturestudio.web.api.message.CoursePresenceMessage;
import org.lecturestudio.web.api.message.MessengerMessage;
import org.lecturestudio.web.api.message.SpeechBaseMessage;
import org.lecturestudio.web.api.message.SpeechCancelMessage;
import org.lecturestudio.web.api.message.SpeechRequestMessage;
import org.lecturestudio.web.api.message.util.MessageUtil;
import org.lecturestudio.web.api.model.Message;
import org.lecturestudio.web.api.model.ScreenSource;
import org.lecturestudio.web.api.service.ServiceParameters;
import org.lecturestudio.web.api.stream.ScreenPresentationViewContext;
import org.lecturestudio.web.api.stream.model.CourseParticipant;
import org.lecturestudio.web.api.stream.model.CoursePresence;
import org.lecturestudio.web.api.stream.service.StreamProviderService;

public class SlidesPresenter extends Presenter<SlidesView> {

	private final EventBus eventBus;

	private final Map<KeyEvent, Action> shortcutMap;

	private final Map<JanusParticipantContext, ParticipantView> participantViewMap;

	private final PageObjectRegistry pageObjectRegistry;

	private final DocumentChangeListener documentChangeListener;

	private final DocumentRecorder documentRecorder;

	private final WebService webService;

	private final WebServiceInfo webServiceInfo;

	private final WebRtcStreamService streamService;

	private final ScreenPresentationViewContext screenViewContext;

	private final ViewContextFactory viewFactory;

	private final ToolController toolController;

	private final PresentationController presentationController;

	private final RenderController renderController;

	private final BookmarkService bookmarkService;

	private final DocumentService documentService;

	private final RecordingService recordingService;

	private final UserPrivilegeService userPrivilegeService;

	private final ParticipantViewCollection participantViewCollection;

	private StylusHandler stylusHandler;

	private PageEditedListener pageEditedListener;

	private SlideViewAddressOverlay addressOverlay;

	private ExecutableState streamingState;

	private ExecutableState messengerState;

	private ExecutableState quizState;

	private ToolType toolType;

	private TextBoxView lastFocusedTextBox;

	private SelectionIdleTimer idleTimer;


	@Inject
	SlidesPresenter(ApplicationContext context, SlidesView view,
					ViewContextFactory viewFactory,
					ToolController toolController,
					PresentationController presentationController,
					RenderController renderController,
					BookmarkService bookmarkService,
					DocumentService documentService,
					DocumentRecorder documentRecorder,
					RecordingService recordingService,
					UserPrivilegeService userPrivilegeService,
					WebService webService,
					WebServiceInfo webServiceInfo,
					WebRtcStreamService streamService) {
		super(context, view);

		this.viewFactory = viewFactory;
		this.toolController = toolController;
		this.presentationController = presentationController;
		this.renderController = renderController;
		this.documentRecorder = documentRecorder;
		this.bookmarkService = bookmarkService;
		this.documentService = documentService;
		this.recordingService = recordingService;
		this.userPrivilegeService = userPrivilegeService;
		this.webService = webService;
		this.webServiceInfo = webServiceInfo;
		this.streamService = streamService;
		this.eventBus = context.getEventBus();
		this.shortcutMap = new HashMap<>();
		this.participantViewMap = new ConcurrentHashMap<>();
		this.pageObjectRegistry = new PageObjectRegistry();
		this.documentChangeListener = new DocumentChangeHandler();
		this.screenViewContext = new ScreenPresentationViewContext();
		this.participantViewCollection = new ParticipantViewCollection(1);
	}

	@Subscribe
	public void onEvent(DocumentEvent event) {
		Document doc = event.getDocument();

		switch (event.getType()) {
			case CREATED -> documentCreated(doc);
			case CLOSED -> documentClosed(doc);
			case SELECTED -> documentSelected(event.getOldDocument(), doc);
			case REPLACED -> documentReplaced(event.getOldDocument(), doc);
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
		boolean prevSuspended = recordingService.getPreviousState() == ExecutableState.Suspended;

		if (!event.started() || prevSuspended) {
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

		if (event.started()) {
			loadParticipants();
		}
		else if (event.stopped()) {
			onEvent(new ScreenShareStateEvent(null, event.getState()));
		}
	}

	@Subscribe
	public void onEvent(final StreamReconnectStateEvent event) {
		if (event.stopped()) {
			// Update state on successful reconnection.
			loadParticipants();
		}
	}

	@Subscribe
	public void onEvent(final ScreenShareStateEvent event) {
		PresenterContext ctx = (PresenterContext) context;
		PresentationViewContext viewContext = null;

		ScreenSource screenSource = event.getScreenSource();
		Document screenDocument = null;

		if (nonNull(screenSource)) {
			for (Document doc : documentService.getDocuments().asList()) {
				if (doc.getTitle().equals(screenSource.getTitle())) {
					screenDocument = doc;
					break;
				}
			}
		}

		// The document related to the screen source may have been closed already
		// due to the sharing has finished.
		if (nonNull(screenDocument)) {
			view.setScreenShareState(event.getState(), screenDocument);
		}

		switch (event.getState()) {
			case Started -> {
				viewContext = screenViewContext;
			}
			case Stopped -> {
				viewContext = new SlidePresentationViewContext();
				ctx.setScreenSharingStarted(false);
			}
		}

		presentationController.setPresentationViewContext(viewContext);
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

		if (message.isDeleted()) {
			onDiscardMessage(message);
			view.removeMessengerMessage(message.getMessageId());
			return;
		}

		if (message.isEdited()) {
			view.setModifiedMessengerMessage(message);
			MessageUtil.updateOutdatedMessage(presenterContext.getMessengerMessages(), message);
			MessageUtil.updateOutdatedMessage(presenterContext.getAllReceivedMessengerMessages(), message);
			return;
		}

		presenterContext.getMessengerMessages().add(message);
		presenterContext.getAllReceivedMessengerMessages().add(message);

		if (MessageUtil.isReply(message)) {
			final MessengerMessage messageToReplyTo = MessageUtil.findMessageToReplyTo(
					((PresenterContext) context).getAllReceivedMessengerMessages(),
					message);
			view.setMessengerMessageAsReply(message, messageToReplyTo);
		}
		else {
			view.setMessengerMessage(message);
		}
	}

	@Subscribe
	public void onEvent(SpeechRequestMessage message) {
		requireNonNull(message);

		PresenterContext presenterContext = (PresenterContext) context;
		presenterContext.getSpeechRequests().add(message);

		view.addSpeechRequest(message);
	}

	@Subscribe
	public void onEvent(SpeechCancelMessage message) {
		requireNonNull(message);

		PresenterContext presenterContext = (PresenterContext) context;
		presenterContext.getSpeechRequests().removeIf(m -> Objects.equals(
				m.getRequestId(), message.getRequestId()));

		view.removeSpeechRequest(message);

		ParticipantView participantView = unregisterParticipantView(message.getRequestId());

		if (nonNull(participantView)) {
			view.removeParticipantView(participantView);
		}
	}

	@Subscribe
	public void onEvent(CoursePresenceMessage message) {
		PresenterContext presenterContext = (PresenterContext) context;

		var participant = new CourseParticipant(message.getUserId(),
				message.getFirstName(), message.getFamilyName(),
				message.getCoursePresenceType(),
				message.getCourseParticipantType());

		if (CoursePresence.isConnected(message.getCoursePresence())) {
			presenterContext.getCourseParticipants().removeIf(
					p -> p.getUserId().equals(participant.getUserId()));
			presenterContext.getCourseParticipants().add(participant);

			view.addParticipant(participant);
		}
		else {
			presenterContext.getCourseParticipants().remove(participant);

			view.removeParticipant(participant);
		}
	}

	@Subscribe
	public void onEvent(PeerStateEvent event) {
		ExecutableState state = event.getState();
		PresenterContext presenterContext = (PresenterContext) context;
		StreamConfiguration streamConfig = presenterContext.getConfiguration().getStreamConfig();
		JanusParticipantContext participantContext = event.getParticipantContext();

		if (state == ExecutableState.Starting) {
			ParticipantView participantView = viewFactory.getInstance(ParticipantView.class);
			participantView.setState(event.getState());
			participantView.setParticipantContext(participantContext);

			registerParticipantView(participantView, participantContext);

			UUID requestId = participantContext.getRequestId();
			if (nonNull(requestId)) {
				// Kick only participants who initiated a speech request.
				participantView.setOnKick(() -> streamService.stopPeerConnection(participantContext));
			}

			participantContext.setTalkingActivityConsumer(talking -> {
				ParticipantVideoLayout layout = streamConfig.getParticipantVideoLayout();

				// Do not change active participant in gallery layout.
				if (layout == ParticipantVideoLayout.GALLERY) {
					return;
				}
				if (talking) {
					participantViewCollection.setActiveParticipant(participantView);

					view.setParticipantViews(participantViewCollection, layout);
				}
			});

			participantViewCollection.addParticipant(participantView);

			view.addParticipantView(participantView);
		}
		else if (state == ExecutableState.Started) {
			ParticipantView participantView = getParticipantView(participantContext);

			if (nonNull(participantView)) {
				participantView.setState(state);
			}
		}
		else if (state == ExecutableState.Stopped) {
			ParticipantView participantView = unregisterParticipantView(participantContext);

			if (nonNull(participantView)) {
				participantViewCollection.removeParticipant(participantView);

				view.removeParticipantView(participantView);
			}
		}
	}

	@Subscribe
	public void onEvent(LocalScreenVideoFrameEvent event) {
		screenViewContext.addScreenVideoFrameEvent(event);
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
	public void onEvent(ExternalParticipantsViewEvent event) {
		if (event.isEnabled()) {
			if (event.isShow()) {
				viewShowExternalParticipants(event.isPersistent());
			}
			else {
				view.hideExternalParticipants();
			}
		}
		else {
			viewHideExternalParticipants(event.isPersistent());
		}
	}

	@Subscribe
	public void onEvent(ExternalParticipantVideoViewEvent event) {
		if (event.isEnabled()) {
			if (event.isShow()) {
				viewShowExternalParticipantVideo(event.isPersistent());
			}
			else {
				view.hideExternalParticipantVideo();
			}
		}
		else {
			viewHideExternalParticipantVideo(event.isPersistent());
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
	public void onEvent(ExternalNotesViewEvent event) {
		if (event.isEnabled()) {
			if (event.isShow()) {
				viewShowExternalNotes(event.isPersistent());
			}
			else {
				view.hideExternalNotes();
			}
		}
		else {
			viewHideExternalNotes(event.isPersistent());
		}
	}

	@Subscribe
	public void onEvent(ExternalSlideNotesViewEvent event) {
		if (event.isEnabled()) {
			if (event.isShow()) {
				viewShowExternalSlideNotes(event.isPersistent());
			}
			else {
				view.hideExternalSlideNotes();
			}
		}
		else {
			viewHideExternalSlideNotes(event.isPersistent());
		}
	}

	@Subscribe
	public void onEvent(MessageBarPositionEvent event) {
		final MessageBarPosition position = event.position();

		view.setMessageBarPosition(position);

		getPresenterConfig().getSlideViewConfiguration().setMessageBarPosition(position);
	}

	@Subscribe
	public void onEvent(ParticipantsPositionEvent event) {
		final ParticipantsPosition position = event.position();

		view.setParticipantsPosition(position);

		getPresenterConfig().getSlideViewConfiguration().setParticipantsPosition(position);
	}

	@Subscribe
	public void onEvent(ParticipantVideoPositionEvent event) {
		final ParticipantVideoPosition position = event.position();

		view.setParticipantVideoPosition(position);

		getPresenterConfig().getSlideViewConfiguration().setParticipantVideoPosition(position);
	}

	@Subscribe
	public void onEvent(PreviewPositionEvent event) {
		view.setPreviewPosition(event.position());
	}

	@Subscribe
	public void onEvent(NotesBarPositionEvent event) {
		final SlideNotesPosition position = event.position();

		view.setNotesPosition(position);

		getPresenterConfig().getSlideViewConfiguration().setSlideNotesPosition(position);
	}

	@Subscribe
	public void onEvent(SlideNotesBarPositionEvent event) {
		final NoteSlidePosition position = event.position();

		view.setNoteSlidePosition(position);

		getPresenterConfig().getSlideViewConfiguration().setNoteSlidePosition(position);
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
			textFont.setSize(textFont.getSize() / toolController.getViewTransform().getScaleX());

			lastFocusedTextBox.setTextFont(textFont);
		}
	}

	private void registerParticipantView(ParticipantView view, JanusParticipantContext context) {
		participantViewMap.put(context, view);
	}

	private ParticipantView unregisterParticipantView(JanusParticipantContext context) {
		return participantViewMap.remove(context);
	}

	private ParticipantView unregisterParticipantView(UUID requestId) {
		for (var entry : participantViewMap.entrySet()) {
			if (Objects.equals(requestId, entry.getKey().getRequestId())) {
				// Found by request ID.
				ParticipantView participantView = entry.getValue();
				participantViewMap.remove(entry.getKey());
				return participantView;
			}
		}
		return null;
	}

	private ParticipantView getParticipantView(JanusParticipantContext context) {
		return participantViewMap.get(context);
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

	private void externalParticipantsPositionChanged(ExternalWindowPosition position) {
		final ExternalWindowConfiguration config = getExternalParticipantsConfig();

		config.setPosition(position.getPosition());
		config.setScreen(position.getScreen());
	}

	private void externalParticipantsSizeChanged(Dimension size) {
		getExternalParticipantsConfig().setSize(size);
	}

	private void externalParticipantsClosed() {
		eventBus.post(new ExternalParticipantsViewEvent(false));
	}

	private void externalParticipantVideoPositionChanged(ExternalWindowPosition position) {
		final ExternalWindowConfiguration config = getExternalParticipantVideoConfig();

		config.setPosition(position.getPosition());
		config.setScreen(position.getScreen());
	}

	private void externalParticipantVideoSizeChanged(Dimension size) {
		getExternalParticipantVideoConfig().setSize(size);
	}

	private void externalParticipantVideoClosed() {
		view.setParticipantViews(participantViewCollection, ParticipantVideoLayout.GALLERY);

		eventBus.post(new ExternalParticipantVideoViewEvent(false));
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

	private void externalNotesPositionChanged(ExternalWindowPosition position) {
		final ExternalWindowConfiguration config = getExternalNotesConfig();

		config.setPosition(position.getPosition());
		config.setScreen(position.getScreen());
	}

	private void externalNotesSizeChanged(Dimension size) {
		getExternalNotesConfig().setSize(size);
	}

	private void externalNotesClosed() {
		eventBus.post(new ExternalNotesViewEvent(false));
	}

	private void externalSlideNotesPositionChanged(ExternalWindowPosition position) {
		final ExternalWindowConfiguration config = getExternalSlideNotesConfig();

		config.setPosition(position.getPosition());
		config.setScreen(position.getScreen());
	}

	private void externalSlideNotesSizeChanged(Dimension size) {
		getExternalSlideNotesConfig().setSize(size);
	}

	private void externalSlideNotesClosed() {
		eventBus.post(new ExternalSlideNotesViewEvent(false));
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

	private void onAcceptSpeech(SpeechBaseMessage message) {
		// Create and configure a participant context for the speech publisher.
		// BigInteger '-1' is temporarily used as the peerId to identify the remote participant.
		JanusParticipantContext pContext = new JanusParticipantContext();
		pContext.setPeerId(BigInteger.valueOf(-1));
		pContext.setRequestId(message.getRequestId());
		pContext.setDisplayName(message.getFullName());

		streamService.acceptSpeechRequest(pContext);

		PresenterContext presenterContext = (PresenterContext) context;
		presenterContext.getSpeechRequests().remove(message);

		view.acceptSpeechRequest(message);

		onEvent(new PeerStateEvent(pContext, ExecutableState.Starting));
	}

	private void onRejectSpeech(SpeechBaseMessage message) {
		streamService.rejectSpeechRequest(message);

		PresenterContext presenterContext = (PresenterContext) context;
		presenterContext.getSpeechRequests().remove(message);

		view.removeSpeechRequest(message);

		unregisterParticipantView(message.getRequestId());
	}

	private void onBan(CourseParticipant user) {
		if (isNull(user)) {
			return;
		}
		if (user.getUserId().equals(userPrivilegeService.getUserInfo().getUserId())) {
			return;
		}
		if (userPrivilegeService.hasPrivilege("PARTICIPANTS_BAN")){
			streamService.ban(user);
		}
	}

	private void onDiscardMessage(MessengerMessage message) {
		PresenterContext presenterContext = (PresenterContext) context;
		presenterContext.getMessengerMessages().remove(message);
	}

	private void onCreateMessageSlide(MessengerMessage message) {
		onDiscardMessage(message);

		PresenterContext presenterContext = (PresenterContext) context;
		DocumentTemplateConfiguration templateConfig = presenterContext.getConfiguration()
				.getTemplateConfig().getChatMessageTemplateConfig();
		String template = templateConfig.getTemplatePath();
		Rectangle2D templateBounds = templateConfig.getBounds();

		try {
			File file = new File(nonNull(template) ? template : "");

			Document messageDocument = new MessageDocument(file, templateBounds,
					context.getDictionary(), message.getMessage().getText());

			Document prevMessageDocument = null;

			for (Document doc : documentService.getDocuments().asList()) {
				if (doc.isMessage() && doc instanceof MessageDocument) {
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

	private void toolChanged(ToolType toolType) {
		this.toolType = toolType;

		setFocusedTeXView(null);

		view.removeAllPageObjectViews();

		loadPageObjectViews(view.getPage());
	}

	private void pageObjectViewClosed(PageObjectView<? extends Shape> objectView) {
		view.removePageObjectView(objectView);

		// Remove associated shape from the page.
		Shape shape = objectView.getPageShape();

		Page page = view.getPage();
		page.removeShape(shape);

		if (shape instanceof TextShape textShape) {
			// TODO: make this generic or remove at all
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
		Class<? extends Shape> shapeClass = pageObjectRegistry.getShapeClass(ToolType.TEXT);

		if (nonNull(shapeClass) && shapeClass.isAssignableFrom(objectView.getPageShape().getClass())) {
			lastFocusedTextBox = (TextBoxView) objectView;
		}

		// Set latex text.
		shapeClass = pageObjectRegistry.getShapeClass(ToolType.LATEX);

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

	private void stopScreenShare() {
		eventBus.post(new ScreenShareEndEvent());
	}

	private void sendMessage(String text) {
		Message message = new Message(text);

		try {
			webService.sendChatMessage("public", message);
		}
		catch (Exception e) {
			logException(e, "Send chat-message failed");
		}
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
		//	documentService.selectPage(page);

		// Ignore all previous tasks.
		if (nonNull(idleTimer)) {
			idleTimer.stop();
		}

		// Select page with a delay to prevent unwanted page changes.
		Integer delay = getPresenterConfig().getPageSelectionDelay();
		delay = nonNull(delay) ? Math.abs(delay) : 0;

		idleTimer = new SelectionIdleTimer(page, delay);
		idleTimer.runIdleTask();
	}

	private void firstPage() {
		documentService.selectPage(0);
	}

	private void lastPage() {
		Document doc = documentService.getDocuments().getSelectedDocument();

		documentService.selectPage(doc.getPageCount() - 1);
	}

	private void nextPage() {
		documentService.selectNextPage();
	}

	private void tenPagesForward() {
		Document doc = documentService.getDocuments().getSelectedDocument();
		int pageNumber = Math.min(doc.getCurrentPageNumber() + 10, doc.getPageCount() - 1);

		documentService.selectPage(pageNumber);
	}

	private void previousPage() {
		documentService.selectPreviousPage();
	}

	private void tenPagesBack() {
		Document doc = documentService.getDocuments().getSelectedDocument();
		int pageNumber = Math.max(doc.getCurrentPageNumber() - 10, 0);

		documentService.selectPage(pageNumber);
	}

	private void overlayStart() {
		Document doc = documentService.getDocuments().getSelectedDocument();
		Page page = doc.getCurrentPage();

		if (page.isOverlay()) {
			Page lastOverlay = null;
			var listIter = doc.getPages().listIterator(doc.getPageIndex(page));

			while (listIter.hasPrevious()) {
				Page previous = listIter.previous();
				if (!previous.isOverlay() && nonNull(lastOverlay)) {
					documentService.selectPage(lastOverlay);
					break;
				}

				lastOverlay = previous;
			}
		}
	}

	private void overlayEnd() {
		Document doc = documentService.getDocuments().getSelectedDocument();
		Page page = doc.getCurrentPage();

		if (page.isOverlay()) {
			Page lastOverlay = null;
			var listIter = doc.getPages().listIterator(doc.getPageIndex(page));

			while (listIter.hasNext()) {
				Page next = listIter.next();
				if (!next.isOverlay() && nonNull(lastOverlay)) {
					documentService.selectPage(lastOverlay);
					break;
				}

				lastOverlay = next;
			}
		}
	}

	private void overlayPreviousPage() {
		Document doc = documentService.getDocuments().getSelectedDocument();
		Page page = doc.getCurrentPage();

		if (page.isOverlay()) {
			var listIter = doc.getPages().listIterator(doc.getPageIndex(page));

			while (listIter.hasPrevious()) {
				Page previous = listIter.previous();
				if (!previous.isOverlay()) {
					documentService.selectPage(previous);
					break;
				}
			}
		}
	}

	private void overlayNextPage() {
		Document doc = documentService.getDocuments().getSelectedDocument();
		Page page = doc.getCurrentPage();

		if (page.isOverlay()) {
			var listIter = doc.getPages().listIterator(doc.getPageIndex(page));

			while (listIter.hasNext()) {
				Page next = listIter.next();
				if (!next.isOverlay()) {
					documentService.selectPage(next);
					break;
				}
			}
		}
	}

	private void bookmarkSlide() {
        try {
			bookmarkCreated(bookmarkService.createDefaultBookmark());
        }
		catch (BookmarkException e) {
			handleException(e, "Create bookmark failed", "bookmark.assign.warning", "bookmark.exists");
        }
    }

	private void bookmarkGotoLastSlide() {
		Document doc = documentService.getDocuments().getSelectedDocument();
		Bookmark bookmark = bookmarkService.getBookmarks().getLastBookmark(doc);

		if (nonNull(bookmark)) {
            try {
                bookmarkService.gotoBookmark(bookmark);
            }
			catch (BookmarkException e) {
				handleException(e, "Go to bookmark failed", "bookmark.goto.error");
            }
        }
	}

	private void bookmarkCreated(Bookmark bookmark) {
		String shortcut = bookmark.getShortcut().toUpperCase();
		String message = MessageFormat.format(context.getDictionary().get("bookmark.created"), shortcut);

		context.showNotificationPopup(message);
		close();
	}

	private void timerStart() {
		PresenterContext pContext = (PresenterContext) context;
		try {
			pContext.getStopwatch().start();
		}
		catch (ExecutableException e) {
			throw new RuntimeException(e);
		}
	}

	private void timerPause() {
		PresenterContext pContext = (PresenterContext) context;
        try {
            pContext.getStopwatch().suspend();
        }
		catch (ExecutableException e) {
            throw new RuntimeException(e);
        }
    }

	private void timerReset() {
		PresenterContext pContext = (PresenterContext) context;
		pContext.getStopwatch().reset();
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

		view.clearNotesViewContainer();
		//A page can have multiple notes
		for(String note : page.getNotes()){
			view.setNotesText(note);
		}

		view.setSlideNotes(page, parameter);
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

	private ExternalWindowConfiguration getExternalParticipantsConfig() {
		return getPresenterConfig().getExternalParticipantsConfig();
	}

	private ExternalWindowConfiguration getExternalParticipantVideoConfig() {
		return getPresenterConfig().getExternalParticipantVideoConfig();
	}

	private ExternalWindowConfiguration getExternalSlidePreviewConfig() {
		return getPresenterConfig().getExternalSlidePreviewConfig();
	}

	private ExternalWindowConfiguration getExternalNotesConfig() {
		return getPresenterConfig().getExternalNotesConfig();
	}

	private ExternalWindowConfiguration getExternalSlideNotesConfig() {
		return getPresenterConfig().getExternalSlideNotesConfig();
	}

	@Override
	public void initialize() {
		stylusHandler = new StylusHandler(toolController, () -> {
			// Cancel a page selection task.
			if (nonNull(idleTimer)) {
				idleTimer.stop();
				idleTimer = null;

				// Tell the view to keep the currently selected page.
				Page page = documentService.getDocuments().getSelectedDocument().getCurrentPage();
				PresentationParameterProvider ppProvider = context.getPagePropertyProvider(ViewType.User);
				PresentationParameter parameter = ppProvider.getParameter(page);

				view.setPage(page, parameter);
			}
		});

		pageObjectRegistry.register(ToolType.TEXT, TextBoxView.class);
		pageObjectRegistry.register(ToolType.LATEX, TeXBoxView.class);

		eventBus.register(this);

		final PresenterContext ctx = (PresenterContext) context;
		final PresenterConfiguration config = getPresenterConfig();
		final DisplayConfiguration displayConfig = config.getDisplayConfig();
		final WhiteboardConfiguration wbConfig = config.getWhiteboardConfig();

		// Set default tool.
		toolController.selectPenTool();

		pageEditedListener = (event) -> {
			switch (event.getType()) {
				case SHAPE_ADDED -> pageShapeAdded(event.getShape());
				case CLEAR -> setPage(event.getPage());
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

		wbConfig.showGridOnDisplaysProperty().addListener((observable, oldValue, newValue) -> {
			// Update grid parameter.
			PresentationParameterProvider pProvider = context.getPagePropertyProvider(ViewType.Presentation);

			if (Boolean.FALSE.equals(newValue)) {
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

		config.getStreamConfig().participantVideoLayoutProperty().addListener((o, oldValue, newValue) -> {
			participantViewCollection.clearActiveParticipants();

			view.setParticipantViews(participantViewCollection, newValue);
		});

//		config.useMouseInputProperty().addListener((o, oldValue, newValue) -> {
//			setUseMouse(newValue);
//		});

		view.setScrollHandler(this::onScrollEvent);

		if (OsInfo.isMacOs()) {
			setUseMouse(true);
		}
		else {
			setUseMouse(config.getUseMouseInput());
		}

		view.setOnExternalMessagesPositionChanged(this::externalMessagesPositionChanged);
		view.setOnExternalMessagesSizeChanged(this::externalMessagesSizeChanged);
		view.setOnExternalMessagesClosed(this::externalMessagesClosed);
		initExternalScreenBehavior(getExternalMessagesConfig(),
				(enabled, show) -> eventBus.post(new ExternalMessagesViewEvent(enabled, show)));

		view.setOnExternalParticipantsPositionChanged(this::externalParticipantsPositionChanged);
		view.setOnExternalParticipantsSizeChanged(this::externalParticipantsSizeChanged);
		view.setOnExternalParticipantsClosed(this::externalParticipantsClosed);
		initExternalScreenBehavior(getExternalParticipantsConfig(),
				(enabled, show) -> eventBus.post(new ExternalParticipantsViewEvent(enabled, show)));

		view.setOnExternalParticipantVideoPositionChanged(this::externalParticipantVideoPositionChanged);
		view.setOnExternalParticipantVideoSizeChanged(this::externalParticipantVideoSizeChanged);
		view.setOnExternalParticipantVideoClosed(this::externalParticipantVideoClosed);
		initExternalScreenBehavior(getExternalParticipantVideoConfig(),
				(enabled, show) -> eventBus.post(new ExternalParticipantVideoViewEvent(enabled, show)));

		view.setOnExternalSlidePreviewPositionChanged(this::externalSlidePreviewPositionChanged);
		view.setOnExternalSlidePreviewSizeChanged(this::externalSlidePreviewSizeChanged);
		view.setOnExternalSlidePreviewClosed(this::externalSlidePreviewClosed);
		initExternalScreenBehavior(getExternalSlidePreviewConfig(),
				(enabled, show) -> eventBus.post(new ExternalSlidePreviewViewEvent(enabled, show)));

		view.setOnExternalNotesPositionChanged(this::externalNotesPositionChanged);
		view.setOnExternalNotesSizeChanged(this::externalNotesSizeChanged);
		view.setOnExternalNotesClosed(this::externalNotesClosed);
		initExternalScreenBehavior(getExternalNotesConfig(),
				(enabled, show) -> eventBus.post(new ExternalNotesViewEvent(enabled, show)));

		view.setOnExternalSlideNotesPositionChanged(this::externalSlideNotesPositionChanged);
		view.setOnExternalSlideNotesSizeChanged(this::externalSlideNotesSizeChanged);
		view.setOnExternalSlideNotesClosed(this::externalSlideNotesClosed);
		initExternalScreenBehavior(getExternalSlideNotesConfig(),
				(enabled, show) -> eventBus.post(new ExternalSlideNotesViewEvent(enabled, show)));

		view.setPageRenderer(renderController);
		view.setExtendedFullscreen(config.getExtendedFullscreen());
		view.setMessengerState(ExecutableState.Stopped);

		view.setSlideViewConfig(config.getSlideViewConfiguration());
		view.bindShowOutline(ctx.showOutlineProperty());
		view.setOnOutlineItem(this::setOutlineItem);

		view.setOnKeyEvent(this::keyEvent);
		view.setOnStopQuiz(this::stopQuiz);
		view.setOnToggleScreenShare(ctx.screenSharingStartedProperty());
		view.setOnStopScreenShare(this::stopScreenShare);
		view.setOnSendMessage(this::sendMessage);
		view.setOnNewPage(this::newWhiteboardPage);
		view.setOnDeletePage(this::deleteWhiteboardPage);
		view.setOnSelectPage(this::selectPage);
		view.setOnSelectDocument(this::selectDocument);
		view.setOnViewTransform(this::setViewTransform);

		view.setOnAcceptSpeech(this::onAcceptSpeech);
		view.setOnRejectSpeech(this::onRejectSpeech);
		view.setOnBan(this::onBan);
		view.setOnDiscardMessage(this::onDiscardMessage);
		view.setOnCreateMessageSlide(this::onCreateMessageSlide);

		// Register shortcuts that are associated with the SlideView.
		registerShortcut(Shortcut.SLIDE_FIRST, this::firstPage);
		registerShortcut(Shortcut.SLIDE_LAST, this::lastPage);

		registerShortcut(Shortcut.SLIDE_NEXT_DOWN, this::nextPage);
		registerShortcut(Shortcut.SLIDE_NEXT_PAGE_DOWN, this::nextPage);
		registerShortcut(Shortcut.SLIDE_NEXT_RIGHT, this::nextPage);
		registerShortcut(Shortcut.SLIDE_NEXT_SPACE, this::nextPage);
		registerShortcut(Shortcut.SLIDE_NEXT_10, this::tenPagesForward);

		registerShortcut(Shortcut.SLIDE_PREVIOUS_LEFT, this::previousPage);
		registerShortcut(Shortcut.SLIDE_PREVIOUS_PAGE_UP, this::previousPage);
		registerShortcut(Shortcut.SLIDE_PREVIOUS_UP, this::previousPage);
		registerShortcut(Shortcut.SLIDE_PREVIOUS_BACK_SPACE, this::previousPage);
		registerShortcut(Shortcut.SLIDE_PREVIOUS_10, this::tenPagesBack);

		registerShortcut(Shortcut.SLIDE_OVERLAY_START, this::overlayStart);
		registerShortcut(Shortcut.SLIDE_OVERLAY_END, this::overlayEnd);
		registerShortcut(Shortcut.SLIDE_OVERLAY_PREVIOUS, this::overlayPreviousPage);
		registerShortcut(Shortcut.SLIDE_OVERLAY_NEXT, this::overlayNextPage);

		registerShortcut(Shortcut.BOOKMARK_SLIDE, this::bookmarkSlide);
		registerShortcut(Shortcut.BOOKMARK_GOTO_LAST, this::bookmarkGotoLastSlide);

		registerShortcut(Shortcut.TIMER_START, this::timerStart);
		registerShortcut(Shortcut.TIMER_PAUSE, this::timerPause);
		registerShortcut(Shortcut.TIMER_RESET, this::timerReset);

		registerShortcut(Shortcut.COPY_OVERLAY, this::copyOverlay);
		registerShortcut(Shortcut.COPY_OVERLAY_NEXT_PAGE_CTRL, this::copyNextOverlay);

		view.setMessageBarPosition(getPresenterConfig()
				.getSlideViewConfiguration().getMessageBarPosition());

		view.setNotesPosition(getPresenterConfig()
				.getSlideViewConfiguration().getSlideNotesPosition());

		view.setNoteSlidePosition(getPresenterConfig()
				.getSlideViewConfiguration().getNoteSlidePosition());

		view.setParticipantsPosition(getPresenterConfig()
				.getSlideViewConfiguration().getParticipantsPosition());

		view.setParticipantVideoPosition(getPresenterConfig()
				.getSlideViewConfiguration().getParticipantVideoPosition());

		view.setPreviewPosition(getPresenterConfig()
				.getSlideViewConfiguration().getSlidePreviewPosition());

		try {
			recordingService.init();

			documentRecorder.setHasChangesProperty(ctx.hasRecordedChangesProperty());
			documentRecorder.start();
		}
		catch (ExecutableException e) {
			throw new RuntimeException(e);
		}
	}

	private void setUseMouse(boolean useMouse) {
		if (useMouse) {
			view.createMouseInput(toolController);
		}
		else {
			view.createStylusInput(stylusHandler);
		}
	}

	private void onScrollEvent(ScrollHandler.ScrollEvent e) {
		if (e.deltaY() < 0) {
			previousPage();
		}
		else {
			nextPage();
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

				checkScreenExists(config);

				action.accept(config.isEnabled(), true);
			}
		});
	}

	private void showExternalScreens() {
		SlideViewConfiguration viewConfig = getPresenterConfig().getSlideViewConfiguration();

		if (viewConfig.getMessageBarPosition() == MessageBarPosition.EXTERNAL) {
			showExternalScreen(getExternalMessagesConfig(),
					(enabled, show) -> eventBus.post(new ExternalMessagesViewEvent(enabled, show)));
		}
		if (viewConfig.getParticipantsPosition() == ParticipantsPosition.EXTERNAL) {
			showExternalScreen(getExternalParticipantsConfig(),
					(enabled, show) -> eventBus.post(new ExternalParticipantsViewEvent(enabled, show)));
		}
		if (viewConfig.getParticipantVideoPosition() == ParticipantVideoPosition.EXTERNAL) {
			showExternalScreen(getExternalParticipantVideoConfig(),
					(enabled, show) -> eventBus.post(new ExternalParticipantVideoViewEvent(enabled, show)));
		}
		if (viewConfig.getSlidePreviewPosition() == SlidePreviewPosition.EXTERNAL) {
			showExternalScreen(getExternalSlidePreviewConfig(),
					(enabled, show) -> eventBus.post(new ExternalSlidePreviewViewEvent(enabled, show)));
		}
		if (viewConfig.getSlideNotesPosition() == SlideNotesPosition.EXTERNAL) {
			showExternalScreen(getExternalNotesConfig(),
					(enabled, show) -> eventBus.post(new ExternalNotesViewEvent(enabled, show)));
		}
		if (viewConfig.getNoteSlidePosition() == NoteSlidePosition.EXTERNAL) {
			showExternalScreen(getExternalSlideNotesConfig(),
					(enabled, show) -> eventBus.post(new ExternalSlideNotesViewEvent(enabled, show)));
		}
	}

	private void showExternalScreen(ExternalWindowConfiguration config, BiConsumer<Boolean, Boolean> action) {
		checkScreenExists(config);

		action.accept(config.isEnabled(), true);
	}

	private void hideExternalScreens() {
		view.hideExternalSlidePreview();
		view.hideExternalMessages();
	}

	private void checkScreenExists(ExternalWindowConfiguration config) {
		final ObservableList<Screen> screens = presentationController.getScreens();

		if (!checkIfScreenInList(screens, config.getScreen())) {
			config.setScreen(null);
		}
	}

	private void viewShowExternalMessages(boolean persistent) {
		final ExternalWindowConfiguration config = getExternalMessagesConfig();

		if (persistent) {
			config.setEnabled(true);
		}

		checkScreenExists(config);

		view.showExternalMessages(config.getScreen(), config.getPosition(), config.getSize());
	}

	private void viewHideExternalMessages(boolean persistent) {
		final ExternalWindowConfiguration config = getExternalMessagesConfig();

		if (persistent) {
			config.setEnabled(false);
		}

		view.hideExternalMessages();
	}

	private void viewShowExternalParticipants(boolean persistent) {
		final ExternalWindowConfiguration config = getExternalParticipantsConfig();

		if (persistent) {
			config.setEnabled(true);
		}

		checkScreenExists(config);

		view.showExternalParticipants(config.getScreen(), config.getPosition(), config.getSize());
	}

	private void viewHideExternalParticipants(boolean persistent) {
		final ExternalWindowConfiguration config = getExternalParticipantsConfig();

		if (persistent) {
			config.setEnabled(false);
		}

		view.hideExternalParticipants();
	}

	private void viewShowExternalParticipantVideo(boolean persistent) {
		final ExternalWindowConfiguration config = getExternalParticipantVideoConfig();

		if (persistent) {
			config.setEnabled(true);
		}

		checkScreenExists(config);

		view.showExternalParticipantVideo(config.getScreen(), config.getPosition(), config.getSize());
	}

	private void viewHideExternalParticipantVideo(boolean persistent) {
		final ExternalWindowConfiguration config = getExternalParticipantVideoConfig();

		if (persistent) {
			config.setEnabled(false);
		}

		view.hideExternalParticipantVideo();
	}

	private void viewShowExternalSlidePreview(boolean persistent) {
		final ExternalWindowConfiguration config = getExternalSlidePreviewConfig();

		if (persistent) {
			config.setEnabled(true);
		}

		checkScreenExists(config);

		view.showExternalSlidePreview(config.getScreen(), config.getPosition(), config.getSize());
	}

	private void viewHideExternalSlidePreview(boolean persistent) {
		final ExternalWindowConfiguration config = getExternalSlidePreviewConfig();

		if (persistent) {
			config.setEnabled(false);
		}

		view.hideExternalSlidePreview();
	}

	private void viewShowExternalNotes(boolean persistent) {
		final ExternalWindowConfiguration config = getExternalNotesConfig();

		if (persistent) {
			config.setEnabled(true);
		}

		checkScreenExists(config);

		view.showExternalNotes(config.getScreen(), config.getPosition(), config.getSize());
	}

	private void viewHideExternalNotes(boolean persistent) {
		final ExternalWindowConfiguration config = getExternalNotesConfig();

		if (persistent) {
			config.setEnabled(false);
		}

		view.hideExternalNotes();
	}

	private void viewShowExternalSlideNotes(boolean persistent) {
		final ExternalWindowConfiguration config = getExternalSlideNotesConfig();

		if (persistent) {
			config.setEnabled(true);
		}

		checkScreenExists(config);

		view.showExternalSlideNotes(config.getScreen(), config.getPosition(), config.getSize());
	}

	private void viewHideExternalSlideNotes(boolean persistent) {
		final ExternalWindowConfiguration config = getExternalSlideNotesConfig();

		if (persistent) {
			config.setEnabled(false);
		}

		view.hideExternalSlideNotes();
	}

	private boolean checkIfScreenInList(List<Screen> screens, Screen screen) {
		if (screen == null) {
			return true;
		}
		return screens.stream().anyMatch(s -> s.equals(screen));
	}

	private void recordPage(Page page) {
		if (page.getDocument().isQuiz() && quizState == ExecutableState.Started) {
			// Do not record pages from running quizzes.
			return;
		}

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

	private void loadParticipants() {
		PresenterContext ctx = (PresenterContext) context;
		PresenterConfiguration config = ctx.getConfiguration();

		long courseId = ctx.getCourse().getId();

		ServiceParameters parameters = new ServiceParameters();
		parameters.setUrl(webServiceInfo.getStreamPublisherApiUrl());

		StreamProviderService spService = new StreamProviderService(parameters,
				config.getStreamConfig()::getAccessToken);

		List<CourseParticipant> participants = spService.getParticipants(courseId);

		ctx.getCourseParticipants().addAll(participants);

		view.setParticipants(participants);
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



	private class SelectionIdleTimer extends Timer {

		private final Page page;

		private final int idleTime;

		private TimerTask idleTask;


		SelectionIdleTimer(Page page, int idleTime) {
			this.page = page;
			this.idleTime = idleTime;
		}

		void runIdleTask() {
			idleTask = new TimerTask() {

				@Override
				public void run() {
					documentService.selectPage(page);
				}
			};

			schedule(idleTask, idleTime);
		}

		public void stop() {
			cancel();
			purge();

			idleTask = null;
		}
	}
}

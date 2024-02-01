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

import javax.inject.Inject;

import java.awt.Color;
import java.awt.Desktop;
import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.text.MessageFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import com.google.common.eventbus.Subscribe;

import org.lecturestudio.core.ExecutableException;
import org.lecturestudio.core.ExecutableState;
import org.lecturestudio.core.app.ApplicationContext;
import org.lecturestudio.core.app.configuration.Configuration;
import org.lecturestudio.core.app.dictionary.Dictionary;
import org.lecturestudio.core.audio.AudioDeviceNotConnectedException;
import org.lecturestudio.core.bus.EventBus;
import org.lecturestudio.core.bus.event.*;
import org.lecturestudio.core.controller.ToolController;
import org.lecturestudio.core.model.Document;
import org.lecturestudio.core.model.NotesPosition;
import org.lecturestudio.core.model.Page;
import org.lecturestudio.core.model.RecentDocument;
import org.lecturestudio.core.model.listener.PageEditEvent;
import org.lecturestudio.core.model.listener.ParameterChangeListener;
import org.lecturestudio.core.presenter.AboutPresenter;
import org.lecturestudio.core.presenter.Presenter;
import org.lecturestudio.core.presenter.command.CloseApplicationCommand;
import org.lecturestudio.core.presenter.command.ClosePresenterCommand;
import org.lecturestudio.core.presenter.command.ShowPresenterCommand;
import org.lecturestudio.core.service.DocumentService;
import org.lecturestudio.core.util.FileUtils;
import org.lecturestudio.core.util.ListChangeListener;
import org.lecturestudio.core.util.ObservableList;
import org.lecturestudio.core.view.*;
import org.lecturestudio.presenter.api.config.PresenterConfiguration;
import org.lecturestudio.presenter.api.config.SlideViewConfiguration;
import org.lecturestudio.presenter.api.context.PresenterContext;
import org.lecturestudio.presenter.api.event.ExternalMessagesViewEvent;
import org.lecturestudio.presenter.api.event.ExternalNotesViewEvent;
import org.lecturestudio.presenter.api.event.ExternalSlideNotesViewEvent;
import org.lecturestudio.presenter.api.event.ExternalParticipantsViewEvent;
import org.lecturestudio.presenter.api.event.ExternalSlidePreviewViewEvent;
import org.lecturestudio.presenter.api.event.ExternalSpeechViewEvent;
import org.lecturestudio.presenter.api.event.MessageBarPositionEvent;
import org.lecturestudio.presenter.api.event.MessengerStateEvent;
import org.lecturestudio.presenter.api.event.NotesBarPositionEvent;
import org.lecturestudio.presenter.api.event.SlideNotesBarPositionEvent;
import org.lecturestudio.presenter.api.event.ParticipantsPositionEvent;
import org.lecturestudio.presenter.api.event.PreviewPositionEvent;
import org.lecturestudio.presenter.api.event.QuizStateEvent;
import org.lecturestudio.presenter.api.event.RecordingStateEvent;
import org.lecturestudio.presenter.api.event.RecordingTimeEvent;
import org.lecturestudio.presenter.api.event.StreamReconnectStateEvent;
import org.lecturestudio.presenter.api.event.StreamingStateEvent;
import org.lecturestudio.presenter.api.model.*;
import org.lecturestudio.presenter.api.presenter.command.StopwatchCommand;
import org.lecturestudio.presenter.api.service.BookmarkService;
import org.lecturestudio.presenter.api.service.QuizWebServiceState;
import org.lecturestudio.presenter.api.service.RecordingService;
import org.lecturestudio.presenter.api.service.StreamService;
import org.lecturestudio.presenter.api.view.MenuView;
import org.lecturestudio.presenter.api.view.MessengerWindow;

public class MenuPresenter extends Presenter<MenuView> {

	/** Mainly used for Desktop.getDesktop().open to circumvent errors. */
	private final ExecutorService executorService = Executors.newSingleThreadExecutor();

	private final Map<Class<?>, Object> viewPositionMap = new HashMap<>();

	private final DateTimeFormatter timeFormatter;

	private final Timer timer;

	private final EventBus eventBus;

	private final Stopwatch stopwatch;

	@Inject
	private ToolController toolController;

	@Inject
	private ViewContextFactory viewFactory;

	@Inject
	private BookmarkService bookmarkService;

	@Inject
	private DocumentService documentService;

	@Inject
	private RecordingService recordingService;

	@Inject
	private StreamService streamService;


	@Inject
	MenuPresenter(ApplicationContext context, MenuView view) {
		super(context, view);

		this.eventBus = context.getEventBus();
		this.timeFormatter = DateTimeFormatter.ofPattern("HH:mm", getPresenterConfig().getLocale());
		this.timer = new Timer("MenuTime", true);
		this.stopwatch = ((PresenterContext) this.context).getStopwatch();
	}

	@Subscribe
	public void onEvent(final DocumentEvent event) {
		Document doc = event.closed() ? null : event.getDocument();
		Page page = isNull(doc) ? null : doc.getCurrentPage();

		if (event.selected() && nonNull(page)) {
			page.addPageEditedListener(this::pageEdited);
		}

		view.setDocument(doc);

		pageChanged(page);
	}

	@Subscribe
	public void onEvent(final PageEvent event) {
		final Page page = event.getPage();

		if (event.isRemoved()) {
			page.removePageEditedListener(this::pageEdited);
		}
		else if (event.isSelected()) {
			Page oldPage = event.getOldPage();

			if (nonNull(oldPage)) {
				oldPage.removePageEditedListener(this::pageEdited);
			}

			page.addPageEditedListener(this::pageEdited);

			stopwatch.setRunStopwatch(true);
			pageChanged(page);
		}
	}

	@Subscribe
	public void onEvent(final RecordingStateEvent event) {
		view.setRecordingState(event.getState());
	}

	@Subscribe
	public void onEvent(final RecordingTimeEvent event) {
		view.setRecordingTime(event.getTime());
	}

	@Subscribe
	public void onEvent(final MessengerStateEvent event) {
		view.setMessengerState(event.getState());
	}

	@Subscribe
	public void onEvent(final QuizWebServiceState state) {
		view.setQuizServiceState(state);
	}

	@Subscribe
	public void onEvent(final QuizStateEvent event) {
		view.setQuizState(event.getState());
	}

	@Subscribe
	public void onEvent(final StreamingStateEvent event) {
		view.setStreamingState(event.getState());
	}

	@Subscribe
	public void onEvent(final StreamReconnectStateEvent event) {
		view.setStreamReconnectState(event.getState());
	}

	@Subscribe
	public void onEvent(final ViewVisibleEvent event) {
		Class<? extends View> viewClass = event.getViewClass();
		boolean visible = event.isVisible();

		if (viewClass == MessengerWindow.class) {
			view.setMessengerWindowVisible(visible);
		}
	}

	@Subscribe
	public void onEvent(final SplitSlidesPositionEvent event){
		switch (event.getNotesPosition()){
			case RIGHT -> view.setSplitNotesPositionRight();
			case LEFT -> view.setSplitNotesPositionLeft();
			case NONE -> view.setSplitNotesPositionNone();
		}
	}

	@Subscribe
	public void onEvent(final ExternalMessagesViewEvent event) {
		if (!event.isEnabled()) {
			// Set the previous position.
			MessageBarPosition position = getViewPosition(MessageBarPosition.class);

			if (nonNull(position)) {
				view.setMessagesPosition(position);
			}
		}
	}

	@Subscribe
	public void onEvent(final ExternalParticipantsViewEvent event) {
		if (!event.isEnabled()) {
			// Set the previous position.
			ParticipantsPosition position = getViewPosition(ParticipantsPosition.class);

			if (nonNull(position)) {
				view.setParticipantsPosition(position);
			}
		}
	}

	@Subscribe
	public void onEvent(final ExternalSlidePreviewViewEvent event) {
		if (!event.isEnabled()) {
			// Set the previous position.
			SlidePreviewPosition position = getViewPosition(SlidePreviewPosition.class);

			if (nonNull(position)) {
				view.setSlidePreviewPosition(position);
			}
		}
	}

	@Subscribe
	public void onEvent(final ExternalNotesViewEvent event) {
		if (!event.isEnabled()) {
			// Set the previous position.
			SlideNotesPosition position = getViewPosition(SlideNotesPosition.class);

			if (nonNull(position)) {
				view.setSlideNotesPosition(position);
			}
		}
	}

	@Subscribe
	public void onEvent(final ExternalSlideNotesViewEvent event) {
		if (!event.isEnabled()) {
			// Set the previous position.
			NoteSlidePosition position = getViewPosition(NoteSlidePosition.class);

			if (nonNull(position)) {
				view.setNoteSlidePosition(position);
			}
		}
	}

	public void positionSplitNotes(NotesPosition position){
		documentService.selectNotesPosition(position);
	}

	public void openBookmark(Bookmark bookmark) {
		try {
			bookmarkService.gotoBookmark(bookmark);
		}
		catch (BookmarkKeyException e) {
			context.showError("bookmark.goto.error", "bookmark.key.not.existing", bookmark.getShortcut());
		}
		catch (Exception e) {
			handleException(e, "Go to bookmark failed", "bookmark.goto.error");
		}
	}

	public void openPrevBookmark(){
		Page page = bookmarkService.getPrevBookmarkPage();
		if (nonNull(page)) {
			documentService.selectPage(page);
		}
	}

	public void openNextBookmark(){
		Page page = bookmarkService.getNextBookmarkPage();
		if (nonNull(page)) {
			documentService.selectPage(page);
		}
	}

	public void openDocument(File documentFile) {
		documentService.openDocument(documentFile)
				.exceptionally(throwable -> {
					handleException(throwable, "Open document failed", "open.document.error", documentFile.getPath());
					return null;
				});
	}

	public void closeSelectedDocument() {
		documentService.closeSelectedDocument();
	}

	public void saveDocuments() {
		eventBus.post(new ShowPresenterCommand<>(SaveDocumentsPresenter.class));
	}

	public void exit() {
		eventBus.post(new CloseApplicationCommand());
	}

	public void undo() {
		toolController.undo();
	}

	public void redo() {
		toolController.redo();
	}

	public void showSettingsView() {
		eventBus.post(new ShowPresenterCommand<>(SettingsPresenter.class));
	}

	public void customizeToolbar() {
		eventBus.post(new CustomizeToolbarEvent());
	}

	public void positionSpeech(SpeechPosition position) {
		if (position == SpeechPosition.EXTERNAL) {
			eventBus.post(new ExternalSpeechViewEvent(true));
		}
		else {
			setViewPosition(SpeechPosition.class, position);
		}
	}

	public void positionMessages(MessageBarPosition position) {
		if (position == MessageBarPosition.EXTERNAL) {
			eventBus.post(new ExternalMessagesViewEvent(true));
		}
		else {
			setViewPosition(MessageBarPosition.class, position);

			eventBus.post(new MessageBarPositionEvent(position));
		}
	}

	public void positionParticipants(ParticipantsPosition position) {
		if (position == ParticipantsPosition.EXTERNAL) {
			eventBus.post(new ExternalParticipantsViewEvent(true));
		}
		else {
			setViewPosition(ParticipantsPosition.class, position);

			eventBus.post(new ParticipantsPositionEvent(position));
		}
	}

	public void positionSlidePreview(SlidePreviewPosition position) {
		if (position == SlidePreviewPosition.EXTERNAL) {
			eventBus.post(new ExternalSlidePreviewViewEvent(true));
		}
		else {
			setViewPosition(SlidePreviewPosition.class, position);

			eventBus.post(new PreviewPositionEvent(position));
		}
	}

	public void positionSlideNotes(SlideNotesPosition position) {
		if (position == SlideNotesPosition.EXTERNAL) {
			eventBus.post(new ExternalNotesViewEvent(true));
		}
		else {
			setViewPosition(SlideNotesPosition.class, position);

			eventBus.post(new NotesBarPositionEvent(position));
		}
	}

	public void positionNoteSlide(NoteSlidePosition position) {
		if (position == NoteSlidePosition.EXTERNAL) {
			eventBus.post(new ExternalSlideNotesViewEvent(true));
		}
		else {
			setViewPosition(NoteSlidePosition.class, position);

			eventBus.post(new SlideNotesBarPositionEvent(position));
		}
	}

	public void newWhiteboard() {
		PresenterConfiguration config = getPresenterConfig();
		String template = config.getTemplateConfig()
				.getWhiteboardTemplateConfig().getTemplatePath();

		documentService.addWhiteboard(template);
	}

	public void newWhiteboardPage() {
		documentService.createWhiteboardPage();
	}

	public void deleteWhiteboardPage() {
		documentService.deleteWhiteboardPage();
	}

	public void showGrid(boolean show) {
		toolController.toggleGrid();
	}

	public void startRecording() {
		try {
			if (recordingService.started()) {
				recordingService.suspend();
			}
			else {
				recordingService.start();
			}
		}
		catch (ExecutableException e) {
			Throwable cause = nonNull(e.getCause()) ? e.getCause().getCause() : null;

			if (cause instanceof AudioDeviceNotConnectedException ex) {
				context.showError("recording.start.error", "recording.start.device.error", ex.getDeviceName());
			}
			else {
				handleException(e, "Start recording failed", "recording.start.error");
			}
		}
	}

	public void stopRecording() {
		PresenterConfiguration config = getPresenterConfig();

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

	public void showMessengerWindow(boolean show) {
		if (show) {
			eventBus.post(new ShowPresenterCommand<>(MessengerWindowPresenter.class));
		}
		else {
			eventBus.post(new ClosePresenterCommand(MessengerWindowPresenter.class));
		}
	}

	public void selectQuiz() {
		eventBus.post(new ShowPresenterCommand<>(SelectQuizPresenter.class));
	}

	public void newQuiz() {
		eventBus.post(new ShowPresenterCommand<>(CreateQuizPresenter.class));
	}

	public void closeQuiz() {
		streamService.stopQuiz();
	}


	public void pauseStopwatch(){
		stopwatch.startStopStopwatch();
	}

	public void resetStopwatch(){
		stopwatch.resetStopwatch();
		view.setCurrentStopwatch(stopwatch.calculateCurrentStopwatch());
	}
	public void clearBookmarks() {
		bookmarkService.clearBookmarks();
	}

	public void newBookmark() {
		eventBus.post(new ShowPresenterCommand<>(CreateBookmarkPresenter.class));
	}

	public void newDefaultBookmark() {
		try {
			bookmarkCreated(bookmarkService.createDefaultBookmark());
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

	public void removeBookmark() {
		try {
			if (nonNull(bookmarkService.getPageBookmark())) {
				String shortcut = bookmarkService.getPageBookmark().getShortcut();
				bookmarkService.deleteBookmark(bookmarkService.getPageBookmark());
				bookmarkRemoved(shortcut);
			}
		}
		catch (BookmarkException e) {
			handleException(e, "Remove bookmark failed", "bookmark.assign.warning");
		}
	}
	private void bookmarkRemoved(String shortcut) {
		String message = MessageFormat.format(context.getDictionary().get("bookmark.removed"), shortcut);

		context.showNotificationPopup(message);
		close();
	}


	private void bookmarkCreated(Bookmark bookmark) {
		String shortcut = bookmark.getShortcut().toUpperCase();
		String message = MessageFormat.format(context.getDictionary().get("bookmark.created"), shortcut);

		context.showNotificationPopup(message);
		close();
	}

	public void gotoBookmark() {
		eventBus.post(new ShowPresenterCommand<>(GotoBookmarkPresenter.class));
	}

	public void previousBookmark() {
		bookmarkService.gotoPreviousBookmark();
	}

	public void showLog() {
		// Run async to avoid 'CoInitializeEx() failed.' with Desktop.getDesktop().open
		CompletableFuture.runAsync(() -> {
			try {
				Desktop.getDesktop().open(new File(context.getDataLocator()
						.getAppDataPath()));
			}
			catch (IOException e) {
				handleException(e, "Open log path failed", "generic.error");
			}
		}, executorService);
	}

	public void showAboutView() {
		eventBus.post(new ShowPresenterCommand<>(AboutPresenter.class));
	}

	private void selectNewDocument() {
		final String pathContext = PresenterContext.SLIDES_CONTEXT;
		Configuration config = getPresenterConfig();
		Dictionary dict = context.getDictionary();
		Map<String, String> contextPaths = config.getContextPaths();
		Path dirPath = FileUtils.getContextPath(config, pathContext);

		FileChooserView fileChooser = viewFactory.createFileChooserView();
		fileChooser.setInitialDirectory(dirPath.toFile());
		fileChooser.addExtensionFilter(dict.get("file.description.pdf"),
				PresenterContext.SLIDES_EXTENSION);

		File selectedFile = fileChooser.showOpenFile(view);

		if (nonNull(selectedFile)) {
			contextPaths.put(pathContext, selectedFile.getParent());

			openDocument(selectedFile);
		}
	}

	private void pageChanged(Page page) {
		PresentationParameter parameter = null;

		if (nonNull(page)) {
			PresentationParameterProvider ppProvider = context.getPagePropertyProvider(ViewType.User);
			parameter = ppProvider.getParameter(page);
		}

		view.setPage(page, parameter);
	}

	private void pageEdited(final PageEditEvent event) {
		if (event.shapedChanged()) {
			return;
		}

		// Update undo/redo etc. items.
		Page page = event.getPage();
		PresentationParameter parameter = null;

		if (nonNull(page)) {
			PresentationParameterProvider ppProvider = context.getPagePropertyProvider(ViewType.User);
			parameter = ppProvider.getParameter(page);
		}

		view.setPage(page, parameter);
	}

	private PresenterConfiguration getPresenterConfig() {
		return (PresenterConfiguration) context.getConfiguration();
	}

	@SuppressWarnings("unchecked")
	private <T> T getViewPosition(Class<T> cls) {
		return (T) viewPositionMap.getOrDefault(cls, null);
	}

	private <T> void setViewPosition(Class<T> cls, T value) {
		viewPositionMap.put(cls, value);
	}

	@Override
	public void initialize() {
		final PresenterContext presenterContext = (PresenterContext) context;
		final PresenterConfiguration config = getPresenterConfig();
		final SlideViewConfiguration slideViewConfig = config.getSlideViewConfiguration();

		eventBus.register(this);

		setViewPosition(MessageBarPosition.class, slideViewConfig.getMessageBarPosition());
		setViewPosition(ParticipantsPosition.class, slideViewConfig.getParticipantsPosition());
		setViewPosition(SlidePreviewPosition.class, slideViewConfig.getSlidePreviewPosition());
		setViewPosition(SlideNotesPosition.class, slideViewConfig.getSlideNotesPosition());
		setViewPosition(NoteSlidePosition.class, slideViewConfig.getNoteSlidePosition());
		setViewPosition(SpeechPosition.class, slideViewConfig.getSpeechPosition());

		view.setRecordingState(ExecutableState.Stopped);
		view.setMessengerState(ExecutableState.Stopped);
		view.setStreamingState(ExecutableState.Stopped);
		view.setQuizState(ExecutableState.Stopped);

		view.bindCourseParticipantsCount(presenterContext.courseParticipantsCountProperty());
		view.bindMessageCount(presenterContext.messageCountProperty());
		view.bindSpeechRequestCount(presenterContext.speechRequestCountProperty());

		view.setDocument(null);
		view.setPage(null, null);

		view.setOnOpenDocument(this::selectNewDocument);
		view.setOnOpenDocument(this::openDocument);
		view.setOnCloseDocument(this::closeSelectedDocument);
		view.setOnSaveDocuments(this::saveDocuments);
		view.setOnExit(this::exit);

		view.setOnUndo(this::undo);
		view.setOnRedo(this::redo);
		view.setOnSettings(this::showSettingsView);

		view.bindShowOutline(presenterContext.showOutlineProperty());
		view.bindFullscreen(presenterContext.fullscreenProperty());

		view.setOnCustomizeToolbar(this::customizeToolbar);

		view.setSpeechPosition(slideViewConfig.getSpeechPosition());
		view.setOnSpeechPosition(this::positionSpeech);

		view.setMessagesPosition(slideViewConfig.getMessageBarPosition());
		view.setOnMessagesPosition(this::positionMessages);

		view.setSlideNotesPosition(slideViewConfig.getSlideNotesPosition());
		view.setOnSlideNotesPosition(this::positionSlideNotes);

		view.setNoteSlidePosition(slideViewConfig.getNoteSlidePosition());
		view.setOnNoteSlidePosition(this::positionNoteSlide);

		view.setParticipantsPosition(slideViewConfig.getParticipantsPosition());
		view.setOnParticipantsPosition(this::positionParticipants);

		view.setSlidePreviewPosition(slideViewConfig.getSlidePreviewPosition());
		view.setOnSlidePreviewPosition(this::positionSlidePreview);

		view.setOnNewWhiteboard(this::newWhiteboard);
		view.setOnNewWhiteboardPage(this::newWhiteboardPage);
		view.setOnDeleteWhiteboardPage(this::deleteWhiteboardPage);
		view.setOnShowGrid(this::showGrid);

		view.setOnStartRecording(this::startRecording);
		view.setOnStopRecording(this::stopRecording);
		view.bindEnableStream(presenterContext.streamStartedProperty());
		view.bindViewStream(presenterContext.viewStreamProperty());
		view.bindEnableStreamingMicrophone(config.getStreamConfig().enableMicrophoneProperty());
		view.bindEnableStreamingCamera(config.getStreamConfig().enableCameraProperty());
		view.bindEnableMessenger(presenterContext.messengerStartedProperty());
		view.setOnShowMessengerWindow(this::showMessengerWindow);
		view.setOnShowSelectQuizView(this::selectQuiz);
		view.setOnShowNewQuizView(this::newQuiz);
		view.setOnCloseQuiz(this::closeQuiz);
		view.setOnPauseStopwatch(this::pauseStopwatch);
		view.setOnResetStopwatch(this::resetStopwatch);
		view.setCurrentStopwatch(this::pauseStopwatch);
		view.setOnConfigStopwatch(this::startStopwatchConfiguration);

		view.setOnClearBookmarks(this::clearBookmarks);
		view.setOnShowNewBookmarkView(this::newBookmark);
		view.setOnRemoveBookmarkView(this::removeBookmark);
		view.setOnCreateNewDefaultBookmarkView(this::newDefaultBookmark);
		view.setOnShowGotoBookmarkView(this::gotoBookmark);
		view.setOnPreviousBookmark(this::previousBookmark);
		view.setOnPrevBookmark(this::openPrevBookmark);
		view.setOnNextBookmark(this::openNextBookmark);
		view.setOnOpenBookmark(this::openBookmark);

		view.setOnOpenLog(this::showLog);
		view.setOnOpenAbout(this::showAboutView);

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
				view.setPage(page, parameter);
			}
		});

		// Set file menu.
		ObservableList<RecentDocument> recentDocs = getPresenterConfig().getRecentDocuments();

		// Add new (sorted) recent document items.
		if (!recentDocs.isEmpty()) {
			Iterator<RecentDocument> iter = recentDocs.iterator();

			while (iter.hasNext()) {
				final String path = iter.next().getDocumentPath();

				File file = new File(path);
				if (!file.exists()) {
					// Skip and remove missing document.
					iter.remove();
				}
			}

			view.setRecentDocuments(recentDocs);
		}

		// Subscribe to document changes.
		recentDocs.addListener(new ListChangeListener<>() {

			@Override
			public void listChanged(ObservableList<RecentDocument> list) {
				view.setRecentDocuments(list);
			}

		});

		// Set bookmarks menu.
		Bookmarks bookmarks = bookmarkService.getBookmarks();
		bookmarks.addBookmarksListener(new BookmarksListener() {

			@Override
			public void bookmarksChanged(Bookmarks bookmarks) {
				view.setBookmarks(bookmarks);
			}

		});

		view.setBookmarks(bookmarks);

		// Update current time every 30 seconds.
		timer.scheduleAtFixedRate(new TimerTask() {

			@Override
			public void run() {
				view.setCurrentTime(LocalDateTime.now().format(timeFormatter));
			}
		}, 0, 30000);

		timer.scheduleAtFixedRate(new TimerTask() {

			@Override
			public void run() {
				stopwatch.updateStopwatchInterval();
				view.setCurrentStopwatch(stopwatch.calculateCurrentStopwatch());
				//Timer blinks 5times when the time ran out
				if(stopwatch.isTimerEnded()) {
					if (stopwatch.getTimerEndedInterval() % 2 == 0) {
						view.setCurrentStopwatchBackgroundColor(Color.WHITE);
					} else {
						view.setCurrentStopwatchBackgroundColor(Color.RED);
					}
				}
			}
		}, 0, 1000);
		view.setOnSplitNotesPositionNone(() -> positionSplitNotes(NotesPosition.NONE));
		view.setOnSplitNotesPositionRight(() -> positionSplitNotes(NotesPosition.RIGHT));
		view.setOnSplitNotesPositionLeft(() -> positionSplitNotes(NotesPosition.LEFT));

	}

	public void startStopwatchConfiguration() {
		eventBus.post(new StopwatchCommand(() -> {
			stopwatch.stopStopwatch();
			view.setCurrentStopwatch(stopwatch.calculateCurrentStopwatch());
		}));
	}
}

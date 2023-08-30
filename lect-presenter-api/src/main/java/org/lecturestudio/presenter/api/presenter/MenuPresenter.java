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

import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Iterator;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javax.inject.Inject;

import org.lecturestudio.core.ExecutableException;
import org.lecturestudio.core.ExecutableState;
import org.lecturestudio.core.app.ApplicationContext;
import org.lecturestudio.core.app.configuration.Configuration;
import org.lecturestudio.core.app.dictionary.Dictionary;
import org.lecturestudio.core.audio.AudioDeviceNotConnectedException;
import org.lecturestudio.core.beans.ObjectProperty;
import org.lecturestudio.core.bus.EventBus;
import org.lecturestudio.core.bus.event.CustomizeToolbarEvent;
import org.lecturestudio.core.bus.event.DocumentEvent;
import org.lecturestudio.core.bus.event.PageEvent;
import org.lecturestudio.core.bus.event.ViewVisibleEvent;
import org.lecturestudio.core.controller.ToolController;
import org.lecturestudio.core.model.Document;
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
import org.lecturestudio.presenter.api.event.*;
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
			if(page.isOverlay())
				showNotificationPopup("Overlay!");
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
	public void onEvent(final ExternalMessagesViewEvent event) {
		view.setExternalMessages(event.isEnabled(), event.isShow());
	}

	@Subscribe
	public void onEvent(final ExternalParticipantsViewEvent event) {
		view.setExternalParticipants(event.isEnabled(), event.isShow());
	}

	@Subscribe
	public void onEvent(final ExternalSlidePreviewViewEvent event) {
		view.setExternalSlidePreview(event.isEnabled(), event.isShow());
	}

	@Subscribe
	public void onEvent(final ExternalSpeechViewEvent event) {
		view.setExternalSpeech(event.isEnabled(), event.isShow());
	}

	@Subscribe
	public void onEvent(final ExternalNotesViewEvent event) {
		view.setExternalNotes(event.isEnabled(), event.isShow());
	}

	public void openBookmark(Bookmark bookmark) {
		try {
			bookmarkService.gotoBookmark(bookmark);
		}
		catch (BookmarkKeyException e) {
			showError("bookmark.goto.error", "bookmark.key.not.existing", bookmark.getShortcut());
		}
		catch (Exception e) {
			handleException(e, "Go to bookmark failed", "bookmark.goto.error");
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

	public void externalMessages(boolean selected) {
		eventBus.post(new ExternalMessagesViewEvent(selected));
	}

	public void externalParticipants(boolean selected) {
		eventBus.post(new ExternalParticipantsViewEvent(selected));
	}

	public void externalSlidePreview(boolean selected) {
		eventBus.post(new ExternalSlidePreviewViewEvent(selected));
	}

	public void externalSpeech(boolean selected) {
		eventBus.post(new ExternalSpeechViewEvent(selected));
	}

	public void externalNotes(boolean selected) {
		eventBus.post(new ExternalNotesViewEvent(selected));
	}

	public void positionMessages(MessageBarPosition position) {
		eventBus.post(new MessageBarPositionEvent(position));
	}

	public void positionNotes(NoteBarPosition position) {
		eventBus.post(new NotesBarPositionEvent(position));
	}

	public void positionParticipants(MessageBarPosition position) {
		eventBus.post(new ParticipantsPositionEvent(position));
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
				showError("recording.start.error", "recording.start.device.error", ex.getDeviceName());
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

	@Override
	public void initialize() {
		final PresenterContext presenterContext = (PresenterContext) context;
		final PresenterConfiguration config = getPresenterConfig();
		final SlideViewConfiguration slideViewConfig = config.getSlideViewConfiguration();

		eventBus.register(this);

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

		view.setOnExternalMessages(this::externalMessages);
		view.setOnExternalParticipants(this::externalParticipants);
		view.setOnExternalSlidePreview(this::externalSlidePreview);
		view.setOnExternalSpeech(this::externalSpeech);
		view.setOnExternalNotes(this::externalNotes);

		switch (slideViewConfig.getMessageBarPosition()) {
			case LEFT -> view.setMessagesPositionLeft();
			case BOTTOM -> view.setMessagesPositionBottom();
			case RIGHT -> view.setMessagesPositionRight();
		}

		view.setOnMessagesPositionLeft(() -> positionMessages(MessageBarPosition.LEFT));
		view.setOnMessagesPositionBottom(() -> positionMessages(MessageBarPosition.BOTTOM));
		view.setOnMessagesPositionRight(() -> positionMessages(MessageBarPosition.RIGHT));

		switch (slideViewConfig.getNotesBarPosition()) {
			case LEFT -> view.setNotesPositionLeft();
			case BOTTOM -> view.setNotesPositionBottom();
		}

		view.setOnNotesPositionLeft(() -> positionNotes(NoteBarPosition.LEFT));
		view.setOnNotesPositionBottom(() -> positionNotes(NoteBarPosition.BOTTOM));

		switch (slideViewConfig.getParticipantsPosition()) {
			case LEFT -> view.setParticipantsPositionLeft();
			case RIGHT -> view.setParticipantsPositionRight();
		}

		view.setOnParticipantsPositionLeft(() -> positionParticipants(MessageBarPosition.LEFT));
		view.setOnParticipantsPositionRight(() -> positionParticipants(MessageBarPosition.RIGHT));

		ObjectProperty<MessageBarPosition> previewPosition = slideViewConfig.previewPositionProperty();
		previewPosition.addListener((o, oldPos, newPos) -> {
			eventBus.post(new PreviewPositionEvent(newPos));
		});

		view.bindPreviewPosition(previewPosition);

		view.setOnNewWhiteboard(this::newWhiteboard);
		view.setOnNewWhiteboardPage(this::newWhiteboardPage);
		view.setOnDeleteWhiteboardPage(this::deleteWhiteboardPage);
		view.setOnShowGrid(this::showGrid);

		view.setOnStartRecording(this::startRecording);
		view.setOnStopRecording(this::stopRecording);
		view.bindEnableStream(presenterContext.streamStartedProperty());
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
		view.setOnShowGotoBookmarkView(this::gotoBookmark);
		view.setOnPreviousBookmark(this::previousBookmark);
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
	}

	public void startStopwatchConfiguration() {
		eventBus.post(new StopwatchCommand(() -> {
			stopwatch.stopStopwatch();
			view.setCurrentStopwatch(stopwatch.calculateCurrentStopwatch());
		}));
	}
}

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

import java.awt.Desktop;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.nio.file.Path;
import java.text.MessageFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;

import javax.imageio.ImageIO;
import javax.inject.Inject;

import org.lecturestudio.core.ExecutableException;
import org.lecturestudio.core.ExecutableState;
import org.lecturestudio.core.app.ApplicationContext;
import org.lecturestudio.core.app.configuration.Configuration;
import org.lecturestudio.core.app.dictionary.Dictionary;
import org.lecturestudio.core.audio.AudioDeviceNotConnectedException;
import org.lecturestudio.core.bus.EventBus;
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
import org.lecturestudio.core.util.DesktopUtils;
import org.lecturestudio.core.util.FileUtils;
import org.lecturestudio.core.util.ListChangeListener;
import org.lecturestudio.core.util.NetUtils;
import org.lecturestudio.core.util.ObservableList;
import org.lecturestudio.core.view.FileChooserView;
import org.lecturestudio.core.view.PresentationParameter;
import org.lecturestudio.core.view.PresentationParameterProvider;
import org.lecturestudio.core.view.View;
import org.lecturestudio.core.view.ViewContextFactory;
import org.lecturestudio.core.view.ViewType;
import org.lecturestudio.presenter.api.config.PresenterConfiguration;
import org.lecturestudio.presenter.api.context.PresenterContext;
import org.lecturestudio.presenter.api.event.*;
import org.lecturestudio.presenter.api.model.Bookmark;
import org.lecturestudio.presenter.api.model.BookmarkKeyException;
import org.lecturestudio.presenter.api.model.Bookmarks;
import org.lecturestudio.presenter.api.model.BookmarksListener;
import org.lecturestudio.presenter.api.pdf.embedded.QuizParser;
import org.lecturestudio.presenter.api.presenter.command.ShowSettingsCommand;
import org.lecturestudio.presenter.api.service.*;
import org.lecturestudio.presenter.api.view.MenuView;
import org.lecturestudio.presenter.api.view.MessengerWindow;
import org.lecturestudio.web.api.message.MessengerMessage;
import org.lecturestudio.web.api.model.Message;
import org.lecturestudio.web.api.model.quiz.Quiz;
import org.lecturestudio.web.api.service.DLZMessageService;

public class MenuPresenter extends Presenter<MenuView> {

	private final DateTimeFormatter timeFormatter;

	private final Timer timer;

	private final EventBus eventBus;

	private final QuizParser quizParser;


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
	private WebService webService;

	@Inject
	private DLZService dlzService;

	@Inject
	MenuPresenter(ApplicationContext context, MenuView view) {
		super(context, view);

		this.eventBus = context.getEventBus();
		this.quizParser = new QuizParser();
		this.timeFormatter = DateTimeFormatter.ofPattern("HH:mm", context.getConfiguration().getLocale());
		this.timer = new Timer("MenuTime", true);
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
	public void onEvent(final MessageWebServiceState state) {
		view.setMessageServiceState(state);
	}

	@Subscribe
	public void onEvent(final MessengerStateEvent event) {
		view.setMessengerState(event.getState());
	}

	@Subscribe
	public void onEvent(final DLZStateEvent event){
		view.setDLZState(event.getState());
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
	public void onEvent(final ViewVisibleEvent event) {
		Class<? extends View> viewClass = event.getViewClass();
		boolean visible = event.isVisible();

		if (viewClass == MessengerWindow.class) {
			view.setMessengerWindowVisible(visible);
		}
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

	public void openPageURI(URI uri) {
		try {
			DesktopUtils.browseURI(uri);
		}
		catch (Exception e) {
			handleException(e, "Open page uri failed", "error", "open.page.uri.error");
		}
	}

	public void openPageFileLink(File file) {
		File fileLink = file;

		if (!fileLink.exists()) {
			// Try relative path of the opened document.
			Document selectedDoc = documentService.getDocuments().getSelectedDocument();
			String docPath = selectedDoc.getFilePath();

			if (nonNull(docPath) && !docPath.isEmpty()) {
				docPath = docPath.substring(0, docPath.lastIndexOf(File.separator));

				fileLink = new File(docPath + File.separator + file.getPath());

				if (!fileLink.exists()) {
					fileLink = new File(docPath + File.separator + file.getName());
				}
			}
		}

		try {
			DesktopUtils.openFile(fileLink);
		}
		catch (Exception e) {
			handleException(e, "Open page file link failed", "error", "open.page.file.error");
		}
	}

	public void openPageQuiz(Quiz quiz) {
		CompletableFuture.runAsync(() -> {
			try {
				webService.startQuiz(quiz);
			}
			catch (ExecutableException e) {
				throw new CompletionException(e);
			}
		})
		.exceptionally(e -> {
			handleServiceError(e, "Start quiz failed", "quiz.start.error");
			return null;
		});
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

	public void saveQuizResults() {
		eventBus.post(new ShowPresenterCommand<>(SaveQuizResultsPresenter.class));
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

	public void setAdvancedSettings(boolean selected) {
		context.getConfiguration().setAdvancedUIMode(selected);
	}

	public void newWhiteboard() {
		documentService.addWhiteboard();
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

	public void toggleRecording(boolean start) {
		if (start) {
			startRecording();
		}
		else {
			stopRecording();
		}
	}

	public void toggleStreaming(boolean start) {
		if (start) {
			startStreaming();
		}
		else {
			stopStreaming();
		}
	}

	public void toggleMessenger(boolean start) {
		if (start) {
			startMessenger();
		}
		else {
			stopMessenger();
		}
	}

	public void toggleDLZ(boolean start){
		if(start) {
			startDLZ();
		}
		else{
			stopDLZ();
		}
	}

	public void toggleCamera(boolean start) {
		if (start) {
			startCamera();
		}
		else {
			stopCamera();
		}
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

			if (cause instanceof AudioDeviceNotConnectedException) {
				var ex = (AudioDeviceNotConnectedException) cause;
				showError("recording.start.error", "recording.start.device.error", ex.getDeviceName());
				logException(e, "Start recording failed");
			}
			else {
				handleException(e, "Start recording failed", "recording.start.error");
			}
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

	public void startStreaming() {
		CompletableFuture.runAsync(() -> {
			try {
				streamService.start();
				PresenterConfiguration config = (PresenterConfiguration) context.getConfiguration();
				if(config.getDlzRoom() != null) {
					String link = "http://127.0.0.1/stream";
					org.lecturestudio.web.api.service.DLZSendMessageService.SendTextMessage("Live-Stream wurde gestartet " + link , config.getDlzRoom().getId());
				}
			}
			catch (ExecutableException e) {
				throw new CompletionException(e);
			}
		})
		.exceptionally(e -> {
			handleServiceError(e, "Start stream failed", "stream.start.error");
			return null;
		});
	}

	public void stopStreaming() {
		CompletableFuture.runAsync(() -> {
			try {
				streamService.stop();
			}
			catch (ExecutableException e) {
				throw new CompletionException(e);
			}
		})
		.exceptionally(e -> {
			handleServiceError(e, "Stop stream failed", "stream.stop.error");
			return null;
		});
	}

	public void startMessenger() {
		CompletableFuture.runAsync(() -> {
			try {
				webService.startMessenger();
			}
			catch (ExecutableException e) {
				throw new CompletionException(e);
			}
		})
		.exceptionally(e -> {
			handleServiceError(e, "Start messenger failed", "messenger.start.error");
			return null;
		});
	}

	public void stopMessenger() {
		CompletableFuture.runAsync(() -> {
			try {
				webService.stopMessenger();
			}
			catch (ExecutableException e) {
				throw new CompletionException(e);
			}
		})
		.exceptionally(e -> {
			handleServiceError(e, "Stop messenger failed", "messenger.stop.error");
			return null;
		});
	}

	public void showMessengerWindow(boolean show) {
		if (show) {
			eventBus.post(new ShowPresenterCommand<>(MessengerWindowPresenter.class));
		}
		else {
			eventBus.post(new ClosePresenterCommand(MessengerWindowPresenter.class));
		}
	}

	public void startDLZ(){
		DLZMessageService.active = true;
		PresenterConfiguration config = (PresenterConfiguration) context.getConfiguration();
		context.getEventBus().post(new DLZStateEvent(ExecutableState.Starting));
		dlzService.start();
		//org.lecturestudio.web.api.service.DLZSendMessageService.SendTextMessage("Test aus lectstudio" , config.getDlzRoom().getId());
		context.getEventBus().post(new DLZStateEvent(ExecutableState.Started));
		/*try {
			InputStream test;
			test = org.lecturestudio.web.api.service.DLZPictureService.getPic();
			BufferedImage imBuff = ImageIO.read(test);
			System.out.println(imBuff);


			MessengerMessage messengerMessage = new MessengerMessage();
			messengerMessage.setDate(new Date());
			messengerMessage.setImage(imBuff);
			messengerMessage.setMessage(new Message("Test"));

			context.getEventBus().post(messengerMessage);

			showNotificationPopup("DLZ Bild");
		}
		catch (Exception e){
			e.printStackTrace();
		}*/
	}

	public void stopDLZ(){
		DLZMessageService.active = false;
		System.out.println("DLZ-Chat gestoppt");
		context.getEventBus().post(new DLZStateEvent(ExecutableState.Stopping));
		dlzService.stop();
		context.getEventBus().post(new DLZStateEvent(ExecutableState.Stopped));
	}

	public void showDLZWindow(boolean show){
		if (show) {
			eventBus.post(new ShowPresenterCommand<>(MessengerWindowPresenter.class));
		}
		else {
			eventBus.post(new ClosePresenterCommand(MessengerWindowPresenter.class));
		}
	}

	public void startCamera() {
		try {
			streamService.startCameraStream();
		}
		catch (ExecutableException e) {
			handleException(e, "Start camera stream failed", "stream.start.error");
		}
	}

	public void stopCamera() {
		try {
			streamService.stopCameraStream();
		}
		catch (ExecutableException e) {
			handleException(e, "Stop camera stream failed", "stream.start.error");
		}
	}

	public void selectQuiz() {
		eventBus.post(new ShowPresenterCommand<>(SelectQuizPresenter.class));
	}

	public void newQuiz() {
		eventBus.post(new ShowPresenterCommand<>(CreateQuizPresenter.class));
	}

	public void closeQuiz() {
		CompletableFuture.runAsync(() -> {
			try {
				webService.stopQuiz();
			}
			catch (ExecutableException e) {
				throw new CompletionException(e);
			}
		})
		.exceptionally(e -> {
			handleServiceError(e, "Stop quiz failed", "quiz.stop.error");
			return null;
		});
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
		try {
			Desktop.getDesktop().open(new File(
					context.getDataLocator().getAppDataPath()));
		}
		catch (IOException e) {
			handleException(e, "Open log path failed", "generic.error");
		}
	}

	public void showAboutView() {
		eventBus.post(new ShowPresenterCommand<>(AboutPresenter.class));
	}

	public void showCameraSettings() {
		eventBus.post(new ShowSettingsCommand("camera"));
	}

	public void showMessengerSettings() {
		eventBus.post(new ShowSettingsCommand("web-service"));
	}

	public void showDLZSettings(){
		System.out.println("DLZ-Chat Fenster angezeigt");
	}

	public void showRecordingSettings() {
		eventBus.post(new ShowSettingsCommand("recording"));
	}

	public void showStreamingSettings() {
		eventBus.post(new ShowSettingsCommand("live-stream"));
	}

	private void selectNewDocument() {
		final String pathContext = PresenterContext.SLIDES_CONTEXT;
		Configuration config = context.getConfiguration();
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
		List<Quiz> embeddedQuizzes = null;
		List<URI> embeddedUriActions = null;
		List<File> embeddedFileActions = null;

		if (nonNull(page)) {
			PresentationParameterProvider ppProvider = context.getPagePropertyPropvider(ViewType.User);
			parameter = ppProvider.getParameter(page);

			String text = page.getPageText();
			embeddedQuizzes = quizParser.parse(text);

			embeddedUriActions = page.getUriActions();
			embeddedFileActions = page.getLaunchActions();

		}

		view.setPage(page, parameter);
		view.setPageFileLinks(embeddedFileActions);
		view.setPageURIs(embeddedUriActions);
		view.setPageQuizzes(embeddedQuizzes);
	}

	private void pageEdited(final PageEditEvent event) {
		if (event.shapedChanged()) {
			return;
		}

		// Update undo/redo etc. items.
		Page page = event.getPage();
		PresentationParameter parameter = null;

		if (nonNull(page)) {
			PresentationParameterProvider ppProvider = context.getPagePropertyPropvider(ViewType.User);
			parameter = ppProvider.getParameter(page);
		}

		view.setPage(page, parameter);
	}

	@Override
	public void initialize() {
		PresenterContext ctx = (PresenterContext) context;
		Configuration config = context.getConfiguration();

		eventBus.register(this);

		view.setRecordingState(ExecutableState.Stopped);
		view.setMessengerState(ExecutableState.Stopped);
		view.setStreamingState(ExecutableState.Stopped);
		view.setQuizState(ExecutableState.Stopped);

		view.setDocument(null);
		view.setPage(null, null);

		view.setOnOpenDocument(this::selectNewDocument);
		view.setOnOpenDocument(this::openDocument);
		view.setOnCloseDocument(this::closeSelectedDocument);
		view.setOnSaveDocuments(this::saveDocuments);
		view.setOnSaveQuizResults(this::saveQuizResults);
		view.setOnExit(this::exit);

		view.setOnUndo(this::undo);
		view.setOnRedo(this::redo);
		view.setOnSettings(this::showSettingsView);

		view.bindShowOutline(ctx.showOutlineProperty());
		view.setAdvancedSettings(config.getAdvancedUIMode());
		view.bindFullscreen(ctx.fullscreenProperty());
		view.setOnAdvancedSettings(this::setAdvancedSettings);

		view.setOnNewWhiteboard(this::newWhiteboard);
		view.setOnNewWhiteboardPage(this::newWhiteboardPage);
		view.setOnDeleteWhiteboardPage(this::deleteWhiteboardPage);
		view.setOnShowGrid(this::showGrid);

		view.setOnStartRecording(this::startRecording);
		view.setOnStopRecording(this::stopRecording);
		view.setOnStartStreaming(this::startStreaming);
		view.setOnStopStreaming(this::stopStreaming);
		view.setOnStartMessenger(this::startMessenger);
		view.setOnStopMessenger(this::stopMessenger);
		view.setOnShowMessengerWindow(this::showMessengerWindow);
		view.setOnShowSelectQuizView(this::selectQuiz);
		view.setOnShowNewQuizView(this::newQuiz);
		view.setOnCloseQuiz(this::closeQuiz);
		view.setOnStartDLZ(this::startDLZ);
		view.setOnStopDLZ(this::stopDLZ);
		view.setOnShowDLZWindow(this::showDLZWindow);

		view.setOnControlCamera(this::toggleCamera);
		view.setOnControlCameraSettings(this::showCameraSettings);
		view.setOnControlMessenger(this::toggleMessenger);
		view.setOnControlMessengerSettings(this::showMessengerSettings);
		view.setOnControlMessengerWindow(this::showMessengerWindow);
		view.setOnControlRecording(this::toggleRecording);
		view.setOnControlRecordingSettings(this::showRecordingSettings);
		view.setOnControlStreaming(this::toggleStreaming);
		view.setOnControlStreamingSettings(this::showStreamingSettings);
		view.setOnControlDLZ(this::toggleDLZ);
		view.setOnControlDLZSettings(this::showDLZSettings);
		view.setOnControlDLZWindow(this::showDLZWindow);

		view.setOnClearBookmarks(this::clearBookmarks);
		view.setOnShowNewBookmarkView(this::newBookmark);
		view.setOnShowGotoBookmarkView(this::gotoBookmark);
		view.setOnPreviousBookmark(this::previousBookmark);
		view.setOnOpenBookmark(this::openBookmark);

		view.setOnOpenPageFileLink(this::openPageFileLink);
		view.setOnOpenPageURI(this::openPageURI);
		view.setOnOpenPageQuiz(this::openPageQuiz);

		view.setOnOpenLog(this::showLog);
		view.setOnOpenAbout(this::showAboutView);

		// Bind configuration.
		context.getConfiguration().advancedUIModeProperty().addListener((observable, oldValue, newValue) -> {
			view.setAdvancedSettings(newValue);
		});

		// Register for page parameter change updates.
		PresentationParameterProvider ppProvider = context.getPagePropertyPropvider(ViewType.User);
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
		ObservableList<RecentDocument> recentDocs = context.getConfiguration().getRecentDocuments();

		// Add new (sorted) recent document items.
		if (recentDocs.size() > 0) {
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
	}

	private void handleServiceError(Throwable error, String errorMessage, String title) {
		String message = null;

		if (NetUtils.isSocketTimeout(error.getCause())) {
			message = "service.timeout.error";
		}

		handleException(error, errorMessage, title, message);
	}
}

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

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.function.Predicate;

import javax.inject.Inject;

import org.lecturestudio.core.ExecutableException;
import org.lecturestudio.core.ExecutableState;
import org.lecturestudio.core.app.ApplicationContext;
import org.lecturestudio.core.app.util.SaveConfigurationHandler;
import org.lecturestudio.core.audio.AudioSystemProvider;
import org.lecturestudio.core.beans.BooleanProperty;
import org.lecturestudio.core.bus.event.DocumentEvent;
import org.lecturestudio.core.bus.event.ViewVisibleEvent;
import org.lecturestudio.core.controller.PresentationController;
import org.lecturestudio.core.geometry.Rectangle2D;
import org.lecturestudio.core.input.KeyEvent;
import org.lecturestudio.core.model.Document;
import org.lecturestudio.core.model.NotesPosition;
import org.lecturestudio.core.net.MediaType;
import org.lecturestudio.core.presenter.NotificationPresenter;
import org.lecturestudio.core.presenter.Presenter;
import org.lecturestudio.core.presenter.command.CloseApplicationCommand;
import org.lecturestudio.core.presenter.command.ClosePresenterCommand;
import org.lecturestudio.core.presenter.command.ShowPresenterCommand;
import org.lecturestudio.core.service.DocumentService;
import org.lecturestudio.core.util.ObservableHashMap;
import org.lecturestudio.core.util.ObservableMap;
import org.lecturestudio.core.util.ShutdownHandler;
import org.lecturestudio.core.view.Action;
import org.lecturestudio.core.view.NotificationPopupManager;
import org.lecturestudio.core.view.NotificationPopupView;
import org.lecturestudio.core.view.NotificationType;
import org.lecturestudio.core.view.View;
import org.lecturestudio.core.view.ViewContextFactory;
import org.lecturestudio.core.view.ViewHandler;
import org.lecturestudio.core.view.ViewLayer;
import org.lecturestudio.presenter.api.config.PresenterConfiguration;
import org.lecturestudio.presenter.api.config.StreamConfiguration;
import org.lecturestudio.presenter.api.context.PresenterContext;
import org.lecturestudio.presenter.api.event.MessengerStateEvent;
import org.lecturestudio.presenter.api.event.QuizStateEvent;
import org.lecturestudio.presenter.api.event.RecordingStateEvent;
import org.lecturestudio.presenter.api.event.ScreenShareEndEvent;
import org.lecturestudio.presenter.api.event.ScreenShareSelectEvent;
import org.lecturestudio.presenter.api.event.StreamReconnectStateEvent;
import org.lecturestudio.presenter.api.event.StreamingStateEvent;
import org.lecturestudio.presenter.api.handler.AudioDeviceChangeHandler;
import org.lecturestudio.presenter.api.handler.CheckVersionHandler;
import org.lecturestudio.presenter.api.handler.MicrophoneMuteHandler;
import org.lecturestudio.presenter.api.handler.PresenterHandler;
import org.lecturestudio.presenter.api.handler.ScreenShareHandler;
import org.lecturestudio.presenter.api.handler.StreamHandler;
import org.lecturestudio.presenter.api.handler.ViewStreamHandler;
import org.lecturestudio.presenter.api.handler.shutdown.ActionHandler;
import org.lecturestudio.presenter.api.handler.shutdown.CloseMainViewHandler;
import org.lecturestudio.presenter.api.input.Shortcut;
import org.lecturestudio.presenter.api.presenter.command.StartScreenSharingCommand;
import org.lecturestudio.presenter.api.recording.RecordingBackup;
import org.lecturestudio.presenter.api.service.BookmarkService;
import org.lecturestudio.presenter.api.service.RecordingService;
import org.lecturestudio.presenter.api.service.ScreenShareService;
import org.lecturestudio.presenter.api.service.ScreenSourceService;
import org.lecturestudio.presenter.api.service.StreamService;
import org.lecturestudio.presenter.api.handler.shutdown.SaveDocumentsHandler;
import org.lecturestudio.presenter.api.handler.shutdown.SaveRecordingHandler;
import org.lecturestudio.presenter.api.service.WebService;
import org.lecturestudio.presenter.api.util.ScreenDocumentCreator;
import org.lecturestudio.presenter.api.view.MainView;
import org.lecturestudio.web.api.exception.StreamMediaException;
import org.lecturestudio.web.api.message.StopStreamEnvironmentMessage;

public class MainPresenter extends org.lecturestudio.core.presenter.MainPresenter<MainView> implements ViewHandler {

	private final AudioSystemProvider audioSystemProvider;

	private final ObservableMap<Class<? extends View>, BooleanProperty> viewMap;

	private final Map<KeyEvent, Predicate<KeyEvent>> shortcutMap;

	private final List<ShutdownHandler> shutdownHandlers;

	private final List<Presenter<?>> contexts;

	private final List<PresenterHandler> handlers;

	private final PresentationController presentationController;

	private final NotificationPopupManager popupManager;

	private final ViewContextFactory contextFactory;

	private final DocumentService documentService;

	private final BookmarkService bookmarkService;

	private final RecordingService recordingService;

	private final StreamService streamService;

	private final ScreenSourceService screenSourceService;

	private final ScreenShareService screenShareService;

	private final WebService webService;

	private SlidesPresenter slidesPresenter;

	/** The waiting notification. */
	private NotificationPresenter notificationPresenter;


	@Inject
	MainPresenter(ApplicationContext context, MainView view,
			AudioSystemProvider audioSystemProvider,
			PresentationController presentationController,
			NotificationPopupManager popupManager,
			ViewContextFactory contextFactory,
			DocumentService documentService,
			BookmarkService bookmarkService,
			RecordingService recordingService,
			StreamService streamService,
			ScreenShareService screenShareService,
			WebService webService) {
		super(context, view);

		this.audioSystemProvider = audioSystemProvider;
		this.presentationController = presentationController;
		this.popupManager = popupManager;
		this.contextFactory = contextFactory;
		this.documentService = documentService;
		this.bookmarkService = bookmarkService;
		this.recordingService = recordingService;
		this.streamService = streamService;
		this.screenSourceService = new ScreenSourceService();
		this.screenShareService = screenShareService;
		this.webService = webService;
		this.viewMap = new ObservableHashMap<>();
		this.shortcutMap = new HashMap<>();
		this.contexts = new ArrayList<>();
		this.handlers = new ArrayList<>();
		this.shutdownHandlers = new ArrayList<>();
	}

	@Override
	public void openFile(File file) {
		if (isNull(file)) {
			return;
		}

		showWaitingNotification("open.document");

		documentService.openDocument(file)
			.thenRun(() -> {
				hideWaitingNotification();
			})
			.exceptionally(throwable -> {
				hideWaitingNotification();
				handleException(throwable, "Open document failed",
						"open.document.error", file.getPath());
				return null;
			});
	}

	@Override
	public void setArgs(String[] args) {

	}

	@Override
	public void setOnClose(Action action) {
		super.setOnClose(action);

		addShutdownHandler(new ActionHandler(action));
	}

	@Override
	public void initialize() {
		registerShortcut(Shortcut.CLOSE_VIEW, this::closeView);
		registerShortcut(Shortcut.PAUSE_RECORDING, this::togglePauseRecording);
		registerShortcut(Shortcut.PAUSE_RECORDING_P, this::togglePauseRecording);

		PresenterContext presenterContext = (PresenterContext) context;
		PresenterConfiguration config = (PresenterConfiguration) context.getConfiguration();

		config.setAdvancedUIMode(true);

		addHandler(new AudioDeviceChangeHandler(presenterContext,
				audioSystemProvider, recordingService));
		addHandler(new MicrophoneMuteHandler(presenterContext, recordingService));
		addHandler(new ViewStreamHandler(presenterContext));
		addHandler(new StreamHandler(presenterContext, streamService,
				screenShareService));
		addHandler(new ScreenShareHandler(presenterContext, streamService,
				screenShareService, screenSourceService, documentService,
				recordingService));

		// TODO: create more separate handlers.

		presenterContext.messengerStartedProperty().addListener((o, oldValue, newValue) -> {
			streamService.enableMessenger(newValue);
		});

		addShutdownHandler(new SaveRecordingHandler(presenterContext));
		addShutdownHandler(new SaveDocumentsHandler(presenterContext));
		addShutdownHandler(new SaveConfigurationHandler(presenterContext));
		addShutdownHandler(new CloseMainViewHandler(view));

		context.setFullscreen(config.getStartFullscreen());
		context.fullscreenProperty().addListener((observable, oldValue, newValue) -> {
			setFullscreen(newValue);
		});

		config.extendedFullscreenProperty().addListener((observable, oldValue, newValue) -> {
			view.setMenuVisible(!newValue);
		});

		config.getAudioConfig().recordingFormatProperty().addListener((observable, oldFormat, newFormat) -> {
			recordingService.setAudioFormat(newFormat);
		});

		config.getStreamConfig().enableCameraProperty().addListener((observable, oldValue, newValue) -> {
			streamService.enableStreamCamera(newValue);
		});

		config.powerPlanScreenProperty().addListener((observable, oldValue, newValue) -> {

		});

		slidesPresenter = createPresenter(SlidesPresenter.class);

		if (nonNull(slidesPresenter)) {
			slidesPresenter.initialize();
		}

		view.setMenuVisible(!config.getExtendedFullscreen());
		view.setOnClose(this::closeWindow);
		view.setOnShown(this::onViewShown);
		view.setOnFocus(this::onViewFocus);
		view.setOnBounds(this::onViewBounds);
		view.setOnKeyEvent(this::keyEvent);

		context.getEventBus().register(this);

		createSettingsPresentation();

		addHandler(new CheckVersionHandler(presenterContext));
	}

	@Subscribe
	public void onStreamMediaException(StreamMediaException exception) {
		if (exception.getMediaType() == MediaType.Camera) {
			PresenterConfiguration config = (PresenterConfiguration) context.getConfiguration();
			StreamConfiguration streamConfig = config.getStreamConfig();
			streamConfig.setCameraEnabled(false);

			context.showNotification(NotificationType.WARNING,
					"stream.camera.error.title", "stream.camera.error.message",
					streamConfig.getCameraName());
		}
	}

	@Subscribe
	public void onCommand(CloseApplicationCommand command) {
		closeWindow();
	}

	@Subscribe
	public void onCommand(final ClosePresenterCommand command) {
		destroyHandler(command.getPresenterClass());
	}

	@Subscribe
	public <T extends Presenter<?>> void onCommand(ShowPresenterCommand<T> command) {
		T presenter = findCachedPresenter(command.getPresenterClass());

		if (isNull(presenter)) {
			presenter = createPresenter(command.getPresenterClass());
		}

		try {
			command.execute(presenter);
		}
		catch (Exception e) {
			logException(e, "Execute command failed");
		}

		display(presenter);
	}

	@Subscribe
	public void onEvent(DocumentEvent event) {
		Document doc = event.getDocument();

		if (event.created()) {
			documentCreated(doc);
		}
		else if (event.closed()) {
			documentClosed(doc);
		}
		else if (event.selected()) {
			documentSelected();
		}
	}

	@Subscribe
	public void onEvent(final MessengerStateEvent event) {
		ExecutableState state = event.getState();

//		if (state == ExecutableState.Starting) {
//			showWaitingNotification("messenger.starting");
//		}
//		else if (state == ExecutableState.Started) {
//			hideWaitingNotification();
//
////			display(createPresenter(MessengerWindowPresenter.class));
//		}
//		else
		if (state == ExecutableState.Stopped) {
			PresenterContext presenterContext = (PresenterContext) context;
			presenterContext.getMessengerMessages().clear();
			presenterContext.getAllReceivedMessengerMessages().clear();

//			destroyHandler(MessengerWindowPresenter.class);
		}
	}

	@Subscribe
	public void onEvent(final QuizStateEvent event) {
		ExecutableState state = event.getState();

		if (state == ExecutableState.Starting) {
			showWaitingNotification("quiz.starting");
		}
		else if (state == ExecutableState.Started) {
			hideWaitingNotification();
		}
	}

	@Subscribe
	public void onEvent(final StreamingStateEvent event) {
		ExecutableState state = event.getState();

		if (state == ExecutableState.Starting) {
			showWaitingNotification("stream.starting");
		}
		else if (state == ExecutableState.Started) {
			hideWaitingNotification();
		}
		else if (state == ExecutableState.Stopped) {
			PresenterContext presenterContext = (PresenterContext) context;
			presenterContext.getCourseParticipants().clear();
			presenterContext.getSpeechRequests().clear();
		}
		else if (state == ExecutableState.Error) {
			context.showError("stream.closed.by.remote.host.title", "stream.closed.by.remote.host");
		}
	}

	@Subscribe
	public void onEvent(final StreamReconnectStateEvent event) {
		ExecutableState state = event.getState();

		if (state == ExecutableState.Started) {
			context.getEventBus().post(new ShowPresenterCommand<>(
					ReconnectStreamPresenter.class));
		}
		else if (state == ExecutableState.Stopped) {
			context.getEventBus().post(new ClosePresenterCommand(
					ReconnectStreamPresenter.class));
		}
		else if (state == ExecutableState.Error) {
			context.showError("stream.closed.by.remote.host.title", "stream.closed.by.remote.host");
		}
	}

	@Subscribe
	public void onEvent(final RecordingStateEvent event) {
		if (event.stopped()) {
			stopAllScreenRecordings();
		}
	}

	@Subscribe
	public void onEvent(final ScreenShareSelectEvent event) {
		PresenterContext presenterContext = (PresenterContext) context;

		context.getEventBus()
			.post(new StartScreenSharingCommand((context) -> {
				CompletableFuture.runAsync(() -> {
					try {
						if (presenterContext.getStreamStarted()) {
							streamService.setScreenShareContext(context);
						}

						ScreenDocumentCreator.create(documentService, context.getSource());

						// Register created screen document with the screen source.
						screenSourceService.addScreenShareContext(
								documentService.getDocuments().getSelectedDocument(),
								context);
					}
					catch (Exception e) {
						throw new RuntimeException(e);
					}
				}).exceptionally(e -> {
					handleException(e, "Set screen-source failed",
							"stream.screen.share.error");
					return null;
				});
			}));
	}

	@Subscribe
	public void onEvent(final ScreenShareEndEvent event) {
		PresenterContext presenterContext = (PresenterContext) context;

		if (!presenterContext.getStreamStarted()) {
			stopLocalScreenCapture();
		}
		else {
			streamService.enableScreenSharing(false);
		}

		stopScreenRecording();

		// Remove document.
		Document screenDoc = documentService.getDocuments()
				.getSelectedDocument();

		documentService.closeDocument(screenDoc);
		documentService.selectLastDocument();

		screenSourceService.removeScreenSource(screenDoc);

		if (event.isForced()) {
			context.showNotification(NotificationType.DEFAULT, "screen.share",
					"screen.share.stopped");
		}
	}

	@Subscribe
	public void onEvent(StopStreamEnvironmentMessage event) {
		//This method tries to force the stop of all stream components (stream, messenger, quiz) even if they might be inactive from this side

		final String initiator = String.format("%s %s", event.getFirstName(), event.getFamilyName());

		try {
			webService.stopQuiz();
		}
		catch (Exception exception) {
			logException(exception, "Stop quiz failed");
		}
		try {
			webService.stopMessenger();
		}
		catch (Exception exception) {
			logException(exception, "Stop messenger failed");
		}

		((PresenterContext) context).setStreamStarted(false);

		context.showNotification(NotificationType.WARNING,
			"stream.environment.stopped.by.message.title",
			"stream.environment.stopped.by.message",
			initiator);
	}

	@Override
	public void addShutdownHandler(ShutdownHandler handler) {
		requireNonNull(handler, "ShutdownHandler must not be null.");

		if (!shutdownHandlers.contains(handler)) {
			shutdownHandlers.add(handler);
		}
	}

	@Override
	public void removeShutdownHandler(ShutdownHandler handler) {
		requireNonNull(handler, "ShutdownHandler must not be null.");

		shutdownHandlers.remove(handler);
	}

	@Override
	public void showView(View childView, ViewLayer layer) {
		if (layer == ViewLayer.NotificationPopup) {
			popupManager.show(view, (NotificationPopupView) childView);
		}
		else {
			view.showView(childView, layer);

			setViewShown(getViewInterface(childView.getClass()));
		}
	}

	@Override
	public void display(Presenter<?> presenter) {
		requireNonNull(presenter);

		Presenter<?> cachedPresenter = findCachedPresenter(presenter.getClass());

		try {
			if (nonNull(cachedPresenter)) {
				View view = cachedPresenter.getView();

				if (nonNull(view)) {
					BooleanProperty property = getViewVisibleProperty(getViewInterface(view.getClass()));

					if (property.get()) {
						return;
					}

					showView(view, cachedPresenter.getViewLayer());
				}
			}
			else {
				if (presenter.getClass().equals(NotificationPresenter.class) &&
						nonNull(notificationPresenter) &&
						!notificationPresenter.equals(presenter)) {
					hideWaitingNotification();
				}

				presenter.initialize();

				View view = presenter.getView();

				if (nonNull(view)) {
					BooleanProperty property = getViewVisibleProperty(getViewInterface(view.getClass()));

					// Allow notification popups to be shown, since they do not
					// block the main view.
					if (property.get() && presenter.getViewLayer() != ViewLayer.NotificationPopup) {
						return;
					}

					presenter.setOnClose(() -> destroy(presenter));

					showView(view, presenter.getViewLayer());

					addContext(presenter);
				}
			}
		}
		catch (Exception e) {
			handleException(e, "Show view failed", "error", "generic.error");
		}
	}

	@Override
	public void destroy(Presenter<?> presenter) {
		requireNonNull(presenter);

		View childView = presenter.getView();

		try {
			view.removeView(childView, presenter.getViewLayer());

			setViewHidden(getViewInterface(childView.getClass()));

			if (!presenter.cache()) {
				presenter.destroy();

				removeContext(presenter);
			}
		}
		catch (Exception e) {
			handleException(e, "Destroy view failed", "error", "generic.error");
		}
	}

	@Override
	public void closeWindow() {
		destroy();
	}

	@Override
	public void setFullscreen(boolean enable) {
		view.setFullscreen(enable);
	}

	@Override
	public void destroy() {
		if (shutdownHandlers.isEmpty()) {
			return;
		}

		Runnable shutdownLoop = () -> {
			for (ShutdownHandler handler : shutdownHandlers) {
				try {
					if (!handler.execute()) {
						// Abort shutdown process.
						break;
					}
				}
				catch (Exception e) {
					logException(e, "Execute shutdown handler failed");
				}
			}
		};

		Thread thread = new Thread(shutdownLoop, "ShutdownHandler-Thread");
		thread.start();
	}

	private void addHandler(PresenterHandler handler) {
		handlers.add(handler);

		handler.initialize();
	}

	private void addContext(Presenter<?> presenter) {
		requireNonNull(presenter);

		if (!contexts.contains(presenter)) {
			contexts.add(presenter);
		}
	}

	private void removeContext(Presenter<?> presenter) {
		requireNonNull(presenter);

		contexts.remove(presenter);
	}

	@SuppressWarnings("unchecked")
	private <T extends Presenter<?>> T findCachedPresenter(Class<T> presenterClass) {
		requireNonNull(presenterClass);

		for (Presenter<?> p : contexts) {
			if (presenterClass == p.getClass() && p.cache()) {
				return (T) p;
			}
		}

		return null;
	}

	private boolean keyEvent(KeyEvent event) {
		Predicate<KeyEvent> action = shortcutMap.get(event);

		if (nonNull(action)) {
			return action.test(event);
		}

		return false;
	}

	private BooleanProperty getViewVisibleProperty(Class<? extends View> viewClass) {
		BooleanProperty property = viewMap.get(viewClass);

		if (isNull(property)) {
			property = new BooleanProperty(false);
			property.addListener((observable, oldValue, newValue) -> {
				context.getEventBus().post(new ViewVisibleEvent(viewClass, newValue));
			});

			viewMap.put(viewClass, property);
		}

		return property;
	}

	private void destroyHandler(Class<? extends Presenter<?>> presenterClass) {
		for (Presenter<?> presenter : contexts) {
			if (presenter.getClass() == presenterClass) {
				destroy(presenter);
				break;
			}
		}
	}

	private void onViewShown() {
		PresenterContext presenterContext = (PresenterContext) context;

		try {
			RecordingBackup backup = new RecordingBackup(presenterContext.getRecordingDirectory());

			if (backup.hasCheckpoint()) {
				display(createPresenter(RestoreRecordingPresenter.class));
			}
		}
		catch (IOException e) {
			handleException(e, "Open recording backup failed", "recording.restore.missing.backup");
		}
	}

	private void onViewBounds(Rectangle2D bounds) {
		presentationController.setMainWindowBounds(bounds);
	}

	private void onViewFocus(boolean hasFocus) {
		if (hasFocus) {
			// Stop screen sharing whe the main window has gained focus.
			PresenterContext ctx = (PresenterContext) context;
			ctx.setScreenSharingStarted(false);
		}
	}

	private boolean closeView(KeyEvent event) {
		if (!contexts.isEmpty()) {
			Presenter<?> presenter = contexts.get(contexts.size() - 1);
			View view = presenter.getView();

			BooleanProperty property = getViewVisibleProperty(getViewInterface(view.getClass()));

			if (property.get()) {
				presenter.close();
				return true;
			}
		}

		return false;
	}

	private void registerShortcut(Shortcut shortcut, Predicate<KeyEvent> action) {
		shortcutMap.put(shortcut.getKeyEvent(), action);
	}

	private void showWaitingNotification(String title) {
		String message = "please.wait";

		if (context.getDictionary().contains(title)) {
			title = context.getDictionary().get(title);
		}
		if (context.getDictionary().contains(message)) {
			message = context.getDictionary().get(message);
		}

		notificationPresenter = createPresenter(NotificationPresenter.class);

		if (nonNull(notificationPresenter)) {
			notificationPresenter.setMessage(message);
			notificationPresenter.setNotificationType(NotificationType.WAITING);
			notificationPresenter.setTitle(title);

			display(notificationPresenter);
		}
	}

	private void hideWaitingNotification() {
		if (nonNull(notificationPresenter)) {
			destroy(notificationPresenter);
			notificationPresenter = null;
		}
	}

	private void setViewHidden(Class<? extends View> viewClass) {
		BooleanProperty property = getViewVisibleProperty(viewClass);
		property.set(false);
	}

	private void setViewShown(Class<? extends View> viewClass) {
		BooleanProperty property = getViewVisibleProperty(viewClass);
		property.set(true);
	}

	private void createSettingsPresentation() {
		// Create settings asynchronously as this can take some time.
		CompletableFuture.runAsync(() -> {
			try {
				SettingsPresenter presenter = createPresenter(SettingsPresenter.class);

				if (nonNull(presenter)) {
					presenter.initialize();
					presenter.setOnClose(() -> destroy(presenter));

					addContext(presenter);
				}
			}
			catch (Exception e) {
				throw new CompletionException(e);
			}
		})
		.exceptionally(throwable -> {
			logException(throwable, "Create settings failed");
			return null;
		});
	}

	private boolean togglePauseRecording(KeyEvent event) {
		if (!recordingService.suspended()) {
			try {
				recordingService.suspend();
			}
			catch (Exception e) {
				handleException(e, "Pause recording failed", "recording.pause.error");
			}
		}
		else if (recordingService.suspended()) {
			try {
				recordingService.start();
			}
			catch (ExecutableException e) {
				handleException(e, "Start recording failed", "recording.start.error");
			}
		}

		return true;
	}

	private void documentCreated(Document doc) {
		// Hook in here to set documents note position from the config.
		PresenterConfiguration config = (PresenterConfiguration) context.getConfiguration();

		if (doc.getSplitSlideNotesPosition() != NotesPosition.NONE) {
			doc.setSplitSlideNotesPosition(config.getSlideViewConfiguration().getNotesPosition());
		}

		showView(slidesPresenter.getView(), slidesPresenter.getViewLayer());
	}

	private void documentClosed(Document doc) {
		if (documentService.getDocuments().asList().isEmpty()) {
			StartPresenter presenter = createPresenter(StartPresenter.class);

			if (nonNull(presenter)) {
				destroyHandler(presenter.getClass());

				Class<? extends View> viewClass = getViewInterface(presenter.getView().getClass());

				if (isNull(viewMap.get(viewClass))) {
					setViewHidden(viewClass);
				}

				display(presenter);
			}
		}

		// Remove bookmarks for the closed document.
		bookmarkService.clearBookmarks(doc);
	}

	private void documentSelected() {
		streamService.enableScreenSharing(false);
	}

	private <T extends Presenter<?>> T createPresenter(Class<T> pClass) {
		try {
			return contextFactory.getInstance(pClass);
		}
		catch (Throwable e) {
			handleException(e, "Create presenter failed", "generic.error");
			return null;
		}
	}

	@SuppressWarnings("unchecked")
	private static Class<? extends View> getViewInterface(Class<?> cls) {
		while (nonNull(cls)) {
			final Class<?>[] interfaces = cls.getInterfaces();

			for (final Class<?> i : interfaces) {
				if (i == View.class) {
					return (Class<? extends View>) cls;
				}

				return getViewInterface(i);
			}

			cls = cls.getSuperclass();
		}

		return null;
	}

	private void stopLocalScreenCapture() {
		screenShareService.stopScreenCapture();
	}

	private void stopScreenRecording() {
		screenShareService.stopScreenRecording();
	}

	private void stopAllScreenRecordings() {
		screenShareService.stopAllScreenRecordings();
	}
}
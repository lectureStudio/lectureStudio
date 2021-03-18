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
import static java.util.Objects.requireNonNull;

import com.google.common.eventbus.Subscribe;

import java.io.File;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;

import javax.inject.Inject;

import org.lecturestudio.core.app.ApplicationContext;
import org.lecturestudio.core.beans.BooleanProperty;
import org.lecturestudio.core.bus.event.ViewVisibleEvent;
import org.lecturestudio.core.input.KeyEvent;
import org.lecturestudio.core.model.RecentDocument;
import org.lecturestudio.core.presenter.NotificationPresenter;
import org.lecturestudio.core.presenter.Presenter;
import org.lecturestudio.core.presenter.command.CloseApplicationCommand;
import org.lecturestudio.core.presenter.command.ClosePresenterCommand;
import org.lecturestudio.core.presenter.command.FullscreenCommand;
import org.lecturestudio.core.presenter.command.ShowPresenterCommand;
import org.lecturestudio.core.service.RecentDocumentService;
import org.lecturestudio.core.util.FileUtils;
import org.lecturestudio.core.util.ObservableHashMap;
import org.lecturestudio.core.util.ObservableMap;
import org.lecturestudio.core.util.ShutdownHandler;
import org.lecturestudio.core.view.NotificationPopupManager;
import org.lecturestudio.core.view.NotificationPopupView;
import org.lecturestudio.core.view.NotificationType;
import org.lecturestudio.core.view.View;
import org.lecturestudio.core.view.ViewContextFactory;
import org.lecturestudio.core.view.ViewHandler;
import org.lecturestudio.core.view.ViewLayer;
import org.lecturestudio.media.recording.RecordingEvent;
import org.lecturestudio.editor.api.input.Shortcut;
import org.lecturestudio.editor.api.service.RecordingFileService;
import org.lecturestudio.editor.api.util.SaveRecordingHandler;
import org.lecturestudio.editor.api.view.MainView;

public class MainPresenter extends org.lecturestudio.core.presenter.MainPresenter<MainView> implements ViewHandler {

	private final ObservableMap<Class<? extends View>, BooleanProperty> viewMap;

	private final Map<KeyEvent, Predicate<KeyEvent>> shortcutMap;

	private final List<ShutdownHandler> shutdownHandlers;

	private final List<Presenter<?>> contexts;

	private final NotificationPopupManager popupManager;

	private final ViewContextFactory contextFactory;

	private final RecentDocumentService recentDocumentService;

	private final RecordingFileService recordingService;

	private SlidesPresenter slidesPresenter;

	/** The waiting notification. */
	private NotificationPresenter notificationPresenter;


	@Inject
	MainPresenter(ApplicationContext context, MainView view,
			NotificationPopupManager popupManager,
			ViewContextFactory contextFactory,
			RecentDocumentService recentDocumentService,
			RecordingFileService recordingService) {
		super(context, view);

		this.popupManager = popupManager;
		this.contextFactory = contextFactory;
		this.recentDocumentService = recentDocumentService;
		this.recordingService = recordingService;
		this.viewMap = new ObservableHashMap<>();
		this.shortcutMap = new HashMap<>();
		this.contexts = new ArrayList<>();
		this.shutdownHandlers = new ArrayList<>();
	}

	@Override
	public void openFile(File file) {
		if (isNull(file) || !file.exists()) {
			return;
		}

		showWaitingNotification("open.recording", null);

		recordingService.openRecording(file)
			.thenRun(() -> {
				hideWaitingNotification();

				RecentDocument recentDoc = new RecentDocument();
				recentDoc.setDocumentName(FileUtils.stripExtension(file.getName()));
				recentDoc.setDocumentPath(file.getAbsolutePath());
				recentDoc.setLastModified(new Date());

				recentDocumentService.add(recentDoc);
			})
			.exceptionally(throwable -> {
				hideWaitingNotification();
				handleException(throwable, "Open recording failed",
						"open.recording.error", file.getPath());
				return null;
			});
	}

	@Override
	public void setArgs(String[] args) {

	}

	@Override
	public void initialize() {
		registerShortcut(Shortcut.CLOSE_VIEW, this::closeView);

		addShutdownHandler(new SaveRecordingHandler(context, recordingService));
		addShutdownHandler(new ShutdownHandler() {

			@Override
			public boolean execute() {
				// Save configuration.
				try {
					context.saveConfiguration();
				}
				catch (Exception e) {
					logException(e, "Save configuration failed");
				}
				return true;
			}
		});
		addShutdownHandler(new ShutdownHandler() {

			@Override
			public boolean execute() {
				if (nonNull(closeAction)) {
					closeAction.execute();
				}
				return true;
			}
		});

		context.getConfiguration().extendedFullscreenProperty().addListener((observable, oldValue, newValue) -> {
			view.setMenuVisible(!newValue);
		});

		slidesPresenter = createPresenter(SlidesPresenter.class);
		slidesPresenter.initialize();

		view.setMenuVisible(!context.getConfiguration().getExtendedFullscreen());
		view.setOnClose(this::closeWindow);
		view.setOnShown(this::onViewShown);
		view.setOnKeyEvent(this::keyEvent);

		context.getEventBus().register(this);
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

	@Subscribe
	public void onCommand(final CloseApplicationCommand command) {
		closeWindow();
	}

	@Subscribe
	public void onCommand(final ClosePresenterCommand command) {
		destroyHandler(command.getPresenterClass());
	}

	@Subscribe
	public void onCommand(final FullscreenCommand command) {
		setFullscreen(command.isFullscreen());
	}

	@Subscribe
	public <T extends Presenter<?>> void onCommand(final ShowPresenterCommand<T> command) {
		T presenter = createPresenter(command.getPresenterClass());

		try {
			command.execute(presenter);
		}
		catch (Exception e) {
			logException(e, "Execute command failed");
		}

		display(presenter);
	}

	@Subscribe
	public void onEvent(RecordingEvent event) {
		if (event.created()) {
			recordingCreated();
		}
		else if (event.closed()) {
			recordingClosed();
		}
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

		Presenter<?> cachedPresenter = findCachedContext(presenter);

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

					if (property.get()) {
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

	private void addContext(Presenter<?> presenter) {
		requireNonNull(context);

		if (!contexts.contains(presenter)) {
			contexts.add(presenter);
		}
	}

	private void removeContext(Presenter<?> presenter) {
		requireNonNull(presenter);

		contexts.remove(presenter);
	}

	private Presenter<?> findCachedContext(Presenter<?> presenter) {
		requireNonNull(presenter);

		for (Presenter<?> p : contexts) {
			if ((presenter.equals(p) || presenter.getClass() == p.getClass()) && p.cache()) {
				return p;
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
			if (getViewInterface(presenter.getClass()) == presenterClass) {
				destroy(presenter);
				break;
			}
		}
	}

	private void onViewShown() {

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

	private void showWaitingNotification(String title, String message) {
		if (context.getDictionary().contains(title)) {
			title = context.getDictionary().get(title);
		}
		if (context.getDictionary().contains(message)) {
			message = context.getDictionary().get(message);
		}

		notificationPresenter = createPresenter(NotificationPresenter.class);
		notificationPresenter.setMessage(message);
		notificationPresenter.setNotificationType(NotificationType.WAITING);
		notificationPresenter.setTitle(title);

		display(notificationPresenter);
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

	private void recordingCreated() {
		showView(slidesPresenter.getView(), slidesPresenter.getViewLayer());
	}

	private void recordingClosed() {
		if (!recordingService.hasRecordings()) {
			display(createPresenter(StartPresenter.class));
		}
	}

	private <T extends Presenter<?>> T createPresenter(Class<T> pClass) {
		return contextFactory.getInstance(pClass);
	}

	@SuppressWarnings("unchecked")
	private Class<? extends View> getViewInterface(Class<?> cls) {
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
}
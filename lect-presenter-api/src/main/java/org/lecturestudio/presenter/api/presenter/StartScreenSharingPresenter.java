/*
 * Copyright (C) 2021 TU Darmstadt, Department of Computer Science,
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

import static java.util.Objects.nonNull;

import dev.onvoid.webrtc.media.video.desktop.DesktopCapturer;
import dev.onvoid.webrtc.media.video.desktop.DesktopSource;
import dev.onvoid.webrtc.media.video.desktop.ScreenCapturer;
import dev.onvoid.webrtc.media.video.desktop.WindowCapturer;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

import javax.inject.Inject;

import org.lecturestudio.core.ExecutableBase;
import org.lecturestudio.core.ExecutableException;
import org.lecturestudio.core.presenter.Presenter;
import org.lecturestudio.core.util.ObservableArrayList;
import org.lecturestudio.core.util.ObservableList;
import org.lecturestudio.core.view.ConsumerAction;
import org.lecturestudio.core.view.ViewLayer;
import org.lecturestudio.presenter.api.config.PresenterConfiguration;
import org.lecturestudio.presenter.api.config.StreamConfiguration;
import org.lecturestudio.presenter.api.context.PresenterContext;
import org.lecturestudio.presenter.api.model.ScreenShareContext;
import org.lecturestudio.presenter.api.model.SharedScreenSource;
import org.lecturestudio.presenter.api.net.ScreenShareProfiles;
import org.lecturestudio.presenter.api.view.StartScreenSharingView;

public class StartScreenSharingPresenter extends Presenter<StartScreenSharingView> {

	private ConsumerAction<ScreenShareContext> startAction;

	private ScheduledExecutorService executorService;

	private ScheduledFuture<?> future;

	private Map<SharedScreenSource, DesktopSourceTask> screenMap;

	private Map<SharedScreenSource, DesktopSourceTask> windowMap;

	private ObservableList<SharedScreenSource> screens;

	private ObservableList<SharedScreenSource> windows;

	private WindowCapturer windowCapturer;

	private ScreenCapturer screenCapturer;

	private ScreenShareContext screenShareContext;


	@Inject
	StartScreenSharingPresenter(PresenterContext context,
			StartScreenSharingView view) {
		super(context, view);
	}

	@Override
	public void initialize() {
		PresenterConfiguration config = (PresenterConfiguration) context.getConfiguration();
		StreamConfiguration streamConfig = config.getStreamConfig();

		executorService = Executors.newScheduledThreadPool(1);
		screenCapturer = new ScreenCapturer();
		windowCapturer = new WindowCapturer();
		screenMap = new ConcurrentHashMap<>();
		windowMap = new ConcurrentHashMap<>();
		screens = new ObservableArrayList<>();
		windows = new ObservableArrayList<>();
		screenShareContext = new ScreenShareContext();
		screenShareContext.profileProperty().set(
				nonNull(streamConfig.getScreenShareProfile())
				? streamConfig.getScreenShareProfile()
				: ScreenShareProfiles.STILL);

		future = executorService.scheduleAtFixedRate(() -> {
			getWindows();
			getScreens();
		}, 0, 2, TimeUnit.SECONDS);

		view.setScreens(screens);
		view.setWindows(windows);
		view.bindScreenSource(screenShareContext.sourceProperty());
		view.bindScreenShareProfile(screenShareContext.profileProperty());
		view.setScreenShareProfiles(ScreenShareProfiles.DEFAULT);
		view.setOnStart(this::onStart);
		view.setOnClose(this::close);
	}

	@Override
	public void close() {
		dispose();

		PresenterContext presenterContext = (PresenterContext) context;
		presenterContext.setScreenSharingStarted(false);
	}

	@Override
	public ViewLayer getViewLayer() {
		return ViewLayer.Dialog;
	}

	public void setOnStart(ConsumerAction<ScreenShareContext> action) {
		startAction = action;
	}

	private void onStart() {
		dispose();

		if (nonNull(startAction)) {
			startAction.execute(screenShareContext);
		}
	}

	private void dispose() {
		if (nonNull(future) && !future.isCancelled()) {
			future.cancel(true);
		}
		executorService.shutdownNow();

		try {
			screenCapturer.dispose();
			windowCapturer.dispose();
		}
		catch (Exception e) {
			// Ignore.
		}

		for (var task : screenMap.values()) {
			disposeTask(task);
		}

		for (var task : windowMap.values()) {
			disposeTask(task);
		}

		super.close();
	}

	private void disposeTask(DesktopSourceTask task) {
		if (task.destroyed()) {
			return;
		}

		try {
			task.destroy();
		}
		catch (ExecutableException e) {
			logException(e, "Destroy screen capture task failed");
		}
	}

	private void getScreens() {
		List<DesktopSource> sourceList = screenCapturer.getDesktopSources();

		// Remove closed screen sources.
		removeClosedSources(sourceList, screens, screenMap);

		// Add active screen sources.
		for (DesktopSource source : sourceList) {
			// The API returns no title for screens.
			String title = context.getDictionary().get("start.screen.sharing.screen")
					+ " " + source.id;
			SharedScreenSource screenSource = new SharedScreenSource(title,
					source.id, false);

			if (!screenMap.containsKey(screenSource)) {
				addNewSource(new ScreenCapturer(), source, screenSource,
						screens, screenMap);
			}
		}
	}

	private void getWindows() {
		List<DesktopSource> sourceList = windowCapturer.getDesktopSources();

		// Remove closed screen sources.
		removeClosedSources(sourceList, windows, windowMap);

		// Add active screen sources.
		for (DesktopSource source : sourceList) {
			SharedScreenSource screenSource = new SharedScreenSource(
					source.title, source.id, true);

			if (!windowMap.containsKey(screenSource)) {
				addNewSource(new WindowCapturer(), source, screenSource,
						windows, windowMap);
			}
		}
	}

	private void removeClosedSources(List<DesktopSource> sourceList,
			ObservableList<SharedScreenSource> observableList,
			Map<SharedScreenSource, DesktopSourceTask> sourceMap) {
		// Remove closed screen sources.
		var iter = sourceMap.entrySet().iterator();

		while (iter.hasNext()) {
			var entry = iter.next();
			var task = entry.getValue();

			if (!sourceList.contains(task.source)) {
				// Stop observing a screen source.
				disposeTask(task);

				observableList.remove(entry.getKey());

				iter.remove();
			}
		}
	}

	private void addNewSource(DesktopCapturer capturer, DesktopSource source,
			SharedScreenSource screenSource,
			ObservableList<SharedScreenSource> observableList,
			Map<SharedScreenSource, DesktopSourceTask> sourceMap) {
		DesktopSourceTask task = new DesktopSourceTask(capturer, source, screenSource);

		sourceMap.put(screenSource, task);

		observableList.add(screenSource);

		// Start observing a screen source.
		try {
			task.start();
		}
		catch (ExecutableException e) {
			logException(e, "Capture desktop source [" + source.title + "] failed");
		}
	}



	private class DesktopSourceTask extends ExecutableBase {

		final AtomicBoolean disposed = new AtomicBoolean();

		final DesktopSource source;

		final DesktopCapturer capturer;

		final SharedScreenSource sharedSource;

		ScheduledFuture<?> future;


		DesktopSourceTask(DesktopCapturer capturer, DesktopSource source,
				SharedScreenSource sharedSource) {
			this.source = source;
			this.capturer = capturer;
			this.sharedSource = sharedSource;
		}

		@Override
		protected void initInternal() throws ExecutableException {
			capturer.selectSource(source);
		}

		@Override
		protected void startInternal() throws ExecutableException {
			capturer.start((result, desktopFrame) -> {
				sharedSource.setVideoFrame(desktopFrame);
			});

			future = executorService.scheduleAtFixedRate(() -> {
				if (!disposed.get()) {
					synchronized (capturer) {
						capturer.captureFrame();
					}
				}
			}, 0, 2, TimeUnit.SECONDS);
		}

		@Override
		protected void stopInternal() throws ExecutableException {
			if (nonNull(future) && !future.isCancelled()) {
				future.cancel(true);
			}
		}

		@Override
		protected void destroyInternal() throws ExecutableException {
			disposed.set(true);

			synchronized (capturer) {
				capturer.dispose();
			}
		}
	}
}
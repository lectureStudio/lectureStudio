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

package org.lecturestudio.javafx.app;

import static java.util.Objects.nonNull;
import static java.util.Objects.requireNonNull;

import java.awt.Desktop;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.FutureTask;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;

import org.lecturestudio.core.ExecutableException;
import org.lecturestudio.core.app.ApplicationBase;
import org.lecturestudio.core.app.ApplicationContext;
import org.lecturestudio.core.app.ApplicationFactory;
import org.lecturestudio.core.app.GraphicalApplication;
import org.lecturestudio.core.app.Preloader;
import org.lecturestudio.core.app.configuration.Configuration;
import org.lecturestudio.core.presenter.MainPresenter;
import org.lecturestudio.core.util.FileUtils;
import org.lecturestudio.core.util.OsInfo;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public abstract class JavaFxApplication extends ApplicationBase implements GraphicalApplication {

	private static final Logger LOG = LogManager.getLogger(JavaFxApplication.class);

	private static CountDownLatch startFxLatch;

	private static Stage primaryStage;


	public static void launch(final String[] args, Class<? extends Preloader> preloaderClass) {
		Platform.startup(() -> {
			if (!OsInfo.isMacOs()) {
				return;
			}

			Desktop.getDesktop().setOpenFileHandler(event -> {
				LOG.info("Open files: " + event.getFiles());

				if (nonNull(openFilesHandler)) {
					openFilesHandler.accept(event.getFiles());
				}
				else {
					OPEN_FILES.addAll(event.getFiles());
				}
			});
		});

		ApplicationBase.launch(args, preloaderClass);
	}

	@Override
	protected final void initInternal(final String[] args) throws ExecutableException {
		final CountDownLatch initLatch = new CountDownLatch(1);
		startFxLatch = new CountDownLatch(1);

		Thread thread = new Thread(() -> {
			Application.launch(FxApplication.class, args);
		});
		thread.setName("JavaFX-Application-Launcher");
		thread.start();

		final ApplicationFactory appFactory;
		final ApplicationContext appContext;
		final MainPresenter<?> mainPresenter;

		/*
		 * Initialize outside the JavaFX Application Thread, otherwise it will block
		 * the runtime and delay the splash appearance.
		 */
		try {
			appFactory = createApplicationFactory();
			requireNonNull(appFactory, "Application factory must not be null.");

			appContext = appFactory.getApplicationContext();
			requireNonNull(appContext, "Application context must not be null.");

			mainPresenter = appFactory.getStartPresenter();
			requireNonNull(mainPresenter, "Start presenter was not initialized.");
		}
		catch (Exception e) {
			throw new ExecutableException(e);
		}

		FutureTask<Void> initStartViewTask = new FutureTask<>(() -> {
			try {
				mainPresenter.setArgs(args);
				mainPresenter.initialize();
			}
			catch (Exception e) {
				LOG.error("Initialize start context failed.", e);

				System.exit(-1);
			}

			if (!Parent.class.isAssignableFrom(mainPresenter.getView().getClass())) {
				throw new Exception("Start view must be a subclass of a JavaFX parent node.");
			}

			mainPresenter.setOnClose(() -> {
				try {
					destroy();
				}
				catch (ExecutableException e) {
					LOG.error("Destroy application failed.", e);

					System.exit(-1);
				}
			});

			Configuration config = appContext.getConfiguration();
			Scene scene = new Scene((Parent) mainPresenter.getView());

			String[] appIcons = FileUtils.getResourceListing("/resources/gfx/app-icon", (name) -> name.endsWith(".png"));

			for (String iconPath : appIcons) {
				primaryStage.getIcons().add(new Image(JavaFxApplication.class.getResourceAsStream(iconPath)));
			}

			boolean fullscreen = config.getStartFullscreen();
			boolean maximized = config.getStartMaximized();

			primaryStage.setScene(scene);
			primaryStage.setTitle(config.getApplicationName());
			primaryStage.setMaximized(!fullscreen && maximized);
			primaryStage.setFullScreen(fullscreen);

			initLatch.countDown();

			return null;
		});

		try {
			// Wait until the JavaFX Application has been initialized.
			startFxLatch.await();

			requireNonNull(primaryStage, "Stage was not initialized.");

			// Initialize the initial/start view.
			Platform.runLater(initStartViewTask);

			// Wait until the initial/start view has been initialized.
			initLatch.await();

			// Get potential exceptions.
			initStartViewTask.get();
		}
		catch (Exception e) {
			throw new ExecutableException(e);
		}

		openFilesHandler = files -> {
			LOG.info("Handle files: " + files);

			mainPresenter.openFile(files.get(0));
		};

		if (!OPEN_FILES.isEmpty()) {
			mainPresenter.openFile(OPEN_FILES.get(0));
			OPEN_FILES.clear();
		}
	}

	@Override
	protected final void startInternal() throws ExecutableException {
		final CountDownLatch startLatch = new CountDownLatch(1);

		FutureTask<Void> startTask = new FutureTask<>(() -> {
			primaryStage.show();

			startLatch.countDown();

			return null;
		});

		Platform.runLater(startTask);

		try {
			startLatch.await();

			// Get potential exceptions.
			startTask.get();
		}
		catch (Exception e) {
			throw new ExecutableException(e);
		}
	}

	@Override
	protected final void stopInternal() {
		Platform.exit();
	}

	@Override
	protected final void destroyInternal() {

	}



	public static class FxApplication extends Application {

		@Override
		public void start(Stage primaryStage) {
			primaryStage.getProperties().put("hostServices", getHostServices());

			JavaFxApplication.primaryStage = primaryStage;
			JavaFxApplication.startFxLatch.countDown();
		}
	}

}

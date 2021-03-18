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

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.FutureTask;

import javafx.application.Platform;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.layout.Pane;
import javafx.stage.Popup;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

import org.lecturestudio.core.ExecutableException;
import org.lecturestudio.core.app.Preloader;

public abstract class JavaFxPreloader implements Preloader {

	private Stage stage;

	private Popup popup;


	abstract protected Node createView(Scene scene) throws Exception;


	@Override
	public void init(String[] args) throws ExecutableException {
		final CountDownLatch initLatch = new CountDownLatch(1);

		FutureTask<Void> initTask = new FutureTask<>(() -> {
			stage = new Stage();

			Scene scene = new Scene(new Pane());

			/*
			 * Hide the primary stage:
			 * 1. as UTILITY to not to show the window icon of the pre-loader stage.
			 * 2. with zero opacity to close the window borders, since it is UTILITY.
			 */
			stage.initStyle(StageStyle.UTILITY);
			stage.setOpacity(0);
			stage.setScene(scene);

			Node view = createView(scene);

			popup = new Popup();
			popup.getContent().add(view);
			popup.centerOnScreen();

			initLatch.countDown();

			return null;
		});

		/*
		 * The preloader window may be closed before the application window opens
		 * which will cause the JavaFX runtime to shut down. Thus do not implicitly
		 * shutdown the JavaFX runtime.
		 */
		Platform.setImplicitExit(false);
		Platform.runLater(initTask);

		try {
			initLatch.await();

			// Get potential exceptions.
			initTask.get();
		}
		catch (Exception e) {
			throw new ExecutableException(e);
		}
	}

	@Override
	public void start() throws ExecutableException {
		final CountDownLatch startLatch = new CountDownLatch(1);

		FutureTask<Void> startTask = new FutureTask<>(() -> {
			stage.show();
			popup.show(stage);

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
	public void close() throws ExecutableException {
		final CountDownLatch closeLatch = new CountDownLatch(1);

		FutureTask<Void> closeTask = new FutureTask<>(() -> {
			stage.close();

			closeLatch.countDown();

			return null;
		});

		Platform.runLater(closeTask);

		try {
			closeLatch.await();

			// Get potential exceptions.
			closeTask.get();
		}
		catch (Exception e) {
			throw new ExecutableException(e);
		}
	}

	@Override
	public void destroy() {

	}
}

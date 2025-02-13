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

package org.lecturestudio.swing.view;

import static java.util.Objects.nonNull;

import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.RunnableFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import javax.swing.*;

import javafx.application.Platform;
import javafx.stage.DirectoryChooser;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

import org.lecturestudio.core.view.DirectoryChooserView;
import org.lecturestudio.core.view.View;

public class SwingFxDirectoryChooserView implements DirectoryChooserView {

	private final Lock lock;

	private final Condition condition;

	private File selectedDirectory;

	private String chooserTitle;


	public SwingFxDirectoryChooserView() {
		lock = new ReentrantLock();
		condition = lock.newCondition();
	}

	@Override
	public void setInitialDirectory(File directory) {
		selectedDirectory = directory;
	}

	@Override
	public void setTitle(String title) {
		chooserTitle = title;
	}

	@Override
	public File show(View parent) {
		return openDialogBlocked(parent);
	}

	/**
	 * Open the DirectoryChooser in the JavaFX event thread and wait until the
	 * DirectoryChooser is closed.
	 *
	 * @param parent The parent view.
	 *
	 * @return The selected directory or {@code null} if aborted.
	 */
	private File openDialog(View parent) {
		File selectedDir;
		Window ownerWindow;
		Stage stage = null;

		if (nonNull(parent)) {
			if (!Component.class.isAssignableFrom(parent.getClass())) {
				throw new IllegalArgumentException("View expected to be a java.awt.Component");
			}

			ownerWindow = SwingUtilities.getWindowAncestor((Component) parent);

			// Show the stage on top of the Swing Window to block input.
			stage = new Stage();
			stage.initStyle(StageStyle.UTILITY);
			stage.setOpacity(0.01); // Make the stage barely visible, but able to block input.
			stage.setX(ownerWindow.getX());
			stage.setY(ownerWindow.getY());
			stage.setWidth(ownerWindow.getWidth());
			stage.setHeight(ownerWindow.getHeight());
			stage.show();

			// Prevent the main window to be shown on top of the file-chooser.
			Stage finalStage = stage;
			ownerWindow.addWindowFocusListener(new WindowAdapter() {

				@Override
				public void windowGainedFocus(WindowEvent e) {
					// Bring the FX file-chooser to the front.
					Platform.runLater(finalStage::requestFocus);
				}
			});
		}

		DirectoryChooser dirChooser = new DirectoryChooser();
		dirChooser.setTitle(chooserTitle);
		dirChooser.setInitialDirectory(selectedDirectory);

		selectedDir = dirChooser.showDialog(stage);

		if (nonNull(stage)) {
			// Close the stage with the native dialog.
			stage.hide();
		}

		return selectedDir;
	}

	/**
	 * Open the DirectoryChooser in the JavaFX event thread and wait until the
	 * DirectoryChooser is closed. The call of this method will block the
	 * executing thread.
	 *
	 * @param parent The parent view.
	 *
	 * @return The selected directory or {@code null} if aborted.
	 */
	private File openDialogBlocked(View parent) {
		lock.lock();

		File selectedFile;

		RunnableFuture<File> dirChooserRunnable = new DirectoryChooserRunnable(parent);

		try {
			Platform.runLater(dirChooserRunnable);

			condition.await();

			selectedFile = dirChooserRunnable.get();
		}
		catch (InterruptedException | ExecutionException e) {
			throw new RuntimeException(e);
		}
		finally {
			lock.unlock();
		}

		return selectedFile;
	}

	/**
	 * Resume operation on the current thread.
	 */
	private void resume() {
		lock.lock();

		try {
			condition.signal();
		}
		finally {
			lock.unlock();
		}
	}



	private class DirectoryChooserRunnable implements RunnableFuture<File> {

		private final View parent;

		private File file;


		DirectoryChooserRunnable(View parent) {
			this.parent = parent;
			this.file = null;
		}

		@Override
		public void run() {
			file = openDialog(parent);

			resume();
		}

		@Override
		public boolean cancel(boolean mayInterruptIfRunning) {
			return false;
		}

		@Override
		public boolean isCancelled() {
			return false;
		}

		@Override
		public boolean isDone() {
			return false;
		}

		@Override
		public File get() {
			return file;
		}

		@Override
		public File get(long timeout, TimeUnit unit) {
			return file;
		}
	}
}

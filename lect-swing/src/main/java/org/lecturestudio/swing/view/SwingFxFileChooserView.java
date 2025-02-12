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

import javafx.application.Platform;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

import java.awt.*;
import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.RunnableFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.stream.Collectors;

import javax.swing.*;

import org.lecturestudio.core.view.FileChooserView;
import org.lecturestudio.core.view.View;

public class SwingFxFileChooserView implements FileChooserView {

	private enum Type { OPEN, SAVE }

	private final Lock lock;

	private final Condition condition;

	private final List<FileChooser.ExtensionFilter> extensionFilters;

	private File selectedDirectory;

	private String selectedFileName;


	public SwingFxFileChooserView() {
		lock = new ReentrantLock();
		condition = lock.newCondition();
		extensionFilters = new ArrayList<>();
	}

	@Override
	public void addExtensionFilter(String description, String... extensions) {
		List<String> list = Arrays.stream(extensions).map(s -> "*." + s)
				.collect(Collectors.toList());

		extensionFilters.add(new FileChooser.ExtensionFilter(description, list));
	}

	@Override
	public void setInitialDirectory(File directory) {
		selectedDirectory = directory;
	}

	@Override
	public void setInitialFileName(String name) {
		selectedFileName = name;
	}

	@Override
	public File showOpenFile(View parent) {
		return openFileChooser(parent, Type.OPEN);
	}

	@Override
	public File showSaveFile(View parent) {
		return openFileChooser(parent, Type.SAVE);
	}

	private File openFileChooser(View parent, Type type) {
		return openDialogBlocked(parent, type);
	}

	/**
	 * Open the FileChooser in the JavaFX event thread and wait until the
	 * FileChooser is closed.
	 *
	 * @param parent The parent view.
	 * @param type   The dialog selection type.
	 *
	 * @return The selected file or {@code null} if aborted.
	 */
	private File openDialog(View parent, Type type) {
		File selectedFile;
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
		}

		FileChooser fileChooser = new FileChooser();
		fileChooser.getExtensionFilters().addAll(extensionFilters);
		fileChooser.setInitialDirectory(selectedDirectory);
		fileChooser.setInitialFileName(selectedFileName);

		selectedFile = switch (type) {
			case OPEN -> fileChooser.showOpenDialog(stage);
			case SAVE -> fileChooser.showSaveDialog(stage);
		};

		if (nonNull(stage)) {
			// Close the stage with the native dialog.
			stage.hide();
		}

		return selectedFile;
	}

	/**
	 * Open the FileChooser in the JavaFX event thread and wait until the
	 * FileChooser is closed. The call of this method will block the
	 * executing thread.
	 *
	 * @param parent The parent view.
	 * @param type   The dialog selection type.
	 *
	 * @return The selected file or {@code null} if aborted.
	 */
	private File openDialogBlocked(View parent, Type type) {
		lock.lock();

		File selectedFile;

		RunnableFuture<File> fileChooserRunnable = new FileChooserRunnable(parent, type);

		try {
			Platform.runLater(fileChooserRunnable);

			condition.await();

			selectedFile = fileChooserRunnable.get();
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



	private class FileChooserRunnable implements RunnableFuture<File> {

		private final View parent;

		private final Type type;

		private File file;


		FileChooserRunnable(View parent, Type type) {
			this.parent = parent;
			this.type = type;
			this.file = null;
		}

		@Override
		public void run() {
			file = openDialog(parent, type);

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

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

import javafx.application.Platform;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

import javax.swing.*;

import org.lecturestudio.core.view.View;

public abstract class SwingFxChooserView {

	protected enum Type { OPEN, SAVE }

	private final Lock lock;

	private final Condition condition;


	abstract protected File chooseFile(Stage stage, Type type);


	protected SwingFxChooserView() {
		lock = new ReentrantLock();
		condition = lock.newCondition();
	}

	/**
	 * Opens the chooser-view in the JavaFX event thread and wait until the
	 * view is closed.
	 *
	 * @param parent The parent view.
	 * @param type   The dialog selection type.
	 *
	 * @return The selected file or {@code null} if aborted.
	 */
	protected File openDialog(View parent, Type type) {
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

		selectedFile = chooseFile(stage, type);

		if (nonNull(stage)) {
			// Close the stage with the native dialog.
			stage.hide();
		}

		return selectedFile;
	}

	/**
	 * Open the chooser-view in the JavaFX event thread and wait until the
	 * view is closed. The call of this method will block the executing thread.
	 *
	 * @param parent The parent view.
	 * @param type   The dialog selection type.
	 *
	 * @return The selected file or {@code null} if aborted.
	 */
	protected File openDialogBlocked(View parent, Type type) {
		lock.lock();

		File selectedFile;

		RunnableFuture<File> chooserRunnable = new ChooserRunnable(parent, type);

		try {
			Platform.runLater(chooserRunnable);

			condition.await();

			selectedFile = chooserRunnable.get();
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



	private class ChooserRunnable implements RunnableFuture<File> {

		private final View parent;

		private final Type type;

		private File file;


		ChooserRunnable(View parent, Type type) {
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

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

import java.awt.Component;
import java.awt.Window;
import java.io.File;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.RunnableFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import javax.swing.JFileChooser;
import javax.swing.SwingUtilities;
import javax.swing.filechooser.FileNameExtensionFilter;

import org.lecturestudio.core.view.FileChooserView;
import org.lecturestudio.core.view.View;

public class SwingFileChooserView implements FileChooserView {

	private enum Type { OPEN, SAVE }

	private final Lock lock;

	private final Condition condition;

	private final JFileChooser fileChooser;


	public SwingFileChooserView() {
		lock = new ReentrantLock();
		condition = lock.newCondition();
		fileChooser = new JFileChooser();
		fileChooser.setAcceptAllFileFilterUsed(false);
	}

	@Override
	public void addExtensionFilter(String description, String... extensions) {
		fileChooser.setFileFilter(new FileNameExtensionFilter(description, extensions));
	}

	@Override
	public void setInitialDirectory(File directory) {
		fileChooser.setCurrentDirectory(directory);
	}

	@Override
	public void setInitialFileName(String name) {
		fileChooser.setSelectedFile(new File(name));
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
		if (SwingUtilities.isEventDispatchThread()) {
			return openDialog(parent, type);
		}

		return openDialogBlocked(parent, type);
	}

	/**
	 * Open the FileChooser in the AWT event dispatching thread and wait until
	 * the FileChooser is closed.
	 *
	 * @param parent The parent view.
	 * @param type   The dialog selection type.
	 *
	 * @return The selected file or {@code null} if aborted.
	 */
	private File openDialog(View parent, Type type) {
		File selectedFile = null;
		Window ownerWindow = null;

		if (nonNull(parent)) {
			if (!Component.class.isAssignableFrom(parent.getClass())) {
				throw new IllegalArgumentException("View expected to be a java.awt.Component");
			}

			ownerWindow = SwingUtilities.getWindowAncestor((Component) parent);
		}

		int status = JFileChooser.CANCEL_OPTION;

		switch (type) {
			case OPEN:
				status = fileChooser.showOpenDialog(ownerWindow);
				break;

			case SAVE:
				status = fileChooser.showSaveDialog(ownerWindow);
				break;

			default:
				selectedFile = null;
				break;
		}

		if (status == JFileChooser.APPROVE_OPTION) {
			selectedFile = fileChooser.getSelectedFile();
		}

		return selectedFile;
	}

	/**
	 * Open the FileChooser in the AWT event dispatching thread and wait until
	 * the FileChooser is closed. The call of this method will block the
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
			SwingUtilities.invokeLater(fileChooserRunnable);

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

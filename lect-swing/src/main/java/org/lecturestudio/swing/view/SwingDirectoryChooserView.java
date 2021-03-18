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

import org.lecturestudio.core.view.DirectoryChooserView;
import org.lecturestudio.core.view.View;
import org.lecturestudio.swing.util.SwingUtils;

public class SwingDirectoryChooserView implements DirectoryChooserView {

	private final Lock lock;

	private final Condition condition;

	private final JFileChooser dirChooser;


	public SwingDirectoryChooserView() {
		lock = new ReentrantLock();
		condition = lock.newCondition();
		dirChooser = new JFileChooser();
		dirChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
		dirChooser.setAcceptAllFileFilterUsed(false);
	}

	@Override
	public void setInitialDirectory(File directory) {
		dirChooser.setCurrentDirectory(directory);
	}

	@Override
	public void setTitle(String title) {
		dirChooser.setDialogTitle(title);
	}

	@Override
	public File show(View parent) {
		if (SwingUtilities.isEventDispatchThread()) {
			return openDialog(parent);
		}

		return openDialogBlocked(parent);
	}

	/**
	 * Open the DirectoryChooser in the AWT event dispatching thread and wait
	 * until the DirectoryChooser is closed.
	 *
	 * @param parent The parent view.
	 *
	 * @return The selected directory or {@code null} if aborted.
	 */
	private File openDialog(View parent) {
		Window ownerWindow = null;

		if (nonNull(parent)) {
			if (!Component.class.isAssignableFrom(parent.getClass())) {
				throw new IllegalArgumentException("View expected to be a java.awt.Component");
			}

			ownerWindow = SwingUtilities.getWindowAncestor((Component) parent);
		}

		if (dirChooser.showOpenDialog(ownerWindow) == JFileChooser.APPROVE_OPTION) {
			return dirChooser.getSelectedFile();
		}

		return null;
	}

	/**
	 * Open the DirectoryChooser in the AWT event dispatching thread and wait
	 * until the DirectoryChooser is closed. The call of this method will block
	 * the executing thread.
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
			SwingUtils.invoke(dirChooserRunnable);

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

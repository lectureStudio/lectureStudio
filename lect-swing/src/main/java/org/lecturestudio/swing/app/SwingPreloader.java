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

package org.lecturestudio.swing.app;

import java.awt.Dimension;
import java.awt.GraphicsDevice;
import java.awt.Rectangle;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.FutureTask;

import javax.swing.JComponent;
import javax.swing.JWindow;
import javax.swing.SwingUtilities;

import org.lecturestudio.core.ExecutableException;
import org.lecturestudio.core.app.Preloader;
import org.lecturestudio.core.app.view.Screens;

public abstract class SwingPreloader implements Preloader {

	private JWindow window;


	abstract protected JComponent createView() throws Exception;


	@Override
	public void init(String[] args) throws ExecutableException {
		final CountDownLatch initLatch = new CountDownLatch(1);

		FutureTask<Void> initTask = new FutureTask<>(() -> {
			JComponent content = createView();

			// Open on the default screen.
			GraphicsDevice dev = Screens.getDefaultScreenDevice();
			Rectangle screenSize = dev.getDefaultConfiguration().getBounds();

			window = new JWindow();
			window.getContentPane().add(content);
			window.setLocation((screenSize.width - content.getWidth()) >> 1,(screenSize.height - content.getHeight()) >> 1);
			window.setSize(content.getWidth(), content.getHeight());
			window.setPreferredSize(new Dimension(content.getWidth(), content.getHeight()));

			initLatch.countDown();

			return null;
		});

		SwingUtilities.invokeLater(initTask);

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
			window.setVisible(true);

			startLatch.countDown();

			return null;
		});

		SwingUtilities.invokeLater(startTask);

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
			window.setVisible(false);
			window.dispose();

			closeLatch.countDown();

			return null;
		});

		SwingUtilities.invokeLater(closeTask);

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

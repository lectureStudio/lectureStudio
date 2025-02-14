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

import static java.util.Objects.requireNonNull;

import java.awt.Component;
import java.awt.GraphicsDevice;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.FutureTask;

import javax.swing.JFrame;
import javax.swing.SwingUtilities;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import org.lecturestudio.core.ExecutableException;
import org.lecturestudio.core.app.ApplicationBase;
import org.lecturestudio.core.app.ApplicationContext;
import org.lecturestudio.core.app.ApplicationFactory;
import org.lecturestudio.core.app.GraphicalApplication;
import org.lecturestudio.core.app.configuration.Configuration;
import org.lecturestudio.core.app.view.Screens;
import org.lecturestudio.core.presenter.MainPresenter;

public abstract class SwingApplication extends ApplicationBase implements GraphicalApplication {

	private static final Logger LOG = LogManager.getLogger(SwingApplication.class);

	private JFrame window;

	private MainPresenter<?> mainPresenter;


	@Override
	protected void initInternal(String[] args) throws ExecutableException {
		final CountDownLatch initLatch = new CountDownLatch(1);
		final ApplicationFactory appFactory;
		final ApplicationContext appContext;
		final MainPresenter<?> mainPresenter;

		try {
			appFactory = createApplicationFactory();
			requireNonNull(appFactory, "Application factory must not be null");

			appContext = appFactory.getApplicationContext();
			requireNonNull(appContext, "Application context must not be null");

			mainPresenter = appFactory.getStartPresenter();
			requireNonNull(mainPresenter, "Start presenter was not initialized");
		}
		catch (Exception e) {
			throw new ExecutableException(e);
		}

		final Configuration config = appContext.getConfiguration();

		FutureTask<Void> initTask = new FutureTask<>(() -> {
			try {
				mainPresenter.setArgs(args);
				mainPresenter.initialize();
			}
			catch (Exception e) {
				LOG.error("Initialize start-presenter failed", e);

				System.exit(-1);
			}

			if (!Component.class.isAssignableFrom(mainPresenter.getView().getClass())) {
				throw new Exception("Start view must be a subclass of java.awt.Component");
			}

			mainPresenter.setOnClose(() -> {
				try {
					destroy();
				}
				catch (ExecutableException e) {
					LOG.error("Destroy application failed", e);

					System.exit(-1);
				}
			});

			window = new JFrame();
			window.setTitle(config.getApplicationName());
			window.getContentPane().add((Component) mainPresenter.getView());

			if (config.getStartFullscreen()) {
				GraphicsDevice device = Screens.getScreenDevice(window);

				window.setUndecorated(true);
				window.setBounds(device.getDefaultConfiguration().getBounds());
				window.setResizable(false);
				window.validate();
			}
			else {
				window.pack();

				if (config.getStartMaximized()) {
					window.setExtendedState(window.getExtendedState() | JFrame.MAXIMIZED_BOTH);
				}
			}

			initLatch.countDown();

			return null;
		});

		SwingUtilities.invokeLater(initTask);

		try {
			// Wait until the initial/start view has been initialized.
			initLatch.await();

			// Get potential exceptions.
			initTask.get();
		}
		catch (Exception e) {
			throw new ExecutableException(e);
		}

		this.mainPresenter = mainPresenter;
	}

	@Override
	protected void startInternal() throws ExecutableException {
		final CountDownLatch startLatch = new CountDownLatch(1);

		FutureTask<Void> startTask = new FutureTask<>(() -> {
			window.setLocationRelativeTo(null);
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
		if (!OPEN_FILES.isEmpty()) {
			mainPresenter.openFile(OPEN_FILES.get(0));
			OPEN_FILES.clear();
		}
	}

	@Override
	protected void stopInternal() throws ExecutableException {
		window.dispose();
	}

	@Override
	protected void destroyInternal() throws ExecutableException {

	}
}

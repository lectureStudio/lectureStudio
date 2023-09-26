/*
 * Copyright (C) 2023 TU Darmstadt, Department of Computer Science,
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

package org.lecturestudio.presenter.api.handler;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

import org.lecturestudio.core.bus.EventBus;
import org.lecturestudio.core.presenter.ProgressPresenter;
import org.lecturestudio.core.presenter.command.ShowPresenterCommand;
import org.lecturestudio.core.view.ProgressView;
import org.lecturestudio.presenter.api.context.PresenterContext;

import me.friwi.jcefmaven.CefAppBuilder;
import me.friwi.jcefmaven.EnumProgress;
import me.friwi.jcefmaven.UnsupportedPlatformException;
import me.friwi.jcefmaven.impl.step.check.CefInstallationChecker;

/**
 * Handle stream viewing state by incorporating and installing the Java Chromium
 * Embedded Framework (JCEF) if necessary.
 *
 * @author Alex Andres
 */
public abstract class CefStreamHandler extends PresenterHandler {

	private final EventBus eventBus;


	public CefStreamHandler(PresenterContext context) {
		super(context);

		eventBus = context.getEventBus();
	}

	/**
	 * Should be used by concrete implementations to proceed once the
	 * installation has finished.
	 */
	abstract protected void installFinished();

	protected File getJcefDir() {
		return new File(context.getDataLocator().toAppDataPath("jcef"));
	}

	protected boolean isJcefInstalled() throws UnsupportedPlatformException {
		return CefInstallationChecker.checkInstallation(getJcefDir());
	}

	protected void installJcef() {
		// Show download progress if JCEF needs to be downloaded.
		loadJcef(context, getJcefDir());
	}

	protected void loadJcef(PresenterContext context, File jcefDir) {
		eventBus.post(new ShowPresenterCommand<>(ProgressPresenter.class) {

			@Override
			public void execute(ProgressPresenter presenter) {
				var dict = context.getDictionary();

				CefAppBuilder builder = new CefAppBuilder();
				builder.setInstallDir(jcefDir);

				ProgressView progressView = presenter.getView();
				progressView.setTitle(dict.get("loading.jcef"));
				progressView.setMessage(dict.get("loading.jcef.description"));
				progressView.setOnViewShown(() -> {
					// Install, to not block the UI, asynchronously.
					CompletableFuture.runAsync(() -> {
						try {
							builder.install();
						}
						catch (Exception e) {
							throw new RuntimeException(e);
						}
					});
				});

				// Show download progress.
				builder.setProgressHandler((state, progress) -> {
					if (state == EnumProgress.DOWNLOADING) {
						progressView.setProgress(progress / 100.0);
					}
					else if (state == EnumProgress.INSTALL) {
						presenter.close();

						CompletableFuture.runAsync(() -> {
							try {
								waitForInstallLock(jcefDir);
							}
							catch (Exception e) {
								throw new RuntimeException(e);
							}
						});
					}
				});
			}
		});
	}

	private void waitForInstallLock(File jcefDir) throws Exception {
		Path path = Paths.get(jcefDir.getAbsolutePath(), "install.lock");
		boolean lockFound;
		int attempts = 3;

		while (!(lockFound = Files.exists(path)) && attempts > 0) {
			TimeUnit.MILLISECONDS.sleep(500);
			attempts--;
		}

		if (lockFound) {
			installFinished();
		}
	}
}

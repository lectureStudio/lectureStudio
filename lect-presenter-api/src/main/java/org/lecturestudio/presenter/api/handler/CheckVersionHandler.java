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

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;

import org.lecturestudio.core.model.VersionInfo;
import org.lecturestudio.core.presenter.NewVersionPresenter;
import org.lecturestudio.core.presenter.command.NewVersionCommand;
import org.lecturestudio.presenter.api.config.PresenterConfiguration;
import org.lecturestudio.presenter.api.context.PresenterContext;
import org.lecturestudio.web.api.model.GitHubRelease;
import org.lecturestudio.web.api.service.VersionChecker;

public class CheckVersionHandler extends PresenterHandler {

	/**
	 * Create a new {@code CheckVersionHandler} with the given context.
	 *
	 * @param context The presenter application context.
	 */
	public CheckVersionHandler(PresenterContext context) {
		super(context);
	}

	@Override
	public void initialize() {
		PresenterConfiguration config = context.getConfiguration();

		// Check for a new version.
		if (config.getCheckNewVersion()) {
			CompletableFuture.runAsync(() -> {
				try {
					VersionChecker versionChecker = new VersionChecker(
							"lectureStudio", "lectureStudio");

					if (versionChecker.newVersionAvailable()) {
						GitHubRelease release = versionChecker.getLatestRelease();

						VersionInfo version = new VersionInfo();
						version.downloadUrl = versionChecker.getMatchingAssetUrl();
						version.htmlUrl = release.getUrl();
						version.published = release.getPublishedAt();
						version.version = release.getTagName();

						context.getEventBus().post(new NewVersionCommand(
								NewVersionPresenter.class, version));
					}
				}
				catch (Exception e) {
					throw new CompletionException(e);
				}
			})
			.exceptionally(throwable -> {
				logException(throwable, "Check for new version failed");
				return null;
			});
		}
	}
}

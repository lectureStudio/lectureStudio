/*
 * Copyright (C) 2021 TU Darmstadt, Department of Computer Science,
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

package org.lecturestudio.web.api.service;

import static java.util.Objects.isNull;

import java.net.URL;
import java.util.List;

import org.apache.maven.artifact.versioning.ComparableVersion;
import org.lecturestudio.core.model.VersionInfo;
import org.lecturestudio.core.util.OsInfo;
import org.lecturestudio.web.api.model.GitHubRelease;

/**
 * Checks for the availability of new releases. The current implementation
 * queries GitHub releases.
 *
 * @author Alex Andres
 */
public class VersionChecker {

	private final GitHubService gitHubService;

	private GitHubRelease latestRelease;


	/**
	 * Creates a new {@code VersionChecker}.
	 */
	public VersionChecker() {
		ServiceParameters parameters = new ServiceParameters();
		parameters.setUrl("https://api.github.com");

		gitHubService = new GitHubService(parameters);
	}

	/**
	 * Checks if a new version of the application is available.
	 *
	 * @return True if a new version of the application is available.
	 */
	public boolean newVersionAvailable() {
		// Retrieve the most recently published version.
		try {
			latestRelease = gitHubService.getLatestRelease();
		}
		catch (Exception e) {
			// No release found. Get all pre-releases.
			List<GitHubRelease> releases = gitHubService.getReleases();

			if (releases.isEmpty()) {
				// Not a single release available.
				return false;
			}

			// The most recent pre-release is the first one.
			latestRelease = releases.get(0);
		}

		ComparableVersion appVersion = new ComparableVersion(VersionInfo.getAppVersion());
		ComparableVersion repoVersion = new ComparableVersion(latestRelease.getTagName());

		return repoVersion.compareTo(appVersion) < 0;
	}

	/**
	 * Gets the latest release information. If {@link #newVersionAvailable()}
	 * returns {@code false} this method returns {@code null}.
	 *
	 * @return The latest GitHub release.
	 */
	public GitHubRelease getLatestRelease() {
		return latestRelease;
	}

	/**
	 * Gets the download URL for an asset that matches the running OS. If {@link
	 * #newVersionAvailable()} returns {@code false} this method returns {@code
	 * null}.
	 *
	 * @return The download URL for an asset.
	 */
	public URL getMatchingAssetUrl() {
		if (isNull(latestRelease) || latestRelease.getAssets().isEmpty()) {
			return null;
		}

		for (var asset : latestRelease.getAssets()) {
			if (asset.getName().contains(OsInfo.getPlatformName())) {
				return asset.getDownloadUrl();
			}
		}

		return null;
	}
}

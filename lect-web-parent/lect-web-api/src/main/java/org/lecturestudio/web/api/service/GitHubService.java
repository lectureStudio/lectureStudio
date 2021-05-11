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

import java.net.URI;
import java.util.List;

import javax.inject.Inject;

import org.eclipse.microprofile.rest.client.RestClientBuilder;
import org.lecturestudio.web.api.client.GitHubClient;
import org.lecturestudio.web.api.model.GitHubRelease;

/**
 * Service implementation to retrieve project related information - like the
 * latest release - from the GitHub project.
 *
 * @author Alex Andres
 */
public class GitHubService extends ProviderService {

	private final GitHubClient providerClient;

	/**
	 * Creates a new {@code GitHubService}.
	 *
	 * @param parameters The service connection parameters.
	 */
	@Inject
	public GitHubService(ServiceParameters parameters) {
		providerClient = RestClientBuilder.newBuilder()
				.baseUri(URI.create(parameters.getUrl()))
				.build(GitHubClient.class);
	}

	/**
	 * Gets a list of lectureStudio releases. The list does not include regular
	 * Git tags that have not been associated with a release.
	 *
	 * @return A list of lectureStudio releases.
	 */
	public List<GitHubRelease> getReleases() {
		return providerClient.getReleases();
	}

	/**
	 * Gets the latest published full release of lectureStudio on GitHub. The
	 * latest release is the most recent non-pre-release, non-draft release.
	 *
	 * @return The latest release of lectureStudio.
	 */
	public GitHubRelease getLatestRelease() {
		return providerClient.getLatestRelease();
	}
}

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

package org.lecturestudio.web.api.client;

import java.util.List;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;

import org.eclipse.microprofile.rest.client.annotation.RegisterProvider;
import org.eclipse.microprofile.rest.client.annotation.RegisterProviders;

import org.lecturestudio.web.api.data.bind.JsonConfigProvider;
import org.lecturestudio.web.api.model.GitHubRelease;

/**
 * Client implementation to retrieve project related information from the GitHub
 * project.
 *
 * @author Alex Andres
 */
@Path("/repos")
@RegisterProviders({
	@RegisterProvider(JsonConfigProvider.class)
})
public interface GitHubClient {

	/**
	 * Gets a list of lectureStudio releases. The list does not include regular
	 * Git tags that have not been associated with a release.
	 *
	 * @param owner      The owner (user, organization) on GitHub.
	 * @param repository The repository name on GitHub.
	 *
	 * @return A list of lectureStudio releases.
	 */
	@GET
	@Path("/{owner}/{repository}/releases")
	List<GitHubRelease> getReleases(@PathParam("owner") String owner,
			@PathParam("repository") String repository);

	/**
	 * Gets the latest published full release of lectureStudio on GitHub. The
	 * latest release is the most recent non-pre-release, non-draft release.
	 *
	 * @param owner      The owner (user, organization) on GitHub.
	 * @param repository The repository name on GitHub.
	 *
	 * @return The latest release of lectureStudio.
	 */
	@GET
	@Path("/{owner}/{repository}/releases/latest")
	GitHubRelease getLatestRelease(@PathParam("owner") String owner,
			@PathParam("repository") String repository);

}

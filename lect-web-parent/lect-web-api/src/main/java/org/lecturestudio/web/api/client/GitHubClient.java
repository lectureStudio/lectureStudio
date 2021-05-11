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

import org.lecturestudio.web.api.model.GitHubRelease;

/**
 * Client implementation to retrieve project related information from the GitHub
 * project.
 *
 * @author Alex Andres
 */
@Path("/repos")
public interface GitHubClient {

	/**
	 * Gets a list of lectureStudio releases. The list does not include regular
	 * Git tags that have not been associated with a release.
	 *
	 * @return A list of lectureStudio releases.
	 */
	@GET
	@Path("/lectureStudio/lectureStudio/releases")
	List<GitHubRelease> getReleases();

	/**
	 * Gets the latest published full release of lectureStudio on GitHub. The
	 * latest release is the most recent non-pre-release, non-draft release.
	 *
	 * @return The latest release of lectureStudio.
	 */
	@GET
	@Path("/lectureStudio/lectureStudio/releases/latest")
	GitHubRelease getLatestRelease();

}

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

package org.lecturestudio.web.api.model;

import java.net.URL;
import java.time.LocalDateTime;
import java.util.List;

import javax.json.bind.annotation.JsonbDateFormat;

/**
 * GitHub release container with only relevant fields to give a concise view of
 * the release and it's assets.
 *
 * @author Alex Andres
 */
public class GitHubRelease {

	private URL html_url;

	private String tag_name;

	private Boolean prerelease;

	@JsonbDateFormat(value = "yyyy-MM-dd'T'HH:mm:ss'Z'")
	private LocalDateTime published_at;

	private List<GitHubAsset> assets;


	/**
	 * Gets the GitHub HTML URL of this release.
	 *
	 * @return The GitHub URL of this release.
	 */
	public URL getUrl() {
		return html_url;
	}

	/**
	 * Gets the tag name of this release.
	 *
	 * @return The tag name of this release.
	 */
	public String getTagName() {
		return tag_name;
	}

	/**
	 * Indicates whether this is a pre-release or not.
	 *
	 * @return True if this is a pre-release.
	 */
	public Boolean isPreRelease() {
		return prerelease;
	}

	/**
	 * Gets the date when this release has been published.
	 *
	 * @return The published date.
	 */
	public LocalDateTime getPublishedAt() {
		return published_at;
	}

	/**
	 * Gets all assets associated with this release.
	 *
	 * @return The assets of this release.
	 */
	public List<GitHubAsset> getAssets() {
		return assets;
	}
}

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

/**
 * GitHub release asset with only relevant fields to be used to identify the
 * asset name and it's download URL.
 *
 * @author Alex Andres
 */
public class GitHubAsset {

	private String name;

	private long size;

	private URL browser_download_url;


	/**
	 * Gets get name of this asset. Usually the name represents the filename.
	 *
	 * @return The name of this asset.
	 */
	public String getName() {
		return name;
	}

	/**
	 * Gets the file size of this asset in bytes.
	 *
	 * @return The file size of this asset,
	 */
	public long getSize() {
		return size;
	}

	/**
	 * Gets the download URL of this asset.
	 *
	 * @return The download URL of this asset.
	 */
	public URL getDownloadUrl() {
		return browser_download_url;
	}
}

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

package org.lecturestudio.editor.api.config;

import org.lecturestudio.core.app.configuration.Configuration;
import org.lecturestudio.core.beans.StringProperty;

public class EditorConfiguration extends Configuration {

	/** The path where the video export files are stored at. */
	private final StringProperty videoExportPath = new StringProperty();


	/**
	 * Get the path where the video export files are stored at.
	 *
	 * @return the video export path.
	 */
	public String getVideoExportPath() {
		return videoExportPath.get();
	}

	/**
	 * Set the path where the video export files should be stored.
	 *
	 * @param path the video export path to set.
	 */
	public void setVideoExportPath(String path) {
		this.videoExportPath.set(path);
	}

	/**
	 * Get the video export path property.
	 *
	 * @return the video export path property.
	 */
	public StringProperty videoExportPathProperty() {
		return videoExportPath;
	}
}

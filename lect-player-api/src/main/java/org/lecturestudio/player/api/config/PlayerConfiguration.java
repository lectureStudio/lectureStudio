/*
 * Copyright (C) 2016 TU Darmstadt, Department of Computer Science,
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
package org.lecturestudio.player.api.config;

import org.lecturestudio.core.app.configuration.Configuration;
import org.lecturestudio.core.beans.StringProperty;

public class PlayerConfiguration extends Configuration {

	private final StringProperty documentsPath = new StringProperty();


	/**
	 * @return the documentsPath
	 */
	public String getDocumentsPath() {
		return documentsPath.get();
	}

	/**
	 * @param documentsPath the documentsPath to set
	 */
	public void setDocumentsPath(String documentsPath) {
		this.documentsPath.set(documentsPath);
	}
	
	public StringProperty documentsPathProperty() {
		return documentsPath;
	}
}

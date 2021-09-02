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

package org.lecturestudio.core.app;

import java.util.ArrayList;
import java.util.List;

import org.lecturestudio.core.util.FileUtils;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Theme helper class to conveniently handle application themes. Application
 * theme files have the suffix '.theme.css'.
 *
 * @author Alex Andres
 */
public class ThemeProvider {

	/** Logger for {@link ThemeProvider} */
	private final static Logger LOG = LogManager.getLogger(ThemeProvider.class);


	/**
	 * Get all available UI themes.
	 *
	 * @return the list of all available UI themes.
	 */
	public List<Theme> getThemes() {
		List<Theme> themes = new ArrayList<>();
		themes.add(new Theme("default", null));

		String suffix = ".theme.css";

		try {
			String[] listing = FileUtils.getResourceListing("/resources/css",
				(name) -> name.endsWith(suffix));

			for (String fileName : listing) {
				String name = fileName.substring(0, fileName.lastIndexOf(suffix));
				name = name.substring(name.lastIndexOf("/") + 1);

				themes.add(new Theme(name, fileName));
			}
		}
		catch (Exception e) {
			LOG.warn("Load themes failed", e);
		}

		return themes;
	}

}

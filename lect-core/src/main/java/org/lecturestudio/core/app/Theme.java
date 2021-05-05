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

import java.util.Objects;

/**
 * A Theme defines the graphical appearance of the user interface. Themes can be
 * used to customize the the look and feel of the window and its graphical
 * control elements. Themes are loaded from theme files.
 *
 * @author Alex Andres
 */
public class Theme {

	/** The theme name. */
	private String name;

	/** The theme file. */
	private String file;


	/**
	 * Create a new {@link Theme} with empty name.
	 */
	public Theme() {
		this("", null);
	}

	/**
	 * Create a new {@link Theme} with the specified name and source file.
	 *
	 * @param name The name of the theme.
	 * @param file The file containing theme definitions.
	 */
	public Theme(String name, String file) {
		this.name = name;
		this.file = file;
	}

	/**
	 * Obtain the theme name.
	 *
	 * @return the theme name.
	 */
	public String getName() {
		return name;
	}

	/**
	 * Obtain the theme file.
	 *
	 * @return the theme file.
	 */
	public String getFile() {
		return file;
	}

	@Override
	public boolean equals(Object other) {
		if (this == other) {
			return true;
		}
		if (other == null || getClass() != other.getClass()) {
			return false;
		}

		Theme theme = (Theme) other;

		return name.equals(theme.name) && Objects.equals(file, theme.file);
	}

	@Override
	public int hashCode() {
		return Objects.hash(name, file);
	}
}

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

package org.lecturestudio.editor.api.util;

/**
 * Enum representing types of page replacement operations.
 * Used to specify whether to replace a single page or all pages.
 */
public enum ReplacePageType {

	/**
	 * Represents the operation to replace all pages.
	 */
	REPLACE_ALL_PAGES("allPagesTypeRadio"),

	/**
	 * Represents the operation to replace only the current page.
	 */
	REPLACE_SINGLE_PAGE("currentPageTypeRadio");


	/**
	 * The name identifier associated with this replacement type.
	 */
	private final String name;

	/**
	 * Constructs a ReplacePageType with the specified name.
	 *
	 * @param name the name identifier for this replacement type.
	 */
	ReplacePageType(String name) {
		this.name = name;
	}

	/**
	 * Returns the name identifier of this replacement type.
	 *
	 * @return the name identifier.
	 */
	public String getName() {
		return name;
	}

	/**
	 * Parses a string name and returns the corresponding ReplacePageType.
	 *
	 * @param name the name to parse.
	 *
	 * @return the matching ReplacePageType, or null if no match is found.
	 */
	public static ReplacePageType parse(String name) {
		if (name.equals(REPLACE_ALL_PAGES.getName())) {
			return REPLACE_ALL_PAGES;
		}

		if (name.equals(REPLACE_SINGLE_PAGE.getName())) {
			return REPLACE_SINGLE_PAGE;
		}

		return null;
	}
}

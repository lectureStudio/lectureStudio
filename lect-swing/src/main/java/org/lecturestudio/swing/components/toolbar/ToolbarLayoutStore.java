/*
 * Copyright (C) 2025 TU Darmstadt, Department of Computer Science,
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

package org.lecturestudio.swing.components.toolbar;

import java.util.List;

/**
 * Abstraction for storing and retrieving the ordered layout (item identifiers)
 * of one or more toolbars. Implementations may persist layouts in memory,
 * on disk, or any other backing store.
 *
 * @author Alex Andres
 */
public interface ToolbarLayoutStore {

	/**
	 * Retrieve the ordered list of item identifiers for the given toolbar.
	 *
	 * @param toolbarName the logical name of the toolbar.
	 *
	 * @return ordered list of item identifiers; never {@code null} (empty if none stored).
	 */
	List<String> get(String toolbarName);

	/**
	 * Store (or replace) the ordered list of item identifiers for the given toolbar.
	 *
	 * @param toolbarName the logical name of the toolbar.
	 * @param items       ordered list of item identifiers; must not be {@code null}.
	 */
	void set(String toolbarName, List<String> items);

	/**
	 * Reset the layout for the given toolbar to its default state.
	 *
	 * @param toolbarName the logical name of the toolbar.
	 */
	void reset(String toolbarName);

	/**
	 * Reset all stored toolbar layouts to their default state.
	 */
	void resetAll();

}

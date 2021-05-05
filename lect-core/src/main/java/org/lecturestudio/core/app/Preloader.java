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

import org.lecturestudio.core.ExecutableException;

/**
 * Common interface to provide a consistent mechanism for creating a graphical
 * preloader for an application.
 *
 * @author Alex Andres
 */
public interface Preloader {

	/**
	 * Prepare the preloader for starting. This method should perform any
	 * initialization required post object creation, optionally using the
	 * arguments provided by the {@code main(String[])} method.
	 *
	 * @param args The main method's arguments.
	 *
	 * @throws ExecutableException If a fatal error occurred that prevents this
	 *                             preloader from being used.
	 */
	void init(final String[] args) throws ExecutableException;

	/**
	 * Start the preloader and show the UI.
	 *
	 * @throws ExecutableException If a fatal error occurred that prevents this
	 *                             preloader from being used.
	 */
	void start() throws ExecutableException;

	/**
	 * Close the preloader and hide the UI.
	 *
	 * @throws ExecutableException If a fatal error occurred that prevents this
	 *                             preloader from being closed.
	 */
	void close() throws ExecutableException;

	/**
	 * Cleanup resources used by this preloader.
	 *
	 * @throws ExecutableException If a fatal error occurred that prevents this
	 *                             preloader from being destroyed.
	 */
	void destroy() throws ExecutableException;

}

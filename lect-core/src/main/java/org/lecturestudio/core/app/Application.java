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
import org.lecturestudio.core.ExecutableState;

/**
 * Sub-classes may implement this interface to provide a consistent mechanism to
 * start and stop the application.
 *
 * @author Alex Andres
 */
public interface Application {

	/**
	 * Prepare the application for starting. This method should perform any
	 * initialization required post object creation, optionally using the
	 * arguments provided by the {@code main(String[])} method.
	 *
	 * @param args the main method's arguments.
	 *
	 * @throws ExecutableException If this application detects a fatal error
	 *                             that prevents this application from being
	 *                             used.
	 */
	void init(final String[] args) throws ExecutableException;

	/**
	 * Responsible for starting the application; e.g. for creating and showing
	 * the initial UI.
	 *
	 * @throws ExecutableException If this application detects a fatal error
	 *                             that prevents this application from being
	 *                             used.
	 */
	void start() throws ExecutableException;

	/**
	 * Prepare the application to shut down. Subclasses may override this method
	 * to do any cleanup that is necessary before exiting.
	 *
	 * @throws ExecutableException If this application detects a fatal error
	 *                             that needs to be reported.
	 */
	void stop() throws ExecutableException;

	/**
	 * Cleanup resources used by this application.
	 *
	 * @throws ExecutableException If this application detects a fatal error
	 *                             that prevents this application from being
	 *                             destroyed.
	 */
	void destroy() throws ExecutableException;

	/**
	 * Registers a {@link ApplicationStateListener} on this application.
	 *
	 * @param listener the state listener to be registered.
	 */
	void addStateListener(ApplicationStateListener listener);

	/**
	 * Removes a {@link ApplicationStateListener} from this application.
	 *
	 * @param listener the state listener to be removed.
	 */
	void removeStateListener(ApplicationStateListener listener);

	/**
	 * Obtain the current state of this application.
	 *
	 * @return The current state of this application.
	 */
	ExecutableState getState();

}

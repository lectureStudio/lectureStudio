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

package org.lecturestudio.core;

/**
 * Common interface to provide a consistent mechanism for executable components
 * managed by life cycle methods. The current state can be monitored by
 * observing the {@link ExecutableState}. The state will change to the error
 * state if the attempted transition is not valid.
 *
 * @author Alex Andres
 */
public interface Executable {

	/**
	 * Prepare the executable component for starting. This method should perform
	 * any initialization required post object creation.
	 *
	 * @throws ExecutableException if this component detects a fatal error that
	 *                             prevents this component from being used.
	 */
	void init() throws ExecutableException;

	/**
	 * Prepare for the beginning of active use of this executable component.
	 *
	 * @throws ExecutableException if this component detects a fatal error that
	 *                             prevents this component from being used.
	 */
	void start() throws ExecutableException;

	/**
	 * Stops this executable component.
	 *
	 * @throws ExecutableException if this component detects a fatal error that
	 *                             needs to be reported.
	 */
	void stop() throws ExecutableException;

	/**
	 * Suspends this executable component.
	 *
	 * @throws ExecutableException if this component detects a fatal error that
	 *                             needs to be reported.
	 */
	void suspend() throws ExecutableException;

	/**
	 * Dispose this executable component.
	 *
	 * @throws ExecutableException if this component detects a fatal error that
	 *                             prevents this component from being destroyed.
	 */
	void destroy() throws ExecutableException;

	/**
	 * Obtain the current state of this executable component.
	 *
	 * @return The current state of this component.
	 */
	ExecutableState getState();

	/**
	 * Indicates whether this component has been created.
	 *
	 * @return {@code true} if this component has been created, otherwise {@code
	 * false}.
	 */
	default boolean created() {
		return getState() == ExecutableState.Created;
	}

	/**
	 * Indicates whether this component has been initialized.
	 *
	 * @return {@code true} if this component has been initialized, otherwise
	 * {@code false}.
	 */
	default boolean initialized() {
		return getState() == ExecutableState.Initialized;
	}

	/**
	 * Indicates whether this component has been started.
	 *
	 * @return {@code true} if this component has been started, otherwise {@code
	 * false}.
	 */
	default boolean started() {
		return getState() == ExecutableState.Started;
	}

	/**
	 * Indicates whether this component has been stopped.
	 *
	 * @return {@code true} if this component has been stopped, otherwise {@code
	 * false}.
	 */
	default boolean stopped() {
		return getState() == ExecutableState.Stopped;
	}

	/**
	 * Indicates whether this component has been suspended.
	 *
	 * @return {@code true} if this component has been suspended, otherwise
	 * {@code false}.
	 */
	default boolean suspended() {
		return getState() == ExecutableState.Suspended;
	}

	/**
	 * Indicates whether this component has been destroyed.
	 *
	 * @return {@code true} if this component has been destroyed, otherwise
	 * {@code false}.
	 */
	default boolean destroyed() {
		return getState() == ExecutableState.Destroyed;
	}

	/**
	 * Indicates whether an error has occurred during a state transition of this
	 * component.
	 *
	 * @return {@code true} if an error has occurred, otherwise {@code false}.
	 */
	default boolean error() {
		return getState() == ExecutableState.Error;
	}
}

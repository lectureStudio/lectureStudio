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
 * Valid states for executable components that implement the {@link Executable}
 * interface.
 *
 * @author Alex Andres
 */
public enum ExecutableState {

	/** The component has been created but not initialized yet. */
	Created,

	/** The component is being initialized. */
	Initializing,

	/** The component has been successfully initialized. */
	Initialized,

	/** The component is being started. */
	Starting,

	/** The component has been successfully started. */
	Started,

	/** The component is being stopped. */
	Stopping,

	/** The component has been successfully stopped. */
	Stopped,

	/** The component is being suspended. */
	Suspending,

	/** The component has been successfully suspended. */
	Suspended,

	/** The component is being destroyed. */
	Destroying,

	/** The component has been successfully destroyed. */
	Destroyed,

	/** An fatal error has occurred during a state transition. */
	Error

}

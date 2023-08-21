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

import static java.util.Objects.nonNull;

import java.util.ArrayList;
import java.util.List;

/**
 * Observes executable components for collective states. If all registered
 * components reach the same state, a corresponding event will be dispatched.
 *
 * @author Alex Andres
 */
public class ExecutableStateObserver {

	private final List<ExecutableBase> executables = new ArrayList<>();

	private final ExecutableStateListener listener = this::onStateChange;

	private Runnable onStarted;

	private Runnable onStopped;


	/**
	 * Registers an executable component for state observation.
	 *
	 * @param executable The executable component to register.
	 */
	public void addExecutable(ExecutableBase executable) {
		executable.addStateListener(listener);

		executables.add(executable);
	}

	/**
	 * Unregisters an executable component from state observation.
	 *
	 * @param executable The executable component to remove.
	 */
	public void removeExecutable(ExecutableBase executable) {
		executable.removeStateListener(listener);

		executables.remove(executable);
	}

	/**
	 * Unregisters all executable components from state observation.
	 */
	public void removeAllExecutables() {
		executables.forEach(e -> e.removeStateListener(listener));
		executables.clear();
	}

	/**
	 * Sets the listener that is called when all registered components have
	 * reached started state.
	 *
	 * @param listener The listener to be called when all components have
	 *                 started.
	 */
	public void setStartedListener(Runnable listener) {
		this.onStarted = listener;
	}

	/**
	 * Sets the listener that is called when all registered components have
	 * reached stopped state.
	 *
	 * @param listener The listener to be called when all components have
	 *                 stopped.
	 */
	public void setStoppedListener(Runnable listener) {
		this.onStopped = listener;
	}

	private void onStateChange(ExecutableState oldState, ExecutableState newState) {
		int startedCount = 0;
		int stoppedCount = 0;

		for (ExecutableBase executable : executables) {
			if (executable.getState() == ExecutableState.Started) {
				startedCount++;
			}
			else if (executable.getState() == ExecutableState.Stopped) {
				stoppedCount++;
			}
		}

		// All started.
		if (startedCount == executables.size() && nonNull(onStarted)) {
			onStarted.run();
		}
		// All stopped.
		if (stoppedCount == executables.size() && nonNull(onStopped)) {
			onStopped.run();
		}
	}
}

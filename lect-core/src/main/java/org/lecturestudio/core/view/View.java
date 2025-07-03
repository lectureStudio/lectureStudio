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

package org.lecturestudio.core.view;

import static java.util.Objects.nonNull;

/**
 * Represents a view component in the application. Provides utility methods for executing actions.
 *
 * @author Alex Andres
 */
public interface View {

	/**
	 * Executes the given action if it is not null.
	 *
	 * @param action the action to execute
	 */
	default void executeAction(Action action) {
		if (nonNull(action)) {
			action.execute();
		}
	}

	/**
	 * Executes the given consumer action with the provided parameter if the action is not null.
	 *
	 * @param <T>    the type of the parameter.
	 * @param action the consumer action to execute.
	 * @param param  the parameter to pass to the action.
	 */
	default <T> void executeAction(ConsumerAction<T> action, T param) {
		if (nonNull(action)) {
			action.execute(param);
		}
	}

}

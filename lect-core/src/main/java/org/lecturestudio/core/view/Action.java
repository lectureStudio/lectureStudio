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

import static java.util.Objects.isNull;

import java.util.Objects;

/**
 * Represents an action that takes no arguments and returns no result.
 *
 * @author Alex Andres
 */
@FunctionalInterface
public interface Action {

	/**
	 * Performs this operation.
	 */
	void execute();

	/**
	 * Concatenates two given actions. If the first {@code Action} is {@code null},
	 * then the next {@code Action} is returned.
	 *
	 * @param first the first action.
	 * @param next the action to concatenate to the first action.
	 *
	 * @return the concatenated actions.
	 *
	 * @see #andThen(Action)
	 */
	static Action concatenate(Action first, Action next) {
		return isNull(first) ? next : first.andThen(next);
	}

	/**
	 * Returns a composed {@code Action} that executes, in sequence, this
	 * action followed by the {@code next} action. If performing either
	 * action throws an exception, it is relayed to the caller of the
	 * composed action. If performing this action throws an exception,
	 * the {@code next} action will not be performed.
	 *
	 * @param next the action to perform after this action.
	 *
	 * @return a composed {@code Action} that executes in sequence this
	 * action followed by the {@code next} action.
	 *
	 * @throws NullPointerException if {@code next} is null.
	 */
	default Action andThen(Action next) {
		Objects.requireNonNull(next);

		return () -> {
			execute();
			next.execute();
		};
	}

}

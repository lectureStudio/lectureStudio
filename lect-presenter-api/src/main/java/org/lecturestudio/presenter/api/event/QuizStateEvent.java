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

package org.lecturestudio.presenter.api.event;

import org.lecturestudio.core.ExecutableState;
import org.lecturestudio.core.bus.event.ExecutableEvent;

/**
 * Event that represents a state change in a quiz. This event is fired when a quiz component
 * changes its executable state.
 *
 * @author Alex Andres
 */
public class QuizStateEvent extends ExecutableEvent {

	/**
	 * Creates a new quiz state event with the specified executable state.
	 *
	 * @param state The new state of the quiz.
	 */
	public QuizStateEvent(ExecutableState state) {
		super(state);
	}

}

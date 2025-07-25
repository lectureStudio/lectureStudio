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

package org.lecturestudio.presenter.api.view;

import org.lecturestudio.core.view.Action;
import org.lecturestudio.core.view.ConsumerAction;
import org.lecturestudio.core.view.View;
import org.lecturestudio.web.api.model.quiz.QuizOption;

/**
 * Interface representing a view for creating and managing quiz options.
 * Provides functionality for editing quiz options, handling UI events, and managing option navigation.
 *
 * @author Alex Andres
 */
public interface CreateQuizOptionView extends View {

	/**
	 * Sets focus to this view component.
	 */
	void focus();

	/**
	 * Retrieves the current quiz option.
	 *
	 * @return The current quiz option.
	 */
	QuizOption getOption();

	/**
	 * Sets the quiz option to be displayed or edited.
	 *
	 * @param option The quiz option to set.
	 */
	void setOption(QuizOption option);

	/**
	 * Adds an action listener to be triggered when the quiz option changes.
	 *
	 * @param action The action to be executed when the option changes.
	 */
	void addOnChange(Action action);

	/**
	 * Adds an action listener to be triggered when the "correct" state of the option changes.
	 *
	 * @param action The action to be executed when the correct state changes,
	 *               with the new state as parameter.
	 */
	void addOnChangeCorrect(ConsumerAction<Boolean> action);

	/**
	 * Sets the action to be executed when the remove option is triggered.
	 *
	 * @param action The action to be executed on removal.
	 */
	void setOnRemove(Action action);

	/**
	 * Sets the action to be executed when moving the option up in the list.
	 *
	 * @param action The action to be executed when moving up.
	 */
	void setOnMoveUp(Action action);

	/**
	 * Sets the action to be executed when moving the option down in the list.
	 *
	 * @param action The action to be executed when moving down.
	 */
	void setOnMoveDown(Action action);

	/**
	 * Sets the action to be executed when the 'Enter' key is pressed.
	 *
	 * @param action The action to be executed on Enter key press.
	 */
	void setOnEnterKey(Action action);

	/**
	 * Sets the action to be executed when a 'Tab' key is pressed.
	 *
	 * @param action The action to be executed on Tab key press.
	 */
	void setOnTabKey(Action action);

}

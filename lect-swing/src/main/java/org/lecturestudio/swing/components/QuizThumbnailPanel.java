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

package org.lecturestudio.swing.components;

import java.awt.Color;

import javax.swing.JButton;

import org.lecturestudio.core.ExecutableState;
import org.lecturestudio.core.app.dictionary.Dictionary;
import org.lecturestudio.core.view.Action;
import org.lecturestudio.swing.util.SwingUtils;

public class QuizThumbnailPanel extends ThumbnailPanel {

	private final JButton stopQuizButton;


	public QuizThumbnailPanel(Dictionary dict) {
		super();

		stopQuizButton = new JButton(dict.get("slides.quiz.stop"));
		stopQuizButton.setBackground(Color.decode("#FEE2E2"));

		SwingUtils.bindAction(stopQuizButton, () -> {
			stopQuizButton.setEnabled(false);    // One-time action.
		});

		addButton(stopQuizButton);

		setEnabled(false);
	}

	public void setOnStopQuiz(Action action) {
		SwingUtils.bindAction(stopQuizButton, action);
	}

	public void setQuizState(ExecutableState state) {
		if (state == ExecutableState.Started) {
			stopQuizButton.setEnabled(true);

			setEnabled(false);
		}
		else if (state == ExecutableState.Stopped) {
			stopQuizButton.setEnabled(false);

			setEnabled(true);
		}
	}
}

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

package org.lecturestudio.presenter.swing.view;

import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;

import java.awt.BorderLayout;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;

import javax.swing.*;

import org.lecturestudio.core.view.ConsumerAction;
import org.lecturestudio.presenter.api.view.CreateQuizDefaultOptionView;
import org.lecturestudio.swing.event.DefaultDocumentListener;
import org.lecturestudio.swing.util.SwingUtils;
import org.lecturestudio.swing.view.SwingView;
import org.lecturestudio.swing.view.ViewPostConstruct;
import org.lecturestudio.web.api.model.quiz.QuizOption;

@SwingView(name = "quiz-default-option")
public class SwingQuizDefaultOptionView extends SwingQuizOptionView implements CreateQuizDefaultOptionView {

	private final QuizOption option = new QuizOption("", false);

	private JTextField optionTextField;

	private JCheckBox correctCheckBox;


	SwingQuizDefaultOptionView() {
		super();
	}

	@Override
	public void focus() {
		SwingUtils.invoke(() -> {
			optionTextField.requestFocus();
		});
	}

	@Override
	public QuizOption getOption() {
		return option;
	}

	@Override
	public void setOption(QuizOption option) {
		SwingUtils.invoke(() -> {
			this.option.setOptionText(option.getOptionText());
			this.option.setCorrect(option.isCorrect());
		});
	}

	@Override
	public void addOnChangeCorrect(ConsumerAction<Boolean> action) {
		SwingUtils.bindAction(correctCheckBox, action);
	}

	@Override
	void setOptionTooltip(String tooltip) {
		optionTextField.setToolTipText(tooltip);
	}

	@ViewPostConstruct
	private void initialize() {
		if (isNull(optionTextField)) {
			return;
		}
		if (nonNull(getButtons())) {
			add(getButtons(), BorderLayout.EAST);
		}

		optionTextField.addKeyListener(new KeyAdapter() {

			@Override
			public void keyPressed(KeyEvent e) {
				upDownKeyHandler(e);
			}
		});
		optionTextField.addKeyListener(new KeyAdapter() {

			@Override
			public void keyPressed(KeyEvent e) {
				enterKeyHandler(e);
			}
		});
		optionTextField.addKeyListener(new KeyAdapter() {

			@Override
			public void keyPressed(KeyEvent e) {
				tabKeyHandler(e);
			}
		});
		optionTextField.getDocument().addDocumentListener(new DefaultDocumentListener(super::fireChange));

		correctCheckBox.addActionListener(e -> fireChange());

		SwingUtils.bindBidirectional(optionTextField, option.textProperty());
		SwingUtils.bindBidirectional(correctCheckBox, option.correctProperty());
	}

}

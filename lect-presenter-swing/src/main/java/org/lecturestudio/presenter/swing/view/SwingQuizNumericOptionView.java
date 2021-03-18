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
import java.text.NumberFormat;
import java.text.ParseException;

import javax.swing.JTextField;

import org.lecturestudio.presenter.api.view.CreateQuizNumericOptionView;
import org.lecturestudio.swing.util.SwingUtils;
import org.lecturestudio.swing.view.SwingView;
import org.lecturestudio.swing.view.ViewPostConstruct;

@SwingView(name = "quiz-numeric-option")
public class SwingQuizNumericOptionView extends SwingQuizOptionView implements CreateQuizNumericOptionView {

	private final NumberFormat numberFormat;

	private JTextField optionTextField;

	private JTextField minTextField;

	private JTextField maxTextField;


	SwingQuizNumericOptionView() {
		super();

		numberFormat = NumberFormat.getNumberInstance();
	}

	@Override
	public void focus() {
		SwingUtils.invoke(() -> {
			optionTextField.requestFocus();
		});
	}

	@Override
	public String getOptionText() {
		return optionTextField.getText();
	}

	@Override
	public void setOptionText(String text) {
		SwingUtils.invoke(() -> optionTextField.setText(text));
	}

	@Override
	void setOptionTooltip(String tooltip) {
		optionTextField.setToolTipText(tooltip);
	}

	@Override
	public int getMinValue() {
		try {
			return numberFormat.parse(minTextField.getText()).intValue();
		}
		catch (ParseException e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public int getMaxValue() {
		try {
			return numberFormat.parse(maxTextField.getText()).intValue();
		}
		catch (ParseException e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public void setMinValue(int value) {
		minTextField.setText(numberFormat.format(value));
	}

	@Override
	public void setMaxValue(int value) {
		maxTextField.setText(numberFormat.format(value));
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
		maxTextField.addKeyListener(new KeyAdapter() {

			@Override
			public void keyPressed(KeyEvent e) {
				enterKeyHandler(e);
			}
		});
		maxTextField.addKeyListener(new KeyAdapter() {

			@Override
			public void keyPressed(KeyEvent e) {
				tabKeyHandler(e);
			}
		});
	}
}

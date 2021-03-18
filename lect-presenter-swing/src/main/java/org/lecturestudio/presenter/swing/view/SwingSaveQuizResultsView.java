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

import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JTextField;

import org.lecturestudio.core.beans.IntegerProperty;
import org.lecturestudio.core.beans.StringProperty;
import org.lecturestudio.core.view.Action;
import org.lecturestudio.core.view.ConsumerAction;
import org.lecturestudio.presenter.api.view.SaveQuizResultsView;
import org.lecturestudio.swing.components.ContentPane;
import org.lecturestudio.swing.util.SwingUtils;
import org.lecturestudio.swing.view.SwingView;
import org.lecturestudio.swing.view.ViewPostConstruct;

@SwingView(name = "save-quiz-result")
public class SwingSaveQuizResultsView extends ContentPane implements SaveQuizResultsView {

	private final IntegerProperty anySelected;

	private JTextField pathTextField;

	private JCheckBox csvCheckBox;

	private JCheckBox pdfCheckBox;

	private JButton closeButton;

	private JButton saveButton;

	private JButton selectPathButton;


	SwingSaveQuizResultsView() {
		super();

		anySelected = new IntegerProperty();
	}

	@Override
	public void setSavePath(StringProperty path) {
		SwingUtils.bindBidirectional(pathTextField, path);
	}

	@Override
	public void selectCsvOption(boolean select) {
		SwingUtils.invoke(() -> csvCheckBox.setSelected(select));
	}

	@Override
	public void selectPdfOption(boolean select) {
		SwingUtils.invoke(() -> pdfCheckBox.setSelected(select));
	}

	@Override
	public void setOnCsvSelection(ConsumerAction<Boolean> action) {
		SwingUtils.bindAction(csvCheckBox, action);
	}

	@Override
	public void setOnPdfSelection(ConsumerAction<Boolean> action) {
		SwingUtils.bindAction(pdfCheckBox, action);
	}

	@Override
	public void setOnClose(Action action) {
		SwingUtils.bindAction(closeButton, action);
	}

	@Override
	public void setOnSave(Action action) {
		SwingUtils.bindAction(saveButton, action);
	}

	@Override
	public void setOnSelectPath(Action action) {
		SwingUtils.bindAction(selectPathButton, action);
	}

	@ViewPostConstruct
	private void initialize() {
		ItemListener listener = e -> {
			if (e.getStateChange() == ItemEvent.SELECTED) {
				anySelected.set(anySelected.get() + 1);
			}
			else {
				anySelected.set(anySelected.get() - 1);
			}
		};

		csvCheckBox.addItemListener(listener);
		pdfCheckBox.addItemListener(listener);

		anySelected.addListener((observable, oldValue, newValue) -> {
			saveButton.setEnabled(newValue > 0);
		});
	}

}

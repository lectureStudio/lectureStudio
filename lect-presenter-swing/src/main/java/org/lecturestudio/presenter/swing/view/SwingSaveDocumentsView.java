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

import java.awt.Component;
import java.awt.Container;

import javax.swing.JButton;
import javax.swing.JTextField;

import org.lecturestudio.core.beans.IntegerProperty;
import org.lecturestudio.core.beans.StringProperty;
import org.lecturestudio.core.view.Action;
import org.lecturestudio.presenter.api.view.SaveDocumentOptionView;
import org.lecturestudio.presenter.api.view.SaveDocumentsView;
import org.lecturestudio.swing.components.ContentPane;
import org.lecturestudio.swing.util.SwingUtils;
import org.lecturestudio.swing.view.SwingView;
import org.lecturestudio.swing.view.ViewPostConstruct;

@SwingView(name = "save-documents")
public class SwingSaveDocumentsView extends ContentPane implements SaveDocumentsView {

	private final IntegerProperty anySelected;

	private Container individualContainer;

	private JTextField pathTextField;

	private JButton closeButton;

	private JButton selectPathButton;

	private JButton saveMergedButton;


	SwingSaveDocumentsView() {
		super();

		anySelected = new IntegerProperty();
	}

	@Override
	public void addDocumentOptionView(SaveDocumentOptionView optionView) {
		if (!SwingUtils.isComponent(optionView)) {
			throw new RuntimeException("View expected to be a Component");
		}

		optionView.setOnSelectDocument(() -> {
			anySelected.set(anySelected.get() + 1);
		});
		optionView.setOnDeselectDocument(() -> {
			anySelected.set(anySelected.get() - 1);
		});

		SwingUtils.invoke(() -> {
			individualContainer.add((Component) optionView);
			individualContainer.revalidate();
		});
	}

	@Override
	public void setSavePath(StringProperty path) {
		SwingUtils.bindBidirectional(pathTextField, path);
	}

	@Override
	public void setOnClose(Action action) {
		SwingUtils.bindAction(closeButton, action);
	}

	@Override
	public void setOnMerge(Action action) {
		SwingUtils.bindAction(saveMergedButton, action);
	}

	@Override
	public void setOnSelectPath(Action action) {
		SwingUtils.bindAction(selectPathButton, action);
	}

	@ViewPostConstruct
	private void initialize() {
		anySelected.addListener((observable, oldValue, newValue) -> {
			saveMergedButton.setEnabled(newValue > 0);
		});
	}

}

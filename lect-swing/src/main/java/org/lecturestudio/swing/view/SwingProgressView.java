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

package org.lecturestudio.swing.view;

import java.util.ResourceBundle;

import javax.inject.Inject;
import javax.swing.JButton;
import javax.swing.JProgressBar;
import javax.swing.SwingUtilities;
import javax.swing.border.EmptyBorder;

import org.lecturestudio.core.view.Action;
import org.lecturestudio.core.view.NotificationType;
import org.lecturestudio.core.view.ProgressView;
import org.lecturestudio.swing.components.NotificationPane;
import org.lecturestudio.swing.util.SwingUtils;

public class SwingProgressView extends NotificationPane implements ProgressView, ComponentView {

	private Action closeAction;

	private Action viewShownAction;

	private JProgressBar progressIndicator;

	private JButton closeButton;


	@Inject
	SwingProgressView(ResourceBundle resources) {
		super();

		initialize(resources);
	}

	@Override
	public void setError(String message) {
		setType(NotificationType.ERROR);
		setMessage(message);
	}

	@Override
	public void setProgress(double progress) {
		progressIndicator.setValue((int) (progress * 100));
	}

	@Override
	public void setOnClose(Action action) {
		closeAction = Action.concatenate(closeAction, action);

		SwingUtils.bindAction(closeButton, action);
	}

	@Override
	public void setOnViewShown(Action action) {
		viewShownAction = Action.concatenate(viewShownAction, action);
	}

	@Override
	public void onParentSet() {
		SwingUtilities.invokeLater(() -> executeAction(viewShownAction));
	}

	private void initialize(ResourceBundle resources) {
		registerOnParent();

		progressIndicator = new JProgressBar();
		progressIndicator.setBorder(new EmptyBorder(0, 0, 20, 0));

		closeButton = new JButton(resources.getString("button.close"));

		setContent(progressIndicator);
		addButton(closeButton);

		closeButton.requestFocus();
	}
}

/*
 * Copyright (C) 2023 TU Darmstadt, Department of Computer Science,
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

import java.util.ResourceBundle;

import javax.inject.Inject;
import javax.swing.JButton;

import org.lecturestudio.core.view.Action;
import org.lecturestudio.presenter.api.view.RemindRecordingView;
import org.lecturestudio.swing.components.NotificationPane;
import org.lecturestudio.swing.util.SwingUtils;

public class SwingRemindRecordingView extends NotificationPane implements RemindRecordingView {

	private JButton recordButton;

	private JButton closeButton;


	@Inject
	SwingRemindRecordingView(ResourceBundle resources) {
		super();

		initialize(resources);
	}

	@Override
	public void setOnClose(Action action) {
		SwingUtils.bindAction(closeButton, action);
	}

	@Override
	public void setOnRecord(Action action) {
		SwingUtils.bindAction(recordButton, action);
	}

	private void initialize(ResourceBundle resources) {
		recordButton = new JButton(resources.getString("button.record"));
		closeButton = new JButton(resources.getString("button.close"));

		addButton(recordButton);
		addButton(closeButton);
	}
}

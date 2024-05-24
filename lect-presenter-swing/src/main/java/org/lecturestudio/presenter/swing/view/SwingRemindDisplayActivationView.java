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

import java.util.ResourceBundle;

import javax.inject.Inject;
import javax.swing.*;

import org.lecturestudio.core.view.Action;
import org.lecturestudio.presenter.api.view.RemindDisplayActivationView;
import org.lecturestudio.swing.components.NotificationPane;
import org.lecturestudio.swing.util.SwingUtils;

public class SwingRemindDisplayActivationView extends NotificationPane implements RemindDisplayActivationView {

	private JButton activateButton;

	private JButton closeButton;


	@Inject
	SwingRemindDisplayActivationView(ResourceBundle resources) {
		super();

		initialize(resources);
	}

	@Override
	public void setOnActivate(Action action) {
		SwingUtils.bindAction(activateButton, action);
	}

	@Override
	public void setOnClose(Action action) {
		SwingUtils.bindAction(closeButton, action);
	}

	private void initialize(ResourceBundle resources) {
		activateButton = new JButton(resources.getString("button.activate"));
		closeButton = new JButton(resources.getString("button.close"));

		addButton(activateButton);
		addButton(closeButton);
	}
}

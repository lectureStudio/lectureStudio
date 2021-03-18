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

import javax.swing.JButton;
import javax.swing.event.AncestorEvent;
import javax.swing.event.AncestorListener;

import org.lecturestudio.core.view.Action;
import org.lecturestudio.presenter.api.view.ConfirmStopRecordingView;
import org.lecturestudio.swing.components.NotificationPane;
import org.lecturestudio.swing.util.SwingUtils;
import org.lecturestudio.swing.view.SwingView;
import org.lecturestudio.swing.view.ViewPostConstruct;

@SwingView(name = "stop-recording")
public class SwingConfirmStopRecordingView extends NotificationPane implements ConfirmStopRecordingView {

	private JButton stopButton;

	private JButton continueButton;


	SwingConfirmStopRecordingView() {
		super();
	}

	@Override
	public void setOnStopRecording(Action action) {
		SwingUtils.bindAction(stopButton, action);
	}

	@Override
	public void setOnContinueRecording(Action action) {
		SwingUtils.bindAction(continueButton, action);
	}

	@Override
	public void setOnClose(Action action) {

	}

	@ViewPostConstruct
	private void initialize() {
		addAncestorListener(new AncestorListener() {

			@Override
			public void ancestorAdded(AncestorEvent event) {
				continueButton.requestFocus();
			}

			@Override
			public void ancestorRemoved(AncestorEvent event) {

			}

			@Override
			public void ancestorMoved(AncestorEvent event) {

			}
		});
	}
}

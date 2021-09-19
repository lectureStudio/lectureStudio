/*
 * Copyright (C) 2021 TU Darmstadt, Department of Computer Science,
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

import java.awt.BorderLayout;
import java.awt.Color;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;

import org.lecturestudio.core.app.dictionary.Dictionary;
import org.lecturestudio.core.view.Action;

public class SpeechRequestView extends MessagePanel {

	private long requestId;

	private JLabel stateLabel;

	private JButton acceptButton;

	private JButton rejectButton;


	public SpeechRequestView(Dictionary dict) {
		super(dict);
	}

	public void setOnAccept(Action action) {
		acceptButton.addActionListener(e -> {
			setAccepted();

			action.execute();
		});
	}

	public void setOnReject(Action action) {
		rejectButton.addActionListener(e -> {
			setRejected();

			action.execute();
		});
	}

	private void setAccepted() {
		setState(dict.get("speech.accepted"));
	}

	public void setCanceled() {
		setState(dict.get("speech.canceled"));
	}

	private void setRejected() {
		setState(dict.get("speech.rejected"));
	}

	public long getRequestId() {
		return requestId;
	}

	public void setRequestId(long id) {
		requestId = id;
	}

	@Override
	protected void createContent(JPanel content) {
		stateLabel = new JLabel();
		stateLabel.setBorder(BorderFactory.createEmptyBorder(0, 10, 0, 10));
		stateLabel.setText(dict.get("speech.requested"));

		acceptButton = new JButton(dict.get("speech.accept"));
		acceptButton.setBackground(Color.decode("#D1FAE5"));

		rejectButton = new JButton(dict.get("speech.reject"));
		rejectButton.setBackground(Color.decode("#FEE2E2"));

		Box controlPanel = Box.createHorizontalBox();
		controlPanel.setBorder(BorderFactory.createEmptyBorder(2, 5, 2, 5));
		controlPanel.setOpaque(false);
		controlPanel.add(fromLabel);
		controlPanel.add(Box.createHorizontalGlue());
		controlPanel.add(rejectButton);
		controlPanel.add(Box.createHorizontalStrut(5));
		controlPanel.add(acceptButton);
		controlPanel.add(Box.createHorizontalStrut(10));
		controlPanel.add(timeLabel);

		content.add(controlPanel, BorderLayout.NORTH);
		content.add(stateLabel, BorderLayout.CENTER);
	}

	private void setState(String state) {
		stateLabel.setForeground(Color.BLUE);
		stateLabel.setText(state);
	}
}

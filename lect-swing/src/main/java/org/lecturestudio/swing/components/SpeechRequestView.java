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

import static java.util.Objects.isNull;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.text.MessageFormat;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;

import org.lecturestudio.core.app.dictionary.Dictionary;
import org.lecturestudio.core.view.ConsumerAction;
import org.lecturestudio.swing.border.RoundedBorder;

public class SpeechRequestView extends JPanel {

	private final Dictionary dict;

	private long requestId;

	private JPanel actionPane;

	private JLabel fromLabel;

	private JLabel timeLabel;

	private JButton acceptButton;

	private JButton rejectButton;


	public SpeechRequestView(Dictionary dict) {
		super();

		this.dict = dict;

		initialize();
	}

	public void setOnAccept(ConsumerAction<Long> action) {
		acceptButton.addActionListener(e -> {
			action.execute(requestId);
		});
	}

	public void setOnReject(ConsumerAction<Long> action) {
		rejectButton.addActionListener(e -> {
			action.execute(requestId);
		});
	}

	public void setCanceled() {
		setStatus(dict.get("speech.canceled"));
	}

	private void setRejected() {
		setStatus(dict.get("speech.rejected"));
	}

	public long getRequestId() {
		return requestId;
	}

	public void setRequestId(long id) {
		requestId = id;
	}

	public void setDate(ZonedDateTime date) {
		if (isNull(date)) {
			return;
		}

		ZonedDateTime dateUTC = date.withZoneSameInstant(ZoneId.systemDefault());
		String formattedDate = dateUTC.format(DateTimeFormatter.ofPattern("H:mm"));

		timeLabel.setText(formattedDate);
	}

	public void setUserName(String user) {
		fromLabel.setText(MessageFormat.format(dict.get("speech.from"), user));
	}

	public void pack() {
		setPreferredSize(new Dimension(getPreferredSize().width, getPreferredSize().height));
		setMaximumSize(new Dimension(getMaximumSize().width, getPreferredSize().height));
		setMinimumSize(new Dimension(200, getPreferredSize().height));
	}

	private void initialize() {
		setLayout(new BorderLayout(1, 1));
		setBackground(Color.WHITE);
		setBorder(new RoundedBorder(Color.LIGHT_GRAY, 5));

		GridBagConstraints constraints = new GridBagConstraints();
		constraints.anchor = GridBagConstraints.WEST;
		constraints.gridx = 0;
		constraints.weightx = 1.D;

		fromLabel = new JLabel();

		JPanel controlPanel = new JPanel(new GridBagLayout());
		controlPanel.setBorder(BorderFactory.createEmptyBorder(2, 5, 2, 5));
		controlPanel.setOpaque(false);
		controlPanel.add(fromLabel, constraints);

		constraints.anchor = GridBagConstraints.EAST;
		constraints.weightx = 1.D;
		constraints.gridx = 1;

		timeLabel = new JLabel();
		timeLabel.setForeground(Color.BLUE);

		controlPanel.add(timeLabel, constraints);

		actionPane = new JPanel();
		actionPane.setBorder(BorderFactory.createEmptyBorder(3, 5, 3, 5));
		actionPane.setLayout(new BoxLayout(actionPane, BoxLayout.LINE_AXIS));
		actionPane.setOpaque(false);

		acceptButton = new JButton(dict.get("speech.accept"));
		acceptButton.setBackground(Color.decode("#D1FAE5"));

		rejectButton = new JButton(dict.get("speech.reject"));
		rejectButton.setBackground(Color.decode("#FEE2E2"));
		rejectButton.addActionListener(e -> {
			setRejected();
		});

		actionPane.add(rejectButton);
		actionPane.add(Box.createHorizontalStrut(10));
		actionPane.add(acceptButton);

		add(controlPanel, BorderLayout.NORTH);
		add(actionPane, BorderLayout.CENTER);
	}

	private void setStatus(String status) {
		JLabel statusLabel = new JLabel(status);
		statusLabel.setForeground(Color.BLUE);

		actionPane.removeAll();
		actionPane.revalidate();
		actionPane.repaint();
		actionPane.add(statusLabel);
	}
}

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

package org.lecturestudio.swing.components;

import static java.util.Objects.nonNull;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.text.SimpleDateFormat;
import java.util.Date;

import javax.swing.*;

public class MessageView extends JPanel {

	private final SimpleDateFormat dateFormat = new SimpleDateFormat("H:mm");

	private JLabel fromLabel;

	private JLabel timeLabel;

	private JTextArea textArea;

	private JComponent imageView;

	private BufferedImage image;

	public MessageView() {
		super();

		initialize();
	}

	public void setDate(Date date) {
		timeLabel.setText(nonNull(date) ? dateFormat.format(date) : "");
	}

	public void setHost(String host) {
		fromLabel.setText(host);
	}

	public void setMessage(String message) {
		textArea.setText(message);
	}

	/**
	 * Method to either insert a picture from an incoming message or remove the reserved space
	 * @param picture BufferedImage which should be displayed
	 */
	public void setImage(BufferedImage picture) {
		image = picture;
		if(image == null){
			imageView.setPreferredSize(new Dimension(0, 0));
		}
		repaint();
	}


	private void initialize() {
		setLayout(new BorderLayout(1, 1));
		setBackground(Color.WHITE);
		setBorder(BorderFactory.createMatteBorder(1, 1, 1, 1, Color.GRAY));

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

		textArea = new JTextArea();
		textArea.setOpaque(false);
		textArea.setEditable(false);
		textArea.setLineWrap(true);
		textArea.setWrapStyleWord(true);
		textArea.setFont(textArea.getFont().deriveFont(12f));

		imageView = new JComponent() {

			@Override
			protected void paintComponent(Graphics g) {
				if(image != null){
					super.paintComponent(g);
					g.drawImage(image, 0, 0,256, 144,  null);
				}
			}
		};
		imageView.setPreferredSize(new Dimension(256, 144));


		JScrollPane scrollPane = new JScrollPane(textArea);
		scrollPane.setBorder(BorderFactory.createEmptyBorder(2, 5, 2, 5));
		scrollPane.setOpaque(false);
		scrollPane.getViewport().setOpaque(false);


		add(controlPanel, BorderLayout.NORTH);
		add(scrollPane, BorderLayout.CENTER);
		add(imageView, BorderLayout.SOUTH);

	}


}

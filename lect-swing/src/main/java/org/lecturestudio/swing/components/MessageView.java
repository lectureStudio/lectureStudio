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

import java.awt.BorderLayout;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;

import org.lecturestudio.core.app.dictionary.Dictionary;
import org.lecturestudio.core.view.Action;
import org.lecturestudio.swing.util.SwingUtils;

public class MessageView extends MessagePanel {

	private JButton discardButton;

	private JTextArea textArea;


	public MessageView(Dictionary dict) {
		super(dict);
	}

	public void setMessage(String message) {
		textArea.setText(message);
	}

	public void setOnDiscard(Action action) {
		SwingUtils.bindAction(discardButton, action);
	}

	@Override
	protected void createContent(JPanel content) {
		discardButton = new JButton(dict.get("button.discard"));

		Box controlPanel = Box.createHorizontalBox();
		controlPanel.setBorder(BorderFactory.createEmptyBorder(2, 5, 2, 5));
		controlPanel.setOpaque(false);
		controlPanel.add(fromLabel);
		controlPanel.add(Box.createHorizontalGlue());
		controlPanel.add(discardButton);
		controlPanel.add(Box.createHorizontalStrut(10));
		controlPanel.add(timeLabel);

		textArea = new JTextArea();
		textArea.setOpaque(false);
		textArea.setEditable(false);
		textArea.setLineWrap(true);
		textArea.setWrapStyleWord(true);
		textArea.setFont(textArea.getFont().deriveFont(12f));

		JScrollPane scrollPane = new JScrollPane(textArea);
		scrollPane.setBorder(BorderFactory.createEmptyBorder(2, 5, 2, 5));
		scrollPane.setOpaque(false);
		scrollPane.getViewport().setOpaque(false);

		content.add(controlPanel, BorderLayout.NORTH);
		content.add(scrollPane, BorderLayout.CENTER);
	}
}

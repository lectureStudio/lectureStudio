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
import java.awt.Dimension;
import java.awt.Insets;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JTextArea;

import org.lecturestudio.core.app.dictionary.Dictionary;
import org.lecturestudio.core.view.Action;
import org.lecturestudio.swing.AwtResourceLoader;
import org.lecturestudio.swing.util.SwingUtils;

public class MessageView extends MessagePanel {

	private JButton discardButton;

	protected JTextArea textArea;

	private JButton createSlideButton;

	private String messageId;

	public MessageView(Dictionary dict) {
		super(dict);
	}

	public void setMessage(String message, String correspondingMessageId) {
		textArea.setText(message);
		this.messageId = correspondingMessageId;
	}

	public void setPrivateText(String text) {
		privateLabel.setText(text);
		privateLabel.setVisible(true);
	}

	public void setOnDiscard(Action action) {
		SwingUtils.bindAction(discardButton, action);
	}

	public void setOnCreateSlide(Action action) {
		SwingUtils.bindAction(createSlideButton, action);
	}

	public String getMessageId() {
		return messageId;
	}

	public void setIsEdited() {
		editedLabel.setVisible(true);
	}

	@Override
	protected void createContent(JPanel content) {
		initComponents();

		Box userPanel = createUserPanel();

		Box timeEditedPanel = Box.createHorizontalBox();
		timeEditedPanel.setOpaque(false);
		timeEditedPanel.add(timeLabel);
		timeEditedPanel.add(Box.createHorizontalStrut(10));
		timeEditedPanel.add(editedLabel);
		timeEditedPanel.add(Box.createHorizontalGlue());

		Box userTimePanel = Box.createVerticalBox();
		userTimePanel.setBorder(BorderFactory.createEmptyBorder());
		userTimePanel.setOpaque(false);
		userTimePanel.add(userPanel);
		userTimePanel.add(timeEditedPanel);

		Box controlPanel = Box.createHorizontalBox();
		controlPanel.setBorder(BorderFactory.createEmptyBorder(2, 5, 2, 5));
		controlPanel.setOpaque(false);
		controlPanel.add(userTimePanel);
		controlPanel.add(Box.createHorizontalGlue());
		controlPanel.add(Box.createHorizontalStrut(5));
		controlPanel.add(createSlideButton);
		controlPanel.add(Box.createHorizontalStrut(5));
		controlPanel.add(discardButton);

		textArea = new JTextArea();
		textArea.setOpaque(false);
		textArea.setEditable(false);
		textArea.setLineWrap(true);
		textArea.setWrapStyleWord(true);
		textArea.setFont(textArea.getFont().deriveFont(12f));

		content.add(controlPanel, BorderLayout.NORTH);
		content.add(textArea, BorderLayout.CENTER);

		content.addComponentListener(new ComponentAdapter() {

			@Override
			public void componentResized(ComponentEvent e) {
				SwingUtils.invoke(() -> {
					Insets insets = getInsets();
					Insets cInsets = content.getInsets();
					Dimension size = getPreferredSize();
					Dimension textSize = textArea.getPreferredSize();

					int height = controlPanel.getPreferredSize().height;
					height += textSize.height;
					height += insets.top + insets.bottom + cInsets.top + cInsets.bottom;

					setSize(new Dimension(size.width, height));
					setPreferredSize(new Dimension(size.width, height));
					setMaximumSize(new Dimension(Integer.MAX_VALUE, height));

					revalidate();
					repaint();
				});
			}
		});
	}

	protected Box createUserPanel() {
		final Box userPanel = Box.createHorizontalBox();

		userPanel.setOpaque(false);
		userPanel.add(userLabel);
		userPanel.add(privateLabel);
		userPanel.add(Box.createHorizontalGlue());

		return userPanel;
	}

	protected void initComponents() {
		discardButton = new JButton(AwtResourceLoader.getIcon("message-check.svg", 18));
		createSlideButton = new JButton(AwtResourceLoader.getIcon("message-slide.svg", 18));

		discardButton.setToolTipText(dict.get("button.processed"));
		createSlideButton.setToolTipText(dict.get("button.create.slide"));
	}
}

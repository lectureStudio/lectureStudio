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

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;

import javax.swing.AbstractButton;
import javax.swing.Icon;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextArea;
import javax.swing.border.EmptyBorder;

import org.lecturestudio.core.view.NotificationType;
import org.lecturestudio.swing.AwtResourceLoader;
import org.lecturestudio.swing.util.SwingUtils;

public class NotificationPane extends GlassPane {

	private static final Icon ERROR_ICON = AwtResourceLoader.getIcon("error.svg", 50);

	private static final Icon WARNING_ICON = AwtResourceLoader.getIcon("warning.svg", 50);

	private static final Icon QUESTION_ICON = AwtResourceLoader.getIcon("question.svg", 50);

	private NotificationType type;

	private JLabel iconLabel;

	private JLabel titleLabel;

	private JTextArea messageLabel;

	private JPanel contentContainer;

	private JPanel buttonContainer;


	public NotificationPane() {
		super();

		this.type = NotificationType.DEFAULT;

		initialize();
	}

	public void addButton(AbstractButton button) {
		buttonContainer.add(button);
	}

	public void removeButton(AbstractButton button) {
		buttonContainer.remove(button);
	}

	public void clearButtons() {
		buttonContainer.removeAll();
		buttonContainer.revalidate();
	}

	public NotificationType getType() {
		return type;
	}

	public void setType(NotificationType type) {
		this.type = type;

		updateType(type);
	}

	public void setIcon(Icon icon) {
		SwingUtils.invoke(() -> {
			iconLabel.setIcon(icon);
			iconLabel.setVisible(nonNull(icon));
		});
	}

	public void setTitle(String title) {
		SwingUtils.invoke(() -> {
			titleLabel.setText(title);
		});
	}

	public void setMessage(String message) {
		SwingUtils.invoke(() -> {
			boolean visible = nonNull(message) && !message.isEmpty() && !message.isBlank();

			messageLabel.setText(message);
			messageLabel.setVisible(visible);
		});
	}

	public void setContent(Component content) {
		contentContainer.add(content, BorderLayout.CENTER);
	}

	private void initialize() {
		JPanel container = new JPanel(new GridBagLayout());
		container.setBorder(new EmptyBorder(20, 20, 20, 20));

		GridBagConstraints constraints = new GridBagConstraints();
		constraints.gridx = 0;
		constraints.gridy = 0;
		constraints.gridheight = 2;
		constraints.anchor = GridBagConstraints.NORTH;
		constraints.insets = new Insets(0, 0, 0, 10);

		iconLabel = new JLabel();

		titleLabel = new JLabel();
		titleLabel.setFont(titleLabel.getFont().deriveFont(Font.BOLD));

		messageLabel = new JTextArea();
		messageLabel.setBorder(new EmptyBorder(0, 0, 0, 0));
		messageLabel.setEditable(false);
		messageLabel.setFocusable(false);
		messageLabel.setLineWrap(true);
		messageLabel.setWrapStyleWord(true);
		messageLabel.setOpaque(false);
		messageLabel.addComponentListener(new ComponentAdapter() {

			@Override
			public void componentResized(ComponentEvent e) {
				int height = messageLabel.getSize().height;

				messageLabel.setSize(new Dimension(500, height));
			}
		});

		contentContainer = new JPanel(new BorderLayout());

		buttonContainer = new JPanel(new FlowLayout(FlowLayout.RIGHT, 5, 0));

		container.add(iconLabel, constraints);

		constraints.gridx++;
		constraints.gridheight = 1;
		constraints.fill = GridBagConstraints.HORIZONTAL;
		constraints.insets = new Insets(0, 0, 0, 0);

		container.add(titleLabel, constraints);

		constraints.gridy++;
		constraints.insets = new Insets(10, 0, 15, 0);

		container.add(messageLabel, constraints);

		constraints.gridy++;
		constraints.insets = new Insets(0, 0, 0, 0);

		container.add(contentContainer, constraints);

		constraints.gridx = 0;
		constraints.gridy++;
		constraints.weightx = 1.0;
		constraints.gridwidth = 2;
		constraints.insets = new Insets(0, 0, 0, 0);
		constraints.fill = GridBagConstraints.HORIZONTAL;
		constraints.anchor = GridBagConstraints.EAST;

		container.add(buttonContainer, constraints);

		add(container);

		updateType(getType());
	}

	private void updateType(NotificationType type) {
		switch (type) {
			case DEFAULT:
				setIcon(null);
				break;

			case ERROR:
				setIcon(ERROR_ICON);
				break;

			case QUESTION:
				setIcon(QUESTION_ICON);
				break;

			case WARNING:
				setIcon(WARNING_ICON);
				break;

			default:
				break;
		}
	}
}

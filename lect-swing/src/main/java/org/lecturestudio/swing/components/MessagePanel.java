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
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Shape;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JPanel;

import org.lecturestudio.core.app.dictionary.Dictionary;
import org.lecturestudio.swing.border.RoundedBorder;

public abstract class MessagePanel extends JPanel {

	protected final Dictionary dict;

	protected JLabel fromLabel;

	protected JLabel timeLabel;

	protected JLabel privateLabel;


	abstract protected void createContent(JPanel content);


	public MessagePanel(Dictionary dict) {
		super();

		this.dict = dict;

		initialize();
	}

	public void setDate(ZonedDateTime date) {
		if (isNull(date)) {
			return;
		}

		ZonedDateTime dateUTC = date.withZoneSameInstant(ZoneId.systemDefault());
		String formattedDate = dateUTC.format(DateTimeFormatter.ofPattern("H:mm"));

		timeLabel.setText(formattedDate);
	}

	public void setUserName(String host) {
		fromLabel.setText(host);
	}

	public void pack() {
		setPreferredSize(new Dimension(getPreferredSize().width, getPreferredSize().height));
		setMaximumSize(new Dimension(getMaximumSize().width, getPreferredSize().height));
		setMinimumSize(new Dimension(200, getPreferredSize().height));
	}

	private void initialize() {
		setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
		setBorder(BorderFactory.createEmptyBorder(0, 0, 3, 0));

		JPanel content = new JPanel() {

			@Override
			protected void paintComponent(Graphics g) {
				if (getBorder() instanceof RoundedBorder) {
					g.setColor(getBackground());
					Shape borderShape = ((RoundedBorder) getBorder())
							.getBorderShape(getWidth(), getHeight());
					((Graphics2D) g).fill(borderShape);
				}

				super.paintComponent(g);
			}
		};
		content.setLayout(new BorderLayout(1, 1));
		content.setBackground(Color.WHITE);
		content.setBorder(new RoundedBorder(Color.LIGHT_GRAY, 5));
		content.setOpaque(false);

		fromLabel = new JLabel();

		timeLabel = new JLabel();
		timeLabel.setForeground(Color.BLUE);

		privateLabel = new JLabel();
		privateLabel.setForeground(Color.RED);
		privateLabel.setVisible(false);

		createContent(content);

		add(content);
	}
}

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

import static java.util.Objects.isNull;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.util.List;

import javax.swing.Box;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextArea;
import javax.swing.border.EmptyBorder;

public class Contributor extends JPanel {

	private JLabel nameLabel;

	private JTextArea firmLabel;

	private JPanel contributionPanel;


	public Contributor() {
		super();

		initialize();
	}

	public void setContributions(List<String> contributions) {
		if (isNull(contributions)) {
			return;
		}

		for (String contribution : contributions) {
			contributionPanel.add(new Badge(contribution));
		}
	}

	public String getName() {
		return nameLabel.getText();
	}

	public void setName(String name) {
		nameLabel.setText(name);
	}

	public String getFirm() {
		return firmLabel.getText();
	}

	public void setFirm(String firm) {
		firmLabel.setText(firm);
	}

	private void initialize() {
		Dimension size = new Dimension(200, 200);

		setLayout(new BorderLayout());
		//setBorder(new ShadowBorder(5, 5, 5, 5));
		setBackground(Color.white);
		setPreferredSize(size);
		setMaximumSize(size);

		nameLabel = new JLabel();
		nameLabel.setBorder(new EmptyBorder(10, 10, 10, 10));
		nameLabel.setFont(nameLabel.getFont().deriveFont(Font.BOLD));

		firmLabel = new JTextArea();
		firmLabel.setEditable(false);
		firmLabel.setFocusable(false);
		firmLabel.setOpaque(false);
		firmLabel.setLineWrap(true);
		firmLabel.setWrapStyleWord(true);

		Font firmFont = firmLabel.getFont();
		firmLabel.setFont(firmFont.deriveFont(firmFont.getSize2D() - 1.f));

		contributionPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 5));
		contributionPanel.setBorder(new EmptyBorder(0, 0, 5, 0));
		contributionPanel.setOpaque(false);

		Box content = Box.createVerticalBox();
		content.setBorder(new EmptyBorder(0, 5, 5, 5));
		content.add(contributionPanel);
		content.add(firmLabel);
		content.add(Box.createVerticalGlue());

		add(nameLabel, BorderLayout.NORTH);
		add(content, BorderLayout.CENTER);
	}
}

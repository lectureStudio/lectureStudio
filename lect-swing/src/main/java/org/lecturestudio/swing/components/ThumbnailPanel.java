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
import java.awt.Color;
import java.awt.FlowLayout;

import javax.swing.Box;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.MatteBorder;

public class ThumbnailPanel extends ThumbPanel {

	private final JPanel buttonContainer;


	public ThumbnailPanel() {
		super();

		buttonContainer = new JPanel();
		buttonContainer.setBorder(new CompoundBorder(
				new MatteBorder(1, 0, 0, 0, Color.LIGHT_GRAY),
				new EmptyBorder(5, 0, 5, 0)));
		buttonContainer.setLayout(new FlowLayout(FlowLayout.LEFT, 0, 0));
		buttonContainer.setOpaque(false);
	}

	public void addButton(JButton button) {
		int compCount = buttonContainer.getComponentCount();

		if (compCount == 0) {
			add(buttonContainer, BorderLayout.SOUTH);
		}
		else if (compCount > 0) {
			buttonContainer.add(Box.createHorizontalStrut(5));
		}

		buttonContainer.add(button);
	}
}

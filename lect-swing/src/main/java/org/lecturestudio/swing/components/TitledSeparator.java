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

import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;

import javax.swing.Box;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JSeparator;

public class TitledSeparator extends JComponent {

	private final JLabel label;

	private final Box.Filler gapFiller;


	public TitledSeparator() {
		super();

		label = new JLabel();

		Font font = label.getFont();
		label.setFont(font.deriveFont(Font.BOLD, font.getSize2D()));

		gapFiller = (Box.Filler) Box.createHorizontalStrut(5);

		initialize();
	}

	public int getGap() {
		return gapFiller.getWidth();
	}

	public void setGap(int width) {
		gapFiller.changeShape(new Dimension(width, 0), new Dimension(width, 0),
				new Dimension(width, Short.MAX_VALUE));
	}

	public void setText(String text) {
		label.setText(text);
	}

	public String getText() {
		return label.getText();
	}

	private void initialize() {
		setLayout(new GridBagLayout());

		GridBagConstraints c = new GridBagConstraints();
		c.gridx = 0;
		c.gridy = 0;

		add(label, c);

		c.gridx++;

		add(gapFiller, c);

		c.gridx++;
		c.weightx = 1;
		c.fill = GridBagConstraints.HORIZONTAL;

		add(new JSeparator(), c);
	}
}

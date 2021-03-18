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

package org.lecturestudio.swing.list;

import java.awt.Component;
import java.awt.Font;

import javax.swing.DefaultListCellRenderer;
import javax.swing.JLabel;
import javax.swing.JList;

public class FontCellRenderer extends DefaultListCellRenderer {

	private static final int FONT_SIZE = 18;


	@Override
	public Component getListCellRendererComponent(JList<?> list, Object value,
			int index, boolean isSelected, boolean cellHasFocus) {
		JLabel label = (JLabel) super.getListCellRendererComponent(
				list, value, index, isSelected, cellHasFocus);

		Font labelFont = new Font((String) value, Font.PLAIN, FONT_SIZE);

		if (labelFont.canDisplayUpTo((String) value) != -1) {
			String fontName = label.getFont().getFontName();
			labelFont = new Font(fontName, Font.PLAIN, FONT_SIZE);
			label.setFont(labelFont);
		}

		label.setFont(labelFont);
		//label.setPreferredSize(new Dimension(100, 20));

		return label;
	}

}

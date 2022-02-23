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

import javax.swing.*;
import javax.swing.border.EmptyBorder;

import org.lecturestudio.swing.ui.VerticalLabelUI;

public class VerticalTab extends JLabel {

	public VerticalTab(int tabPlacement) {
		super();

		setUI(new VerticalLabelUI(tabPlacement));
		setBorder(new EmptyBorder(0, 10, 0, 10));
	}

	public boolean isClockwise() {
		return ((VerticalLabelUI) getUI()).isClockwise();
	}

	public static VerticalTab fromJLabel(JLabel label, int tabPlacement) {
		return fromText(label.getText(), tabPlacement, label.getIcon());
	}

	public static VerticalTab fromText(String text, int tabPlacement) {
		return fromText(text, tabPlacement, null);
	}

	public static VerticalTab fromText(String text, int tabPlacement, Icon icon) {
		final VerticalTab tab = new VerticalTab(tabPlacement);
		tab.setText(text);
		tab.setIcon(icon);
		return tab;
	}
}

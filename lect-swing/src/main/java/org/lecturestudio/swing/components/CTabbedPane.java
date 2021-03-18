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

import java.awt.Component;
import java.awt.Insets;

import javax.swing.JTabbedPane;
import javax.swing.UIManager;

public class CTabbedPane extends JTabbedPane {

	private static final long serialVersionUID = 8040497774731440213L;


	public CTabbedPane(int tabPlacement) {
		super(tabPlacement);
	}

	public static CTabbedPane createInstance(int tabPlacement) {
		switch (tabPlacement) {
			case JTabbedPane.LEFT:
			case JTabbedPane.RIGHT:
				Object textIconGap = UIManager.get("TabbedPane.textIconGap");
				Insets tabInsets = UIManager.getInsets("TabbedPane.tabInsets");

				UIManager.put("TabbedPane.textIconGap", 1);
				UIManager.put("TabbedPane.tabInsets", new Insets(
						tabInsets.left, tabInsets.right, tabInsets.right, tabInsets.left));

				CTabbedPane tabPane = new CTabbedPane(tabPlacement);

				UIManager.put("TabbedPane.textIconGap", textIconGap);
				UIManager.put("TabbedPane.tabInsets", tabInsets);

				return tabPane;
			default:
				return new CTabbedPane(tabPlacement);
		}
	}

	@Override
	public void addTab(String text, Component comp) {
		int tabPlacement = getTabPlacement();

		switch (tabPlacement) {
			case JTabbedPane.LEFT:
			case JTabbedPane.RIGHT:
				VerticalTextIcon icon = new VerticalTextIcon(text, tabPlacement);
				super.addTab(null, icon, comp);
				return;
			default:
				super.addTab(text, null, comp);
		}
	}

}

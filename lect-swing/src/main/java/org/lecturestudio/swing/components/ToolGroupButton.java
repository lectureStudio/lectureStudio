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

import javax.swing.Icon;
import javax.swing.JToggleButton;

import org.lecturestudio.core.tool.ToolType;

public class ToolGroupButton extends JToggleButton {

	private ToolType toolType;

	private Icon copyIcon;

	private Icon selectIcon;

	private Icon selectGroupIcon;


	public ToolGroupButton() {
		super();

		initialize();
	}

	public void setCopyIcon(Icon icon) {
		copyIcon = icon;
	}

	public void setSelectIcon(Icon icon) {
		selectIcon = icon;
	}

	public void setSelectGroupIcon(Icon icon) {
		selectGroupIcon = icon;
	}

	public void selectToolType(ToolType type) {
		this.toolType = type;

		if (type == ToolType.SELECT) {
			setIcon(selectIcon);
		}
		else if (type == ToolType.SELECT_GROUP) {
			setIcon(selectGroupIcon);
		}
		else if (type == ToolType.CLONE) {
			setIcon(copyIcon);
		}
	}

	private void initialize() {
	}

}

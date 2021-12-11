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

import javax.swing.JButton;

import org.lecturestudio.core.app.dictionary.Dictionary;
import org.lecturestudio.core.view.Action;
import org.lecturestudio.swing.util.SwingUtils;

public class WhiteboardThumbnailPanel extends ThumbnailPanel {

	private final JButton addPageButton;

	private final JButton removePageButton;


	public WhiteboardThumbnailPanel(Dictionary dict) {
		super();

		addPageButton = new JButton("+");
		removePageButton = new JButton("-");

		addButton(addPageButton);
		addButton(removePageButton);
	}

	public void setOnAddPage(Action action) {
		SwingUtils.bindAction(addPageButton, action);
	}

	public void setOnRemovePage(Action action) {
		SwingUtils.bindAction(removePageButton, action);
	}
}

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

import org.lecturestudio.core.view.Action;
import org.lecturestudio.swing.util.SwingUtils;

import javax.swing.*;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.MatteBorder;
import java.awt.*;

public class EditableThumbnailPanel extends ThumbPanel {

	private final JButton addPageButton;

	private final JButton deletePageButton;

	protected final JPanel container;

	public EditableThumbnailPanel() {
		super();

		addPageButton = new JButton("+");
		deletePageButton = new JButton("-");

		container = new JPanel();
		container.setBorder(new CompoundBorder(
				new MatteBorder(1, 0, 0, 0, Color.LIGHT_GRAY),
				new EmptyBorder(5, 0, 5, 0)));
		container.setLayout(new FlowLayout(FlowLayout.LEFT, 0, 0));
		container.setOpaque(false);
		container.add(addPageButton);
		container.add(Box.createHorizontalStrut(5));
		container.add(deletePageButton);

		add(container, BorderLayout.SOUTH);
	}

	public void setOnNewPage(Action action) {
		SwingUtils.bindAction(addPageButton, action);
	}

	public void setOnDeletePage(Action action) {
		SwingUtils.bindAction(deletePageButton, action);
	}
}

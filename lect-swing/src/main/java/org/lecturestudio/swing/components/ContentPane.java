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
import java.awt.Component;
import java.awt.Font;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;

public class ContentPane extends JPanel {

	private JLabel titleLabel;


	public ContentPane() {
		super();

		initialize();
	}

	@Override
	public void setName(String name) {
		super.setName(name);

		setTitle(name);
	}

	public String getTitle() {
		return titleLabel.getText();
	}

	public void setTitle(String value) {
		titleLabel.setText(value);
	}

	/**
	 * The content to show within the main content area. The content can be any
	 * Component such as UI controls or groups of components added to a layout
	 * container.
	 *
	 * @param content the content component.
	 */
	public final void setContent(Component content) {
		add(content, BorderLayout.CENTER);
	}

	/**
	 * The content component associated with this pane.
	 *
	 * @return The component associated with this pane.
	 */
	public final Component getContent() {
		BorderLayout layout = (BorderLayout) getLayout();

		return layout.getLayoutComponent(BorderLayout.CENTER);
	}

	private void initialize() {
		setLayout(new ContentLayout());

		titleLabel = new JLabel();
		titleLabel.setBorder(new EmptyBorder(20, 20, 20, 20));

		Font font = titleLabel.getFont();
		titleLabel.setFont(font.deriveFont(font.getSize2D() * 1.5f));

		add(titleLabel, BorderLayout.NORTH);
	}



	private static class ContentLayout extends BorderLayout {

		@Override
		public void addLayoutComponent(Component comp, Object constraints) {
			if (isNull(constraints)) {
				super.addLayoutComponent(comp, BorderLayout.CENTER);
			}
			else {
				super.addLayoutComponent(comp, constraints);
			}
		}

	}
}

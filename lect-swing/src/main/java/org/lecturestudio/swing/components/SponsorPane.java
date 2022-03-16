/*
 * Copyright (C) 2022 TU Darmstadt, Department of Computer Science,
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

import java.awt.Color;
import java.awt.Cursor;
import java.awt.Desktop;
import java.awt.Font;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.net.URI;

import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;

import org.lecturestudio.swing.AwtResourceLoader;

public class SponsorPane extends JPanel {

	private JLabel organizationLabel;

	private JLabel linkLabel;

	private JLabel imageLabel;


	public SponsorPane() {
		super();

		initialize();
	}

	public void setOrganization(String organization) {
		organizationLabel.setText(organization);
	}

	public void setOrganizationLink(String name, String url) {
		linkLabel.setText(name);
		linkLabel.setName(url);
	}

	public void setOrganizationImage(String path) {
		imageLabel.setIcon(AwtResourceLoader.getIcon(path, 50));
	}

	private void initialize() {
		MouseListener clickListener = new MouseAdapter() {

			@Override
			public void mouseClicked(MouseEvent event) {
				JLabel label = (JLabel) event.getSource();
				try {
					Desktop.getDesktop().browse(new URI(label.getName()));
				}
				catch (Exception e) {
					// Ignore.
				}
			}
		};

		setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));

		organizationLabel = new JLabel();
		organizationLabel.setBorder(new EmptyBorder(0, 0, 3, 0));
		organizationLabel.setFont(organizationLabel.getFont().deriveFont(Font.BOLD));

		linkLabel = new JLabel();
		linkLabel.setBorder(new EmptyBorder(0, 0, 3, 0));
		linkLabel.setForeground(Color.BLUE.darker());
		linkLabel.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
		linkLabel.addMouseListener(clickListener);

		imageLabel = new JLabel();
		imageLabel.setBorder(new EmptyBorder(0, 0, 20, 0));

		add(organizationLabel);
		add(linkLabel);
		add(imageLabel);
	}
}

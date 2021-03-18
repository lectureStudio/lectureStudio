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

package org.lecturestudio.presenter.swing.view;

import java.awt.Container;
import java.awt.FlowLayout;
import java.awt.Font;
import java.util.AbstractMap.SimpleEntry;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.Properties;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JTable;

import org.lecturestudio.core.model.Contributor;
import org.lecturestudio.core.model.Sponsor;
import org.lecturestudio.core.view.AboutView;
import org.lecturestudio.core.view.Action;
import org.lecturestudio.presenter.swing.view.model.SystemPropertyTableModel;
import org.lecturestudio.swing.components.ContentPane;
import org.lecturestudio.swing.layout.WrapFlowLayout;
import org.lecturestudio.swing.util.SwingUtils;
import org.lecturestudio.swing.view.SwingView;
import org.lecturestudio.swing.view.ViewPostConstruct;

@SwingView(name = "about")
public class SwingAboutView extends ContentPane implements AboutView {

	private JTable systemPropertiesTable;

	private Container contributorPane;

	private Container sponsorPane;

	private JButton closeButton;


	public SwingAboutView() {
		super();
	}

	@Override
	public void setContributors(List<Contributor> contributors) {
		SwingUtils.invoke(() -> {
			for (Contributor contributor : contributors) {
				org.lecturestudio.swing.components.Contributor c = new org.lecturestudio.swing.components.Contributor();
				c.setFirm(contributor.firm);
				c.setName(contributor.name);
				c.setContributions(contributor.contributions);

				contributorPane.add(c);
			}
		});
	}

	@Override
	public void setSponsors(List<Sponsor> sponsors) {
		SwingUtils.invoke(() -> {
			for (Sponsor sponsor : sponsors) {
				JLabel label = new JLabel(sponsor.name);
				label.setFont(label.getFont().deriveFont(Font.BOLD));

				sponsorPane.add(label);
			}
		});
	}

	@Override
	public void setProperties(Properties properties) {
		SwingUtils.invoke(() -> {
			List<SimpleEntry<String, String>> entries = new ArrayList<>();
			Enumeration<?> enumeration = properties.propertyNames();

			while (enumeration.hasMoreElements()) {
				String key = (String) enumeration.nextElement();
				String value = (String) properties.get(key);

				entries.add(new SimpleEntry<>(key, value));
			}

			SwingUtils.invoke(() -> {
				SystemPropertyTableModel model = (SystemPropertyTableModel) systemPropertiesTable.getModel();
				model.setItems(entries);
			});
		});
	}

	@Override
	public void setOnClose(Action action) {
		SwingUtils.bindAction(closeButton, action);
	}

	@ViewPostConstruct
	private void initialize() {
		contributorPane.setLayout(new WrapFlowLayout(FlowLayout.LEFT, 10, 10));
		sponsorPane.setLayout(new WrapFlowLayout(FlowLayout.LEFT, 10, 10));

		systemPropertiesTable.setModel(new SystemPropertyTableModel(
				systemPropertiesTable.getColumnModel()));
	}
}

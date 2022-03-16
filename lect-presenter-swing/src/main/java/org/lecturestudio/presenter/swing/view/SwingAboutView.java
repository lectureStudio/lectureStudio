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
import java.awt.Cursor;
import java.awt.Desktop;
import java.awt.FlowLayout;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.net.URI;
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
import org.lecturestudio.swing.components.SponsorPane;
import org.lecturestudio.swing.layout.WrapFlowLayout;
import org.lecturestudio.swing.util.SwingUtils;
import org.lecturestudio.swing.view.SwingView;
import org.lecturestudio.swing.view.ViewPostConstruct;

@SwingView(name = "about")
public class SwingAboutView extends ContentPane implements AboutView {

	private JLabel versionLabel;

	private JLabel buildDateLabel;

	private JLabel websiteLabel;

	private JLabel issueLabel;

	private JTable systemPropertiesTable;

	private Container contributorPane;

	private Container sponsorsPane;

	private JButton closeButton;


	public SwingAboutView() {
		super();
	}

	@Override
	public void setAppName(String name) {
		SwingUtils.invoke(() -> {
			setTitle(getTitle() + " " + name);
		});
	}

	@Override
	public void setAppVersion(String version) {
		SwingUtils.invoke(() -> {
			versionLabel.setText(version);
		});
	}

	@Override
	public void setAppBuildDate(String date) {
		SwingUtils.invoke(() -> {
			buildDateLabel.setText(date);
		});
	}

	@Override
	public void setWebsite(String website) {
		SwingUtils.invoke(() -> {
			websiteLabel.setName(website);
			websiteLabel.setText(website);
		});
	}

	@Override
	public void setIssueWebsite(String website) {
		SwingUtils.invoke(() -> {
			issueLabel.setName(website);
		});
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
				SponsorPane pane = new SponsorPane();
				pane.setOrganization(sponsor.organization);
				pane.setOrganizationLink(sponsor.link.name, sponsor.link.url);
				pane.setOrganizationImage(sponsor.logo);

				sponsorsPane.add(pane);
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

		websiteLabel.setCursor(new Cursor(Cursor.HAND_CURSOR));
		websiteLabel.addMouseListener(clickListener);
		issueLabel.setCursor(new Cursor(Cursor.HAND_CURSOR));
		issueLabel.addMouseListener(clickListener);

		contributorPane.setLayout(new WrapFlowLayout(FlowLayout.LEFT, 10, 10));

		systemPropertiesTable.setModel(new SystemPropertyTableModel(
				systemPropertiesTable.getColumnModel()));
	}
}
